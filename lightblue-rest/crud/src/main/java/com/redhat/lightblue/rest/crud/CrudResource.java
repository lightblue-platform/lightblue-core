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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.EntityVersion;
import com.redhat.lightblue.crud.FindRequest;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.rest.crud.hystrix.*;
import com.redhat.lightblue.util.JsonUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import com.redhat.lightblue.util.Error;

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
//metadata/ prefix is the application context
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CrudResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrudResource.class);

    private static final String FIELD_Q_EQ_TMPL = "{\"field\": \"${field}\", \"op\": \"=\",\"rvalue\": \"${value}\"}";
    private static final String FIELD_Q_IN_TMPL = "{\"field\":\"${field}\", \"op\":\"$in\", \"values\":[${value}]}";
    private static final String PROJECTION_TMPL = "{\"field\":\"${field}\",\"include\": ${include}, \"recursive\": ${recursive}}";
    private static final String SORT_TMPL = "{\"${field}\":\"${order}\"}";
    private static final String DEFAULT_PROJECTION_TMPL = "{\"field\":\"*\",\"recursive\":true}";

    private static final String PARAM_ENTITY = "entity";
    private static final String PARAM_VERSION = "version";

    @PUT
    @Path("/{entity}")
    public String insert(@PathParam(PARAM_ENTITY) String entity,
                         String request) {
        return insert(entity, null, request);
    }

    @PUT
    @Path("/{entity}/{version}")
    public String insert(@PathParam(PARAM_ENTITY) String entity,
                         @PathParam(PARAM_VERSION) String version,
                         String request) {
        Error.reset();
        return new InsertCommand(null, entity, version, request).execute();
    }

    @POST
    @Path("/save/{entity}")
    public String save(@PathParam(PARAM_ENTITY) String entity,
                       String request) {
        return save(entity, null, request);
    }

    @POST
    @Path("/save/{entity}/{version}")
    public String save(@PathParam(PARAM_ENTITY) String entity,
                       @PathParam(PARAM_VERSION) String version,
                       String request) {
        Error.reset();
        return new SaveCommand(null, entity, version, request).execute();
    }

    @POST
    @Path("/update/{entity}")
    public String update(@PathParam(PARAM_ENTITY) String entity,
                         String request) {
        return update(entity, null, request);
    }

    @POST
    @Path("/update/{entity}/{version}")
    public String update(@PathParam(PARAM_ENTITY) String entity,
                         @PathParam(PARAM_VERSION) String version,
                         String request) {
        Error.reset();
        return new UpdateCommand(null, entity, version, request).execute();
    }

    @POST
    @Path("/delete/{entity}")
    public String delete(@PathParam(PARAM_ENTITY) String entity,
                         String request) {
        return delete(entity, null, request);
    }

    @POST
    @Path("/delete/{entity}/{version}")
    public String delete(@PathParam(PARAM_ENTITY) String entity,
                         @PathParam(PARAM_VERSION) String version,
                         String req) {
        Error.reset();
        return new DeleteCommand(null, entity, version, req).execute();
    }

    @POST
    @Path("/find/{entity}")
    public String find(@PathParam(PARAM_ENTITY) String entity,
                       String request) {
        return find(entity, null, request);
    }

    @POST
    @Path("/find/{entity}/{version}")
    public String find(@PathParam(PARAM_ENTITY) String entity,
                       @PathParam(PARAM_VERSION) String version,
                       String request) {
        Error.reset();
        return new FindCommand(null, entity, version, request).execute();
    }

    @GET
    @Path("/find/{entity}")
    //?Q&P&S&from&to
    public String simpleFind(@PathParam(PARAM_ENTITY) String entity,
                             @QueryParam("Q") String q,
                             @QueryParam("P") String p,
                             @QueryParam("S") String s,
                             @DefaultValue("0") @QueryParam("from") long from,
                             @DefaultValue("-1") @QueryParam("to") long to) throws IOException {
        return simpleFind(entity, null, q, p, s, from, to);
    }

    @GET
    @Path("/find/{entity}/{version}")
    //?Q&P&S&from&to
    public String simpleFind(@PathParam(PARAM_ENTITY) String entity,
                             @PathParam(PARAM_VERSION) String version,
                             @QueryParam("Q") String q,
                             @QueryParam("P") String p,
                             @QueryParam("S") String s,
                             @DefaultValue("0") @QueryParam("from") long from,
                             @DefaultValue("-1") @QueryParam("to") long to) throws IOException {
        Error.reset();
        // spec -> https://github.com/lightblue-platform/lightblue/wiki/Rest-Spec-Data#get-simple-find
        String sq = null;
        if (q != null && !"".equals(q.trim())) {
            List<String> queryList = Arrays.asList(q.split(";"));
            if (queryList.size() > 1) {
                StringBuilder sbq = new StringBuilder("{ \"$and\" : [");
                for (int i = 0; i < queryList.size(); i++) {
                    sbq.append(buildQueryFieldTemplate(queryList.get(i)));
                    if ((i + 1) < queryList.size()) {
                        sbq.append(',');
                    }
                }
                sbq.append("]}");
                sq = sbq.toString();

            } else {
                sq = buildQueryFieldTemplate(queryList.get(0));
            }
        }
        LOGGER.debug("query: {} -> {}", q, sq);

        String sp = DEFAULT_PROJECTION_TMPL;
        if (p != null && !"".equals(p.trim())) {
            List<String> projectionList = Arrays.asList(p.split(","));
            if (projectionList.size() > 1) {
                StringBuilder sbp = new StringBuilder("[");
                for (int i = 0; i < projectionList.size(); i++) {
                    sbp.append(buildProjectionTemplate(projectionList.get(i)));
                    if ((i + 1) < projectionList.size()) {
                        sbp.append(',');
                    }
                }
                sbp.append("]");
                sp = sbp.toString();

            } else {
                sp = buildProjectionTemplate(projectionList.get(0));
            }
        }
        LOGGER.debug("projection: {} -> {}", p, sp);

        String ss = null;
        if (s != null && !"".equals(s.trim())) {
            List<String> sortList = Arrays.asList(s.split(","));
            if (sortList.size() > 1) {
                StringBuilder sbs = new StringBuilder("[");
                for (int i = 0; i < sortList.size(); i++) {
                    sbs.append(buildSortTemplate(sortList.get(i)));
                    if ((i + 1) < sortList.size()) {
                        sbs.append(',');
                    }
                }
                sbs.append("]");
                ss = sbs.toString();

            } else {
                ss = buildSortTemplate(sortList.get(0));
            }
        }
        LOGGER.debug("sort:{} -> {}", s, ss);

        FindRequest findRequest = new FindRequest();
        findRequest.setEntityVersion(new EntityVersion(entity, version));
        findRequest.setQuery(sq == null ? null : QueryExpression.fromJson(JsonUtils.json(sq)));
        findRequest.setProjection(sp == null ? null : Projection.fromJson(JsonUtils.json(sp)));
        findRequest.setSort(ss == null ? null : Sort.fromJson(JsonUtils.json(ss)));
        findRequest.setFrom(from);
        findRequest.setTo(to);
        String request = findRequest.toString();

        return new FindCommand(null, entity, version, request).execute();
    }

    private String buildQueryFieldTemplate(String s1) {
        String sq;
        String template = null;

        String[] split = s1.split(":");

        Map<String, String> map = new HashMap<>();
        map.put("field", split[0]);
        String value = null;

        String[] comma = split[1].split(",");
        if (comma.length > 1) {
            template = FIELD_Q_IN_TMPL;
            value = "\"" + StringUtils.join(comma, "\",\"") + "\"";
        } else {
            template = FIELD_Q_EQ_TMPL;
            value = split[1];
        }
        map.put("value", value);

        StrSubstitutor sub = new StrSubstitutor(map);

        sq = sub.replace(template);
        return sq;
    }

    private String buildProjectionTemplate(String s1) {
        String sp;
        String[] split = s1.split(":");

        Map<String, String> map = new HashMap<>();
        map.put("field", split[0]);
        map.put("include", split[1].charAt(0) == '1' ? "true" : "false");
        map.put("recursive", split[1].length() < 2 ? "false" : (split[1].charAt(1) == 'r' ? "true" : "false"));

        StrSubstitutor sub = new StrSubstitutor(map);

        sp = sub.replace(PROJECTION_TMPL);
        return sp;
    }

    private String buildSortTemplate(String s1) {
        String ss;

        String[] split = s1.split(":");

        Map<String, String> map = new HashMap<>();
        map.put("field", split[0]);
        map.put("order", split[1].charAt(0) == 'd' ? "$desc" : "$asc");

        StrSubstitutor sub = new StrSubstitutor(map);

        ss = sub.replace(SORT_TMPL);
        return ss;
    }
}
