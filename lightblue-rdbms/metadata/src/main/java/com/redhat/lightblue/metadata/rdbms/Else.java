package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.parser.MetadataParser;

public class Else extends Then{
    @Override
    public String getName(){
        return "$else";
    }
}
