package org.hippoecm.frontend.plugins.reviewedactions;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.hippoecm.frontend.dialog.DialogWindow;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.plugin.WorkflowPlugin;
import org.hippoecm.frontend.plugins.reviewedactions.authordialogs.cancelrequest.CancelRequestDialog;
import org.hippoecm.repository.api.WorkflowDescriptor;
import org.hippoecm.repository.api.WorkflowManager;
import org.hippoecm.repository.reviewedactions.RequestWorkflow;
import org.hippoecm.repository.reviewedactions.AuthorRequestWorkflow;

public class AuthorRequestWorkflowPlugin extends WorkflowPlugin {
    private static final long serialVersionUID = 1L;

    public AuthorRequestWorkflowPlugin(String id, final JcrNodeModel model, WorkflowManager workflowManager,
            WorkflowDescriptor workflowDescriptor) {
        super(id, model, workflowManager, workflowDescriptor);

        final DialogWindow cancelRequestDialog = new DialogWindow("cancelRequest-dialog", model, false);
        cancelRequestDialog.setPageCreator(new ModalWindow.PageCreator() {
            private static final long serialVersionUID = 1L;
            public Page createPage() {
                return new CancelRequestDialog(cancelRequestDialog, model, (AuthorRequestWorkflow) getWorkflow());
            }
        });
        add(cancelRequestDialog);
        add(cancelRequestDialog.dialogLink("cancelRequest"));
    }

    public void update(AjaxRequestTarget target, JcrNodeModel model) {
        // TODO Auto-generated method stub

    }

}
