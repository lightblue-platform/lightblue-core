package com.redhat.lightblue.crud;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Field;
import com.redhat.lightblue.metadata.ObjectArrayElement;
import com.redhat.lightblue.metadata.ObjectField;
import com.redhat.lightblue.metadata.SimpleArrayElement;
import com.redhat.lightblue.metadata.SimpleField;
import com.redhat.lightblue.metadata.types.StringType;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

public class TranslatorToJsonTest {

    private final JsonNodeFactory factory = JsonNodeFactory.withExactBigDecimals(true);

    @Test
    public void testTranslateSimpleField() throws Exception {
        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new SimpleField("uid", StringType.TYPE));

        FakeTranslatorToJson<SingleFieldObject<String>> translator = new FakeTranslatorToJson<SingleFieldObject<String>>(factory, md) {

            @Override
            protected Object getValueFor(Object source, Path path) {
                if (source instanceof SingleFieldObject) {
                    return ((SingleFieldObject<?>) source).value;
                }
                throw new RuntimeException("Unexpected");
            }

        };

        JsonDoc json = translator.translate(new SingleFieldObject<>("fake value"));
        assertNotNull(json);
        JSONAssert.assertEquals("{\"uid\":\"fake value\"}", json.toString(), true);
    }

    @Test
    public void testTranslateSimpleArrayField() throws Exception {
        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new ArrayField("array", new SimpleArrayElement(StringType.TYPE)));

        FakeTranslatorToJson<SingleFieldObject<String[]>> translator = new FakeTranslatorToJson<SingleFieldObject<String[]>>(factory, md) {

            @Override
            protected List<? extends Object> getSimpleArrayValues(Object o, SimpleArrayElement simpleArrayElement) {
                return Arrays.asList((String[]) o);
            }

            @Override
            protected Object getValueFor(Object source, Path path) {
                if (source instanceof SingleFieldObject) {
                    return ((SingleFieldObject<?>) source).value;
                }
                throw new RuntimeException("Unexpected");
            }

        };

        JsonDoc json = translator.translate(new SingleFieldObject<>(new String[]{"hello","world"}));
        assertNotNull(json);
        JSONAssert.assertEquals("{\"array\":[\"hello\",\"world\"]}", json.toString(), true);
    }

    @Test
    public void testTranslateObjectField() throws Exception{
        ObjectField objField = new ObjectField("obj");
        objField.getFields().addNew(new SimpleField("uid", StringType.TYPE));
        EntityMetadata md = fakeEntityMetadata("fakeMetadata", objField);

        FakeTranslatorToJson<SingleFieldObject<SingleFieldObject<String>>> translator = new FakeTranslatorToJson<SingleFieldObject<SingleFieldObject<String>>>(factory, md) {

            @Override
            protected Object getValueFor(Object source, Path path) {
                if (source instanceof SingleFieldObject<?>) {
                    SingleFieldObject<?> sfo = (SingleFieldObject<?>) source;
                    if ("obj.uid".equalsIgnoreCase(path.toString())) {
                        return ((SingleFieldObject<?>) sfo.value).value;
                    }
                }
                throw new RuntimeException("Unexpected");
            }

        };

        JsonDoc json = translator.translate(new SingleFieldObject<>(new SingleFieldObject<>("fake value")));
        assertNotNull(json);
        JSONAssert.assertEquals("{\"obj\":{\"uid\":\"fake value\"}}", json.toString(), true);
    }

    @Test
    public void testTranslateObjectArrayField() throws Exception{
        ObjectArrayElement oae = new ObjectArrayElement();
        oae.getFields().addNew(new SimpleField("uid", StringType.TYPE));
        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new ArrayField("objArr", oae));

        FakeTranslatorToJson<SingleFieldObject<SingleFieldObject<String>[]>> translator = new FakeTranslatorToJson<SingleFieldObject<SingleFieldObject<String>[]>>(factory, md) {

            @Override
            protected Object getValueFor(Object source, Path path) {
                if (source instanceof SingleFieldObject) {
                    return ((SingleFieldObject<?>) source).value;
                }

                throw new RuntimeException("Unexpected");
            }

        };

        @SuppressWarnings("unchecked")
        JsonDoc json = translator.translate(new SingleFieldObject<>(new SingleFieldObject[]{
            new SingleFieldObject<>("fake value1"), new SingleFieldObject<>("fake value2")}));
        assertNotNull(json);
        JSONAssert.assertEquals("{\"objArr\":[{\"uid\":\"fake value1\"},{\"uid\":\"fake value2\"}]}", json.toString(), true);
    }

    @Test
    public void testTranslateObjectArrayField_WithList() throws Exception {
        ObjectArrayElement oae = new ObjectArrayElement();
        oae.getFields().addNew(new SimpleField("uid", StringType.TYPE));
        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new ArrayField("objArr", oae));

        FakeTranslatorToJson<SingleFieldObject<List<SingleFieldObject<String>>>> translator = new FakeTranslatorToJson<SingleFieldObject<List<SingleFieldObject<String>>>>(factory, md) {

            @Override
            protected Object getValueFor(Object source, Path path) {
                if (source instanceof SingleFieldObject) {
                    return ((SingleFieldObject<?>) source).value;
                }

                throw new RuntimeException("Unexpected");
            }

        };

        JsonDoc json = translator.translate(
                new SingleFieldObject<>(Arrays.asList(
                        new SingleFieldObject<>("fake value1"),
                        new SingleFieldObject<>("fake value2"))));
        assertNotNull(json);
        JSONAssert.assertEquals(
                "{\"objArr\":[{\"uid\":\"fake value1\"},{\"uid\":\"fake value2\"}]}",
                json.toString(), true);
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
        protected List<? extends Object> getSimpleArrayValues(Object o, SimpleArrayElement simpleArrayElement) {
            throw new RuntimeException("Method was not expected to be called");
        }

    }

}
