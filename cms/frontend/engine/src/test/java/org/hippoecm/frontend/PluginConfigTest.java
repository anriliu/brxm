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
package org.hippoecm.frontend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;

import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.model.map.HippoMap;
import org.hippoecm.frontend.model.map.IHippoMap;
import org.hippoecm.frontend.model.map.JcrMap;
import org.hippoecm.frontend.plugin.config.IClusterConfig;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugin.config.impl.ClusterConfigDecorator;
import org.hippoecm.frontend.plugin.config.impl.JcrClusterConfig;
import org.hippoecm.frontend.plugin.config.impl.JcrPluginConfig;
import org.hippoecm.repository.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PluginConfigTest extends TestCase {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    Node root, cleanerConfigNode;

    String[] content = new String[] {
            "/map", "nt:unstructured", "a", "b", "c", "d",
            "/config", "frontend:pluginconfig", "a", "b",
            "/config/sub", "frontend:pluginconfig", "c", "d",
            "/cluster", "frontend:plugincluster",
            "/cluster/plugin", "frontend:plugin", "c", "d", "x", "${cluster.id}",
            "/cluster/plugin/sub", "frontend:pluginconfig", "a", "b", "y", "${cluster.id}"
        };

    @Before
    public void setUp() throws Exception {
        super.setUp(true);
        root = session.getRootNode();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMap() throws Exception {
        build(session, content);

        IHippoMap map = new JcrMap(root.getNode("map"));
        assertEquals("b", map.get("a"));

        Set set = map.entrySet();
        assertEquals(2, set.size());

        Map<String, String> expected = new HashMap<String, String>();
        expected.put("a", "b");
        expected.put("c", "d");

        Iterator iter;
        Map.Entry entry;
        String key;
        Set keys;
        Collection values;

        iter = set.iterator();
        assertTrue(iter.hasNext());
        for (int i = 0; i < 2; i++) {
            entry = (Map.Entry) iter.next();
            assertTrue(expected.containsKey(entry.getKey()));
            assertEquals(expected.get(entry.getKey()), entry.getValue());
            expected.remove(entry.getKey());
        }
        assertFalse(iter.hasNext());

        values = map.values();
        assertEquals(2, values.size());
        assertTrue(values.contains("b"));
        assertTrue(values.contains("d"));

        List<String> expectedKeys = new ArrayList(2);
        expectedKeys.add("a");
        expectedKeys.add("c");

        keys = map.keySet();
        assertEquals(2, keys.size());
        iter = keys.iterator();
        for (int i = 0; i < 2; i++) {
            assertTrue(iter.hasNext());
            key = (String) iter.next();
            assertTrue(expectedKeys.contains(key));
            expectedKeys.remove(key);
        }

        assertTrue(map.containsKey("a"));
        assertTrue(map.containsValue("b"));
        assertTrue(map.containsKey("c"));
        assertTrue(map.containsValue("d"));

        map.put("a", "c");
        assertEquals("c", map.get("a"));

        map.put("b", Boolean.TRUE);
        assertEquals(Boolean.TRUE, map.get("b"));

        HippoMap subMap = new HippoMap();
        subMap.put("x", "y");
        List list = new ArrayList();
        list.add(subMap);
        map.put("m", list);

        List checkList = (List) map.get("m");
        assertEquals(1, checkList.size());
        IHippoMap checkMap = (IHippoMap) checkList.get(0);
        assertEquals("y", checkMap.get("x"));
    }

    protected IPluginConfig getPluginConfig() throws Exception {
        return new JcrPluginConfig(new JcrNodeModel(root.getNode("config")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testConfig() throws Exception {
        build(session, content);

        IPluginConfig config = getPluginConfig();
        assertEquals("b", config.getString("a"));

        IPluginConfig subConfig = config.getPluginConfig("sub");
        assertEquals("d", subConfig.getString("c"));

        config.put("e", "f");
        assertEquals("f", config.getString("e"));
    }

    protected IClusterConfig getClusterConfig() throws Exception {
        return new JcrClusterConfig(new JcrNodeModel(root.getNode("cluster")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCluster() throws Exception {
        build(session, content);

        IClusterConfig config = getClusterConfig();
        List<IPluginConfig> plugins = config.getPlugins();
        assertEquals(1, plugins.size());

        assertEquals(0, config.getServices().size());
        assertEquals(0, config.getReferences().size());
        assertEquals(0, config.getProperties().size());
        
        IPluginConfig pluginConfig = plugins.get(0);
        assertEquals("d", pluginConfig.getString("c"));
        assertEquals("${cluster.id}", pluginConfig.getString("x"));

        IPluginConfig subConfig = pluginConfig.getPluginConfig("sub");
        assertEquals("b", subConfig.getString("a"));
        assertEquals("${cluster.id}", subConfig.getString("y"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDecorator() throws Exception {
        build(session, content);

        IClusterConfig decorator = new ClusterConfigDecorator(getClusterConfig(), "cluster");
        List<IPluginConfig> plugins = decorator.getPlugins();
        assertEquals(1, plugins.size());

        IPluginConfig pluginConfig = plugins.get(0);
        assertEquals("d", pluginConfig.getString("c"));
        assertEquals("cluster", pluginConfig.getString("x"));

        IPluginConfig subConfig = pluginConfig.getPluginConfig("sub");
        assertEquals("b", subConfig.getString("a"));
        assertEquals("cluster", subConfig.getString("y"));
    }

}
