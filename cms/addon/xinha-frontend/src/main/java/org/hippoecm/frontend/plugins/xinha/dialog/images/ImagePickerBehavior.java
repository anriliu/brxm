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
package org.hippoecm.frontend.plugins.xinha.dialog.images;

import java.util.HashMap;

import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugins.xinha.XinhaImageService;
import org.hippoecm.frontend.plugins.xinha.dialog.JsBean;
import org.hippoecm.frontend.plugins.xinha.dialog.XinhaDialogBehavior;

public class ImagePickerBehavior extends XinhaDialogBehavior {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    private static final long serialVersionUID = 1L;

    String imagesServiceId;

    public ImagePickerBehavior(IPluginContext context, IPluginConfig config, String serviceId) {
        super(context, config, serviceId);
        imagesServiceId = serviceId + ".images";
        
        dialog.getModal().setInitialWidth(500);
        dialog.getModal().setInitialHeight(300);
        dialog.getModal().setResizable(false);
    }

    @Override
    protected String getServiceId() {
        return "cms-pickers/image";
    }

    @Override
    protected JsBean newDialogModelObject(HashMap<String, String> p) {
        return getImageService().createXinhaImage(p);
    }

    @Override
    protected void onOk(JsBean bean) {
        XinhaImage xi = (XinhaImage) bean;
        String url = getImageService().attach(xi);
        if (url != null) {
            xi.setUrl(url);
        }
    }

    private XinhaImageService getImageService() {
        return context.getService(imagesServiceId, XinhaImageService.class);
    }

}
