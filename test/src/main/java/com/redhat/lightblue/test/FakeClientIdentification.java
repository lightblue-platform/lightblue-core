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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.redhat.lightblue.ClientIdentification;

public class FakeClientIdentification extends ClientIdentification {

    private static final long serialVersionUID = -4593345567334145097L;

    private final String principal;
    private final Set<String> validUserRoles;

    public FakeClientIdentification(String principal, String... validUserRoles){
        this(principal, new HashSet<String>(Arrays.asList(validUserRoles)));
    }

    public FakeClientIdentification(String principal, Set<String> validUserRoles){
        this.principal = principal;
        this.validUserRoles = validUserRoles;
    }

    @Override
    public String getPrincipal() {
        return principal;
    }

    @Override
    public boolean isUserInRole(String role) {
        return validUserRoles.contains(role);
    }

}
