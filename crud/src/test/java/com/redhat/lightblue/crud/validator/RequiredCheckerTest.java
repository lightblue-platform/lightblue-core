package com.redhat.lightblue.crud.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.crud.ConstraintValidator;
import com.redhat.lightblue.metadata.constraints.RequiredConstraint;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.KeyValueCursor;
import com.redhat.lightblue.util.Path;

public class RequiredCheckerTest {

    /**
     * If {@link RequiredConstraint#getValue()} method returns false, then
     * do not perform this check
     */
    @Test
    public void testCheckConstraint_Deactivated(){
        ConstraintValidator validator = mock(ConstraintValidator.class);

        RequiredConstraint constraint = mock(RequiredConstraint.class);
        when(constraint.getValue()).thenReturn(false);

        new RequiredChecker().checkConstraint(validator, null, null, constraint, null);

        verify(validator, never()).addDocError(any(Error.class));
    }

    @Test
    public void testCheckConstraint_NoErrors(){
        ConstraintValidator validator = mock(ConstraintValidator.class);

        Path path = mock(Path.class);
        when(path.nAnys()).thenReturn(0);

        RequiredConstraint constraint = mock(RequiredConstraint.class);
        when(constraint.getValue()).thenReturn(true);

        JsonDoc doc = mock(JsonDoc.class);
        when(doc.get(path)).thenReturn(mock(JsonNode.class));

        new RequiredChecker().checkConstraint(validator, null, path, constraint, doc);

        verify(validator, never()).addDocError(any(Error.class));
    }

    @Test
    public void testCheckConstraint_WithErrors(){
        ConstraintValidator validator = mock(ConstraintValidator.class);

        Path path = mock(Path.class);
        when(path.nAnys()).thenReturn(0);

        RequiredConstraint constraint = mock(RequiredConstraint.class);
        when(constraint.getValue()).thenReturn(true);

        JsonDoc doc = mock(JsonDoc.class);
        when(doc.get(path)).thenReturn(null);

        new RequiredChecker().checkConstraint(validator, null, path, constraint, doc);

        verify(validator, times(1)).addDocError(any(Error.class));
    }

    /**
     * When all the paths exist, then no errors will be thrown.
     */
    @Test
    public void testGetMissingFields_WithoutAnys_PathExists(){
        Path path = mock(Path.class);
        when(path.nAnys()).thenReturn(0);

        JsonDoc doc = mock(JsonDoc.class);
        when(doc.get(path)).thenReturn(mock(JsonNode.class));

        List<Path> results = RequiredChecker.getMissingFields(path, doc);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    /**
     * When an field is missing, then an error should be returned.
     */
    @Test
    public void testGetMissingFields_WithoutAnys_PathNotExist(){
        Path path = mock(Path.class);
        when(path.nAnys()).thenReturn(0);

        JsonDoc doc = mock(JsonDoc.class);
        when(doc.get(path)).thenReturn(null);

        List<Path> results = RequiredChecker.getMissingFields(path, doc);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(path, results.get(0));
    }

    /**
     * When an field is missing, then an error should be returned.
     */
    @Test
    public void testGetMissingFields_WithAnys_PathNotExist(){
        String fieldName = "FAKE_FIELD_NAME";
        String parentPath = "FAKE_PATH";

        Path path = mock(Path.class);
        when(path.nAnys()).thenReturn(1);
        when(path.prefix(-1)).thenReturn(mock(Path.class));
        when(path.tail(0)).thenReturn(fieldName);

        Path kvPath = mock(Path.class);
        when(kvPath.toString()).thenReturn(parentPath);

        @SuppressWarnings("unchecked")
        KeyValueCursor<Path, JsonNode> kvCursor = mock(KeyValueCursor.class);
        when(kvCursor.hasNext()).thenReturn(true, false);
        when(kvCursor.getCurrentValue()).thenReturn(mock(JsonNode.class));
        when(kvCursor.getCurrentKey()).thenReturn(kvPath);

        JsonDoc doc = mock(JsonDoc.class);
        when(doc.get(path)).thenReturn(mock(JsonNode.class));
        when(doc.getAllNodes(any(Path.class))).thenReturn(kvCursor);

        List<Path> results = RequiredChecker.getMissingFields(path, doc);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertNotNull(results.get(0));
        assertEquals(fieldName, results.get(0).tail(0));
        assertEquals(parentPath, results.get(0).tail(1));
    }

    /**
     * When all the paths exist, then no errors will be thrown.
     */
    @Test
    public void testGetMissingFields_WithAnys_PathExists(){
        String fieldName = "FAKE_FIELD_NAME";

        Path path = mock(Path.class);
        when(path.nAnys()).thenReturn(1);
        when(path.prefix(-1)).thenReturn(mock(Path.class));
        when(path.tail(0)).thenReturn(fieldName);

        JsonNode parentNode = mock(JsonNode.class);
        when(parentNode.get(fieldName)).thenReturn(mock(JsonNode.class));

        @SuppressWarnings("unchecked")
        KeyValueCursor<Path, JsonNode> kvCursor = mock(KeyValueCursor.class);
        when(kvCursor.hasNext()).thenReturn(true, false);
        when(kvCursor.getCurrentValue()).thenReturn(parentNode);

        JsonDoc doc = mock(JsonDoc.class);
        when(doc.get(path)).thenReturn(mock(JsonNode.class));
        when(doc.getAllNodes(any(Path.class))).thenReturn(kvCursor);

        List<Path> results = RequiredChecker.getMissingFields(path, doc);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testGetMissingFields_WithAnys_EmptyCursor(){
        Path path = mock(Path.class);
        when(path.nAnys()).thenReturn(1);
        when(path.prefix(-1)).thenReturn(mock(Path.class));
        when(path.tail(0)).thenReturn("FAKE_FIELD_NAME");

        @SuppressWarnings("unchecked")
        KeyValueCursor<Path, JsonNode> kvCursor = mock(KeyValueCursor.class);
        when(kvCursor.hasNext()).thenReturn(false);

        JsonDoc doc = mock(JsonDoc.class);
        when(doc.get(path)).thenReturn(mock(JsonNode.class));
        when(doc.getAllNodes(any(Path.class))).thenReturn(kvCursor);

        List<Path> results = RequiredChecker.getMissingFields(path, doc);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

}
