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
package org.hippoecm.frontend.plugins.console.menu.node;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.wicket.model.PropertyModel;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.plugins.console.menu.MenuPlugin;
import org.hippoecm.frontend.sa.dialog.AbstractDialog;
import org.hippoecm.frontend.sa.dialog.IDialogService;
import org.hippoecm.frontend.sa.plugin.IPluginContext;
import org.hippoecm.frontend.sa.service.ITitleDecorator;
import org.hippoecm.frontend.widgets.TextFieldWidget;

public class NodeDialog extends AbstractDialog implements ITitleDecorator{
    private static final long serialVersionUID = 1L;

    private String name;
    private String type = "nt:unstructured";
    
    private MenuPlugin plugin;

    public NodeDialog(MenuPlugin plugin, IPluginContext context, IDialogService dialogWindow) {
        super(context, dialogWindow);     
        this.plugin = plugin;
        
        add(new TextFieldWidget("name", new PropertyModel(this, "name")));
        add(new TextFieldWidget("type", new PropertyModel(this, "type")));
    }

    @Override
    public void ok() throws RepositoryException {
        JcrNodeModel nodeModel = (JcrNodeModel) plugin.getModel();
        Node node = nodeModel.getNode().addNode(getName(), getType());
        plugin.setModel(new JcrNodeModel(node));
        
//        Channel channel = getChannel();
//        if(channel != null) {
//            Request request = channel.createRequest("flush", nodeModel);
//            channel.send(request);
//            request = channel.createRequest("select", newModel);
//            channel.send(request);
//        }
    }

    @Override
    public void cancel() {
    }
    

    public String getTitle() {
        return "Add a new Node";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
