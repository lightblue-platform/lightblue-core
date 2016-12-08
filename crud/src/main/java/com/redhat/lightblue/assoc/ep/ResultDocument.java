package com.redhat.lightblue.assoc.ep;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.KeyValueCursor;
import com.redhat.lightblue.util.Tuples;

import com.redhat.lightblue.query.Value;

import com.redhat.lightblue.assoc.Binder;
import com.redhat.lightblue.assoc.BindQuery;
import com.redhat.lightblue.assoc.BoundObject;

import com.redhat.lightblue.metadata.DocId;
import com.redhat.lightblue.metadata.ResolvedReferenceField;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.ArrayField;

/**
 * A document, and its slots. Slots are organized by their reference fields. If
 * a slot is in an array element, the constructor creates slots for every
 * element in that array. Thus, every slot is connected to a path containing no
 * '*'s
 */
public class ResultDocument {
    private JsonDoc doc;
    private final ExecutionBlock block;
    private DocId docId;
    private Map<ResolvedReferenceField, List<ChildSlot>> slots = new HashMap<>();

    public ResultDocument(ExecutionBlock block, JsonDoc doc) {
        this.doc = doc;
        this.block = block;
        initializeSlots();
    }

    /**
     * Copy constructor, copies everything from 'copy', but replaces the
     * document with 'newDoc'
     */
    public ResultDocument(JsonDoc newDoc, ResultDocument copy) {
        this.doc = newDoc;
        this.block = copy.block;
        this.docId = copy.docId;
        this.slots = copy.slots;
    }

    public JsonDoc getDoc() {
        return doc;
    }

    /**
     * Returns the execution block produced this document
     */
    public ExecutionBlock getBlock() {
        return block;
    }

    /**
     * Returns the document identifier.
     */
    public DocId getDocId() {
        if (docId == null) {
            docId = block.getIdExtractor().getDocId(doc);
        }
        return docId;
    }

    /**
     * Returns the slots for this document
     */
    public Map<ResolvedReferenceField, List<ChildSlot>> getSlots() {
        return slots;
    }

    /**
     * Initializes the document specific slots
     */
    private void initializeSlots() {
        // Need to create a new slot for each array element
        for (ChildSlot blockSlot : block.getChildSlots()) {
            List<ChildSlot> childSlots = slots.get(blockSlot.getReference());
            if (childSlots == null) {
                slots.put(blockSlot.getReference(), childSlots = new ArrayList<>());
            }
            if (blockSlot.hasAnys()) {
                // Slot under array. Add one slot for each array element
                KeyValueCursor<Path, JsonNode> cursor = doc.getAllNodes(blockSlot.getLocalContainerName());
                while (cursor.hasNext()) {
                    cursor.next();
                    ChildSlot newSlot = new ChildSlot(cursor.getCurrentKey(),
                            blockSlot.getReference());
                    childSlots.add(newSlot);
                }
            } else {
                childSlots.add(blockSlot);
            }
        }
    }

    /**
     * If this document is the parent document of a destination block documents,
     * then the child documents will be inserted into the slots of this
     * document. This function builds a BindQuery object for each slot. If the
     * child document attaches to an array element, then there will be multiple
     * slots in the returned map, one for each array element. If not, there will
     * be only one slot for each field. The child block will retrieve documents
     * based on queries written using the binder, and attach the child documents
     * to the corresponding slot
     */
    public Map<ChildSlot, BindQuery> getBindersForChild(AssociationQuery childAq) {
        Map<ChildSlot, BindQuery> ret = new HashMap<>();
        if (childAq.getQuery() != null) {
            List<ChildSlot> slots = this.slots.get(childAq.getReference());
            if (slots != null) {
                for (ChildSlot slot : slots) {
                    ret.put(slot, getBindersForSlot(slot, childAq));
                }
            }
        }
        return ret;
    }

    /**
     * Returns a query binder for a slot. The slot cannot have '*' in it (i.e.
     * there must be only one location for the slot in the document).
     */
    public BindQuery getBindersForSlot(ChildSlot slot, AssociationQuery childAq) {
        List<Binder> binders = new ArrayList<>();
        for (BoundObject bo : childAq.getFieldBindings()) {
            // Interpret field based on this slot
            Path field = bo.getFieldInfo().getEntityRelativeFieldNameWithContext();
            Path fieldAtSlot = field.mutableCopy().rewriteIndexes(slot.getLocalContainerName()).immutableCopy();
            if (fieldAtSlot.nAnys() > 0) {
                KeyValueCursor<Path, JsonNode> cursor = doc.getAllNodes(fieldAtSlot);
                List<Value> value=new ArrayList<Value>();
                while (cursor.hasNext()) {
                    cursor.next();
                    ((List)value).add(getValue(bo.getFieldInfo().getFieldMd(),cursor.getCurrentValue()));
                }
                binders.add(new Binder(bo,value));
            } else {
            	binders.add(new Binder(bo,getValue(bo.getFieldInfo().getFieldMd(),doc.get(fieldAtSlot))));
            }
        }
        return new BindQuery(binders);
    }

    /**
     * This document is a child document, and it will be used to locate the
     * parent. The destination block needs queries based on the values in this
     * document to retrieve those documents.
     *
     * The return value is a list of BindQuery objects, each should be used to
     * write queries to search for parent documents.
     */
    public List<BindQuery> getBindersForParent(AssociationQuery parentAq) {
        List<BindQuery> ret = new ArrayList<>();
        if (parentAq.getQuery() != null) {
            Tuples<Binder> binderTuple = new Tuples<>();

            for (BoundObject bo : parentAq.getFieldBindings()) {
                Path field = bo.getFieldInfo().getEntityRelativeFieldNameWithContext();
                if (field.nAnys() > 0) {
                    List<Binder> l = new ArrayList<>();
                    KeyValueCursor<Path, JsonNode> cursor = doc.getAllNodes(field);
                    while (cursor.hasNext()) {
                        cursor.next();
                        l.add(new Binder(bo, getValue(bo.getFieldInfo().getFieldMd(), cursor.getCurrentValue())));
                    }
                    binderTuple.add(l);
                } else {
                    List<Binder> l = new ArrayList<>(1);
                    l.add(new Binder(bo, getValue(bo.getFieldInfo().getFieldMd(), doc.get(field))));
                    binderTuple.add(l);
                }
            }
            for (Iterator<List<Binder>> itr = binderTuple.tuples(); itr.hasNext();) {
                ret.add(new BindQuery(itr.next()));
            }
        }
        return ret;
    }

    private Object getValue(FieldTreeNode fieldMd, JsonNode valueNode) {
        if (fieldMd instanceof ArrayField) {
            Type t = ((ArrayField) fieldMd).getElement().getType();
            if (valueNode instanceof ArrayNode) {
                List<Value> list = new ArrayList<>();
                for (Iterator<JsonNode> itr = ((ArrayNode) valueNode).elements(); itr.hasNext();) {
                    list.add(new Value(t.fromJson(itr.next())));
                }
                return list;
            } else {
                return new ArrayList<Value>();
            }
        } else {
            return new Value(fieldMd.getType().fromJson(valueNode));
        }
    }

    /**
     * Insert the child documents into a slot of this document
     *
     * @param parentDoc The parent document
     * @param childDocs A list of child docs
     * @param dest The destination field name to insert the result set
     */
    public void insertChildDocs(ChildSlot slot,
                                Stream<ResultDocument> childDocs) {
        ObjectNode containerField;
        if (slot.getLocalContainerName().isEmpty()) {
            containerField = (ObjectNode) doc.getRoot();
        } else {
            containerField = (ObjectNode) doc.get(slot.getLocalContainerName());
            if (containerField == null) {
                doc.modify(slot.getLocalContainerName(), containerField = JsonNodeFactory.instance.objectNode(), true);
            }
        }
        ArrayNode arrayField = (ArrayNode) containerField.get(slot.getReferenceFieldName());
        if (arrayField == null) {
            containerField.set(slot.getReferenceFieldName(), arrayField = JsonNodeFactory.instance.arrayNode());
        }
        for (Iterator<ResultDocument> docItr = childDocs.iterator(); docItr.hasNext();) {
            arrayField.add(docItr.next().doc.getRoot());
        }
    }

    @Override
    public String toString() {
        return doc.toString();
    }

}
