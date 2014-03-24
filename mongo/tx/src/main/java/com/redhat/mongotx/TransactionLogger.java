package com.redhat.mongotx;

import java.util.Date;
import java.util.List;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.WriteConcern;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

import com.mongodb.MongoClient;
import com.mongodb.Mongo;


/**
 * This is a transaction logger for MongoDB databases. It provides a
 * rudimentary way of implementing READ_COMMITTED isolation level
 * semantics. That is:
 * 
 * <ul>
 *   <li>All modification operations performed within a transaction 
 *       establishes write-locks on documents</li>
 *   <li>All clients reading documents that were modified within a 
 *       transaction that is not yet committed 
 *       read the unmodified version of the document</li>
 *   <li>All modifications performed during a transaction is not 
 *       visible to the others until transaction is committed</li>
 * </ul>
 *
 * This class requires a new collection to maintain transaction
 * information. This transaction collection contains two types of
 * documents:
 * <ul>
 *   <li>Transaction data: Document identifying a transaction
 * <pre>
 *   {
 *      _id: <transaction id, auto-generated>,
 *      state: <active, rolling_back, or commiting>,
 *      started: <Timestamp for transaction start>,
 *      lastTouched: <Timestamp for last write activity>,
 *      collections: <array of collection names involved in the transaction>
 *   }
 * </pre>
 *   <li>Lock data: For every locked document:
 * <pre>
 *   {
 *     _id: <lockId, format is <collectionName>:<docId>>,
 *     txId: <transaction id>,
 *     state: "new" or "deleted", anything else means "updated",
 *     original: <The original copy of the modified document>
 *   }
 * </pre>
 *
 * When a document is modified or inserted with a transaction, the
 * document is written to a collection named
 * <pre>
 *    <collectionName>.tx
 * </pre>
 * where the collectionName is the collection to which the document 
 * is to be written. 
 */
public class TransactionLogger {

    private final DB db;
    private final DBCollection txCollection;

    private static final String ID="_id";
    private static final String STATE="st";
    private static final String STARTED="strt";
    private static final String LAST_TOUCHED="last";
    private static final String LAST_COLLECTIONS="coll";
    private static final String TX_ID="txId";
    private static final String ORIGINAL="org";

    private static final String IN="$in";

    public enum LockOp { _new, _deleted, _updated };

    enum TxState { active,rolling_back, committing };

    public TransactionLogger(DB db,String txCollectionName) {
        this.db=db;
        this.txCollection=db.getCollection(txCollectionName);
    }

    public TransactionLogger(DB db) {
        this(db,"transactions");
    }

    /**
     * Inserts a record into the transaction collection, and returns
     * the new transaction id
     */
    public String startTransaction() {
        Date date=new Date();
        BasicDBObject txDoc=new BasicDBObject().
            append(STATE,TxState.active.toString()).
            append(STARTED,date).
            append(LAST_TOUCHED,date);
        txCollection.insert(txDoc,WriteConcern.SAFE);
        return txDoc.get(ID).toString();
    }

    /**
     * Returns a transactional data collection for the given
     * collection. It is a collection obtained by appending the name
     * ".tx". 
     */
    private DBCollection getTxDataCollection(DBCollection collection) {
        return db.getCollection(collection.getName()+".tx");
    }

    /**
     * Writes an IN query: _id in ids
     */
    private BasicDBObject idInQ(List<String> ids) {
        return new BasicDBObject(ID,new BasicDBObject(IN,ids));
    }


    /**
     * Checks if the transaction exists with active status, and
     * updates the timestamp. Adds the collection to the collections
     * set.
     */
    public void touch(String txId,DBCollection collection) 
        throws InvalidTransactionException {
        BasicDBObject q=new BasicDBObject().
            append(ID,txId).
            append(STATE,TxState.active.toString());
        WriteResult wr=txCollection.update(q,
                                           new BasicDBObject("$set",
                                                             new BasicDBObject(LAST_TOUCHED,new Date())).
                                           new BasicDBObject("$addToSet",
                                                             new BasicDBObject(COLLECTIONS,collection.getName())),
                                           false,false,WriteConcern.SAFE);
        if(wr.getN()!=1)
            throw new InvalidLockException(txId);
    }

    /**
     * Returns a list of lock ids given document ids and a collection
     */
    private List<String> getLockIds(DBCollection collection,List<String> docIds) {
        String prefix=collection.getName()+":";
        List<String> ids=new ArrayList<>(docIds.size());
        for(String id:docIds)
            ids.add(prefix+id);
        return ids;
    }

    /**
     * Returns a list of IDs of the documents
     */
    private List<String> getIds(List<DBObject> docs) {
        List<String> list=new ArrayList<>(docs.size());
        for(DBObject doc:docs) {
            list.add(doc.get(ID));
        }
        return list;
    }
    
    /**
     * Either locks all docs, or none of them. Lock data is saved into
     * the transaction collection, with id=collection:docId, txId:
     * transactionId, and lock:w
     */
    public void lock(String txId,DBCollection collection,List<String> docIds,LockOp op) 
        throws LockException, InvalidTransactionException {
        // Try to lock the document by inserting lock data
        DBObject[] lockData=new DBObject[docIds.size()];
        int i=0;
        List<String> lockIds=new ArrayList<>(docIds.size());
        String prefix=collection.getName()+":";
        for(String docId:docIds) {
            String lockId=prefix+docId;
            lockIds.add(lockId);
            lockData[i++]=new BasicDBObject().
                append(ID,lockId).
                append(TX_ID,txId).
                append(STATE,op.toString());
        }
        try {
            WriteResult result=txCollection.insert(lockData,WriteConcern.SAFE);
            if(result.getError()!=null)
                throw new LockException(txId,collection.getName(),docIds);

            // Check transaction validity here
            touch(txId,collection);
        } catch (Exception e) {
            // Cleanup
            try {
                txCollection.remove(idInQ(lockIds));
            } catch (Exception e) {
                System.out.println("Error during lock cleanup:"+e);
            }
        }
    }

    public void lock(String txId,DBCollection collection,String docId,LockOp state) 
        throws LockException, InvalidTransactionException {
        lock(txId,collection,new String[] {docId},state);
    }

    public void unlock(String txId,DBCollection collection,List<String> docIds) 
        throws LockException, InvalidTransactionException {
        touch(txId,collection);
        List<String> lockIds=getLockIds(collection,docIds);
        try {
            // Invalidate the locks first
            txCollection.remove(idInQ(lockIds));
            // Cleanup any modified data in the collection
            getTxDataCollection(collection).remove(idInQ(docIds));
        } catch (Exception e) {
            throw new LockException(txId,collection.getName(),docIds);
        }
    }
    
    public void insert(String txId,DBCollection collection,List<DBObject> docs) {
        touch(txId,collection);
        try {
            WriteResult wr=getTxDataCollection(collection).insert(docs);
            if(wr.getError()!=null)
                throw new LockException(txId,collection,wr.getError());
            List<BasicDBObject> lockData=new List<>(docs.size());
            String prefix=collection.getName()+":";
            for(DBObject doc:docs) {
                lockData.add(new BasicDBObject().
                             append(ID,prefix+doc.get(ID).toString()).
                             append(TX_ID,txId).
                             append(STATE,LockOp._new.toString()));
            }
            wr=txCollection.insert(lockData);
            if(wr.getError()!=null)
                throw new LockException(txId,collection,wr.getError());
        } catch(LockException le) {
            throw le;
        } catch(Exception e) {
            throw new LockException(txId,collection,e.toString());
        }
    }
     
    public DBObject find(String txId,DBCollection collection,String docId) {
        BasicDBObject q=new BasicDBObject();
        q.put(ID,docId);
        return getTxDataCollection(collection).findOne(q);
    }

    public DBCursor find(String txId,DBCollection collection,DBObject query) {
        return getTxDataCollection(collection).find(query);
    }

    /**
     * Assumes all docs are already locked
     */
    public void delete(String txId,DBCollection collection,List<String> docIds),
        throws LockException, InvalidTransactionException {
        List<String> lockIds=getLockIds(collection,docIds);
        // Mark locks as deleted
        touch(txId,collection);
        // Remove locks for all new records
        txCollection.remove(idInQ(lockIds).append(STATE,LockOp._new.toString()),WriteConcern.SAFE);
        BasicDBObject inq=idInQ(docIds);
        // Remove all docs from the data collection
        getTxDataCollection(collection).remove(idInQ(docIds));
        // Copy the document original versions to lock data, and update lock states to "deleted"
        DBCursor cursor=collection.find(inq);
        while(cursor.hasNext()) {
            DBObject doc=cursor.next();
            txCollection.update(new BasicDBObject(ID,doc.get(ID).toString()),
                                new BasicDBObject("$set",
                                                  new BasicDBObject(ORIGINAL,doc).
                                                  append(STATE,LockOp._deleted)));
        }
        cursor.close();
    }

    /**
     * Assumes docs are already locked
     */
    public void update(String txId,DBCollection collection,List<DBObject> docs) {
        touch(txId,collection);
        // Make sure all docs are locked, and not deleted
        List<String> lockIds=new ArrayList<>(docs.size());
        String prefix=collection.getName()+":";
        for(DBObject doc:docs) {
            lockIds.add(prefix+doc.get(ID));
        }
        long count=txCollection.count(idInQ(lockIds).append(STATE,new BasicDBObject("$ne",LockOp._deleted.toString())));
        // We expect to find exactly that many documents
        if(count!=docs.size())
            throw new LockException(txId,collection,"Expected to find "+docs.size()+
                                    " locked docs, but there are "+cursor.count());
        
        // Save the originals for those docs that are not saved before
        DBCursor cursor=txCollection.find(inInQ(lockIds).append(ORIGINAL,new BasicDBObject("$exists",false)).
                                          append(STATE,new BasicDBObject("$ne",LockOp._new.toString())),
                                          new BasicDBObject(ID));
        int suffixIx=collection.getName()+1;
        List<DBObject> idDocs=cursor.toArray();
        cursor.close();
        if(!idDocs.isEmpty()) {
            List<String> saveIds=new ArrayList<>(idDocs.size());
            for(DBObject idDoc:idDocs) {
                saveIds.add(idDoc.get(ID).substring(suffixIx));
            }
            cursor=collection.find(idInQ(saveIds));
            while(cursor.hasNext()) {
                DBObject doc=cursor.next();
                txCollection.update(new BasicDBObject(ID,prefix+doc.get(ID).toString()),
                                    new BasicDBObject("$set",
                                                      new BasicDBObject(ORIGINAL,doc)));
            }
            cursor.close();
        }
        
        // Update the docs
        DBCollection dataCollection=getTxDataCollection(collection);
        for(DBObject doc:docs) {
            String docId=doc.get(ID).toString();
            
            WriteResult wr=dataCollection.update(new BasicDBObject(ID,docId),
                                                 doc,
                                                 false,false);
            if(wr.getError()!=null) {
                throw new LockException(txId,collection,docId);
            }
        }
    }

    public void rollback(String txId) {
        // Set the transaction to rolling-back state
        WriteResult wr=txCollection.update(new BasicDBObject(ID,txId).
                                           append(STATE,TxState.active.toString()),
                                           new BasicDBObject("$set",new BasicDBObject(STATE,TxState.rolling_back.toString())),
                                           false,false,WriteConcern.FSYNCED);
        if(wr.getError()!=null||wr.getN()!=1)
            throw new IllegalTransactionException(txId);
        // Read the transaction
        DBObject txData=txCollection.findOne(new BasicDBObject(ID,txId));
        if(txData==null)
            throw new IllegalTransactionException(txId);
        // Process all the collections in the tx
        List<String> collections=txData.get(COLLECTIONS);
        DBObject projectId=new BasicDBObject(ID);
        if(collections!=null) {
            for(String collName:collections) {
                // Read all the lock ids for the docs in this collection
                BasicDBObject lockq=new BasicDBObject(ID, 
                                                      new BasicDBObject("$regex", collName+":.*"));
                DBCursor cursor=txCollection.find(lockq,projectId);
                List<DBObject> lockDocs=cursor.toArray();
                List<String> docIds=new ArrayList<>(lockDocs.size());
                for(DBObject lockDoc:lockDocs) {
                    String id=lockDoc.get(ID).toString();
                    id=id.substring(collName.length()+1);
                    docIds.add(id);
                }
                txCollection.getDB().getCollection(collName).remove(new BasicDBObject(ID,
                                                                                      new BasicDBObject("$in",docIds)));
                txCollection.remove(lockq);
            }
        }
        txCollection.remove(new BasicDBObject(TX_ID,txId));
        txCollection.remove(new BasicDBObject(ID,txId));
    }

    private static final List<String> newDeletedList=Arrays.asList(LockOp._new.toString(),LockOp._deleted.toString());

    public void commit(String txId) {
        // Set the transaction to committing state
        WriteResult wr=txCollection.update(new BasicDBObject(ID,txId).
                                           append(STATE,TxState.active.toString()),
                                           new BasicDObject("$set",new BasicDBObject(STATE,TxState.committing.toString())),
                                           false,false,WriteConcern.FSYNCED);
        if(wr.getError()!=null||wr.getN()!=1)
            throw new IllegalTransactionException(txId);
        // Read the transaction
        DBObject txData=txCollection.findOne(new BasicDBObject(ID,txId));
        if(txData==null)
            throw new IllegalTransactionException(txId);
        
        // Process all the collections in the tx
        List<String> collections=txData.get(COLLECTIONS);
        if(collections!=null) {
            for(String collName:collections) {
                DBCollection txDataCollection=getTxDataCollection(collName);
                DBCollection dataCollection=db.getCollection(collName);

                DBCursor cursor=getDocsOfState(txDataCollection,collName,LockOp._new.toString());
                if(cursor!=null) {
                    List<DBObject> docs=cursor.toArray();
                    if(!docs.isEmpty()) {
                        WriteResult wr=dataCollection.insert(docs.toArray(new DBObject[docs.size()]),WriteConcern.FSYNCED);
                        if(wr.getError()!=null)
                            throw new CommitError(wr.getError());
                    }
                }
                
                List<DBObject> locks=getLocksOfState(collName,LockOp._deleted.toString());
                if(!locks.isEmpty()) {
                    List<String> ids=getDocIdsFromLocks(locks);
                    wr=dataCollection.remove(new BasicDBObject(ID,
                                                               new BasicDBObject("$in",ids)),WriteConcern.FSYNCED);
                    if(wr.getError()!=null)
                        throw new CommitError(wr.getError());
                }
                

                // Anything that's not new or deleted 
                cursor=txCollection.find(new BasicDBObject(ID,
                                                           new BasicDBObject("$regex",collName+":.*")).
                                         append(STATE,
                                                new BasicDBObject("$nin",newDeletedList)));
                locks=cursor.toArray();
                if(!locks.isEmpty()) {
                    cursor=getDocsForLocks(collName,locks);
                    List<DBObject> docs=cursor.toArray();
                    for(DBObject doc:docs) {
                        wr=collection.update(new BasicDBObject(ID,doc.get(ID).toString()),
                                             doc,
                                             false,false,WriteConcern.FSYNCED);
                    }
                }
            }
        }
        txCollection.remove(new BasicDBObject(TX_ID,txId));
        txCollection.remove(new BasicDBObject(ID,txId));
    }

    public void restore(String txId) {
        // Set the transaction to rolling-back state
        WriteResult wr=txCollection.update(new BasicDBObject(ID,txId),
                                           new BasicDBObject("$set",new BasicDBObject(STATE,TxState.rolling_back.toString())),
                                           false,false,WriteConcern.FSYNCED);
        if(wr.getError()!=null||wr.getN()!=1)
            throw new IllegalTransactionException(txId);
        // Read the transaction
        DBObject txData=txCollection.findOne(new BasicDBObject(ID,txId));
        if(txData==null)
            throw new IllegalTransactionException(txId);
        // Process all the collections in the tx
        List<String> collections=txData.get(COLLECTIONS);
        DBObject projectId=new BasicDBObject(ID);
        if(collections!=null) {
            for(String collName:collections) {
                DBCollection collection=db.getCollection(collName);
                // Restore all deleted docs
                DBCursor cursor=txCollection.find(new BasicDBObject(ID,
                                                                    new BasicDBObject("$regex",collName+":.*")).
                                                  append(STATE,LockOp._deleted.toString()));
                while(cursor.hasNext()) {
                    DBObject doc=cursor.next();
                    DBObject original=doc.get(ORIGINAL);
                    if(original!=null) {
                        collection.insert(original);
                    }
                }

                // Restore all modified docs
                cursor=txCollection.find(new BasicDBObject(ID,
                                                           new BasicDBObject("$regex",collName+":.*")).
                                         append(STATE,
                                                new BasicDBObject("$nin",newDeletedList)));
                while(cursor.hasNext()) {
                    DBObject doc=cursor.next();
                    DBObject original=doc.get(ORIGINAL);
                    if(original!=null) {
                        collection.save(original);
                    }
                }
            }
        }
        txCollection.remove(new BasicDBObject(TX_ID,txId));
        txCollection.remove(new BasicDBObject(ID,txId));
    }


    private List<DBObject> getLocksOfState(String collName,
                                           String state) {
        // Get all lock records with that state
        DBCursor cursor=txCollection.find(new BasicDBObject(ID,
                                                            new BasicDBObject("$regex",collName+":.*")).
                                          append(STATE,state));
        return cursor.toArray();
    }
    
    private DBCursor getDocsOfState(DBCollection txDataCollection,
                                    String collName,
                                    String state) {
        List<String> lockData=getLocksOfState(collName,state);
        if(!lockData.isEmpty())
            return getDocsForLocks(collName,lockData);
        else
            return null;
    }

    private DBCursor getDocsForLocks(String collName,List<DBObject> lockData) {
        List<String> docIds=new ArrayList<>(lockData.size());
        int collSuffix=collName.length()+1;
        for(DBObject lock:lockData)
            docIds.add(lock.get(ID).toString().substring(collSuffix));
        return txDataCollection.find(idInQ(docIds));
    }

    public static void main(String[] args) throws Exception {
        String dbHost = arg("dbHost", args, true);
        String dbPort = arg("dbPort", args, true);
        String dbName = arg("dbName", args);
        DB db=null;
        if (dbHost == null) {
            dbHost = "localhost";
        }
        if (dbPort == null) {
            dbPort = "27017";
        }
        MongoClient client = new MongoClient(dbHost, Integer.valueOf(dbPort));
        db = client.getDB(dbName);
        if(db!=null) {
            TransactionLogger tx=new TransactionLogger(db,"tx");

            tx.getTxDataCollection(db.getCollection("test"));

            String cmd=arg("cmd",args);
            if(cmd!=null) {
                if("startTransaction".equals(cmd)) {
                    System.out.println(tx.startTransaction());
                } else if("lock".equals(cmd)) {
                    String txId=arg("txId",args);
                    String collection=arg("collection",args);
                    String docId=arg("docId",args);
                    String state=arg("state",args,true);
                    DBCollection coll=db.getCollection(collection);
                    //tx.lock(txId,coll,docId,state);
                } else if("unlock".equals(cmd)) {
                    String txId=arg("txId",args);
                    String collection=arg("collection",args);
                    String docId=arg("docId",args);
                    DBCollection coll=db.getCollection(collection);
                    tx.unlock(txId,coll,docId);
                } else
                    System.out.println("Unknown command");
            } 
        } else
            System.out.println("Can't connect");
    }

    private static String arg(String argName, String[] args, boolean optional) {
        for (String x : args) {
            if (x.startsWith(argName + "=")) {
                return x.substring(argName.length() + 1);
            }
        }
        if (!optional) {
            throw new RuntimeException("Required:" + argName);
        }
        return null;
    }

    private static String arg(String argName, String[] args) {
        return arg(argName, args, false);
    }
}
