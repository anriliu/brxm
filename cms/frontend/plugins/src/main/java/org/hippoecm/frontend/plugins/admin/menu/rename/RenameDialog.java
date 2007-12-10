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
package org.hippoecm.frontend.plugins.admin.menu.rename;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.wicket.model.PropertyModel;
import org.hippoecm.frontend.UserSession;
import org.hippoecm.frontend.dialog.AbstractDialog;
import org.hippoecm.frontend.dialog.DialogWindow;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.plugin.JcrEvent;
import org.hippoecm.frontend.plugin.PluginEvent;
import org.hippoecm.frontend.widgets.TextFieldWidget;

public class RenameDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    /**
     * The name of the current node represented in the dialog
     */
    private String name;

    public RenameDialog(DialogWindow dialogWindow) {
        super(dialogWindow);
        dialogWindow.setTitle("Rename Node");

        JcrNodeModel nodeModel = dialogWindow.getNodeModel();
        try {
            // get name of current node
            name = nodeModel.getNode().getName();
        } catch (RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        add(new TextFieldWidget("name", new PropertyModel(this, "name")));
        if (nodeModel.getNode() == null) {
            ok.setVisible(false);
        }
    }

    @Override
    protected PluginEvent ok() throws RepositoryException {
        JcrNodeModel nodeModel = dialogWindow.getNodeModel();

        PluginEvent result;
        if (nodeModel.getParentModel() != null) {
            JcrNodeModel parentModel = nodeModel.getParentModel();            
            
            //The actual JCR move
            String oldPath = nodeModel.getNode().getPath();
            String newPath = parentModel.getNode().getPath();
            if (!newPath.endsWith("/")) {
                newPath += "/";
            }
            newPath += getName();
            Session jcrSession = ((UserSession) getSession()).getJcrSession();
            jcrSession.move(oldPath, newPath);
            
            JcrNodeModel newNodeModel = new JcrNodeModel(parentModel, parentModel.getNode().getNode(getName()));
            result = new PluginEvent(getOwningPlugin(), JcrEvent.NEW_MODEL, newNodeModel);
            result.chainEvent(JcrEvent.NEEDS_RELOAD, parentModel);
        } else {
            result = new PluginEvent(getOwningPlugin(), JcrEvent.NEW_MODEL, nodeModel);
        }
        return result;
    }

    @Override
    protected void cancel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
