package com.redhat.lightblue.crud;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Field;
import com.redhat.lightblue.metadata.ReferenceField;
import com.redhat.lightblue.metadata.SimpleArrayElement;
import com.redhat.lightblue.metadata.SimpleField;
import com.redhat.lightblue.metadata.types.StringType;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

public class TranslatorToJsonTest {

    private final JsonNodeFactory factory = JsonNodeFactory.withExactBigDecimals(true);

    @Test
    public void testTranslateSimpleField() {
        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new SimpleField("uid", StringType.TYPE));

        FakeTranslatorToJson<SingleFieldObject<String>> translator = new FakeTranslatorToJson<SingleFieldObject<String>>(factory, md) {

            @Override
            protected Object getValueFor(SingleFieldObject<String> source, Path path) {
                return source.value;
            }

        };

        JsonDoc json = translator.translate(new SingleFieldObject<>("fake value"));
        assertNotNull(json);
    }

    @Test
    public void testTranslateSimpleArrayField() {
        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new ArrayField("array", new SimpleArrayElement(StringType.TYPE)));

        FakeTranslatorToJson<SingleFieldObject<String[]>> translator = new FakeTranslatorToJson<SingleFieldObject<String[]>>(factory, md) {

            @Override
            protected List<? extends Object> getSimpleArrayValues(Object o, SimpleArrayElement simpleArrayElement) {
                return Arrays.asList((String[]) o);
            }

            @Override
            protected Object getValueFor(SingleFieldObject<String[]> source, Path path) {
                return source.value;
            }

        };

        JsonDoc json = translator.translate(new SingleFieldObject<>(new String[]{"hello","world"}));
        assertNotNull(json);
    }

    protected EntityMetadata fakeEntityMetadata(String name, Field... fields) {
        EntityMetadata md = new EntityMetadata(name);
        for (Field f : fields) {
            md.getFields().addNew(f);
        }
        return md;
    }

    private class SingleFieldObject<T> {

        public final T value;

        public SingleFieldObject(T value) {
            this.value = value;
        }

    }

    private abstract class FakeTranslatorToJson<S> extends TranslatorToJson<S> {

        public FakeTranslatorToJson(JsonNodeFactory factory, EntityMetadata entityMetadata) {
            super(factory, entityMetadata);
        }

        @Override
        protected JsonNode translate(ReferenceField field, Object o) {
            throw new RuntimeException("Method was not expected to be called");
        }

        @Override
        protected List<? extends Object> getSimpleArrayValues(Object o, SimpleArrayElement simpleArrayElement) {
            throw new RuntimeException("Method was not expected to be called");
        }

    }

}
