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

package org.onehippo.cms7.essentials.rest.model.gallery;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.onehippo.cms7.essentials.rest.model.PropertyRestful;
import org.onehippo.cms7.essentials.rest.model.Restful;

/**
 * @version "$Id$"
 */
@XmlRootElement(name = "imageProcessor")
public class ImageSetRestful implements Restful {

    private static final long serialVersionUID = 1L;

    private String name;
    private String path;
    private String id;
    private List<PropertyRestful> properties = new ArrayList<>();
    private List<ImageVariantRestful> variants = new ArrayList<>();

    public ImageSetRestful() {
    }

    public ImageSetRestful(final String name, final String path) {

        this.name = name;
        this.name = path;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public List<PropertyRestful> getProperties() {
        return properties;
    }

    public void setProperties(final List<PropertyRestful> properties) {
        this.properties = properties;
    }

    public List<ImageVariantRestful> getVariants() {
        return variants;
    }

    public void setVariants(final List<ImageVariantRestful> variants) {
        this.variants = variants;
    }

    public void addVariant(final ImageVariantRestful variant) {
        this.variants.add(variant);
    }

    public void addProperty(final PropertyRestful property) {
        this.properties.add(property);
    }
}
