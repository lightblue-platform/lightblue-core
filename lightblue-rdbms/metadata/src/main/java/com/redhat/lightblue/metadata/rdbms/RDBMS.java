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
package com.redhat.lightblue.metadata.rdbms;

public class RDBMS {

    private Operation delete;
    private Operation fetch;
    private Operation insert;
    private Operation save;
    private Operation update;

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
}
