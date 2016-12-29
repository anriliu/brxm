/*
 *  Copyright 2016-2017 Hippo B.V. (http://www.onehippo.com)
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

import java.io.IOException;
import java.util.Map;

import org.junit.Test;
import org.onehippo.cm.api.model.ConfigDefinition;
import org.onehippo.cm.api.model.Configuration;
import org.onehippo.cm.api.model.DefinitionNode;
import org.onehippo.cm.api.model.Module;
import org.onehippo.cm.api.model.Project;
import org.onehippo.cm.api.model.Source;

import static org.junit.Assert.assertEquals;

public class ConfigurationParserTest extends AbstractBaseTest {

    @Test
    public void expect_hierarchy_test_loads() throws IOException {
        final TestFiles files = collectFiles("/parser/hierarchy_test/repo-config.yaml");
        final ConfigurationParser parser = new ConfigurationParser();

        final Map<String, Configuration> configurations = parser.parse(files.repoConfig, files.sources);
        assertEquals(2, configurations.size());

        final Configuration base = assertConfiguration(configurations, "base", new String[0], 1);
        final Project project1 = assertProject(base, "project1", new String[0], 1);
        final Module module1 = assertModule(project1, "module1", new String[0], 1);
        final Source source1 = assertSource(module1, "hierarchy_test/repo-config/base/project1/module1/config.yaml", 1);
        final ConfigDefinition definition1 = assertDefinition(source1, 0, ConfigDefinition.class);

        final DefinitionNode rootDefinition1 = assertNode(definition1, "/", "/", definition1, false, 3, 1);
        assertProperty(rootDefinition1, "root-level-property", "/root-level-property",
                definition1, false, "root-level-property-value");
        final DefinitionNode nodeWithSingleProperty =
                assertNode(rootDefinition1, "node-with-single-property", "/node-with-single-property", false, definition1, false, 0, 1);
        assertProperty(nodeWithSingleProperty, "property", "/node-with-single-property/property",
                definition1, false, "node-with-single-property-value");
        final DefinitionNode nodeWithMultipleProperties =
                assertNode(rootDefinition1, "node-with-multiple-properties", "/node-with-multiple-properties", false, definition1, false, 0, 3);
        assertProperty(nodeWithMultipleProperties, "single", "/node-with-multiple-properties/single",
                definition1, false, "value1");
        assertProperty(nodeWithMultipleProperties, "multiple", "/node-with-multiple-properties/multiple",
                definition1, false, new String[]{"value2","value3"});
        assertProperty(nodeWithMultipleProperties, "empty-multiple", "/node-with-multiple-properties/empty-multiple",
                definition1, false, new String[0]);
        final DefinitionNode nodeWithSubNode =
                assertNode(rootDefinition1, "node-with-sub-node", "/node-with-sub-node", false, definition1, false, 1, 0);
        final DefinitionNode subNode =
                assertNode(nodeWithSubNode, "sub-node", "/node-with-sub-node/sub-node", false, definition1, false, 0, 1);
        assertProperty(subNode, "property", "/node-with-sub-node/sub-node/property",
                definition1, false, "sub-node-value");

        final Configuration myhippoproject = assertConfiguration(configurations, "myhippoproject", new String[]{"base"}, 1);
        final Project project2 = assertProject(myhippoproject, "project2", new String[0], 1);
        final Module module2 = assertModule(project2, "module2", new String[0], 1);
        final Source source2 = assertSource(module2, "hierarchy_test/repo-config/myhippoproject/project2/module2/config.yaml", 1);
        final ConfigDefinition definition2 = assertDefinition(source2, 0, ConfigDefinition.class);

        final DefinitionNode rootDefinition2 =
                assertNode(definition2, "/node-with-sub-node/sub-node", "/node-with-sub-node/sub-node", definition2, false, 0, 1);
        assertProperty(rootDefinition2, "property", "/node-with-sub-node/sub-node/property", definition2, false, "override");
    }

}
