package xyz.jieee.ldapdemo.constants;

/**
 * @author lingjie@jieee.xyz
 * @version 1.0
 * @date 2020/1/23 12:38 下午
 **/
public interface Constants {
    int ACCOUNT_DISABLE = 0x0001 << 1; // 账户已禁用
    int LOCKOUT = 0x0001 << 4; // 账户已锁定
    int PASSWD_NOTREQD = 0x0001 << 5; // 不需要密码
    int PASSWD_CANT_CHANGE = 0x0001 << 6; // 用户不能更改密码(只读，不能修改)
    int NORMAL_ACCOUNT = 0x0001 << 9; // 正常账户
    int DONT_EXPIRE_PASSWORD = 0x0001 << 16; // 密码永不过期
    int PASSWORD_EXPIRED = 0x0001 << 23; // 密码已过期
}
