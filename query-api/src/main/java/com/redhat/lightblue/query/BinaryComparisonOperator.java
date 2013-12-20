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

import java.util.HashMap;
import java.util.Map;

/**
 * Comparison operators
 */
public enum BinaryComparisonOperator { 
    _eq("$eq","="), 
    _neq("$neq","!="), 
    _lt("$lt","<"), 
    _gt("$gt",">"), 
    _lte("$lte","<="), 
    _gte("$gte",">=");

    private final String[] ops;

    private static final Map<String,BinaryComparisonOperator> map;

    static {
        map=new HashMap<String,BinaryComparisonOperator>();
        _eq.init(map);
        _neq.init(map);
        _lt.init(map);
        _gt.init(map);
        _lte.init(map);
        _gte.init(map);
    }    
                
    private void init(Map<String,BinaryComparisonOperator> map) {
        for(String x:ops) {
            map.put(x,this);
        }
    }

    private BinaryComparisonOperator(String... op) {
        this.ops=op;
    }
    
    /**
     * Applies the operator 
     * 
     * @param cmp Comparison result of x and y, wheres <0 denotes x<y,
     * >0 denotes x>y, and 0 denotes x=y
     *
     *  @return Returns the value of x op y based on the comparison result of x and y
     */
    public boolean apply(int cmp) {
        if(cmp<0) {
            return this==_neq||
                this==_lt||
                this==_lte;
        } else if(cmp==0) {
            return this==_eq||
                this==_lte||
                this==_gte;
        } else {
            return this==_neq||
                this==_gt||
                this==_gte;
        }
    }

    public String toString() {
        return ops[0];
    }

    public static BinaryComparisonOperator fromString(String s) {
        return map.get(s);
    }
}
