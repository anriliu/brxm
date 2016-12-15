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

import com.google.common.collect.ImmutableList;

import org.junit.Test;
import org.onehippo.cm.impl.model.ConfigurationImpl;

public class CircularDependencyTest extends AbstractBaseTest{


    @Test(expected = CircularDependencyException.class)
    public void self_circular_dependency() {
        // config 1 depends on config 1
        configuration1.setDependsOn(ImmutableList.of(configuration1.getName()));

        ConfigurationNodeBuilder builder = new ConfigurationNodeBuilder();
        builder.verifyDependencies(configuration1, configuration2);
    }

    @Test(expected = CircularDependencyException.class)
    public void two_wise_circular_dependency() {
        // config 1 depends on config 2
        configuration1.setDependsOn(ImmutableList.of(configuration2.getName()));
        // config 2 depends on config 1
        configuration2.setDependsOn(ImmutableList.of(configuration1.getName()));

        ConfigurationNodeBuilder builder = new ConfigurationNodeBuilder();
        builder.verifyDependencies(configuration1, configuration2);
    }

    @Test(expected = CircularDependencyException.class)
    public void three_wise_circular_dependency() {
        // config 1 depends on config 2
        configuration1.setDependsOn(ImmutableList.of(configuration2.getName()));
        // config 2 depends on config 3
        configuration2.setDependsOn(ImmutableList.of(configuration3.getName()));
        // config 3 depends on config 1
        configuration3.setDependsOn(ImmutableList.of(configuration1.getName()));

        ConfigurationNodeBuilder builder = new ConfigurationNodeBuilder();
        builder.verifyDependencies(configuration1, configuration2, configuration3);
    }

    @Test(expected = CircularDependencyException.class)
    public void complex_multiple_circular_dependencies() {
        // this test is to assure the verifyDependencies does not by accident loops forever

        // config 2 depends on config 1
        configuration2.setDependsOn(ImmutableList.of(configuration1.getName()));
        // config 3 depends on config 2
        configuration3.setDependsOn(ImmutableList.of(configuration2.getName()));
        // config 1 depends on config 3
        configuration1.setDependsOn(ImmutableList.of(configuration3.getName()));

        // extra circle
        ConfigurationImpl configuration2a = new ConfigurationImpl();
        ConfigurationImpl configuration2b = new ConfigurationImpl();

        configuration2a.setName("configuration2a");
        configuration2b.setName("configuration2b");

        configuration2.setDependsOn(ImmutableList.of(configuration2a.getName()));
        configuration2a.setDependsOn(ImmutableList.of(configuration2b.getName()));
        configuration2b.setDependsOn(ImmutableList.of(configuration2.getName()));

        ConfigurationNodeBuilder builder = new ConfigurationNodeBuilder();
        builder.verifyDependencies(configuration1, configuration2, configuration3, configuration2a, configuration2b);
    }
}
