package com.redhat.lightblue.hooks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redhat.lightblue.metadata.parser.HookConfigurationParser;

/**
 * Created by lcestari on 4/17/15.
 */
public class SimpleHookResolver implements HookResolver {

    private static final long serialVersionUID = 4033911991277254400L;

    private final Map<String, CRUDHook> map = new HashMap<>();

    public SimpleHookResolver(List<HookConfigurationParser> hookConfigurationParsers) {
        this(hookConfigurationParsers, null);
    }

    public SimpleHookResolver(List<HookConfigurationParser> hookConfigurationParsers, List<HookPostParseListener> listeners) {
        if (hookConfigurationParsers != null && !hookConfigurationParsers.isEmpty()) {
            for (HookConfigurationParser parser : hookConfigurationParsers) {
                CRUDHook hook = parser.getCRUDHook();
                if (listeners != null) {
                    for (HookPostParseListener listener : listeners) {
                        listener.fire(hook);
                    }
                }
                map.put(parser.getName(), hook);
            }
        }
    }

    @Override
    public CRUDHook getHook(String name) {
        return map.get(name);
    }
}
