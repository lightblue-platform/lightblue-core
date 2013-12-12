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
import com.redhat.lightblue.util.Error;

public class RegexMatchExpression 
    extends RelationalExpression {

    public static final String INVALID_REGEX_EXPRESSION="INVALID_REGEX_EXPRESSION";

    private Path field;
    private String regex;
    private boolean caseInsensitive;
    private boolean multiline;
    private boolean extended;
    private boolean dotall;

    public RegexMatchExpression() {}

    public RegexMatchExpression(Path field,String regex,
                                boolean caseInsensitive,
                                boolean multiline,
                                boolean extended,
                                boolean dotall) {
        this.field=field;
        this.regex=regex;
        this.caseInsensitive=caseInsensitive;
        this.multiline=multiline;
        this.extended=extended;
        this.dotall=dotall;
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

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    public void setCaseInsensitive(boolean b) {
        caseInsensitive=b;
    }

    public boolean isMultiline() {
        return multiline;
    }

    public void setMultiline(boolean b) {
        multiline=b;
    }

    public boolean isExtended() {
        return extended;
    }

    public void setExtended(boolean b) {
        extended=b;
    }

    public boolean isDotAll() {
        return dotall;
    }

    public void setDotAll(boolean b) {
        dotall=b;
    }

    public JsonNode toJson() {
        ObjectNode node=factory.objectNode().
            put("field",field.toString()).
            put("regex",regex);
        if(caseInsensitive)
            node.put("case_insensitive",true);
        if(multiline)
            node.put("multiline",true);
        if(extended)
            node.put("extended",true);
        if(dotall)
            node.put("dotall",true);
        return node;
    }

    public static RegexMatchExpression fromJson(ObjectNode node) {
        JsonNode x=node.get("field");
        if(x!=null) {
            Path field=new Path(x.asText());
            x=node.get("regex");
            if(x!=null) {
                String regex=x.asText();
                return new RegexMatchExpression(field,regex,
                                                asBoolean(node.get("case_insensitive")),
                                                asBoolean(node.get("multiline")),
                                                asBoolean(node.get("extended")),
                                                asBoolean(node.get("dotall")));
            }
        }
      throw Error.get(INVALID_REGEX_EXPRESSION,node.toString());
    }

    private static boolean asBoolean(JsonNode node) {
        if(node!=null)
            return node.asBoolean();
        return false;
    }
}
