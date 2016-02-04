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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.util.Constants;
import com.redhat.lightblue.util.Error;

public class DateTypeTest {

    Type dateType;

    @Before
    public void setUp() throws Exception {
        dateType = DateType.TYPE;
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testIsAContainerType() {
        assertTrue(ContainerType.class.isAssignableFrom(ArrayType.class));
    }

    @Test
    public void testGetName() {
        assertEquals(dateType.getName(), DateType.NAME);
    }

    @Test
    public void testSupportsEq() {
        assertTrue(dateType.supportsEq());
    }

    @Test
    public void testSupportsOrdering() {
        assertTrue(dateType.supportsOrdering());
    }

    @Test
    public void testToJson() {
        DateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_STR);
        //dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String date = dateFormat.format(new Date());
        JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(true);
        JsonNode jsonNode = dateType.toJson(jsonNodeFactory, date);
        assertTrue(jsonNode.asText().equals(date));
    }

    @Test
    public void testFromJson() {
        JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).textNode(new SimpleDateFormat(Constants.DATE_FORMAT_STR).format(new Date()));
        Object fromJson = dateType.fromJson(jsonNode);
        assertTrue(fromJson instanceof Date);
    }

    @Test(expected = Error.class)
    public void testFromJsonWithBadValue() {
        JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).textNode("badstring");
        dateType.fromJson(jsonNode);
    }

    @Test(expected = Error.class)
    public void testFromJsonWithIncompatibleValue() {
        JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).objectNode();
        dateType.fromJson(jsonNode);
    }

    @Test
    public void testCastNull() {
        assertNull(dateType.cast(null));
    }

    @Test
    public void testCastDate() {
        assertTrue(dateType.cast(new Date()) instanceof Date);
    }

    @Test
    public void testCastString() {
        assertTrue(dateType.cast(new SimpleDateFormat(Constants.DATE_FORMAT_STR).format(new Date())) instanceof Date);
    }

    @Test(expected = Error.class)
    public void testCastBadString() {
        assertTrue(dateType.cast("badstring") instanceof Date);
    }

    @Test(expected = Error.class)
    public void testCastOther() {
        Object object = new Object();
        dateType.cast(object);
    }

    @Test
    public void testCompareBothNull() {
        assertEquals(dateType.compare(null, null), 0);
    }

    @Test
    public void testCompareV1Null() {
        assertEquals(dateType.compare(null, new Object()), -1);
    }

    @Test
    public void testCompareV2Null() {
        assertEquals(dateType.compare(new Object(), null), 1);
    }

    @Test
    public void testCompareEqual() {
        Date date = new Date();
        assertEquals(dateType.compare(date, date), 0);
    }

    @Test
    public void testCompareNotEqual() {
        Date date1 = new Date(new GregorianCalendar(2014, 00, 14).getTimeInMillis());
        Date date2 = new Date(new GregorianCalendar(2014, 00, 15).getTimeInMillis());
        assertEquals(dateType.compare(date1, date2), -1);
    }

    @Test
    public void testEqualsTrue() {
        assertTrue(dateType.equals(DateType.TYPE));
    }

    @Test
    public void testEqualsFalse() {
        assertFalse(dateType.equals(Double.MAX_VALUE));
    }

    @Test
    public void testHashCode() {
        assertEquals(dateType.hashCode(), 7);
    }

    @Test
    public void testToString() {
        assertEquals(dateType.toString(), DateType.NAME);
    }

}
