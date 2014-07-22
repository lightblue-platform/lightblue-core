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

import com.redhat.lightblue.metadata.parser.MetadataParser;
import com.redhat.lightblue.util.Path;
import java.util.List;

public class IfPathEmpty extends  If {
    private Path path1;
    public void setPath1(Path path1) {
        this.path1 = path1;
    }

    public Path getPath1() {
        return path1;
    }   

    @Override
    public <T> void convert(MetadataParser<T> p, Object lastArrayNode, T node) {
        if(path1 == null || path1.isEmpty()){
            throw com.redhat.lightblue.util.Error.get(RDBMSConstants.ERR_FIELD_REQ, "No path1 informed");
        }
        T s = p.newNode();

        p.putString(s,"path1",path1.toString());

        if(lastArrayNode == null){
            p.putObject(node,"$path-empty",s);
        } else {
            T iT = p.newNode();
            p.putObject(iT,"$path-empty",s);
            p.addObjectToArray(lastArrayNode, iT);
        }
    }
}
