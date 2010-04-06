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
package org.hippoecm.hst.core.jcr.pool;

import javax.jcr.Credentials;
import javax.jcr.Session;

import org.apache.commons.proxy.Interceptor;
import org.apache.commons.proxy.Invocation;
import org.hippoecm.hst.core.ResourceLifecycleManagement;
import org.hippoecm.hst.core.jcr.pool.util.ProxyFactory;

public class PooledSessionDecoratorProxyFactoryImpl implements SessionDecorator, PoolingRepositoryAware {
    
    protected PoolingRepository poolingRepository;
    protected boolean manageLifecycleOnImpersonatedSession = true;

    public PooledSessionDecoratorProxyFactoryImpl() {
    }

    public final Session decorate(Session session) {
        return decorate(session, null);
    }
    
    public final Session decorate(Session session, String userID) {
        PooledSession pooledSessionProxy = null;
        ProxyFactory factory = new ProxyFactory();
        PooledSessionInterceptor interceptor = new PooledSessionInterceptor(userID);
        
        ClassLoader sessionClassloader = session.getClass().getClassLoader();
        ClassLoader currentClassloader = Thread.currentThread().getContextClassLoader();
        
        try {
            if (sessionClassloader != currentClassloader) {
                Thread.currentThread().setContextClassLoader(sessionClassloader);
            }
            
            pooledSessionProxy = (PooledSession) factory.createInterceptorProxy(session.getClass().getClassLoader(), session, interceptor, new Class [] { PooledSession.class });
            interceptor.setPooledSessionProxy(pooledSessionProxy);
        } finally {
            if (sessionClassloader != currentClassloader) {
                Thread.currentThread().setContextClassLoader(currentClassloader);
            }
        }
        
        return pooledSessionProxy;
    }

    public void setPoolingRepository(PoolingRepository poolingRepository) {
        this.poolingRepository = poolingRepository;
    }
    
    public void setManageLifecycleOnImpersonatedSession(boolean manageLifecycleOnImpersonatedSession) {
        this.manageLifecycleOnImpersonatedSession = manageLifecycleOnImpersonatedSession;
    }
    
    protected class PooledSessionInterceptor implements Interceptor {
        
        private boolean passivated;
        private long lastRefreshed;
        private PooledSession pooledSessionProxy;
        private String userID;
        
        public PooledSessionInterceptor() {
            this(null);
        }
        
        public PooledSessionInterceptor(String userID) {
            this.userID = userID;
        }
        
        public Object intercept(Invocation invocation) throws Throwable {
            Object ret = null;
            
            String methodName = invocation.getMethod().getName();
            
            if ("activate".equals(methodName)) {
                this.passivated = false;
            } else if ("passivate".equals(methodName)) {
                this.passivated = true;
            } else if ("logoutSession".equals(methodName)) {
                Session session = (Session) invocation.getProxy();
                // temporary, in fact unneeded fix, to reduce memory kept by repo. This is fixed in repo trunk already, not in the ecm 2.06.07 and 2.07.00 versions
                session.refresh(false);
                session.logout();
            } else if ("lastRefreshed".equals(methodName)) {
                ret = new Long(lastRefreshed);
            } else if ("getUserID".equals(methodName)) {
                if (userID != null) {
                    ret = userID;
                } else {
                    ret = invocation.proceed();
                }
            } else if ("refresh".equals(methodName)) {
                ret = invocation.proceed();
                lastRefreshed = System.currentTimeMillis();
            } else if ("toString".equals(methodName)) {
                ret = super.toString() + " (" + invocation.proceed().toString() + ")";
            } else if ("hashCode".equals(methodName)) {
                ret = invocation.proceed();
            } else {
                if (this.passivated) {
                    throw new IllegalStateException("Invalid session which is already returned to the pool!");
                } else {
                    if ("logout".equals(methodName)) {
                        /*
                         * when logout(), it actually returns the session to the pool.
                         * If this session is already returned, do nothing. 
                         * Sessions can be cleaned up (returned to the pool) by the PooledSessionDecoratorFactory#disposeAllResources() 
                         * at the end of the request.
                         * A call to poolingRepository.returnSession(pooledSessionProxy); would end up in a double call in 
                         * GenericObjectPool returnObject(session) for one and the same session, resulting in negative numActives and 
                         * a broken pool, retaining in the end far more open sessions then configured in the pool config
                         */ 
                        
                         poolingRepository.returnSession(pooledSessionProxy);
                        
                    } else if ("getRepository".equals(methodName)) {
                        // when getRepository(), it actually returns the session pooling repository
                        ret = poolingRepository;
                    } else if ("impersonate".equals(methodName)) {
                        // when impersonate(), it actually returns a session which is borrowed 
                        // from another session pool repository based on the credentials.
                        Credentials credentials = (Credentials) invocation.getArguments()[0];
                        ret = poolingRepository.impersonate(credentials);
                        
                        // if the poolingRepository returns null, it means the poolingRepository cannot retrieve
                        // a pooled session with the credentials.
                        // in this case, just proceeed to allow impersonation by underlying JCR session.
                        if (ret == null) {
                            ret = invocation.proceed();
                            
                            if (manageLifecycleOnImpersonatedSession && ret != null) {
                                ResourceLifecycleManagement pooledSessionLifecycleManagement = poolingRepository.getResourceLifecycleManagement();
                                if (pooledSessionLifecycleManagement != null && pooledSessionLifecycleManagement.isActive()) {
                                    pooledSessionLifecycleManagement.registerResource(ret);
                                }
                            }
                        }
                    } else {
                        ret = invocation.proceed();
                    }
                }
            }

            return ret;
        }
        
        public void setPooledSessionProxy(PooledSession pooledSessionProxy) {
            this.pooledSessionProxy = pooledSessionProxy;
        }
    }

}
