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

import com.redhat.lightblue.query.Value;

import com.redhat.lightblue.assoc.QueryPlanNode;
import com.redhat.lightblue.assoc.Binder;
import com.redhat.lightblue.assoc.BoundObject;

import com.redhat.lightblue.metadata.DocId;
import com.redhat.lightblue.metadata.ResolvedReferenceField;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.ArrayField;

public class ResultDocument {
    private JsonDoc doc;
    private final ExecutionBlock block;
    private DocId docId;
    private Map<ResolvedReferenceField,List<ChildSlot>> slots=new HashMap<>();

    public ResultDocument(ExecutionBlock block,JsonDoc doc) {
        this.doc=doc;
        this.block=block;
        initializeSlots();
    }

    /**
     * Copy constructor, copies everything from 'copy', but replaces the document with 'newDoc'
     */
    public ResultDocument(JsonDoc newDoc,ResultDocument copy) {
        this.doc=newDoc;
        this.block=copy.block;
        this.docId=copy.docId;
        this.slots=copy.slots;
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
        if(docId==null)
            docId=block.getIdExtractor().getDocId(doc);
        return docId;
    }

    /**
     * Returns the slots for this document
     */
    public Map<ResolvedReferenceField,List<ChildSlot>> getSlots() {
        return slots;
    }
    
    /**
     * Initializes the document specific slots
     */
    private void initializeSlots() {
        // Need to create a new slot for each array element
        for(ChildSlot blockSlot:block.getChildSlots()) {
            List<ChildSlot> childSlots=slots.get(blockSlot.getReference());
            if(childSlots==null) {
                slots.put(blockSlot.getReference(),childSlots=new ArrayList<>());
            }
            if(blockSlot.hasAnys()) {
                // Slot under array. Add one slot for each array element
                KeyValueCursor<Path,JsonNode> cursor=doc.getAllNodes(blockSlot.getLocalContainerName());
                while(cursor.hasNext()) {
                    cursor.next();
                    ChildSlot newSlot=new ChildSlot(cursor.getCurrentKey(),
                                                    blockSlot.getReference());
                    childSlots.add(newSlot);
                }
            } else {
                childSlots.add(blockSlot);
            }
        }            
    }

    /**
     * If this document is the parent document of a destination block
     * documents, then the child documents will be inserted into this
     * document. This function builds a list of binders for each
     * slot. If the child document attaches to an array element, then
     * there will be multiple slots in the returned map, one for each
     * array element. If not, there will be only one item. The child
     * block will retrieve documents based on this query, and attach
     * the child documents to the corresponding slot
     */
    public Map<ChildSlot,List<Binder>> getBindersForChild(AssociationQuery childAq) {
        Map<ChildSlot,List<Binder>> ret=new HashMap<>();
        if(childAq.getQuery()!=null) {
            List<ChildSlot> slots=this.slots.get(childAq.getReference());
            if(slots!=null) {
                for(ChildSlot slot:slots) {
                    ret.put(slot,getBindersForSlot(slot,childAq));
                }
            }
        }
        return ret;
    }

    /**
     * Returns binders for a slot. If the child document attaches to an array element, then
     * there will be multiple slots in the returned map, one for each
     * array element. If not, there will be only one item. The child
     * block will retrieve documents based on this query, and attach
     * the child documents to the corresponding slot
     */
    public List<Binder> getBindersForSlot(ChildSlot slot,AssociationQuery childAq) {
        List<Binder> binders=new ArrayList<>();
        for(BoundObject bo:childAq.getFieldBindings()) {
            // Interpret field based on this slot
            Path field=bo.getFieldInfo().getEntityRelativeFieldNameWithContext();
            Path fieldAtSlot=field.mutableCopy().rewriteIndexes(slot.getLocalContainerName()).immutableCopy();
            if(fieldAtSlot.nAnys()>0) {
                KeyValueCursor<Path,JsonNode> cursor=doc.getAllNodes(fieldAtSlot);
                while(cursor.hasNext()) {
                    cursor.next();
                    binders.add(new Binder(bo,getValue(bo.getFieldInfo().getFieldMd(),cursor.getCurrentValue())));
                }
            } else {
                binders.add(new Binder(bo,getValue(bo.getFieldInfo().getFieldMd(),doc.get(fieldAtSlot))));
            }
        }
        return binders;
    }

    /**
     * If this document is a child document of a destination block
     * document, then this document will be inserted into the slots of
     * those destination documents, but the destination documents are
     * not found yet. The destination block needs queries based on the
     * values in this document to retrieve those documents.
     *
     * The return value is a list of [Binder]s. Each element of the
     * return value is a list, containing only one item if the bound
     * field refers to a field that's not in an array, or multiple
     * items if the bound field is in an array.
     */
    public List<List<Binder>> getBindersForParent(AssociationQuery parentAq) {
        List<List<Binder>> ret=new ArrayList<>();
        if(parentAq.getQuery()!=null) {
            List<Binder> binders=new ArrayList<>();
            for(BoundObject bo:parentAq.getFieldBindings()) {
                Path field=bo.getFieldInfo().getEntityRelativeFieldNameWithContext();
                if(field.nAnys()>0) {
                    KeyValueCursor<Path,JsonNode> cursor=doc.getAllNodes(field);
                    while(cursor.hasNext()) {   
                        cursor.next();
                        binders.add(new Binder(bo,getValue(bo.getFieldInfo().getFieldMd(),cursor.getCurrentValue())));
                    }
                } else {
                    binders.add(new Binder(bo,getValue(bo.getFieldInfo().getFieldMd(),doc.get(field))));
                }
            }
            ret.add(binders);
        }
        return ret;
    }

    private Object getValue(FieldTreeNode fieldMd,JsonNode valueNode) {
        if(fieldMd instanceof ArrayField) {
            Type t=((ArrayField)fieldMd).getElement().getType();
            if(valueNode instanceof ArrayNode) {
                List<Value> list=new ArrayList<>();
                for(Iterator<JsonNode> itr=((ArrayNode)valueNode).elements();itr.hasNext();) {
                    list.add(new Value(t.fromJson(itr.next())));
                }
                return list;
            } else
                return new ArrayList<Value>();
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
        if(slot.getLocalContainerName().isEmpty())
            containerField=(ObjectNode)doc.getRoot();
        else {
            containerField=(ObjectNode)doc.get(slot.getLocalContainerName());
            if(containerField==null)
                doc.modify(slot.getLocalContainerName(),containerField=JsonNodeFactory.instance.objectNode(),true);
        }
        ArrayNode arrayField=(ArrayNode)containerField.get(slot.getReferenceFieldName());
        if(arrayField==null) {
            containerField.set(slot.getReferenceFieldName(),arrayField=JsonNodeFactory.instance.arrayNode());
        }
        for(Iterator<ResultDocument> docItr=childDocs.iterator();docItr.hasNext();)
            arrayField.add(docItr.next().doc.getRoot());
    }
    
    
    // /**
    //  * Associate child documents with their parents. The association
    //  * query is for the association from the child to the parent, so
    //  * caller must flip it before sending it in if necessary. The
    //  * caller also make sure parentDocs is a unique stream.
    //  */
    // public static void  associateDocs(Stream<ResultDocument> parentDocs,
    //                                   QueryPlanNode parentNode,
    //                                   List<ResultDocument> childDocs,
    //                                   QueryPlanNode childNode,
    //                                   AssociationQuery aq) {
    //     if(!childDocs.isEmpty()) {
    //         // This is an expensive operation, might benefit from a parallel stream
    //         parentDocs.parellel().forEach(parentDoc -> {
    //                 List<Slot> parentSlots=parentDoc.getChildSlots(childDocs);
    //                 QueryExpression query=aq==null?null:aq.getQuery();
    //                 if(parentSlots!=null) {
    //                     for(Slot slot:parentSlots) {
    //                         if(query!=null) {
    //                             Tuple<Binder> binders=slot.getBinders();
    //                             for(Iterator<List<Binder>> bindings:binders.iterator()) {
    //                                 // Bind query
    //                                 BindQuery bq=new BindQuery(bindings);
    //                                 QueryExpression boundQuery=bq.iterate(query);
    //                                 LOGGER.debug("Bound query:{}",boundQuery);
    //                                 QueryEvaluator qeval=QueryEvaluator.getInstance(boundQuery,parentNode.getMetadata());
    //                                 parentDoc.insertChildDocs(slot,childDocs.stream().filter(doc->qeval.evaluate(doc).getResult()));
    //                             }
    //                         } else {
    //                             parentDoc.insertChildDocs(slot,childDocs.stream());
    //                         }
    //                     }
    //                 }
    //             });
    //     }
    // }

    // /**
    //  * Adds a destination to this document for child documents
    //  */
    // public void addChildDocumentDestination(QueryPlanNode childNode,AssociationQuery aq) {
    //     ResolvedReferenceField reference=aq.getReference();
    //     Path destinationArrayField=reference.getFullPath();
    //     Path destinationReferenceParent=destinationArrayField.prefix(-1);
    //     List<Slot> slots=new ArrayList<>();
    //     childDocumentSlots.put(childNode,slots);
        
    //     if(destinationReferenceParent.nAnys()>0) {
    //         KeyValueCursor<Path,JsonNode> cursor=doc.getAllNodes(destinationReferenceParent);
    //         while(cursor.hasNext()) {
    //             cursor.next();
    //             Path destinationReferenceParentInstance=cursor.getCurrentPath();
    //             // Create a slot with the parent container and the field name
    //             Slot s=new Slot(destinationReferenceParentInstance,reference.getReferenceField().getName());
    //             s.fill(doc,aq.getFieldBindings());
    //             slots.add(s);
    //         }
    //     } else {
    //         // The destination field is not in an array, there is only one slot
    //         Slot s=new Slot(destinationReferenceParent,reference.getReferenceField().getName());
    //         s.fill(doc,aq.getFieldBindings());
    //         slots.add(s);
    //     }
    // }

    
    @Override
    public String toString() {
        return doc.toString();
    }

}

