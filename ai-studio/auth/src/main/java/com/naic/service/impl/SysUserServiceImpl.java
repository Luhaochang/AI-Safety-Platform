package com.naic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.naic.annotation.DataScope;
import com.naic.api.api.Assert;
import com.naic.api.api.ServiceException;
import com.naic.api.service.SmsService;
import com.naic.api.utils.Condition;
import com.naic.api.utils.SpringUtils;
import com.naic.common.interceptor.LoginIdHolder;
import com.naic.constant.UserConstants;
import com.naic.domain.entity.cnv.SysDeptCnv;
import com.naic.domain.entity.cnv.SysRoleCnv;
import com.naic.domain.entity.cnv.SysUserCnv;
import com.naic.domain.entity.dto.SysUserDTO;
import com.naic.domain.entity.po.*;
import com.naic.domain.entity.vo.*;
import com.naic.mapper.*;
import com.naic.service.ISysUserService;
import com.naic.utils.StringUtils;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.validation.Validator;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户 业务层处理
 * 
 * @author wangyunong
 */
@Service
@AllArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUserPO> implements ISysUserService
{
    private static final Logger log = LoggerFactory.getLogger(SysUserServiceImpl.class);

    private final SysUserMapper userMapper;

    private final SysRoleMapper roleMapper;

    private final SysPostMapper postMapper;

    private final SysUserRoleMapper userRoleMapper;

    private final SysUserPostMapper userPostMapper;

    protected final Validator validator;

    private final SysUserCnv sysUserCnv;

    private final SmsService smsService;

    private final SysDeptMapper deptMapper;

    private final SysDeptCnv deptCnv;

    private final SysRoleCnv roleCnv;
    /**
     * 根据条件分页查询用户列表
     * 
     * @param condition 条件查询
     * @return 用户信息集合信息
     */
    @Override
    public List<SysUserVO> selectUserList(Map<String, Object> condition)
    {
        List<SysUserPO> list=userMapper.selectList(Condition.getQueryWrapper(condition,SysUserPO.class));
        List<SysUserVO> voList=new ArrayList<>();
        for(SysUserPO po:list){
            SysUserVO vo=sysUserCnv.poToVo(po);
            if (StringUtils.isNotNull(vo.getDeptId())) {
                vo.setDept(deptCnv.poToVo(deptMapper.selectById(vo.getDeptId())));
            }
            voList.add(vo);
        }
        return voList;
    }

    /**
     * 根据条件分页查询已分配用户角色列表
     * 
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    public List<SysUserVO> selectAllocatedList(SysUserDTO user)
    {
        return userMapper.selectAllocatedList(user);
    }

    /**
     * 根据条件分页查询未分配用户角色列表
     * 
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    public List<SysUserVO> selectUnallocatedList(SysUserDTO user)
    {
        return userMapper.selectUnallocatedList(user);
    }

    /**
     * 通过用户名查询用户
     * 
     * @param userName 用户名
     * @return 用户对象信息
     */
    @Override
    public SysUserVO selectUserByUserName(String userName)
    {
        List<SysUserPO> list=userMapper.selectList(new QueryWrapper<SysUserPO>().eq("user_name",userName));
        if (list.isEmpty()){
            throw new ServiceException("未匹配到用户");
        }
        return sysUserCnv.poToVo(list.get(0));
    }

    /**
     * 通过用户ID查询用户
     * 
     * @param userId 用户ID
     * @return 用户对象信息
     */
    @Override
    public SysUserVO selectUserById(Long userId)
    {
        List<SysUserPO> list=userMapper.selectList(new QueryWrapper<SysUserPO>().eq("user_id",userId));
        if (list.isEmpty()){
            throw new ServiceException("未匹配到用户");
        }
        SysUserVO user=sysUserCnv.poToVo(list.get(0));
        List<SysUserRolePO> userRoleList=userRoleMapper.selectList(new QueryWrapper<SysUserRolePO>().eq("user_id",user.getUserId()));
        List<SysRoleVO> roleList=new ArrayList<>();
        for(SysUserRolePO po:userRoleList){
            SysRolePO role= roleMapper.selectById(po.getRoleId());
            roleList.add(roleCnv.poToVo(role));
        }
        user.setRoles(roleList);
        return user;
    }

    /**
     * 查询用户所属角色组
     * 
     * @param userName 用户名
     * @return 结果
     */
    @Override
    public String selectUserRoleGroup(String userName)
    {
        List<SysRolePO> list = roleMapper.selectRolesByUserName(userName);
        if (CollectionUtils.isEmpty(list))
        {
            return StringUtils.EMPTY;
        }
        return list.stream().map(SysRolePO::getRoleName).collect(Collectors.joining(","));
    }

    /**
     * 查询用户所属岗位组
     * 
     * @param userName 用户名
     * @return 结果
     */
    @Override
    public String selectUserPostGroup(String userName)
    {
        List<SysPostPO> list = postMapper.selectPostsByUserName(userName);
        if (CollectionUtils.isEmpty(list))
        {
            return StringUtils.EMPTY;
        }
        return list.stream().map(SysPostPO::getPostName).collect(Collectors.joining(","));
    }

    /**
     * 校验用户名称是否唯一
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public boolean checkUserNameUnique(SysUserDTO user)
    {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        List<SysUserPO> list=userMapper.selectList(new QueryWrapper<SysUserPO>().eq("user_name",user.getUserName()).eq("del_flag",0));
        if (list.isEmpty()){
            return UserConstants.UNIQUE;
        }
        SysUserPO info = list.get(0);
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验手机号码是否唯一
     *
     * @param user 用户信息
     * @return
     */
    @Override
    public boolean checkPhoneUnique(SysUserDTO user)
    {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        List<SysUserPO> list=userMapper.selectList(new QueryWrapper<SysUserPO>().eq("phone_number",user.getPhoneNumber()).eq("del_flag",0));
        if(list.isEmpty()){
            return UserConstants.UNIQUE;
        }
        SysUserPO info = list.get(0);
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验email是否唯一
     *
     * @param user 用户信息
     * @return
     */
    @Override
    public boolean checkEmailUnique(SysUserDTO user)
    {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        List<SysUserPO> list=userMapper.selectList(new QueryWrapper<SysUserPO>().eq("email",user.getEmail()).eq("del_flag",0));
        if (list.isEmpty()){
            return UserConstants.UNIQUE;
        }
        SysUserPO info = list.get(0);
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验用户是否允许操作
     * 
     * @param user 用户信息
     */
    @Override
    public void checkUserAllowed(SysUserDTO user)
    {
        if (SpringUtils.isAdmin(user.getUserId()))
        {
            throw new ServiceException("不允许操作超级管理员用户");
        }
    }

    /**
     * 校验用户是否有数据权限
     * 
     * @param userId 用户id
     */
    @Override
    public void checkUserDataScope(Long userId)
    {
        if (!LoginIdHolder.isAdmin())
        {
            Map<String, Object> condition=new HashMap<>();
            if (userId!=null) {
                condition.put("userIdEq", userId);
            }
            List<SysUserVO> users =this.selectUserList(condition);
            if (StringUtils.isEmpty(users))
            {
                throw new ServiceException("没有权限访问用户数据！");
            }
        }
    }

    /**
     * 新增保存用户信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public int insertUser(SysUserDTO user)
    {
        SysUserPO po= sysUserCnv.dtoToPo(user);
        // 新增用户信息
        int rows = userMapper.insert(po);

        user.setUserId(po.getUserId());
        // 新增用户岗位关联
        insertUserPost(user);
        // 新增用户与角色管理
        insertUserRole(user);
        return rows;
    }

    /**
     * 注册用户信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public boolean registerUser(SysUserDTO user)
    {
        SysUserPO po= sysUserCnv.dtoToPo(user);
        return userMapper.insert(po) > 0;
    }

    /**
     * 修改保存用户信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public int updateUser(SysUserDTO user)
    {
        SysUserPO po= sysUserCnv.dtoToPo(user);
        Long userId = user.getUserId();
        // 删除用户与角色关联
        userRoleMapper.deleteUserRoleByUserId(userId);
        // 新增用户与角色管理
        insertUserRole(user);
        // 删除用户与岗位关联
        userPostMapper.deleteUserPostByUserId(userId);
        // 新增用户与岗位管理
        insertUserPost(user);
        return userMapper.updateById(po);
    }

    /**
     * 用户授权角色
     * 
     * @param userId 用户ID
     * @param roleIds 角色组
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void insertUserAuth(Long userId, Long[] roleIds)
    {
        userRoleMapper.deleteUserRoleByUserId(userId);
        insertUserRole(userId, roleIds);
    }

    /**
     * 修改用户状态
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUserStatus(SysUserDTO user)
    {
        SysUserPO po= sysUserCnv.dtoToPo(user);
        return userMapper.updateById(po);
    }

    /**
     * 修改用户基本信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUserProfile(SysUserDTO user)
    {
        SysUserPO po= sysUserCnv.dtoToPo(user);
        return userMapper.updateById(po);
    }

    /**
     * 修改用户头像
     * 
     * @param userName 用户名
     * @param avatar 头像地址
     * @return 结果
     */
    @Override
    public boolean updateUserAvatar(String userName, String avatar)
    {
        SysUserPO po=userMapper.selectList(new QueryWrapper<SysUserPO>().eq("user_name",userName)).get(0);
        po.setAvatar(avatar);
        return userMapper.updateById(po) > 0;
    }

    /**
     * 重置用户密码
     * 
     * @param map info
     * @return 结果
     */
    @Override
    public int resetPwd(Map<String, String> map)
    {
        String idStr = map.get("id");
        String newPassword = map.get("newPassword");
        String oldPassword = map.get("oldPassword");
        long id = Long.parseLong(idStr);
        SysUserPO user=userMapper.selectById(id);
        if (user == null) {
            Assert.fail("用户不存在");
        }
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        if (user.getPassword() != null && !encoder.matches(oldPassword, user.getPassword())) {
            Assert.fail("旧密码错误");
        }
        if (com.naic.api.utils.SpringUtils.isEmpty(idStr) || SpringUtils.isEmpty(newPassword)) {
            Assert.fail("请填写完整信息");
        }
        this.checkUserDataScope(user.getUserId());
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return userMapper.updateById(new SysUserPO().setUserId(id).setPassword(passwordEncoder.encode(newPassword)));
    }

    /**
     * 重置用户密码
     * 
     * @param userName 用户名
     * @param password 密码
     * @return 结果
     */
    @Override
    public int resetUserPwd(String userName, String password)
    {
        SysUserPO po=userMapper.selectList(new QueryWrapper<SysUserPO>().eq("user_name",userName)).get(0);
        po.setPassword(password);
        return userMapper.updateById(po);
    }

    /**
     * 新增用户角色信息
     * 
     * @param user 用户对象
     */
    public void insertUserRole(SysUserDTO user)
    {
        this.insertUserRole(user.getUserId(), user.getRoleIds());
    }

    /**
     * 新增用户岗位信息
     * 
     * @param user 用户对象
     */
    public void insertUserPost(SysUserDTO user)
    {
        Long[] posts = user.getPostIds();
        if (StringUtils.isNotEmpty(posts))
        {
            // 新增用户与岗位管理
            List<SysUserPostPO> list = new ArrayList<SysUserPostPO>(posts.length);
            for (Long postId : posts)
            {
                SysUserPostPO up = new SysUserPostPO();
                up.setUserId(user.getUserId());
                up.setPostId(postId);
                list.add(up);
            }
            userPostMapper.batchUserPost(list);
        }
    }

    /**
     * 新增用户角色信息
     * 
     * @param userId 用户ID
     * @param roleIds 角色组
     */
    public void insertUserRole(Long userId, Long[] roleIds)
    {
        if (StringUtils.isNotEmpty(roleIds))
        {
            // 新增用户与角色管理
            List<SysUserRolePO> list = new ArrayList<SysUserRolePO>(roleIds.length);
            for (Long roleId : roleIds)
            {
                SysUserRolePO ur = new SysUserRolePO();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                list.add(ur);
            }
            userRoleMapper.batchUserRole(list);
        }
    }

    /**
     * 通过用户ID删除用户
     * 
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public int deleteUserById(Long userId)
    {
        // 删除用户与角色关联
        userRoleMapper.deleteUserRoleByUserId(userId);
        // 删除用户与岗位表
        userPostMapper.deleteUserPostByUserId(userId);
        SysUserPO po=userMapper.selectById(userId);
        po.setDelFlag("1");
        return userMapper.updateById(po);
    }

    /**
     * 批量删除用户信息
     * 
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public int deleteUserByIds(Long[] userIds)
    {
        for (Long userId : userIds)
        {
            SysUserDTO user=new SysUserDTO();
            user.setUserId(userId);
            checkUserAllowed(user);
            checkUserDataScope(userId);
        }
        // 删除用户与角色关联
        userRoleMapper.deleteUserRole(userIds);
        // 删除用户与岗位关联
        userPostMapper.deleteUserPost(userIds);
        return userMapper.deleteUserByIds(userIds);
    }

    @Override
    public void setPassword(Map<String, String> map) {
        String tel = map.get("tel");
        String newPassword = map.get("newPassword");
        String code = map.get("code");
        if (org.springframework.util.StringUtils.isEmpty(tel) || org.springframework.util.StringUtils.isEmpty(newPassword) || org.springframework.util.StringUtils.isEmpty(code)) {
            Assert.fail("请填写完整信息");
        }
        SysUserPO user = userMapper.selectOne(Wrappers.<SysUserPO>lambdaQuery().eq(SysUserPO::getPhoneNumber, tel));
        if (user == null) {
            Assert.fail("用户不存在");
        }
        if (!smsService.check(user.getPhoneNumber(), code)) {
            Assert.fail("验证码错误");
        }
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        userMapper.updateById(new SysUserPO().setUserId(user.getUserId()).setPassword(passwordEncoder.encode(newPassword)));
    }

    @Override
    public void register(Map<String, String> map) {
        String userName = map.get("userName");
        String password = map.get("password");
        String tel = map.get("tel");
        String code = map.get("code");
        String nickName=map.get("nickName");
        if (org.springframework.util.StringUtils.isEmpty(userName)
                || org.springframework.util.StringUtils.isEmpty(password)
                || org.springframework.util.StringUtils.isEmpty(tel)
                || org.springframework.util.StringUtils.isEmpty(code)
                || org.springframework.util.StringUtils.isEmpty(nickName)) {
            Assert.fail("请填写完整必要的信息");
        }
        SysUserPO cur = userMapper.selectOne(Wrappers.<SysUserPO>lambdaQuery().eq(SysUserPO::getUserName, userName));
        if (cur != null) {
            Assert.fail("用户名已存在");
        }
        cur = userMapper.selectOne(Wrappers.<SysUserPO>lambdaQuery().eq(SysUserPO::getPhoneNumber, tel));
        if (cur != null) {
            Assert.fail("电话号码已经注册过");
        }
        // 校验验证码
        if (!smsService.check(tel, code)) {
            Assert.fail("验证码错误或已失效");
        }
        SysUserPO user = new SysUserPO().setUserName(userName)
                .setPassword(new BCryptPasswordEncoder().encode(password))
                .setPhoneNumber(tel)
                .setNickName(nickName);
        userMapper.insert(user);
    }

}
