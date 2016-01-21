/**
 *
 */
package com.redhat.lightblue.query;

import com.redhat.lightblue.util.Path;

/**
 * @author bvulaj
 *
 */
public class IndexSortKey extends SortKey {

    private static final long serialVersionUID = 1L;

    private final boolean caseInsensitive;

    /**
     * @param field
     * @param desc
     */
    public IndexSortKey(Path field, boolean desc) {
        super(field, desc);
        caseInsensitive = false;
    }

    /**
     * @param field
     * @param desc
     * @param caseInsensitive
     */
    public IndexSortKey(Path field, boolean desc, boolean caseInsensitive) {
        super(field, desc);
        this.caseInsensitive = caseInsensitive;

    }

    public boolean isCaseInsensitive() {
        return this.caseInsensitive;
    }
}
