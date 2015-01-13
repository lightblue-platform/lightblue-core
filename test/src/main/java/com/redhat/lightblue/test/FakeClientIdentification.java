/*
 Copyright 2015 Red Hat, Inc. and/or its affiliates.

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
package com.redhat.lightblue.test;

import java.util.HashMap;
import java.util.Map;

import com.redhat.lightblue.ClientIdentification;

public class FakeClientIdentification extends ClientIdentification {

    private static final long serialVersionUID = -4593345567334145097L;

    private final String principal;
    private final Map<String, Boolean> userRoles;

    public FakeClientIdentification(String principal){
        this(principal, new HashMap<String, Boolean>());
    }

    public FakeClientIdentification(String principal, Map<String, Boolean> userRoles){
        this.principal = principal;
        this.userRoles = userRoles;
    }

    @Override
    public String getPrincipal() {
        return principal;
    }

    @Override
    public boolean isUserInRole(String role) {
        Boolean isValid = userRoles.get(role);
        if(isValid == null){
            return false;
        }

        return isValid;
    }

}
