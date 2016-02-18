package com.redhat.lightblue.assoc.ep;

public class ResultDocument {
    private final JsonDoc doc;
    private final QueryPlanNode qpNode;
    private final Map<QueryPlanNode,List<Slot>>  childDocumentSlots=new HashMap<>();
    // parent node?

    public static class Slot {
        private final Path containerField;
        private final Path referenceFieldName;
        private final Map<FieldBinding,List<Binder>> bindings=new HashMap<>();

        public Slot(Path containerField,Path referenceField) {
            this.containerField=containerField;
            this.referenceFieldName=referenceField;
        }

        public Path getContainerField() {
            return containerField;
        }

        public Path getReferenceFieldName() {
            return referenceFieldName;
        }

        public void fill(JsonDoc doc,List<FieldBinding> fieldBindings) {
            for(FieldBinding binding:fieldBindings) {
                QueryFieldInfo fieldInfo=binding.getFieldInfo();
                Path fieldName=fieldInfo.getEntityRelativeFieldNameWithContext().mutableCopy().rewriteIndexes(containerField);
                List<Binder> values=new ArrayList<>();
                KeyValueCursor<Path,JsonNode> cursor=doc.
                    getAllNodes(fieldName);
                while(cursor.hasNext()) {
                    cursor.next();
                    Object value;
                    JsonNode valueNode=cursor.getCurrentValue();
                    
                    if(binding instanceof ValueBinding) {
                        if(valueNode==null||valueNode.isNullNode())
                            value=null;
                        else
                            value=type.fromJson(valueNode);
                    } else {
                        if(valueNode==null||valueNode.isNullNode()) 
                            value=Collections.emptyList();
                        else {
                            List<Value> l=new ArrayList<Value>( ((ArrayNode)valueNode).size());
                            for(Iterator<JsonNode> itr=((ArrayNode)valueNode).elements();itr.hasNext();) {
                                l.add(new Value(type.fromJson(itr.next())));
                            }
                            value=l;
                        }
                    }
                    values.add(new Binder(binding,value));
                }
                bindings.put(binding,values);
            }
        }
    }

    public ResultDocument(QueryPlanNode qpNode,JsonDoc doc) {
        this.doc=doc;
        this.qpNode=qpNode;
    }

    public JsonDoc getDoc() {
        return doc;
    }

    /**
     * Returns the query plan node with which this document is associated
     */
    public QueryPlanNode getQueryPlanNode() {
        return qpNode;
    }

    /**
     * Adds a destination to this document for child documents
     */
    public void addChildDocumentDestination(QueryPlanNode childNode,AssociationQuery aq) {
        ResolvedReferenceField reference=aq.getReference();
        Path destinationArrayField=reference.getFullPath();
        Path destinationReferenceParent=destinationArrayField.prefix(-1);
        List<Slot> slots=new ArrayList<>();
        childDocumentSlots.put(childNode,slots);
        
        if(destinationReferenceParent.nAnys()>0) {
            KeyValueCursor<Path,JsonNode> cursor=doc.getAllNodes(destinationReferenceParent);
            while(cursor.hasNext()) {
                cursor.next();
                Path destinationReferenceParentInstance=cursor.getCurrentPath();
                // Create a slot with the parent container and the field name
                Slot s=new Slot(destinationReferenceParentInstance,reference.getReferenceField().getName());
                s.fill(doc,aq.getFieldBindings());
                slots.add(s);
            }
        } else {
            // The destination field is not in an array, there is only one slot
            Slot s=new Slot(destinationReferenceParent,reference.getReferenceField().getName());
            s.fill(doc,aq.getFieldBindings());
            slots.add(s);
        }
    }

    /**
     * Returns document bindings once they are initialized
     */
    public DocumentBindings getBindings() {
        return bindings;
    }

    /**
     * Computes the document bindings based on the association query
     */
    public void initializeBindings(AssociationQuery aq) {
        bindings=new DocumentBindings(doc,aq);
    }
}

