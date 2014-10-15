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

    public static enum Inclusion {explicit_exclusion,implicit_exclusion,explicit_inclusion,implicit_inclusion,undecided};

    public static Projection fromJson(JsonNode node) {
        if (node instanceof ArrayNode) {
            return ProjectionList.fromJson((ArrayNode) node);
        } else {
            return BasicProjection.fromJson((ObjectNode) node);
        }
    }

    /**
     * Adds two projections and returns a new projection containing both. Any
     * projection can be null. If the resulting projection is empty, returns
     * null.
     */
    public static Projection add(Projection p1, Projection p2) {
        List<Projection> list = new ArrayList<>();
        if (p1 instanceof ProjectionList) {
            list.addAll(((ProjectionList) p1).getItems());
        } else if (p1 != null) {
            list.add(p1);
        }
        if (p2 instanceof ProjectionList) {
            list.addAll(((ProjectionList) p2).getItems());
        } else if (p2 != null) {
            list.add(p2);
        }
        return list.isEmpty() ? null : new ProjectionList(list);
    }

    /**
     * Returns whether to include/exclude the field based on whether the
     * field matches the pattern
     *
     * @param field The field name
     * @param pattern The projection pattern
     *
     * <pre>
     *     field    pattern   result
     *      a          *       true
     *      a          a       true
     *      a          b       false
     *      a.b        *       false
     *      a.b       *.b      true
     *      a.b       a.*      true
     *      a.b       *.*      true
     *      a.b       a.b      true
     * </pre>
     *
     * The return value is <code>inclusion</code> if result is true,
     * <code>null</code> if result is false, meaning that whether the
     * field should be included or not cannot be decided.
     */
    public static Boolean fieldMatchesPattern(Path field,Path pattern,boolean inclusion) {
        if(field.matches(pattern))
            return inclusion;
        else
            return null;
    }

    /**
     * If the field is an ancestor of the patter, and if
     * <code>inclusion</code> is true, returns true. Otherwise,
     * returns null, meaning that whether the field is included or not
     * cannot be decided
     */
    public static Boolean fieldAncestorOfPattern(Path field,Path pattern,boolean inclusion) {
        if(field.matchingPrefix(pattern))
            if(inclusion)
                return Boolean.TRUE;
            else
                return null;
        else
            return null;
    }

    /**
     * Returns if the field should be included based on the recursive pattern
     */
    public static Boolean impliedInclusion(Path field,Path pattern,boolean inclusion) {
        if(field.numSegments() > pattern.numSegments() &&   // If we're checking a field deeper than the pattern
           // And if we're checking a field under the subtree of the pattern
           field.prefix(pattern.numSegments()).matches(pattern) ) {
            return inclusion ? Boolean.TRUE:Boolean.FALSE;
        } else
            return null;
    }

    /**
     * Returns if the field should be included based on the pattern given.
     *
     * @param field The field whose inclusion is to be decided
     * @param pattern The field pattern of projection
     * @param inclusion If the projection expression includes the
     * field. If false, the projection is for exclusion
     * @param recursive If the projection is recursive
     *
     * @return implicit or explicit inclusion/exclusion, or undecided
     * if projection does not decide if the field should be
     * included/excluded
     */
    public static Inclusion isFieldIncluded(Path field,
                                            Path pattern,
                                            boolean inclusion,
                                            boolean recursive) {
        Boolean v=fieldMatchesPattern(field,pattern,inclusion);
        if(v!=null)
            return v?Inclusion.explicit_inclusion:Inclusion.explicit_exclusion;
        v=fieldAncestorOfPattern(field,pattern,inclusion);
        if(v!=null)
            return v?Inclusion.explicit_inclusion:Inclusion.explicit_exclusion;
        if(recursive) {
            v=impliedInclusion(field,pattern,inclusion);
            if(v!=null)
                return v?Inclusion.implicit_inclusion:Inclusion.implicit_exclusion;
        }
        return Inclusion.undecided;
    }

    protected static Path getNonRelativePath(Path p) {
        List<String> segments = new ArrayList<>();
        int numberOfParentsOnPath = 0;
        for (int i = p.numSegments() - 1; i >= 0; i--) {
            if (Path.THIS.equals(p.head(i))) {
                continue;
            } else if (Path.PARENT.equals(p.head(i))) {
                numberOfParentsOnPath++;
            } else {
                if (numberOfParentsOnPath > 0) {
                    numberOfParentsOnPath--;
                    continue;
                }
                segments.add(p.head(i));
            }
        }
        Collections.reverse(segments);
        return new Path(Joiner.on(".").join(segments));
    }
}
