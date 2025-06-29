<%--

    Copyright (C) 2000 - 2024 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page import="org.silverpeas.core.contribution.content.form.Form"%>
<%@page import="org.silverpeas.core.contribution.content.form.PagesContext"%>
<%@page import="org.silverpeas.core.contribution.content.form.DataRecord"%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<c:set var="language" value="${requestScope.resources.language}"/>

<fmt:setLocale value="${language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<c:set var="instanceSettings" value="${requestScope.InstanceSettings}" />
<jsp:useBean id="instanceSettings" type="org.silverpeas.components.classifieds.ClassifiedsComponentSettings"/>
<c:set var="profile" value="${requestScope.Profile}" />
<c:set var="categories" value="${requestScope.Categories}" />
<c:set var="nbTotal" value="${requestScope.NbTotal}" />
<c:set var="validation" value="${requestScope.Validation}" />

<c:set var="formSearch" value="${requestScope.Form}" />
<c:set var="data" value="${requestScope.Data}" />
<c:set var="instanceId" value="${requestScope.InstanceId}" />

<view:sp-page angularJsAppName="silverpeas.classifieds">
<view:sp-head-part>
  <view:includePlugin name="toggle"/>
  <view:script src="/classifieds/jsp/javaScript/classifieds.js"/>
</view:sp-head-part>
<view:sp-body-part id="classifieds">
    <div id="${instanceId}">

      <view:browseBar />

      <view:operationPane>
        <c:if test="${profile.name != 'anonymous'}">
          <c:if test="${(profile.name == 'admin') || (profile.name == 'publisher')}">
            <fmt:message var="addOp" key="classifieds.addClassified" />
            <fmt:message var="addIcon" key="classifieds.addClassified" bundle="${icons}" />
            <c:url var="addIcon" value="${addIcon}"/>
            <view:operationOfCreation action="NewClassified" altText="${addOp}" icon="${addIcon}" />
          </c:if>

          <fmt:message var="myOp" key="classifieds.myClassifieds" />
          <fmt:message var="myIcon" key="classifieds.myClassifieds" bundle="${icons}" />
          <view:operation action="ViewMyClassifieds" altText="${myOp}" icon="${myIcon}" />

          <view:operationSeparator />

          <fmt:message var="subAddOp" key="classifieds.addSubscription" />
          <fmt:message var="subAddIcon" key="classifieds.subscriptionsAdd" bundle="${icons}" />
          <view:operation action="javascript:addSubscription()" altText="${subAddOp}" icon="${subAddIcon}" />

          <fmt:message var="mySubOp" key="classifieds.mySubscriptions" />
          <fmt:message var="mySubIcon" key="classifieds.mySubscriptions" bundle="${icons}" />
          <view:operation action="ViewMySubscriptions" altText="${mySubOp}" icon="${mySubIcon}" />
        </c:if>

        <c:if test="${(profile.name == 'admin') && (validation)}">
          <view:operationSeparator />
          <fmt:message var="toValidateOp" key="classifieds.viewClassifiedToValidate" />
          <fmt:message var="toValidateIcon" key="classifieds.viewClassifiedToValidate" bundle="${icons}" />
          <view:operation action="ViewClassifiedToValidate" altText="${toValidateOp}" icon="${toValidateIcon}" />
        </c:if>
      </view:operationPane>

      <view:window>
        <view:frame>
          <view:componentInstanceIntro componentId="${instanceId}" language="${language}"/>
          <view:areaOfOperationOfCreation/>
          <jsp:include page="subscriptionManager.jsp"/>
          <form id="searchForm" name="searchForm" action="SearchClassifieds" method="post" enctype="multipart/form-data">
            <c:if test="${not empty formSearch}">
              <div id="search" >
                <!-- Search Form -->
                  <%
                    String language = (String) pageContext.getAttribute("language");
                    String instanceId = (String) pageContext.getAttribute("instanceId");
                    Form formSearch = (Form) pageContext.getAttribute("formSearch");
                    DataRecord data = (DataRecord) pageContext.getAttribute("data");

                    PagesContext context = new PagesContext("myForm", "0", language, false, instanceId, null, null);
                    context.setIgnoreDefaultValues(true);
                    context.setCreation(false);
                    context.setBorderPrinted(false);
                    formSearch.display(out, context, data);
                  %>
                  <br/>
                  <view:buttonPane>
                    <fmt:message var="searchLabel" key="classifieds.searchButton">
                      <fmt:param value="${nbTotal}" />
                    </fmt:message>
                    <view:button label="${searchLabel}"
                      action="javascript:onClick=sendData();" />
                  </view:buttonPane>
              </div>
            </c:if>
          </form>

          <div id="categories">
            <c:if test="${not empty categories}">
              <c:forEach items="${categories}" var="category" varStatus="loopStatus">
                <div id="category${category.key}" class="category${((loopStatus.index % 2) == 0) ? 'left' : 'right'}">
                  <div class="categoryTitle">
                    <a href="javascript:viewClassifieds(0, '${category.key}')"> ${category.value} </a>
                  </div>
                  <div class="categoryContent">
                    <c:if test="${empty category.classifieds}">
                      <span class="emptyCategory"><fmt:message key="classifieds.CategoryEmpty" />
                      </span>
                    </c:if>
                    <c:if test="${not empty category.classifieds}">
                      <ul>
                        <c:forEach items="${category.classifieds}" var="classified" end="4">
                          <li><a href="ViewClassified?ClassifiedId=${classified.classifiedId}">${classified.title}</a>
                          <c:if test="${instanceSettings.priceAllowed and classified.price > 0}">
                          ${classified.price} &euro;
                          </c:if>
                            <span class="date">
                              <c:if test="${not empty classified.validateDate}">
                                              <span class="sep"> - </span><view:formatDateTime value="${classified.validateDate}" language="${language}"/>
                                            </c:if>
                                            <c:if test="${empty classified.validateDate}">
                                  <c:if test="${not empty classified.updateDate}">
                                                <span class="sep"> - </span><view:formatDateTime value="${classified.updateDate}" language="${language}"/>
                                              </c:if>
                                              <c:if test="${empty classified.updateDate}">
                                                <span class="sep"> - </span><view:formatDateTime value="${classified.creationDate}" language="${language}"/>
                                              </c:if>
                                </c:if>
                            </span>
                          </li>
                        </c:forEach>
                      </ul>
                    </c:if>
                  </div>
                  <div class="ViewAllClassifiedsByCategory">
                    <a href="javascript:viewClassifieds(0, '${category.key}')">
                      <fmt:message key="classifieds.viewAllClassifiedsByCategory" />
                    </a>
                  </div>
                  <c:if
                    test="${(profile.name == 'admin') || (profile.name == 'publisher')}">
                    <div class="newClassified">
                      <a href="NewClassified?FieldKey=${category.key}"> <fmt:message
                          key="classifieds.newClassified" /> </a>
                    </div>
                  </c:if>
                </div>
              </c:forEach>
            </c:if>
          </div>

          <!-- legal notice -->
          <div id="infos" class="inlineMessage">
            <fmt:message key="classifieds.infos" />
          </div>

        </view:frame>
      </view:window>
    </div>
</view:sp-body-part>
</view:sp-page>