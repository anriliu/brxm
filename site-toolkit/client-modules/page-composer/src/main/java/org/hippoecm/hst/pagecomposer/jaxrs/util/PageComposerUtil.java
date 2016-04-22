/*
 * Copyright 2016 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.hippoecm.hst.pagecomposer.jaxrs.util;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.hippoecm.hst.core.parameters.Parameter;
import org.hippoecm.hst.core.parameters.ParametersInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PageComposerUtil {
    private static Logger log = LoggerFactory.getLogger(PageComposerUtil.class);
    public static final String HST_COMPONENTCLASSNAME = "hst:componentclassname";

    private PageComposerUtil() {}

    /**
     * Returns the {@link Map} of annotated parameter name as key and annotated default value as value. Parameters with
     * empty default value are also represented in the returned map.
     *
     * @param node the current container item node
     * @return the Map of all {@link Parameter} names and their default value
     */
    public static Map<String, String> getAnnotatedDefaultValues(Node node) {
        try {
            String componentClassName = null;
            if (node.hasProperty(HST_COMPONENTCLASSNAME)) {
                componentClassName = node.getProperty(HST_COMPONENTCLASSNAME).getString();
            }

            if (componentClassName != null) {
                Class<?> componentClass = Thread.currentThread().getContextClassLoader().loadClass(componentClassName);
                if (componentClass.isAnnotationPresent(ParametersInfo.class)) {
                    ParametersInfo parametersInfo = componentClass.getAnnotation(ParametersInfo.class);
                    Class<?> classType = parametersInfo.type();
                    if (classType == null) {
                        return Collections.emptyMap();
                    }
                    Map<String, String> result = new HashMap<String, String>();
                    for (Method method : classType.getMethods()) {
                        if (method.isAnnotationPresent(Parameter.class)) {
                            Parameter annotation = method.getAnnotation(Parameter.class);
                            result.put(annotation.name(), annotation.defaultValue());
                        }
                    }
                    return result;
                }
            }
        } catch (RepositoryException e) {
            log.error("Failed to load annotated default values", e);
        } catch (ClassNotFoundException e) {
            log.error("Failed to load annotated default values", e);
        }
        return Collections.emptyMap();
    }
}
