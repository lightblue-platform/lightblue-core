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

import java.util.Iterator;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;

import java.util.concurrent.TimeUnit;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.NullNode;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import com.redhat.lightblue.Request;
import com.redhat.lightblue.Response;
import com.redhat.lightblue.ClientIdentification;
import com.redhat.lightblue.EntityVersion;

import com.redhat.lightblue.config.SavedSearchConfiguration;

import com.redhat.lightblue.crud.FindRequest;
import com.redhat.lightblue.crud.CrudConstants;

import com.redhat.lightblue.mediator.Mediator;

import com.redhat.lightblue.metadata.EntityMetadata;

import com.redhat.lightblue.query.*;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonUtils;

/**
 * This class is the main access point to saved searches. It loads
 * saved searches from the db, and keeps them in a weak cache.
 *
 * We only provide the basics to implement saved search functionality
 * at this level. The functionality must be enabled at the higher
 * layer, REST or embedded application layers.
 *
 * The implementation should:
 * <ul>
 * <li>Instantiate a singleton instance of the SavedSearchCache. This 
 * should be shared among all threads.</li>
 * <li>Determine the name of the saved search, the entity, and its version.
 * The saved search name is meaningful only with its associated entity<li?.
 * <li>Retrieve the saved search document using SavedSearchCache.getSavedSearch</li>
 * <li>Collect the query parameters, and fill in defaults using FindRequestBuilder.fillDefaults</li>
 * <li>Prepare a FindRequest using FindRequestBuilder.buildRequest.</li>
 * <li>Call find()</li>
 * </ul>
 */
public class SavedSearchCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(SavedSearchCache.class);

    public static final String ERR_SAVED_SEARCH="crud:saved-search";

    Cache<Key,ObjectNode> cache;

    private static final Path P_NAME=new Path("name");
    private static final Path P_ENTITY=new Path("entity");
    private static final Path P_VERSIONS=new Path("versions");

    private static final Value NULL_VALUE=new Value(null);

    public final String savedSearchEntity;
    public final String savedSearchVersion;

    public static class RetrievalError extends RuntimeException {
        public final List<Error> errors;
        
        public RetrievalError(List<Error> errors) {
            this.errors=errors;
        }

        @Override
        public String toString() {
            return errors.toString()+"\n"+super.toString();
        }
    }
    
    private static class Key {
        final String searchName;
        final String entity;
        final String version;
        
        @Override
        public boolean equals(Object o) {
            if(o instanceof Key) {
                return Objects.equals(((Key)o).searchName,searchName)&&
                    Objects.equals(((Key)o).entity,entity)&&
                    Objects.equals(((Key)o).version,version);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (version==null?1:version.hashCode())*searchName.hashCode()*entity.hashCode();
        }

        @Override
        public String toString() {
            return searchName+":"+entity+":"+version;
        }
        
        public Key(String searchName,String entity,String version) {
            this.searchName=searchName;
            this.entity=entity;
            this.version=version;
        }
    }
    
    public SavedSearchCache(SavedSearchConfiguration cfg) {
        if(cfg!=null) {
            savedSearchEntity=cfg.getEntity();
            savedSearchVersion=cfg.getEntityVersion();
            initializeCache(cfg.getCacheConfig());
        } else {
            savedSearchEntity="savedSearch";
            savedSearchVersion=null;
            initializeCache(null);
        }
    }

    private void initializeCache(String spec) {
        if(spec==null) {
            cache=CacheBuilder.newBuilder().
                maximumSize(2048).
                expireAfterAccess(2,TimeUnit.MINUTES).
                softValues().
                build();
        } else {
            cache=CacheBuilder.from(spec).build();
        }
    }


    /**
     * Retrieves a saved search from the database. 
     *
     * @param m Mediator instance
     * @param clid The client id
     * @param searchName name of the saved search
     * @param entit Name of the entity
     * @param version Entity version the search should run on
     * 
     * The returned JsonNode can be an ObjectNode, or an ArrayNode
     * containing zero or more documents. If there are more than one
     * documents, only one of them is for the requested version, and
     * the other is for the null version that applies to all
     * versions. It returns null or empty array if nothing is found.
     * In case of retrieval error, a RetrievalError is thrown
     * containing the errors.
     */
    public JsonNode getSavedSearchFromDB(Mediator m,
                                         ClientIdentification clid,
                                         String searchName,
                                         String entity,
                                         String version) {
        
        FindRequest findRequest=new FindRequest();
        findRequest.setEntityVersion(new EntityVersion(savedSearchEntity,savedSearchVersion));
        findRequest.setClientId(clid);
        List<Value> versionList=new ArrayList<>(2);
        versionList.add(NULL_VALUE);
        // Include all segments of the version in the search list
        if(version!=null) {
            int index=0;
            while((index=version.indexOf('.',index))!=-1) {
                versionList.add(new Value(version.substring(0,index)));
                index++;
            }
            versionList.add(new Value(version));
        }
        QueryExpression q=new NaryLogicalExpression(NaryLogicalOperator._and,
                                                    new ValueComparisonExpression(P_NAME,
                                                                                  BinaryComparisonOperator._eq,
                                                                                  new Value(searchName)),
                                                    new ValueComparisonExpression(P_ENTITY,
                                                                                  BinaryComparisonOperator._eq,
                                                                                  new Value(entity)),
                                                    new ArrayContainsExpression(P_VERSIONS,
                                                                                ContainsOperator._any,
                                                                                versionList));
        LOGGER.debug("Searching {}",q);
        findRequest.setQuery(q);
        findRequest.setProjection(FieldProjection.ALL);
        Response response=m.find(findRequest);
        if(response.getErrors()!=null&&!response.getErrors().isEmpty())
            throw new RetrievalError(response.getErrors());
        LOGGER.debug("Found {}",response.getEntityData());
        return response.getEntityData();
    }

    /**
     * Either loads the saved search from the db, or from the
     * cache. 
     *
     * @param m Mediator instance
     * @param clid The client id
     * @param searchName name of the saved search
     * @param entit Name of the entity
     * @param version Entity version the search should run on
     *
     * @return The saved search document, or null if not found
     */
    public JsonNode getSavedSearch(Mediator m,
                                   ClientIdentification clid,
                                   String searchName,
                                   String entity,
                                   String version) {
        LOGGER.debug("Loading {}:{}:{}",searchName,entity,version);
        ObjectNode doc=null;
        String loadVersion;
        if(version==null) {
            LOGGER.debug("{} version is null, attempting to find default version for entity",entity);
            EntityMetadata md=m.metadata.getEntityMetadata(entity,null);
            if(md==null)
                throw Error.get(CrudConstants.ERR_UNKNOWN_ENTITY,entity+":"+version);
            loadVersion=md.getVersion().getValue();
            LOGGER.debug("Loading {}:{}:{}",searchName,entity,loadVersion);
        } else {
            loadVersion=version;
        }
        Key key=new Key(searchName,entity,loadVersion);
        LOGGER.debug("Lookup {}",key);
        doc=cache.getIfPresent(key);
        if(doc==null) {
            key=new Key(searchName,entity,null);
            LOGGER.debug("Lookup {}",key);
            doc=cache.getIfPresent(key);
        }
        if(doc==null) {
            LOGGER.debug("Loading {} from DB",searchName);
            JsonNode node=getSavedSearchFromDB(m,clid,searchName,entity,loadVersion);
            if(node instanceof ObjectNode) {
                LOGGER.debug("Loaded a single search");
                doc=(ObjectNode)node;
                store(doc);
            } else if(node instanceof ArrayNode) {
                LOGGER.debug("Loaded an array of searches");
                store((ArrayNode)node);
                doc=findDocForVersion((ArrayNode)node,loadVersion);
            }
            if(doc!=null) {
                store(doc);
            }
        }
        return doc;
    }

    private ObjectNode findDocForVersion(ArrayNode node,String version) {
        LOGGER.debug("Searching {} in the array",version);
        ObjectNode ret=null;
        String matchedVersion=null;
        for(Iterator<JsonNode> itr=node.elements();itr.hasNext();) {
            JsonNode searchNode=itr.next();
            if(searchNode instanceof ObjectNode) {
                JsonNode versionsNode=searchNode.get("versions");
                LOGGER.debug("Versions in search:{}",versionsNode);
                if(versionsNode instanceof ArrayNode && versionsNode.size()>0) {
                    LOGGER.debug("Looking up in versions array");
                    for(Iterator<JsonNode> vitr=versionsNode.elements();vitr.hasNext();) {
                        JsonNode versionNode=vitr.next();
                        if(versionNode instanceof NullNode) {
                            if(betterMatch(version,null,matchedVersion)) {
                                ret=(ObjectNode)searchNode;
                                matchedVersion=null;
                            }
                        } else if(versionNode instanceof TextNode) {
                            String v=versionNode.asText();
                            if(betterMatch(version,v,matchedVersion)) {
                                ret=(ObjectNode)searchNode;
                                matchedVersion=v;
                            }
                        }
                    }
                } else {
                    if(ret==null) {
                        ret=(ObjectNode)searchNode;
                        matchedVersion=null;
                    }
                }
                LOGGER.debug("Current best match after {}:{}",searchNode,ret);
            }
        }
        LOGGER.debug("Best match for version {}:{}",version,ret);
        return ret;
    }

    /**
     * Returns true if newVersion is a better match to searchedVersion than matchedVersion
     */
    private boolean betterMatch(String searchedVersion,String newVersion,String matchedVersion) {
        if(searchedVersion==null) {
            return newVersion==null;
        } else {
            if(newVersion==null) { // Match any version
                return matchedVersion==null;
            } else {
                // newVersion not null
                // If searched version is this version, than it is a perfect match
                if(searchedVersion.equals(newVersion)) {
                    return true;
                } else {
                    // if newVersion is a longer prefix of searchVersion than matchedVersion is, then it is a better match
                    int newMatchingPrefix=getMatchingPrefix(searchedVersion,newVersion);
                    int oldMatchingPrefix=getMatchingPrefix(searchedVersion,matchedVersion);
                    LOGGER.debug("Comparing to {}: {}: prefix={} {}: prefix={}",searchedVersion,newVersion,newMatchingPrefix,
                                 matchedVersion,oldMatchingPrefix);
                    return newMatchingPrefix>oldMatchingPrefix;
                }
            }
        }
    }

    /**
     * Return the length of the matching version prefix
     */
    private int getMatchingPrefix(String fullVersion,String prefix) {
        if(prefix==null) {
            return 0;
        } else {
            int p=prefix.length();
            if(p<=fullVersion.length()) {
                if(fullVersion.startsWith(prefix)) {
                    if(p<fullVersion.length()) {
                        if(fullVersion.charAt(p)=='.') {
                            return prefix.length();
                        } else {
                            return 0;
                        }
                    } else {
                        return fullVersion.length();
                    }
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        }
    }

    private synchronized void store(ObjectNode doc) {
        String name=doc.get("name").asText();
        String entity=doc.get("entity").asText();
        ArrayNode arr=(ArrayNode)doc.get("versions");
        Key key=null;
        if(arr!=null) {
            for(Iterator<JsonNode> itr=arr.elements();itr.hasNext();) {
                JsonNode version=itr.next();
                if(version instanceof NullNode) {
                    key=new Key(name,entity,null);
                } else {
                    key=new Key(name,entity,version.asText());
                }
            }
        }
        if(key==null) {
            key=new Key(name,entity,null);
        }
        LOGGER.debug("Adding {} to cache",key);
        cache.put(key,doc);
    }

    private synchronized void store(ArrayNode arr) {
        for(Iterator<JsonNode> itr=arr.elements();itr.hasNext();) {
            JsonNode node=itr.next();
            if(node instanceof ObjectNode)
                store( (ObjectNode)node);
        }
    }
}
