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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jackrabbit.core.NodeId;
import org.apache.jackrabbit.core.state.ItemStateManager;
import org.apache.jackrabbit.core.state.SessionItemStateManager;
import org.apache.jackrabbit.core.state.LocalItemStateManager;

import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.core.state.PropertyState;
import org.apache.jackrabbit.core.state.ItemState;
import org.apache.jackrabbit.name.QName;

class HippoSessionItemStateManager extends SessionItemStateManager {
    HippoSessionItemStateManager(NodeId rootNodeId, LocalItemStateManager manager, SessionImpl session) {
        super(rootNodeId, manager, session);
    }

    HippoSessionItemStateManager(NodeId rootNodeId, LocalItemStateManager manager, XASessionImpl session) {
        super(rootNodeId, manager, session);
    }

    public NodeState createNew(NodeState transientState) throws IllegalStateException {
        return super.createNew(transientState);
    }

    public PropertyState createNew(QName propName, NodeId parentId) throws IllegalStateException {
        return super.createNew(propName, parentId);
    }

    public PropertyState createNew(PropertyState transientState) throws IllegalStateException {
        return super.createNew(transientState);
    }

    public void store(ItemState state) throws IllegalStateException {
        super.store(state);
    }
}
