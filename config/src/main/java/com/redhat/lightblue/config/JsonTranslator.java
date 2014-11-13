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
package com.redhat.lightblue.config;

import java.util.Map;
import java.util.HashMap;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import com.github.fge.jsonschema.main.JsonSchema;

import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.Error;

/**
 * This class is used to translate json documents into POJO,
 * optionally validating based on a schema. The POJO classes and their
 * correcponding schema must be registered before they're used to
 * parse json docs.
 */
public class JsonTranslator {

    private static final Logger LOGGER=LoggerFactory.getLogger(JsonTranslator.class);

    private final Map<Class,TranslationInfo> translationMap=new HashMap<>();

    /**
     * An abstraction that defines how json document is parsed to a POJO
     */
    public interface FromJson {
        /**
         * Parse the json document to a POJO
         */
        Object fromJson(JsonNode node);
    }

    /**
     * An implementation of FromJson that uses a static factory method
     * that gets a JsonNode object as argument, and returns a POJO
     */
    public static class StaticFactoryMethod implements FromJson {

        private final Method method;

        /**
         * Initialize the instance using the factory method
         */
        public StaticFactoryMethod(Method m) {
            this.method=m;
        }

        /**
         * Initialize the instance using the given class, method name,
         * and arg type describing the static factory method
         */
        public StaticFactoryMethod(Class c, String method,Class argType)
            throws NoSuchMethodException  {
            this(c.getMethod(method,argType));
        }

        /**
         * Initialize the instance using the given class and method
         * name describing the static factory method that gets a
         * JsonNode argument.
         */
        public StaticFactoryMethod(Class c,String method) 
            throws NoSuchMethodException {
            this(c,method,JsonNode.class);
        }

        /**
         * Calls the method to parse the JsonNode 
         */
        @Override
        public Object fromJson(JsonNode node) {
            try {
                return method.invoke(null,node);
            } catch (Exception e) {
                throw new IllegalArgumentException("Cannot call method "+method);
            }
        }
    }

    private static class TranslationInfo {
        private final FromJson fromJson;
        private boolean validate;
        private final JsonSchema schema;

        public TranslationInfo(FromJson fromJson,JsonSchema schema) {
            this.fromJson=fromJson;
            this.schema=schema;
        }

        public JsonSchema getSchema() {
            return schema;
        }
    }

    /**
     * Registers a translation
     *
     * @param clazz The POJO class that will be returned when a JSON
     * document of this type is parsed
     * @param fromJson The implementation of FromJson interface that
     * performs the actual parsing
     * @param resource The resource name in class path containing the schema
     */
    public void registerTranslation(Class clazz,FromJson fromJson, String resource) {
        try {
            registerTranslation(clazz,fromJson,JsonUtils.loadSchema(resource));
        } catch (Exception e) {
            throw new IllegalArgumentException(resource,e);
        }
    }

    /**
     * Registers a translation with the given schema
     * 
     * @param clazz The POJO class that will be returned when a JSON
     * document of this type is parsed
     * @param fromJson The implementation of FromJson interface that
     * performs the actual parsing
     * @param schema The JSON schema
     */
    public void registerTranslation(Class clazz,FromJson fromJson,JsonSchema schema) {
        TranslationInfo ti=new TranslationInfo(fromJson,schema);
        translationMap.put(clazz,ti);
    }

    /**
     * Registers a translation with the given schema for a POJO with a
     * static factory method getting a JsonNode argument
     *
     * @param clazz  The POJO class that will be returned when a JSON
     * document of this type is parsed
     * @param schema The JSON schema
     */
    public void registerTranslation(Class clazz,JsonSchema schema) 
        throws NoSuchMethodException {
        registerTranslation(clazz,new StaticFactoryMethod(clazz,"fromJson"),schema);
    }

    /**
     * Sets the validation flag for all POJOs that are subclasses of
     * <code>clazz</code>, including <code>clazz</code> itself.
     *
     * @param clazz The base class for which the validate flag will be set
     * @param validate The new value of the validation flag
     */
    public void setValidation(Class clazz, boolean validate) {
        for(Map.Entry<Class,TranslationInfo> entry:translationMap.entrySet()) {
            if(clazz.isAssignableFrom(entry.getKey()))
                entry.getValue().validate=validate;
        }
    }

    /**
     * Sets the validation flag for all POJOs
     */
    public void setAllValidation(boolean validate) {
        setValidation(Object.class,validate);
    }

    /**
     * Parses a json document, optionally validating it according to a
     * registered schema
     *
     * @param clazz The expected return POJO type
     * @param node The JSON node that will be parsed
     *
     * If there are schema validation errors, an Error will be thrown
     * with errors messages in itall
     *
     * @return The POJO
     */
    public <T> T parse(Class<T> clazz,JsonNode node) {
        LOGGER.debug("Parsing {}",clazz);
        TranslationInfo t=translationMap.get(clazz);
        if(t==null)
            throw new IllegalArgumentException("No translation for "+clazz.getName());
        if(t.validate) {
            LOGGER.debug("validating {}",clazz);
            try {
                String validationErrors=JsonUtils.jsonSchemaValidation(t.getSchema(),node);
                if(validationErrors!=null) {
                    throw Error.get(ConfigConstants.ERR_VAILDATION_FAILED,validationErrors);
                }
            } catch (RuntimeException re) {
                throw re;
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
        return (T)t.fromJson.fromJson(node);
    }
}
