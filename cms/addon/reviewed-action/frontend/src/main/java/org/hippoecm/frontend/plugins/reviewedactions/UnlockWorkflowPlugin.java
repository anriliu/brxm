/*
 *  Copyright 2008 Hippo.
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
package org.hippoecm.frontend.plugins.reviewedactions;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.model.StringResourceModel;
import org.hippoecm.addon.workflow.CompatibilityWorkflowPlugin;
import org.hippoecm.addon.workflow.WorkflowDescriptorModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.repository.api.Workflow;
import org.hippoecm.repository.api.WorkflowDescriptor;
import org.hippoecm.repository.reviewedactions.UnlockWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnlockWorkflowPlugin extends CompatibilityWorkflowPlugin {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(BasicReviewedActionsWorkflowPlugin.class);

    WorkflowAction unlockAction;

    public UnlockWorkflowPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);

        add(unlockAction = new WorkflowAction("unlock", new StringResourceModel("unlock", this, null).getString(), null) {

            @Override
            protected ResourceReference getIcon() {
                return new ResourceReference(getClass(), "unlock-16.png");
            }

            @Override
            protected String execute(Workflow wf) throws Exception {
                UnlockWorkflow workflow = (UnlockWorkflow) wf;
                workflow.unlock();
                return null;
            }
        });

        onModelChanged();
    }

    @Override
    public void onModelChanged() {
        super.onModelChanged();
        WorkflowDescriptorModel model = (WorkflowDescriptorModel) getDefaultModel();
        if (model != null) {
            try {
                Map<String, Serializable> hints = ((WorkflowDescriptor) model.getObject()).hints();
                if (hints.containsKey("unlock") && (hints.get("unlock") instanceof Boolean)
                        && !((Boolean) hints.get("unlock")).booleanValue()) {
                    unlockAction.setVisible(false);
                }
            } catch (RepositoryException ex) {
                // status unknown, maybe there are legit reasons for this, so don't emit a warning
                log.info(ex.getClass().getName() + ": " + ex.getMessage());
            }
        }
    }
}
