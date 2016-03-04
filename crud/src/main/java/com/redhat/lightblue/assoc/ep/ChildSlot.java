package com.redhat.lightblue.assoc.ep;

import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.ResolvedReferenceField;


import com.redhat.lightblue.util.Path;

/**
 * Represents a slot for the child documents to insert into the documents of this block.
 * If the reference field is in an array, there will be only one slot in the execution
 * block for that field. The ResultDocument object will contain one slot for each
 * instance of the reference.
 */
public class ChildSlot {
    private final Path localContainerName;
    private final String referenceFieldName;
    private final ResolvedReferenceField reference;
    
    public ChildSlot(CompositeMetadata root,ResolvedReferenceField reference) {
        Path erfn=root.getEntityRelativeFieldName(reference);
        if(erfn.numSegments()==1) {
            localContainerName=Path.EMPTY;
            referenceFieldName=erfn.head(0);
        } else {
            localContainerName=erfn.prefix(-1);
            referenceFieldName=erfn.tail(0);
        }
        this.reference=reference;
    }
    
    public ChildSlot(Path localContainerName,ResolvedReferenceField reference) {
        this.localContainerName=localContainerName;
        this.referenceFieldName=reference.getName();
        this.reference=reference;
    }
    
    public Path getLocalContainerName() {
        return localContainerName;
    }
    
    public String getReferenceFieldName() {
        return referenceFieldName;
    }
    
    public ResolvedReferenceField getReference() {
        return reference;
    }
    
    public boolean hasAnys() {
        return localContainerName.nAnys()>0;
    }

    @Override
    public String toString() {
        return localContainerName+"."+referenceFieldName;
    }
}

