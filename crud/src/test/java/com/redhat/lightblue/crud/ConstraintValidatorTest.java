package com.redhat.lightblue.crud;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.metadata.EntityConstraint;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Field;
import com.redhat.lightblue.metadata.FieldConstraint;
import com.redhat.lightblue.metadata.FieldCursor;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.util.DefaultRegistry;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.KeyValueCursor;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Registry;

@RunWith(MockitoJUnitRunner.class)
public class ConstraintValidatorTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Mock
    private EntityMetadata entityMetadata;

    @Mock
    private FieldConstraintDocChecker fieldConstraintDocChecker;
    private final static String FIELD_REGISTRY_KEY_DOC = "doc";

    @Mock
    private FieldConstraintValueChecker fieldConstraintValueChecker;
    private final static String FIELD_REGISTRY_KEY_VALUE = "value";

    @Mock
    private EntityConstraintChecker entityConstraintChecker;
    private final static String ENTITY_REGISTRY_KEY = "ENTITY KEY";

    private ConstraintValidator validator;

    protected static FieldCursor mockFieldCursor(FieldCursor mockedCursor, FieldCursorNode... values){
        if(values.length <= 0){
            when(mockedCursor.next()).thenReturn(false);
            return mockedCursor;
        }

        Boolean[] nextResponses = new Boolean[values.length];
        for(int x = 0; x < nextResponses.length - 1; x++){
            nextResponses[x] = true;
        }
        nextResponses[nextResponses.length - 1] = false;
        when(mockedCursor.next()).thenReturn(true, nextResponses);

        FieldTreeNode[] nodes = new FieldTreeNode[values.length - 1];
        for(int x = 0; x < nodes.length - 1; x++){
            nodes[x] = values[x + 1].getFieldTreeNode();
        }
        when(mockedCursor.getCurrentNode()).thenReturn(values[0].getFieldTreeNode(), nodes);

        Path[] paths = new Path[values.length - 1];
        for(int x = 0; x < paths.length - 1; x++){
            paths[x] = values[x + 1].getPath();
        }
        when(mockedCursor.getCurrentPath()).thenReturn(values[0].getPath(), paths);

        return mockedCursor;
    }

    protected static KeyValueCursor<Path, JsonNode> mockKeyValueCursor(KeyValueCursor<Path, JsonNode> mockedCursor, KeyValueCursorNode... values){
        if(values.length <= 0){
            when(mockedCursor.hasNext()).thenReturn(false);
            return mockedCursor;
        }

        Boolean[] nextResponses = new Boolean[values.length];
        for(int x = 0; x < nextResponses.length - 1; x++){
            nextResponses[x] = true;
        }
        nextResponses[nextResponses.length - 1] = false;
        when(mockedCursor.hasNext()).thenReturn(true, nextResponses);

        Path[] paths = new Path[values.length - 1];
        for(int x = 0; x < paths.length - 1; x++){
            paths[x] = values[x + 1].getPath();
        }
        when(mockedCursor.getCurrentKey()).thenReturn(values[0].getPath(), paths);

        JsonNode[] nodes = new JsonNode[values.length - 1];
        for(int x = 0; x < nodes.length - 1; x++){
            nodes[x] = values[x + 1].getJsonNode();
        }
        when(mockedCursor.getCurrentValue()).thenReturn(values[0].getJsonNode(), nodes);

        return mockedCursor;
    }

    protected static FieldTreeNode mockFieldTreeNode(Field mockedFieldTreeNode, List<FieldConstraint> values){
        when(mockedFieldTreeNode.getConstraints()).thenReturn(values);

        return mockedFieldTreeNode;
    }

    protected static Path mockPath_toString(Path mockedPath, String toString){
        when(mockedPath.toString()).thenReturn(toString);

        return mockedPath;
    }

    @Before
    public void setUp(){
        @SuppressWarnings("serial")
        Registry<String, FieldConstraintChecker> fieldCheckerRegistry = new DefaultRegistry<String, FieldConstraintChecker>(){
            {
                add(FIELD_REGISTRY_KEY_DOC, fieldConstraintDocChecker);
                add(FIELD_REGISTRY_KEY_VALUE, fieldConstraintValueChecker);
            }
        };

        @SuppressWarnings("serial")
        Registry<String, EntityConstraintChecker> entityCheckerRegistry =
        new DefaultRegistry<String, EntityConstraintChecker>(){
            {
                add(ENTITY_REGISTRY_KEY, entityConstraintChecker);
            }
        };

        validator = new ConstraintValidator(fieldCheckerRegistry, entityCheckerRegistry, entityMetadata);
    }

    /**
     * For the expected usage scenario, ensure that using a EntityConstraint does not throw any exceptions
     */
    @Test
    public void testValidateDoc_EntityConstraints(){
        EntityConstraint entityConstraint = mock(EntityConstraint.class);
        when(entityConstraint.getType()).thenReturn(ENTITY_REGISTRY_KEY);

        when(entityMetadata.getConstraints()).thenReturn(Arrays.asList(entityConstraint));

        FieldCursor cursor = mockFieldCursor(mock(FieldCursor.class));
        when(entityMetadata.getFieldCursor()).thenReturn(cursor);

        JsonDoc jsonDoc = mock(JsonDoc.class);

        validator.validateDoc(jsonDoc);

        //Not sure what to test here, aside from no exceptions were thrown.
    }

    /**
     * If no entry can be found in the registry for the given key,
     * then an exception is thrown.
     */
    @Test
    public void testValidateDoc_EntityConstraints_NotInRegistry(){
        expectedEx.expect(com.redhat.lightblue.util.Error.class);
        expectedEx.expectMessage("{\"objectType\":\"error\",\"context\":\"validateDoc/KEY DOES NOT EXIST\",\"errorCode\":\"crud:NoConstraint\"}");

        EntityConstraint entityConstraint = mock(EntityConstraint.class);
        when(entityConstraint.getType()).thenReturn("KEY DOES NOT EXIST");

        when(entityMetadata.getConstraints()).thenReturn(Arrays.asList(entityConstraint));

        FieldCursor cursor = mockFieldCursor(mock(FieldCursor.class));
        when(entityMetadata.getFieldCursor()).thenReturn(cursor);

        JsonDoc jsonDoc = mock(JsonDoc.class);

        validator.validateDoc(jsonDoc);
    }

    /**
     * For the expected usage scenario, ensure that using a FieldConstraintDocChecker does not throw any exceptions
     */
    @Test
    public void testValidateDoc_FieldRegistryDoc(){
        FieldConstraint constaintForDoc = mock(FieldConstraint.class);
        when(constaintForDoc.getType()).thenReturn(FIELD_REGISTRY_KEY_DOC);

        FieldCursor cursor = mockFieldCursor(
                mock(FieldCursor.class),
                new FieldCursorNode(
                        mockFieldTreeNode(mock(Field.class), Arrays.asList(constaintForDoc)),
                        mockPath_toString(mock(Path.class), "Fake Path"))
                );

        when(entityMetadata.getFieldCursor()).thenReturn(cursor);

        JsonDoc jsonDoc = mock(JsonDoc.class);

        validator.validateDoc(jsonDoc);

        //Not sure what to test here, aside from no exceptions were thrown.
    }

    /**
     * For the expected usage scenario, ensure that using a FieldConstraintValueChecker does not throw any exceptions
     */
    @Test
    public void testValidateDoc_FieldRegistryValue(){
        FieldConstraint constaintForDoc = mock(FieldConstraint.class);
        when(constaintForDoc.getType()).thenReturn(FIELD_REGISTRY_KEY_VALUE);

        Path path = mock(Path.class);
        FieldCursor cursor = mockFieldCursor(
                mock(FieldCursor.class),
                new FieldCursorNode(
                        mockFieldTreeNode(mock(Field.class), Arrays.asList(constaintForDoc)),
                        mockPath_toString(path, "Fake Path"))
                );

        when(entityMetadata.getFieldCursor()).thenReturn(cursor);

        @SuppressWarnings("unchecked")
        KeyValueCursor<Path, JsonNode> kvCursor = mockKeyValueCursor(
                mock(KeyValueCursor.class),
                new KeyValueCursorNode(mock(JsonNode.class), mock(Path.class))
                );

        JsonDoc jsonDoc = mock(JsonDoc.class);
        when(jsonDoc.getAllNodes(path)).thenReturn(kvCursor);

        validator.validateDoc(jsonDoc);

        //Not sure what to test here, aside from no exceptions were thrown.
    }

    /**
     * If no entry can be found in the registry for the given key,
     * then an exception is thrown.
     */
    @Test
    public void testValidateDoc_NotInFieldRegistry(){
        expectedEx.expect(com.redhat.lightblue.util.Error.class);
        expectedEx.expectMessage("{\"objectType\":\"error\",\"context\":\"validateDoc/Fake Path/KEY DOES NOT EXIST\",\"errorCode\":\"crud:NoConstraint\"}");


        FieldConstraint constaintForDoc = mock(FieldConstraint.class);
        when(constaintForDoc.getType()).thenReturn("KEY DOES NOT EXIST");

        Path path = mock(Path.class);
        FieldCursor cursor = mockFieldCursor(
                mock(FieldCursor.class),
                new FieldCursorNode(
                        mockFieldTreeNode(mock(Field.class), Arrays.asList(constaintForDoc)),
                        mockPath_toString(path, "Fake Path"))
                );

        when(entityMetadata.getFieldCursor()).thenReturn(cursor);

        JsonDoc jsonDoc = mock(JsonDoc.class);

        validator.validateDoc(jsonDoc);
    }

    /**
     * Ensures an unexpected exception is handled properly.
     */
    @Test
    @Ignore
    public void testValidateDoc_UnexpectedException(){
        expectedEx.expect(com.redhat.lightblue.util.Error.class);
        expectedEx.expectMessage("{\"objectType\":\"error\",\"context\":\"validateDoc\",\"errorCode\":\"" + CrudConstants.ERR_CRUD + "\",\"msg\":\"Fake Exception\"}");

        when(entityMetadata.getFieldCursor()).thenThrow(new RuntimeException("Fake Exception"));

        JsonDoc jsonDoc = mock(JsonDoc.class);

        validator.validateDoc(jsonDoc);
    }

    private static class FieldCursorNode{
        private final FieldTreeNode node;
        private final Path path;

        public FieldTreeNode getFieldTreeNode(){
            return node;
        }

        public Path getPath(){
            return path;
        }

        public FieldCursorNode(FieldTreeNode node, Path path){
            this.node = node;
            this.path = path;
        }
    }

    private static class KeyValueCursorNode{
        private final JsonNode node;
        private final Path path;

        public JsonNode getJsonNode(){
            return node;
        }

        public Path getPath(){
            return path;
        }

        public KeyValueCursorNode(JsonNode node, Path path){
            this.node = node;
            this.path = path;
        }
    }

}
