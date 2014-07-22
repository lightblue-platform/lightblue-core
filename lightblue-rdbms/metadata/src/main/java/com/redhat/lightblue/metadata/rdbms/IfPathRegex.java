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

public class IfPathRegex extends If {
    private Path path;
    private String regex;
    private boolean caseInsensitive;
    private boolean multiline;
    private boolean extended;
    private boolean dotall;

    public void setPath(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getRegex() {
        return regex;
    }

    public void setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    public void setMultiline(boolean multiline) {
        this.multiline = multiline;
    }

    public boolean isMultiline() {
        return multiline;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
    }

    public boolean isExtended() {
        return extended;
    }

    public void setDotall(boolean dotall) {
        this.dotall = dotall;
    }

    public boolean isDotall() {
        return dotall;
    }

    @Override
    public <T> void convert(MetadataParser<T> p, Object lastArrayNode, T node) {
        if (path == null || path.isEmpty()) {
            throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "No path1 informed");
        }
        if (regex == null || regex.isEmpty()) {
            throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "No conditional informed");
        }
        T s = p.newNode();

        p.putString(s, "path", path.toString());
        p.putString(s, "regex", regex);
        if (!caseInsensitive) { // different than the default value
            p.putString(s, "case_insensitive", str(caseInsensitive));
        }
        if (!multiline) {
            p.putString(s, "multiline", str(multiline));
        }
        if (!multiline) {
            p.putString(s, "extended", str(extended));
        }
        if (!multiline) {
            p.putString(s, "dotall", str(dotall));
        }

        if (lastArrayNode == null) {
            p.putObject(node, "$path-regex", s);
        } else {
            T iT = p.newNode();
            p.putObject(iT, "$path-regex", s);
            p.addObjectToArray(lastArrayNode, iT);
        }
    }

    //to make more readable
    private String str(boolean b) {
        return Boolean.toString(b);
    }
}
