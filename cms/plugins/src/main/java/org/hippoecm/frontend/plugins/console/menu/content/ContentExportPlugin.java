package org.hippoecm.frontend.plugins.console.menu.content;

import java.util.Map;

import javax.jcr.Node;

import org.apache.wicket.model.Model;
import org.hippoecm.frontend.dialog.DialogLink;
import org.hippoecm.frontend.dialog.IDialogFactory;
import org.hippoecm.frontend.dialog.IDialogService.Dialog;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.service.render.RenderPlugin;

public class ContentExportPlugin extends RenderPlugin<Node> {

    private static final long serialVersionUID = 1L;
    
    public ContentExportPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);
        IDialogFactory factory = new IDialogFactory() {
            private static final long serialVersionUID = 1L;
            public Dialog createDialog() {
                return new ContentExportDialog(ContentExportPlugin.this);
            }
        };
        add(new DialogLink("link", new Model<String>("XML export"), factory, getDialogService()));
    }

}
