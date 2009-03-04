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
package org.hippoecm.hst.core.jcr;

import java.util.List;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventListenersContainerImpl implements EventListenersContainer {
    
    static Logger log = LoggerFactory.getLogger(EventListenersContainerImpl.class);
    
    protected Repository repository;
    protected Credentials credentials;
    protected Session session;
    protected boolean sessionLiveCheck;
    protected long sessionLiveCheckInterval = 60000L;
    protected Workspace workspace;
    protected ObservationManager observationManager;
    protected List<EventListenerItem> eventListenerItems;
    
    protected boolean initialized;
    
    public void setRepository(Repository repository) {
        this.repository = repository;
    }
    
    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }
    
    public List<EventListenerItem> getEventListenerItems() {
        return this.eventListenerItems;
    }

    public void setEventListenerItems(List<EventListenerItem> eventListenerItems) {
        this.eventListenerItems = eventListenerItems;
    }
    
    public void setSessionLiveCheck(boolean sessionLiveCheck) {
        this.sessionLiveCheck = sessionLiveCheck;
    }
    
    public void setSessionLiveCheckInterval(long sessionLiveCheckInterval) {
        this.sessionLiveCheckInterval = sessionLiveCheckInterval;
    }

    public void start() {
        if (!this.sessionLiveCheck) {
            doInit();
        } else {
            final Thread sessionCheckerThread = new Thread("EventListenersContainerSessionChecker") {
                public void run() {
                    while (true) {
                        if (session == null || !session.isLive()) {
                            doInit();
                        }
                        
                        synchronized (this) {
                            try {
                                wait(sessionLiveCheckInterval);
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                }
            };
            
            sessionCheckerThread.start();
        }
    }
    
    protected void doInit() {
        if (log.isDebugEnabled()) log.debug("EventListenersContainer will initialize itself.");
        
        stop();
        
        try {
            this.session = this.repository.login();
            this.workspace = this.session.getWorkspace();
            this.observationManager = this.workspace.getObservationManager();
            
            for (EventListenerItem item : this.eventListenerItems) {
                
                EventListener eventListener = item.getEventListener();
                int eventTypes = item.getEventTypes();
                String absolutePath = item.getAbsolutePath();
                boolean isDeep = item.isDeep();
                String [] uuids = item.getUuids();
                String [] nodeTypeNames = item.getNodeTypeNames();
                boolean noLocal = item.isNoLocal();
                
                if (eventListener == null) {
                    if (log.isWarnEnabled()) log.warn("event listener object is null. Just ignored.");
                    continue;
                }
                
                if (eventTypes <= 0) {
                    if (log.isWarnEnabled()) log.warn("event listener's event types is invalid: {}. Just ignored.", eventTypes);
                    continue;
                }
                
                this.observationManager.addEventListener(eventListener, eventTypes, absolutePath, isDeep, uuids, nodeTypeNames, noLocal);
                
                if (log.isDebugEnabled()) {
                    log.debug("event listener registered: listener=" + eventListener + ", eventTypes=" + eventTypes + 
                            ", absolutePath=" + absolutePath + ", isDeep=" + isDeep + ", uuids=" + uuids + 
                            ", nodeTypeNames=" + nodeTypeNames + ", noLocal=" + noLocal);
                }
            }
            
            this.initialized = true;

            if (log.isDebugEnabled()) log.debug("EventListenersContainer's initialization done.");
        } catch (LoginException e) {
            if (log.isDebugEnabled()) {
                log.warn("Cannot register event listeners because of failure to log on to the repository: {}", e.getMessage(), e);
            } else if (log.isWarnEnabled()) {
                log.warn("Cannot register event listeners because of failure to log on to the repository: {}", e.getMessage());
            }
        } catch (RepositoryException e) {
            if (log.isDebugEnabled()) {
                log.warn("Cannot register event listeners because the repository is not available: {}", e.getMessage(), e);
            } else if (log.isWarnEnabled()) {
                log.warn("Cannot register event listeners because the repository is not available: {}", e.getMessage());
            }
        }
    }

    public void stop() {
        if (this.observationManager != null && this.eventListenerItems != null) {
            for (EventListenerItem item : this.eventListenerItems) {
                try {
                    this.observationManager.removeEventListener(item.getEventListener());
                } catch (Exception e) {
                    if (log.isWarnEnabled()) {
                        log.warn("Cannot remove event listener. {}", e.getMessage());
                    }
                }
            }
        }
        
        if (this.session != null) {
            try {
                this.session.logout();
            } catch (Exception ce) {
            }
        }
        
        this.observationManager = null;
        this.workspace = null;
        this.session = null;
    }

}
