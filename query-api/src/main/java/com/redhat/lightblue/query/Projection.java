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
package com.redhat.lightblue.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.redhat.lightblue.util.JsonObject;
import com.redhat.lightblue.util.Path;

/**
 * Base class for all projection objects
 */
public abstract class Projection extends JsonObject {
	private static final long serialVersionUID = 1L;
	
    public static Projection fromJson(JsonNode node) {
        if (node instanceof ArrayNode) {
            return ProjectionList.fromJson((ArrayNode) node);
        } else {
            return BasicProjection.fromJson((ObjectNode) node);
        }
    }

    /**
     * Adds two projections and returns a new projection containing
     * both. Any projection can be null. If the resulting projection
     * is empty, returns null.
     */
    public static Projection add(Projection p1,Projection p2) {
        List<Projection> list=new ArrayList<Projection>();
        if(p1 instanceof ProjectionList)
            list.addAll( ((ProjectionList)p1).getItems());
        else if(p1 !=null)
            list.add(p1);
        if(p2 instanceof ProjectionList)
            list.addAll( ((ProjectionList)p2).getItems());
        else if(p2!=null)
            list.add(p2);
        return list.isEmpty()?null:new ProjectionList(list);
    }
    
    protected static Path getNonRelativePath(Path p) {
        List<String> segments = new ArrayList<String>();
        int numberOfParentsOnPath = 0;
        for (int i = p.numSegments() - 1; i >= 0; i--) {
            if(Path.THIS.equals(p.head(i))) {
                continue;
            } else if(Path.PARENT.equals(p.head(i))) {
                numberOfParentsOnPath ++ ;
            } else {
                if(numberOfParentsOnPath > 0) {
                    numberOfParentsOnPath --;
                    continue;
                }
                segments.add(p.head(i));
            }
        }
        Collections.reverse(segments);
        return new Path(Joiner.on(".").join(segments));
    }

}
