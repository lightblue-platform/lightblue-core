/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author nmalik
 */
public class Indexes implements Serializable {

    private final ArrayList<Index> indexes = new ArrayList<>();

    public Indexes() {
    }

    public void setIndexes(Collection<Index> indexes) {
        this.indexes.clear();
        if (indexes != null) {
            this.indexes.addAll(indexes);
        }
    }

    public List<Index> getIndexes() {
        return (List<Index>) indexes.clone();
    }

    public boolean isEmpty() {
        return indexes.isEmpty();
    }
}
