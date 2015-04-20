package com.redhat.lightblue.hooks;

import com.redhat.lightblue.metadata.parser.HookConfigurationParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lcestari on 4/17/15.
 */
public class SimpleHookResolver implements HookResolver {
    private Map<String, CRUDHook> map = new HashMap<>();

    public SimpleHookResolver(List<HookConfigurationParser> hookConfigurationParsers) {
        if(hookConfigurationParsers != null && !hookConfigurationParsers.isEmpty()) {
            for (HookConfigurationParser parser : hookConfigurationParsers) {
                map.put(parser.getName(), parser.getCRUDHook());
            }
        }
    }

    @Override
    public CRUDHook getHook(String name) {
        return map.get(name);
    }
}
