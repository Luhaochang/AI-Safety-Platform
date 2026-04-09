package com.naic.api.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.naic.api.constant.AuthorizationConstant;
import com.naic.api.utils.ObjectUtils;
import lombok.AllArgsConstructor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * des:
 *
 * @author HuZhenSha
 * @date 2021/3/19 20:41
 */

@Component
@AllArgsConstructor
public class EntityMetaObjectHandler implements MetaObjectHandler {
    private static final String UPDATE_TIME = "updateTime";
    private static final String CREATE_TIME = "createTime";
    private static final String UPDATE_USER = "updateUser";
    private static final String CREATE_USER = "createUser";

    private final HttpServletRequest request;

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime date = LocalDateTime.now();
        if(metaObject.hasSetter(UPDATE_TIME)) {
            if (ObjectUtils.isEmpty(getFieldValByName(UPDATE_TIME, metaObject))) {
                if (LocalDateTime.class.isAssignableFrom(metaObject.getSetterType(UPDATE_TIME))) {
                    setFieldValByName(UPDATE_TIME, date, metaObject);
                } else {
                    setFieldValByName(UPDATE_TIME, new Date(), metaObject);
                }
            }
        }
        if(metaObject.hasSetter(CREATE_TIME)) {
            if (ObjectUtils.isEmpty(getFieldValByName(CREATE_TIME, metaObject))) {
                if (LocalDateTime.class.isAssignableFrom(metaObject.getSetterType(CREATE_TIME))) {
                    setFieldValByName(CREATE_TIME, date, metaObject);
                } else {
                    setFieldValByName(CREATE_TIME, new Date(), metaObject);
                }
            }
        }
        try {
            String header = request.getHeader(AuthorizationConstant.LOGIN_USER_ID_HEADER);
            if (header != null) {
                Long userId = Long.parseLong(header);
                if (metaObject.hasSetter(CREATE_USER)) {
                    Object oldCreateUser = getFieldValByName(CREATE_USER, metaObject);
                    if (ObjectUtils.isEmpty(oldCreateUser) || ((Long) oldCreateUser) <= 0) {
                        setFieldValByName(CREATE_USER, userId, metaObject);
                    }
                }
                if (metaObject.hasSetter(UPDATE_USER)) {
                    Object oldUpdateUser = getFieldValByName(UPDATE_USER, metaObject);
                    if (ObjectUtils.isEmpty(oldUpdateUser) || ((Long) oldUpdateUser) <= 0) {
                        setFieldValByName(UPDATE_USER, userId, metaObject);
                    }
                }
            }
        }catch (Exception e){

        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if(metaObject.hasSetter(UPDATE_TIME)) {
            if (ObjectUtils.isEmpty(getFieldValByName(UPDATE_TIME, metaObject))) {
                if (LocalDateTime.class.isAssignableFrom(metaObject.getSetterType(UPDATE_TIME))) {
                    setFieldValByName(UPDATE_TIME, LocalDateTime.now(), metaObject);
                } else {
                    setFieldValByName(UPDATE_TIME, new Date(), metaObject);
                }
            }
        }
        try {
            String header = request.getHeader(AuthorizationConstant.LOGIN_USER_ID_HEADER);
            if (header != null) {
                Long userId = Long.parseLong(header);
                if (metaObject.hasSetter(UPDATE_USER)) {
                    Object oldUpdateUser = getFieldValByName(UPDATE_USER, metaObject);
                    if (ObjectUtils.isEmpty(oldUpdateUser) || ((Long) oldUpdateUser) <= 0) {
                        setFieldValByName(UPDATE_USER, userId, metaObject);
                    }
                }
            }
        }catch (Exception e){

        }
    }

}
