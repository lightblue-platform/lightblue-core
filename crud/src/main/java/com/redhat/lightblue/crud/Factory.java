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
package com.redhat.lightblue.crud;

import java.io.Serializable;

import com.redhat.lightblue.util.DefaultRegistry;
import com.redhat.lightblue.util.Resolver;

import com.redhat.lightblue.metadata.EntityMetadata;

import com.redhat.lightblue.hooks.HookResolver;
import com.redhat.lightblue.hooks.CRUDHook;

import com.redhat.lightblue.interceptor.InterceptorManager;

/**
 * Factory class should be configured on initialization with all the validators and hooks from all the subsystems, and
 * used as a shared singleton object by all threads.
 */
public class Factory implements Serializable {

    private static final long serialVersionUID = 1L;

    private final DefaultRegistry<String, FieldConstraintChecker> fieldConstraintValidatorRegistry = new DefaultRegistry<>();
    private final DefaultRegistry<String, EntityConstraintChecker> entityConstraintValidatorRegistry = new DefaultRegistry<>();

    private final DefaultRegistry<String, CRUDController> crudControllers = new DefaultRegistry<>();

    private HookResolver hookResolver;
    private final InterceptorManager interceptors=new InterceptorManager();

    /**
     * Adds a field constraint validator
     *
     * @param name Constraint name
     * @param checker Constraint checker
     */
    public synchronized void addFieldConstraintValidator(String name, FieldConstraintChecker checker) {
        fieldConstraintValidatorRegistry.add(name, checker);
    }

    /**
     * Adds a set of field constraint validators
     *
     * @param r A field constraint checker resolver containing a set of constraint checkers
     */
    public synchronized void addFieldConstraintValidators(Resolver<String, FieldConstraintChecker> r) {
        fieldConstraintValidatorRegistry.add(r);
    }

    /**
     * Adds an entity constraint validator
     *
     * @param name Constraint name
     * @param checker Constraint checker
     */
    public synchronized void addEntityConstraintValidator(String name, EntityConstraintChecker checker) {
        entityConstraintValidatorRegistry.add(name, checker);
    }

    /**
     * Adds a set of entity constraint validators
     *
     * @param r An entity constraint checker resolver containing a set of constraint checkers
     */
    public synchronized void addEntityConstraintValidators(Resolver<String, EntityConstraintChecker> r) {
        entityConstraintValidatorRegistry.add(r);
    }

    /**
     * Returns a constraint validator containing field and entity constraint validators for the given entity
     */
    public ConstraintValidator getConstraintValidator(EntityMetadata md) {
        return new ConstraintValidator(fieldConstraintValidatorRegistry,
                entityConstraintValidatorRegistry,
                md);
    }

    /**
     * Adds a CRUD controller for the given backend type
     *
     * @param backendType Type of the backend for which a controller is being added
     * @param controller The controller
     */
    public synchronized void addCRUDController(String backendType, CRUDController controller) {
        crudControllers.add(backendType, controller);
    }

    /**
     * Returns a CRUD controller for the given backend type
     */
    public CRUDController getCRUDController(String backendType) {
        return crudControllers.find(backendType);
    }

    /**
     * Returns a CRUD controller for the given entity
     */
    public CRUDController getCRUDController(EntityMetadata md) {
        return getCRUDController(md.getDataStore().getBackend());
    }

    /**
     * Sets the hook resolver
     */
    public void setHookResolver(HookResolver h) {
        hookResolver = h;
    }

    /**
     * Returns the hook resolver
     */
    public HookResolver getHookResolver() {
        return hookResolver;
    }

    /**
     * Returns the hook with the given name. Returns null if hook doesn't exist
     */
    public CRUDHook getHook(String hookName) {
        return hookResolver.getHook(hookName);
    }

    /**
     * Returns the interceptor manager
     */
    public InterceptorManager getInterceptors() {
        return interceptors;
    }

}
