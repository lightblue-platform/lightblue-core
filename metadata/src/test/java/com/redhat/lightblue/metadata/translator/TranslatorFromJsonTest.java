package com.redhat.lightblue.metadata.translator;

import static com.redhat.lightblue.util.JsonUtils.json;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Field;
import com.redhat.lightblue.metadata.ObjectArrayElement;
import com.redhat.lightblue.metadata.ObjectField;
import com.redhat.lightblue.metadata.SimpleArrayElement;
import com.redhat.lightblue.metadata.SimpleField;
import com.redhat.lightblue.metadata.translator.TranslatorFromJson;
import com.redhat.lightblue.metadata.types.StringType;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

public class TranslatorFromJsonTest {

    @Test
    public void testTranslateSimpleField() throws Exception {
        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new SimpleField("uid", StringType.TYPE));


        FakeTranslatorFromJson<SingleFieldObject<String>> translator = new FakeTranslatorFromJson<SingleFieldObject<String>>(md){

            @SuppressWarnings("unchecked")
            @Override
            protected void translate(SimpleField field, JsonNode node, Object target) {
                if (target instanceof SingleFieldObject) {
                    ((SingleFieldObject<String>) target).value = fromJson(field.getType(), node).toString();
                }
                else {
                    throw new RuntimeException("Unexpected");
                }
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

            @SuppressWarnings("unchecked")
            @Override
            protected void translate(ArrayField field, List<Object> items, Object target) {
                if (target instanceof SingleFieldObject) {
                    List<String> translatedItems = new ArrayList<>();
                    items.forEach(item -> translatedItems.add(item.toString()));

                    ((SingleFieldObject<String[]>) target).value = translatedItems.toArray(new String[0]);
                }
                else {
                    throw new RuntimeException("Unexpected");
                }
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
            protected void translate(SimpleField field, JsonNode node, Object target) {
                if (target instanceof SingleFieldObject) {
                    @SuppressWarnings("unchecked")
                    SingleFieldObject<SingleFieldObject<String>> thing = (SingleFieldObject<SingleFieldObject<String>>) target;

                    if (thing.value == null) {
                        thing.value = new SingleFieldObject<>();
                    }

                    thing.value.value = fromJson(field.getType(), node).toString();
                }
                else {
                    throw new RuntimeException("Unexpected");
                }
            }

        };

        SingleFieldObject<SingleFieldObject<String>> response = new SingleFieldObject<>();
        translator.translate(new JsonDoc(json("{\"obj\":{\"uid\":\"fake value\"}}")), response);

        assertNotNull(response);
        assertNotNull(response.value);
        assertEquals("fake value", response.value.value);
    }

    @Test
    public void testTranslateObjectArrayField() throws Exception{
        ObjectArrayElement oae = new ObjectArrayElement();
        oae.getFields().addNew(new SimpleField("uid", StringType.TYPE));
        oae.getFields().addNew(new SimpleField("name", StringType.TYPE));
        EntityMetadata md = fakeEntityMetadata("fakeMetadata",
                new ArrayField("objArr", oae));

        FakeTranslatorFromJson<SingleFieldObject<List<Map<String, String>>>> translator = new FakeTranslatorFromJson<SingleFieldObject<List<Map<String, String>>>>(md) {

            @SuppressWarnings("unchecked")
            @Override
            protected void translate(SimpleField field, JsonNode node, Object target) {
                switch(field.getFullPath().toString()) {
                    case "objArr.*.uid":
                        ((Map<String, String>) target).put("uid", fromJson(field.getType(), node).toString());
                        break;
                    case "objArr.*.name":
                        ((Map<String, String>) target).put("name", fromJson(field.getType(), node).toString());
                        break;
                    default:
                        throw new RuntimeException("Unexpected");
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void translate(ArrayField field, List<Object> items, Object target) {
                if (field.getFullPath().toString().equals("objArr")) {
                    ((SingleFieldObject<List<?>>) target).value = items;
                }
                else {
                    throw new RuntimeException("Unexpected");
                }
            }

            @Override
            protected Object createInstanceFor(Path path) {
                switch (path.toString()) {
                    case "objArr.*":
                        return new HashMap<>();
                    default:
                        throw new RuntimeException("Unexpected");
                }

            }

        };

        SingleFieldObject<List<Map<String, String>>> response = new SingleFieldObject<>();
        translator.translate(new JsonDoc(json("{\"objArr\":[{\"uid\":\"fake value1\",\"name\":\"name1\"},{\"uid\":\"fake value2\",\"name\":\"name2\"}]}")), response);

        assertNotNull(response);
        assertNotNull(response.value);
        assertEquals(2, response.value.size());
        assertEquals("fake value1", response.value.get(0).get("uid"));
        assertEquals("name1", response.value.get(0).get("name"));
        assertEquals("fake value2", response.value.get(1).get("uid"));
        assertEquals("name2", response.value.get(1).get("name"));
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

        public SingleFieldObject() {}

    }

    private class FakeTranslatorFromJson<T> extends TranslatorFromJson<T> {

        public FakeTranslatorFromJson(EntityMetadata entityMetadata) {
            super(entityMetadata);
        }

        @Override
        protected void translate(SimpleField field, JsonNode node, Object target) {
            throw new RuntimeException("Method was not expected to be called");
        }

        @Override
        protected void translate(ArrayField field, List<Object> items, Object target) {
            throw new RuntimeException("Method was not expected to be called");
        }

        @Override
        protected Object createInstanceFor(Path path) {
            throw new RuntimeException("Method was not expected to be called");
        }

    }

}
