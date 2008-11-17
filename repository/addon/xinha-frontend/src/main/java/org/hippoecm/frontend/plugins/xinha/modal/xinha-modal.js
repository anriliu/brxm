/*
 * Copyright 2008 Hippo
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

var openModalDialog = null;

ModalDialog = function(url, plugin, token, editor) {
    this.editor = editor;
    this.callbackUrl = url; 
    this.callbackUrl += url.indexOf('?') > -1 ? "&" : "?";
    this.callbackUrl += ('pluginName=' + encodeURIComponent(plugin));
    this.token = token;
};

ModalDialog.prototype = {
    _values : null,
        
    show : function(parameters) {
        this.saveState();
        
        var url = this.callbackUrl;
        for (var p in parameters) {
            url += ('&' + this.token + p + '=' + encodeURIComponent(parameters[p]));
        }
        wicketAjaxGet(url, null, null, null);
        openModalDialog = this;
    },

    close : function(values) {
        this.restoreState();
        this._values = values;
        this.onOk(values);
        openModalDialog = null;
    },

    cancel : function() {
        this.restoreState();
        this.onCancel();
        openModalDialog = null;        
        this._values = null;
    },
    
    hide : function() {
        return this._values;
    },
    
    onOk : function(values){
    },
    onCancel: function(){
    },
    
    saveState: function() {
        // We need to preserve the selection
        // if this is called before some editor has been activated, it activates the editor
        if (Xinha._someEditorHasBeenActivated)
        {
          this._lastRange = this.editor.saveSelection();
        }
        this.editor.deactivateEditor();
        this.editor.suspendUpdateToolbar = true;
        this.editor.currentModal = this;

        // unfortunately we have to hide the editor (iframe/caret bug)
        if (Xinha.is_ff2)
        {
          this._restoreTo = [this.editor._textArea.style.display, this.editor._iframe.style.visibility, this.editor.hidePanels()];
          this.editor._textArea.style.display = 'none';
          this.editor._iframe.style.visibility   = 'hidden';
        }
    },
    
    restoreState: function() {
        if (Xinha.is_ff2) {
          this.editor._textArea.style.display = this._restoreTo[0];
          this.editor._iframe.style.visibility   = this._restoreTo[1];
          this.editor.showPanels(this._restoreTo[2]);
        }

        this.editor.suspendUpdateToolbar = false;
        this.editor.currentModal = null;
        this.editor.activateEditor();
        this.editor.restoreSelection(this._lastRange);
        this.editor.updateToolbar();
        this.editor.focusEditor();
    }
}

