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
package org.hippoecm.hst.core.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.collections.collection.CompositeCollection;
import org.hippoecm.hst.core.container.ContainerConfiguration;
import org.hippoecm.hst.core.container.HstComponentWindow;
import org.hippoecm.hst.core.request.HstRequestContext;

public class HstRequestImpl extends HttpServletRequestWrapper implements HstRequest {
    
    public static final String CONTAINER_ATTR_NAME_PREFIXES_PROP_KEY = HstRequest.class.getName() + ".containerAttributeNamePrefixes"; 
    
    private static String [] CONTAINER_ATTR_NAME_PREFIXES = null;
    
    protected HstRequestContext requestContext;
    protected Map<String, Map<String, Object>> namespaceParametersMap = new HashMap<String, Map<String, Object>>();
    protected Map<String, Map<String, Object>> namespaceAttributesMap = new HashMap<String, Map<String, Object>>();
    protected HstComponentWindow componentWindow;
    protected String parameterNameComponentSeparator;
    
    public HstRequestImpl(HttpServletRequest servletRequest, HstRequestContext requestContext, HstComponentWindow componentWindow) {
        super(servletRequest);
        this.requestContext = requestContext;
        this.componentWindow = componentWindow;
        this.parameterNameComponentSeparator = requestContext.getURLFactory().getServletUrlProvider().getParameterNameComponentSeparator();
    }
    
    public void setRequest(HttpServletRequest servletRequest) {
        super.setRequest(servletRequest);
    }

    public Map<String, Object> getParameterMap() {
        String referenceNamespace = this.componentWindow.getReferenceNamespace();
        return getParameterMap(referenceNamespace);
    }
    
    public Map<String, Object> getParameterMap(String referencePath) {
        Map<String, Object> parameterMap = null;
        
        String namespace = getReferenceNamespacePath(referencePath);
        String prefix = getFullNamespacePrefix(namespace);
        int paramPrefixLen = prefix.length();
        parameterMap = this.namespaceParametersMap.get(prefix);
        
        if (parameterMap == null) {
            parameterMap = new HashMap<String, Object>();

            if (this.requestContext.getBaseURL().getActionWindowReferenceNamespace() != null) {
                Map<String, String []> actionParams = this.requestContext.getBaseURL().getActionParameterMap();
                
                if (actionParams != null) {
                    for (Map.Entry<String, String []> entry : actionParams.entrySet()) {
                        String paramName = entry.getKey();
                        String [] paramValues = entry.getValue();
                        parameterMap.put(paramName, paramValues.length > 1 ? paramValues : paramValues[0]);
                    }
                }
                
                for (Enumeration paramNames = super.getParameterNames(); paramNames.hasMoreElements(); ) {
                    String paramName = (String) paramNames.nextElement();
                    String [] paramValues = super.getParameterValues(paramName);
                    parameterMap.put(paramName, paramValues.length > 1 ? paramValues : paramValues[0]);
                }
            } else {
                for (Enumeration paramNames = super.getParameterNames(); paramNames.hasMoreElements(); ) {
                    String encodedParamName = (String) paramNames.nextElement();
                    
                    if (encodedParamName.startsWith(prefix)) {
                        String paramName = encodedParamName.substring(paramPrefixLen);
                        String [] paramValues = super.getParameterValues(encodedParamName);
                        parameterMap.put(paramName, paramValues.length > 1 ? paramValues : paramValues[0]);
                    }
                }
            }
            
            this.namespaceParametersMap.put(prefix, parameterMap);
        }
        
        return parameterMap;
    }
    
    public Map<String, Object> getAttributeMap() {
        String referenceNamespace = this.componentWindow.getReferenceNamespace();
        return getAttributeMap(referenceNamespace);
    }
    
    public Map<String, Object> getAttributeMap(String referencePath) {
        String namespace = getReferenceNamespacePath(referencePath);
        String prefix = getFullNamespacePrefix(namespace);
        int prefixLen = prefix.length();
        Map<String, Object> attributesMap = this.namespaceAttributesMap.get(prefix);
        
        if (attributesMap == null) {
            attributesMap = new HashMap<String, Object>();
            
            for (Enumeration attributeNames = super.getAttributeNames(); attributeNames.hasMoreElements(); ) {
                String encodedAttributeName = (String) attributeNames.nextElement();
                
                if (encodedAttributeName.startsWith(prefix)) {
                    String attributeName = encodedAttributeName.substring(prefixLen);
                    Object attributeValue = super.getAttribute(encodedAttributeName);
                    attributesMap.put(attributeName, attributeValue);
                }
            }
            
            this.namespaceAttributesMap.put(prefix, attributesMap);
        }
        
        return attributesMap;
    }

    @Override
    public String getParameter(String name) {
        if (name == null) {
            throw new IllegalArgumentException("parameter name cannot be null.");
        }
        
        Object value = getParameterMap().get(name);

        if (value == null) {
            return null;
        } else if (value instanceof String[]) {
            return (((String[]) value)[0]);
        } else if (value instanceof String) {
            return ((String) value);
        } else {
            return (value.toString());
        }
    }

    @Override
    public Enumeration getParameterNames() {
        return Collections.enumeration(this.getParameterMap().keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        if (name == null) {
            throw new IllegalArgumentException("parameter name cannot be null.");
        }
        
        Object value = getParameterMap().get(name);
        
        if (value == null) {
            return null;
        } else if (value instanceof String[]) {
            return ((String[]) value);
        } else if (value instanceof String) {
            return new String [] { (String) value };
        } else {
            return new String [] { value.toString() };
        }
    }

    @Override
    public Enumeration getAttributeNames() {
        List servletRequestAttrs = EnumerationUtils.toList(super.getAttributeNames());
        Set localRequestAttrs = this.getAttributeMap().keySet();
        Collection composite = new CompositeCollection(new Collection [] { servletRequestAttrs, localRequestAttrs });
        return Collections.enumeration(composite);
    }
    
    @Override
    public Object getAttribute(String name) {
        if (name == null) {
            throw new IllegalArgumentException("attribute name cannot be null.");
        }
        
        Object value = null;
        
        if (isContainerAttributeName(name)) {
            value = super.getAttribute(name);
        } else {
            value = getAttributeMap().get(name);
            
            if (value == null) {
                value = super.getAttribute(name);
            }
        }
        
        return value;
    }

    @Override
    public void setAttribute(String name, Object value) {
        if (name == null) {
            throw new IllegalArgumentException("attribute name cannot be null.");
        }
        
        if (value == null) {
            removeAttribute(name);
        } else if (isContainerAttributeName(name)) {
            super.setAttribute(name, value);
        } else {
            getAttributeMap().put(name, value);
        }
    }

    @Override
    public void removeAttribute(String name) {
        if (name == null) {
            throw new IllegalArgumentException("attribute name cannot be null.");
        }
        
        if (isContainerAttributeName(name)) {
            super.removeAttribute(name);
        } else {
            Object value = getAttributeMap().remove(name);
            
            // Remove attribute from the servlet request
            // if no attribute was removed from the this local request attributes.
            if (value == null) {
                super.removeAttribute(name);
            }
        }
    }
    
    public HstRequestContext getRequestContext() {
        return (HstRequestContext) super.getAttribute(HstRequestContext.class.getName());
    }

    public HstComponentWindow getComponentWindow() {
        return this.componentWindow;
    }
    
    public String getResourceID() {
        return this.requestContext.getBaseURL().getResourceId();
    }

    protected String getReferenceNamespacePath(String referencePath) {
        return referencePath;
    }
    
    protected String getFullNamespacePrefix(String referenceNamespace) {
        String prefix = referenceNamespace + this.parameterNameComponentSeparator;
        return prefix;
    }

    protected boolean isContainerAttributeName(String attrName) {
        boolean containerAttrName = false;
        
        if (CONTAINER_ATTR_NAME_PREFIXES == null) {
            synchronized (HstRequestImpl.class) {
                if (CONTAINER_ATTR_NAME_PREFIXES == null) {
                    ArrayList containerAttrNamePrefixes = new ArrayList(Arrays.asList("javax."));
                    ContainerConfiguration containerConfiguration = this.requestContext.getContainerConfiguration();
                    
                    if (containerConfiguration != null) {
                        containerAttrNamePrefixes.addAll(this.requestContext.getContainerConfiguration().getList(CONTAINER_ATTR_NAME_PREFIXES_PROP_KEY));
                    }
                    
                    CONTAINER_ATTR_NAME_PREFIXES = (String []) containerAttrNamePrefixes.toArray(new String[0]);
                }
            }
        }
        
        for (String prefix : CONTAINER_ATTR_NAME_PREFIXES) {
            if (attrName.startsWith(prefix)) {
                containerAttrName = true;
                break;
            }
        }
        
        return containerAttrName;
    }
}
