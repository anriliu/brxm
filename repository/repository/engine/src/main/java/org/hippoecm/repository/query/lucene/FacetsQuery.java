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
package org.hippoecm.repository.query.lucene;

import java.util.List;

import org.apache.jackrabbit.core.query.lucene.NamespaceMappings;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.conversion.IllegalNameException;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.hippoecm.repository.jackrabbit.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FacetsQuery {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    /**
     * The logger instance for this class
     */
    private static final Logger log = LoggerFactory.getLogger(FacetsQuery.class);

    /**
     * The lucene query
     */
    private BooleanQuery query;

    public FacetsQuery(List<KeyValue<String, String>> facetsQuery, NamespaceMappings nsMappings,
                       ServicingIndexingConfiguration indexingConfig) {
        this.query = new BooleanQuery(true);

        if (facetsQuery != null) {
            for (KeyValue<String, String> keyValue : facetsQuery ) {
                Name propertyName;
                try {
                    propertyName = NameFactoryImpl.getInstance().create(keyValue.getKey());
                    if (indexingConfig.isFacet(propertyName)) {
                        
                        String internalName = ServicingNameFormat.getInteralPropertyPathName(nsMappings, keyValue.getKey());
                        Query tq ;
                        if(keyValue.getValue() == null) {
                        	// only make sure the document contains at least the facet (propertyName)
                        	tq = new FacetPropExistsQuery(keyValue.getKey() , internalName, indexingConfig).getQuery();
                        } else {
                            String internalFacetName = ServicingNameFormat.getInternalFacetName(internalName);
                        	tq = new TermQuery(new Term(internalFacetName, keyValue.getValue()));
                        	
                    	}
                        this.query.add(tq, Occur.MUST);
                    } else {
                        log.warn("Property " + keyValue.getKey() + " not allowed for facetted search. " + "Add the property to the indexing configuration to be defined as FACET");
                    }
                } catch (IllegalNameException e) {
                    log.error(e.toString());
                } 
            }
        }
    }

    public BooleanQuery getQuery() {
        return query;
    }
}
