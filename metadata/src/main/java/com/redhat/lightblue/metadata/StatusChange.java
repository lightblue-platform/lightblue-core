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
package com.redhat.lightblue.metadata;

import java.io.Serializable;

import java.util.Date;

public class StatusChange implements Serializable {
    private static final long serialVersionUID = 1l;

    private Date date;
    private MetadataStatus status;
    private String comment;

    /**
     * Gets the value of date
     *
     * @return the value of date
     */
    public Date getDate() {
        return this.date;
    }

    /**
     * Sets the value of date
     *
     * @param argDate Value to assign to this.date
     */
    public void setDate(Date argDate) {
        this.date = argDate;
    }

    /**
     * Gets the value of status
     *
     * @return the value of status
     */
    public MetadataStatus getStatus() {
        return this.status;
    }

    /**
     * Sets the value of status
     *
     * @param argStatus Value to assign to this.status
     */
    public void setStatus(MetadataStatus argStatus) {
        this.status = argStatus;
    }

    /**
     * Gets the value of comment
     *
     * @return the value of comment
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * Sets the value of comment
     *
     * @param argComment Value to assign to this.comment
     */
    public void setComment(String argComment) {
        this.comment = argComment;
    }

}
