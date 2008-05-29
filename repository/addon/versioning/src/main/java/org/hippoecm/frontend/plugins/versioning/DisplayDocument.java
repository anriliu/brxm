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
package org.hippoecm.frontend.plugins.versioning;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.wicket.IClusterable;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.sa.plugin.IPluginContext;
import org.hippoecm.frontend.sa.plugin.config.IPluginConfig;
import org.hippoecm.frontend.sa.service.render.RenderPlugin;
import org.hippoecm.repository.api.HippoNodeType;

public class DisplayDocument extends RenderPlugin {
    private static final long serialVersionUID = 1L;
    
    static Logger log = LoggerFactory.getLogger(DisplayDocument.class);
    private Label content;

    public DisplayDocument(IPluginContext context, IPluginConfig config) {
        super(context, config);

        add(content = new Label("content"));
    }

    private void writeObject(ObjectOutputStream out)
            throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        log = LoggerFactory.getLogger(DisplayDocument.class);
    }

    @Override
    public void onModelChanged() {
        super.onModelChanged();
        JcrNodeModel model = (JcrNodeModel)getModel();
        if (model!=null) {
            Node document = model.getNode();
            try {
                Item primaryItem = document;
                try {
                    if (document.isNodeType(HippoNodeType.NT_DOCUMENT)) {
                        do {
                            primaryItem = ((Node)primaryItem).getPrimaryItem();
                        } while (primaryItem.isNode());
                        String data = ((Property)primaryItem).getString();
                        content.setModel(new Model(data));
                    } else if (document.isNodeType("nt:frozenNode")) {
                        do {
                            String primaryType = ((Node)primaryItem).getProperty("jcr:frozenPrimaryType").getString();
                            NodeType nodeType = ((Node)primaryItem).getSession().getWorkspace().getNodeTypeManager().getNodeType(primaryType);
                            if (((Node)primaryItem).hasProperty(nodeType.getPrimaryItemName()))
                                primaryItem = ((Node)primaryItem).getProperty(nodeType.getPrimaryItemName());
                            else
                                primaryItem = ((Node)primaryItem).getNode(nodeType.getPrimaryItemName());
                        } while (primaryItem.isNode());
                        String data = ((Property)primaryItem).getString();
                        content.setModel(new Model(data));
                    } else {
                        content.setModel(new Model("selected item is not a document"));
                    }
                } catch (ItemNotFoundException ex) {
                    content.setModel(new Model("selected item has no default content to display"));
                }
            } catch (RepositoryException ex) {
                System.err.println(ex.getClass().getName()+": "+ex.getMessage());
                ex.printStackTrace(System.err);
                content.setModel(new Model(""));
            }
        }
    }
}
