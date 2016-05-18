
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
package com.redhat.lightblue.mediator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.OperationStatus;

import com.redhat.lightblue.crud.CRUDController;
import com.redhat.lightblue.crud.CRUDFindRequest;
import com.redhat.lightblue.crud.CRUDFindResponse;
import com.redhat.lightblue.crud.Factory;

import com.redhat.lightblue.metadata.EntityMetadata;

public class SimpleFindImpl implements Finder {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleFindImpl.class);

    private final EntityMetadata md;
    private final CRUDController controller;

    public SimpleFindImpl(EntityMetadata md,
                          Factory factory) {
        this.md = md;
        this.controller = factory.getCRUDController(md);
        LOGGER.debug("Controller for {}:{}", md.getName(), controller.getClass().getName());
    }

    @Override
    public CRUDFindResponse find(OperationContext ctx,
                                 CRUDFindRequest req) {
        CRUDFindResponse result = controller.find(ctx,
                req.getQuery(),
                req.getProjection(),
                req.getSort(),
                req.getFrom(),
                req.getTo());
        return result;
    }
}
