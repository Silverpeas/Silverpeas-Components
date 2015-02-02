<%--

    Copyright (C) 2000 - 2013 Silverpeas

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

<c:set var="lang" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>

<fmt:setLocale value="${lang}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<c:set var="forms" value="${requestScope['formsList']}"/>
<c:set var="userRequests" value="${requestScope['UserRequests']}"/>
<c:set var="requestsAsValidator" value="${requestScope['RequestsAsValidator']}"/>
<c:set var="role" value="${requestScope['Role']}"/>
<c:set var="app" value="${requestScope['App']}"/>

<c:url var="iconAdd" value="/util/icons/create-action/add-form.png"/>
<c:url var="iconEdit" value="/util/icons/update.gif"/>
<c:url var="iconPublish" value="/util/icons/lock.gif"/>
<c:url var="iconUnpublish" value="/util/icons/unlock.gif"/>
<c:url var="iconDelete" value="/util/icons/delete.gif"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<view:looknfeel/>
<script type="text/javascript">
  function deleteForm(idModel) {
    if (window.confirm("<fmt:message key="formsOnline.deleteFormConfirm"/>")) {
      document.deleteForm.formId.value = idModel;
      document.deleteForm.submit();
    }
  }

  function allRequests() {
    location.href = "Inbox";
  }
</script>
</head>
<body>
<view:browseBar/>
<c:if test="${role == 'admin'}">
  <view:operationPane>
    <fmt:message var="addForm" key="formsOnline.createForm"/>
    <view:operationOfCreation action="CreateForm" icon="${iconAdd}" altText="${addForm}"/>
  </view:operationPane>
</c:if>
<view:window>

<h2 class="formsOnline-title">${app.getLabel(lang)}</h2>

<c:if test="${not empty app.getDescription(lang)}">
<div class="formsOnline-description">
  <p>${app.getDescription(lang)}</p>
</div>
</c:if>

<c:choose>
  <c:when test="${role == 'senderOnly'}">
    <div id="my-formsOnline" class="lecteur-view">
  </c:when>
  <c:otherwise>
    <div id="my-formsOnline">
  </c:otherwise>
</c:choose>

  <c:if test="${requestsAsValidator != null}">
  <div class="secteur-container formsOnline-waitMyAction">
    <div class="header">
      <c:choose>
        <c:when test="${empty requestsAsValidator.toValidate}">
          <h3 class="formsOnline-waitMyAction-title"><fmt:message key="formsOnline.home.toValidate.none.title"/></h3>
        </c:when>
        <c:otherwise>
          <h3 class="formsOnline-waitMyAction-title"><strong>${fn:length(requestsAsValidator.toValidate)} </strong><fmt:message key="formsOnline.home.toValidate.title"><fmt:param value="${fn:length(requestsAsValidator.toValidate)}"/> </fmt:message></h3>
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
        <a href="ViewRequest?Id=${request.id}"><span class="form-title">${request.form.title}</span><span class="ask-form-author"><view:username userId="${request.creatorId}"/></span><span class="ask-form-date"><view:formatDate value="${request.creationDate}"/></span></a></li>
      </c:forEach>
    </ul>
    <a href="InBox" class="more"> <fmt:message key="formsOnline.home.requests.toggle.more"/></a>
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
      <c:forEach items="${userRequests.toValidate}" var="request">
        <li>
          <a href="ViewRequest?Id=${request.id}"><span class="ask-form-date"><view:formatDate value="${request.creationDate}"/></span><span class="form-title">${request.form.title}</span></a>
          <c:choose>
            <c:when test="${request.read}">
              <span class="form-statut"><fmt:message key="formsOnline.stateRead"/></span>
            </c:when>
            <c:otherwise>
              <span class="form-statut"><fmt:message key="formsOnline.stateUnread"/></span>
            </c:otherwise>
          </c:choose>
        </li>
      </c:forEach>
    </ul>
  </div>

  <c:if test="${not empty userRequests.validated}">
  <div class="secteur-container my-formsOnline" id="my-formsOnline-validate">
    <div class="header">
      <h3 class="my-formsOnline-title"><fmt:message key="formsOnline.home.requests.mine"/> <strong><fmt:message key="formsOnline.home.requests.mine.validated"/></strong></h3>
    </div>
    <ul>
      <c:forEach items="${userRequests.validated}" var="request">
        <li><a href="ViewRequest?Id=${request.id}"><span class="ask-form-date"><view:formatDate value="${request.creationDate}"/></span><span class="form-title">${request.form.title}</span></a></li>
      </c:forEach>
    </ul>
  </div>
  </c:if>

  <c:if test="${not empty userRequests.denied}">
  <div class="secteur-container my-formsOnline" id="my-formsOnline-refused">
    <div class="header">
      <h3 class="my-formsOnline-title"><fmt:message key="formsOnline.home.requests.mine"/> <strong><fmt:message key="formsOnline.home.requests.mine.denied"/></strong></h3>
    </div>
    <ul>
      <c:forEach items="${userRequests.denied}" var="request">
        <li><a href="ViewRequest?Id=${request.id}"><span class="ask-form-date"><view:formatDate value="${request.creationDate}"/></span><span class="form-title">${request.form.title}</span></a></li>
      </c:forEach>
    </ul>
  </div>
  </c:if>

  <c:if test="${not empty userRequests.archived}">
    <div class="secteur-container my-formsOnline" id="my-formsOnline-archived">
      <div class="header">
        <h3 class="my-formsOnline-title"><fmt:message key="formsOnline.home.requests.mine"/> <strong><fmt:message key="formsOnline.home.requests.mine.archived"/></strong></h3>
      </div>
      <ul>
        <c:forEach items="${userRequests.archived}" var="request">
          <li><a href="ViewRequest?Id=${request.id}"><span class="ask-form-date"><view:formatDate value="${request.creationDate}"/></span><span class="form-title">${request.form.title}</span></a></li>
        </c:forEach>
      </ul>
    </div>
  </c:if>
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
          <li>
            <c:if test="${form.sendable}">
            <a href="NewRequest?FormId=${form.id}">
              </c:if>
              <span class="form-title">${form.title}</span>
              <span class="form-description">${silfn:escapeHtmlWhitespaces(form.description)}</span>
              <c:if test="${form.sendable}">
            </a>
            </c:if>
            <c:if test="${role == 'admin'}">
              <div class="operation">
                <a href="EditForm?formId=${form.id}" title="<fmt:message key="GML.modify"/>"><img border="0" src="${iconEdit}" alt="<fmt:message key="GML.modify"/>" title="<fmt:message key="GML.modify"/>" /></a>
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
                <c:if test="${not form.alreadyUsed}">
                  <a href="javascript:onclick=deleteForm('${form.id}')" title="<fmt:message key="GML.delete"/>"><img border="0" src="${iconDelete}" alt="<fmt:message key="GML.delete"/>" title="<fmt:message key="GML.delete"/>" /></a>
                </c:if>
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
  <input type="hidden" name="formId"/>
</form>
</body>
</html>