package com.redhat.lightblue.assoc.ep;

import java.util.Map;
import java.util.List;

import java.util.stream.Stream;

import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

import com.redhat.lightblue.assoc.QueryPlanNode;
import com.redhat.lightblue.assoc.Binder;

import com.redhat.lightblue.metadata.DocId;

public class ResultDocument {
    private final JsonDoc doc;
    private final ExecutionBlock block;

    private DocId docId;
    
    // private final Map<QueryPlanNode,List<Slot>>  childDocumentSlots=new HashMap<>();
    // // parent node?

    // /**
    //  * This class represents a slot in the document into which arrays
    //  * of child documents should be inserted. Each slot contains the
    //  * name of the container field (which can be empty if the
    //  * association field is at the root level), and the name of the
    //  * reference field. If the reference field is inside an array, the
    //  * container field contains the array indexes.
    //  *
    //  * Each slot contains a list of binders for its field bindings.
    //  */
    // public static class Slot {
    //     private final Path containerField;
    //     private final String referenceFieldName;
    //     private final Map<FieldBinding,List<Binder>> bindings=new HashMap<>();

    //     public Slot(Path containerField,Path referenceField) {
    //         this.containerField=containerField;
    //         this.referenceFieldName=referenceField;
    //     }

    //     public Path getContainerField() {
    //         return containerField;
    //     }

    //     public String getReferenceFieldName() {
    //         return referenceFieldName;
    //     }

    //     public void fill(JsonDoc doc,List<FieldBinding> fieldBindings) {
    //         for(FieldBinding binding:fieldBindings) {
    //             QueryFieldInfo fieldInfo=binding.getFieldInfo();
    //             Path fieldName=fieldInfo.getEntityRelativeFieldNameWithContext().mutableCopy().rewriteIndexes(containerField);
    //             List<Binder> values=new ArrayList<>();
    //             KeyValueCursor<Path,JsonNode> cursor=doc.
    //                 getAllNodes(fieldName);
    //             while(cursor.hasNext()) {
    //                 cursor.next();
    //                 Object value;
    //                 JsonNode valueNode=cursor.getCurrentValue();
                    
    //                 if(binding instanceof ValueBinding) {
    //                     if(valueNode==null||valueNode.isNullNode())
    //                         value=null;
    //                     else
    //                         value=type.fromJson(valueNode);
    //                 } else {
    //                     if(valueNode==null||valueNode.isNullNode()) 
    //                         value=Collections.emptyList();
    //                     else {
    //                         List<Value> l=new ArrayList<Value>( ((ArrayNode)valueNode).size());
    //                         for(Iterator<JsonNode> itr=((ArrayNode)valueNode).elements();itr.hasNext();) {
    //                             l.add(new Value(type.fromJson(itr.next())));
    //                         }
    //                         value=l;
    //                     }
    //                 }
    //                 values.add(new Binder(binding,value));
    //             }
    //             bindings.put(binding,values);
    //         }
    //     }

    //     public Tuple<Binder> getBinders() {
    //         Tuple<Binder> t=new Tuple<>();
    //         t.addAll(bindings.values());
    //         return t;
    //     }
    // }

    public ResultDocument(ExecutionBlock block,JsonDoc doc) {
        this.doc=doc;
        this.block=block;
    }

    public ResultDocument(JsonDoc doc,ResultDocument t) {
        this.doc=doc;
        this.block=t.block;
    }

    public JsonDoc getDoc() {
        return doc;
    }


    // /**
    //  * Returns a list of slots in this document into where the
    //  * documents of the destination node should be inserted
    //  */
    // public List<Slot> getChildSlots(QueryPlanNode destination) {
    //     return childDocumentSlots.get(destination);
    // }

    /**
     * Returns the query plan node with which this document is associated
     */
    public QueryPlanNode getQueryPlanNode() {
        return block.getQueryPlanNode();
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

    // /**
    //  * Insert the child documents into this document
    //  *
    //  * @param parentDoc The parent document
    //  * @param childDocs A list of child docs
    //  * @param dest The destination field name to insert the result set
    //  */
    // public void insertChildDocs(Slot slot,
    //                             Stream<ResultDocument> childDocs) {
    //     JsonNode containerField;
    //     if(slot.getContainerField().isEmpty())
    //         containerField=doc.getRoot();
    //     else {
    //         containerField=doc.get(slot.getContainerField());
    //         if(containerField==null)
    //             doc.modify(slot.getContainerField(),JsonNodeFactory.instance.objectNode(),true);
    //     }
    //     ArrayNode arrayField=containerField.get(slot.getReferenceFieldName());
    //     if(arrayField==null) {
    //         containerField.set(slot.getReferenceFieldName(),arrayField=JsonNodeFactory.arrayNode());
    //     }
    //     childDocs.
    //         map(result->result.doc.getOutputDocument()).
    //         forEach(d->arrayField.add(d));
    // }
    
    
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

    /**
     * Modifies the document to include only root document, removes all associated documents
     *
     * @return this
     */
    public ResultDocument trim() {
    }
    
    @Override
    public String toString() {
        return doc.toString();
    }

}

