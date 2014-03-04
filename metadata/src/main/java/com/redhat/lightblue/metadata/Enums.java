/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.metadata;

import java.io.Serializable;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

import com.redhat.lightblue.util.Error;

/**
 *
 * @author nmalik
 */
public class Enums implements Serializable {

    private static final long serialVersionUID=1l;

    private final Map<String,Enum> enums = new HashMap<String,Enum>();

    /**
     * Sets enums
     */
    public void setEnums(Collection<Enum> l) {
        enums.clear();
        if (l != null) {
            for(Enum x:l)
                addEnum(x);
        }
    }

    public void addEnum(Enum x) {
        if(enums.containsKey(x.getName()))
            throw Error.get(MetadataConstants.ERR_DUPLICATE_ENUM,x.getName());
        enums.put(x.getName(),x);
    }

    /**
     * Returns all enums
     */
    @SuppressWarnings("unchecked")
    public Map<String,Enum> getEnums() {
        return (Map<String,Enum>) ((HashMap)enums).clone();
    }

    /**
     * Returns an enum with the given name, or null if it doesn't exist
     */
    public Enum getEnum(String name) {
        return enums.get(name);
    }
    
    /**
     * Returns if enums list is empty
     */
    public boolean isEmpty() {
        return enums.isEmpty();
    }
}
