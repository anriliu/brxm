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
package org.hippoecm.frontend.plugins.reviewedactions.authordialogs.requestdepublication;

import org.hippoecm.frontend.dialog.DialogWindow;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.plugins.reviewedactions.dialogs.AbstractWorkflowDialog;
import org.hippoecm.repository.reviewedactions.AuthorReviewedActionsWorkflow;

public class RequestDePublicationDialog extends AbstractWorkflowDialog {
    private static final long serialVersionUID = 1L;

    private AuthorReviewedActionsWorkflow workflow;

    public RequestDePublicationDialog(final DialogWindow dialogWindow, JcrNodeModel model, AuthorReviewedActionsWorkflow workflow) {
        super(dialogWindow, model);
        dialogWindow.setTitle("Request de-publication");
        this.workflow = workflow;
        if (model.getNode() == null) {
            ok.setVisible(false);
        }
    }

    public void ok() throws Exception {
        workflow.requestDepublication();
        super.ok();
    }

    public void cancel() {
    }

}
