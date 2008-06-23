/*
 * Copyright 2008 Hippo
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
package org.hippoecm.frontend.plugins.standardworkflow;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.apache.wicket.markup.html.basic.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugin.workflow.AbstractWorkflowPlugin;

public class FolderEmbeddedWorkflowPlugin extends AbstractWorkflowPlugin {

    private static final long serialVersionUID = 1L;
    transient Logger log = LoggerFactory.getLogger(FolderWorkflowPlugin.class);

    public FolderEmbeddedWorkflowPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);
        add(new Label("delete-dialog", "delete"));
        add(new Label("move-dialog", "move"));
        add(new Label("rename-dialog", "rename"));
        add(new Label("duplicate-dialog", "duplicate"));
    }

    private void writeObject(ObjectOutputStream out)
            throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        log = LoggerFactory.getLogger(FolderEmbeddedWorkflowPlugin.class);
    }

}
