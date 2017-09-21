/*
 * Copyright 2017 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hippoecm.hst.demo.jaxrs.services;

import org.hippoecm.hst.core.parameters.Parameter;

public interface ProductResourceParamsInfo {

    @Parameter(name = "rest.product.siteLinkIncluded", defaultValue = "true", required = false,
            description = "Whether or not to include site link information.")
    public boolean isSiteLinkIncluded();

    @Parameter(name = "rest.product.nodeLinkIncluded", defaultValue = "true", required = false,
            description = "Whether or not to include node link information.")
    public boolean isNodeLinkIncluded();

    @Parameter(name = "rest.product.sortFields", defaultValue = "demosite:product,-demosite:price", required = false,
            description = "Sort fields information as a comma-separated string in a search result. "
                    + "Each item suggests sorting the result by the field name in ascending order by default. "
                    + "If prefixed by '-', it suggests sorting the result by the field name in descending order.")
    public String getSortFields();

}
