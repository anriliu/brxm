/*
 * Copyright 2007 Hippo
 *
 * Licensed under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hippoecm.frontend.plugins.cms.browse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.nodetype.NodeType;

import org.apache.wicket.IClusterable;
import org.hippoecm.frontend.plugin.parameters.ParameterValue;
import org.hippoecm.repository.api.HippoNodeType;

/**
 * @deprecated use org.hippoecm.frontend.plugins.cms.browse.sa.* instead
 */
@Deprecated
public class FolderTreeConfig implements IClusterable {
    private static final long serialVersionUID = 1L;

    // hardcoded ignore path set
    private Set<String> ignorePaths;

    // ignore nodes below these types
    private String[] ignoreNodesBelowType = new String[] { HippoNodeType.NT_DOCUMENT, HippoNodeType.NT_NAMESPACE };

    // ignore nodes below these types
    private String[] ignoreNodesBelowPath;

    // shortcut paths shown as root folders + the name how to show them
    private Map<String, String> shortCutInfo;

    public FolderTreeConfig(Map<String, ParameterValue> parameters) {
        shortCutInfo = new HashMap<String, String>();
        shortCutInfo.put("/hippo:namespaces", "document types");

        ignoreNodesBelowPath = new String[] { };

        ignorePaths = new HashSet<String>(Arrays.asList(new String[] { "/jcr:system", "/hippo:configuration",
                "/hippo:namespaces" }));
        List<String> ignored = parameters.get("ignored").getStrings();
        for (String path : ignored) {
            ignorePaths.add(path);
        }
    }

    public String getShortcut(String path) {
        if (shortCutInfo.containsKey(path)) {
            return shortCutInfo.get(path);
        }
        return null;
    }

    public boolean isNodeTypeIgnored(NodeType nt) {
        for (String type : ignoreNodesBelowType) {
            if (nt.isNodeType(type)) {
                return true;
            }
        }
        return false;
    }

    public boolean isPathIgnored(String nodePath) {
        for (String path : ignorePaths) {
            if (nodePath.equals(path)) {
                return true;
            }
        }
        return false;
    }

    public boolean areChildrenIgnored(String nodePath) {
        for (String path : ignoreNodesBelowPath) {
            if (nodePath.equals(path)) {
                return true;
            }
        }
        return false;
    }

    public Set<String> getShortcuts() {
        return shortCutInfo.keySet();
    }
}
