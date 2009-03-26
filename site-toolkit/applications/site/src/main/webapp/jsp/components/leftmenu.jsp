<%--
  Copyright 2008 Hippo

  Licensed under the Apache License, Version 2.0 (the  "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS"
  BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License. --%>

<%@ page language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://www.hippoecm.org/jsp/hst/core" prefix='hst'%>

<div style="border:1px solid">
    <h1>My menu : ${menu.name}</h1>
    <ul>
    <c:forEach var="item" items="${menu.siteMenuItems}">
        <li>
            <c:choose >
                <c:when test="${item.selected}">
                    <b>${item.name}</b>
                    <ul>
                    <c:forEach var="subitem" items="${item.childMenuItems}">
                        <li>
                            <hst:link var="link" link="${subitem.hstLink}"/>
                            <a href="${link}">
                            ${subitem.name}
                            </a>
                            <c:if test="${subitem.selected}">
                                <b>${subitem.name}</b>
                            </c:if>
                        </li>
                    </c:forEach>
                    </ul>
                </c:when>
                <c:otherwise>
                    <hst:link var="link" link="${item.hstLink}"/>
                    <a href="${link}">
                        ${item.name}
                    </a>
                </c:otherwise>
            </c:choose>
            
        </li>
    </c:forEach>
    </ul>
</div>