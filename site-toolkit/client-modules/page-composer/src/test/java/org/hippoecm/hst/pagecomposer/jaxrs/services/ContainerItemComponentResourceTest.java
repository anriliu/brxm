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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBException;

import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.hippoecm.hst.pagecomposer.jaxrs.model.ContainerItemComponentPropertyRepresentation;
import org.hippoecm.hst.pagecomposer.jaxrs.util.HstComponentParameters;
import org.junit.Test;
import org.onehippo.repository.mock.MockNode;
import org.onehippo.repository.mock.MockNodeFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ContainerItemComponentResourceTest {

    private static final String HST_PARAMETERVALUES = "hst:parametervalues";
    private static final String HST_PARAMETERNAMES = "hst:parameternames";
    private static final String HST_PARAMETERNAMEPREFIXES = "hst:parameternameprefixes";

    @Test
    public void testGetParameters() throws RepositoryException, ClassNotFoundException, JAXBException, IOException {
        MockNode node = MockNodeFactory.fromXml("/org/hippoecm/hst/pagecomposer/jaxrs/services/ContainerItemComponentResourceTest-test-component.xml");

        List<ContainerItemComponentPropertyRepresentation> result = new ContainerItemComponentResource().doGetParameters(node, null, "", "").getProperties();
        assertEquals(2, result.size());
        assertEquals("parameterOne", result.get(0).getName());
        assertEquals("bar", result.get(0).getValue());
        assertEquals("", result.get(0).getDefaultValue());
        assertEquals("parameterTwo", result.get(1).getName());
        assertEquals("", result.get(1).getValue());
        assertEquals("test", result.get(1).getDefaultValue());

        result = new ContainerItemComponentResource().doGetParameters(node, null, "prefix", "").getProperties();
        assertEquals(2, result.size());
        assertEquals("parameterOne", result.get(0).getName());
        assertEquals("baz", result.get(0).getValue());
        assertEquals("", result.get(0).getDefaultValue());
        assertEquals("parameterTwo", result.get(1).getName());
        assertEquals("", result.get(1).getValue());
        assertEquals("test", result.get(1).getDefaultValue());
    }

    @Test
    public void testVariantCreation() throws RepositoryException, JAXBException, IOException {
        Node node = MockNodeFactory.fromXml("/org/hippoecm/hst/pagecomposer/jaxrs/services/ContainerItemComponentResourceTest-empty-component.xml");

        MultivaluedMap<String, String> params;

        // 1. add a non annotated parameter for 'default someNonAnnotatedParameter = lux
        params = new MetadataMap<String, String>();
        params.add("parameterOne", "bar");
        params.add("someNonAnnotatedParameter", "lux");

        new ContainerItemComponentResource().doSetParameters(node, null, params);

        assertTrue(node.hasProperty(HST_PARAMETERNAMES));
        assertTrue(node.hasProperty(HST_PARAMETERVALUES));
        // do not contain HST_PARAMETERNAMEPREFIXES
        assertTrue(!node.hasProperty(HST_PARAMETERNAMEPREFIXES));

        Map<String, String> defaultAnnotated =  ContainerItemComponentResource.getAnnotatedDefaultValues(node);
        assertTrue(defaultAnnotated.containsKey("parameterOne"));
        assertEquals(defaultAnnotated.get("parameterOne"), "");
        assertTrue(defaultAnnotated.containsKey("parameterTwo"));
        assertEquals(defaultAnnotated.get("parameterTwo"), "test");


        Set<String> variants =  ContainerItemComponentResource.doGetVariants(node);
        assertTrue(variants.size() == 1);
        assertTrue(variants.contains("default"));

        // 2. create a new variant 'lux' : The creation of the variant should 
        // pick up the explicitly defined parameters from 'default' that are ALSO annotated (thus parameterOne, and NOT someNonAnnotatedParameter) PLUS
        // the implicit parameters from the DummyInfo (parameterTwo but not parameterOne because already from 'default')

        new ContainerItemComponentResource().doCreateVariant(node, new HstComponentParameters(node), "newvar");
        assertTrue(node.hasProperty(HST_PARAMETERNAMES));
        assertTrue(node.hasProperty(HST_PARAMETERVALUES));
        // now it must contain HST_PARAMETERNAMEPREFIXES
        assertTrue(node.hasProperty(HST_PARAMETERNAMEPREFIXES));

        variants = ContainerItemComponentResource.doGetVariants(node);
        assertTrue(variants.size() == 2);
        assertTrue(variants.contains("default"));
        assertTrue(variants.contains("newvar"));

        HstComponentParameters componentParameters = new HstComponentParameters(node);
        assertTrue(componentParameters.hasParameter("newvar", "parameterOne"));
        assertEquals("bar", componentParameters.getValue("newvar", "parameterOne"));
        assertTrue(componentParameters.hasParameter("newvar", "parameterTwo"));
        // from  @Parameter(name = "parameterTwo", required = true, defaultValue = "test")
        assertEquals("test", componentParameters.getValue("newvar", "parameterTwo"));
        assertFalse(componentParameters.hasParameter("newvar", "someNonAnnotatedParameter"));

        // 3. try to remove the new variant
        new ContainerItemComponentResource().doDeleteVariant(new HstComponentParameters(node), "newvar");
        variants = ContainerItemComponentResource.doGetVariants(node);
        assertTrue(variants.size() == 1);
        assertTrue(variants.contains("default"));

        // 4. try to remove the 'default' variant : this should not be allowed
        boolean removeSucceeded = true;
        try {
            new ContainerItemComponentResource().doDeleteVariant(new HstComponentParameters(node), "default");
            fail("Default variant should not be possible to be removed");
        } catch (IllegalStateException e) {
            removeSucceeded = false;
        }
        assertFalse("Remove should not have succeeded", removeSucceeded);
    }

}
