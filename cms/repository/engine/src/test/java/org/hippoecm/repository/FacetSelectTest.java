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
package org.hippoecm.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.junit.After;
import org.junit.Test;

public class FacetSelectTest extends TestCase {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    String[] content = new String[] {
        "/test",              "nt:unstructured",
        "/test/docs",         "hippo:testdocument",
        "jcr:mixinTypes",     "hippo:harddocument",
        "/test/docs/doc",     "hippo:handle",
        "jcr:mixinTypes",     "hippo:hardhandle",
        "/test/docs/doc/doc", "hippo:document",
        "jcr:mixinTypes",     "hippo:harddocument",
        "/test/mirror",       "hippo:facetselect",
        "hippo:docbase",      "/test/docs",
        "hippo:values",       null,
        "hippo:facets",       null,
        "hippo:modes",        null,
        "/test/spiegel",      "hippo:testmirror",
        "hippo:docbase",      "/test/docs",
        "hippo:values",       null,
        "hippo:facets",       null,
        "hippo:modes",        null,
        "/test/notallowed",   "hippo:facetselect",
        "hippo:docbase",      "/",
        "hippo:values",       null,
        "hippo:facets",       null,
        "hippo:modes",        null
    };

    String[] combineContent1 = new String[] {
        "/test",              "nt:unstructured",
        "jcr:mixinTypes",     "mix:referenceable",
        "/test/docs",         "hippo:testdocument",
        "jcr:mixinTypes",     "hippo:harddocument",
        "/test/docs/one",     "hippo:handle",
        "jcr:mixinTypes",     "hippo:hardhandle",
        "/test/docs/one/one", "hippo:testdocument",
        "jcr:mixinTypes",     "hippo:harddocument",
        "vehicle", "car",
        "color", "red",
        "/test/docs/two",     "hippo:handle",
        "jcr:mixinTypes",     "hippo:hardhandle",
        "/test/docs/two/two", "hippo:testdocument",
        "jcr:mixinTypes",     "hippo:harddocument",
        "vehicle", "car",
        "color", "blue",
        "/test/docs/three",     "hippo:handle",
        "jcr:mixinTypes",     "hippo:hardhandle",
        "/test/docs/three/three", "hippo:testdocument",
        "jcr:mixinTypes",     "hippo:harddocument",
        "vehicle", "bike",
        "color", "red",
        "/test/docs/four",     "hippo:handle",
        "jcr:mixinTypes",     "hippo:hardhandle",
        "/test/docs/four/four", "hippo:testdocument",
        "jcr:mixinTypes",     "hippo:harddocument",
        "vehicle", "bike",
        "color", "blue"
    };
    String[] combineContent2 = new String[] {
        "/test/filter1",      "hippo:facetselect",
        "jcr:mixinTypes",     "mix:referenceable",
        "hippo:docbase",      "/test/docs",
        "hippo:facets",       "vehicle",
        "hippo:values",       "car",
        "hippo:modes",        "select"
    };
    String[] combineContentFilterPointsToFilter = new String[] {
        "/test/filterTofilter",      "hippo:facetselect",
        "hippo:docbase",      "/test/filter1",
        "hippo:facets",       "color",
        "hippo:values",       "red",
        "hippo:modes",        "select"
    };
    String[] combineContentFilterPointsToParentOfFilter = new String[] {
        "/test/filterToParentOfFilter",      "hippo:facetselect",
        "hippo:docbase",      "/test",
        "hippo:facets",       "color",
        "hippo:values",       "red",
        "hippo:modes",        "select"
    };

    @After
    @Override
    public void tearDown() throws Exception {
        if(session.getRootNode().hasNode("test")) {
	    session.getRootNode().getNode("test").remove();
	}
	session.save();
	super.tearDown();
    }

    @Test
    public void testRequestsLast() throws Exception {
        final String[] data = new String[] {
            "/test", "nt:unstructured",
            "/test/docs", "hippo:testdocument",
            "jcr:mixinTypes", "hippo:harddocument",
            "/test/docs/doc", "hippo:handle",
            "jcr:mixinTypes", "hippo:hardhandle",
            "/test/docs/doc/hippo:request", "hipposys:request",
            "/test/docs/doc/doc", "hippo:testdocument",
            "jcr:mixinTypes", "hippo:harddocument",
            "state", "published",
            "/test/test1", "hippo:facetselect",
            "hippo:docbase", "/test/docs",
            "hippo:facets", "state",
            "hippo:values", "published",
            "hippo:modes", "single",
            "/test/testa", "hippo:facetselect",
            "hippo:docbase", "/test/docs/doc",
            "hippo:facets", "state",
            "hippo:values", "published",
            "hippo:modes", "single",
            "/test/test2", "hippo:facetselect",
            "hippo:docbase", "/test/docs",
            "hippo:facets", "state",
            "hippo:values", "published",
            "hippo:modes", "prefer-single",
            "/test/testb", "hippo:facetselect",
            "hippo:docbase", "/test/docs/doc",
            "hippo:facets", "state",
            "hippo:values", "published",
            "hippo:modes", "prefer-single",
            "/test/test3", "hippo:facetselect",
            "hippo:docbase", "/test/docs",
            "hippo:facets", "state",
            "hippo:values", "published",
            "hippo:modes", "prefer",
            "/test/testc", "hippo:facetselect",
            "hippo:docbase", "/test/docs/doc",
            "hippo:facets", "state",
            "hippo:values", "published",
            "hippo:modes", "prefer",
            "/test/test4", "hippo:facetselect",
            "hippo:docbase", "/test/docs",
            "hippo:facets", "state",
            "hippo:values", "published",
            "hippo:modes", "select",
            "/test/testd", "hippo:facetselect",
            "hippo:docbase", "/test/docs/doc",
            "hippo:facets", "state",
            "hippo:values", "published",
            "hippo:modes", "select",
            "/test/test5", "hippo:facetselect",
            "hippo:docbase", "/test/docs",
            "hippo:facets", null,
            "hippo:values", null,
            "hippo:modes", null,
            "/test/teste", "hippo:facetselect",
            "hippo:docbase", "/test/docs/doc",
            "hippo:facets", null,
            "hippo:values", null,
            "hippo:modes", null,
            "/test/test6", "hippo:mirror",
            "hippo:docbase", "/test/docs",
            "/test/testf", "hippo:mirror",
            "hippo:docbase", "/test/docs/doc"
        };
        build(session, data);
        session.save();
        session.refresh(false);
        assertEquals("doc",traverse(session, "/test/test1/doc").getNodes().nextNode().getName());
        assertEquals("doc",traverse(session, "/test/testa").getNodes().nextNode().getName());
        assertEquals("doc",traverse(session, "/test/test2/doc").getNodes().nextNode().getName());
        assertEquals("doc",traverse(session, "/test/testb").getNodes().nextNode().getName());
        assertEquals("doc",traverse(session, "/test/test3/doc").getNodes().nextNode().getName());
        assertEquals("doc",traverse(session, "/test/testc").getNodes().nextNode().getName());
        assertEquals("doc",traverse(session, "/test/test4/doc").getNodes().nextNode().getName());
        //assertEquals("doc",traverse(session, "/test/testd").getNodes().nextNode().getName());
        assertEquals("doc",traverse(session, "/test/test5/doc").getNodes().nextNode().getName());
        //assertEquals("doc",traverse(session, "/test/teste").getNodes().nextNode().getName());
        assertEquals("doc",traverse(session, "/test/test6/doc").getNodes().nextNode().getName());
        //assertEquals("doc",traverse(session, "/test/testf").getNodes().nextNode().getName());
    }

    @Test
    public void testBasics() throws Exception {
        build(session, content);
        session.save();
        session.refresh(false);
        assertNotNull(traverse(session, "/test/mirror/doc/doc"));
    }

    @Test
    public void testSubtyped() throws Exception {
        build(session, content);
        session.save();
        session.refresh(false);
        assertNotNull(traverse(session, "/test/spiegel/doc/doc"));
    }

    @Test
    public void testNoRoot() throws Exception {
        build(session, content);
        session.save();
        session.refresh(false);
        Node mirror = traverse(session, "/test/notallowed");
        assertNotNull(mirror);
        assertFalse(mirror.hasNodes());
        NodeIterator iter = mirror.getNodes();
        while(iter.hasNext()) {
            fail("no child nodes allowed");
        }
    }

    /*
     * This test is to make sure, that a facetselect/mirror directly pointing to another mirror/facetselect is not allowed
     * and will return an unpopulated facetselect/mirror with no children
     * @throws Exception
     */
    @Test
    public void testNotAllowedCombineDirect() throws Exception {
        build(session, combineContent1);
        session.save();
        build(session, combineContent2);
        session.save();
        build(session, combineContentFilterPointsToFilter);
        session.save();
        session.refresh(true);

        assertTrue(session.getRootNode().getNode("test").hasNode("filterTofilter"));
        assertFalse(session.getRootNode().getNode("test").getNode("filterTofilter").hasNodes());
       
    }

    @Test
    public void testCombineIndirect() throws Exception {
        build(session, combineContent1);
        session.save();
        build(session, combineContent2);
        session.save();
        build(session, combineContentFilterPointsToParentOfFilter);
        session.save();
        session.refresh(false);
        
        assertTrue(session.getRootNode().hasNode("test/filterToParentOfFilter/filter1/one/one"));
        assertFalse(session.getRootNode().hasNode("test/filterToParentOfFilter/filter1/three/three"));
        assertNotNull(traverse(session, "/test/filter1/two"));
        assertNotNull(traverse(session, "/test/filter1/two/two"));
        assertNotNull(traverse(session, "/test/filter1/three"));
        assertNull(traverse(session, "/test/filter1/three/three"));
        
        assertNotNull(traverse(session, "/test/filterToParentOfFilter/filter1/one/one"));
        assertNull(traverse(session, "/test/filterToParentOfFilter/filter1/two/two"));
        assertFalse(session.getRootNode().getNode("test/filterToParentOfFilter/filter1/two").getNodes().hasNext());
        assertFalse(session.getRootNode().getNode("test/filterToParentOfFilter/filter1/three").getNodes().hasNext());
        assertFalse(session.getRootNode().getNode("test/filterToParentOfFilter/filter1/four").getNodes().hasNext());
    }
}
