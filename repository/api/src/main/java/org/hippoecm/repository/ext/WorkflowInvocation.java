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
package org.hippoecm.repository.ext;

import java.io.Externalizable;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.hippoecm.repository.api.WorkflowException;

/**
 * 
 * @author berry
 */
public interface WorkflowInvocation extends Externalizable {
    /**
     * 
     */

    /**
     * 
     * @param session
     * @return
     * @throws javax.jcr.RepositoryException
     * @throws org.hippoecm.repository.api.WorkflowException
     */
    public Object invoke(Session session) throws RepositoryException, WorkflowException;

    /**
     * 
     * @return
     */
    public Node getSubject();

    /**
     * 
     * @param node
     */
    public void setSubject(Node node);
}
