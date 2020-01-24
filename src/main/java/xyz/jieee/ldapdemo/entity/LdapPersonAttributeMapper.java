package xyz.jieee.ldapdemo.entity;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

/**
 * @author lingjie@jieee.xyz
 * @version 1.0
 * @date 2020/1/23 7:52 下午
 **/
public class LdapPersonAttributeMapper implements AttributesMapper<Person> {
    @Override
    public Person mapFromAttributes(Attributes attributes) throws NamingException {
        Person person = new Person();
        person.setName(attributes.get("cn").get().toString());
        person.setAccount(attributes.get("sAMAccountName").get().toString());
        person.setEmail(attributes.get("mail").get().toString());
        person.setStatus(attributes.get("userAccountControl").get().toString());
        person.setDn(LdapUtils.newLdapName(attributes.get("distinguishedName").get().toString()));
        return person;
    }
}
