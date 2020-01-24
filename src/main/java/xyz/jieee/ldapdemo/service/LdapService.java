package xyz.jieee.ldapdemo.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.control.PagedResultsCookie;
import org.springframework.ldap.control.PagedResultsDirContextProcessor;
import org.springframework.ldap.core.AttributesMapperCallbackHandler;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.SearchExecutor;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Service;
import xyz.jieee.ldapdemo.constants.Constants;
import xyz.jieee.ldapdemo.entity.LdapPersonAttributeMapper;
import xyz.jieee.ldapdemo.entity.Person;

import javax.naming.InvalidNameException;
import javax.naming.directory.*;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author lingjie@jieee.xyz
 * @version 1.0
 * @date 2020/1/24 1:04 下午
 **/
@Slf4j
@Service
public class LdapService {

    @Autowired
    private LdapTemplate ldapTemplate;

    /**
     * 账号查找
     */
    public Person find1(String account) {
        Person person = ldapTemplate.findOne(LdapQueryBuilder.query().where("sAMAccountName").is(account)
                .and("objectClass").is("person"), Person.class);
        return person;
    }

    /**
     * 账号查找
     */
    public Person find2(String account) {
        List<Person> list = ldapTemplate.find(LdapQueryBuilder.query()
                .where("sAMAccountName").is(account)
                .and("objectClass").is("person"), Person.class);
        if (list == null || list.size() == 0) {
            return null;
        } else if (list.size() > 1) {
            throw new RuntimeException("匹配到多个用户");
        } else {
            return list.get(0);
        }
    }

    /**
     * 查询所有用户
     */
    public List<Person> queryAll1() {
        List<Person> list = ldapTemplate.find(LdapQueryBuilder.query().base("ou=测试公司").where("objectClass").is("person"), Person.class);
        return list;
    }

    /**
     * 查找用户
     */
    public List<Person> queryAll2() {
        List<Person> list = ldapTemplate.search(LdapQueryBuilder.query().base("ou=测试公司").where("objectClass").is("person"), new LdapPersonAttributeMapper());
        return list;
    }

    /**
     * 分页查找用户
     */
    public List<Person> queryAllPage() {
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningObjFlag(true);

        SearchExecutor executor = ctx -> ctx.search(LdapUtils.newLdapName("ou=测试公司"), "(&(objectClass=person))", controls);
        PagedResultsCookie cookie = null;
        PagedResultsDirContextProcessor requestControl;
        AttributesMapperCallbackHandler<Person> callbackHandler = new AttributesMapperCallbackHandler<>(new LdapPersonAttributeMapper());
        do {
            requestControl = new PagedResultsDirContextProcessor(500, cookie);
            ldapTemplate.search(executor, callbackHandler, requestControl);
            cookie = requestControl.getCookie();
        } while (requestControl.hasMore());
        return callbackHandler.getList();
    }

    /**
     * 增量查询
     */
    public List<Person> queryDiff1(Date startTime) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss.SZ");
        List<Person> list = ldapTemplate.find(LdapQueryBuilder.query().base("ou=测试公司").where("objectClass").is("person")
                .and("whenChanged").gte(format.format(startTime)), Person.class);
        return list;
    }

    /**
     * 增量查询
     */
    public List<Person> queryDiff2(Date startTime) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss.SZ");
        List<Person> list = ldapTemplate.search(LdapQueryBuilder.query().base("ou=测试公司").where("objectClass").is("person")
                .and("whenChanged").gte(format.format(startTime)), new LdapPersonAttributeMapper());
        return list;
    }

    /**
     * 添加ou
     */
    public boolean addOU(String name, String baseDn) {
        Attributes ouAttributes = new BasicAttributes();
        BasicAttribute ouBasicAttribute = new BasicAttribute("objectClass");
        ouBasicAttribute.add("organizationalUnit");
        ouAttributes.put(ouBasicAttribute);
        if (StringUtils.isBlank(baseDn)) {
            baseDn = "";
        }
        LdapName dn = LdapUtils.newLdapName(baseDn);
        try {
            dn.add(new Rdn("ou", name));
        } catch (InvalidNameException e) {
            log.error("{}", e.getMessage(), e);
            return false;
        }
        ldapTemplate.bind(dn, null, ouAttributes);
        return true;
    }

    /**
     * 删除ou
     */
    public boolean deleteOU(String dn, boolean recursive) {
        try {
            ldapTemplate.lookup(dn);
        } catch (NameNotFoundException e) {
            throw new RuntimeException("OU不存在");
        }
        ldapTemplate.unbind(dn, recursive);
        return true;
    }

    /**
     * 添加用户
     */
    public boolean addUser(String name, String account, String baseDn) {
        Attributes attrs = new BasicAttributes();
        BasicAttribute oc = new BasicAttribute("objectClass");
        oc.add("top");
        oc.add("person");
        oc.add("organizationalPerson");
        oc.add("user");
        attrs.put(oc);
        attrs.put("sAMAccountName", account);
        attrs.put("cn", name);
        attrs.put("displayName", name);
        attrs.put("mail", account + "@xxx.com");
        int status = Constants.NORMAL_ACCOUNT;
        attrs.put("userAccountControl", String.valueOf(status));
        String pwd = "Hello@1234"; // 初始密码

        attrs.put("unicodePwd", encodePwd(pwd));
        if (StringUtils.isBlank(baseDn)) {
            baseDn = "";
        }
        LdapName dn = LdapNameBuilder.newInstance(baseDn)
                .add("cn", name)
                .build();
        ldapTemplate.bind(dn, null, attrs);
        return true;
    }

    /**
     * 删除用户
     */
    public boolean deleteUser(String dn) {
        try {
            ldapTemplate.lookup(dn);
        } catch (NameNotFoundException e) {
            throw new RuntimeException("用户不存在");
        }
        ldapTemplate.unbind(dn);
        return true;
    }

    /**
     * 修改密码
     */
    public boolean updatePwd(String dn, String newPwd) {
        try {
            ldapTemplate.lookup(dn);
        } catch (NameNotFoundException e) {
            throw new RuntimeException("用户不存在");
        }
        ModificationItem[] mods = new ModificationItem[1];
        mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("unicodePwd", encodePwd(newPwd)));
        ldapTemplate.modifyAttributes(dn, mods);
        return true;
    }

    /**
     * 添加用户组
     */
    public boolean addGroup(String name, String baseDn) {
        Attributes groupAttributes=new BasicAttributes();
        BasicAttribute oc=new BasicAttribute("objectClass");
        oc.add("top");
        oc.add("group");
        groupAttributes.put(oc);
        groupAttributes.put("cn", name);
        groupAttributes.put("sAMAccountName", name);
        BasicAttribute member = new BasicAttribute("member");
        member.add("cn=张三,ou=部门一,ou=测试公司");
        member.add("cn=李四,ou=部门一,ou=测试公司");
        groupAttributes.put(member);
        if(StringUtils.isBlank(baseDn)){
            baseDn = "";
        }
        LdapName dn = LdapUtils.newLdapName(baseDn);
        try {
            dn.add(new Rdn("cn", name));
        } catch (InvalidNameException e) {
            log.error("{}",e.getMessage(), e);
            return false;
        }
        ldapTemplate.bind(dn,null,groupAttributes);
        return true;
    }

    /**
     * 添加用户至用户组
     */
    public boolean addUserToGroup(String userDn, String groupDn) {
        DirContextOperations ctxGroup;
        try {
            ctxGroup = ldapTemplate.lookupContext(groupDn);
        }catch (NameNotFoundException e){
            throw new RuntimeException("用户组不存在");
        }
        DirContextOperations ctxUser;
        try {
            ctxUser = ldapTemplate.lookupContext(userDn);
        }catch (NameNotFoundException e){
            throw new RuntimeException("用户组不存在");
        }
        ctxGroup.addAttributeValue("member", ctxUser.getStringAttribute("distinguishedName"));
        ldapTemplate.modifyAttributes(ctxGroup);
        return true;
    }

    /**
     * 从用户组中删除用户
     */
    public boolean removeUserFromGroup(String userDn, String groupDn) {
        DirContextOperations ctxGroup;
        try {
            ctxGroup = ldapTemplate.lookupContext(groupDn);
        }catch (NameNotFoundException e){
            throw new RuntimeException("用户组不存在");
        }
        DirContextOperations ctxUser;
        try {
            ctxUser = ldapTemplate.lookupContext(userDn);
        }catch (NameNotFoundException e){
            throw new RuntimeException("用户组不存在");
        }
        ctxGroup.removeAttributeValue("member", ctxUser.getStringAttribute("distinguishedname"));
        ldapTemplate.modifyAttributes(ctxGroup);
        return true;
    }

    /**
     * 密码校验
     */
    public boolean auth(String account, String password) {
        ldapTemplate.authenticate(LdapQueryBuilder.query().where("sAMAccountName").is(account), password);
        return true;
    }

    private byte[] encodePwd(String source) {
        String quotedPassword = "\"" + source + "\""; // 注意：必须在密码前后加上双引号
        return quotedPassword.getBytes(StandardCharsets.UTF_16LE);
    }
}
