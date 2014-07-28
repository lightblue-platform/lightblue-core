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
package com.redhat.lightblue.metadata.rdbms.model;

import com.redhat.lightblue.metadata.parser.MetadataParser;
import com.redhat.lightblue.metadata.rdbms.converter.ComplexConverter;
import com.redhat.lightblue.metadata.rdbms.parser.SimpleParser;
import com.redhat.lightblue.metadata.rdbms.util.RDBMSMetadataConstants;
import java.util.ArrayList;
import java.util.List;

public abstract class If<Z extends If,N extends If> implements ComplexConverter, SimpleParser<N> {

    private static If ifChain;
    private If next;
    static {
        ifChain = new If() {
            @Override public <T> void convert(MetadataParser<T> p, Object lastArrayNode, T node) {throw new UnsupportedOperationException("Not supported yet.");}
            @Override public If parse(MetadataParser p, Object node) {
                If n = this.next();
                while (n != null){
                    If parsed = (If) n.parse(p, node);
                    if(parsed != null) {
                        return parsed;
                    }
                    n = n.next();
                }
                throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_WRONG_FIELD, "No valid if field was set ->" + node.toString());
            }
        };
        
        ifChain.next = new IfOr();
        ifChain.next.next = new IfAnd();
        ifChain.next.next.next = new IfNot();
        ifChain.next.next.next.next = new IfFieldEmpty();
        ifChain.next.next.next.next.next = new IfFieldCheckField();
        ifChain.next.next.next.next.next.next = new IfFieldCheckValue();
        ifChain.next.next.next.next.next.next.next = new IfFieldCheckValues();
        ifChain.next.next.next.next.next.next.next.next = new IfFieldRegex();        
    }
    
    public static If getChain(){
        return ifChain;
    }
    
    public static <T,Z extends If> Z parseIfs(MetadataParser<T> p, List<T> orArray, Z ifC) {
        List<If> l = new ArrayList<>();
        for (T t : orArray) {
            If eiIf = (If) getChain().parse(p, t);
            l.add(eiIf);
        }
        ifC.setConditions(l);
        return ifC;
    }
    
    private List<Z> conditions;
    
    If next(){
        return this.next;
    }
    
    public void setConditions(List<Z> conditions) {
        this.conditions = conditions;
    }

    public List<Z> getConditions() {
        return conditions;
    }
}
