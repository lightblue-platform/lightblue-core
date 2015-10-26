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
package com.redhat.lightblue.util;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class Measure {

    private final Map<String,Stat> map=new HashMap<>();

    private static class Stat {
        private long total;
        private long numSamples;
        private final ArrayList<Long> stack=new ArrayList<>(32);

        public void push(long l) {
            stack.add(l);
        }

        public void pop(long l) {
            int n=stack.size();
            if(n>0) {
                n--;
                long entry=stack.get(n);
                stack.remove(n);
                next(l-entry);
            }
        }

        public void next(long value) {
            total+=value;
            numSamples++;
        }

        public long avg() {
            return numSamples==0?0:total/numSamples;
        }

        public String toString() {
            return numSamples+","+total+","+avg();
        }
    }
    
    public void begin(String f) {
        Stat s=map.get(f);
        if(s==null)
            map.put(f,s=new Stat());
        s.push(System.nanoTime());
    }

    public void end(String f) {
        Stat s=map.get(f);
        if(s!=null)
            s.pop(System.nanoTime());
    }

    public String toString() {
        StringBuilder bld=new StringBuilder();
        for(Map.Entry<String,Stat> entry:map.entrySet())
            bld.append(entry.getKey()).append(',').append(entry.getValue().toString()).append('\n');
        return bld.toString();
    }
}

