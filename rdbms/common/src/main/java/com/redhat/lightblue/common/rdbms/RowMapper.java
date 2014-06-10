package com.redhat.lightblue.common.rdbms;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by lcestari on 6/5/14.
 */
public interface RowMapper<T> {
    T map (ResultSet resultSet) throws SQLException;
}
