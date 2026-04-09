package com.naic.common.interceptor;

/**
 * @author wangyunong
 */
public class LoginIdHolder {
    private static final ThreadLocal<Long> LOGIN_ID_HOLDER = new ThreadLocal<Long>() {
        @Override
        protected Long initialValue() {
            return 0L;
        }
    };

    public LoginIdHolder() {
    }

    public static void setLoginId(Long header) {
        LOGIN_ID_HOLDER.set(header);
    }

    public static Long getLoginId() {
        return (Long)LOGIN_ID_HOLDER.get();
    }

    public static boolean isAdmin(){
        return LOGIN_ID_HOLDER.get().equals(1L);

    }
}
