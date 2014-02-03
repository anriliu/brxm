/*
 * Copyright 2013 Hippo B.V. (http://www.onehippo.com)
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
 */

package org.onehippo.cms7.essentials.dashboard.utils.inject;

import javax.inject.Inject;

import org.junit.Test;
import org.onehippo.cms7.essentials.BaseTest;

import static org.junit.Assert.assertEquals;

/**
 * @version "$Id$"
 */
public class EventBusModuleTest extends BaseTest {


    @Inject
    private TestEventsListener testEventListener;

    @Test
    public void testConfigure() throws Exception {
        final TestEventsApplication application = testEventListener.getApplication();
        for (int i = 0; i < 100; i++) {
            application.postEvent();
        }
        assertEquals(100, application.getCounter());

    }


}
