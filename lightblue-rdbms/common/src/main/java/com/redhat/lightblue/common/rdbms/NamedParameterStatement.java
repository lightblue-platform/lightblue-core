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
package com.redhat.lightblue.common.rdbms;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A simple PreparedStatement wrapper to make SQL statements easier
 * Source inspired from  http://www.javaworld.com/article/2077706/core-java/named-parameters-for-preparedstatement.html
 * and
 * https://github.com/bnewport/Samples/blob/master/wxsutils/src/main/java/com/devwebsphere/jdbc/loader/NamedParameterStatement.java
 */
public class NamedParameterStatement {
    /** The statement this object is wrapping. */
    private final PreparedStatement statement;

    /** Maps parameter names to arrays of integers which are the parameter indices. */
    private Map<String, int[]> indexMap;

    public NamedParameterStatement(Connection connection, String query) throws SQLException {
        String parsedQuery = parse(query);
        statement = connection.prepareStatement(parsedQuery);
    }

    private String parse(String query) {
        // I was originally using regular expressions, but they didn't work well for ignoring
        // parameter-like strings inside quotes.
        int length = query.length();
        StringBuilder parsedQuery = new StringBuilder(length);
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        int index = 1;
        HashMap<String, List<Integer>> indexes = new HashMap<>(15); // Setting a good initialCapacity for a better performance

        for (int i = 0; i < length; i++) {
            char c = query.charAt(i);
            if (inSingleQuote) {
                if (c == '\'') {
                    inSingleQuote = false;
                }
            } else if (inDoubleQuote) {
                if (c == '"') {
                    inDoubleQuote = false;
                }
            } else {
                if (c == '\'') {
                    inSingleQuote = true;
                } else if (c == '"') {
                    inDoubleQuote = true;
                } else if (c == ':' && i + 1 < length && Character.isJavaIdentifierStart(query.charAt(i + 1))) {//isJavaIdentifierStart-> make sure that the parameter's chars are all qualified for java (which would be more readable)
                    int j = i + 2;
                    while (j < length && Character.isJavaIdentifierPart(query.charAt(j))) {
                        j++;
                    }
                    String name = query.substring(i + 1, j);
                    c = '?'; // replace the parameter with a question mark
                    i += name.length(); // skip past the end if the parameter

                    List<Integer> indexList = indexes.get(name);
                    if (indexList == null) {
                        indexList = new LinkedList<>();
                        indexes.put(name, indexList);
                    }
                    indexList.add(index);

                    index++;
                }
            }
            parsedQuery.append(c);
        }

        indexMap = new HashMap<>(indexes.size());
        // replace the lists of Integer objects with arrays of integers
        for (Map.Entry<String, List<Integer>> entry : indexes.entrySet()) {
            List<Integer> list = entry.getValue();
            int[] intIndexes = new int[list.size()];
            int i = 0;
            for (Integer x : list) {
                intIndexes[i++] = x;
            }
            indexMap.put(entry.getKey(), intIndexes);
        }

        return parsedQuery.toString();
    }

    private int[] getIndexes(String name) {
        int[] indexes = indexMap.get(name);
        if (indexes == null) {
            throw new IllegalStateException("Parameter not found: " + name);
        }
        return indexes;
    }

    public void setObject(String name, Object value) throws SQLException {
        int[] indexes = getIndexes(name);
        for (int index : indexes) {
            statement.setObject(index, value);
        }
    }

    public void setString(String name, String value) throws SQLException {
        int[] indexes = getIndexes(name);
        for (int index : indexes) {
            statement.setString(index, value);
        }
    }

    public void setInt(String name, int value) throws SQLException {
        int[] indexes = getIndexes(name);
        for (int index : indexes) {
            statement.setInt(index, value);
        }
    }

    public void setLong(String name, long value) throws SQLException {
        int[] indexes = getIndexes(name);
        for (int index : indexes) {
            statement.setLong(index, value);
        }
    }

    public void setTimestamp(String name, Timestamp value) throws SQLException {
        int[] indexes = getIndexes(name);
        for (int index: indexes) {
            statement.setTimestamp(index, value);
        }
    }

    public boolean execute() throws SQLException {
        return statement.execute();
    }

    public ResultSet executeQuery() throws SQLException {
        return statement.executeQuery();
    }

    public int executeUpdate() throws SQLException {
        return statement.executeUpdate();
    }


    public void close() throws SQLException {
        statement.close();
    }
}