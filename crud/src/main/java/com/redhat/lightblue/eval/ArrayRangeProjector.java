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
package com.redhat.lightblue.eval;

import com.redhat.lightblue.util.Path;

import com.redhat.lightblue.metadata.FieldTreeNode;

import com.redhat.lightblue.query.ArrayRangeProjection;
import com.redhat.lightblue.query.Projection;

/**
 * Projector that returns a range of elements from an array
 */
public class ArrayRangeProjector extends ArrayProjector {

	private final int from;
	private final int to;

	/**
	 * Ctor
	 *
	 * @param p
	 *            The projection expression
	 * @param ctxPath
	 *            The absolute path relative to which this is to be interpreted
	 * @param context
	 *            The metadata node at which this is to be interpreted
	 */
	public ArrayRangeProjector(ArrayRangeProjection p, Path ctxPath, FieldTreeNode ctx) {
		super(p, ctxPath, ctx);
		from = p.getFrom();
		to = p.getTo();
	}

	@Override
	protected Projection.Inclusion projectArray(Path p, QueryEvaluationContext ctx) {
		// Is this array element in range?
		int index = p.getIndex(p.numSegments() - 1);
		if (to <= 0) {
			if (index >= from) {
				// This array element is selected.
				return isIncluded() ? Projection.Inclusion.explicit_inclusion : Projection.Inclusion.explicit_exclusion;
			} else {
				return Projection.Inclusion.explicit_exclusion;
			}
		} else {
			if (index >= from && index <= to) {
				// This array element is selected.
				return isIncluded() ? Projection.Inclusion.explicit_inclusion : Projection.Inclusion.explicit_exclusion;
			} else {
				return Projection.Inclusion.explicit_exclusion;
			}
		}
	}
}
