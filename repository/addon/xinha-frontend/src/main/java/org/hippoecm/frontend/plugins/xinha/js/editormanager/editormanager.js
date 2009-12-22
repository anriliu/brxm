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

/**
 * @description
 * <p>
 * Todo
 * </p>
 * @namespace YAHOO.hippo
 * @requires hippoajax, hashmap
 * @module editormanager
 */

/**
 * Xinha globals
 */
var _editor_url = null;
var _editor_lang = null;
var _editor_skin = null;
var xinha_editors = [];

YAHOO.namespace('hippo');

if (!YAHOO.hippo.EditorManager) {
    (function() {
        var Dom = YAHOO.util.Dom, Lang = YAHOO.lang, HippoAjax = YAHOO.hippo.HippoAjax;
        
        /**
         * The editor-manager controls the life-cycle of Xinha editors.
         * Optionally, Xinha instances can be cached in the browser DOM, turned off by default.
         */
        YAHOO.hippo.EditorManagerImpl = function() {
        };

        YAHOO.hippo.EditorManagerImpl.prototype = {

            defaultTimeout : 2000,
            editors : new YAHOO.hippo.HashMap(),
            activeEditors : new YAHOO.hippo.HashMap(),
            initialized : false,
            usePool : false,
            pool: null,

            init : function(editorUrl, editorLang, editorSkin) {
                if (this.initialized) {
                    return;
                }

                // set Xinha globals
                _editor_url = editorUrl;
                _editor_lang = editorLang;
                _editor_skin = editorSkin;
                
                //and load XinhaLoader.js
                var me = this;
                this._loadback(editorUrl + 'XinhaLoader.js', function() { 
                    me.initialized = true; 
                });

                //Save open editors when a WicketAjax callback is executed
                Wicket.Ajax.registerPreCallHandler(function() { 
                    me.saveEditors(); 
                });
            },
            
            _loadback : function(Url, Callback, Scope, Bonus) {
                var agt       = navigator.userAgent.toLowerCase();
                var is_ie    = ((agt.indexOf("msie") != -1) && (agt.indexOf("opera") == -1));
                var T = !is_ie ? "onload" : 'onreadystatechange';
                var S = document.createElement("script");
                S.type = "text/javascript";
                S.src = Url;
                if ( Callback ) {
                  S[T] = function() {      
                    if ( is_ie && ( ! ( /loaded|complete/.test(window.event.srcElement.readyState) ) ) ){
                      return;
                    }
                    Callback.call(Scope ? Scope : this, Bonus);
                    S[T] = null;
                  };
                }
                document.getElementsByTagName("head")[0].appendChild(S);
            },

            register : function(cfg) {
                if (!this.initialized) {
                    //XinhaLoader.js hasn't been added to the head section yet, wait a little longer
                    var me = this;
                    var f = function() {
                        me.register(cfg);
                    }
                    window.setTimeout(f, 200);
                    return;
                }
                
                if(this.usePool && this.editors.containsKey(cfg.name)) {
                    var editor = this.editors.get(cfg.name);

                    //update properties
                    editor.xinha.config = this._appendProperties(editor.xinha.config, cfg.properties);
                    for ( var i = 0; i < cfg.pluginProperties.length; i++) {
                        var pp = cfg.pluginProperties[i];
                        editor.xinha.config[pp.name] = this._appendProperties(editor.xinha.config[pp.name], pp.values);
                    }

                    this.render(cfg.name);
                    return;
                }
                
                //create new xinha editor
                var editor = {
                    createStarted : false,
                    pluginsLoaded : false,
                    xinhaAvailable : false,
                    name : cfg.name,
                    config : cfg,
                    xinha : null,
                    lastData: null
                };
    
                this.editors.put(editor.name, editor);
    
                if(this.usePool && this.pool == null) {
                    //create a pool element in the document body
                    var pool = document.createElement('div');
                    pool.id = 'poolid';
                    Dom.setStyle(pool, 'display', 'none');
                    Dom.setStyle(pool, 'position', 'absolute');
                    Dom.setXY(pool, [-4000, -4000]);
                    this.pool = document.body.appendChild(pool);
                }
                
                this.createAndRender(editor);
            },

            createAndRender : function(editor) {
                editor.createStarted = true;

                var me = this;
                var f = function() {
                    me.createAndRender(editor);
                }

                if (!Xinha.loadPlugins(editor.config.plugins, f)) {
                    //Plugins not loaded yet, createAndRender will be recalled
                    return;
                }
                editor.pluginsLoaded = true;

                var xinhaConfig = new Xinha.Config();
                if (!Lang.isUndefined(editor.config.styleSheets)
                        && editor.config.styleSheets.length > 0) {
                    //load xinha stylesheets
                    xinhaConfig.pageStyleSheets = [editor.config.styleSheets.length];
                    for ( var i = 0; i < editor.config.styleSheets.length; i++) {
                        var ss = editor.config.styleSheets[i];
                        if (!ss.indexOf("/") == 0 && !ss.indexOf("http://") == 0) {
                            ss = _editor_url + ss;
                        }
                        xinhaConfig.pageStyleSheets[i] = ss;
                    }
                }
                
                //Set formatting options
                if(!Lang.isUndefined(editor.config.formatBlock)) {
                    xinhaConfig.formatblock = editor.config.formatBlock;
                }
                
                //make editor
                var xinha = Xinha.makeEditors([ editor.name ], xinhaConfig, editor.config.plugins)[editor.name];
                
                //concatenate default properties with configured properties
                xinha.config = this._appendProperties(xinha.config, editor.config.properties);

                //configure toolbar
                if(editor.config.toolbars.length == 0) {
                    //Load toolbar with all Xinha default buttons
                    //remove button popupeditor
                    outerLoop:
                    for(var i=0; i<xinha.config.toolbar.length; i++) {
                        for(var j=0; j<xinha.config.toolbar[i].length; j++) {
                            if(xinha.config.toolbar[i][j] == 'popupeditor') {
                                xinha.config.toolbar[i].splice(j, 1);//remove element from array
                                break outerLoop;      
                            }
                        }
                    }
                } else {
                    //load custom toolbar
                    xinha.config.toolbar = [ editor.config.toolbars ];
                }
                
                //concat default plugin properties with configured properties
                for ( var i = 0; i < editor.config.pluginProperties.length; i++) {
                    var pp = editor.config.pluginProperties[i];
                    xinha.config[pp.name] = this._appendProperties(xinha.config[pp.name], pp.values);
                }

                editor.xinha = xinha;
                xinha_editors[editor.name] = editor.xinha;
                editor.xinhaAvailable = true;
                
                this.render(editor.name);
            },

            render : function(name) {
                if(this.activeEditors.containsKey(name)) {
                    return;
                }
                
                var editor = this.editors.get(name);
                if (!editor.xinhaAvailable) {
                    if (!editor.createStarted) {
                        this.createAndRender(editor);
                    }
                    return;
                }
                
                if(!this.usePool) {
                    var me = this;
                    //register onload callback
                    editor.xinha._onGenerate = function() {
                        me.editorLoaded(editor);
                    }
                    Xinha.startEditors([ editor.xinha ]);
                } else {
                    var id = 'POOLID-' + editor.name;
                    var el = Dom.getElementBy(function(node) {
                        return node.id == id;
                    }, 'div', this.pool);
                
                    if(el.length == 0) {
                        Xinha.startEditors([ editor.xinha ]);
                    } else {
                        var textarea = Dom.get(name);
                        var textareaName = textarea.name;
                        var value = textarea.value;
    
                        var parent = new YAHOO.util.Element(textarea.parentNode);
                        parent.removeChild(textarea);
                        parent.appendChild(Dom.getFirstChild(el));
                        this.pool.removeChild(el);
                        
                        editor.xinha._framework.ed_cell.replaceChild(textarea, editor.xinha._textArea);
                        editor.xinha._textArea = textarea;
                        
                        editor.xinha._textArea.value = value;
                        editor.xinha.initIframe();
    
                        editor.xinha.setEditorContent(value);
    
                        editor.xinha._textArea.id = name
                        editor.xinha._textArea.name = textareaName;
                        
                        this.registerCleanup(editor.xinha._framework.table, name);
                        
                        editor.xinha.deactivateEditor();
                        editor.lastData = editor.xinha.getInnerHTML();
                    }
                }
                
                this.activeEditors.put(editor.name, editor);
            },
            
            cleanup : function(name) {
                var editor = this.activeEditors.remove(name);
                if(this.usePool) {
                    this.saveInPool(editor);
                }
            },
            
            saveInPool : function(editor) {
                editor.xinha._textArea.id = null;
                editor.xinha._textArea.name = null;
                var poolEl = document.createElement('div');
                poolEl.id = 'POOLID-' + editor.name;
                Dom.setStyle(poolEl, 'display', 'none');
                poolEl.appendChild(editor.xinha._framework.table);
                this.pool.appendChild(poolEl);
            },

            editorLoaded : function(editor) {
                var xId = editor.xinha._textArea.getAttribute("id");
                var xTable = editor.xinha._framework.table;
                this.registerCleanup(xTable, xId);
                editor.lastData = editor.xinha.getInnerHTML();
                
                //Workaround for http://issues.onehippo.com/browse/HREPTWO-2960
                //Test if IE8, than set&remove focus on Xinha to prevent UI lockup
                if(YAHOO.env.ua.ie >= 8) {
                    editor.xinha.activateEditor();
                    editor.xinha.focusEditor();
                    editor.xinha.deactivateEditor();
                }
            },
            
            registerCleanup : function(element, name) {
                Dom.setStyle(element.parentNode, 'display', 'block');
                HippoAjax.registerDestroyFunction(element, this.cleanup, this, name);
            },
            
            saveEditors : function() {
                var keys = this.activeEditors.keySet();
                for ( var i = 0; i < keys.length; i++) {
                    this.saveEditor(keys[i]);
                }
            },

            saveEditor : function(name) {
                var editor = this.editors.get(name);
                if(editor != null && editor.xinha.plugins['AutoSave']) {
                    try {
                        var data = editor.xinha.getInnerHTML();
                        if(data != editor.lastData) {
                            editor.xinha.plugins['AutoSave'].instance.save();
                            editor.lastData = data;
                        }
                    } catch(e) {
                        YAHOO.log('Error retrieving innerHTML from xinha, skipping save', 'error', 'EditorManager');
                    }
                }
            },

            clear : function() {
                this.editors.forEach(this, function(k, v) {
                    if (Dom.get(k) == null) {
                        this.editors.remove(k);
                    }
                });
            },

            _appendProperties : function(base, properties) {
                for (var i = 0; i < properties.length; i++) {
                    base[properties[i].key] = properties[i].value;
                }
                return base;
            },

            log : function(message) {
                YAHOO.log(message, "info", "EditorManager");           
            }
        };
        
    })();

    YAHOO.hippo.EditorManager = new YAHOO.hippo.EditorManagerImpl();
    YAHOO.register("editormanager", YAHOO.hippo.EditorManager, {
        version : "2.7.0", build : "1799"
    });
}