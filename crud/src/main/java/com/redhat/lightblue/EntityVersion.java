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
package com.redhat.lightblue;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents entity:value, a particular version of the entity metadata
 */
public final class EntityVersion implements Serializable {

    private static final long serialVersionUID = 1l;

    private String entity;
    private String version;

    public EntityVersion() {
    }

    public EntityVersion(String entity, String version) {
        this.entity = entity;
        this.version = version;
    }

    /**
     * The entity name
     */
    public String getEntity() {
        return entity;
    }

    /**
     * The entity name
     */
    public void setEntity(String entity) {
        this.entity = entity;
    }

    /**
     * The entity version
     */
    public String getVersion() {
        return version;
    }

    /**
     * The entity version
     */
    public void setVersion(String v) {
        version = v;
    }

    public boolean equals(Object o) {
        if (o instanceof EntityVersion) {
            return equalsVersion((EntityVersion) o);
        } else {
            return false;
        }
    }

    public boolean equalsVersion(EntityVersion o) {
        return o == null ? false : Objects.equals(entity, o.getEntity()) && Objects.equals(version, o.getVersion());
    }

    public int hashCode() {
        return (entity == null ? 1 : entity.hashCode()) * (version == null ? 1 : version.hashCode());
    }

    public String toString() {
        return entity + ":" + version;
    }
}
