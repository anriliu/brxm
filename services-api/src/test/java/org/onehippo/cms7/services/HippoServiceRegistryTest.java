/*
 *  Copyright 2012-2016 Hippo B.V. (http://www.onehippo.com)
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
package org.onehippo.cms7.services;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("Duplicates")
public class HippoServiceRegistryTest {

    @SingletonService
    interface TestService {

        void doSomething();
    }

    interface AnotherInterface extends TestService {

    }

    @Test
    public void serviceIsRegistered() {
        TestService testService = new TestService() {
            @Override
            public void doSomething() {
            }
        };
        HippoServiceRegistry.registerService(testService, TestService.class);
        try {
            TestService service = HippoServiceRegistry.getService(TestService.class);
            assertNotNull(service);
            assertFalse(service instanceof AnotherInterface);
        } finally {
            HippoServiceRegistry.unregisterService(testService, TestService.class);
        }
    }

    @Test
    public void serviceIsRegisteredImplementingAnotherInterface() {
        TestService testService = new AnotherInterface() {
            @Override
            public void doSomething() {
            }
        };
        HippoServiceRegistry.registerService(testService, TestService.class);
        try {
            TestService service = HippoServiceRegistry.getService(TestService.class);
            assertNotNull(service);
            assertFalse(service instanceof AnotherInterface);
        } finally {
            HippoServiceRegistry.unregisterService(testService, TestService.class);
        }
    }

    @Test
    public void serviceIsRegisteredWithAdditionalInterface() {
        TestService testService = new AnotherInterface() {
            @Override
            public void doSomething() {
            }
        };
        HippoServiceRegistry.registerService(testService, new Class[]{TestService.class, AnotherInterface.class});
        try {
            TestService service = HippoServiceRegistry.getService(TestService.class);
            assertNotNull(service);
            assertTrue(service instanceof AnotherInterface);
        } finally {
            HippoServiceRegistry.unregisterService(testService, TestService.class);
        }
    }
}
