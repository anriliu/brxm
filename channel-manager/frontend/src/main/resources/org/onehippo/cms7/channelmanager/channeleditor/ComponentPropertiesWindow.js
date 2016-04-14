/*
 * Copyright 2016 Hippo B.V. (http://www.onehippo.com)
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
(function () {
  "use strict";

  Ext.namespace('Hippo.ChannelManager.ChannelEditor');

  Hippo.ChannelManager.ChannelEditor.ComponentPropertiesWindow = Ext.extend(Hippo.ux.window.FloatingWindow, {

    _formStates: {},

    constructor: function (config) {
      var buttons = [],
        windowWidth = config.width;

      this.componentPropertiesPanel = new Hippo.ChannelManager.ChannelEditor.ComponentPropertiesPanel({
        bubbleEvents: ['save', 'deleteComponent', 'deleteVariant', 'propertiesChanged'],
        resources: config.resources,
        locale: config.locale,
        composerRestMountUrl: config.composerRestMountUrl,
        variantsUuid: config.variantsUuid,
        mountId: config.mountId,
        listeners: {
          visibleHeightChanged: this._adjustHeight,
          close: this.hide,
          clientvalidation: this._onClientValidation,
          onLoad: this._resetFormStates,
          beforetabchange: function (panel, newTab, currentTab) {
            if (newTab) {
              newTab.addClass('qa-tab-active');
            }
            if (currentTab) {
              currentTab.removeClass('qa-tab-active');
            }
          },
          enableDeleteComponent: this._enableDeleteComponentButton,
          scope: this
        }
      });

      if (Ext.isDefined(config.variantsUuid)) {
        windowWidth += this.componentPropertiesPanel.tabWidth;
      }

      this.saveButton = new Ext.Button({
        xtype: 'button',
        cls: 'btn btn-default qa-save-button',
        text: Hippo.ChannelManager.ChannelEditor.Resources['properties-window-button-save'],
        scope: this,
        handler: function () {
          this.componentPropertiesPanel.saveAll().then(this._resetFormStates.bind(this));
        }
      });

      this.deleteComponentButton = new Ext.Button({
        xtype: 'button',
        cls: 'btn btn-default qa-delete-button',
        text: Hippo.ChannelManager.ChannelEditor.Resources['properties-window-button-delete'],
        scope: this.componentPropertiesPanel,
        handler: this.componentPropertiesPanel.deleteComponent
      });

      this.closeButon = new Ext.Button({
        xtype: 'button',
        cls: 'btn btn-default qa-close-button',
        text: Hippo.ChannelManager.ChannelEditor.Resources['properties-window-button-close'],
        scope: this,
        handler: function () {
          this.componentPropertiesPanel.clearComponent();
          this.hide();
        }
      });

      Hippo.ChannelManager.ChannelEditor.ComponentPropertiesWindow.superclass.constructor.call(this, Ext.apply(config, {
        layout: 'fit',
        width: windowWidth,
        items: this.componentPropertiesPanel,
        buttonAlign: 'left',
        buttons: [
          this.deleteComponentButton,
          {xtype: 'tbfill'},
          this.saveButton,
          this.closeButon
        ]
      }));
    },

    initComponent: function () {
      Hippo.ChannelManager.ChannelEditor.ComponentPropertiesWindow.superclass.initComponent.apply(this, arguments);

      this.addEvents('save', 'close', 'deleteVariant', 'propertiesChanged');

      this.on('hide', this.componentPropertiesPanel.onHide, this.componentPropertiesPanel);
    },

    _adjustHeight: function (propertiesPanelVisibleHeight) {
      var newVisibleHeight = propertiesPanelVisibleHeight + this.getFrameHeight(),
        pageEditorHeight = Hippo.ChannelManager.ChannelEditor.Instance.getHeight(),
        windowY = this.getPosition()[1],
        spaceBetweenWindowAndBottom = 4,
        maxHeight = pageEditorHeight - windowY - spaceBetweenWindowAndBottom,
        newHeight = Math.min(newVisibleHeight, maxHeight);
      if (this.getHeight() !== newHeight) {
        this.setHeight(newHeight);
      }
    },

    _resetFormStates: function() {
      this._formStates = {};
    },

    _onClientValidation: function (form, valid) {
      var disableSaveButton = true,
        count = 0,
        name = form.variant.variantName;

      this._formStates[name] = {
        name: name,
        valid: valid,
        dirty: form.isDirty()
      };

      // enable save if all forms are valid and exists a dirty one
      for (name in this._formStates) {
        if (!this._formStates[name].valid) {
          break;
        }
        count++;
      }
      if (count === Object.keys(this._formStates).length) {
        // check if a dirty form exists
        for (name in this._formStates) {
          if (this._formStates[name].dirty) {
            disableSaveButton = false;
            break;
          }
        }
      }

      this.saveButton.setDisabled(disableSaveButton);
    },

    showComponent: function (component, container, page) {
      this.setTitle(component.label);
      this.componentPropertiesPanel.setComponent(component, container, page);
      this.show();
    },

    onComponentRemoved: function () {
      // clear recorded changes because the component has been removed.
      this.componentPropertiesPanel.clearComponent(true);
    },

    _enableDeleteComponentButton: function (enabled) {
      if (!this.deleteComponentButton) {
        return;
      }
      if (enabled) {
        if (this.deleteComponentButton.disabled) {
          this.deleteComponentButton.enable();
        }
      } else {
        if (!this.deleteComponentButton.disabled) {
          this.deleteComponentButton.disable();
        }
      }
    }
  });

}());
