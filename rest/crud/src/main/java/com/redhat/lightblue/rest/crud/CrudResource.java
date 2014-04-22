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
package com.redhat.lightblue.rest.crud;

import com.redhat.lightblue.EntityVersion;
import com.redhat.lightblue.crud.FindRequest;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.rest.crud.hystrix.*;
import com.redhat.lightblue.util.JsonUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author nmalik
 * @author bserdar
 */
@Path("/") // metadata/ prefix is the application context
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CrudResource {
    @PUT
    @Path("/{entity}")
    public String insert(@PathParam("entity") String entity, String request) {
        return insert(entity, null, request);
    }

    @PUT
    @Path("/{entity}/{version}")
    public String insert(@PathParam("entity") String entity, @PathParam("version") String version, String request) {
        return new InsertCommand(null, entity, version, request).execute();
    }

    @POST
    @Path("/save/{entity}")
    public String save(@PathParam("entity") String entity, String request) {
        return save(entity, null, request);
    }

    @POST
    @Path("/save/{entity}/{version}")
    public String save(@PathParam("entity") String entity, @PathParam("version") String version, String request) {
        return new SaveCommand(null, entity, version, request).execute();
    }

    @POST
    @Path("/update/{entity}")
    public String update(@PathParam("entity") String entity, String request) {
        return update(entity, null, request);
    }

    @POST
    @Path("/update/{entity}/{version}")
    public String update(@PathParam("entity") String entity, @PathParam("version") String version, String request) {
        return new UpdateCommand(null, entity, version, request).execute();
    }

    @POST
    @Path("/delete/{entity}")
    public String delete(@PathParam("entity") String entity, String request) {
        return delete(entity, null, request);
    }

    @POST
    @Path("/delete/{entity}/{version}")
    public String delete(@PathParam("entity") String entity, @PathParam("version") String version, String req) {
        return new DeleteCommand(null, entity, version, req).execute();
    }

    @POST
    @Path("/find/{entity}")
    public String find(@PathParam("entity") String entity, String request) {
        return find(entity, null, request);
    }

    @POST
    @Path("/find/{entity}/{version}")
    public String find(@PathParam("entity") String entity, @PathParam("version") String version, String request) {
        return new FindCommand(null, entity, version, request).execute();
    }

    @GET
    @Path("/find/{entity}") //?Q&P&S&from&to
    public String simpleFind(@PathParam("entity") String entity                                      , @QueryParam("Q") String q, @QueryParam("P") String p, @QueryParam("S") String s, @DefaultValue("0") @QueryParam("from") long from, @DefaultValue("-1") @QueryParam("to") long to ) throws IOException {
        return simpleFind(entity, null, q, p, s, from, to);
    }

    @GET
    @Path("/find/{entity}/{version}") //?Q&P&S&from&to
    public String simpleFind(@PathParam("entity") String entity, @PathParam("version") String version, @QueryParam("Q") String q, @QueryParam("P") String p, @QueryParam("S") String s, @DefaultValue("0") @QueryParam("from") long from, @DefaultValue("-1") @QueryParam("to") long to ) throws IOException {
        // spec -> https://github.com/lightblue-platform/lightblue/wiki/Rest-Spec-Data#get-simple-find
        String sq = null;
        List<String> queryList = Arrays.asList(q.split(";"));
        if (queryList.size() > 1) {
            StringBuilder sbq = new StringBuilder("{ \"$and\" : [" );
            for (int i = 0; i < queryList.size(); i++) {
                sbq.append(buildQueryFieldTemplate(queryList.get(i)));
                if((i+1) < queryList.size()) {
                    sbq.append(',');
                }
            }
            sbq.append("]}");
            sq=sbq.toString();

        } else {
            sq = buildQueryFieldTemplate(queryList.get(0));
        }

        String sp = null;
        List<String> projectionList = Arrays.asList(p.split(","));
        if (projectionList.size() > 1) {
            StringBuilder sbp = new StringBuilder("[" );
            for (int i = 0; i < projectionList.size(); i++) {
                sbp.append(buildProjectionTemplate(projectionList.get(i)));
                if((i+1) < projectionList.size()) {
                    sbp.append(',');
                }
            }
            sbp.append("]");
            sp=sbp.toString();

        } else {
            sp = buildProjectionTemplate(projectionList.get(0));
        }

        String ss = null;
        List<String> sortList = Arrays.asList(s.split(","));
        if (sortList.size() > 1) {
            StringBuilder sbs = new StringBuilder("[" );
            for (int i = 0; i < sortList.size(); i++) {
                sbs.append(buildSortTemplate(sortList.get(i)));
                if((i+1) < sortList.size()) {
                    sbs.append(',');
                }
            }
            sbs.append("]");
            ss=sbs.toString();

        } else {
            ss = buildSortTemplate(sortList.get(0));
        }

        FindRequest findRequest = new FindRequest();
        findRequest.setEntityVersion(new EntityVersion(entity, version));
        findRequest.setQuery(QueryExpression.fromJson(JsonUtils.json(sq)));
        findRequest.setProjection(Projection.fromJson(JsonUtils.json(sp)));
        findRequest.setSort(s == null ? null : Sort.fromJson(JsonUtils.json(ss)));
        findRequest.setFrom(from);
        findRequest.setTo(to);
        String request = findRequest.toString();

        return new FindCommand(null, entity, version, request).execute();
    }

    private String buildQueryFieldTemplate(String s1) {
        String sq;
        String template = null;
        String templateString1 = "{\"field\": \"${field}\", \"op\": \"=\",\"rvalue\": \"${value}\"}";
        String templateString2 = "{\"field\":\"${field}\", \"op\":\"$in\", \"values\":[${value}]}";

        String[] split = s1.split(":");

        Map<String,String> map = new HashMap<>();
        map.put("field", split[0]);
        String value = null;

        String[] comma = split[1].split(",");
        if(comma.length > 1){
            template = templateString2;
            value = "\""+ StringUtils.join(comma, "\",\"")+"\"";
        }else{
            template = templateString1;
            value = split[1];
        }
        map.put("value", value );

        StrSubstitutor sub = new StrSubstitutor(map);

        sq=sub.replace(template);
        return sq;
    }

    private String buildProjectionTemplate(String s1) {
        String sp;
        String templateString = "{\"field\":\"${field}\",\"include\": ${include}, \"recursive\": ${recursive}}";

        String[] split = s1.split(":");

        Map<String,String> map = new HashMap<>();
        map.put("field", split[0]);
        map.put("include", split[1].charAt(0)=='1' ? "true" : "false");
        map.put("recursive", split[1].length()<2 ? "false" : (split[1].charAt(1)=='r' ? "true" : "false") );

        StrSubstitutor sub = new StrSubstitutor(map);

        sp=sub.replace(templateString);
        return sp;
    }

    private String buildSortTemplate(String s1) {
        String ss;
        String templateString = "{\"${field}\":\"${order}\"}";

        String[] split = s1.split(":");

        Map<String,String> map = new HashMap<>();
        map.put("field", split[0]);
        map.put("order", split[1].charAt(0)=='d' ? "$desc" : "$asc");

        StrSubstitutor sub = new StrSubstitutor(map);

        ss=sub.replace(templateString);
        return ss;
    }
}
