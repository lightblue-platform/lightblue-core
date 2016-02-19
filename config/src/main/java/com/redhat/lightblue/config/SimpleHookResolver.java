package com.redhat.lightblue.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redhat.lightblue.hooks.CRUDHook;
import com.redhat.lightblue.hooks.HookResolver;
import com.redhat.lightblue.metadata.parser.HookConfigurationParser;

/**
 * Created by lcestari on 4/17/15.
 */
public class SimpleHookResolver implements HookResolver {

    private static final long serialVersionUID = 4033911991277254400L;

    private final Map<String, CRUDHook> map = new HashMap<>();

    public SimpleHookResolver(List<HookConfigurationParser> hookConfigurationParsers, LightblueFactory lightblueFactory) {
        if (hookConfigurationParsers != null && !hookConfigurationParsers.isEmpty()) {
            for (HookConfigurationParser parser : hookConfigurationParsers) {
                lightblueFactory.injectDependencies(parser);
                CRUDHook hook = parser.getCRUDHook();
                lightblueFactory.injectDependencies(hook);
                map.put(parser.getName(), hook);
            }
        }
    }

    @Override
    public CRUDHook getHook(String name) {
        return map.get(name);
    }
}
