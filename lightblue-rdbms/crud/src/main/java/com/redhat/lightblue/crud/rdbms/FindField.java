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
package com.redhat.lightblue.crud.rdbms;

import com.redhat.lightblue.query.*;

/**
 * Created by lcestari on 8/8/14.
 */
public class FindField extends Translator {
    @Override
    protected void translateFromToDependencies(TranslationContext t) {

    }

    @Override
    protected void recursiveTranslateArrayElemMatch(TranslationContext c, ArrayMatchExpression arrayMatchExpression) {

    }

    @Override
    protected void recursiveTranslateFieldComparison(TranslationContext c, FieldComparisonExpression fieldComparisonExpression) {

    }

    @Override
    protected void recursiveTranslateNaryLogicalExpression(TranslationContext c, NaryLogicalExpression naryLogicalExpression) {

    }

    @Override
    protected void recursiveTranslateNaryRelationalExpression(TranslationContext c, NaryRelationalExpression naryRelationalExpression) {

    }

    @Override
    protected void recursiveTranslateRegexMatchExpression(TranslationContext c, RegexMatchExpression regexMatchExpression) {

    }

    @Override
    protected void recursiveTranslateUnaryLogicalExpression(TranslationContext c, UnaryLogicalExpression unaryLogicalExpression) {

    }

    @Override
    protected void recursiveTranslateValueComparisonExpression(TranslationContext c, ValueComparisonExpression valueComparisonExpression) {

    }

    @Override
    protected void recursiveTranslateArrayContainsAll(TranslationContext c) {
        String translatePath = translatePath(c.tmpArray);
    }

    @Override
    protected void recursiveTranslateArrayContainsAny(TranslationContext c) {

    }

    @Override
    protected void recursiveTranslateArrayContainsNone(TranslationContext c) {

    }
}
