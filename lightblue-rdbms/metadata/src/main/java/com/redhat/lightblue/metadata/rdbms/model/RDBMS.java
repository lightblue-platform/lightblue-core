/*
 Copyright 2013 Red Hat, Inc. and/or its affiliates.

 This file is part of lightblue.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redhat.lightblue.metadata.rdbms.model;

import com.redhat.lightblue.metadata.parser.MetadataParser;
import com.redhat.lightblue.metadata.rdbms.converter.RootConverter;
import com.redhat.lightblue.metadata.rdbms.enums.LightblueOperators;
import com.redhat.lightblue.metadata.rdbms.util.RDBMSMetadataConstants;

public class RDBMS implements RootConverter {

    private Operation delete;
    private Operation fetch;
    private Operation insert;
    private Operation save;
    private Operation update;

    @Override
    public <T> void convert(MetadataParser<T> p, T parent) {
        T rdbms = p.newNode();
        
        if(this.getDelete() == null && this.getFetch() == null && this.getInsert() == null && this.getSave() == null && this.getUpdate() == null ) {
            throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "No operation informed");
        }
        
        if(this.getDelete() != null){
            this.getDelete().convert(p,rdbms );
        }
        if(this.getFetch() != null){
            this.getFetch().convert(p,rdbms );
        }
        if(this.getInsert() != null){
            this.getInsert().convert(p,rdbms );
        }
        if(this.getSave() != null){
            this.getSave().convert(p,rdbms );
        }
        if(this.getUpdate() != null){
            this.getUpdate().convert(p, rdbms);  
        }
        
        p.putObject(parent, "rdbms", rdbms);
    }

    
    public void setDelete(Operation delete) {
        this.delete = delete;
    }

    public Operation getDelete() {
        return delete;
    }

    public void setFetch(Operation fetch) {
        this.fetch = fetch;
    }

    public Operation getFetch() {
        return fetch;
    }

    public void setInsert(Operation insert) {
        this.insert = insert;
    }

    public Operation getInsert() {
        return insert;
    }

    public void setSave(Operation save) {
        this.save = save;
    }

    public Operation getSave() {
        return save;
    }

    public void setUpdate(Operation update) {
        this.update = update;
    }

    public Operation getUpdate() {
        return update;
    }    
    
    public Operation getOperationByName(String operation){
        switch(operation){
            case LightblueOperators.DELETE:
                    return delete;
            case LightblueOperators.FETCH:
                    return fetch;
            case LightblueOperators.INSERT:
                    return insert;
            case LightblueOperators.SAVE:
                    return save;
            case LightblueOperators.UPDATE:
                    return update;
            default: 
                    throw new IllegalArgumentException("Not valid operation -> "+operation);
        }
        
    }
}
