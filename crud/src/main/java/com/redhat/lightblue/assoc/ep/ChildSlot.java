package com.redhat.lightblue.assoc.ep;

import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.ResolvedReferenceField;

import com.redhat.lightblue.util.Path;

/**
 * Represents a slot for the child documents to insert into the documents of
 * this block. If the reference field is in an array, there will be only one
 * slot in the execution block for that field. The ResultDocument object will
 * contain one slot for each instance of the reference.
 */
public class ChildSlot {
    private final Path localContainerName;
    private final String referenceFieldName;
    private final Path slotFieldName;
    private final ResolvedReferenceField reference;

    public ChildSlot(CompositeMetadata root, ResolvedReferenceField reference) {
        Path erfn = root.getEntityRelativeFieldName(reference);
        if (erfn.numSegments() == 1) {
            localContainerName = Path.EMPTY;
            referenceFieldName = erfn.head(0);
            slotFieldName = new Path(referenceFieldName);
        } else {
            localContainerName = erfn.prefix(-1);
            referenceFieldName = erfn.tail(0);
            slotFieldName = erfn;
        }
        this.reference = reference;
    }

    public ChildSlot(Path localContainerName, ResolvedReferenceField reference) {
        this.localContainerName = localContainerName;
        this.referenceFieldName = reference.getName();
        this.reference = reference;
        this.slotFieldName = localContainerName.isEmpty() ? new Path(referenceFieldName)
                : new Path(localContainerName, new Path(referenceFieldName));
    }

    /**
     * Returns the local name of the container containing the destination field.
     * Empty if the destination field is at root level
     */
    public Path getLocalContainerName() {
        return localContainerName;
    }

    /**
     * Name of the destination field
     */
    public String getReferenceFieldName() {
        return referenceFieldName;
    }

    /**
     * Full local name of the destination field
     */
    public Path getSlotFieldName() {
        return slotFieldName;
    }

    public ResolvedReferenceField getReference() {
        return reference;
    }

    public boolean hasAnys() {
        return localContainerName.nAnys() > 0;
    }

    @Override
    public String toString() {
        return localContainerName + "." + referenceFieldName;
    }
}
