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
package org.hippoecm.cmsprototype.frontend.plugins.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.hippoecm.cmsprototype.frontend.model.exception.ModelWrapException;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.model.NodeModelWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SortableQueryResultProvider extends SortableDataProvider {

    private static final long serialVersionUID = 1L;

    static final Logger log = LoggerFactory.getLogger(SortableQueryResultProvider.class);


    /**
     * result of query needs to be transient because it is not serializable
     */
    private transient QueryResult result;
    private transient javax.jcr.Session session;
    
    public SortableQueryResultProvider(QueryResult result, javax.jcr.Session session) {
        this.result = result;
        this.session = session;
    }

    public Iterator<NodeModelWrapper> iterator(int first, int count) {
        List<NodeModelWrapper> list = new ArrayList<NodeModelWrapper>();
        if (result == null ) {
            return null;
        }
        try {
            // we have to use row to be able to get rep:excerpt(.) 
            int i = 0;
            for(RowIterator rows = result.getRows(); rows.hasNext() ; i++){
                Row row = rows.nextRow();
                if (i >= first && i < (first + count)) {
                    String path = row.getValue("jcr:path").getString();
                    Node n = (Node) session.getItem(path);
                    try {
                        SearchDocument node = new SearchDocument(new JcrNodeModel(n));
                        if(row.getValue(SearchPlugin.REP_EXCERPT) != null ){
                            node.setExcerpt(row.getValue(SearchPlugin.REP_EXCERPT).getString());
                            String similarLink = "//element(*, hippo:document)[rep:similar(., '" + path + "')]/rep:excerpt(.)" ;
                            node.setSimilar("similar");
                            node.setSimilarLink(similarLink);
                        }
                        
                        list.add(node);
                    } catch (ModelWrapException e) {
                        log.error(e.getMessage());
                    }
                }
            }
            
            return list.iterator();
        } catch (RepositoryException e) {
            e.printStackTrace();
            return null;
        }
    }

    public IModel model(Object object) {
         return (NodeModelWrapper) object;
    }

    public int size() {
        if( result == null ) {
            return 0;
        }
        try {
            return (int)result.getNodes().getSize();
        } catch (RepositoryException e) {
            e.printStackTrace();
            return 0;
        }
    }

    

}
