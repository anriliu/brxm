/**
 * Copyright 2013 Hippo B.V. (http://www.onehippo.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onehippo.repository.documentworkflow;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.scxml2.ErrorReporter;
import org.apache.commons.scxml2.EventDispatcher;
import org.apache.commons.scxml2.SCInstance;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.TriggerEvent;
import org.apache.commons.scxml2.model.ModelException;
import org.hippoecm.repository.api.Document;
import org.hippoecm.repository.api.MappingException;
import org.hippoecm.repository.api.WorkflowException;
import org.hippoecm.repository.standardworkflow.DefaultWorkflow;
import org.hippoecm.repository.util.JcrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom action for archiving document.
 */
public class ArchiveAction extends AbstractDocumentAction {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(ArchiveAction.class);

    @Override
    protected void doExecute(EventDispatcher evtDispatcher, ErrorReporter errRep, SCInstance scInstance, Log appLog,
            Collection<TriggerEvent> derivedEvents) throws ModelException, SCXMLExpressionException,
            RepositoryException {

        DocumentHandle handle = getDocumentHandle(scInstance);

        if (handle.getDraft() != null) {
            deleteDocument(handle.getDraft());
        }

        if (handle.getPublished() != null) {
            deleteDocument(handle.getPublished());
        }

        try {
            DefaultWorkflow defaultWorkflow = (DefaultWorkflow) getWorkflowContext(scInstance).getWorkflow("core", handle.getUnpublished());
            defaultWorkflow.archive();
        } catch (MappingException ex) {
            log.warn("invalid default workflow, falling back in behaviour", ex);
        } catch (WorkflowException ex) {
            log.warn("no default workflow for published documents, falling back in behaviour", ex);
        } catch (RepositoryException ex) {
            log.warn("exception trying to archive document, falling back in behaviour", ex);
        } catch (RemoteException ex) {
            log.warn("exception trying to archive document, falling back in behaviour", ex);
        }
    }

    protected void deleteDocument(Document document) throws RepositoryException {
        JcrUtils.ensureIsCheckedOut(document.getNode());
        JcrUtils.ensureIsCheckedOut(document.getNode().getParent());
        document.getNode().remove();
    }

}
