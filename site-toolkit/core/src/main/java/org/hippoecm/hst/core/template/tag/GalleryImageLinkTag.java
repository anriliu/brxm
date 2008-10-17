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
package org.hippoecm.hst.core.template.tag;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.hippoecm.hst.core.HSTHttpAttributes;
import org.hippoecm.hst.core.mapping.URLMapping;
import org.hippoecm.hst.core.template.node.el.ELNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GalleryImageLinkTag extends SimpleTagSupport {
	private static final Logger log = LoggerFactory.getLogger(GalleryImageLinkTag.class);
	
	private String var;
	private String relPath;
	private String type;

	private ELNode item;
	
	@Override
	public void doTag() throws JspException, IOException {
		PageContext pageContext = (PageContext) getJspContext(); 
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

		URLMapping urlMapping = (URLMapping)request.getAttribute(HSTHttpAttributes.URL_MAPPING_ATTR);
		String src = "";
        if(item!= null) {
            try {
            	if(item.getJcrNode().hasNode(relPath)){
				Node imageNode = item.getJcrNode().getNode(relPath);
				if(imageNode!=null){
					  if(imageNode.hasProperty("hippo:docbase")){
						  Node facetedNode = imageNode.getSession().getNodeByUUID(imageNode.getProperty("hippo:docbase").getValue().getString());
						  Node childFacetNode = facetedNode.getNode(facetedNode.getName());
						  Node gpn = null;
						  if(type!=null){
							  if(type.equals("picture"))
								  gpn = childFacetNode.getNode("hippogallery:picture");
							  else if(type.equals("thumbnail")){
								  gpn = childFacetNode.getNode("hippogallery:thumbnail");
							  }							  
						  }
						  else{
							  gpn = childFacetNode.getNode("hippogallery:picture");
						  }
                          src = urlMapping.rewriteLocation(gpn);
                          pageContext.setAttribute(getVar(), src);
					  }
				}
            	}
			} catch (PathNotFoundException e) {
				e.printStackTrace();
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
        }		
	}

    public ELNode getItem(){
        return item;
    }
    

    public void setItem(ELNode item){
        this.item = item;
    }
	
	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}

	public String getRelPath() {
		return relPath;
	}

	public void setRelPath(String relPath) {
		this.relPath = relPath;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}


}
