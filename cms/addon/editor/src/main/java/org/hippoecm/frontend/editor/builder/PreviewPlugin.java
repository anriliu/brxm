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
package org.hippoecm.frontend.editor.builder;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.hippoecm.frontend.editor.ITemplateEngine;
import org.hippoecm.frontend.editor.config.BuiltinTemplateStore;
import org.hippoecm.frontend.editor.impl.TemplateEngine;
import org.hippoecm.frontend.model.IJcrNodeModelListener;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.model.ModelService;
import org.hippoecm.frontend.plugin.IClusterControl;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IClusterConfig;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugin.config.impl.JavaPluginConfig;
import org.hippoecm.frontend.plugin.config.impl.JcrClusterConfig;
import org.hippoecm.frontend.service.IJcrService;
import org.hippoecm.frontend.service.render.RenderPlugin;
import org.hippoecm.frontend.service.render.RenderService;
import org.hippoecm.frontend.types.IFieldDescriptor;
import org.hippoecm.frontend.types.ITypeDescriptor;
import org.hippoecm.frontend.types.ITypeStore;
import org.hippoecm.frontend.types.JcrTypeDescriptor;
import org.hippoecm.frontend.types.JcrTypeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreviewPlugin extends RenderPlugin implements IJcrNodeModelListener {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(PreviewPlugin.class);

    private ITypeStore typeStore;
    private IClusterControl child;
    private ModelService helperModel;
    private Map<String, String> paths;

    public PreviewPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);

        addExtensionPoint("template");

        // create model service for helper
        helperModel = new ModelService(config.getString("helper.model"), null);
        helperModel.init(context);

        paths = new HashMap<String, String>();

        // register for flush events
        context.registerService(this, IJcrService.class.getName());

        onModelChanged();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (typeStore != null) {
            typeStore.detach();
        }
    }

    @Override
    public void onModelChanged() {
        super.onModelChanged();

        if (child != null) {
            child.stop();
            child = null;
        }

        updatePrototype();

        typeStore = null;
        JcrTypeHelper typeHelper = new JcrTypeHelper((JcrNodeModel) getModel());
        ITypeDescriptor type = typeHelper.getTypeDescriptor();
        if (type != null) {
            String typeName = typeHelper.getName();
            String prefix = typeName.substring(0, typeName.indexOf(':'));
            String mode = getPluginConfig().getString("mode");

            IPluginContext context = getPluginContext();

            typeStore = new JcrTypeStore(prefix);

            TemplateEngine engine = new TemplateEngine(context, typeStore);
            context.registerService(engine, ITemplateEngine.class.getName());
            String engineId = context.getReference(engine).getServiceId();

            IClusterConfig template;
            JcrNodeModel templateModel = typeHelper.getTemplate();
            if (templateModel == null) {
                BuiltinTemplateStore builtinStore = new BuiltinTemplateStore(typeStore, null);
                if ("edit".equals(mode)) {
                    IClusterConfig cluster = builtinStore.getTemplate(type, "edit");
                    templateModel = typeHelper.storeTemplate(cluster);
                    template = new PreviewClusterConfig(context, templateModel, helperModel, engineId);
                } else {
                    template = builtinStore.getTemplate(type, mode);
                }
            } else {
                if ("edit".equals(mode)) {
                    template = new PreviewClusterConfig(context, templateModel, helperModel, engineId);
                } else {
                    template = new JcrClusterConfig(templateModel);
                }
            }

            IPluginConfig parameters = new JavaPluginConfig();
            parameters.put(ITemplateEngine.ENGINE, engineId);
            parameters.put(ITemplateEngine.MODE, mode);
            parameters.put(RenderService.WICKET_ID, getPluginConfig().getString("template"));

            final IClusterControl control = context.newCluster(template, parameters);
            String modelId = control.getClusterConfig().getString(RenderService.MODEL_ID);
            JcrNodeModel prototypeModel = typeHelper.getPrototype();
            final ModelService modelService = new ModelService(modelId, prototypeModel);

            child = new IClusterControl() {
                private static final long serialVersionUID = 1L;

                public void stop() {
                    control.stop();
                    modelService.destroy();
                }

                public IClusterConfig getClusterConfig() {
                    return null;
                }

                public void start() {
                    modelService.init(getPluginContext());
                    control.start();
                }
            };
            child.start();
            redraw();
        }
    }

    private void updatePrototype() {
        JcrTypeHelper typeHelper = new JcrTypeHelper((JcrNodeModel) getModel());
        JcrTypeDescriptor typeModel = typeHelper.getTypeDescriptor();
        ITemplateEngine engine = getPluginContext().getService(getPluginConfig().getString("engine"),
                ITemplateEngine.class);
        JcrNodeModel prototypeModel = typeHelper.getPrototype();
        if (prototypeModel == null) {
            return;
        }

        try {
            Node prototype = prototypeModel.getNode();
            if (prototype != null && typeModel != null) {
                Map<String, String> oldFields = paths;
                paths = new HashMap<String, String>();
                for (Map.Entry<String, IFieldDescriptor> entry : typeModel.getFields().entrySet()) {
                    paths.put(entry.getKey(), entry.getValue().getPath());
                }

                boolean save = false;
                for (Map.Entry<String, String> entry : oldFields.entrySet()) {
                    String oldPath = entry.getValue();
                    IFieldDescriptor newField = typeModel.getField(entry.getKey());
                    if (newField != null) {
                        ITypeDescriptor fieldType = engine.getType(newField.getType());
                        if (!newField.getPath().equals(oldPath) && !newField.getPath().equals("*")
                                && !oldPath.equals("*")) {
                            if (fieldType.isNode()) {
                                if (prototype.hasNode(oldPath)) {
                                    Node child = prototype.getNode(oldPath);
                                    child.getSession().move(child.getPath(),
                                            prototype.getPath() + "/" + newField.getPath());
                                }
                            } else {
                                if (prototype.hasProperty(oldPath)) {
                                    Property property = prototype.getProperty(oldPath);
                                    if (property.getDefinition().isMultiple()) {
                                        Value[] values = property.getValues();
                                        property.remove();
                                        if (newField.isMultiple()) {
                                            prototype.setProperty(newField.getPath(), values);
                                        } else if (values.length > 0) {
                                            prototype.setProperty(newField.getPath(), values[0]);
                                        }
                                    } else {
                                        Value value = property.getValue();
                                        property.remove();
                                        if (newField.isMultiple()) {
                                            prototype.setProperty(newField.getPath(), new Value[] { value });
                                        } else {
                                            prototype.setProperty(newField.getPath(), value);
                                        }
                                    }
                                }
                            }
                            save = true;
                        } else if (oldPath.equals("*") || newField.getPath().equals("*")) {
                            log.warn("Wildcard fields are not supported");
                        }
                    } else {
                        if (oldPath.equals("*")) {
                            log
                                    .warn("Removing wildcard fields is unsupported.  Items that fall under the definition will not be removed.");
                        } else {
                            if (prototype.hasNode(oldPath)) {
                                save = true;
                                prototype.getNode(oldPath).remove();
                            }
                            if (prototype.hasProperty(oldPath)) {
                                save = true;
                                prototype.getProperty(oldPath).remove();
                            }
                        }
                    }
                }
                if (save) {
                    prototype.save();
                }
            }
        } catch (RepositoryException ex) {
            log.error(ex.getMessage());
        }
    }

    public void onFlush(JcrNodeModel nodeModel) {
        // if anything above us or below us changed, refresh
        JcrNodeModel myModel = (JcrNodeModel) getModel();
        if (myModel.getItemModel().hasAncestor(nodeModel.getItemModel())
                || nodeModel.getItemModel().hasAncestor(myModel.getItemModel())) {
            if (typeStore != null) {
                typeStore.detach();
            }
            onModelChanged();
        }
    }
}
