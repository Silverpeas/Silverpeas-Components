<%--

    Copyright (C) 2000 - 2022 Silverpeas

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
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>
<%@ taglib prefix="vien" uri="http://www.silverpeas.com/tld/viewGenerator" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/formsOnline" prefix="formsOnline" %>

<c:set var="lang" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<c:set var="componentId" value="${requestScope.browseContext[3]}"/>

<fmt:setLocale value="${lang}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle basename="org.silverpeas.multilang.generalMultilang" var="generalBundle"/>

<c:set var="forms" value="${requestScope['formsList']}"/>
<c:set var="userRequests" value="${requestScope['UserRequests']}"/>
<jsp:useBean id="userRequests" type="org.silverpeas.components.formsonline.model.RequestsByStatus"/>
<c:set var="requestsAsValidator" value="${requestScope['RequestsAsValidator']}"/>
<c:set var="role" value="${requestScope['Role']}"/>
<c:set var="app" value="${requestScope['App']}"/>

<c:url var="iconAdd" value="/util/icons/create-action/add-form.png"/>
<c:url var="iconEdit" value="/util/icons/update.gif"/>
<c:url var="iconPublish" value="/util/icons/lock.gif"/>
<c:url var="iconUnpublish" value="/util/icons/unlock.gif"/>
<c:url var="iconDelete" value="/util/icons/delete.gif"/>
<c:url var="iconPermalink" value="/util/icons/link.gif"/>

<fmt:message var="permalinkCopyLabel" key="GML.permalink.copy" bundle="${generalBundle}"/>

<view:sp-page angularJsAppName="silverpeas.formsOnline">
<view:sp-head-part>
<view:includePlugin name="toggle"/>
<script type="text/javascript">
  function deleteForm(idModel, nbRequests) {
    var label = "<fmt:message key="formsOnline.deleteFormConfirm"/>";
    if (nbRequests > 0) {
      label = "<fmt:message key="formsOnline.deleteFormAndRequestsConfirm"/>";
    }
    jQuery.popup.confirm(label, function() {
      spProgressMessage.show();
      document.deleteForm.FormId.value = idModel;
      document.deleteForm.submit();
    });
  }

  function allRequests() {
    location.href = "Inbox";
  }
</script>
</view:sp-head-part>
<view:sp-body-part>
<view:browseBar/>
<c:if test="${role == 'admin'}">
  <view:operationPane>
    <fmt:message var="addForm" key="formsOnline.createForm"/>
    <view:operationOfCreation action="CreateForm" icon="${iconAdd}" altText="${addForm}"/>
  </view:operationPane>
</c:if>
<view:window>

<view:componentInstanceIntro componentId="${componentId}" language="${lang}"/>

<c:choose>
  <c:when test="${role == 'senderOnly'}">
    <div id="my-formsOnline" class="lecteur-view">
  </c:when>
  <c:otherwise>
    <div id="my-formsOnline">
  </c:otherwise>
</c:choose>

  <c:if test="${requestsAsValidator != null}">
  <jsp:useBean id="requestsAsValidator" type="org.silverpeas.components.formsonline.model.RequestsByStatus"/>
  <div class="secteur-container formsOnline-waitMyAction">
    <div class="header">
      <c:choose>
        <c:when test="${empty requestsAsValidator.toValidate}">
          <h3 class="formsOnline-waitMyAction-title"><fmt:message key="formsOnline.home.toValidate.none.title"/></h3>
        </c:when>
        <c:otherwise>
          <h3 class="formsOnline-waitMyAction-title"><strong>${requestsAsValidator.toValidate.originalListSize()} </strong><fmt:message key="formsOnline.home.toValidate.title"><fmt:param value="${fn:length(requestsAsValidator.toValidate)}"/> </fmt:message></h3>
        </c:otherwise>
      </c:choose>
    </div>
    <ul>
      <c:if test="${empty requestsAsValidator.toValidate}">
        <li><span class="txt-no-content"><fmt:message key="formsOnline.home.toValidate.none"/></span></li>
      </c:if>
      <c:forEach items="${requestsAsValidator.toValidate}" var="request" varStatus="status">
        <c:choose>
          <c:when test="${request.read}">
            <li class="read">
          </c:when>
          <c:otherwise>
             <li>
          </c:otherwise>
        </c:choose>
        <a href="ViewRequest?Id=${request.id}"><span class="form-title">${request.form.title}</span><span class="ask-form-author"><view:username userId="${request.creatorId}"/></span><span class="ask-form-date"><view:formatDateTime value="${request.creationDate}"/></span></a></li>
      </c:forEach>
    </ul>
    <a href="InBox" class="more"> <fmt:message key="formsOnline.home.requests.toggle.more"/></a>
  </div>
  </c:if>

  <c:if test="${not empty userRequests.draft}">
  <div class="secteur-container my-formsOnline" id="my-formsOnline-draft">
    <div class="header">
      <h3 class="my-formsOnline-title"><fmt:message key="formsOnline.home.requests.mine"/> <strong><fmt:message key="formsOnline.home.requests.mine.draft"/></strong></h3>
    </div>
    <ul>
      <view:listPane var="draftUserRequests" routingAddress="Main" numberLinesPerPage="10">
        <view:listItems items="${userRequests.draft}" var="request">
          <li>
            <a href="EditRequest?Id=${request.id}"><span class="ask-form-date"><view:formatDateTime value="${request.creationDate}"/></span><span class="form-title">${request.form.title}</span></a>
          </li>
        </view:listItems>
      </view:listPane>
      <script type="text/javascript">
        whenSilverpeasReady(function() {
          sp.listPane.ajaxControls('#my-formsOnline-draft');
        });
      </script>
    </ul>
  </div>
  </c:if>

  <div class="secteur-container my-formsOnline" id="my-formsOnline-wait">
    <div class="header">
      <h3 class="my-formsOnline-title"><fmt:message key="formsOnline.home.requests.mine"/> <strong><fmt:message key="formsOnline.home.requests.mine.toValidate"/></strong></h3>
    </div>
    <ul>
      <c:if test="${empty userRequests.toValidate}">
        <li><span class="txt-no-content"><fmt:message key="formsOnline.home.requests.toValidate.none"/></span></li>
      </c:if>
      <view:listPane var="toValidateUserRequests" routingAddress="Main" numberLinesPerPage="10">
        <view:listItems items="${userRequests.toValidate}" var="request">
          <li>
            <a href="ViewRequest?Id=${request.id}"><span class="ask-form-date"><view:formatDateTime value="${request.creationDate}"/></span><span class="form-title">${request.form.title}</span>
            <formsOnline:validationsSchemaImage userRequest="${request}"/></a>
          </li>
        </view:listItems>
      </view:listPane>
      <script type="text/javascript">
        whenSilverpeasReady(function() {
          sp.listPane.ajaxControls('#my-formsOnline-wait');
        });
      </script>
    </ul>
  </div>

  <c:set var="areaValidatedTitle">
    <fmt:message key="formsOnline.home.requests.mine"/> <strong><fmt:message key="formsOnline.home.requests.mine.validated"/></strong>
  </c:set>
  <formsOnline:myRequestsByState requests="${userRequests.validated}" title="${areaValidatedTitle}" state="validate"/>

  <c:set var="areaRefusedTitle">
    <fmt:message key="formsOnline.home.requests.mine"/> <strong><fmt:message key="formsOnline.home.requests.mine.denied"/></strong>
  </c:set>
  <formsOnline:myRequestsByState requests="${userRequests.denied}" title="${areaRefusedTitle}" state="refused"/>

  <c:set var="areaArchivedTitle">
    <fmt:message key="formsOnline.home.requests.mine"/> <strong><fmt:message key="formsOnline.home.requests.mine.archived"/></strong>
  </c:set>
  <formsOnline:myRequestsByState requests="${userRequests.archived}" title="${areaArchivedTitle}" state="archived"/>

  <c:set var="areaCanceledTitle">
    <fmt:message key="formsOnline.home.requests.mine"/> <strong><fmt:message key="formsOnline.home.requests.mine.canceled"/></strong>
  </c:set>
  <formsOnline:myRequestsByState requests="${userRequests.canceled}" title="${areaCanceledTitle}" state="canceled"/>

</div>

<c:choose>
  <c:when test="${role == 'senderOnly'}">
    <div id="all-formsOnline" class="lecteur-view">
  </c:when>
  <c:otherwise>
    <div id="all-formsOnline">
  </c:otherwise>
</c:choose>

  <div class="secteur-container">
    <div class="header">
      <h3 class="all-formsOnline-title"><fmt:message key="formsOnline.home.forms.choose"/></h3>
    </div>
    <view:areaOfOperationOfCreation/>
    <c:if test="${empty forms}">
      <div class="inlineMessage"><fmt:message key="formsOnline.forms.none"/></div>
    </c:if>
    <c:if test="${not empty forms}">
      <ul>
        <c:forEach items="${forms}" var="form">
          <li class="showActionsOnMouseOver">
            <c:if test="${form.sendable}">
              <a href="NewRequest?FormId=${form.id}">
            </c:if>
              <span class="form-title">${form.title}</span>
              <span class="form-description">${silfn:escapeHtmlWhitespaces(form.description)}</span>
              <c:if test="${role == 'admin'}">
                <span class="form-nbRequests">${form.nbRequests} <fmt:message key="formsOnline.home.form.requests.number"><fmt:param value="${form.nbRequests}"/></fmt:message></span>
              </c:if>
            <c:if test="${form.sendable}">
              </a>
            </c:if>
            <c:if test="${role == 'admin'}">
              <div class="operation actionShownOnMouseOver">
                <c:url var="permalinkUrl" value="/Form/${form.id}?ComponentId=${form.instanceId}"/>
                <a href="${permalinkUrl}" title="${permalinkCopyLabel}" class="sp-permalink"><img border="0" src="${iconPermalink}" alt="<fmt:message key="GML.permalink"/>" title="<fmt:message key="GML.permalink"/>" /></a>
                <a href="EditForm?FormId=${form.id}" title="<fmt:message key="GML.modify"/>"><img border="0" src="${iconEdit}" alt="<fmt:message key="GML.modify"/>" title="<fmt:message key="GML.modify"/>" /></a>
                <c:choose>
                  <c:when test="${form.notYetPublished}">
                    <a href="PublishForm?Id=${form.id}" title="<fmt:message key="formsOnline.publishForm"/>"><img border="0" src="${iconPublish}" alt="<fmt:message key="formsOnline.publishForm"/>" title="<fmt:message key="formsOnline.publishForm"/>" /></a>
                  </c:when>
                  <c:when test="${form.published}">
                    <a href="UnpublishForm?Id=${form.id}" title="<fmt:message key="formsOnline.unpublishForm"/>"><img border="0" src="${iconUnpublish}" alt="<fmt:message key="formsOnline.unpublishForm"/>" title="<fmt:message key="formsOnline.unpublishForm"/>" /></a>
                  </c:when>
                  <c:when test="${form.unpublished}">
                    <a href="PublishForm?Id=${form.id}" title="<fmt:message key="formsOnline.republishForm"/>"><img border="0" src="${iconPublish}" alt="<fmt:message key="formsOnline.republishForm"/>" title="<fmt:message key="formsOnline.republishForm"/>" /></a>
                  </c:when>
                </c:choose>
                <a href="javascript:onclick=deleteForm('${form.id}',${form.nbRequests})" title="<fmt:message key="GML.delete"/>"><img border="0" src="${iconDelete}" alt="<fmt:message key="GML.delete"/>" title="<fmt:message key="GML.delete"/>" /></a>
              </div>
            </c:if>
          </li>
        </c:forEach>
      </ul>
    </c:if>
  <div>
</div>
</view:window>
<form name="deleteForm" action="DeleteForm" method="post">
  <input type="hidden" name="FormId"/>
</form>
<view:progressMessage/>
</view:sp-body-part>
</view:sp-page>