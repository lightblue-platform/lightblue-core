package com.redhat.mongotx;

public class LockException extends Exception {

    public LockException(String txId,String collection,String msg) {
        super("Cannot lock: txId=:"+txId+" doc="+collection+":"+msg);
    }
    public LockException(String txId,String collection,List<String> ids) {
        this(txId,collection,ids.toString());
    }
}
