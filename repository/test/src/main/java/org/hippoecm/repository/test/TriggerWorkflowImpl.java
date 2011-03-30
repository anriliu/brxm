/*
 *  Copyright 2011 Hippo.
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
package org.hippoecm.repository.test;

import java.rmi.RemoteException;
import org.hippoecm.repository.api.Document;
import org.hippoecm.repository.api.MappingException;
import org.hippoecm.repository.api.WorkflowException;
import org.hippoecm.repository.ext.WorkflowImpl;
import org.hippoecm.repository.standardworkflow.TriggerWorkflow;

public class TriggerWorkflowImpl extends WorkflowImpl implements TriggerWorkflow {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    public static int fireCount = 0;

    public TriggerWorkflowImpl() throws RemoteException {
    }

    public void fire() throws WorkflowException, MappingException {
        ++fireCount;
    }

    public void fire(Document result) throws WorkflowException, MappingException {
        ++fireCount;
    }
}
