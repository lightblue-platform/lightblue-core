package com.redhat.lightblue.hooks;

public interface HookPostParseListener {

    void fire(CRUDHook hook);

}
