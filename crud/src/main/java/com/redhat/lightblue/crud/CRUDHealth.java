package com.redhat.lightblue.crud;

import java.util.Map;

/**
 * Contains health information for the CRUD layer (CRUD controllers)
 */
public class CRUDHealth {
    private final boolean isHealthy;
    private final Map<String, Object> details;

    public CRUDHealth(boolean isHealthy, Map<String, Object> details) {
        this.isHealthy = isHealthy;
        this.details = details;
    }

    public boolean isHealthy() {
        return isHealthy;
    }

    public Map<String, Object> details() {
        return details;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((details == null) ? 0 : details.hashCode());
        result = prime * result + (isHealthy ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CRUDHealth other = (CRUDHealth) obj;
        if (details == null) {
            if (other.details != null)
                return false;
        } else if (!details.equals(other.details))
            return false;
        if (isHealthy != other.isHealthy)
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CRUDHealth [isHealthy=");
        builder.append(isHealthy);
        builder.append(", details=");
        builder.append(details);
        builder.append("]");
        return builder.toString();
    }
}
