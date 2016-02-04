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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.util.Path;

public class ArrayRangeProjection extends ArrayProjection {

	private static final long serialVersionUID = 1L;

	private final Integer from;
	private final Integer to;

	public ArrayRangeProjection(Path field, boolean include, Projection project, Sort sort, Integer from, Integer to) {
		super(field, include, project, sort);
		this.from = from;
		this.to = to;
	}

	public ArrayRangeProjection(Path field, boolean include, Projection project, Integer from, Integer to) {
		this(field, include, project, null, from, to);
	}

	public Integer getFrom() {
		return this.from;
	}

	public Integer getTo() {
		return this.to;
	}

	@Override
	public JsonNode toJson() {
		ArrayNode arr = getFactory().arrayNode();
		if (from == null) {
			arr.add(getFactory().nullNode());
			if (to == null) {
				arr.add(getFactory().nullNode());
			} else {
				arr.add(getFactory().numberNode(to));
			}
		} else if (from != null) {
			arr.add(getFactory().numberNode(from));
			if (to == null) {
				arr.add(getFactory().nullNode());
			} else {
				arr.add(getFactory().numberNode(to));
			}
		}
		return ((ObjectNode) super.toJson()).set("range", arr);
	}
}
