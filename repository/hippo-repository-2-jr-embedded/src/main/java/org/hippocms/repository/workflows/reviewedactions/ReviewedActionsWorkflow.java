/*
 * Copyright 2007 Hippo.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hippocms.repository.workflows.reviewedactions;

import java.util.Date;
import org.hippocms.repository.model.Document;
import org.hippocms.repository.model.Workflow;

public class ReviewedActionsWorkflow implements Workflow {
    private Document document;
    private PublicationRequest pendingPublicationRequest;

    public ReviewedActionsWorkflow(Document document) {
        super();

        this.document = document;
    }

    public void requestPublication(Date publicationDate, Date unpublicationDate) {
        if (hasPendingRequest()) {
            throw new IllegalStateException("Cannot start a request when a request is pending");
        }
        pendingPublicationRequest = new PublicationRequest(publicationDate, unpublicationDate);
    }

    public PublicationRequest getPendingPublicationRequest() {
        return pendingPublicationRequest;
    }

    private boolean hasPendingRequest() {
        return pendingPublicationRequest != null;
    }
}
