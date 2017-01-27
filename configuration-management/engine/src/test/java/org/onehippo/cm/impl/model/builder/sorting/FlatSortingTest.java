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
package org.onehippo.cm.impl.model.builder.sorting;

import java.util.SortedSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import org.junit.Test;
import org.onehippo.cm.api.model.Configuration;
import org.onehippo.cm.api.model.Module;
import org.onehippo.cm.api.model.Orderable;
import org.onehippo.cm.api.model.Project;
import org.onehippo.cm.impl.model.builder.AbstractBaseTest;

import static org.junit.Assert.assertEquals;

public class FlatSortingTest extends AbstractBaseTest {

    @Test
    public void configuration_sort_two_configurations() throws Exception {

        // config 1 depends on config 2
        configuration1.setAfter(ImmutableList.of(configuration2.getName()));

        SortedSet<Configuration> sorted = new Sorter<Configuration>().sort(ImmutableList.of(configuration1, configuration2));

        String sortedNames = sorted.stream().map((Function<Orderable, Object>)Orderable::getName).collect(Collectors.toList()).toString();
        assertEquals("[configuration2, configuration1]", sortedNames);

        SortedSet<Configuration> sorted2 = new Sorter<Configuration>().sort(ImmutableList.of(configuration2, configuration1));
        assertEquals(sorted, sorted2);
    }

    @Test
    public void configuration_sort_three_configurations() throws Exception {

        // config 1 depends on config 2
        configuration1.setAfter(ImmutableList.of(configuration2.getName()));
        // config 3 depends on config 1
        configuration3.setAfter(ImmutableList.of(configuration1.getName()));

        SortedSet<Configuration> sorted = new Sorter<Configuration>().sort(ImmutableList.of(configuration1, configuration2, configuration3));

        String sortedNames = sorted.stream().map((Function<Orderable, Object>)Orderable::getName).collect(Collectors.toList()).toString();
        assertEquals("[configuration2, configuration1, configuration3]", sortedNames);

        SortedSet<Configuration> sorted2 = new Sorter<Configuration>().sort(ImmutableList.of(configuration2, configuration3, configuration1));
        assertEquals(sorted, sorted2);
    }

    @Test
    public void configuration_sort_three_configurations_multiple_dependencies() throws Exception {

        // config 1 depends on config 2
        configuration1.setAfter(ImmutableList.of(configuration2.getName()));
        // config 3 depends on config 1 and config 2
        configuration3.setAfter(ImmutableList.of(configuration1.getName(), configuration2.getName()));

        SortedSet<Configuration> sorted = new Sorter<Configuration>().sort(ImmutableList.of(configuration1, configuration2, configuration3));

        String sortedNames = sorted.stream().map((Function<Orderable, Object>)Orderable::getName).collect(Collectors.toList()).toString();
        assertEquals("[configuration2, configuration1, configuration3]", sortedNames);

        SortedSet<Configuration> sorted2 = new Sorter<Configuration>().sort(ImmutableList.of(configuration2, configuration3, configuration1));
        assertEquals(sorted, sorted2);

    }

    @Test
    public void configuration_sort_three_undeterministic_depends_sorts_on_name() throws Exception {

        // config 1 depends on config 2
        configuration1.setAfter(ImmutableList.of(configuration2.getName()));
        // config 3 depends on config 2
        configuration3.setAfter(ImmutableList.of(configuration2.getName()));

        SortedSet<Configuration> sorted = new Sorter<Configuration>().sort(ImmutableList.of(configuration1, configuration2, configuration3));

        String sortedNames = sorted.stream().map((Function<Orderable, Object>)Orderable::getName).collect(Collectors.toList()).toString();
        assertEquals("[configuration2, configuration1, configuration3]", sortedNames);

        SortedSet<Configuration> sorted2 = new Sorter<Configuration>().sort(ImmutableList.of(configuration2, configuration3, configuration1));
        assertEquals(sorted, sorted2);
    }


    @Test
    public void project_sort_two_projects() throws Exception {

        project1a.setAfter(ImmutableList.of(project1b.getName()));

        SortedSet<Project> sorted = new Sorter().sort(ImmutableList.of(project1a, project1b));

        String sortedNames = sorted.stream().map((Function<Orderable, Object>)Orderable::getName).collect(Collectors.toList()).toString();
        assertEquals("[project1b, project1a]", sortedNames);

        SortedSet<Project> sorted2 = new Sorter<Project>().sort(ImmutableList.of(project1a, project1b));
        assertEquals(sorted, sorted2);
    }

    @Test
    public void project_sort_three_projects() throws Exception {

        project1a.setAfter(ImmutableList.of(project1b.getName()));
        project1c.setAfter(ImmutableList.of(project1a.getName()));

        SortedSet<Project> sorted = new Sorter<Project>().sort(ImmutableList.of(project1a, project1b, project1c));

        String sortedNames = sorted.stream().map((Function<Orderable, Object>)Orderable::getName).collect(Collectors.toList()).toString();
        assertEquals("[project1b, project1a, project1c]", sortedNames);

        SortedSet<Project> sorted2 = new Sorter<Project>().sort(ImmutableList.of(project1b, project1c, project1a));
        assertEquals(sorted, sorted2);
    }

    @Test
    public void project_sort_three_projects_multiple_dependencies() throws Exception {

        project1a.setAfter(ImmutableList.of(project1b.getName()));
        project1c.setAfter(ImmutableList.of(project1a.getName(), project1b.getName()));

        SortedSet<Project> sorted = new Sorter<Project>().sort(ImmutableList.of(project1a, project1b, project1c));

        String sortedNames = sorted.stream().map((Function<Orderable, Object>)Orderable::getName).collect(Collectors.toList()).toString();
        assertEquals("[project1b, project1a, project1c]", sortedNames);

        SortedSet<Project> sorted2 = new Sorter<Project>().sort(ImmutableList.of(project1b, project1c, project1a));
        assertEquals(sorted, sorted2);

    }

    @Test
    public void project_sort_three_undeterministic_depends_sorts_on_name() throws Exception {

        project1a.setAfter(ImmutableList.of(project1b.getName()));
        project1c.setAfter(ImmutableList.of(project1b.getName()));

        SortedSet<Project> sorted = new Sorter<Project>().sort(ImmutableList.of(project1a, project1b, project1c));

        String sortedNames = sorted.stream().map((Function<Orderable, Object>)Orderable::getName).collect(Collectors.toList()).toString();
        assertEquals("[project1b, project1a, project1c]", sortedNames);

        SortedSet<Project> sorted2 = new Sorter<Project>().sort(ImmutableList.of(project1b, project1c, project1a));
        assertEquals(sorted, sorted2);
    }

    @Test
    public void module_sort_two_modules() throws Exception {

        module1a1.setAfter(ImmutableList.of(module1a2.getName()));

        SortedSet<Module> sorted = new Sorter<Module>().sort(ImmutableList.of(module1a1, module1a2));

        String sortedNames = sorted.stream().map((Function<Orderable, Object>)Orderable::getName).collect(Collectors.toList()).toString();
        assertEquals("[module1a2, module1a1]", sortedNames);

        SortedSet<Module> sorted2 = new Sorter<Module>().sort(ImmutableList.of(module1a1, module1a2));
        assertEquals(sorted, sorted2);
    }

    @Test
    public void module_sort_three_modules() throws Exception {

        module1a1.setAfter(ImmutableList.of(module1a2.getName()));
        module1a3.setAfter(ImmutableList.of(module1a1.getName()));

        SortedSet<Module> sorted = new Sorter<Module>().sort(ImmutableList.of(module1a1, module1a2, module1a3));

        String sortedNames = sorted.stream().map((Function<Orderable, Object>)Orderable::getName).collect(Collectors.toList()).toString();
        assertEquals("[module1a2, module1a1, module1a3]", sortedNames);

        SortedSet<Module> sorted2 = new Sorter<Module>().sort(ImmutableList.of(module1a2, module1a3, module1a1));
        assertEquals(sorted, sorted2);
    }

    @Test
    public void module_sort_three_modules_multiple_dependencies() throws Exception {

        module1a1.setAfter(ImmutableList.of(module1a2.getName()));
        module1a3.setAfter(ImmutableList.of(module1a1.getName(), module1a2.getName()));

        SortedSet<Module> sorted = new Sorter<Module>().sort(ImmutableList.of(module1a1, module1a2, module1a3));

        String sortedNames = sorted.stream().map((Function<Orderable, Object>)Orderable::getName).collect(Collectors.toList()).toString();
        assertEquals("[module1a2, module1a1, module1a3]", sortedNames);

        SortedSet<Module> sorted2 = new Sorter<Module>().sort(ImmutableList.of(module1a2, module1a3, module1a1));
        assertEquals(sorted, sorted2);

    }

    @Test
    public void module_sort_three_undeterministic_depends_sorts_on_name() throws Exception {

        module1a1.setAfter(ImmutableList.of(module1a2.getName()));
        module1a3.setAfter(ImmutableList.of(module1a2.getName()));

        SortedSet<Module> sorted = new Sorter<Module>().sort(ImmutableList.of(module1a1, module1a2, module1a3));

        String sortedNames = sorted.stream().map((Function<Orderable, Object>)Orderable::getName).collect(Collectors.toList()).toString();
        assertEquals("[module1a2, module1a1, module1a3]", sortedNames);

        SortedSet<Module> sorted2 = new Sorter<Module>().sort(ImmutableList.of(module1a2, module1a3, module1a1));
        assertEquals(sorted, sorted2);
    }
}
