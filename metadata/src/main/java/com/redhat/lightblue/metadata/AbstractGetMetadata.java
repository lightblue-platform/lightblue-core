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

import java.io.Serializable;

import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.ArrayQueryMatchProjection;
import com.redhat.lightblue.query.ArrayRangeProjection;
import com.redhat.lightblue.query.FieldProjection;
import com.redhat.lightblue.query.ProjectionList;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.ValueComparisonExpression;
import com.redhat.lightblue.query.FieldComparisonExpression;
import com.redhat.lightblue.query.RegexMatchExpression;
import com.redhat.lightblue.query.NaryRelationalExpression;
import com.redhat.lightblue.query.UnaryLogicalExpression;
import com.redhat.lightblue.query.NaryLogicalExpression;
import com.redhat.lightblue.query.ArrayContainsExpression;
import com.redhat.lightblue.query.ArrayMatchExpression;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.MutablePath;

/**
 * Default implementation of CompositeMetadata.GetMetadata interface
 * that limits the metadata retrieval using the query and
 * projections. To extend it to a concrete implementation, override
 * the retrieveMetadata method.
 *
 * This implementation will return metadata for entities
 * that are required to evaluate a set of projections and queries. The
 * expected usage of this class is to initialize it using projections
 * and queries from the request. Once initialized and passed to
 * CompositeMetadata, this class limits the depth of the composite
 * metadata to the minimal tree that includes all entities sufficient
 * to evaluate all projections and queries.
 *
 */
public abstract class AbstractGetMetadata implements CompositeMetadata.GetMetadata, Serializable {

    private static final long serialVersionUID=1l;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGetMetadata.class);

    private final List<Projection> projections=new ArrayList<>();
    private final List<QueryExpression> queries=new ArrayList<>();

    public AbstractGetMetadata() {}

    public AbstractGetMetadata(Projection projection,
                               QueryExpression query) {
        if(projection!=null)
            projections.add(projection);
        if(query!=null)
            queries.add(query);
    }

    public AbstractGetMetadata(List<Projection> projections,
                               List<QueryExpression> queries) {
        if(projections!=null)
            this.projections.addAll(projections);
        if(queries!=null)
            this.queries.addAll(queries);
    }

    public void add(Projection p) {
        projections.add(p);
    }

    public void add(QueryExpression q) {
        queries.add(q);
    }

    /**
     * This implementation will return the metadata for an entity if
     * that entity is required by the given projections and queries.
     */
    @Override
    public EntityMetadata getMetadata(Path injectionField,
                                      String entityName,
                                      String version) {
        // See if injectionField is projected or used in a query
        if(isProjected(injectionField)||
           isQueried(injectionField))
            return retrieveMetadata(injectionField,entityName,version);
        return null;
    }

    /**
     * The implementation should retrieve and return the metadata for
     * the given version of the given entity
     */
    protected abstract EntityMetadata retrieveMetadata(Path injectionField,
                                                       String entityName,
                                                       String version);

    private boolean isProjected(Path field) {
        LOGGER.debug("Checking if {} is projected",field);
        for(Projection p:projections) {
            Boolean x=isRequired(field,p,Path.EMPTY);
            if(x!=null&&x)
                return true;
        }
        return false;
    }

    private boolean isQueried(Path field) {
        LOGGER.debug("Checking if {} is queried",field);
        for(QueryExpression q:queries)
            if(isRequired(field,q,Path.EMPTY))
                return true;
        return false;
    }

    private boolean isRequired(Path field,QueryExpression q,Path ctx) {
        if (q instanceof ValueComparisonExpression) {
            return isFieldQueried(field,(ValueComparisonExpression)q,ctx);
        } else if (q instanceof FieldComparisonExpression) {
            return isFieldQueried(field,(FieldComparisonExpression)q,ctx);
        } else if (q instanceof RegexMatchExpression) {
            return isFieldQueried(field,(RegexMatchExpression)q,ctx);
        } else if (q instanceof NaryRelationalExpression) {
            return isFieldQueried(field,(NaryRelationalExpression)q,ctx);
        } else if (q instanceof UnaryLogicalExpression) {
            return isFieldQueried(field,(UnaryLogicalExpression)q,ctx);
        } else if (q instanceof NaryLogicalExpression) {
            return isFieldQueried(field,(NaryLogicalExpression)q,ctx);
        } else if (q instanceof ArrayContainsExpression) {
            return isFieldQueried(field,(ArrayContainsExpression)q,ctx);
        } else if (q instanceof ArrayMatchExpression) {
            return isFieldQueried(field,(ArrayMatchExpression)q,ctx);
        }
        return false;
    }

    private Boolean isRequired(Path field,Projection p,Path ctx) {
        Path mfield=toMask(field);
        if(p instanceof FieldProjection) {
            return isFieldProjected(mfield,(FieldProjection)p,ctx);
        } else if(p instanceof ArrayQueryMatchProjection) {
            return isFieldProjected(mfield,(ArrayQueryMatchProjection)p,ctx);
        } else if(p instanceof ArrayRangeProjection) {
            return isFieldProjected(mfield,(ArrayRangeProjection)p,ctx);
        } else if(p instanceof ProjectionList) {
            return isFieldProjected(mfield,(ProjectionList)p,ctx);
        }
        return null;
    }

    private Boolean isFieldProjected(Path field,ArrayQueryMatchProjection p,Path context) {
        Path absField=new Path(context,toMask(p.getField()));
        LOGGER.debug("Checking if array query match projection on {} projects {}",absField,field);
        Projection.Inclusion inc=Projection.isFieldIncluded(field,absField,p.isInclude(),false);
        if(inc==Projection.Inclusion.undecided) {
            LOGGER.debug("No match, checking if query requires the field");
            return isRequired(field,p.getMatch(),new Path(absField,Path.ANYPATH));
        } else {
            LOGGER.debug("array query match projection on {} projects {}: {}",absField,field,inc);
            switch(inc) {
            case explicit_inclusion: return Boolean.TRUE;
            case explicit_exclusion: return Boolean.FALSE;
            default: return null;
            }
        }
    }

    private Boolean isFieldProjected(Path field,ArrayRangeProjection p,Path context) {
        Path absField=new Path(context,toMask(p.getField()));
        LOGGER.debug("Checking if array range projection on {} projects {}",absField,field);
        Projection.Inclusion inc=Projection.isFieldIncluded(field,absField,p.isInclude(),false);
        LOGGER.debug("array range projection on {} projects {}: {}",absField,field,inc);
        switch(inc) {
        case explicit_inclusion: return Boolean.TRUE;
        case explicit_exclusion: return Boolean.FALSE;
        default: return null;
        }
    }

    private Boolean isFieldProjected(Path field,ProjectionList p,Path context) {
        LOGGER.debug("Checking if a projection list projects {}",field);
        Boolean lastResult=null;
        for(Projection x:p.getItems()) {
            Boolean ret=isRequired(field,x,context);
            if(ret!=null) {
                lastResult=ret;
            } 
        }
        LOGGER.debug("Projection list projects {}: {}",field,lastResult);
        return lastResult;
    }

    private Boolean isFieldProjected(Path field,FieldProjection p,Path context) {
        Path absField=new Path(context,toMask(p.getField()));
        LOGGER.debug("Checking if field projection on {} projects {}",absField,field);
        Projection.Inclusion inc=Projection.isFieldIncluded(field,absField,p.isInclude(),false);
        LOGGER.debug("Field projection on {} projects {}: {}",absField,field,inc);
        switch(inc) {
        case explicit_inclusion: return Boolean.TRUE;
        case explicit_exclusion: return Boolean.FALSE;
        default: return null;
        }
    }

    private boolean isFieldQueried(Path field,Path qField,Path context) {
        Path absField=new Path(context,qField);
        if(field.matchingPrefix(absField)) {
            LOGGER.debug("Field {} is queried",absField);
            return true; 
        } else {
            return false;
        }
    }

    private boolean isFieldQueried(Path field,ValueComparisonExpression q,Path context) {
        LOGGER.debug("Checking if field {} is queried by value comparison {}",field,q);
        return isFieldQueried(field,q.getField(),context);
    }

    private boolean isFieldQueried(Path field,FieldComparisonExpression q,Path context) {
        LOGGER.debug("Checking if field {} is queried by field comparison {}",field,q);
        return isFieldQueried(field,q.getField(),context)||
            isFieldQueried(field,q.getRfield(),context);
    }
    
    private boolean isFieldQueried(Path field,RegexMatchExpression q,Path context) {
        LOGGER.debug("Checking if field {} is queried by regex {}",field,q);
        return isFieldQueried(field,q.getField(),context);
    }

    private boolean isFieldQueried(Path field,NaryRelationalExpression q,Path context) {
        LOGGER.debug("Checking if field {} is queried by expr {}",field,q);
        return isFieldQueried(field,q.getField(),context);
    }

    private boolean isFieldQueried(Path field,UnaryLogicalExpression q,Path context) {
        return isRequired(field,q.getQuery(),context);
    }

    private boolean isFieldQueried(Path field,NaryLogicalExpression q,Path context) {
        for(QueryExpression x:q.getQueries()) {
            if(isRequired(field,x,context))
                return true;
        }
        return false;
    }

    private boolean isFieldQueried(Path field,ArrayContainsExpression q,Path context) {
        LOGGER.debug("Checking if field {} is queried by array expression {}",field,q);
        return isFieldQueried(field,q.getArray(),context);
    }

    private boolean isFieldQueried(Path field,ArrayMatchExpression q,Path context) {
        LOGGER.debug("Checking if field {} is queried by array expression {}",field,q);
        if(isFieldQueried(field,q.getArray(),context)) {
            return true;
        } else {
            return isRequired(field,q.getElemMatch(),new Path(new Path(context,field),Path.ANYPATH));
        }
    }

    /**
     * If a path includes array indexes, change the indexes into ANY
     */
    private static Path toMask(Path p) {
        int n=p.numSegments();
        MutablePath mp=null;
        for(int i=0;i<n;i++)
            if(p.isIndex(i)) {
                if(mp==null) {
                    mp=p.mutableCopy();
                }
                mp.set(i,Path.ANY);
            }
        return mp==null?p:mp.immutableCopy();
    }
}
