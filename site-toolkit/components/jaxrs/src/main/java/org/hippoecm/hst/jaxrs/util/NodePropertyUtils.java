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
package org.hippoecm.hst.jaxrs.util;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.hippoecm.hst.jaxrs.model.content.NodeProperty;
import org.hippoecm.hst.jaxrs.model.content.PropertyValue;

/**
 * NodePropertyUtils
 * @version $Id$
 */
public class NodePropertyUtils {
    
    private NodePropertyUtils() {
        
    }
    
    public static void setProperty(Node contentNode, NodeProperty nodeProperty) throws RepositoryException {
        int type = nodeProperty.getType();
        boolean multiple = nodeProperty.getMultiple();
        PropertyValue [] propertyValues = nodeProperty.getValues();
        
        if (!multiple) {
            String value = ((propertyValues != null && propertyValues.length > 0) ? propertyValues[0].getValue() : null);
            contentNode.setProperty(nodeProperty.getName(), value, type);
        } else {
            String [] values = new String[propertyValues.length];
            
            for (int i = 0; i < propertyValues.length; i++) {
                values[i] = propertyValues[i].getValue();
            }
            
            contentNode.setProperty(nodeProperty.getName(), values, type);
        }
    }
    
}
