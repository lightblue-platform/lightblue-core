/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author nmalik
 */
public class Enums {
    private final ArrayList<Enum> enums = new ArrayList<>();

    public Enums() {
    }

    public void setEnums(Collection<Enum> enums) {
        this.enums.clear();
        if (enums != null) {
            this.enums.addAll(enums);
        }
    }

    public List<Enum> getEnums() {
        return (List<Enum>) enums.clone();
    }
    
    public boolean isEmpty() {
        return enums.isEmpty();
    }
}
