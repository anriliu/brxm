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
package org.hippoecm.frontend.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IChainingModel;
import org.apache.wicket.model.IModel;

public class JcrPropertyModel implements IChainingModel, IDataProvider {
    private static final long serialVersionUID = 1L;

    // The Item model that this model chains using the IChainingModel interface
    private JcrItemModel itemModel;

    //  Constructor
    public JcrPropertyModel(Property prop) {
        this.itemModel = new JcrItemModel(prop);
    }

    // Convenience methods, not part of an api

    public Property getProperty() {
        return (Property) itemModel.getObject();
    }
    
    //Implement IChainingModel
    
    public IModel getChainedModel() {
        return itemModel;
    }

    public void setChainedModel(IModel model) {
        if (model instanceof JcrItemModel) {
            itemModel = (JcrItemModel)model;
        }
    }

    public Object getObject() {
        return itemModel.getObject();
    }

    public void setObject(Object object) {
        itemModel.setObject(object);
    }

    public void detach() {
        itemModel.detach();
    }
    

    //  IDataProvider implementation for use in DataViews
    // (subclasses of org.apache.wicket.markup.repeater.data.DataViewBase)

    public Iterator iterator(int first, int count) {
        List list = new ArrayList();
        try {
            Property prop = getProperty();
            if (prop.getDefinition().isMultiple()) {
                Value[] values = prop.getValues();
                for (int i = 0; i < values.length; i++) {
                    list.add(new IndexedValue(values[i].getString(), i));
                }
            } else {
                list.add(new IndexedValue(prop.getValue().getString(), 0));
            }
        } catch (RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return list.iterator();
    }

    public IModel model(Object object) {
        IndexedValue indexedValue = (IndexedValue) object;
        return new JcrValueModel(indexedValue.index, indexedValue.value, this);
    }

    public int size() {
        int result = 1;
        try {
            Property prop = getProperty();
            if (prop.getDefinition().isMultiple()) {
                result = prop.getValues().length;
            }
        } catch (RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    private class IndexedValue {
        protected String value;
        protected int index;

        IndexedValue(String value, int index) {
            this.index = index;
            this.value = value;
        }
    }

    // override Object

    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("item", itemModel.toString())
                .toString();
    }

    public boolean equals(Object object) {
        if (object instanceof JcrPropertyModel == false) {
            return false;
        }
        if (this == object) {
            return true;
        }
        JcrPropertyModel propertyModel = (JcrPropertyModel) object;
        return new EqualsBuilder().append(itemModel, propertyModel.itemModel).isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder(473, 17).append(itemModel).toHashCode();
    }

}
