package com.redhat.lightblue.crud;

import static com.redhat.lightblue.util.JsonUtils.json;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Field;
import com.redhat.lightblue.metadata.ObjectField;
import com.redhat.lightblue.metadata.SimpleArrayElement;
import com.redhat.lightblue.metadata.SimpleField;
import com.redhat.lightblue.metadata.types.StringType;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonNodeCursor;

public class TranslatorFromJsonTest {

    @Test
    public void testTranslateSimpleField() throws Exception {
        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new SimpleField("uid", StringType.TYPE));


        FakeTranslatorFromJson<SingleFieldObject<String>> translator = new FakeTranslatorFromJson<SingleFieldObject<String>>(md){

            @Override
            protected void translate(SimpleField field, JsonNode node, SingleFieldObject<String> target) {
                target.value = fromJson(field.getType(), node).toString();
            }

        };

        SingleFieldObject<String> response = new SingleFieldObject<>();
        translator.translate(new JsonDoc(json("{\"uid\":\"fake value\"}")), response);

        assertNotNull(response);
        assertEquals("fake value", response.value);
    }

    @Test
    public void testTranslateSimpleArrayField() throws Exception {
        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new ArrayField("array", new SimpleArrayElement(StringType.TYPE)));

        FakeTranslatorFromJson<SingleFieldObject<String[]>> translator = new FakeTranslatorFromJson<SingleFieldObject<String[]>>(md) {

            @Override
            protected void translateSimpleArray(ArrayField field, List<Object> items, SingleFieldObject<String[]> target) {
                List<String> translatedItems = new ArrayList<>();
                items.forEach(item -> translatedItems.add(item.toString()));

                target.value = translatedItems.toArray(new String[0]);
            }

        };

        SingleFieldObject<String[]> response = new SingleFieldObject<>();
        translator.translate(new JsonDoc(json("{\"array\":[\"hello\",\"world\"]}")), response);

        assertNotNull(response);
        assertEquals(2, response.value.length);
        assertEquals("hello", response.value[0]);
        assertEquals("world", response.value[1]);
    }

    @Test
    public void testTranslateObjectField() throws Exception{
        ObjectField objField = new ObjectField("obj");
        objField.getFields().addNew(new SimpleField("uid", StringType.TYPE));
        EntityMetadata md = fakeEntityMetadata("fakeMetadata", objField);

        FakeTranslatorFromJson<SingleFieldObject<SingleFieldObject<String>>> translator = new FakeTranslatorFromJson<SingleFieldObject<SingleFieldObject<String>>>(md) {

            @Override
            protected void translate(SimpleField field, JsonNode node, SingleFieldObject<SingleFieldObject<String>> target) {
                if (target.value == null) {
                    target.value = new SingleFieldObject<>();
                }

                target.value.value = fromJson(field.getType(), node).toString();
            }

        };

        SingleFieldObject<SingleFieldObject<String>> response = new SingleFieldObject<>();
        translator.translate(new JsonDoc(json("{\"obj\":{\"uid\":\"fake value\"}}")), response);

        assertNotNull(response);
        assertNotNull(response.value);
        assertEquals("fake value", response.value.value);
    }

    protected EntityMetadata fakeEntityMetadata(String name, Field... fields) {
        EntityMetadata md = new EntityMetadata(name);
        for (Field f : fields) {
            md.getFields().addNew(f);
        }
        return md;
    }

    private class SingleFieldObject<T> {

        public T value;

    }

    private class FakeTranslatorFromJson<T> extends TranslatorFromJson<T> {

        public FakeTranslatorFromJson(EntityMetadata entityMetadata) {
            super(entityMetadata);
        }

        @Override
        protected void translate(SimpleField field, JsonNode node, T target) {
            throw new RuntimeException("Method was not expected to be called");
        }

        @Override
        protected void translateSimpleArray(ArrayField field, List<Object> items, T target) {
            throw new RuntimeException("Method was not expected to be called");
        }

        @Override
        protected void translateObjectArray(ArrayField field, JsonNodeCursor cursor, T target) {
            throw new RuntimeException("Method was not expected to be called");
        }

    }

}
