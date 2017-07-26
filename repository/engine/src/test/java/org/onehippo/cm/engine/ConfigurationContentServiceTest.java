/*
 *  Copyright 2017 Hippo B.V. (http://www.onehippo.com)
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
package org.onehippo.cm.engine;

import java.util.HashSet;
import java.util.List;

import javax.jcr.ItemExistsException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;
import org.onehippo.cm.model.definition.ActionType;
import org.onehippo.cm.model.impl.ConfigurationModelImpl;
import org.onehippo.cm.model.impl.GroupImpl;
import org.onehippo.cm.model.impl.ModuleImpl;
import org.onehippo.cm.model.impl.ProjectImpl;
import org.onehippo.cm.model.impl.definition.ContentDefinitionImpl;
import org.onehippo.cm.model.impl.exceptions.CircularDependencyException;
import org.onehippo.cm.model.impl.source.ContentSourceImpl;
import org.onehippo.cm.model.impl.tree.ConfigurationNodeImpl;
import org.onehippo.cm.model.impl.tree.DefinitionNodeImpl;
import org.onehippo.cm.model.tree.ConfigurationItemCategory;
import org.onehippo.testutils.log4j.Log4jInterceptor;

import com.google.common.collect.ImmutableList;

import junit.framework.AssertionFailedError;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ConfigurationContentServiceTest {

    private ConfigurationBaselineService baselineService;

    private JcrContentProcessor jcrContentProcessor;

    private ConfigurationContentService configurationContentService;

    @Before
    public void setUp() throws Exception {
        baselineService = createNiceMock(ConfigurationBaselineService.class);
        jcrContentProcessor = createNiceMock(JcrContentProcessor.class);
        configurationContentService = new ConfigurationContentService(baselineService, jcrContentProcessor);
    }

    @Test
    public void apply_failed_content() throws Exception {

        final Session session = createNiceMock(Session.class);

        final ConfigurationModelImpl model = createNiceMock(ConfigurationModelImpl.class);

        final ConfigurationNodeImpl configurationNode = new ConfigurationNodeImpl();
        configurationNode.setResidualNodeCategory(ConfigurationItemCategory.CONTENT);

        final ModuleImpl stubModule = new ModuleImpl("stubModule", new ProjectImpl("stubProject", new GroupImpl("stubGroup")));
        final DefinitionNodeImpl defNode4 = addContentDefinition(stubModule, "source4", "/some/sibling/path").getNode();
        final DefinitionNodeImpl defNode2 = addContentDefinition(stubModule, "source2", "/some/path/child").getNode();
        final DefinitionNodeImpl defNode1 = addContentDefinition(stubModule, "source1", "/some/path").getNode();
        final DefinitionNodeImpl defNode3 = addContentDefinition(stubModule, "source3", "/some/sibling").getNode();

        expect(model.getConfigurationRootNode()).andReturn(configurationNode).atLeastOnce();
        expect(model.getModules()).andReturn(ImmutableList.of(stubModule)).anyTimes();
        replay(model);

        expect(baselineService.getAppliedContentPaths(session)).andReturn(new HashSet<>()).times(4);
        baselineService.addAppliedContentPath(anyString(), anyObject());
        expectLastCall().times(2);
        expect(baselineService.contentNodeExists(session)).andReturn(true);
        replay(baselineService);

        expect(session.nodeExists("/content-upgrade-to-12-marker")).andReturn(false);
        replay(session);

        jcrContentProcessor.apply(defNode1, ActionType.APPEND, session, false);
        expectLastCall().andThrow(new ItemExistsException("This is an intentional Exception")).once();
        jcrContentProcessor.apply(defNode3, ActionType.APPEND, session, false);
        expectLastCall().once();
        jcrContentProcessor.apply(defNode4, ActionType.APPEND, session, false);
        expectLastCall().once();
        jcrContentProcessor.apply(defNode2, ActionType.APPEND, session, false);
        expectLastCall().andThrow(new AssertionFailedError("Should not be executed")).anyTimes();

        replay(jcrContentProcessor);

        try (Log4jInterceptor interceptor = Log4jInterceptor.onError().trap(ConfigurationContentService.class).build()) {
            configurationContentService.apply(model, session);
            assertTrue(interceptor.messages().anyMatch(m -> m.equals("Processing 'APPEND' action for node failed: '/some/path'")));
        }

        verify(baselineService);
        verify(jcrContentProcessor);
    }

    @Test
    public void testOrdering() {
        final ModuleImpl module = new ModuleImpl("stubModule", new ProjectImpl("stubProject", new GroupImpl("stubGroup")));

        final ContentDefinitionImpl cc1 = addContentDefinition(module, "rsource13", "/c/c1");
        cc1.getNode().setOrderBefore("a1");

        final ContentDefinitionImpl a1 = addContentDefinition(module, "rsource1", "/a1");
        final ContentDefinitionImpl a2 = addContentDefinition(module, "rsource2", "/a2");
        final ContentDefinitionImpl a3 = addContentDefinition(module, "rsource3", "/a3");
        a3.getNode().setOrderBefore("a2");
        a2.getNode().setOrderBefore("a1");

        addContentDefinition(module, "psource1", "/path/a1/b1/c1");
        addContentDefinition(module, "psource2", "/path/a1/b1/c2");
        addContentDefinition(module, "psource2", "/path/a1/b2/c1");

        final ContentDefinitionImpl pa1 = addContentDefinition(module, "source1", "/path/a1");
        final ContentDefinitionImpl pa2 = addContentDefinition(module, "source2", "/path/a2");
        final ContentDefinitionImpl pa3 = addContentDefinition(module, "source3", "/path/a3");
        final ContentDefinitionImpl pa4 = addContentDefinition(module, "source4", "/path/a4");

        final ContentDefinitionImpl a1b1 = addContentDefinition(module, "source11", "/path/a1/b1");
        final ContentDefinitionImpl a1b2 = addContentDefinition(module, "source12", "/path/a1/b2");
        a1b2.getNode().setOrderBefore("b1");
        pa2.getNode().setOrderBefore("a1");
        pa4.getNode().setOrderBefore("a3");

        final List<ContentDefinitionImpl> sortedDefinitions = configurationContentService.getSortedDefinitions(module.getContentDefinitions());

        assertAfter(sortedDefinitions, a3, a2);
        assertAfter(sortedDefinitions, a2, a1);
        assertAfter(sortedDefinitions, pa2, pa1);
        assertAfter(sortedDefinitions, a1b2, a1b1);
        assertAfter(sortedDefinitions, pa4, pa3);
    }

    /**
     * Verify that source definition's index is greater than the index of target definitions and thus,
     * order before rule can be applied to the source during content apply operation (By the time we process order before dependent
     * definition will exist in repository
     */
    private static void assertAfter(List<ContentDefinitionImpl> definitions, ContentDefinitionImpl source, ContentDefinitionImpl target) {
        assertTrue(definitions.indexOf(source) > definitions.indexOf(target));
    }

    @Test
    public void testComplexOrdering() {

        final ModuleImpl module = new ModuleImpl("stubModule", new ProjectImpl("stubProject", new GroupImpl("stubGroup")));

        final ContentDefinitionImpl a1 = addContentDefinition(module, "s1", "/a1");
        final ContentDefinitionImpl a2 = addContentDefinition(module, "s2", "/a2");
        final ContentDefinitionImpl a3 = addContentDefinition(module, "s3", "/a3");
        final ContentDefinitionImpl a4 = addContentDefinition(module, "s4", "/a4");
        final ContentDefinitionImpl a5 = addContentDefinition(module, "s5", "/a5");
        final ContentDefinitionImpl a6 = addContentDefinition(module, "s6", "/a6");
        final ContentDefinitionImpl a7 = addContentDefinition(module, "s7", "/a7");

        a2.getNode().setOrderBefore("a1");
        a1.getNode().setOrderBefore("a3");
        a3.getNode().setOrderBefore("a7");
        a6.getNode().setOrderBefore("a4");

        final ContentDefinitionImpl z = addContentDefinition(module, "s1", "/z");
        final ContentDefinitionImpl b = addContentDefinition(module, "s2", "/b");
        z.getNode().setOrderBefore("n");
        b.getNode().setOrderBefore("z");

        final List<ContentDefinitionImpl> sortedDefinitions = configurationContentService.getSortedDefinitions(module.getContentDefinitions());

        assertAfter(sortedDefinitions, a2, a1);
        assertAfter(sortedDefinitions, a1, a3);
        assertAfter(sortedDefinitions, a3, a7);
        assertAfter(sortedDefinitions, a6, a4);
        assertAfter(sortedDefinitions, b, z);

    }

    @Test
    public void test_ordering_circular_dependency() {

        final ModuleImpl module = new ModuleImpl("stubModule", new ProjectImpl("stubProject", new GroupImpl("stubGroup")));

        final ContentDefinitionImpl ca1 = addContentDefinition(module, "s1", "/ca1");
        final ContentDefinitionImpl ca2 = addContentDefinition(module, "s2", "/ca2");
        final ContentDefinitionImpl ca3 = addContentDefinition(module, "s2", "/ca3");

        ca1.getNode().setOrderBefore("ca3");
        ca2.getNode().setOrderBefore("ca1");
        ca3.getNode().setOrderBefore("ca2");

        try {
            configurationContentService.getSortedDefinitions(module.getContentDefinitions());
            fail("Circular Dependency exception should have been raised");
        } catch(CircularDependencyException ignore) {
        }
    }

    private ContentDefinitionImpl addContentDefinition(ModuleImpl module, String sourceName, String path) {
        final ContentSourceImpl contentSource = module.addContentSource(sourceName);
        final ContentDefinitionImpl contentDefinition = new ContentDefinitionImpl(contentSource);
        module.getContentDefinitions().add(contentDefinition);
        final DefinitionNodeImpl definitionNode = new DefinitionNodeImpl(path, contentDefinition);
        contentDefinition.setNode(definitionNode);
        return contentDefinition;
    }

}