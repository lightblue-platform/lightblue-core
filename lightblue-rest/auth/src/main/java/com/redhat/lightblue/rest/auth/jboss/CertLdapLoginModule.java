/**
 * 
 */
package com.redhat.lightblue.rest.auth.jboss;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Collection;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.login.LoginException;

import org.jboss.logging.Logger;
import org.jboss.security.SimpleGroup;
import org.jboss.security.auth.spi.CertRolesLoginModule;

import com.redhat.lightblue.rest.auth.LightblueRoleProvider;
import com.redhat.lightblue.rest.auth.ldap.LightblueLdapRoleProvider;


/**
 * @author dhaynes
 *
 * Jboss loginModules authenticator that does authorization against LDAP (authentication provided by CertRolesLoginModule
 * 7/11/2014
 * 
 */

public class CertLdapLoginModule extends CertRolesLoginModule {
	public static final String AUTH_ROLE_NAME = "authRoleName";	
	public static final String LDAP_SERVER = "ldapServer";
	public static final String SEARCH_BASE = "searchBase";
	public static final String BIND_DN = "bindDn";
	public static final String BIND_PWD = "bindPassword";
	
    private Logger logger = Logger.getLogger(CertLdapLoginModule.class);
	private static final String[] ALL_VALID_OPTIONS = {AUTH_ROLE_NAME,LDAP_SERVER,SEARCH_BASE,BIND_DN,BIND_PWD};

	/* (non-Javadoc)
	 * @see org.jboss.security.auth.spi.AbstractServerLoginModule#getRoleSets()
	 */
	@Override
	protected Group[] getRoleSets() throws LoginException {
		System.out.println("getRoleSets() begin");
        logger.debug("staticRoleLoginModule getRoleSets()");
		String roleName = (String)options.get(AUTH_ROLE_NAME);
		String ldapServer = (String)options.get(LDAP_SERVER);
		String searchBase = (String)options.get(SEARCH_BASE);
		String bindDn = (String)options.get(BIND_DN);
		String bindPwd = (String)options.get(BIND_PWD);
		
		SimpleGroup userRoles = new SimpleGroup("Roles");
		
        Principal p = null;
		try {
	        LightblueRoleProvider lbLdap = new LightblueLdapRoleProvider(ldapServer, searchBase, bindDn, bindPwd);
	        
	        logger.info("Prinicipal username:" + getUsername());
	        
	        LdapName name = new LdapName(getUsername());
	        String searchName = new String();
	    	for(Rdn rdn : name.getRdns()) {
	    	    if(rdn.getType().equalsIgnoreCase("cn")) {
	    	        searchName  = (String)rdn.getValue();
	    	        break;
	    	    }
	    	}

	    	Collection<String> groupNames = lbLdap.getUserRoles(searchName);

	        p = super.createIdentity(roleName);
	        
	        userRoles.addMember(p);
            for(String groupName : groupNames) {
            	Principal role = super.createIdentity(groupName);
                logger.debug("Found role: " + groupName);
                userRoles.addMember(role);
            }

			logger.debug("Assign principal [" + p.getName() + "] to role [" + roleName + "]");
		} catch (Exception e) {
			logger.info("Failed to assign principal [" + p.getName() + "] to role [" + roleName + "]", e);
		}
		Group[] roleSets = {userRoles};
		return roleSets;
	}

}