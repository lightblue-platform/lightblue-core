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
package com.redhat.lightblue.crud;

/**
 * Base interface for field constraint checkers. There are two flavors of
 * constraint checkers: FieldConstraintDocChecker is called with the document to
 * allow for constraints validation even if the field does not exist in the
 * document. FieldConstraintValueChecker is called with the value of every
 * field, so these are not called for nonexistant fields.
 *
 * Rationale: Constraint validator iterates through the values that match a
 * certain path. If a value does not exist, the iterator doesn't go through it.
 * So, those need to be caught using the FieldConstraintDocChecker. For simple
 * fields, the logic to figure out that an expected field does not exist is
 * trivial, but things get hairy for objects that are array members, and there
 * are missing fields there.
 *
 * @see FieldConstraintDocChecker
 * @see FieldConstraintValueChecker
 */
public interface FieldConstraintChecker {

}
