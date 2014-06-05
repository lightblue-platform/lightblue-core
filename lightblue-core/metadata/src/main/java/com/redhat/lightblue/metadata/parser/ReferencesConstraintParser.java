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

import com.redhat.lightblue.metadata.MetadataConstants;
import com.redhat.lightblue.metadata.FieldConstraint;
import com.redhat.lightblue.metadata.constraints.Reference;
import com.redhat.lightblue.metadata.constraints.ReferencesConstraint;
import com.redhat.lightblue.util.Error;

public class ReferencesConstraintParser<T> implements FieldConstraintParser<T> {

    @Override
    public FieldConstraint parse(String name, MetadataParser<T> p, T node) {
        if (!ReferencesConstraint.REFERENCES.equals(name)) {
            throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, name);
        }

        T item = p.getObjectProperty(node, ReferencesConstraint.REFERENCES);
        ReferencesConstraint ret = new ReferencesConstraint();
        Reference ref = new Reference();
        ref.setEntityName(p.getStringProperty(item, "entityName"));
        ref.setVersionValue(p.getStringProperty(item, "versionValue"));
        ref.setEntityField(p.getStringProperty(item, "entityField"));
        if (ref.getEntityName() == null) {
            throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, "references.entityName");
        }
        if (ref.getVersionValue() == null) {
            throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, "references.versionValue");
        }
        ret.setReference(ref);
        return ret;
    }

    @Override
    public void convert(MetadataParser<T> p, T emptyNode, FieldConstraint object) {
        T node = p.newNode();
        Reference ref = ((ReferencesConstraint) object).getReference();
        if (ref.getEntityName() != null) {
            p.putString(node, "entityName", ref.getEntityName());
        }
        if (ref.getVersionValue() != null) {
            p.putString(node, "versionValue", ref.getVersionValue());
        }
        if (ref.getEntityField() != null) {
            p.putString(node, "entityField", ref.getEntityField());
        }
        p.putObject(emptyNode, ReferencesConstraint.REFERENCES, node);
    }

}
