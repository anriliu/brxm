/*
 *  Copyright 2012 Hippo.
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
package org.hippoecm.hst.pagecomposer.jaxrs.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.hippoecm.hst.pagecomposer.jaxrs.model.ContainerItemComponentPropertyRepresentation;
import org.hippoecm.repository.TestCase;
import org.junit.Test;

public class ContainerItemComponentResourceTest extends TestCase {
    
    private static final String HST_PARAMETERVALUES = "hst:parametervalues";
    private static final String HST_PARAMETERNAMES = "hst:parameternames";
    private static final String HST_PARAMETERNAMEPREFIXES = "hst:parameternameprefixes";

    private static final String[] emptyTestComponent = {
        "/test", "nt:unstructured",
        "/test/component", "hst:containeritemcomponent",
        "hst:componentclassname", "org.hippoecm.hst.pagecomposer.jaxrs.services.DummyComponent",
        "hst:xtype", "HST.Item"
    };
    
    private static final String[] testComponent = {
        "/test", "nt:unstructured",
        "/test/component", "hst:containeritemcomponent",
        "hst:componentclassname", "org.hippoecm.hst.pagecomposer.jaxrs.services.DummyComponent",
        "hst:xtype", "HST.Item",
        "hst:parameternameprefixes", "",
        "hst:parameternameprefixes", "prefix",
        "hst:parameternames", "parameterOne",
        "hst:parameternames", "parameterOne",
        "hst:parametervalues", "bar",
        "hst:parametervalues", "baz"
    };
    
    @Test
    public void testGetParameters() throws RepositoryException, ClassNotFoundException {
        build(session, testComponent);
        Node node = session.getNode("/test/component");
        
        List<ContainerItemComponentPropertyRepresentation> result = null;
                
        result = new ContainerItemComponentResource().doGetParameters(node, null, "").getProperties();
        assertEquals(2, result.size());
        assertEquals("parameterOne", result.get(0).getName());
        assertEquals("bar", result.get(0).getValue());
        assertEquals("", result.get(0).getDefaultValue());
        assertEquals("parameterTwo", result.get(1).getName());
        assertEquals("", result.get(1).getValue());
        assertEquals("test", result.get(1).getDefaultValue());
        
        result = new ContainerItemComponentResource().doGetParameters(node, null, "prefix").getProperties();
        assertEquals(2, result.size());
        assertEquals("parameterOne", result.get(0).getName());
        assertEquals("baz", result.get(0).getValue());
        assertEquals("", result.get(0).getDefaultValue());
        assertEquals("parameterTwo", result.get(1).getName());
        assertEquals("", result.get(1).getValue());
        assertEquals("test", result.get(1).getDefaultValue());
    }
    
    @Test
    public void testSetParametersWithoutPrefix() throws RepositoryException {
        build(session, emptyTestComponent);
        Node node = session.getNode("/test/component");
        
        Value[] names = null;
        Value[] values = null;
        
        MultivaluedMap<String, String> params = null;
        
        // 1. add foo = bar
        params = new MetadataMap<String, String>();
        params.add("parameterOne", "bar");
        new ContainerItemComponentResource().doSetParameters(node, null, params);
        assertTrue(node.hasProperty(HST_PARAMETERNAMES));
        assertTrue(node.hasProperty(HST_PARAMETERVALUES));
        assertTrue(!node.hasProperty(HST_PARAMETERNAMEPREFIXES));
        
        names = node.getProperty(HST_PARAMETERNAMES).getValues();
        assertEquals(1, names.length);
        assertEquals("parameterOne", names[0].getString());
        
        values = node.getProperty(HST_PARAMETERVALUES).getValues();
        assertEquals(1, values.length);
        assertEquals("bar", values[0].getString());
        
        // 2. if params is empty, old values should be kept AS IS
        params = new MetadataMap<String, String>();
        new ContainerItemComponentResource().doSetParameters(node, null, params);
        names = node.getProperty(HST_PARAMETERNAMES).getValues();
        assertEquals(1, names.length);
        assertEquals("parameterOne", names[0].getString());
        
        values = node.getProperty(HST_PARAMETERVALUES).getValues();
        assertEquals(1, values.length);
        assertEquals("bar", values[0].getString());
        assertTrue(!node.hasProperty(HST_PARAMETERNAMEPREFIXES));
        
        // 3. add bar = test without prefix
        // We should keep the already existing "parameterOne = bar" 
        // but now should also have "parameterTwo = test" : Even though the default
        // value for DummyInfo for "parameterTwo = test" , we STILL store it
        params = new MetadataMap<String, String>();
        params.add("parameterTwo", "test");
        new ContainerItemComponentResource().doSetParameters(node, null, params);
        
        names = node.getProperty(HST_PARAMETERNAMES).getValues();
        assertEquals(2, names.length);
        
        // names[0] is either  parameterOne or parameterTwo (not sure about the order)
        assertTrue(names[0].getString().equals("parameterOne") || names[0].getString().equals("parameterTwo"));
        assertTrue(names[1].getString().equals("parameterOne") || names[1].getString().equals("parameterTwo"));
        
        values = node.getProperty(HST_PARAMETERVALUES).getValues();
        assertEquals(2, values.length);
        if(names[0].getString().equals("parameterOne")) {
            assertEquals("bar", values[0].getString());
            assertEquals("test", values[1].getString());
        } else {
            assertEquals("test", values[0].getString());
            assertEquals("bar", values[1].getString());
        }
        assertTrue(!node.hasProperty(HST_PARAMETERNAMEPREFIXES));

    }
    
    @Test
    public void testSetParameterWithPrefix() throws RepositoryException {
        build(session, emptyTestComponent);
        Node node = session.getNode("/test/component");
        
        Value[] names = null;
        Value[] values = null;
        Value[] prefixes = null;
        
        MultivaluedMap<String, String> params = null;
        
        // 1. add foo = bar
        params = new MetadataMap<String, String>();
        params.add("parameterOne", "bar");
        
        new ContainerItemComponentResource().doSetParameters(node, "prefix", params);
        assertTrue(node.hasProperty(HST_PARAMETERNAMES));
        assertTrue(node.hasProperty(HST_PARAMETERVALUES));
        assertTrue(node.hasProperty(HST_PARAMETERNAMEPREFIXES));
        
        names = node.getProperty(HST_PARAMETERNAMES).getValues();
        assertEquals(1, names.length);
        assertEquals("parameterOne", names[0].getString());
        
        values = node.getProperty(HST_PARAMETERVALUES).getValues();
        assertEquals(1, values.length);
        assertEquals("bar", values[0].getString());
        
        prefixes = node.getProperty(HST_PARAMETERNAMEPREFIXES).getValues();
        assertEquals(1, prefixes.length);
        assertEquals("prefix", prefixes[0].getString());
        

        // 2. if params is empty, old values should be kept AS IS
        params = new MetadataMap<String, String>();
        new ContainerItemComponentResource().doSetParameters(node, "prefix", params);
        names = node.getProperty(HST_PARAMETERNAMES).getValues();
        assertEquals(1, names.length);
        assertEquals("parameterOne", names[0].getString());
        
        values = node.getProperty(HST_PARAMETERVALUES).getValues();
        assertEquals(1, values.length);
        assertEquals("bar", values[0].getString());
        
        prefixes = node.getProperty(HST_PARAMETERNAMEPREFIXES).getValues();
        assertEquals(1, prefixes.length);
        assertEquals("prefix", prefixes[0].getString());

        // 3. if prefixed parameter value is same as default parameter value then persist anyway
        
        // first set default parameter parameterOne = bar
        node.setProperty(HST_PARAMETERNAMES, new String[] {"parameterOne"});
        node.setProperty(HST_PARAMETERVALUES, new String[] {"bar"});
        node.setProperty(HST_PARAMETERNAMEPREFIXES, new String[] {""});
        session.save();

        // try to add parameterOne = bar in variant "prefix"
        params = new MetadataMap<String, String>();
        params.add("parameterOne", "bar");
        new ContainerItemComponentResource().doSetParameters(node, "prefix", params);
        assertTrue(node.hasProperty(HST_PARAMETERNAMES));
        assertTrue(node.hasProperty(HST_PARAMETERVALUES));
        assertTrue(node.hasProperty(HST_PARAMETERNAMEPREFIXES));
        
        // we should now have default parameterOne = bar and prefixed parameterOne = bar
        
        names = node.getProperty(HST_PARAMETERNAMES).getValues();
        assertEquals(2, names.length);
        assertEquals("parameterOne", names[0].getString());
        assertEquals("parameterOne", names[1].getString());

        values = node.getProperty(HST_PARAMETERVALUES).getValues();
        assertEquals(2, values.length);
        assertEquals("bar", values[0].getString());
        assertEquals("bar", values[1].getString());
        
        prefixes = node.getProperty(HST_PARAMETERNAMEPREFIXES).getValues();
        assertEquals(2, values.length);
        
    }
}
