/*
 *  Copyright 2018 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.hst.content.beans.support.jackson;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.ser.VirtualBeanPropertyWriter;
import com.fasterxml.jackson.databind.util.Annotations;

import org.apache.commons.lang3.StringUtils;
import org.hippoecm.hst.configuration.hosting.Mount;
import org.hippoecm.hst.configuration.sitemap.HstSiteMapItem;
import org.hippoecm.hst.container.RequestContextProvider;
import org.hippoecm.hst.core.linking.HstLink;
import org.hippoecm.hst.core.linking.HstLinkCreator;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.hst.core.sitemenu.CommonMenuItem;
import org.hippoecm.hst.core.sitemenu.HstSiteMenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hippoecm.hst.core.container.ContainerConstants.LINK_NAME_SITE;

public class MenuItemLinkVirtualBeanPropertyWriter extends VirtualBeanPropertyWriter {

    private static final long serialVersionUID = 1L;
    public static final String PAGE_MODEL_PIPELINE_NAME = "PageModelPipeline";

    private static Logger log = LoggerFactory.getLogger(MenuItemLinkVirtualBeanPropertyWriter.class);

    @SuppressWarnings("unused")
    public MenuItemLinkVirtualBeanPropertyWriter() {
        super();
    }

    protected MenuItemLinkVirtualBeanPropertyWriter(BeanPropertyDefinition propDef, Annotations contextAnnotations,
                                                    JavaType type) {
        super(propDef, contextAnnotations, type);
    }

    @Override
    protected Object value(Object item, JsonGenerator gen, SerializerProvider prov) throws Exception {
        final HstRequestContext requestContext = RequestContextProvider.get();

        if (requestContext == null) {
            return null;
        }

        Map<String, LinkModel> linksMap = new LinkedHashMap<>();

        final CommonMenuItem menuItem = (CommonMenuItem) item;
        final HstLink menuItemLink = menuItem.getHstLink();

        if (menuItemLink == null || menuItemLink.isNotFound() || menuItemLink.getMount() == null) {
            if (StringUtils.isNotBlank(menuItem.getExternalLink())) {
                linksMap.put(LINK_NAME_SITE, new LinkModel(menuItem.getExternalLink(), "external") );
            }
            return linksMap;
        }

        final HstLinkCreator linkCreator = requestContext.getHstLinkCreator();

        final Mount linkMount = menuItemLink.getMount();
        // admittedly a bit of a dirty check to check on PageModelPipeline. Can this be improved?
        if (PAGE_MODEL_PIPELINE_NAME.equals(linkMount.getNamedPipeline())) {
            final Mount siteMount = linkMount.getParent();
            if (siteMount == null) {
                log.warn("Expected a 'PageModelPipeline' always to be nested below a parent site mount. This is not the " +
                        "case for '{}'. Cannot add site links", linkMount);
                return linksMap;
            }
            // since the selfLink could be resolved, the site link also must be possible to resolve
            final HstLink siteLink = linkCreator.create(menuItemLink.getPath(), siteMount);
            final HstSiteMapItem siteMapItem = siteLink.getHstSiteMapItem();
            if (siteMapItem != null) {
                final String linkType;
                if (siteMapItem.isContainerResource()) {
                    linkType = "resource";
                } else {
                    final String linkApplicationId = siteMapItem.getApplicationId();
                    // although this is the resolved sitemap item for the PAGE_MODEL_PIPELINE_NAME, it should resolve
                    // to exactly the same hst sitemap item configuration node as the parent mount, hence we can compare
                    // the application id
                    final String currentApplicationId = requestContext.getResolvedSiteMapItem().getHstSiteMapItem().getApplicationId();
                    linkType = Objects.equals(linkApplicationId, currentApplicationId) ? "internal" : "external";
                }
                linksMap.put(LINK_NAME_SITE, new LinkModel(siteLink.toUrlForm(requestContext, false), linkType) );
            }
        } else {
            // might be a cross channel link to a mount that does not have resource api: Thus add this as an 'external' link
            linksMap.put(LINK_NAME_SITE, new LinkModel(menuItemLink.toUrlForm(requestContext, false), "external") );
        }

        return linksMap;
    }

    @Override
    public VirtualBeanPropertyWriter withConfig(MapperConfig<?> config, AnnotatedClass declaringClass,
            BeanPropertyDefinition propDef, JavaType type) {
        // Ref: jackson-databind-master/src/test/java/com/fasterxml/jackson/databind/ser/TestVirtualProperties.java
        return new MenuItemLinkVirtualBeanPropertyWriter(propDef, declaringClass.getAnnotations(), type);
    }
}
