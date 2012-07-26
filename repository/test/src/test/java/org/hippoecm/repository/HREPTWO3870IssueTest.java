/*
 *  Copyright 2010 Hippo.
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

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.hippoecm.repository.api.HippoSession;
import org.hippoecm.repository.api.ImportMergeBehavior;
import org.hippoecm.repository.api.ImportReferenceBehavior;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Add a facetsearch, remove the facetsearch and re-add the facetsearch.
 * When the test fails (add the issue is resolved):
 * - the test should be renamed to HREPTWO3870Test
 * - https://issues.onehippo.com/browse/HREPTWO-3870 should be closed.
 */
public class HREPTWO3870IssueTest extends TestCase {

    private final Logger log = LoggerFactory.getLogger(HREPTWO3870IssueTest.class);

    private static final String tags = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<sv:node xmlns:sv=\"http://www.jcp.org/jcr/sv/1.0\" sv:name=\"tags\">"
            + "  <sv:property sv:name=\"jcr:uuid\" sv:type=\"Name\">"
            + "    <sv:value>2c21a29c-aaaa-bbbb-cccc-594e5bf93b25</sv:value>" + "  </sv:property>"
            + "  <sv:property sv:name=\"jcr:primaryType\" sv:type=\"Name\">"
            + "    <sv:value>hippo:facetsearch</sv:value>" + "  </sv:property>"
            + "  <sv:property sv:name=\"hippo:docbase\" sv:type=\"String\">"
            + "    <sv:value>2c21a29c-a5d1-4e84-aec4-594e5bf93b25</sv:value>" + "  </sv:property>"
            + "  <sv:property sv:name=\"hippo:facets\" sv:type=\"String\">" + "    <sv:value>nothing</sv:value>"
            + "  </sv:property>" + "  <sv:property sv:name=\"hippo:queryname\" sv:type=\"String\">"
            + "    <sv:value>tagsquery</sv:value>" + "  </sv:property>" + "</sv:node>";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp(true);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void removeAndAddFacetsearch() throws RepositoryException, IOException {
        // create test node
        Node root = session.getRootNode();
        Node test = root.addNode("test");
        session.save();

        // create facetsearch
        InputStream in = new ByteArrayInputStream(tags.getBytes());
        ((HippoSession) session).importDereferencedXML(test.getPath(), in,
                ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW,
                ImportReferenceBehavior.IMPORT_REFERENCE_NOT_FOUND_REMOVE, ImportMergeBehavior.IMPORT_MERGE_THROW);
        session.save();

        // test integrity
        Node facetsearch = session.getRootNode().getNode("test").getNode("tags");
        facetsearch.getPath();
        NodeIterator iter = facetsearch.getNodes();
        while (iter.hasNext()) {
            iter.nextNode().getPath();
        }

        // remove facetsearch
        facetsearch.remove();
        session.save();

        // re-create facetsearch
        in = new ByteArrayInputStream(tags.getBytes());
        ((HippoSession) session).importDereferencedXML(test.getPath(), in,
                ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW,
                ImportReferenceBehavior.IMPORT_REFERENCE_NOT_FOUND_REMOVE, ImportMergeBehavior.IMPORT_MERGE_THROW);

        try {
            session.save();

            // test integrity
            facetsearch = session.getRootNode().getNode("test").getNode("tags");
            facetsearch.getPath();
            iter = facetsearch.getNodes();
            while (iter.hasNext()) {
                iter.nextNode().getPath();
            }
            fail("Please resolve HREPTWO-3870");
        } catch (RepositoryException e) {
            log.warn("Issue HREPTWO-3870 is not yet solved, error: {}: {}", e.getClass().getName(), e.getMessage());
        }
    }
}
