package xyz.jieee.ldapdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LdapDemoApplication {

	public static void main(String[] args) {
		System.setProperty("javax.net.ssl.trustStore","/path/to/ldap-mapad.jks");
		System.setProperty("javax.net.ssl.trustStorePassword","******");
		SpringApplication.run(LdapDemoApplication.class, args);
	}

}
