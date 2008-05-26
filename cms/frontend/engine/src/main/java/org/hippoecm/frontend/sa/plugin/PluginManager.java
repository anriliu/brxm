/*
 * Copyright 2008 Hippo
 *
 * Licensed under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hippoecm.frontend.sa.plugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.IClusterable;
import org.hippoecm.frontend.sa.Home;
import org.hippoecm.frontend.sa.plugin.config.IPluginConfig;
import org.hippoecm.frontend.sa.plugin.impl.PluginContext;
import org.hippoecm.frontend.sa.service.IServiceListener;
import org.hippoecm.frontend.sa.service.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginManager implements IClusterable {
    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(PluginManager.class);

    private static class RefCount implements IClusterable {
        private static final long serialVersionUID = 1L;

        IClusterable service;
        int count;

        RefCount(IClusterable service) {
            this.service = service;
            count = 1;
        }

        void addRef() {
            count++;
        }

        boolean release() {
            return (--count == 0);
        }
    }

    private Home page;
    private PluginFactory factory;
    private Map<String, List<IClusterable>> services;
    private Map<String, List<IServiceListener>> listeners;
    private Map<Integer, RefCount> referenced;
    private int nextReferenceId;

    public PluginManager(Home page) {
        this.page = page;
        factory = new PluginFactory();
        services = new HashMap<String, List<IClusterable>>();
        listeners = new HashMap<String, List<IServiceListener>>();
        referenced = new HashMap<Integer, RefCount>();
        nextReferenceId = 0;
    }

    public IPlugin start(IPluginConfig config) {
        IPlugin plugin = factory.createPlugin(config);
        if (plugin != null) {
            PluginContext context = new PluginContext(page, config);
            plugin.start(context);
        }
        return plugin;
    }

    public <T extends IClusterable> ServiceReference<T> getReference(T service) {
        ServiceReference<T> ref = internalGetReference(service);
        if (ref == null) {
            log.warn("Referenced service was not registered");
        }
        return ref;
    }

    public <T extends IClusterable> T getService(ServiceReference<T> reference) {
        RefCount refCount = referenced.get(new Integer(reference.getId()));
        if (refCount == null) {
            log.warn("Referenced service is no longer registered");
        }
        return (T) refCount.service;
    }

    public void registerService(IClusterable service, String name) {
        if (name == null) {
            log.error("service name is null");
            return;
        } else {
            log.info("registering " + service + " as " + name);
        }

        List<IClusterable> list = services.get(name);
        if (list == null) {
            list = new LinkedList<IClusterable>();
            services.put(name, list);
        }
        list.add(service);

        ServiceReference<IClusterable> ref = internalGetReference(service);
        if (ref == null) {
            referenced.put(new Integer(nextReferenceId++), new RefCount(service));
        } else {
            referenced.get(new Integer(ref.getId())).addRef();
        }

        List<IServiceListener> notify = listeners.get(name);
        if (notify != null) {
            Iterator<IServiceListener> iter = notify.iterator();
            while (iter.hasNext()) {
                IServiceListener entry = iter.next();
                entry.processEvent(IServiceListener.ADDED, name, service);
            }
        }
    }

    public void unregisterService(IClusterable service, String name) {
        if (name == null) {
            log.error("service name is null");
            return;
        } else {
            log.info("unregistering " + service + " from " + name);
        }

        List<IClusterable> list = services.get(name);
        if (list != null) {
            List<IServiceListener> notify = listeners.get(name);
            if (notify != null) {
                Iterator<IServiceListener> iter = notify.iterator();
                while (iter.hasNext()) {
                    IServiceListener entry = iter.next();
                    entry.processEvent(IServiceListener.REMOVE, name, service);
                }
            }

            list.remove(service);
            if (list.isEmpty()) {
                services.remove(name);
            }

            ServiceReference<IClusterable> ref = internalGetReference(service);
            RefCount refCount = referenced.get(new Integer(ref.getId()));
            if (refCount.release()) {
                referenced.remove(new Integer(ref.getId()));
            }
        } else {
            log.error("unregistering a service that wasn't registered.");
        }
    }

    public void registerListener(IServiceListener listener, String name) {
        if (name == null) {
            log.error("listener name is null");
            return;
        } else {
            log.info("registering listener " + listener + " for " + name);
        }

        List<IServiceListener> list = listeners.get(name);
        if (list == null) {
            list = new LinkedList<IServiceListener>();
            listeners.put(name, list);
        }
        list.add(listener);

        List<IClusterable> notify = services.get(name);
        if (notify != null) {
            Iterator<IClusterable> iter = notify.iterator();
            while (iter.hasNext()) {
                IClusterable service = iter.next();
                listener.processEvent(IServiceListener.ADDED, name, service);
            }
        }
    }

    public void unregisterListener(IServiceListener listener, String name) {
        if (name == null) {
            log.error("listener name is null");
            return;
        } else {
            log.info("unregistering listener " + listener + " for " + name);
        }

        List<IServiceListener> list = listeners.get(name);
        if (list != null) {
            if (list.contains(listener)) {
                list.remove(listener);
            }
            if (list.isEmpty()) {
                listeners.remove(name);
            }
        } else {
            log.error("unregistering a listener that wasn't registered.");
        }
    }

    private <T extends IClusterable> ServiceReference<T> internalGetReference(T service) {
        for (Map.Entry<Integer, RefCount> entry : referenced.entrySet()) {
            if (entry.getValue().service == service) {
                return new ServiceReference<T>(page, entry.getKey().intValue());
            }
        }
        return null;
    }
}
