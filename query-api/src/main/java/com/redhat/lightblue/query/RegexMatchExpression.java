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

/**
 * Represents a regular expression match query of the form
 * <pre>
 * { field: <field>, regex: <pattern>,  
 *      case_insensitive: false, extended: false, multiline: false, dotall: false }  
 * </pre>           
 * 
 */
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

    /**
     *  Constructs a regular expression match expression using the values
     */
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

    /**
     * Returns the field to me matched
     */
    public Path getField() {
        return this.field;
    }

    /**
     * Sets the field to be matched
     */
    public void setField(Path argField) {
        this.field = argField;
    }

    /**
     * Returns the regular expression
     */
    public String getRegex() {
        return this.regex;
    }

    /**
     * Sets the regular expression
     */
    public void setRegex(String argRegex) {
        this.regex = argRegex;
    }

    /**
     * Determines if the regex match will be case sensitive
     */
    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    /**
     * Determines if the regex match will be case sensitive
     */
    public void setCaseInsensitive(boolean b) {
        caseInsensitive=b;
    }

    /**
     * (from regex javadoc)
     * In multiline mode the expressions ^ and $ match just after or
     * just before, respectively, a line terminator or the end of the
     * input sequence. By default these expressions only match at the
     * beginning and the end of the entire input sequence.
     */
    public boolean isMultiline() {
        return multiline;
    }

    /**
     * (from regex javadoc)
     * In multiline mode the expressions ^ and $ match just after or
     * just before, respectively, a line terminator or the end of the
     * input sequence. By default these expressions only match at the
     * beginning and the end of the entire input sequence.
     */
    public void setMultiline(boolean b) {
        multiline=b;
    }

    /**
     * (from regex javadoc)
     *  In this mode, whitespace is ignored, and embedded comments
     *  starting with # are ignored until the end of a line.
     */
    public boolean isExtended() {
        return extended;
    }

    /**
     * (from regex javadoc)
     *  In this mode, whitespace is ignored, and embedded comments
     *  starting with # are ignored until the end of a line.
     */
    public void setExtended(boolean b) {
        extended=b;
    }

    /**
     * (from regex javadoc)
     * In dotall mode, the expression . matches any character,
     *  including a line terminator. By default this expression does
     *  not match line terminators.  
     */
    public boolean isDotAll() {
      return dotall; 
    }

    /**
     * (from regex javadoc)
     * In dotall mode, the expression . matches any character,
     *  including a line terminator. By default this expression does
     *  not match line terminators.  
     */
    public void setDotAll(boolean b) {
        dotall=b;
    }

    @Override
    public JsonNode toJson() {
        ObjectNode node=getFactory().objectNode().
            put("field",field.toString()).
            put("regex",regex);
        if(caseInsensitive) {
            node.put("case_insensitive",true);
        }
        if(multiline) {
            node.put("multiline",true);
        }
        if(extended) {
            node.put("extended",true);
        }
        if(dotall) {
            node.put("dotall",true);
        }
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
        if(node!=null) {
            return node.asBoolean();
        }
        return false;
    }
}
