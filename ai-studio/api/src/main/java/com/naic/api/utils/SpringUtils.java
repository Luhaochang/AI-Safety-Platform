package com.naic.api.utils;

import com.naic.api.constant.StringPool;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author wangyunong
 * @since 2024/4/10
 */
@Component
public class SpringUtils extends org.springframework.util.StringUtils implements ApplicationContextAware, EnvironmentAware {

    private volatile static ApplicationContext context;
    private volatile static Environment environment;

    private static Binder binder;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtils.context = applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        SpringUtils.environment = environment;
        binder = Binder.get(environment);
    }

    public static ApplicationContext getContext() {
        return context;
    }

    /**
     * 获取bean
     *
     * @param bean
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> bean) {
        return context.getBean(bean);
    }

    public static <T> T getBeanOrNull(Class<T> bean) {
        try {
            return context.getBean(bean);
        } catch (Exception e) {
            return null;
        }
    }

    public static Object getBean(String beanName) {
        return context.getBean(beanName);
    }

    public static Object getBeanOrNull(String beanName) {
        try {
            return context.getBean(beanName);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取配置
     *
     * @param key
     * @return
     */
    public static String getConfig(String key) {
        return environment.getProperty(key);
    }

    public static String getConfigOrElse(String mainKey, String slaveKey) {
        String ans = environment.getProperty(mainKey);
        if (ans == null) {
            return environment.getProperty(slaveKey);
        }
        return ans;
    }

    /**
     * 获取配置
     *
     * @param key
     * @param val 配置不存在时的默认值
     * @return
     */
    public static String getConfig(String key, String val) {
        return environment.getProperty(key, val);
    }

    /**
     * 发布事件消息
     *
     * @param event
     */
    public static void publishEvent(ApplicationEvent event) {
        context.publishEvent(event);
    }


    /**
     * 配置绑定类
     *
     * @return
     */
    public static Binder getBinder() {
        return binder;
    }


    /**
     * 驼峰转下划线
     * @param para 字符串
     * @return String
     */
    public static String humpToUnderline(String para) {
        para = lowerFirst(para);
        StringBuilder sb = new StringBuilder(para);
        int temp = 0;
        for (int i = 0; i < para.length(); i++) {
            if (Character.isUpperCase(para.charAt(i))) {
                sb.insert(i + temp, "_");
                temp += 1;
            }
        }
        return sb.toString().toLowerCase();
    }

    /**
     * 首字母变小写
     *
     * @param str 字符串
     * @return {String}
     */
    public static String lowerFirst(String str) {
        char firstChar = str.charAt(0);
        if (firstChar >= StringPool.U_A && firstChar <= StringPool.U_Z) {
            char[] arr = str.toCharArray();
            arr[0] += (StringPool.L_A - StringPool.U_A);
            return new String(arr);
        }
        return str;
    }

    /**
     * 去掉指定前缀
     *
     * @param str    字符串
     * @param prefix 后缀
     * @return 切掉后的字符串，若前缀不是 prefix， 返回原字符串
     */
    public static String removePrefix(CharSequence str, CharSequence prefix){
        if (isEmpty(str)) {
            return StringPool.EMPTY;
        }

        final String str2 = str.toString();
        if (str2.startsWith(prefix.toString())) {
            return subSuf(str2, prefix.length());
        }
        return str2;
    }

    /**
     * 去掉指定后缀
     *
     * @param str    字符串
     * @param suffix 后缀
     * @return 切掉后的字符串，若后缀不是 suffix， 返回原字符串
     */
    public static String removeSuffix(CharSequence str, CharSequence suffix) {
        if (isEmpty(str)) {
            return StringPool.EMPTY;
        }

        final String str2 = str.toString();
        if (str2.endsWith(suffix.toString())) {
            return subPre(str2, str2.length() - suffix.length());
        }
        return str2;
    }

    /**
     * 截取指定位置之后部分的字符串
     *
     * @param string  字符串
     * @param fromIndex 切割起始的位置（包括）
     * @return 后半部分字符串
     */
    public static String subSuf(CharSequence string, int fromIndex){
        return string.subSequence(fromIndex, string.length()).toString();
    }

    /**
     * 截取指定位置之前部分的字符串
     *
     * @param string  字符串
     * @param toIndex 切割到的位置（不包括）
     * @return 前半部分字符串
     */
    public static String subPre(CharSequence string, int toIndex) {
        return string.subSequence(0, toIndex).toString();
    }

    /**
     * 生成唯一商品订单号
     * @param prefix
     * @param suffix
     * @return
     */
    public static String generateOrderNumber(String prefix, String suffix){
        return prefix + UUID.randomUUID().toString().replace("-","") + suffix;
    }


    /**
     * 判断所给id是否有管理员权限
     * @param id
     * @return
     */
    public static Boolean isAdmin(Long id){
        return id != null && 1L == id;

    }
}
