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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.redhat.lightblue.util.JsonObject;
import com.redhat.lightblue.util.MutablePath;
import com.redhat.lightblue.util.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * Base class for all projection objects
 */
public abstract class Projection extends JsonObject {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(Projection.class);

    /**
     * Inclusion status for a field
     *
     * <ul>
     * <li>explicit_exclusion: the projection excludes this field
     * explicitly</li>
     * <li>implicit_exzclusion: the projection excludes this field because an
     * ancestor of the field is excluded</li>
     * <li>explicit_inclusion: the projection includes the field explicitly</li>
     * <li>explicit_exclusion: the projection excludes the field explicitly</li>
     * <li>undecided: the projection does not decide if the field should be
     * included/excluded, and does not reference it</li>
     * <ul>
     */
    public static enum Inclusion {
        explicit_exclusion, implicit_exclusion, explicit_inclusion, implicit_inclusion, undecided
    };

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
     * Returns whether to include/exclude the field based on whether the field
     * matches the pattern
     *
     * @param field The field name
     * @param pattern The projection pattern
     * @param inclusion flag to return if the field matches the given pattern
     * @return value of <code>inclusion</code> if field matches the pattern,
     * else null
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
     * <code>null</code> if result is false, meaning that whether the field
     * should be included or not cannot be decided.
     */
    public static Boolean fieldMatchesPattern(Path field, Path pattern, boolean inclusion) {
        if (field.matches(pattern)) {
            return inclusion;
        } else {
            return null;
        }
    }

    /**
     * If the field is an ancestor of the pattern, and if <code>inclusion</code>
     * is true, returns true. Otherwise, returns null, meaning that whether the
     * field is included or not cannot be decided.
     *
     * @param field The field name
     * @param pattern The projection pattern
     * @param inclusion flag to return if the field matches the given pattern
     * @return TRUE if field is ancestor of the pattern and
     * <code>inclusion</code> is true, else null
     */
    public static Boolean fieldAncestorOfPattern(Path field, Path pattern, boolean inclusion) {
        if (field.matchingPrefix(pattern)) {
            if (inclusion) {
                return Boolean.TRUE;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Returns if the field should be included based on the recursive pattern.
     *
     * @param field The field name
     * @param pattern The projection pattern
     * @param inclusion flag to return if the field matches the given pattern
     * @return Boolean value of <code>inclusion</code> if field is in subtree of
     * <code>pattern</code> else null
     */
    public static Boolean impliedInclusion(Path field, Path pattern, boolean inclusion) {
        if (field.numSegments() > pattern.numSegments()
                && // If we're checking a field deeper than the pattern
                // And if we're checking a field under the subtree of the pattern
                field.prefix(pattern.numSegments()).matches(pattern)) {
            return inclusion ? Boolean.TRUE : Boolean.FALSE;
        } else {
            return null;
        }
    }

    /**
     * Returns if the field should be included based on the pattern given.
     *
     * @param field The field whose inclusion is to be decided
     * @param pattern The field pattern of projection
     * @param inclusion If the projection expression includes the field. If
     * false, the projection is for exclusion
     * @param recursive If the projection is recursive
     *
     * @return implicit or explicit inclusion/exclusion, or undecided if
     * projection does not decide if the field should be included/excluded
     */
    public static Inclusion isFieldIncluded(Path field,
                                            Path pattern,
                                            boolean inclusion,
                                            boolean recursive) {
        // field match first, most specific type of check
        Boolean v = fieldMatchesPattern(field, pattern, inclusion);
        if (v != null) {
            // if the last segment is ANY then inclusion/exclusion is implicit.  else, inclusion/exclusion is explicit
            if (pattern.tail(0).equals(Path.ANY)) {
                return v ? Inclusion.implicit_inclusion : Inclusion.implicit_exclusion;
            } else {
                return v ? Inclusion.explicit_inclusion : Inclusion.explicit_exclusion;
            }
        }

        // check field ancestor
        v = fieldAncestorOfPattern(field, pattern, inclusion);
        if (v != null) {
            // is always explicit
            return v ? Inclusion.explicit_inclusion : Inclusion.explicit_exclusion;
        }

        // if recursive, check for implied inclusion/exclusion
        if (recursive) {
            v = impliedInclusion(field, pattern, inclusion);
            if (v != null) {
                // recursive is always implicit
                return v ? Inclusion.implicit_inclusion : Inclusion.implicit_exclusion;
            }
        }
        return Inclusion.undecided;
    }

    /**
     * Determines if the field is included in this projection
     *
     * @param field The absolute name of the field
     *
     * If the field name contains array indexes, they are converted to '*'
     * before evaluation.
     */
    public Inclusion getFieldInclusion(Path field) {
        LOGGER.debug("Checking if {} is projected", field);
        Inclusion ret=getFieldInclusion(field, Path.EMPTY);
        LOGGER.debug("Inclusion {}={}",field,ret);
        return ret;
    }

    /**
     * Determine if the field is explicitly included/excluded, implicitly
     * included, or the projection does not decide on the field.
     */
    public Inclusion getFieldInclusion(Path field, Path ctx) {
        Path mfield = toMask(field);
        ctx = toMask(ctx);
        if (this instanceof FieldProjection) {
            return getFieldInclusion(mfield, (FieldProjection) this, ctx);
        } else if (this instanceof ArrayProjection) {
            return getFieldInclusion(mfield, (ArrayProjection) this, ctx);
        } else if (this instanceof ProjectionList) {
            return getFieldInclusion(mfield, (ProjectionList) this, ctx);
        }
        return Inclusion.undecided;
    }

    /**
     * Returns true if the field is needed to evaluate the projection
     */
    public boolean isFieldRequiredToEvaluateProjection(Path field) {
        LOGGER.debug("Checking if {} is referenced in projection", field);
        return isFieldRequiredToEvaluateProjection(field, Path.EMPTY);
    }

    /**
     * Returns true if the field is needed to evaluate the projection
     */
    public boolean isFieldRequiredToEvaluateProjection(Path field, Path ctx) {
        Path mfield = toMask(field);
        ctx = toMask(ctx);
        if (this instanceof FieldProjection) {
            switch (getFieldInclusion(mfield, (FieldProjection) this, ctx)) {
                case implicit_inclusion:
                case explicit_inclusion:
                    return true;
                default:
                    return false;
            }
        } else if (this instanceof ArrayQueryMatchProjection) {
            if (getFieldInclusion(mfield, (ArrayProjection) this, ctx) == Inclusion.undecided) {
                LOGGER.debug("whether to include {} is Undecided, checking projection query", mfield);
                Path absField = new Path(ctx, toMask(((ArrayQueryMatchProjection) this).getField()));
                Path nestedCtx = new Path(absField, Path.ANYPATH);
                boolean ret = ((ArrayQueryMatchProjection) this).getMatch().isRequired(field, nestedCtx);
                LOGGER.debug("isRequired({},{}.*={}", field, absField, ret);
                if (ret) {
                    return true;
                }

                LOGGER.debug("Query does not require {}, checking nested projection", mfield);
                if (((ArrayProjection) this).getProject() != null) {
                    ret = ((ArrayProjection) this).getProject().isFieldRequiredToEvaluateProjection(field, nestedCtx);
                }
                LOGGER.debug("result:{}", ret);
                return ret;
            } else {
                return true;
            }
        } else if (this instanceof ArrayRangeProjection) {
            return getFieldInclusion(mfield, (ArrayProjection) this, ctx) != Inclusion.undecided;
        } else if (this instanceof ProjectionList) {
            for (Projection x : ((ProjectionList) this).getItems()) {
                if (x.isFieldRequiredToEvaluateProjection(field, ctx)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Inclusion getFieldInclusion(Path field, ArrayProjection p, Path context) {
        Path absField = new Path(context, toMask(p.getField()));
        LOGGER.debug("Checking if array projection on {} projects {}", absField, field);
        Inclusion inc = isFieldIncluded(field, absField, p.isInclude(), false);
        Inclusion inc2 = p.getProject().getFieldInclusion(field, new Path(absField, Path.ANYPATH));
        Inclusion ret;

        if (inc == Inclusion.explicit_inclusion || inc2 == Inclusion.explicit_inclusion) {
            ret = Inclusion.explicit_inclusion;
        } else if (inc == Inclusion.implicit_inclusion || inc2 == Inclusion.implicit_inclusion) {
            ret = Inclusion.implicit_inclusion;
        } else if (inc == Inclusion.explicit_exclusion || inc2 == Inclusion.explicit_exclusion) {
            ret = Inclusion.explicit_exclusion;
        } else if (inc == Inclusion.implicit_exclusion || inc2 == Inclusion.implicit_exclusion) {
            ret = Inclusion.implicit_exclusion;
        } else {
            ret = Inclusion.undecided;
        }
        LOGGER.debug("array projection on {} projects {}: {}", absField, field, ret);
        return ret;
    }

    private Inclusion getFieldInclusion(Path field, ProjectionList p, Path context) {
        LOGGER.debug("Checking if a projection list projects {}", field);
        Inclusion lastResult = Inclusion.undecided;
        List<Projection> items = p.getItems();
        ListIterator<Projection> itemsItr = items.listIterator(items.size());
        while (itemsItr.hasPrevious()) {
            Inclusion ret = itemsItr.previous().getFieldInclusion(field, context);
            if (ret != Inclusion.undecided) {
                lastResult = ret;
                break;
            }
        }
        LOGGER.debug("Projection list projects {}: {}", field, lastResult);
        return lastResult;
    }

    private Inclusion getFieldInclusion(Path field, FieldProjection p, Path context) {
        Path projectionField = new Path(context, toMask(p.getField()));
        LOGGER.debug("Checking if field projection on {} projects {}", projectionField, field);
        Inclusion inc = isFieldIncluded(field, projectionField, p.isInclude(), p.isRecursive());
        LOGGER.debug("Field projection on {} projects {}: {}", projectionField, field, inc);
        return inc;
    }

    /**
     * If a path includes array indexes, change the indexes into ANY
     */
    private static Path toMask(Path p) {
        int n = p.numSegments();
        MutablePath mp = null;
        for (int i = 0; i < n; i++) {
            if (p.isIndex(i)) {
                if (mp == null) {
                    mp = p.mutableCopy();
                }
                mp.set(i, Path.ANY);
            }
        }
        return mp == null ? p : mp.immutableCopy();
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
