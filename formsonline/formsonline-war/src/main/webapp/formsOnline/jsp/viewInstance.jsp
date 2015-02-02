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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@page import="com.silverpeas.form.Form"%>
<%@page import="com.silverpeas.form.PagesContext"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<c:set var="lang" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<c:set var="currentUser" value="${sessionScope['SilverSessionController'].currentUserDetail}"/>

<fmt:setLocale value="${lang}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<c:set var="userRequest" value="${requestScope['UserRequest']}"/>
<c:set var="validationEnabled" value="${requestScope['ValidationEnabled']}"/>
<c:set var="form" value="${requestScope['FormDetail']}"/>
<c:set var="origin" value="${requestScope['Origin']}"/>

<fmt:message var="buttonBack" key="GML.back"/>

<%
	Form        formView  = (Form) request.getAttribute("Form");

	// context creation
	PagesContext context = (PagesContext) request.getAttribute("FormContext");
	context.setFormName("newInstanceForm");
	context.setFormIndex("0");
	context.setBorderPrinted(false);
%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title></title>
  <view:looknfeel />
  <% formView.displayScripts(out, context); %>
  <script type="text/javascript">
	  function validate() {
		  document.validationForm.decision.value = "validate";
		  document.validationForm.submit();
	  }

	  function refuse() {
		  document.validationForm.decision.value = "refuse";
		  document.validationForm.submit();
	  }

    function deleteRequest() {
      document.requestForm.action = "DeleteRequest";
      document.requestForm.submit();
    }

    function archive() {
      document.requestForm.action = "ArchiveRequest";
      document.requestForm.submit();
    }
  </script>
</head>
<body>
    <view:operationPane>
      <c:choose>
        <c:when test="${userRequest.creatorId == currentUser.id}">
          <c:if test="${userRequest.denied || userRequest.validated}">
            <fmt:message var="opArchive" key="formsOnline.request.action.archive"/>
            <view:operation action="javascript:archive()" altText="${opArchive}"/>
          </c:if>
        </c:when>
        <c:otherwise>
          <c:if test="${userRequest.archived}">
            <fmt:message var="opDelete" key="GML.delete"/>
            <view:operation action="javascript:deleteRequest()" altText="${opDelete}"/>
          </c:if>
        </c:otherwise>
      </c:choose>
    </view:operationPane>
  <view:window>

  <div id="header-OnlineForm">
    <h2 class="title">${form.title}</h2>
    <div id="ask-by" class="bgDegradeGris">
      <fmt:message key="formsOnline.request.from"/> <view:username userId="${userRequest.creatorId}"/>
      <div class="profilPhoto"><view:image src="${userRequest.creator.avatar}" alt="" type="avatar" /></div>
      <div class="ask-date"><fmt:message key="GML.date.the"/> <view:formatDate value="${userRequest.creationDate}"/></div>
    </div>

    <c:choose>
      <c:when test="${userRequest.canBeValidated}">
        <div id="ask-statut" class="inlineMessage"><fmt:message key="GML.contribution.validation.status.PENDING_VALIDATION"/></div>
      </c:when>
      <c:when test="${userRequest.validated}">
        <div id="ask-statut" class="commentaires">
          <div class="inlineMessage-ok oneComment">
            <p class="author"><fmt:message key="GML.contribution.validation.status.VALIDATED"/> <fmt:message key="GML.date.the"/> <view:formatDate value="${userRequest.validationDate}"/> <fmt:message key="GML.by"/> <view:username userId="${userRequest.validatorId}"/></p>
            <div class="avatar"><view:image src="${userRequest.validator.avatar}" alt="" type="avatar" /></div>
            <div>
              <p>${silfn:escapeHtmlWhitespaces(userRequest.comments)}</p>
            </div>
          </div>
        </div>
      </c:when>
      <c:when test="${userRequest.denied}">
        <div id="ask-statut" class="commentaires">
          <div class="inlineMessage-nok oneComment">
            <p class="author"><fmt:message key="GML.contribution.validation.status.REFUSED"/> <fmt:message key="GML.date.the"/> <view:formatDate value="${userRequest.validationDate}"/> <fmt:message key="GML.by"/> <view:username userId="${userRequest.validatorId}"/></p>
            <div class="avatar"><view:image src="${userRequest.validator.avatar}" alt="" type="avatar" /></div>
            <div>
              <p>${silfn:escapeHtmlWhitespaces(userRequest.comments)}</p>
            </div>
          </div>
        </div>
      </c:when>
    </c:choose>
  </div>

	<%
	  formView.display(out, context);
	%>

  <c:if test="${validationEnabled}">
    <div class="commentaires">
      <div id="edition-box">
        <p class="title">Commentez votre décision</p>
        <div class="avatar"><view:image src="${currentUser.avatar}" type="avatar"/> </div>
        <form name="validationForm" action="EffectiveValideForm" method="post">
          <input type="hidden" name="Id" value="${userRequest.id}"/>
          <input type="hidden" name="Origin" value="${origin}"/>
          <input type="hidden" name="decision" value=""/>
          <textarea name="comment"  style="resize: none; overflow-y: hidden; height: 60px;" class="text"></textarea>
        </form>
      </div>
    </div>
    <br/>
    <view:buttonPane>
      <fmt:message var="buttonValidate" key="GML.accept"/>
      <fmt:message var="buttonDeny" key="GML.refuse"/>
      <view:button label="${buttonValidate}" action="javascript:validate();" />
      <view:button label="${buttonDeny}" action="javascript:refuse();" />
      <view:button label="${buttonBack}" action="${origin}" />
    </view:buttonPane>
  </c:if>
  <c:if test="${not validationEnabled}">
    <br/>
    <view:buttonPane>
      <view:button label="${buttonBack}" action="${origin}" />
    </view:buttonPane>
  </c:if>

  </view:window>
<form name="requestForm" action="" method="post">
  <input type="hidden" name="Id" value="${userRequest.id}"/>
</form>
</body>
</html>