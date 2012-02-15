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
package org.hippoecm.frontend.plugins.cms.admin.groups;

import java.util.List;

import javax.jcr.RepositoryException;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbParticipant;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.hippoecm.frontend.dialog.IDialogService;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugins.cms.admin.AdminBreadCrumbPanel;
import org.hippoecm.frontend.plugins.cms.admin.HippoSecurityEventConstants;
import org.hippoecm.frontend.plugins.cms.admin.widgets.AjaxLinkLabel;
import org.hippoecm.frontend.plugins.cms.admin.widgets.ConfirmDeleteDialog;
import org.hippoecm.frontend.plugins.standards.panelperspective.breadcrumb.PanelPluginBreadCrumbLink;
import org.hippoecm.frontend.session.UserSession;
import org.onehippo.cms7.event.HippoEvent;
import org.onehippo.cms7.services.HippoServiceRegistry;
import org.onehippo.cms7.services.eventbus.HippoEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewGroupPanel extends AdminBreadCrumbPanel {
    @SuppressWarnings("unused")
    private static final String SVN_ID = "$Id$";
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(ViewGroupPanel.class);

    private final IModel model;

    public ViewGroupPanel(final String id, final IPluginContext context, final IBreadCrumbModel breadCrumbModel,
            final IModel model) {
        super(id, breadCrumbModel);
        setOutputMarkupId(true);
        
        this.model = model;
        final Group group = (Group) model.getObject();

        // common group properties
        add(new Label("groupname", new PropertyModel(model, "groupname")));
        add(new Label("description", new PropertyModel(model, "description")));

        // local memberships
        add(new Label("group-members-label", new ResourceModel("group-members-label")));
        add(new MembershipsListView("members", "member", new PropertyModel(group, "members")));

        // actions
        PanelPluginBreadCrumbLink edit = new PanelPluginBreadCrumbLink("edit-group", breadCrumbModel) {
            protected IBreadCrumbParticipant getParticipant(final String componentId) {
                return new EditGroupPanel(componentId, breadCrumbModel, model);
            }
        };
        edit.setVisible(!group.isExternal());
        add(edit);
        
        PanelPluginBreadCrumbLink members = new PanelPluginBreadCrumbLink("set-group-members", breadCrumbModel) {
            @Override
            protected IBreadCrumbParticipant getParticipant(final String componentId) {
                return new SetMembersPanel(componentId, breadCrumbModel, model);
            }
        };
        members.setVisible(!group.isExternal());
        add(members);

        add(new AjaxLinkLabel("delete-group", new ResourceModel("group-delete")) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                context.getService(IDialogService.class.getName(), IDialogService.class).show(
                        new ConfirmDeleteDialog(model, this) {
                            private static final long serialVersionUID = 1L;

                            @Override
                            protected void onOk() {
                                deleteGroup(model);
                            }

                            @Override
                            protected String getTitleKey() {
                                return "group-delete-title";
                            }

                            @Override
                            protected String getTextKey() {
                                return "group-delete-text";
                            }
                        });
            }
        });
    }
    
    private void deleteGroup(IModel model) {
        Group group = (Group) model.getObject();
        String groupname = group.getGroupname();
        try {
            group.delete();
            HippoEventBus eventBus = HippoServiceRegistry.getService(HippoEventBus.class);
            if (eventBus != null) {
                final UserSession userSession = UserSession.get();
                HippoEvent event = new HippoEvent(userSession.getApplicationName())
                        .user(userSession.getJcrSession().getUserID())
                        .action("delete-group")
                        .category(HippoSecurityEventConstants.CATEGORY_GROUP_MANAGEMENT)
                        .message("deleted group " + groupname);
                eventBus.post(event);
            }
            Session.get().info(getString("group-removed", model));
            // one up
            List<IBreadCrumbParticipant> l = getBreadCrumbModel().allBreadCrumbParticipants();
            getBreadCrumbModel().setActive(l.get(l.size() -2));
        } catch (RepositoryException e) {
            Session.get().warn(getString("group-remove-failed", model));
            log.error("Unable to delete group '" + groupname + "' : ", e);
        }
    }
    
    /** list view to be nested in the form. */
    private static final class MembershipsListView extends ListView {
        private static final long serialVersionUID = 1L;
        private String labelId;

        public MembershipsListView(final String id, final String labelId, IModel listModel) {
            super(id, listModel);
            this.labelId = labelId;
            setReuseItems(false);
        }

        protected void populateItem(ListItem item) {
            String user = (String) item.getModelObject();
            item.add(new Label(labelId, user));
        }
    }

    public IModel<String> getTitle(Component component) {
        return new StringResourceModel("group-view-title", component, model);
    }

}
