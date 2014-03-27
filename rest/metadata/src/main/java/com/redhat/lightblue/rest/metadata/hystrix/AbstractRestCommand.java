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
package com.redhat.lightblue.rest.metadata.hystrix;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.redhat.lightblue.config.metadata.MetadataManager;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;

/**
 * Note that passing a Metadata in the constructor is optional. If not provided, it is fetched from MetadataManager
 * object.
 *
 * @author nmalik
 */
public abstract class AbstractRestCommand extends HystrixCommand<String> {
    protected static final JsonNodeFactory NODE_FACTORY = JsonNodeFactory.withExactBigDecimals(true);

    private final Metadata metadata;

    public AbstractRestCommand(Class commandClass, String clientKey, Metadata metadata) {
        super(HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(commandClass.getSimpleName()))
                .andCommandKey(HystrixCommandKey.Factory.asKey(clientKey == null ? commandClass.getSimpleName() : (commandClass.getSimpleName() + "-" + clientKey))));
        this.metadata = metadata;
    }

    /**
     * Returns the metadata. If no metadata is set on the command uses MetadataManager#getMetadata() method.
     *
     * @return
     * @throws Exception
     */
    protected Metadata getMetadata() throws Exception {
        if (null != metadata) {
            return metadata;
        } else {
            return MetadataManager.getMetadata();
        }
    }

    protected JSONMetadataParser getJSONParser() throws Exception {
        return MetadataManager.getJSONParser();
    }
}
