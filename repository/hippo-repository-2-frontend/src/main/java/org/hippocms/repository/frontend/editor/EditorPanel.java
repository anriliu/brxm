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
package org.hippocms.repository.frontend.editor;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.hippocms.repository.frontend.IUpdatable;
import org.hippocms.repository.frontend.model.JcrNodeModel;

public class EditorPanel extends Panel implements IUpdatable {
    private static final long serialVersionUID = 1L;

    private NodeEditor editor;

    public EditorPanel(String id, JcrNodeModel model) {
        super(id);
        setOutputMarkupId(true);
        editor = new NodeEditor("editor", model);
        add(editor);
    }

    public void update(AjaxRequestTarget target, JcrNodeModel model) {
        if (model != null) {
            JcrNodeModel editorNodeModel = (JcrNodeModel) editor.getModel();
            editorNodeModel.setNode(model.getNode());
        }
        if (target != null) {
            target.addComponent(editor);
        }
    }
}
