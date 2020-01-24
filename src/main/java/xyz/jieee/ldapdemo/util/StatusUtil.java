package xyz.jieee.ldapdemo.util;


import xyz.jieee.ldapdemo.constants.Constants;

/**
 * @author lingjie@jieee.xyz
 * @version 1.0
 * @date 2020/1/23 1:02 下午
 **/
public class StatusUtil {

    /**
     * 账户是否被禁用
     */
    public static boolean isDisable(int status){
        return (status & Constants.ACCOUNT_DISABLE) == Constants.ACCOUNT_DISABLE;
    }

    /**
     * 账户是否被锁定
     */
    public static boolean isLockout(int status){
        return (status & Constants.LOCKOUT) == Constants.LOCKOUT;
    }

    /**
     * 密码是否已过期
     */
    public static boolean isPasswordExpired(int status){
        return (status & Constants.PASSWORD_EXPIRED) == Constants.PASSWORD_EXPIRED;
    }

    /**
     * 禁用账户
     */
    public static int setDisable(int status){
        return status ^ Constants.ACCOUNT_DISABLE;
    }

    /**
     * 锁定账户
     */
    public static int setLockout(int status){
        return status ^ Constants.LOCKOUT;
    }

    /**
     * 密码设为过期
     */
    public static int setPasswordExpired(int status){
        return status ^ Constants.PASSWORD_EXPIRED;
    }
}
