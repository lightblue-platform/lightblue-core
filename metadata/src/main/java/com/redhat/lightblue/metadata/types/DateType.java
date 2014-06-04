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
package com.redhat.lightblue.metadata.types;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.metadata.MetadataConstants;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.util.Error;

public final class DateType implements Type, Serializable {

    private static final long serialVersionUID = 1l;

    public static final Type TYPE = new DateType();
    public static final String NAME = "date";

    public static final String DATE_FORMAT_STR = "yyyyMMdd'T'HH:mm:ss.SSSZ";
    private static final DateFormat DATE_FORMAT;

    static {
        DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STR);
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * Returns a DateFormat instance using the DATE_FORMAT_STR in GMT. Clone of the static internal variable, because
     * SimpleDateFormat is not thread safe
     */
    public static DateFormat getDateFormat() {
        return (DateFormat) DATE_FORMAT.clone();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean supportsEq() {
        return true;
    }

    @Override
    public boolean supportsOrdering() {
        return true;
    }

    @Override
    public JsonNode toJson(JsonNodeFactory factory, Object obj) {
        return factory.textNode(getDateFormat().format((Date) cast(obj)));
    }

    @Override
    public Object fromJson(JsonNode node) {
        if (node.isValueNode()) {
            try {
                return getDateFormat().parse(node.asText());
            } catch (ParseException e) {
                throw Error.get(NAME, MetadataConstants.ERR_INCOMPATIBLE_VALUE, node.toString());
            }
        } else {
            throw Error.get(NAME, MetadataConstants.ERR_INCOMPATIBLE_VALUE, node.toString());
        }
    }

    @Override
    public Object cast(Object obj) {
        Date value = null;
        if (obj != null) {
            DateFormat fmt = getDateFormat();
            if (obj instanceof Date) {
                value = (Date) obj;
            } else if (obj instanceof String) {
                try {
                    value = fmt.parse((String) obj);
                } catch (ParseException e) {
                    throw Error.get(NAME, MetadataConstants.ERR_INCOMPATIBLE_VALUE, obj.toString());
                }
            } else {
                throw Error.get(NAME, MetadataConstants.ERR_INCOMPATIBLE_VALUE, obj.toString());
            }
        }
        return value;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public int compare(Object v1, Object v2) {
        if (v1 == null) {
            if (v2 == null) {
                return 0;
            } else {
                return -1;
            }
        } else if (v2 == null) {
            return 1;
        } else {
            return ((Comparable) cast(v1)).compareTo(cast(v2));
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DateType;
    }

    @Override
    public int hashCode() {
        return 7;
    }

    @Override
    public String toString() {
        return NAME;
    }

    private DateType() {
    }
}
