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
package com.redhat.lightblue.assoc.ep;

public class Steps {


    /**
     * Sorts the results
     */
    public ResultStream<JsonDoc> sortStep(ResultStream<JsonDoc> in);

    /**
     * Filters docs
     */
    public ResultStream<JsonDoc> filterStep(ResultStream<JsonDoc> in);

    /**
     * Builds bindings from the stream of documents. For each
     * document, a document binding is generated containing possible
     * values of bound fields
     */
    public ResultStream<DocumentBinding> retrieveBindingsStep(ResultStream<JsonDoc> in);
    
    /**
     * Given parent and child document bindings, writes a stream of query expressions
     */
    public ResultStream<QueryExpression> bindStep(ResultStream<DocumentBinding> parent,
                                                  List<ResultStream<DocumentBinding>> children);
    

    /**
     * Returns the included fields for a given entity
     */
    public Set<Path> getIncludedFields(EntityMetadata md,QueryExpression q,Projection p,Sort s) {
        Set<Path> fields=new HashSet<>();
        FieldCursor cursor=md.getFieldCursor();
        // skipPrefix will be set to the root of a subtree that needs to be skipped.
        // If it is non-null, all fields with a prefix 'skipPrefix' will be skipped.
        Path skipPrefix=null;
        if(cursor.next()) {
            boolean done=false;
            do {
                Path field=cursor.getCurrentPath();
                if(skipPrefix!=null) {
                    if(!field.matchingDescendant(skipPrefix)) {
                        skipPrefix=null;
                    }
                }
                if(skipPrefix==null) {
                    FieldTreeNode node=cursor.getCurrentNode();
                    LOGGER.debug("Checking if {} is included ({})",field,node);
                    if(node instanceof ResolvedReferenceField||
                       node instanceof ReferenceField) {
                        skipPrefix=field;
                    } else  {
                        if( (node instanceof ObjectField) ||
                            (node instanceof ArrayField && ((ArrayField)node).getElement() instanceof ObjectArrayElement) ||
                            (node instanceof ArrayElement)) {
                            // include its member fields
                        } else if( (p!=null && p.isFieldRequiredToEvaluateProjection(field)) ||
                                   (q!=null && q.isRequired(field)) ||
                                   (s!=null && s.isRequired(field)) ) {
                            LOGGER.debug("{}: required",field);
                            fields.add(field);
                        } else {
                            LOGGER.debug("{}: not required", field);
                        }
                        done=!cursor.next();
                    }
                } else
                    done=!cursor.next();
            } while(!done);
        }
        return fields;
    }


    public void join(DocumentBinding parent,
                     List<DocumentBinding> children) {
                
    }
}

