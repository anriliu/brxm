/*
 *  Copyright 2016-2017 Hippo B.V. (http://www.onehippo.com)
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
package org.onehippo.cm.impl.model.builder;

import org.junit.Before;
import org.onehippo.cm.impl.model.ConfigurationImpl;

public abstract class AbstractBaseTest {

    protected ConfigurationImpl configuration1;
    protected ConfigurationImpl configuration2;
    protected ConfigurationImpl configuration3;

    @Before
    public void setup() {

        configuration1 = new ConfigurationImpl();
        configuration2 = new ConfigurationImpl();
        configuration3 = new ConfigurationImpl();

        configuration1.setName("configuration1");
        configuration2.setName("configuration2");
        configuration3.setName("configuration3");
    }
}
