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
package com.redhat.lightblue.crud.mongo;

import java.util.List;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.redhat.lightblue.interceptor.InterceptPoint;
import com.redhat.lightblue.common.mongo.DBResolver;
import com.redhat.lightblue.common.mongo.MongoDataStore;
import com.redhat.lightblue.crud.CRUDController;
import com.redhat.lightblue.crud.CRUDDeleteResponse;
import com.redhat.lightblue.crud.CRUDFindResponse;
import com.redhat.lightblue.crud.CRUDInsertionResponse;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.CRUDSaveResponse;
import com.redhat.lightblue.crud.CRUDUpdateResponse;
import com.redhat.lightblue.crud.ConstraintValidator;
import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.crud.DocCtx;
import com.redhat.lightblue.eval.FieldAccessRoleEvaluator;
import com.redhat.lightblue.eval.Projector;
import com.redhat.lightblue.eval.Updater;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.metadata.Index;
import com.redhat.lightblue.metadata.Indexes;
import com.redhat.lightblue.metadata.EntityInfo;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Field;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.MetadataConstants;
import com.redhat.lightblue.query.FieldProjection;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.query.SortKey;
import com.redhat.lightblue.query.UpdateExpression;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

public class MongoCRUDController implements CRUDController {

    public static final String ID_STR = "_id";

    /**
     * Name of the property for the operation context that keeps the last saver class instance used
     */
    public static final String PROP_SAVER = "MongoCRUDController:saver";

    /**
     * Name of the property for the operation context that keeps the last updater class instance used
     */
    public static final String PROP_UPDATER = "MongoCRUDController:updater";

    /**
     * Name of the property for the operation context that keeps the last deleter class instance used
     */
    public static final String PROP_DELETER = "MongoCRUDController:deleter";

    /**
     * Name of the property for the operation context that keeps the last finder class instance used
     */
    public static final String PROP_FINDER = "MongoCRUDController:finder";

    public static final String OP_INSERT = "insert";
    public static final String OP_SAVE = "save";
    public static final String OP_FIND = "find";
    public static final String OP_UPDATE = "update";
    public static final String OP_DELETE = "delete";

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoCRUDController.class);

    private static final Projection ID_PROJECTION = new FieldProjection(new Path(ID_STR), true, false);

    private final DBResolver dbResolver;

    public MongoCRUDController(DBResolver dbResolver) {
        this.dbResolver = dbResolver;
    }

    /**
     * Insertion operation for mongo
     */
    @Override
    public CRUDInsertionResponse insert(CRUDOperationContext ctx,
                                        Projection projection) {
        LOGGER.debug("insert() start");
        CRUDInsertionResponse response = new CRUDInsertionResponse();
        ctx.getFactory().getInterceptors().callInterceptors(InterceptPoint.PRE_CRUD_INSERT,ctx);
        int n = saveOrInsert(ctx, false, projection, OP_INSERT);
        response.setNumInserted(n);
        ctx.getFactory().getInterceptors().callInterceptors(InterceptPoint.POST_CRUD_INSERT,ctx);
        return response;
    }

    @Override
    public CRUDSaveResponse save(CRUDOperationContext ctx,
                                 boolean upsert,
                                 Projection projection) {
        LOGGER.debug("save() start");
        CRUDSaveResponse response = new CRUDSaveResponse();
        ctx.getFactory().getInterceptors().callInterceptors(InterceptPoint.PRE_CRUD_SAVE,ctx);
        int n = saveOrInsert(ctx, upsert, projection, OP_SAVE);
        response.setNumSaved(n);
        ctx.getFactory().getInterceptors().callInterceptors(InterceptPoint.POST_CRUD_SAVE,ctx);
        return response;
    }

    private int saveOrInsert(CRUDOperationContext ctx,
                             boolean upsert,
                             Projection projection,
                             String operation) {
        int ret = 0;
        List<DocCtx> documents = ctx.getDocumentsWithoutErrors();
        if (documents == null || documents.isEmpty()) {
            return ret;
        }
        for (DocCtx doc : documents) {
            doc.setOriginalDocument(doc);
        }
        LOGGER.debug("saveOrInsert() start");
        Error.push(operation);
        Translator translator = new Translator(ctx,ctx.getFactory().getNodeFactory());
        try {
            FieldAccessRoleEvaluator roleEval
                    = new FieldAccessRoleEvaluator(ctx.getEntityMetadata(ctx.getEntityName()),
                            ctx.getCallerRoles());
            LOGGER.debug("saveOrInsert: Translating docs");
            EntityMetadata md = ctx.getEntityMetadata(ctx.getEntityName());
            DBObject[] dbObjects = translator.toBson(documents);
            // dbObjects[i] is the translation of documents.get(i)
            if (dbObjects != null) {
                LOGGER.debug("saveOrInsert: {} docs translated to bson", dbObjects.length);

                MongoDataStore store = (MongoDataStore) md.getDataStore();
                DB db = dbResolver.get(store);
                DBCollection collection = db.getCollection(store.getCollectionName());

                Projection combinedProjection = Projection.add(projection, roleEval.getExcludedFields(FieldAccessRoleEvaluator.Operation.find));

                Projector projector;
                if (combinedProjection != null) {
                    projector = Projector.getInstance(combinedProjection, md);
                } else {
                    projector = null;
                }
                DocSaver saver = new BasicDocSaver(translator, roleEval);
                ctx.setProperty(PROP_SAVER, saver);
                for (int docIndex = 0; docIndex < dbObjects.length; docIndex++) {
                    DBObject dbObject = dbObjects[docIndex];
                    DocCtx inputDoc = documents.get(docIndex);
                    try {
                        saver.saveDoc(ctx, operation.equals(OP_INSERT) ? DocSaver.Op.insert : DocSaver.Op.save,
                                upsert, collection, md, dbObject, inputDoc);
                        ctx.getHookManager().queueHooks(ctx);
                    } catch (Exception e) {
                        LOGGER.error("saveOrInsert failed: {}", e);
                        inputDoc.addError(Error.get(operation, MongoCrudConstants.ERR_SAVE_ERROR, e.toString()));
                    }
                    if (projector != null) {
                        JsonDoc jsonDoc = translator.toJson(dbObject);
                        LOGGER.debug("Translated doc: {}", jsonDoc);
                        inputDoc.setOutputDocument(projector.project(jsonDoc, ctx.getFactory().getNodeFactory()));
                    } else {
                        inputDoc.setOutputDocument(null);
                    }
                    LOGGER.debug("projected doc: {}", inputDoc.getOutputDocument());
                    if (!inputDoc.hasErrors()) {
                        ret++;
                    }
                }
            }
        } catch (Error e) {
            // rethrow lightblue error
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error during insert: {}", e);
            // throw new Error (preserves current error context)
            LOGGER.error(e.getMessage(), e);
            throw Error.get(CrudConstants.ERR_CRUD, e.getMessage());
        } finally {
            Error.pop();
        }
        LOGGER.debug("saveOrInsert() end: {} docs requested, {} saved", documents.size(), ret);
        return ret;
    }

    @Override
    public CRUDUpdateResponse update(CRUDOperationContext ctx,
                                     QueryExpression query,
                                     UpdateExpression update,
                                     Projection projection) {
        if (query == null) {
            throw new IllegalArgumentException(MongoCrudConstants.ERR_NULL_QUERY);
        }
        LOGGER.debug("update start: q:{} u:{} p:{}", query, update, projection);
        Error.push(OP_UPDATE);
        CRUDUpdateResponse response = new CRUDUpdateResponse();
        Translator translator = new Translator(ctx,ctx.getFactory().getNodeFactory());
        ctx.getFactory().getInterceptors().callInterceptors(InterceptPoint.PRE_CRUD_UPDATE,ctx);
        try {
            EntityMetadata md = ctx.getEntityMetadata(ctx.getEntityName());
            if (md.getAccess().getUpdate().hasAccess(ctx.getCallerRoles())) {
                ConstraintValidator validator = ctx.getFactory().getConstraintValidator(md);
                LOGGER.debug("Translating query {}", query);
                DBObject mongoQuery = translator.translate(md, query);
                LOGGER.debug("Translated query {}", mongoQuery);
                FieldAccessRoleEvaluator roleEval = new FieldAccessRoleEvaluator(md, ctx.getCallerRoles());

                Projector projector;
                if (projection != null) {
                    Projection x = Projection.add(projection, roleEval.getExcludedFields(FieldAccessRoleEvaluator.Operation.find));
                    LOGGER.debug("Projection={}", x);
                    projector = Projector.getInstance(x, md);
                } else {
                    projector = null;
                }
                DB db = dbResolver.get((MongoDataStore) md.getDataStore());
                DBCollection coll = db.getCollection(((MongoDataStore) md.getDataStore()).getCollectionName());
                Projector errorProjector;
                if (projector == null) {
                    errorProjector = Projector.getInstance(ID_PROJECTION, md);
                } else {
                    errorProjector = projector;
                }

                // If there are any constraints for updated fields, or if we're updating arrays, we have to use iterate-update
                Updater updater = Updater.getInstance(ctx.getFactory().getNodeFactory(), md, update);
                // Set<Path> updatedFields = updater.getUpdateFields();
                // LOGGER.debug("Fields to be updated:{}", updatedFields);
                // boolean constrainedFieldUpdated = false;
                // for (Path x : updatedFields) {
                //     FieldTreeNode ftn = md.resolve(x);
                //     if (hasArray(ftn)||(ftn instanceof Field  && !((Field) ftn).getConstraints().isEmpty())) {
                //         LOGGER.debug("Field {} either has constraints, or goes through an array, can't run direct mongo update", ftn);
                //         constrainedFieldUpdated = true;
                //         break;
                //     }
                // }
                DocUpdater docUpdater = new IterateAndUpdate(ctx.getFactory().getNodeFactory(), validator, roleEval, translator, updater,
                                                             projector, errorProjector);
                ctx.setProperty(PROP_UPDATER, docUpdater);
                docUpdater.update(ctx, coll, md, response, mongoQuery);
                ctx.getHookManager().queueHooks(ctx);
            } else {
                ctx.addError(Error.get(MongoCrudConstants.ERR_NO_ACCESS, "update:" + ctx.getEntityName()));
            }
        } catch (Error e) {
            // rethrow lightblue error
            throw e;
        } catch (Exception e) {
            // throw new Error (preserves current error context)
            LOGGER.error(e.getMessage(), e);
            throw Error.get(CrudConstants.ERR_CRUD, e.getMessage());
        } finally {
            Error.pop();
        }
        ctx.getFactory().getInterceptors().callInterceptors(InterceptPoint.POST_CRUD_UPDATE,ctx);
        LOGGER.debug("update end: updated: {}, failed: {}", response.getNumUpdated(), response.getNumFailed());
        return response;
    }

    // private boolean hasArray(FieldTreeNode ftn) {
    //     do {
    //         if(ftn instanceof ArrayNode)
    //             return true;
    //         ftn=ftn.getParent();
    //     } while(ftn!=null);
    //     return false;
    // }

    @Override
    public CRUDDeleteResponse delete(CRUDOperationContext ctx,
                                     QueryExpression query) {
        if (query == null) {
            throw new IllegalArgumentException(MongoCrudConstants.ERR_NULL_QUERY);
        }
        LOGGER.debug("delete start: q:{}", query);
        Error.push(OP_DELETE);
        CRUDDeleteResponse response = new CRUDDeleteResponse();
        Translator translator = new Translator(ctx,ctx.getFactory().getNodeFactory());
        ctx.getFactory().getInterceptors().callInterceptors(InterceptPoint.PRE_CRUD_DELETE,ctx);
        try {
            EntityMetadata md = ctx.getEntityMetadata(ctx.getEntityName());
            if (md.getAccess().getDelete().hasAccess(ctx.getCallerRoles())) {
                LOGGER.debug("Translating query {}", query);
                DBObject mongoQuery = translator.translate(md, query);
                LOGGER.debug("Translated query {}", mongoQuery);
                DB db = dbResolver.get((MongoDataStore) md.getDataStore());
                DBCollection coll = db.getCollection(((MongoDataStore) md.getDataStore()).getCollectionName());
                DocDeleter deleter = new IterateDeleter(translator);
                ctx.setProperty(PROP_DELETER, deleter);
                deleter.delete(ctx, coll, mongoQuery, response);
                ctx.getHookManager().queueHooks(ctx);
            } else {
                ctx.addError(Error.get(MongoCrudConstants.ERR_NO_ACCESS, "delete:" + ctx.getEntityName()));
            }
        } catch (Error e) {
            ctx.addError(e);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            ctx.addError(Error.get(e.toString()));
        } finally {
            Error.pop();
        }
        ctx.getFactory().getInterceptors().callInterceptors(InterceptPoint.POST_CRUD_DELETE,ctx);
        LOGGER.debug("delete end: deleted: {}}", response.getNumDeleted());
        return response;
    }

    /**
     * Search implementation for mongo
     */
    @Override
    public CRUDFindResponse find(CRUDOperationContext ctx,
                                 QueryExpression query,
                                 Projection projection,
                                 Sort sort,
                                 Long from,
                                 Long to) {
        if (query == null) {
            throw new IllegalArgumentException(MongoCrudConstants.ERR_NULL_QUERY);
        }
        if (projection == null) {
            throw new IllegalArgumentException(MongoCrudConstants.ERR_NULL_PROJECTION);
        }
        LOGGER.debug("find start: q:{} p:{} sort:{} from:{} to:{}", query, projection, sort, from, to);
        Error.push(OP_FIND);
        CRUDFindResponse response = new CRUDFindResponse();
        Translator translator = new Translator(ctx,ctx.getFactory().getNodeFactory());
        ctx.getFactory().getInterceptors().callInterceptors(InterceptPoint.PRE_CRUD_FIND,ctx);
        try {
            EntityMetadata md = ctx.getEntityMetadata(ctx.getEntityName());
            if (md.getAccess().getFind().hasAccess(ctx.getCallerRoles())) {
                FieldAccessRoleEvaluator roleEval = new FieldAccessRoleEvaluator(md, ctx.getCallerRoles());
                LOGGER.debug("Translating query {}", query);
                DBObject mongoQuery = translator.translate(md, query);
                LOGGER.debug("Translated query {}", mongoQuery);
                DBObject mongoSort;
                if (sort != null) {
                    LOGGER.debug("Translating sort {}", sort);
                    mongoSort = translator.translate(sort);
                    LOGGER.debug("Translated sort {}", mongoSort);
                } else {
                    mongoSort = null;
                }
                DB db = dbResolver.get((MongoDataStore) md.getDataStore());
                DBCollection coll = db.getCollection(((MongoDataStore) md.getDataStore()).getCollectionName());
                LOGGER.debug("Retrieve db collection:" + coll);
                DocFinder finder = new BasicDocFinder(translator);
                ctx.setProperty(PROP_FINDER, finder);
                response.setSize(finder.find(ctx, coll, mongoQuery, mongoSort, from, to));
                // Project results
                Projector projector = Projector.getInstance(Projection.add(projection, roleEval.getExcludedFields(FieldAccessRoleEvaluator.Operation.find)), md);
                for (DocCtx document : ctx.getDocuments()) {
                    document.setOutputDocument(projector.project(document, ctx.getFactory().getNodeFactory()));
                }
                ctx.getHookManager().queueHooks(ctx);
            } else {
                ctx.addError(Error.get(MongoCrudConstants.ERR_NO_ACCESS, "find:" + ctx.getEntityName()));
            }
        } catch (Error e) {
            // rethrow lightblue error
            throw e;
        } catch (Exception e) {
            // throw new Error (preserves current error context)
            LOGGER.error(e.getMessage(), e);
            throw Error.get(CrudConstants.ERR_CRUD, e.getMessage());
        } finally {
            Error.pop();
        }
        ctx.getFactory().getInterceptors().callInterceptors(InterceptPoint.POST_CRUD_FIND,ctx);
        LOGGER.debug("find end: query: {} results: {}", response.getSize());
        return response;
    }

    @Override
    public void updateEntityInfo(Metadata md,EntityInfo ei) {
        createUpdateEntityInfoIndexes(ei);
    }

    @Override
    public void newSchema(Metadata md,EntityMetadata emd) {
        createUpdateEntityInfoIndexes(emd.getEntityInfo());
    }

    private void createUpdateEntityInfoIndexes(EntityInfo ei) {
        LOGGER.debug("createUpdateEntityInfoIndexes: begin");

        Indexes indexes = ei.getIndexes();

        MongoDataStore ds = (MongoDataStore) ei.getDataStore();
        DB entityDB = dbResolver.get(ds);
        DBCollection entityCollection = entityDB.getCollection(ds.getCollectionName());
        Error.push("createUpdateIndex");
        try {
            List<DBObject> existingIndexes = entityCollection.getIndexInfo();
            LOGGER.debug("Existing indexes: {}", existingIndexes);
            for (Index index : indexes.getIndexes()) {
                boolean createIx = true;
                LOGGER.debug("Processing index {}", index);

                for (DBObject existingIndex : existingIndexes) {
                    if (indexFieldsMatch(index, existingIndex)
                            && indexOptionsMatch(index, existingIndex)) {
                        LOGGER.debug("Same index exists, not creating");
                        createIx = false;
                        break;
                    }
                }

                if (createIx) {
                    for (DBObject existingIndex : existingIndexes) {
                        if (indexFieldsMatch(index, existingIndex)
                                && !indexOptionsMatch(index, existingIndex)) {
                            LOGGER.debug("Same index exists with different options, dropping index:{}", existingIndex);
                            // Changing index options, drop the index using its name, recreate with new options
                            entityCollection.dropIndex(existingIndex.get("name").toString());
                        }
                    }
                }

                if (createIx) {
                    DBObject newIndex = new BasicDBObject();
                    for (SortKey p : index.getFields()) {
                        newIndex.put(p.getField().toString(), p.isDesc() ? -1 : 1);
                    }
                    BasicDBObject options = new BasicDBObject("unique", index.isUnique());
                    if (index.getName() != null && index.getName().trim().length() > 0) {
                        options.append("name", index.getName().trim());
                    }
                    options.append("background",true);
                    LOGGER.debug("Creating index {} with options {}", newIndex, options);
                    entityCollection.createIndex(newIndex, options);
                }
            }
        } catch (MongoException me) {
            LOGGER.error("createUpdateEntityInfoIndexes: {}", ei);
            throw Error.get(MongoCrudConstants.ERR_ENTITY_INDEX_NOT_CREATED, me.getMessage());
        } catch (Error e) {
            // rethrow lightblue error
            throw e;
        } catch (Exception e) {
            // throw new Error (preserves current error context)
            LOGGER.error(e.getMessage(), e);
            throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, e.getMessage());
        } finally {
            Error.pop();
        }

        LOGGER.debug("createUpdateEntityInfoIndexes: end");
    }

    private boolean compareSortKeys(SortKey sortKey, String fieldName, Object dir) {
        if (sortKey.getField().toString().equals(fieldName)) {
            int direction = ((Number) dir).intValue();
            return sortKey.isDesc() == (direction < 0);
        }
        return false;
    }

    private boolean indexFieldsMatch(Index index, DBObject existingIndex) {
        BasicDBObject keys = (BasicDBObject) existingIndex.get("key");
        if (keys != null) {
            List<SortKey> fields = index.getFields();
            if (keys.size() == fields.size()) {
                Iterator<SortKey> sortKeyItr = fields.iterator();
                for (Map.Entry<String, Object> entry : keys.entrySet()) {
                    SortKey sortKey = sortKeyItr.next();
                    if (!compareSortKeys(sortKey, entry.getKey(), entry.getValue())) {
                        return false;
                    }
                }
                return true;
            }
        }
        return true;
    }

    private boolean indexOptionsMatch(Index index, DBObject existingIndex) {
        Boolean unique = (Boolean) existingIndex.get("unique");
        if (unique != null) {
            if ((unique && index.isUnique())
                    || (!unique && !index.isUnique())) {
                return true;
            }
        } else {
            if (!index.isUnique()) {
                return true;
            }
        }
        return false;
    }
}
