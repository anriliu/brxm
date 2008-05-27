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
package org.hippoecm.frontend.sa.plugin.root;

import org.hippoecm.frontend.sa.dialog.DialogService;
import org.hippoecm.frontend.sa.plugin.IPluginContext;
import org.hippoecm.frontend.sa.plugin.config.IPluginConfig;
import org.hippoecm.frontend.sa.service.render.RenderPlugin;
import org.hippoecm.frontend.sa.service.render.RenderService;

public class RootPlugin extends RenderPlugin {
    private static final long serialVersionUID = 1L;

    private DialogService dialogService;

    public RootPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);

        for (String extension : new String[] { "content" }) {
            addExtensionPoint(extension);
        }
        dialogService = new DialogService();
        dialogService.init(context, config.getString(RenderService.DIALOG_ID), "dialog");
        add(dialogService);
    }

}
