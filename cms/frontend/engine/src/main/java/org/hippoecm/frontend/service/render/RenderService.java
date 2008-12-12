/*
 *  Copyright 2008 Hippo.
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
package org.hippoecm.frontend.service.render;

import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.service.IRenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RenderService extends AbstractRenderService {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(RenderService.class);


    public RenderService(IPluginContext context, IPluginConfig properties) {
        super(context, properties);
    }

    @Override
    protected void addExtensionPoint(final String extension) {
        super.addExtensionPoint(extension);
        add(new EmptyPanel(extension));
    }

    @Override
    protected ExtensionPoint createExtensionPoint(String extension) {
        return new ExtensionPoint(extension);
    }

    @Override
    protected void removeExtensionPoint(String name) {
        super.removeExtensionPoint(name);
        replace(new EmptyPanel(name));
    }

    protected class ExtensionPoint extends AbstractRenderService.ExtensionPoint {
        private static final long serialVersionUID = 1L;

        ExtensionPoint(String extension) {
            super(extension);
        }

        @Override
        public void onServiceAdded(IRenderService service, String name) {
            service.bind(RenderService.this, extension);
            replace(service.getComponent());
            redraw();
            super.onServiceAdded(service, name);
        }

        @Override
        public void onRemoveService(IRenderService service, String name) {
            replace(new EmptyPanel(extension));
            service.unbind();
            redraw();
            super.onRemoveService(service, name);
        }

    }
}
