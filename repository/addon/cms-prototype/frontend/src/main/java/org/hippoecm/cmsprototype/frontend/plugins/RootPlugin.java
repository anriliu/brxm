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
package org.hippoecm.cmsprototype.frontend.plugins;

import org.hippoecm.frontend.model.IPluginModel;
import org.hippoecm.frontend.plugin.Plugin;
import org.hippoecm.frontend.plugin.PluginDescriptor;
import org.hippoecm.frontend.plugin.channel.Channel;
import org.hippoecm.frontend.plugin.channel.Request;

public class RootPlugin extends Plugin {
    private static final long serialVersionUID = 1L;

    public RootPlugin(PluginDescriptor pluginDescriptor, IPluginModel model, Plugin parentPlugin) {
        super(pluginDescriptor, model, parentPlugin);
    }

    @Override
    public void handle(Request request) {
        if ("select".equals(request.getOperation()) || "logout".equals(request.getOperation())) {
            Channel outgoing = getBottomChannel();
            if (outgoing != null) {
                outgoing.publish(outgoing.createNotification(request));
            }
            return;
        }

        if ("exception".equals(request.getOperation())) {
            request.getContext().addRefresh(this);
            Channel outgoing = getBottomChannel();
            if (outgoing != null) {
                outgoing.publish(outgoing.createNotification(request));
            }
            return;
        }

        super.handle(request);
    }
}
