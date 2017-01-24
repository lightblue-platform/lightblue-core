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
package com.redhat.lightblue.savedsearch;

import java.util.HashMap;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.util.JsonUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class FindRequestBuilderTest {

    @Test
    public void paramStrTest() throws Exception {
        HashMap<String,String> map=new HashMap<>();
        map.put("v1","value1");
        map.put("v2","value2");
        map.put("v3",null);

        String result=FindRequestBuilder.
            applyParameters("test ${v1}=${v2} and ${v3}, ${v4} ${v5",map);

        Assert.assertEquals("test value1=value2 and null, ${v4} ${v5",result);
    }

    @Test
    public void paramJsonTest() throws Exception {
        HashMap<String,String> map=new HashMap<>();
        map.put("v1","value1");
        map.put("v2","value2");
        map.put("v3",null);

        JsonNode sourceNode=JsonUtils.json("{'$and':[{'array':'repositories','elemMatch':{'field':'repositories.*._id','op':'>','rvalue':'${v1}'}},{'array':'repositories','elemMatch':{'field':'published','op':'=','rvalue':'${v2}'}}]}".replaceAll("'","\""));
        
        
        JsonNode result=FindRequestBuilder.
            applyParameters(sourceNode,map);
        
        Assert.assertEquals("{'$and':[{'array':'repositories','elemMatch':{'field':'repositories.*._id','op':'>','rvalue':'value1'}},{'array':'repositories','elemMatch':{'field':'published','op':'=','rvalue':'value2'}}]}".replaceAll("'","\""),
                            result.toString());
    }
}
