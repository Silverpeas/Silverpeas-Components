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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<c:set var="language" value="${requestScope.resources.language}"/>

<fmt:setLocale value="${language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<c:set var="browseContext" value="${requestScope.browseContext}" />
<c:set var="componentLabel" value="${browseContext[1]}" />

<c:set var="instanceSettings" value="${requestScope.InstanceSettings}" />
<jsp:useBean id="instanceSettings" type="org.silverpeas.components.classifieds.ClassifiedsComponentSettings"/>
<c:set var="classifieds" value="${requestScope.Classifieds}" />
<c:set var="title" value="${requestScope.TitlePath}" />
<c:set var="extra" value="${requestScope.Extra}" />
<c:set var="profile" value="${requestScope.Profile}" />


<view:sp-page>
<view:sp-head-part>
</view:sp-head-part>
<view:sp-body-part id="classifieds">
  <fmt:message var="classifiedPath" key="${ (empty title) ? 'classifieds.myClassifieds' : title}" />
  <view:browseBar extraInformations="${extra}">
    <view:browseBarElt label="${classifiedPath}" link="#" />
  </view:browseBar>

  <c:if test="${(profile.name == 'manager') || (profile.name == 'publisher') }">
    <view:operationPane>
      <fmt:message var="addOp" key="classifieds.addClassified" />
      <fmt:message var="addIcon" key="classifieds.addClassified" bundle="${icons}" />
      <c:url var="addIcon" value="${addIcon}"/>
      <view:operationOfCreation action="NewClassified" altText="${addOp}" icon="${addIcon}" />
    </view:operationPane>
  </c:if>

  <view:window>
    <view:frame>
      <view:areaOfOperationOfCreation/>
      <c:if test="${not empty classifieds}">
        <view:board>
          <ul class="list_result_classifieds">
            <c:forEach items="${classifieds}" var="classified">
            <c:set var="title" value="${classified.title}" />
            <c:set var="displayedTitle"><view:encodeHtml string="${title}" /></c:set>
            <li class="status_${classified.status}">
                <a class="title_result_classifieds" href="ViewClassified?ClassifiedId=${classified.classifiedId}">${displayedTitle}</a>
                <c:if test="${instanceSettings.priceAllowed and classified.price > 0}">
                          ${classified.price} &euro; - 
                         </c:if>
                  <span class="status_result_classifieds">
                    <c:choose>
                      <c:when test="${classified.status == 'Draft'}">
                        <fmt:message key="classifieds.draft" /><span class="sep_status"> - </span>
                      </c:when>

                      <c:when test="${classified.status == 'ToValidate'}">
                        <fmt:message key="classifieds.toValidate" /><span class="sep_status"> - </span>
                      </c:when>
                      <c:when test="${classified.status == 'Unvalidate'}">
                        <fmt:message key="classifieds.refuse" /><span class="sep_status"> - </span>
                      </c:when>
                      <c:when test="${classified.status == 'Unpublished'}">
                        <fmt:message key="classifieds.unpublished" /><span class="sep_status"> - </span>
                      </c:when>
                    </c:choose>
                  </span>
                  <span class="creatorName_result_classifieds">
                       <view:username userId="${classified.creatorId}" />
                  </span>
                  <span class="sep_creatorName_result_classifieds"> - </span>
                  <c:if test="${not empty classified.validateDate}">
                    <span class="date_result_classifieds"><view:formatDateTime value="${classified.validateDate}" language="${language}"/></span>
                  </c:if>
                  <c:if test="${empty classified.validateDate}">
                    <c:if test="${not empty classified.updateDate}">
                    <span class="date_result_classifieds updateDate"><view:formatDateTime value="${classified.updateDate}" language="${language}"/></span>
                    </c:if>
                    <c:if test="${empty classified.updateDate}">
                      <span class="date_result_classifieds creationDate"><view:formatDateTime value="${classified.creationDate}" language="${language}"/></span>
                    </c:if>
                  </c:if>
              </li>
            </c:forEach>
          </ul>
        </view:board>
      </c:if>

      <c:if test="${empty classifieds}">
        <div class="inlineMessage">
          <fmt:message key="classifieds.CategoryEmpty" />
        </div>
      </c:if>
    </view:frame>
  </view:window>
</view:sp-body-part>
</view:sp-page>