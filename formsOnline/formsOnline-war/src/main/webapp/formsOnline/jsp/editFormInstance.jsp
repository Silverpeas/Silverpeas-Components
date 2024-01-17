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
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/formsOnline" prefix="formsOnline" %>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<%@page import="org.silverpeas.components.formsonline.model.FormDetail"%>
<%@page import="org.silverpeas.core.contribution.content.form.Form"%>
<%@page import="org.silverpeas.core.contribution.content.form.PagesContext"%>
<%@page import="org.silverpeas.components.formsonline.model.FormInstance" %>

<%
  FormInstance userRequest = (FormInstance) request.getAttribute("UserRequest");
	Form formUpdate = userRequest.getFormWithData();
  FormDetail formDetail = userRequest.getForm();

	// context creation
	PagesContext context = (PagesContext) request.getAttribute("FormContext");
	context.setFormName("newInstanceForm");
%>

<fmt:message var="deletionConfirmMessage" key="formsOnline.request.action.delete.confirm"/>

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

    function deleteDraft() {
      jQuery.popup.confirm('${silfn:escapeJs(deletionConfirmMessage)}', function() {
        spProgressMessage.show();
        document.newInstanceForm.action = "DeleteRequest";
        document.newInstanceForm.submit();
      });
    }
  </script>
</view:sp-head-part>
<view:sp-body-part cssClass="yui-skin-sam">
<view:window>
<view:frame>
<div id="header-OnlineForm">
  <h2 class="title"><%=formDetail.getTitle()%></h2>
  <div><%=formDetail.getDescription()%></div>
</div>

<formsOnline:hierarchicalInfo formDetail="<%=formDetail%>"/>

<form name="newInstanceForm" method="post" action="SaveRequest" enctype="multipart/form-data">
  <input type="hidden" name="Id" value="<%=userRequest.getId()%>"/>
	<%
	formUpdate.display(out, context);
	%>
</form>

  <view:buttonPane>
    <fmt:message var="buttonDraft" key="GML.draft.save"/>
    <fmt:message var="buttonValidate" key="formsOnline.request.send"/>
    <fmt:message var="buttonBack" key="GML.back"/>
    <fmt:message var="buttonDelete" key="GML.delete"/>
    <view:button label="${buttonValidate}" action="javascript:sendRequest();" classes="validateButton"/>
    <view:button label="${buttonDraft}" action="javascript:saveDraft();" />
    <view:button label="${buttonDelete}" action="javascript:deleteDraft();" />
    <view:button label="${buttonBack}" action="Main" />
  </view:buttonPane>

</view:frame>
</view:window>
<view:progressMessage/>
</view:sp-body-part>
</view:sp-page>
