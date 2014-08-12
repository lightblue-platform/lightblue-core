/**
 *
 */
/*
 Copyright 2013 Red Hat, Inc. and/or its affiliates.

 This file is part of lightblue.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import java.util.Arrays;
import java.util.Collections;

/**
 * @author dhaynes
 *
 * Jboss loginModules authenticator that does authorization against LDAP
 * (authentication provided by CertRolesLoginModule 7/11/2014
 *
 */
public class CertLdapLoginModule extends CertRolesLoginModule {
    public static final String AUTH_ROLE_NAME = "authRoleName";
    public static final String LDAP_SERVER = "ldapServer";
    public static final String SEARCH_BASE = "searchBase";
    public static final String BIND_DN = "bindDn";
    public static final String BIND_PWD = "bindPassword";

    private Logger logger = Logger.getLogger(CertLdapLoginModule.class);
    private Logger ACCESS_LOGGER = Logger.getLogger(CertLdapLoginModule.class, "access");

    private static final String[] ALL_VALID_OPTIONS = {AUTH_ROLE_NAME, LDAP_SERVER, SEARCH_BASE, BIND_DN, BIND_PWD};

    /* (non-Javadoc)
     * @see org.jboss.security.auth.spi.AbstractServerLoginModule#getRoleSets()
     */
    @Override
    protected Group[] getRoleSets() throws LoginException {
        System.out.println("getRoleSets() begin");
        logger.debug("staticRoleLoginModule getRoleSets()");
        String roleName = (String) options.get(AUTH_ROLE_NAME);
        String ldapServer = (String) options.get(LDAP_SERVER);
        String searchBase = (String) options.get(SEARCH_BASE);
        String bindDn = (String) options.get(BIND_DN);
        String bindPwd = (String) options.get(BIND_PWD);

        SimpleGroup userRoles = new SimpleGroup("Roles");

        Principal p = null;
        try {
            LightblueRoleProvider lbLdap = new LightblueLdapRoleProvider(ldapServer, searchBase, bindDn, bindPwd);

            logger.info("Prinicipal username:" + getUsername());

            LdapName name = new LdapName(getUsername());
            String searchName = new String();
            for (Rdn rdn : name.getRdns()) {
                if (rdn.getType().equalsIgnoreCase("cn")) {
                    searchName = (String) rdn.getValue();
                    break;
                }
            }

            Collection<String> groupNames = lbLdap.getUserRoles(searchName);

            p = super.createIdentity(roleName);

            userRoles.addMember(p);
            for (String groupName : groupNames) {
                Principal role = super.createIdentity(groupName);
                logger.debug("Found role: " + groupName);
                userRoles.addMember(role);
            }

            if (ACCESS_LOGGER.isInfoEnabled()) {
                ACCESS_LOGGER.info("Principal username: " + getUsername() + ", roles: " + Arrays.toString(groupNames.toArray()));
            }

            logger.debug("Assign principal [" + p.getName() + "] to role [" + roleName + "]");
        } catch (Exception e) {
            String principalName = p == null ? "null" : p.getName();
            logger.info("Failed to assign principal [" + principalName + "] to role [" + roleName + "]", e);
        }
        Group[] roleSets = {userRoles};
        return roleSets;
    }

}
