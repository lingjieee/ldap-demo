package xyz.jieee.ldapdemo.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;

/**
 * @author lingjie@jieee.xyz
 * @version 1.0
 * @date 2020/1/23 11:49 上午
 **/
@Data
@Entry(objectClasses = "person")
public final class Person {

    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private Name dn;

    @Attribute(name = "cn")
    private String name;

    @Attribute(name = "sAMAccountName")
    private String account;

    @Attribute(name = "mail")
    private String email;

    @Attribute(name = "userAccountControl")
    protected String status;
}
