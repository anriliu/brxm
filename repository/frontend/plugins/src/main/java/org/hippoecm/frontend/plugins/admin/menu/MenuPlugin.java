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
package org.hippoecm.frontend.plugins.admin.menu;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
import org.hippoecm.frontend.dialog.DialogWindow;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.plugin.Plugin;
import org.hippoecm.frontend.plugins.admin.menu.delete.DeleteDialog;
import org.hippoecm.frontend.plugins.admin.menu.export.ExportDialog;
import org.hippoecm.frontend.plugins.admin.menu.move.MoveDialog;
import org.hippoecm.frontend.plugins.admin.menu.node.NodeDialog;
import org.hippoecm.frontend.plugins.admin.menu.property.PropertyDialog;
import org.hippoecm.frontend.plugins.admin.menu.rename.RenameDialog;
import org.hippoecm.frontend.plugins.admin.menu.reset.ResetDialog;
import org.hippoecm.frontend.plugins.admin.menu.save.SaveDialog;

public class MenuPlugin extends Plugin {
    private static final long serialVersionUID = 1L;

    public MenuPlugin(String id, final JcrNodeModel model) {
        super(id, model);

        final DialogWindow nodeDialog = new DialogWindow("node-dialog", model, false);
        nodeDialog.setPageCreator(new ModalWindow.PageCreator() {
            private static final long serialVersionUID = 1L;
            public Page createPage() {
                return new NodeDialog(nodeDialog, model);
            }
        });
        add(nodeDialog);
        add(nodeDialog.dialogLink("node-dialog-link"));

        final DialogWindow deleteDialog = new DialogWindow("delete-dialog", model, false);
        deleteDialog.setPageCreator(new ModalWindow.PageCreator() {
            private static final long serialVersionUID = 1L;
            public Page createPage() {
                return new DeleteDialog(deleteDialog, model);
            }
        });
        add(deleteDialog);
        add(deleteDialog.dialogLink("delete-dialog-link"));
        
        final DialogWindow moveDialog = new DialogWindow("move-dialog", model, false);
        moveDialog.setPageCreator(new ModalWindow.PageCreator() {
            private static final long serialVersionUID = 1L;
            public Page createPage() {
                return new MoveDialog(moveDialog, model);
            }
        });
        add(moveDialog);
        add(moveDialog.dialogLink("move-dialog-link"));

        final DialogWindow renameDialog = new DialogWindow("rename-dialog", model, false);
        renameDialog.setPageCreator(new ModalWindow.PageCreator() {
            private static final long serialVersionUID = 1L;
            public Page createPage() {
                return new RenameDialog(renameDialog, model);
            }
        });
        add(renameDialog);
        add(renameDialog.dialogLink("rename-dialog-link"));
        
        final DialogWindow exportDialog = new DialogWindow("export-dialog", model, false);
        exportDialog.setPageCreator(new ModalWindow.PageCreator() {
            private static final long serialVersionUID = 1L;
            public Page createPage() {
                return new ExportDialog(exportDialog, model);
            }
        });
        add(exportDialog);
        add(exportDialog.dialogLink("export-dialog-link"));

        final DialogWindow propertyDialog = new DialogWindow("property-dialog", model, false);
        propertyDialog.setPageCreator(new ModalWindow.PageCreator() {
            private static final long serialVersionUID = 1L;
            public Page createPage() {
                return new PropertyDialog(propertyDialog, model);
            }
        });
        add(propertyDialog);
        add(propertyDialog.dialogLink("property-dialog-link"));

        final DialogWindow saveDialog = new DialogWindow("save-dialog", model, false);
        saveDialog.setPageCreator(new ModalWindow.PageCreator() {
            private static final long serialVersionUID = 1L;
            public Page createPage() {
                return new SaveDialog(saveDialog, model);
            }
        });
        add(saveDialog);
        add(saveDialog.dialogLink("save-dialog-link"));

        final DialogWindow resetDialog = new DialogWindow("reset-dialog", model, false);
        resetDialog.setPageCreator(new ModalWindow.PageCreator() {
            private static final long serialVersionUID = 1L;
            public Page createPage() {
                return new ResetDialog(resetDialog, model);
            }
        });
        add(resetDialog);
        add(resetDialog.dialogLink("reset-dialog-link"));

        add(new Label("path", new PropertyModel(model, "path")));
    }

    public void update(AjaxRequestTarget target, JcrNodeModel model) {
        if (model != null) {
            setModel(model);
        }
        if (target != null) {
            target.addComponent(this);
        }
    }

}
