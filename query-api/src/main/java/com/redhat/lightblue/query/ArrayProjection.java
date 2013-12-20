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
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.util.Path;

public abstract class ArrayProjection extends BasicProjection {

    private Path field;
    private boolean include;
    private Projection project;

    public ArrayProjection() {}

    public ArrayProjection(Path field,
                           boolean include,
                           Projection project) {
        this.field=field;
        this.include=include;
        this.project=project==null?FieldProjection.ALL:project;
    }

    public Path getField() {
        return this.field;
    }

    public void setField(Path argField) {
        this.field = argField;
    }

    public boolean isInclude() {
        return this.include;
    }

    public void setInclude(boolean argInclude) {
        this.include = argInclude;
    }

    public Projection getProject() {
        return this.project;
    }

    public void setProject(Projection argProject) {
        this.project = argProject;
    }

    public JsonNode toJson() {
        ObjectNode node=factory.objectNode().
            put("field",field.toString()).
            put("include",include);
        if(project!=null) {
            node.set("project",project.toJson());
        }
        return node;
    }
            
}
