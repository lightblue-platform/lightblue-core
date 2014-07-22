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
package com.redhat.lightblue.rest.auth.ldap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import com.redhat.lightblue.rest.auth.LightblueRoleProvider;

public class LightblueLdapRoleProvider implements LightblueRoleProvider {

    LdapContext ldapContext;
    String ldapSearchBase;

    public LightblueLdapRoleProvider(String server, String searchBase, String bindDn, String bindDNPwd) throws NamingException {

        Hashtable<String, Object> env = new Hashtable<String, Object>();
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        if (bindDn != null) {
            env.put(Context.SECURITY_PRINCIPAL, bindDn);
        }
        if (bindDNPwd != null) {
            env.put(Context.SECURITY_CREDENTIALS, bindDNPwd);
        }
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, server);
        ldapSearchBase = searchBase;
        ldapContext = new InitialLdapContext(env, null);
    }

    public List<String> getUserRoles(String userName) {
        List<String> userRoles = new ArrayList<String>();

        try {
            userRoles.addAll(getUserRolesFromCache(userName));

            if (userRoles.size() == 0) {
                userRoles.addAll(getUserRolesFromLdap(findUserByUid(userName)));
            }
        } catch (NamingException ne) {
            System.err.println("Problem getting roles for user: " + userName);
        }

        return userRoles;
    }

    @Override
    public Collection<String> getUsersInGroup(String groupName) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void flushRoleCache(String roleName) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void flushUserCache(String userName) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private List<String> getUserRolesFromCache(String userName) {
        //TODO add persistent caching backed by lightblue here at some point
        return Collections.emptyList();
    }

    private SearchResult findUserByUid(String uid) throws NamingException {

        String searchFilter = "(uid=" + uid + ")";

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration<SearchResult> results = ldapContext.search(ldapSearchBase, searchFilter, searchControls);

        SearchResult searchResult = null;
        if (results.hasMoreElements()) {
            searchResult = results.nextElement();

            //make sure there is not another item available, there should be only 1 match
            if (results.hasMoreElements()) {
                System.err.println("Matched multiple users for the accountName: " + uid);
                return null;
            }
        }

        return searchResult;
    }

    private List<String> getUserRolesFromLdap(SearchResult ldapUser) throws NamingException {
        List<String> groups = new ArrayList<String>();

        NamingEnumeration<?> groupAttributes = ldapUser.getAttributes().get("memberOf").getAll();

        while (groupAttributes.hasMore()) {
            LdapName name = new LdapName((String) groupAttributes.next());

            for (Rdn rdn : name.getRdns()) {
                if (rdn.getType().equalsIgnoreCase("cn")) {
                    groups.add((String) rdn.getValue());
                    break;
                }
            }
        }

        return groups;
    }

}
