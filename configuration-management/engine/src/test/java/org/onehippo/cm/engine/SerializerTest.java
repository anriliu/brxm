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
package org.onehippo.cm.engine;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.onehippo.cm.api.ResourceInputProvider;
import org.onehippo.cm.api.model.Configuration;
import org.onehippo.cm.api.model.Module;
import org.onehippo.cm.api.model.Project;
import org.onehippo.cm.api.model.Source;
import org.onehippo.cm.engine.parser.ParserException;
import org.onehippo.cm.impl.model.ConfigDefinitionImpl;
import org.onehippo.cm.impl.model.ConfigurationImpl;
import org.onehippo.cm.impl.model.DefinitionNodeImpl;
import org.onehippo.cm.impl.model.ModuleImpl;
import org.onehippo.cm.impl.model.ProjectImpl;
import org.onehippo.cm.impl.model.SourceImpl;
import org.onehippo.cm.impl.model.ValueImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.onehippo.cm.engine.Constants.DEFAULT_EXPLICIT_SEQUENCING;

public class SerializerTest extends AbstractBaseTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void read_write_hierarchy_test() throws IOException, ParserException {
        readAndWrite("/parser/hierarchy_test/repo-config.yaml");
    }

    @Test
    public void read_write_value_test() throws IOException, ParserException {
        readAndWrite("/parser/value_test/repo-config.yaml");
    }

    @Test
    public void read_write_explicitly_sequenced() throws IOException, ParserException {
        readAndWrite("/parser/explicitly_sequenced_test/repo-config.yaml", true);
    }

    @Test
    public void read_write_not_explicitly_sequenced_test() throws IOException, ParserException {
        readAndWrite("/parser/not_explicitly_sequenced_test/repo-config.yaml", false);
    }

    private void readAndWrite(final String repoConfig) throws IOException, ParserException {
        readAndWrite(repoConfig, DEFAULT_EXPLICIT_SEQUENCING);
    }

    private void readAndWrite(final String repoConfig, final boolean explicitSequencing) throws IOException, ParserException {
        final PathConfigurationReader.ReadResult result = readFromResource(repoConfig, explicitSequencing);

        final FileConfigurationWriter writer = new FileConfigurationWriter(explicitSequencing);
        writer.write(folder.getRoot().toPath(), result.getConfigurations(), result.getModuleContexts());

        final Path expectedRoot = findBase(repoConfig);
        final Path actualRoot = folder.getRoot().toPath();
        final List<Path> expected = findFiles(expectedRoot);
        final List<Path> actual = findFiles(actualRoot);

        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(
                    new String(Files.readAllBytes(expectedRoot.resolve(expected.get(i)))),
                    new String(Files.readAllBytes(actualRoot.resolve(actual.get(i)))));
        }
    }

    private List<Path> findFiles(final Path root) throws IOException {
        final List<Path> paths = new ArrayList<>();
        final BiPredicate<Path, BasicFileAttributes> matcher = (filePath, fileAttr) -> fileAttr.isRegularFile();
        Files.find(root, Integer.MAX_VALUE, matcher).forEachOrdered((path) -> paths.add(root.relativize(path)));
        Collections.sort(paths);
        return paths;
    }

}
