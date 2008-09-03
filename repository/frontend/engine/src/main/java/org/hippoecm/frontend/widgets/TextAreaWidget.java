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
package org.hippoecm.frontend.widgets;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;

public class TextAreaWidget extends AjaxUpdatingWidget {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    private static final long serialVersionUID = 1L;
    private String rows;
    private String cols;

    public TextAreaWidget(String id, IModel model) {
        super(id, model);
        addFormField(new TextArea("widget", this.getModel()) {        
        	 private static final long serialVersionUID = 1L;

             @Override
             protected void onComponentTag(final ComponentTag tag) {
                 if (getRows() != null) {
                     tag.put("rows", getRows());
                 }
                 if (getCols() != null) {
                     tag.put("cols", getCols());
                 }
                 super.onComponentTag(tag);
             }
        });
    }
    public void setRows(String rows) {
        this.rows = rows;
    }

    public String getRows() {
        return rows;
    }

    public void setCols(String cols) {
        this.cols=cols;
    }
    
    public String getCols() {
        return cols;
    }

}
