package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.parser.MetadataParser;

import java.util.List;

public enum IfThenElseConverter {
    INSTANCE;
/*    private static IfConverterImpl impl = new IfConverterImpl();

    static class  IfConverterImpl implements Converter {
        @Override
        public <T> void convert(MetadataParser<T> p, Object expressionsNode) {

        }
    }*/

    public static IfThenElseConverter getInstance(){
        return INSTANCE;
    }

    public <T> void convertIf(MetadataParser<T> p, Object expressionsNode, T eT, If anIf) {

    }

    public <T> void convertThen(MetadataParser<T> p, Object expressionsNode, T eT, Then then) {

    }

    public <T> void convertElseIfList(MetadataParser<T> p, Object expressionsNode, T eT, List<ElseIf> elseIfList) {

    }

    public <T> void convertElse(MetadataParser<T> p, Object expressionsNode, T eT, Else anElse) {

    }

}
