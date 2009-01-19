/*
 *  Copyright 2008 Hippo.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hippoecm.frontend.plugin.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.wicket.IClusterable;
import org.hippoecm.frontend.service.ServiceTracker;

public class ServiceForwarder extends ServiceTracker<IClusterable> {
    private static final long serialVersionUID = 1L;

    private static final ThreadLocal<Set<StackEntry>> threadLocal = new ThreadLocal<Set<StackEntry>>();

    private static class StackEntry {
        String name;
        IClusterable service;

        StackEntry(String name, IClusterable service) {
            this.name = name;
            this.service = service;
        }

        public boolean equals(Object that) {
            if (!(that instanceof StackEntry)) {
                return false;
            }
            StackEntry seThat = (StackEntry) that;
            return new EqualsBuilder().append(seThat.name, this.name).append(seThat.service, this.service).isEquals();
        }

        public int hashCode() {
            return (service.hashCode() << 4) + name.hashCode();
        }
    }

    private PluginManager pluginMgr;
    private String source;
    private String target;

    ServiceForwarder(PluginManager mgr, Class<?> clazz, String source, String target) {
        super(clazz);

        this.pluginMgr = mgr;
        this.source = source;
        this.target = target;
    }

    public void start() {
        pluginMgr.registerTracker(this, source);
    }

    public void stop() {
        pluginMgr.unregisterTracker(this, source);
    }

    @Override
    protected void onServiceAdded(IClusterable service, String name) {
        Set<StackEntry> stack = threadLocal.get();
        if (stack == null) {
            threadLocal.set(stack = new HashSet<StackEntry>());
        }
        // detect recursion; forwarded services may be forwarded yet again,
        // but shouldn't be registered twice under the same name
        StackEntry targetEntry = new StackEntry(target, service);
        if (!stack.contains(targetEntry)) {
            StackEntry sourceEntry = new StackEntry(name, service);
            stack.add(sourceEntry);
            pluginMgr.registerService(service, target);
            stack.remove(sourceEntry);
        }
        if (stack.size() == 0) {
            threadLocal.remove();
        }
    }

    @Override
    protected void onRemoveService(IClusterable service, String name) {
        Set<StackEntry> stack = threadLocal.get();
        if (stack == null) {
            threadLocal.set(stack = new HashSet<StackEntry>());
        }
        StackEntry targetEntry = new StackEntry(target, service);
        if (!stack.contains(targetEntry)) {
            StackEntry sourceEntry = new StackEntry(name, service);
            stack.add(sourceEntry);
            pluginMgr.unregisterService(service, target);
            stack.remove(sourceEntry);
        }
        if (stack.size() == 0) {
            threadLocal.remove();
        }
    }

}
