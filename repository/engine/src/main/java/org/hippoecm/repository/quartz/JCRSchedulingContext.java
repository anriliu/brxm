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
package org.hippoecm.repository.quartz;

import javax.jcr.Session;

import org.quartz.core.SchedulingContext;

public class JCRSchedulingContext extends SchedulingContext {
    @SuppressWarnings("unused")
    private static final String SVN_ID = "$Id$";

    private Session session;

    public JCRSchedulingContext() {
        super();
    }

    public JCRSchedulingContext(Session session) {
        super();
        this.session = session;
    }

    public JCRSchedulingContext(SchedulingContext upstream, Session session) {
        super();
        setInstanceId(upstream.getInstanceId());
        this.session = session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }
}
