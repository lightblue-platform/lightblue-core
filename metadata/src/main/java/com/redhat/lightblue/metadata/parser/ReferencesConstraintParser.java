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
package com.redhat.lightblue.metadata.parser;

import java.util.List;
import java.util.ArrayList;

import com.redhat.lightblue.util.Error;

import com.redhat.lightblue.metadata.EntityConstraint;
import com.redhat.lightblue.metadata.MetadataParser;

import com.redhat.lightblue.metadata.constraints.ReferencesConstraint;
import com.redhat.lightblue.metadata.constraints.Reference;

public class ReferencesConstraintParser<T> implements EntityConstraintParser<T> {

    @Override
    public EntityConstraint parse(String name, MetadataParser<T> p, T node) {
        if (!ReferencesConstraint.REFERENCES.equals(name)) {
            Error.get(MetadataParser.ERR_ILL_FORMED_METADATA, name);
        }
        
        List<T> list = p.getObjectList(node, ReferencesConstraint.REFERENCES);
        ReferencesConstraint ret = new ReferencesConstraint();
        ArrayList<Reference> dest = new ArrayList<>();

        for (T item : list) {
            Reference ref = new Reference();
            ref.setEntityName(p.getStringProperty(item, "entityName"));
            ref.setVersionValue(p.getStringProperty(item, "versionValue"));
            ref.setThisField(p.getStringProperty(item, "thisField"));
            ref.setEntityField(p.getStringProperty(item, "entityField"));
            if (ref.getEntityName() == null) {
                throw Error.get(MetadataParser.ERR_ILL_FORMED_METADATA, "references.entityName");
            }
            if (ref.getVersionValue() == null) {
                throw Error.get(MetadataParser.ERR_ILL_FORMED_METADATA, "references.versionValue");
            }
            dest.add(ref);
        }
        ret.setReferences(dest);
        return ret;
    }

    @Override
    public void convert(MetadataParser<T> p, T emptyNode, EntityConstraint object) {
        Object arr = p.newArrayField(emptyNode, ReferencesConstraint.REFERENCES);
        for (Reference ref : ((ReferencesConstraint) object).getReferences()) {
            T node = p.newNode();
            if (ref.getEntityName() != null) {
                p.putString(node, "entityName", ref.getEntityName());
            }
            if (ref.getVersionValue() != null) {
                p.putString(node, "versionValue", ref.getVersionValue());
            }
            if (ref.getThisField() != null) {
                p.putString(node, "thisField", ref.getThisField());
            }
            if (ref.getEntityField() != null) {
                p.putString(node, "entityField", ref.getEntityField());
            }
            p.addObjectToArray(arr, node);
        }
    }
}
