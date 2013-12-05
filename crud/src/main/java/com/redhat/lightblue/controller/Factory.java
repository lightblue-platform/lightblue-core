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
package com.redhat.lightblue.controller;

import com.redhat.lightblue.util.DefaultRegistry;
import com.redhat.lightblue.util.Resolver;

import com.redhat.lightblue.metadata.EntityMetadata;

/**
 * Factory class should be configured on initialization with all the
 * validators and hooks from all the subsystems, and used as a
 * shared singleton object by all threads. 
 */
public class Factory {

    private final DefaultRegistry<String,FieldConstraintChecker> fieldConstraintValidatorRegistry=
        new DefaultRegistry<String,FieldConstraintChecker>();
    private final DefaultRegistry<String,EntityConstraintChecker> entityConstraintValidatorRegistry=
        new DefaultRegistry<String,EntityConstraintChecker>();

    public synchronized void addFieldConstraintValidator(String name,FieldConstraintChecker checker) {
        fieldConstraintValidatorRegistry.add(name,checker);
    }

    public synchronized void addFieldConstraintValidators(Resolver<String,FieldConstraintChecker> r) {
        fieldConstraintValidatorRegistry.add(r);
    }

    public synchronized void addEntityConstraintValidator(String name,EntityConstraintChecker checker) {
        entityConstraintValidatorRegistry.add(name,checker);
    }

    public synchronized void addEntityConstraintValidators(Resolver<String,EntityConstraintChecker> r) {
        entityConstraintValidatorRegistry.add(r);
    }
        
    public ConstraintValidator getConstraintValidator(EntityMetadata md) {
        return new ConstraintValidator(fieldConstraintValidatorRegistry,
                                       entityConstraintValidatorRegistry,
                                       md);
    }
}
