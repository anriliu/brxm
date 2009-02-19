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
package org.hippoecm.hst.core.request;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.hippoecm.hst.configuration.sitemap.HstSiteMapItem;
import org.hippoecm.hst.core.component.HstURL;

/**
 * HstRequestContext provides repository content context
 * and page/components configuration context.
 * 
 * @version $Id$
 */
public interface HstRequestContext {
    
    /**
     * Returns a session which is normally retrieved from a session pooling repository.
     * 
     * @return a session, which is normally retrieved from a session pooling repository
     * @throws LoginException
     * @throws RepositoryException
     */
    public Session getSession() throws LoginException, RepositoryException;
    
    public Credentials getDefaultCredentials();
    
    public HstSiteMapItem getSiteMapItem();
    
    public String getContextNamespace();
    
    public HstURL createURL(String type, String parameterNamespace);
    
}
