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
package org.hippoecm.repository.jackrabbit;

import org.apache.jackrabbit.core.config.WorkspaceConfig;
import org.apache.jackrabbit.core.state.LocalItemStateManager;
import org.apache.jackrabbit.core.state.SharedItemStateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XAWorkspaceImpl extends org.apache.jackrabbit.core.XAWorkspace {
    private static Logger log = LoggerFactory.getLogger(XAWorkspaceImpl.class);

    protected XAWorkspaceImpl(WorkspaceConfig wspConfig, SharedItemStateManager stateMgr,
            org.apache.jackrabbit.core.RepositoryImpl rep, XASessionImpl session) {
        super(wspConfig, stateMgr, rep, session);
        ((RepositoryImpl)rep).initializeLocalItemStateManager((HippoLocalItemStateManager)this.stateMgr, session);
    }

    protected LocalItemStateManager createItemStateManager(SharedItemStateManager shared) {
        return new HippoLocalItemStateManager(shared, this, rep.getItemStateCacheFactory(), ((RepositoryImpl)rep).getNodeTypeRegistry());
    }
}
