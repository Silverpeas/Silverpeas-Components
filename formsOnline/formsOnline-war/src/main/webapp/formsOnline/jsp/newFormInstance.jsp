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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/formsOnline" prefix="formsOnline" %>

<c:set var="currentUser" value="${sessionScope['SilverSessionController'].currentUserDetail}"/>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<%@page import="org.silverpeas.components.formsonline.model.FormDetail"%>
<%@page import="org.silverpeas.core.contribution.content.form.Form"%>
<%@page import="org.silverpeas.core.contribution.content.form.PagesContext"%>

<c:set var="formDetail" value="${requestScope['FormDetail']}"/>
<jsp:useBean id="formDetail" type="org.silverpeas.components.formsonline.model.FormDetail"/>

<%
	Form formUpdate = (Form) request.getAttribute("Form");

	// context creation
	PagesContext context = (PagesContext) request.getAttribute("FormContext");
	context.setFormName("newInstanceForm");
	context.setFormIndex("0");
	context.setBorderPrinted(false);
%>

<view:sp-page>
<view:sp-head-part>
  <view:includePlugin name="wysiwyg"/>
  <% formUpdate.displayScripts(out, context); %>
  <script type="text/javascript">
    function sendRequest() {
      ifCorrectFormExecute(function() {
        spProgressMessage.show();
        document.newInstanceForm.submit();
      });
    }

    function saveDraft() {
      ifCorrectFormAndIgnoringMandatoryExecute(function() {
        spProgressMessage.show();
        document.newInstanceForm.action = "SaveRequestAsDraft";
        document.newInstanceForm.submit();
      });
    }
  </script>
</view:sp-head-part>
<view:sp-body-part cssClass="yui-skin-sam">
<view:window>
<view:frame>
<div id="header-OnlineForm">
  <h2 class="title">${formDetail.title}</h2>
  <div>${formDetail.description}</div>
</div>

<formsOnline:hierarchicalInfo formDetail="${formDetail}"/>

<form name="newInstanceForm" method="post" action="SaveRequest" enctype="multipart/form-data">
	<%
	formUpdate.display(out, context);
	%>
</form>
</view:frame>

  <view:buttonPane>
    <fmt:message var="buttonDraft" key="GML.draft.save"/>
    <fmt:message var="buttonValidate" key="formsOnline.request.send"/>
    <fmt:message var="buttonCancel" key="GML.cancel"/>
    <c:if test="${formDetail.canBeSentBy(currentUser)}">
      <view:button label="${buttonValidate}" action="javascript:sendRequest();" classes="validateButton" />
      <view:button label="${buttonDraft}" action="javascript:saveDraft();" />
    </c:if>
    <view:button label="${buttonCancel}" action="Main" />
  </view:buttonPane>

</view:window>
<view:progressMessage/>
</view:sp-body-part>
</view:sp-page>