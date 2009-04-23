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
package org.hippoecm.hst.content.beans.query;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.hippoecm.hst.content.beans.manager.ObjectConverter;
import org.hippoecm.hst.content.beans.query.exceptions.QueryException;
import org.hippoecm.hst.content.beans.query.exceptions.ScopeException;
import org.hippoecm.hst.content.beans.query.filter.BaseFilter;
import org.hippoecm.hst.content.beans.query.filter.HstCtxWhereFilter;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.repository.api.HippoQuery;
import org.slf4j.LoggerFactory;

public class HstQueryImpl implements HstQuery {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(HstQueryImpl.class);

    private HstRequestContext hstRequestContext;
    private ObjectConverter objectConverter;
    
    private int limit = -1;
    private int offset = -1;
    private BaseFilter filter;
    private Node scope;
    private List<String> orderByList = new ArrayList<String>();

    public HstQueryImpl(HstRequestContext hstRequestContext, ObjectConverter objectConverter, Node scope) {
        this.hstRequestContext = hstRequestContext;
        this.objectConverter = objectConverter;
        this.scope = scope;
    }

   
    public void addOrderByAscending(String fieldNameAttribute) {
        orderByList.add("@"+fieldNameAttribute + " ascending");
    }

    public void addOrderByDescending(String fieldNameAttribute) {
        orderByList.add("@"+fieldNameAttribute + " descending");
    }
    
    public BaseFilter getFilter() {
        return this.filter;
    }

    public void setFilter(BaseFilter filter) {
        this.filter = filter;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setOffset(int offset) {
        this.offset= offset;
    }

    public String getQuery() throws QueryException{
        BaseFilter ctxWhereFilter = new HstCtxWhereFilter(this.hstRequestContext, this.scope);
        if(ctxWhereFilter.getJcrExpression() == null) {
            throw new ScopeException("HstContextWhereClause is not allowed to be null");
        }
        
        StringBuilder query = new StringBuilder();
        if("".equals(ctxWhereFilter.getJcrExpression())) {
            // no ctxWhereFilter will be applied
        } else {
            query.append("(").append(ctxWhereFilter.getJcrExpression()).append(")");
        }
        
        if(this.getFilter() != null && this.getFilter().getJcrExpression() != null) {
           if(query.length() == 0) {
               query.append(this.getFilter().getJcrExpression());
           } else {
               query.append(" and (").append(this.getFilter().getJcrExpression()).append(")");
           }
        }
        
        query.insert(0, "//*[");
        query.append("]");
        
        if(orderByList.size() > 0) {
            query.append(" order by ");
            boolean first = true;
            for(String orderBy : orderByList) {
                if(!first) {
                    query.append(",");
                }
                query.append(orderBy);
                first = false;
            }
        }
        log.debug("Query to execute is '{}'", query.toString());
        return query.toString();
    }
    
    public HstQueryResult execute() throws QueryException {
        String query = getQuery();
        try {
            QueryManager jcrQueryManager = this.hstRequestContext.getSession().getWorkspace().getQueryManager();
            
            Query jcrQuery = jcrQueryManager.createQuery(query, "xpath");
            if(jcrQuery instanceof HippoQuery)  {
                if(offset > -1){
                    ((HippoQuery)jcrQuery).setOffset(offset);
                }
                if(limit > -1){
                    ((HippoQuery)jcrQuery).setLimit(limit);
                }
            } else {
                log.warn("Query not instanceof of HippoQuery: cannot set limit and offset");
            }
            long start = System.currentTimeMillis();
            QueryResult queryResult = jcrQuery.execute();
            log.debug("Executing query took --({})-- ms to complete for '{}'", (System.currentTimeMillis() - start), query);
            return new HstQueryResultImpl(this.objectConverter, queryResult);
        } catch (InvalidQueryException e) {
            throw new QueryException(e.getMessage(), e);
        } catch (LoginException e) {
            log.warn("LoginException. Return null : {}", e);
        } catch (RepositoryException e) {
            log.warn("RepositoryException. Return null :  {}", e);
        }
        return null;
    }


}
