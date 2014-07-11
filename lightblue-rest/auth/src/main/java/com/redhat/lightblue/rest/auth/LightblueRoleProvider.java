package com.redhat.lightblue.rest.auth;

import java.util.Collection;

public interface LightblueRoleProvider {

	public Collection<String> getUserRoles(String userName);
	
	public Collection<String> getUsersInGroup(String groupName);
	
	public void flushRoleCache(String roleName);
	
	public void flushUserCache(String userName);
	
}
