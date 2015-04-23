/*
 * Copyright 2008-2015 Hippo B.V. (http://www.onehippo.com)
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
package org.onehippo.addon.frontend.gallerypicker;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IChainingModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.util.string.Strings;
import org.hippoecm.frontend.dialog.AbstractDialog;
import org.hippoecm.frontend.dialog.DialogLink;
import org.hippoecm.frontend.dialog.IDialogFactory;
import org.hippoecm.frontend.editor.ITemplateEngine;
import org.hippoecm.frontend.model.IModelReference;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.model.properties.JcrPropertyModel;
import org.hippoecm.frontend.model.properties.JcrPropertyValueModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.service.IBrowseService;
import org.hippoecm.frontend.service.IEditor.Mode;
import org.hippoecm.frontend.service.render.RenderPlugin;
import org.hippoecm.repository.api.HippoNodeType;
import org.onehippo.addon.frontend.gallerypicker.dialog.GalleryPickerDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The org.onehippo.addon.frontend.gallerypicker.GalleryPickerPlugin provides a Wicket dialog that allows a content
 * editor to select an image from the image gallery.
 *
 * @author Jeroen Reijn
 */
public class GalleryPickerPlugin extends RenderPlugin<Node> {

    private static final long serialVersionUID = 2965577252486600004L;

    private static final Logger log = LoggerFactory.getLogger(GalleryPickerPlugin.class);

    private static final String DEFAULT_THUMBNAIL_WIDTH = "50";
    private static final String JCR_ROOT_NODE_UUID = "cafebabe-cafe-babe-cafe-babecafebabe";
    private static final String GALLERY_ROOT_PATH = "/content/gallery/";
    private static final String HIPPO_GALLERY_EXAMPLE_IMAGESET_NODETYPE_NAME = "hippogallery:exampleImageSet";
    private static final String HIPPO_GALLERY_STD_GALLERYSET_NODETYPE_NAME = "hippogallery:stdgalleryset";
    private static final String SUPPORTED_PATHS_KEY = "supported.paths";
    private static final CssResourceReference GALLERY_PICKER_CSS =
            new CssResourceReference(GalleryPickerPlugin.class, GalleryPickerPlugin.class.getSimpleName() + ".css");

    private IModel<String> valueModel;
    private JcrNodeModel currentNodeModel;
    private String[] supportedPaths;

    //this object will be used by wicket based on the propertyModel provided to the inlinePreview image
    @SuppressWarnings("unused")
    private ImageItem image;
    private ImageItemFactory imageFactory;
    private InlinePreviewImage inlinePreviewImage;
    private AjaxLink<Void> remove;

    protected Mode mode;
    protected IPluginConfig config;

    @SuppressWarnings("unchecked")
    public GalleryPickerPlugin(final IPluginContext context, IPluginConfig config) {
        super(context, config);

        this.config = config;

        imageFactory = new ImageItemFactory();

        currentNodeModel = (JcrNodeModel) getModel();

        valueModel = getValueModel(currentNodeModel);
        // See if the plugin is in 'edit' or in 'view' mode
        mode = Mode.fromString(config.getString(ITemplateEngine.MODE, "view"));

        if (config.containsKey(SUPPORTED_PATHS_KEY)) {
            supportedPaths = config.getStringArray(SUPPORTED_PATHS_KEY);
        }

        Fragment fragment;
        switch (mode) {
            case COMPARE:
                fragment = new Fragment("fragment", "compare", this);
                String path = null;
                if (config.containsKey("model.compareTo")) {
                    IModelReference<Node> baseModelRef = context.getService(config.getString("model.compareTo"), IModelReference.class);
                    if (baseModelRef != null) {
                        IModel<Node> baseModel = baseModelRef.getModel();
                        if (baseModel != null && baseModel.getObject() != null) {
                            String uuid = getValueModel(baseModel).getObject();
                            path = imageFactory.createImageItem(uuid).getPrimaryUrl();
                        }
                    }
                }
                InlinePreviewImage baseImagePreview = new InlinePreviewImage("baseImage", Model.of(path), getWidth(), getHeight());
                baseImagePreview.setVisible(!Strings.isEmpty(path));
                fragment.add(baseImagePreview);
                break;

            case EDIT:
                fragment = new Fragment("fragment", "edit", this);
                DialogLink select = new DialogLink("select", new StringResourceModel("picker.select", this, null),
                        createDialogFactory(), getDialogService());
                fragment.add(select);
                addOpenButton(fragment);

                remove = new AjaxLink<Void>("remove") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        valueModel.setObject(JCR_ROOT_NODE_UUID);
                        triggerModelChanged();
                    }
                };
                fragment.add(remove);

                remove.add(new Label("remove-link-text", new StringResourceModel("picker.remove", this, null)));
                remove.setVisible(false);
                if (isValidDisplaySelection()) {
                    remove.setVisible(true);
                }
                break;

            default:
                fragment = new Fragment("fragment", "view", this);
        }

        PropertyModel<String> previewImage = new PropertyModel<>(this, "image.primaryUrl");
        inlinePreviewImage = new InlinePreviewImage("previewImage", previewImage, getWidth(), getHeight());
        inlinePreviewImage.setVisible(isValidDisplaySelection());
        fragment.add(inlinePreviewImage);
        add(fragment);

        setOutputMarkupId(true);

        modelChanged();
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(GALLERY_PICKER_CSS));
    }

    protected String getWidth() {
        return config.getString("preview.width", DEFAULT_THUMBNAIL_WIDTH);
    }

    protected String getHeight() {
        return config.getString("preview.height");
    }

    private static IModel<String> getValueModel(IModel<Node> nodeModel) {
        Node node = nodeModel.getObject();
        if (node != null) {
            try {
                Property prop = node.getProperty("hippo:docbase");
                return new JcrPropertyValueModel<>(-1, prop.getValue(), new JcrPropertyModel<String>(prop));
            } catch (RepositoryException ex) {
                throw new WicketRuntimeException("Property hippo:docbase is not defined.", ex);
            }
        } else {
            return Model.of("");
        }
    }

    private void addOpenButton(Fragment fragment) {
        AjaxLink openButton = new AjaxLink("open") {
            @Override
            public boolean isVisible() {
                return isValidDisplaySelection();
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                open();
            }
        };
        openButton.setOutputMarkupId(true);
        fragment.add(openButton);
    }

    private void open() {
        final IPluginConfig config = getPluginConfig();
        final IPluginContext context = getPluginContext();
        final IModel<String> displayModel = getPathModel();
        final String browserId = config.getString("browser.id", "service.browse");
        final IBrowseService browseService = context.getService(browserId, IBrowseService.class);
        final String location = config.getString("option.location", displayModel.getObject());
        if (browseService != null) {
            //noinspection unchecked
            browseService.browse(new JcrNodeModel(location));
        } else {
            log.warn("no browse service found with id '{}', cannot browse to '{}'", browserId, location);
        }
    }

    /**
     * Create a dialogFactory, which is used by the plugin to render the dialog for selecting an image.
     *
     * @return a new DialogFactory based on the current configuration
     */
    private IDialogFactory createDialogFactory() {
        return new IDialogFactory() {

            public AbstractDialog<String> createDialog() {
                return new GalleryPickerDialog(getPluginContext(), getPluginConfig(), new IChainingModel<String>() {

                    public String getObject() {
                        return valueModel.getObject();
                    }

                    public void setObject(String object) {
                        valueModel.setObject(object);
                        GalleryPickerPlugin.this.modelChanged();
                    }

                    public IModel<?> getChainedModel() {
                        return valueModel;
                    }

                    public void setChainedModel(IModel<?> model) {
                        throw new UnsupportedOperationException("Value model cannot be changed");
                    }

                    public void detach() {
                        valueModel.detach();
                    }

                }) {

                    @Override
                    public IModel<String> getTitle() {
                        return new StringResourceModel("dialog-title", GalleryPickerPlugin.this, null, "Gallery Picker");
                    }

                };

            }
        };
    }

    @Override
    public void onModelChanged() {
        triggerModelChanged();
    }

    /**
     * If the model of this plugin changes, choose what to do with the image preview. If no image is selected, make the
     * image invisible, so it won't show a red cross in IE. If an image is selected, show the selected image.
     */
    public void triggerModelChanged() {
        if (valueModel == null) {
            return;
        }
        String uuid = getUUIDFromValueModel();
        if (isValidDisplaySelection()) {
            inlinePreviewImage.setVisible(true);
            image = imageFactory.createImageItem(uuid);
            if (remove != null) {
                remove.setVisible(true);
            }
        } else {
            inlinePreviewImage.setVisible(false);
            if (remove != null) {
                remove.setVisible(false);
            }
        }
        redraw();
    }

    /**
     * Get the UUID of the selected image from the valueModel object.
     *
     * @return UUID represented by a String value
     */
    private String getUUIDFromValueModel() {
        return valueModel.getObject();
    }

    /**
     * Check to see if the selected item is indeed a uuid of a imagesetNodeType
     *
     * @return true if the selected node is of either example imageset or std gallery set, false otherwise.
     */
    public boolean isValidDisplaySelection() {
        String uuid = getUUIDFromValueModel();
        if (uuid == null) {
            return false;
        } else {
            try {
                Node selectedNode = getJCRSession().getNodeByIdentifier(uuid);
                if (getNodeTypeName(selectedNode).equals(HIPPO_GALLERY_EXAMPLE_IMAGESET_NODETYPE_NAME) ||
                        getNodeTypeName(selectedNode).equals(HIPPO_GALLERY_STD_GALLERYSET_NODETYPE_NAME) ||
                        selectedNode.getPath().startsWith(GALLERY_ROOT_PATH) ||
                        arrayContainsStartWith(supportedPaths, selectedNode.getPath())) {
                    return true;
                }
            } catch (RepositoryException e) {
                log.debug("Something went wrong while trying to get the selected node by UUID: {}", e.getMessage());
                return false;
            }
        }
        return false;
    }

    private Session getJCRSession() throws RepositoryException {
        return currentNodeModel.getNode().getSession();
    }

    /**
     * Get the current node name based on the primaryNodeType;
     *
     * @param node the JCR Node for which to lookup the node type name
     * @return the String representation of the current primary node type name
     * @throws javax.jcr.RepositoryException if something goes wrong while trying to get the node type name
     */
    private String getNodeTypeName(Node node) throws RepositoryException {
        return node.getPrimaryNodeType().getName();
    }

    /**
     * This function is similar to list .contains() function, but instead of a exact match it's looking if the string
     * specified is a start in any of the array items
     *
     * @param array
     * @param path
     * @return boolean; is there a record that starts with the given string
     */
    public static boolean arrayContainsStartWith(String[] array, String path) {
        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                if (path.startsWith(array[i])) {
                    return (i >= 0);
                }
            }
        }
        return false;
    }

    private String getMirrorPath() {
        Node node = GalleryPickerPlugin.this.getModelObject();
        try {
            if (node != null && node.hasProperty(HippoNodeType.HIPPO_DOCBASE)) {
                return getPath(node.getProperty(HippoNodeType.HIPPO_DOCBASE).getString());
            }
        } catch (ValueFormatException e) {
            log.warn("Invalid value format for docbase " + e.getMessage());
            log.debug("Invalid value format for docbase ", e);
        } catch (PathNotFoundException e) {
            log.warn("Docbase not found " + e.getMessage());
            log.debug("Docbase not found ", e);
        } catch (ItemNotFoundException e) {
            log.info("Docbase " + e.getMessage() + " could not be dereferenced");
        } catch (RepositoryException e) {
            log.error("Invalid docbase " + e.getMessage(), e);
        }
        return StringUtils.EMPTY;
    }

    private String getPath(final String docbaseUUID) {
        String path = StringUtils.EMPTY;
        try {
            if (!(docbaseUUID == null || docbaseUUID.equals("") || docbaseUUID.equals(JCR_ROOT_NODE_UUID))) {
                path = getJCRSession().getNodeByIdentifier(docbaseUUID).getPath();
            }
        } catch (RepositoryException e) {
            log.error("Invalid docbase " + e.getMessage(), e);
        }
        return path;
    }

    IModel<String> getPathModel() {
        return new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                return getMirrorPath();
            }
        };
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        if (valueModel == null) {
            return;
        }
        valueModel.detach();
    }

}
