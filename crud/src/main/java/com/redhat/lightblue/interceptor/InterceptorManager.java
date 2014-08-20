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
package com.redhat.lightblue.interceptor;

import java.io.Serializable;

import java.util.TreeMap;
import java.util.HashMap;

import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.DocCtx;
import com.redhat.lightblue.mediator.OperationContext;

public class InterceptorManager implements Serializable {

    private static final long serialVersionUID = 1l;

    private final HashMap<InterceptPoint, TreeMap<Integer, Interceptor>> interceptors = new HashMap<>();

    public void registerInterceptor(int sequence, Interceptor i, InterceptPoint... pt) {
        for (InterceptPoint x : pt) {
            TreeMap<Integer, Interceptor> tmap = interceptors.get(x);
            if (tmap == null) {
                interceptors.put(x, tmap = new TreeMap<>());
            }
            if (x.getInterceptorClass().isAssignableFrom(i.getClass())) {
                tmap.put(sequence, i);
            } else {
                throw new IllegalArgumentException("Interceptor requires "
                        + x.getInterceptorClass().getName()
                        + " but got "
                        + i.getClass().getName());
            }
        }
    }

    public void callInterceptors(InterceptPoint.MediatorInterceptPoint pt, OperationContext ctx) {
        TreeMap<Integer, Interceptor> tree = interceptors.get(pt);
        if (tree != null) {
            for (Interceptor interceptor : tree.values()) {
                pt.call(interceptor, ctx);
            }
        }
    }

    public void callInterceptors(InterceptPoint.CRUDControllerInterceptPoint pt, CRUDOperationContext ctx) {
        TreeMap<Integer, Interceptor> tree = interceptors.get(pt);
        if (tree != null) {
            for (Interceptor interceptor : tree.values()) {
                pt.call(interceptor, ctx);
            }
        }
    }

    public void callInterceptors(InterceptPoint.CRUDDocInterceptPoint pt, CRUDOperationContext ctx, DocCtx doc) {
        TreeMap<Integer, Interceptor> tree = interceptors.get(pt);
        if (tree != null) {
            for (Interceptor interceptor : tree.values()) {
                pt.call(interceptor, ctx, doc);
            }
        }
    }
}
