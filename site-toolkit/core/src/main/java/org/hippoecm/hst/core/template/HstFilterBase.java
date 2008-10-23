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
package org.hippoecm.hst.core.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.hippoecm.hst.core.HSTHttpAttributes;
import org.hippoecm.hst.core.template.node.PageNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for HST related filters.
 *
 */
public abstract class HstFilterBase implements Filter {
	
	public static final String TEMPLATE_CONFIGURATION_LOCATION = "/";
	public static final String TEMPLATE_CONTEXTBASE_NAME = "templateContextBase";
	public static final String SITEMAP_RELATIVE_LOCATION = "hst:sitemap";
	public static final String HSTCONFIGURATION_LOCATION_PARAMETER = "hstConfigurationUrl";
	public static final String ATTRIBUTENAME_INIT_PARAMETER = "attributeName";
	
	//request attributes
	public static final String PAGENODE_REQUEST_ATTRIBUTE = HSTHttpAttributes.CURRENT_PAGE_NODE_REQ_ATTRIBUTE;
	public static final String CONTENT_CONTEXT_REQUEST_ATTRIBUTE = HSTHttpAttributes.CURRENT_CONTENT_CONTEXTBASE_REQ_ATTRIBUTE;
	//public static final String CURRENT_PAGE_MODULE_ATTRIBUTE = "currentPageModule";
	
	//filter init-parameter
	protected static final String IGNOREPATHS_FILTER_INIT_PARAM = "ignorePaths"; //comma separated list with ignore path prefixes
	protected static final String IGNORETYPES_FILTER_INIT_PARAM = "ignoreTypes"; //comma separated list with ignore path prefixes
	
	private List<String> ignorePathsList = null;
	private List<String> ignoreTypesList = null;
	
	private static final Logger log = LoggerFactory.getLogger(HstFilterBase.class);
	
	public void init(FilterConfig filterConfig) throws ServletException {		
		//hstConfigurationUrl = getInitParameter(filterConfig, HSTCONFIGURATION_LOCATION_PARAMETER);
		initIgnoreTypes(filterConfig);
		initIgnorePaths(filterConfig);
	}
	
	protected void initIgnoreTypes(FilterConfig filterConfig) {
		String ignoreTypesString = filterConfig.getInitParameter(IGNORETYPES_FILTER_INIT_PARAM);
		ignoreTypesList = new ArrayList<String>();
		if (ignoreTypesString != null) {	
		    String [] items = ignoreTypesString.split(",");
		    for (int i=0; i < items.length; i++) {
		    	log.debug("filter configured with ignoretype ." + items[i]);
		    	ignoreTypesList.add("." + items[i].trim());
		    }
		}
	}
	
	protected void initIgnorePaths(FilterConfig filterConfig) {
		String ignorePathsString = filterConfig.getInitParameter(IGNOREPATHS_FILTER_INIT_PARAM);
		ignorePathsList = new ArrayList<String>();
		if (ignorePathsString != null) {	
		    String [] items = ignorePathsString.split(",");
		    for (int i=0; i < items.length; i++) {
		    	log.debug("filter configured with ignorepath ." + items[i]);
		    	ignorePathsList.add(items[i].trim());
		    }
		}
	}
	

	
	
	protected boolean ignoreRequest(HttpServletRequest request) {
		if (request.getAttribute(HSTHttpAttributes.REQUEST_IGNORE_HSTPROCESSING_REQ_ATTRIBUTE) != null) {
			return true;
		}
		String requestURI = request.getRequestURI().replaceFirst(request.getContextPath(), "");
		for(String prefix : ignorePathsList) {
		    if(requestURI.startsWith(prefix)) {
		        request.setAttribute(HSTHttpAttributes.REQUEST_IGNORE_HSTPROCESSING_REQ_ATTRIBUTE, "true");
		        return true;
		    }
		}
		for (String suffix: ignoreTypesList) {
			if(requestURI.endsWith(suffix)) {
		        request.setAttribute(HSTHttpAttributes.REQUEST_IGNORE_HSTPROCESSING_REQ_ATTRIBUTE, "true");
		        return true;
		    }
		}
		return false;
	}
	
	
	protected String getUrlPrefix(HttpServletRequest request) {
		String urlPrefix = (String) request.getAttribute(ATTRIBUTENAME_INIT_PARAMETER);
    	urlPrefix = (urlPrefix == null) ? "" : urlPrefix;
    	return urlPrefix;
	}
	
	
	public PageNode getPageNode(HttpServletRequest request, String pageNodeName) throws TemplateException, RepositoryException{
		ContextBase hstConfigurationContextBase = getHstConfigurationContextBase(request, TEMPLATE_CONFIGURATION_LOCATION);
			
		Node siteMapNodes = hstConfigurationContextBase.getRelativeNode(SITEMAP_RELATIVE_LOCATION);
		NodeIterator siteMapItemIterator = siteMapNodes.getNodes();
		if (siteMapItemIterator == null) {
			return null;
		} else {
			while (siteMapItemIterator.hasNext()) {
				Node siteMapItem = siteMapItemIterator.nextNode();
				log.debug("looking for " + pageNodeName + " with location"  + siteMapItem.getPath() + " and name " + siteMapItem.getName());
				if (siteMapItem.getName().equals(pageNodeName)) {
					return new PageNode(hstConfigurationContextBase, siteMapItem);
				}
			}
		}
		return null;
	}
	
	
	
	protected Map <String, PageNode> getURLMappingNodes(ContextBase templateContextBase) throws RepositoryException {
		Map<String, PageNode> siteMapNodes = new HashMap<String, PageNode>();
	
		Node siteMapRootNode = templateContextBase.getRelativeNode(SITEMAP_RELATIVE_LOCATION);	 
	    NodeIterator subNodes =  siteMapRootNode.getNodes();
	    while  (subNodes.hasNext()) {
	    	Node subNode = (Node) subNodes.next();
	    	if(subNode == null) {
	    	    continue;
	    	}
	    	if(subNode.hasProperty("hst:urlmapping")) {
	    	    Property urlMappingProperty = subNode.getProperty("hst:urlmapping");
	            siteMapNodes.put(urlMappingProperty.getValue().getString(), new PageNode(templateContextBase, subNode));
	    	} else {
	    	    log.debug("hst:sitemapitem sitemap item missing 'hst:ulrmapping' property. Item not meant for mapping, but only for binaries");
	    	}
	    }
		return siteMapNodes;
	}
	

	protected void verifyInitParameterHasValue(FilterConfig filterConfig, String param) throws ServletException {
	  if (filterConfig.getInitParameter(param) == null) {
          throw new ServletException("Missing init-param " + param);
      }
	}
	
	
	protected String getInitParameter(FilterConfig filterConfig, String param, boolean required)
			throws ServletException {
		String parameterValue = filterConfig.getInitParameter(param);
		if (parameterValue == null && required) {
			throw new ServletException("Missing init-param " + param);
		}
		return parameterValue;
	}
	
	protected ContextBase getHstConfigurationContextBase(HttpServletRequest request, String hstConfigurationLocation) throws TemplateException {
		ContextBase hstConfigurationContextBase = null;
		if (request.getAttribute(HSTHttpAttributes.CURRENT_HSTCONFIGURATION_CONTEXTBASE_REQ_ATTRIBUTE) == null) {
			Session session =   (Session)request.getAttribute(HSTHttpAttributes.JCRSESSION_MAPPING_ATTR);
			try {
				hstConfigurationContextBase = new ContextBase(TEMPLATE_CONTEXTBASE_NAME, hstConfigurationLocation, request, session);
			} catch (PathNotFoundException e) {
				throw new TemplateException(e);
			} catch (RepositoryException e) {
				throw new TemplateException(e);
			}
		} else {
			hstConfigurationContextBase = (ContextBase) request.getAttribute(HSTHttpAttributes.CURRENT_HSTCONFIGURATION_CONTEXTBASE_REQ_ATTRIBUTE);
		}
		return hstConfigurationContextBase;
	}
}
