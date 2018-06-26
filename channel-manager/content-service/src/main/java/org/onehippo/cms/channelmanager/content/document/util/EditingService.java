/*
 * Copyright 2018 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.onehippo.cms.channelmanager.content.document.util;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import javax.jcr.Node;
import javax.jcr.Session;

import org.hippoecm.repository.standardworkflow.EditableWorkflow;

public interface EditingService {

    /**
     * Get a backing JCR node of a document.
     *
     * @param editableWorkflow workflow for the desired document
     * @param hints            the hints obtained earlier from the workflow
     * @param session          JCR session for obtaining the backing node
     * @return JCR node or nothing, wrapped in an Optional
     */
    Optional<Node> getEditableDocumentNode(EditableWorkflow editableWorkflow, Map<String, Serializable> hints, Session session);
}
