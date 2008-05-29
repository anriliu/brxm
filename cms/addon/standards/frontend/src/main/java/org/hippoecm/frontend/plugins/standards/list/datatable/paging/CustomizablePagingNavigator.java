/*
 * Copyright 2007 Hippo
 *
 * Licensed under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hippoecm.frontend.plugins.standards.list.datatable.paging;

import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigation;

/**
 * @deprecated use org.hippoecm.frontend.plugins.standards.sa.* instead
 */
@Deprecated
public class CustomizablePagingNavigator extends AjaxPagingNavigator{

    private static final long serialVersionUID = 1;

    public CustomizablePagingNavigator(String id, IPageable pageable, IPagingLabelProvider labelProvider) {
        super(id, pageable, labelProvider);
    }
    public CustomizablePagingNavigator(String id, IPageable pageable) {
        super(id, pageable);
    }

    /**
     * Create a new PagingNavigation. May be subclassed to make us of specialized PagingNavigation.
     *
     * @param pageable
     *            the pageable component
     * @param labelProvider
     *            The label provider for the link text.
     * @return the navigation object
     */
    protected PagingNavigation newNavigation(final IPageable pageable,
            final IPagingLabelProvider labelProvider)
    {
        PagingNavigation pagingNavigation = new CustomizablePagingNavigation("navigation", pageable, labelProvider);
        return pagingNavigation;
    }

}
