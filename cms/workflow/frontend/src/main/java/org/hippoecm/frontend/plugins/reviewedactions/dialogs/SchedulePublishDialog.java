/*
 *  Copyright 2010-2015 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.frontend.plugins.reviewedactions.dialogs;

import java.util.Date;

import javax.jcr.Node;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.value.IValueMap;
import org.hippoecm.addon.workflow.AbstractWorkflowDialogRestyling;
import org.hippoecm.addon.workflow.IWorkflowInvoker;
import org.hippoecm.frontend.dialog.Dialog;
import org.hippoecm.frontend.dialog.DialogConstants;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.plugins.reviewedactions.UnpublishedReferenceNodeProvider;
import org.hippoecm.frontend.plugins.reviewedactions.model.ReferenceProvider;
import org.hippoecm.frontend.plugins.reviewedactions.model.UnpublishedReferenceProvider;
import org.hippoecm.frontend.plugins.standards.list.resolvers.CssClass;
import org.hippoecm.frontend.service.IEditorManager;

public class SchedulePublishDialog extends AbstractWorkflowDialogRestyling<Node> {

    public SchedulePublishDialog(IWorkflowInvoker action, JcrNodeModel nodeModel, IModel<Date> dateModel,
            IEditorManager editorMgr) {
        super(nodeModel, action);

        UnpublishedReferenceNodeProvider provider = new UnpublishedReferenceNodeProvider(
                new UnpublishedReferenceProvider(new ReferenceProvider(nodeModel)));
        add(new UnpublishedReferencesView("links", provider, editorMgr));

        addOrReplace(new DatePickerComponent(Dialog.BOTTOM_LEFT_ID, dateModel, new ResourceModel("schedule-publish-text")));

        add(CssClass.append("hippo-window"));
        add(CssClass.append("hippo-workflow-dialog"));

        setFocusOnCancel();
    }

    @Override
    public IModel<String> getTitle() {
        return new StringResourceModel("schedule-publish-title", this, null);
    }

    @Override
    public IValueMap getProperties() {
        return DialogConstants.LARGE_AUTO;
    }
}
