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

public final class Util {

    public static boolean isNumber(String value) {
        if (value != null) {
            int n = value.length();
            for (int i = 0; i < n; i++) {
                char x=value.charAt(i);
                if (!Character.isDigit(x)&&
                    !(i==0&&(x=='-'||x=='+')) ) {
                    return false;
                }
            }
            return n > 0;
        }
        return false;
    }
    

    private Util() {}
}
