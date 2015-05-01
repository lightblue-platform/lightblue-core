package com.redhat.lightblue.config.hooks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redhat.lightblue.config.LightblueFactory;
import com.redhat.lightblue.config.LightblueFactoryAware;
import com.redhat.lightblue.hooks.CRUDHook;
import com.redhat.lightblue.hooks.HookResolver;
import com.redhat.lightblue.metadata.parser.HookConfigurationParser;

/**
 * Created by lcestari on 4/17/15.
 */
public class SimpleHookResolver implements HookResolver, LightblueFactoryAware {

    private static final long serialVersionUID = 4033911991277254400L;

    private final Map<String, CRUDHook> map = new HashMap<>();

    private LightblueFactory lightblueFactory;

    public SimpleHookResolver(List<HookConfigurationParser> hookConfigurationParsers) {
        if (hookConfigurationParsers != null && !hookConfigurationParsers.isEmpty()) {
            for (HookConfigurationParser parser : hookConfigurationParsers) {
                CRUDHook hook = parser.getCRUDHook();
                if (hook instanceof LightblueFactoryAware) {
                    ((LightblueFactoryAware) hook).setLightblueFactory(lightblueFactory);
                }
                map.put(parser.getName(), hook);
            }
        }
    }

    @Override
    public CRUDHook getHook(String name) {
        return map.get(name);
    }

    @Override
    public void setLightblueFactory(LightblueFactory lightblueFactory) {
        this.lightblueFactory = lightblueFactory;
    }
}
