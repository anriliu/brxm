/*
 * Copyright 2008 Hippo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hippoecm.hst.taglib;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.hippoecm.hst.core.Context;
import org.hippoecm.hst.core.HSTConfiguration;
import org.hippoecm.hst.util.PropertyFormatter;

public class ContentTag extends SimpleTagSupport {
    
    private static final String KEY_CONTEXT_NAME = "contenttag.context.name";
    private static final String DEFAULT_CONTEXT_NAME = "context";

    private String contextName;
    private String property;
    private String variable;

    /** String setter for the tag attribute 'property'. */
    public void setProperty(String property) {
        this.property = property;
    }

    /** Setter for the tag attribute 'context'. */
    public void setContext(String contextName) {
        this.contextName = contextName;
    }

    /** Setter for the tag attribute 'var'. */
    public void setVar(String variable) {
        this.variable = variable;
    }

    @Override
    public void doTag() throws JspException {
        
        PageContext pageContext = (PageContext) this.getJspContext(); 
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

        // get context from request or page context
        String contextName = getContextName(request);
        Context context = (Context) request.getAttribute(contextName);

        if (context == null) {
            context = (Context) pageContext.getAttribute(contextName);
        }
        
        // need it!
        if (context == null) {
            return;
        }

        // get, check and write property
        Object property = context.get(this.property);
        
        if (property == null) {
            return;
        }    
            
        // mustn't be a (sub) context
        if (property instanceof Context) {
            throw new JspException("Object gotten from " + context.getLocation() 
                 + " by property " + this.property + " is not a property but a Context");
        }
            
        String propertyAsString = new PropertyFormatter(request).format(property);

        try {

            // normally, write out
            if (this.variable == null) {
                pageContext.getOut().append(propertyAsString);
            }
            
            // ..or set as request attribute if given
            else {
                request.setAttribute(variable, propertyAsString);
            }
        } catch (IOException ioe) {
            throw new JspException(ioe);
        }
    }
    
    private String getContextName(HttpServletRequest request) {
        
        // lazy, or (first) set by setter
        if (this.contextName == null) {

            // second by configuration
            this.contextName = HSTConfiguration.get(request.getSession().getServletContext(), 
                    KEY_CONTEXT_NAME, false/*not required*/);
        
            // third by default
            if (this.contextName == null) {
                this.contextName = DEFAULT_CONTEXT_NAME;    
            }
        }
        
        return this.contextName;
    }
}
