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

public class RegexMatchExpression 
    extends RelationalExpression {

    private Path field;
    private String regex;
    private String options;

    public RegexMatchExpression() {}

    public RegexMatchExpression(Path field,String regex,String options) {
        this.field=field;
        this.regex=regex;
        this.options=options;
    }

    public Path getField() {
        return this.field;
    }

    public void setField(Path argField) {
        this.field = argField;
    }

    public String getRegex() {
        return this.regex;
    }

    public void setRegex(String argRegex) {
        this.regex = argRegex;
    }

    public String getOptions() {
        return this.options;
    }

    public void setOptions(String argOptions) {
        this.options = argOptions;
    }

    public JsonNode toJson() {
        ObjectNode node=factory.objectNode().
            put("field",field.toString()).
            put("regex",regex);
        if(options!=null)
            node.put("options",options);
        return node;
    }
}
