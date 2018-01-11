/*
 * Copyright 2014-2017 Hippo B.V. (http://www.onehippo.com)
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

package org.onehippo.cms7.essentials.dashboard.ctx;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Map;

import org.onehippo.cms7.essentials.dashboard.config.PluginConfigService;
import org.onehippo.cms7.essentials.dashboard.model.ProjectSettings;

/**
 * Plugin context is passed to all HippoEssentials plugins.
 *
 * @version "$Id$"
 */
public interface PluginContext extends Serializable {


    ProjectSettings getProjectSettings();

    void setProjectSettings(ProjectSettings projectSettings);

    /**
     * Returns root of the site directory
     *
     * @return directory of site module
     */
    File getSiteDirectory();

    /**
     * Returns root of the cms directory
     *
     * @return directory of cms module
     */
    File getCmsDirectory();


    /**
     * Returns root of the essentials project directory
     *
     * @return directory of essentials module
     */
    File getEssentialsDirectory();

    /**
     * Package name used for HippoBeans
     *
     * @return beans package name, might be null if not configured
     */
    String beansPackageName();

    /**
     * General package name used for the project
     *
     * @return project package name, might be null if not configured
     */
    String getProjectPackageName();

    /**
     * Package name used for REST components
     *
     * @return REST package name, might be null if not configured
     */
    String restPackageName();

    /**
     * Package name used for HstComponents
     *
     * @return HSt components package name, might be null if not configured
     */
    String getComponentsPackageName();

    /**
     * Sets HST components package name
     *
     * @param componentsPackage name of the components package e.g. {@code org.onehippo.myproject.components}
     */
    void setComponentsPackageName(String componentsPackage);

    /**
     * Path to component package (if all properties are defined like site dir and components package name)
     *
     * @return path to components package or null
     */
    Path getComponentsPackagePath();

    /**
     * Path to HST beans package (if all properties are defined, like site dir and beans package name)
     *
     * @return path to HST beans  package or null
     */
    Path getBeansPackagePath();

    /**
     * Path to root of HST beans
     *
     * @return path, defaults to site java class root (/site/src/main/java)
     */
    Path getBeansRootPath();

    /**
     * Path to HST REST package (if all properties are defined, like site dir and REST package name)
     *
     * @return path to HST REST  package or null
     */
    Path getRestPackagePath();

    /**
     * Sets HST beans package name
     *
     * @param beansPackage name of the beans package e.g. {@code org.onehippo.myproject.beans}
     */
    void setBeansPackageName(String beansPackage);

    /**
     * Sets the general project package name
     *
     * @param projectPackage name of the project package e.g. {@code org.onehippo.myproject}
     */

    void setProjectPackageName(String projectPackage);

    /**
     * Sets HST rest package name
     *
     * @param restPackage name of the beans package e.g. {@code org.onehippo.myproject.rest}
     */
    void setRestPackageName(String restPackage);

    /**
     * Namespace prefix of current project
     *
     * @return namespace prefix of current project
     */
    String getProjectNamespacePrefix();

    /**
     * Sets project namespace e.g. {@code myproject}
     *
     * @param namespace namespace prefix
     */
    void setProjectNamespacePrefix(String namespace);

    /**
     * Indicates that all project settings are set
     *
     * @return true if user did project setup, false otherwise.
     */
    boolean hasProjectSettings();

    /**
     * Returns service instance for given Plugin descriptor
     *
     * @return instance of PluginConfigService tide to Plugin descriptor
     * @see org.onehippo.cms7.essentials.dashboard.model.PluginDescriptor
     */
    PluginConfigService getConfigService();

    /**
     * Returns root of java files e.g. {@code /home/user/project/site/src/main/java}
     *
     * @return root of java file folder like {@code /home/user/project/site/src/main/java}
     */
    String getSiteJavaRoot();


    void addPlaceholderData(String key, Object value);

    void addPlaceholderData(Map<String, Object> data);

    /**
     * returns pre-filled nr of key value pairs for replacement injection in templates etc.
     *
     * @return object containing a number of items which can be used to inject into templates etc.
     */
    Map<String, Object> getPlaceholderData();

    /**
     * @return essentials project resource directory path
     */
    String getEssentialsResourcePath();
}
