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
package org.hippoecm.hst.core.configuration;

import static org.junit.Assert.assertTrue;

import org.hippoecm.hst.configuration.HstSite;
import org.hippoecm.hst.configuration.HstSitesManager;
import org.hippoecm.hst.core.request.HstSiteMapMatcher;
import org.hippoecm.hst.core.request.ResolvedSiteMapItem;
import org.hippoecm.hst.site.request.BasicHstSiteMapMatcher;
import org.hippoecm.hst.test.AbstractSpringTestCase;
import org.junit.Test;

public class TestConfiguration extends AbstractSpringTestCase {

    protected static final String TESTPROJECT_NAME = "testproject";

    private HstSitesManager hstSitesManager;
    private HstSiteMapMatcher hstSiteMapMatcher;
    private HstSite hstSite;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.hstSitesManager = getComponent(HstSitesManager.class.getName());
        hstSiteMapMatcher = new BasicHstSiteMapMatcher();
        hstSite = this.hstSitesManager.getSites().getSite(TESTPROJECT_NAME);
    }

    /**
     * The hst:sitemap structure we expect the unit test to look like is as follows:
     * 
     * news
     *    `-*
     *      |- january  
     *      `- *
     *         |- *
     *         `- **
     */
    
    @Test
    public void testUrl1(){
        
        ResolvedSiteMapItem res = hstSiteMapMatcher.match("news/2009", hstSite);
        assertTrue("Relative content path for 'news/2009' must be to be 'News/2009'", "News/2009".equals(res.getRelativeContentPath()));
        
    }
    
    @Test
    public void testUrl2(){
        
        ResolvedSiteMapItem res = hstSiteMapMatcher.match("news/2009/february/myArticle", hstSite);
        assertTrue("Relative content path for 'news/2009/february/myArticle' must be to be 'News/2009/february/myArticle'", "News/2009/february/myArticle".equals(res.getRelativeContentPath()));
       
    }
    
    @Test
    public void testUrl3_matchANY(){
        
        ResolvedSiteMapItem res = hstSiteMapMatcher.match("news/2009/february/day2/8oclock/16min/4sec/MyArticle", hstSite);
        assertTrue("Relative content path for 'news/2009/february/day2/8oclock/16min/4sec/MyArticle' must be to be 'News/2009/february/day2/8oclock/16min/4sec/MyArticle'", "News/2009/february/day2/8oclock/16min/4sec/MyArticle".equals(res.getRelativeContentPath()));
      
    }
    
   

}
