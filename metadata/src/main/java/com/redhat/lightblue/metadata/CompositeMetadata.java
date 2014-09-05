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

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.MutablePath;

/**
 * Composite metedata is a directed tree. The requested entity is at
 * the root of the composite metadata. Every entity arrived by
 * following an association is another node in the composite metadata,
 * and the edge points to the destination of the association.
 *
 * Composite metadata extends EntityMetadata with these functions:
 * <ul>
 * <li>The tree structure of entities are visible through CompositeMetadata</li>
 * <li>Reference fields are extended to include the projected fields from
 * the associated entities</li>
 * </ul>
 *
 * Composite metadata needs to be computed for every request. The
 * computation takes into account the request queries and projections
 * to determine how deep the reference tree needs to be traversed.
 */
public class CompositeMetadata extends EntityMetadata {

    private final Path entityPath;
    private final CompositeMetadata parent;
    private final Map<Path,CompositeMetadata> children=new HashMap<>();

    /**
     * Interface that returns an instance of entity metadata given the
     * entity name, version, and the field into which entity needs to
     * be injected. The logic to include or exclude the entity based
     * on the projection, query, and sort requirements is implemented
     * by this class. If an instance of entity metadata is not to be
     * included in the composite metadata, the getMetadata method must
     * return null.
     *
     * If a particular version of a requested entity has references,
     * the getMetadata method should return an instance of
     * EntityMetadata, not CompositeMetadata. The caller will
     * recursively descend into all the entities retrieved and
     * construct the top-level composite metadata.
     *
     * If metadata cannot be retrieved, this call should throw an
     * exception. Returning null means the particular entity metadata
     * is not projected.
     */
    public static interface GetMetadata {
        public EntityMetadata getMetadata(Path injectionField,
                                          String entityName,
                                          String version);
    }

    /**
     * Construct a composite metadata using the given entity info and
     * composite schema. This constructor is to construct a
     * CompositeMetadata at the root of an entity tree.
     */
    public CompositeMetadata(EntityInfo info,
                             CompositeSchema schema) {
        this(info,schema,new Path(),null);        
    }

    public CompositeMetadata(EntityInfo info,
                             CompositeSchema schema,
                             Path path,
                             CompositeMetadata parent) {
        super(info,schema);
        this.entityPath=path;
        this.parent=parent;
    }

    public Path getEntityPath() {
        return entityPath;
    }

    public CompositeMetadata getParent() {
        return parent;
    }

    public CompositeMetadata getChild(Path entityPath) {
        return children.get(entityPath);
    }

    public Set<Path> getChildNames() {
        return children.keySet();
    }

    public static CompositeMetadata buildCompositeMetadata(EntityMetadata root,
                                                           GetMetadata gmd) {
        return buildCompositeMetadata(root,gmd,new Path(),null,new MutablePath());
    }

    private static CompositeMetadata buildCompositeMetadata(EntityMetadata root,
                                                            GetMetadata gmd,
                                                            Path entityPath,
                                                            CompositeMetadata parentEntity,
                                                            MutablePath path) {
        // Recursively process and copy the fields, retrieving
        // metadata for references
        CompositeSchema cschema=CompositeSchema.newSchemaWithEmptyFields(root.getEntitySchema());
        CompositeMetadata cmd=new CompositeMetadata(root.getEntityInfo(),cschema,entityPath,parentEntity);
        // Shallow-copy replace references
        copyFields(cschema.getFields(),root.getEntitySchema().getFields(),path,cmd,gmd);
        return cmd;
    }

    private static void copyFields(Fields dest,
                                   Fields source,
                                   MutablePath path,
                                   CompositeMetadata parentEntity,
                                   GetMetadata gmd) {
        for(Iterator<Field> itr=source.getFields();itr.hasNext();) {
            Field field=itr.next();            
            if(field instanceof SimpleField) {
                dest.put(field);
            } else {
                path.push(field.getName());
                if(field instanceof ObjectField) {
                    ObjectField newField=new ObjectField(field.getName());
                    shallowCopy(newField,field);
                    copyFields(newField.getFields(),
                               ((ObjectField)field).getFields(),
                               path,
                               parentEntity,
                               gmd);
                    dest.put(newField);
                } else if(field instanceof ArrayField) {
                    ArrayElement sourceEl=((ArrayField)field).getElement();
                    if(sourceEl instanceof SimpleArrayElement) {
                        // No need to copy a simple array
                        dest.put(field);
                    } else {
                        // Need to copy an Object array, there is a Fields object in it
                        path.push(Path.ANY);
                        ObjectArrayElement newEl=new ObjectArrayElement();
                        newEl.getProperties().putAll(sourceEl.getProperties());
                        copyFields(newEl.getFields(),
                                   ((ObjectArrayElement)sourceEl).getFields(),
                                   path,
                                   parentEntity,
                                   gmd);
                        ArrayField newField=new ArrayField(field.getName(),newEl);
                        shallowCopy(newField,field);
                        dest.put(newField);
                        path.pop();
                    }
                } else {
                    // Field is a reference
                    ReferenceField reference=(ReferenceField)field;
                    ResolvedReferenceField newField=resolveReference(dest,reference,path,parentEntity,gmd);
                    if(newField!=null)
                        dest.put(newField);
                }
                path.pop();
            }

        }
        dest.getProperties().putAll(source.getProperties());
    }

    private static ResolvedReferenceField resolveReference(Fields dest,
                                                           ReferenceField source,
                                                           MutablePath path,
                                                           CompositeMetadata parentEntity,
                                                           GetMetadata gmd) {
        EntityMetadata md=gmd.getMetadata(path.immutableCopy(),
                                          source.getEntityName(),
                                          source.getVersionValue());
        // If metadata is null, the entity is not projected, so we
        // don't even set it in the containing Fields.  If somehow the
        // GetMetadata cannot retrieve the metadata, it throws an
        // exception.
        if(md!=null) {
            // We have the entity metadata. We insert this as a
            // resolved reference
            Path fpath=path.immutableCopy();
            path.push(Path.ANY);
            CompositeMetadata cmd=buildCompositeMetadata(md,gmd,fpath,parentEntity,path);
            path.pop();
            parentEntity.children.put(fpath,cmd);
            ResolvedReferenceField newField=new ResolvedReferenceField(source,cmd);
            return newField;
        } 
        return null;
    }

    private static void shallowCopy(Field dest,Field source) {
        dest.setType(source.getType());

        FieldAccess da=dest.getAccess();
        FieldAccess sa=source.getAccess();
        da.getFind().setRoles(sa.getFind());
        da.getUpdate().setRoles(sa.getUpdate());
        da.getInsert().setRoles(sa.getInsert());
        dest.setConstraints(source.getConstraints());
        dest.getProperties().putAll(source.getProperties());
    }

    public String toTreeString() {
        StringBuilder bld=new StringBuilder();
        toTreeString(0, bld);
        return bld.toString();
    }

    private void toTreeString(int depth,StringBuilder bld) {
        for(int i=0;i<depth;i++)
            bld.append("  ");
        bld.append(getName()).append(':').append(entityPath.toString()).append('\n');
        for(CompositeMetadata ch:children.values())
            ch.toTreeString(depth+1,bld);
    }
}
