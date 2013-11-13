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

package com.redhat.lightblue.metadata;

import java.util.ArrayList;

import com.redhat.lightblue.metadata.types.DefaultTypes;

public class Types implements TypeResolver {

    private static final ArrayList<TypeResolver> typeResolvers=new ArrayList<TypeResolver>();

    /**
     * Adds a new type resolver. If the new type resolver redefined
     * any of the already defined types, the type is overriden by the
     * new copy
     */
    public void addTypeResolver(TypeResolver r) {
        if(!typeResolvers.contains(r))
            typeResolvers.add(0,r);
    }

    /**
     * Adds the default type resolvers to this instance
     */
    public void addDefaultTypeResolvers() {
        addTypeResolver(new DefaultTypes());
    }

    @Override
    public Type getType(String name) {
        for(TypeResolver x:typeResolvers) {
            Type t=x.getType(name);
            if(t!=null)
                return t;
        }
        return null;
    }
}


