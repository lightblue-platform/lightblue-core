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
