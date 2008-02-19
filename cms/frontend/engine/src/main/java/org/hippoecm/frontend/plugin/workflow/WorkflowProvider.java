/*
 * Copyright 2008 Hippo
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
package org.hippoecm.frontend.plugin.workflow;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.wicket.Session;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.hippoecm.frontend.session.UserSession;
import org.hippoecm.repository.api.HippoNodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowProvider implements IDataProvider {
    private static final long serialVersionUID = 1L;

    static final Logger log = LoggerFactory.getLogger(WorkflowProvider.class);

    private List<String> workflows;

    public WorkflowProvider() {
        UserSession session = (UserSession) Session.get();
        workflows = new LinkedList<String>();
        try {
            Node wflsNode = session.getJcrSession().getRootNode().getNode(
                    HippoNodeType.CONFIGURATION_PATH + "/" + HippoNodeType.WORKFLOWS_PATH);
            NodeIterator iterator = wflsNode.getNodes();
            while (iterator.hasNext()) {
                workflows.add(iterator.nextNode().getName());
            }
        } catch (RepositoryException ex) {
            log.error(ex.getMessage());
        }
    }

    public int size() {
        return workflows.size();
    }

    public IModel model(Object object) {
        return new WorkflowCategoryModel((String) object);
    }

    public Iterator iterator(int first, int count) {
        return workflows.subList(first, first + count).iterator();
    }

    public void detach() {
        // nada
    }

    class WorkflowCategoryModel extends Model {
        private static final long serialVersionUID = 1L;

        WorkflowCategoryModel(String category) {
            super(category);
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof WorkflowCategoryModel) {
                WorkflowCategoryModel otherModel = (WorkflowCategoryModel) other;
                if (otherModel.getObject() == null) {
                    return getObject() == null;
                }
                return otherModel.getObject().equals(getObject());
            }
            return false;
        }
    }
}
