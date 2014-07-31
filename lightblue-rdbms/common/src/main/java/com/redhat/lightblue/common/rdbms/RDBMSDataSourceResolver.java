/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.redhat.lightblue.common.rdbms;

import javax.sql.DataSource;

/**
 *
 * @author lcestari
 */
public interface RDBMSDataSourceResolver {
    public DataSource get(RDBMSDataStore store) ;    
}
