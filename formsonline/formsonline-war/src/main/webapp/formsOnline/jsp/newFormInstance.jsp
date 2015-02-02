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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<%@page import="com.silverpeas.formsonline.model.FormDetail"%>
<%@page import="com.silverpeas.form.Form"%>
<%@page import="com.silverpeas.form.PagesContext"%>

<%
	Form formUpdate = (Form) request.getAttribute("Form");
  FormDetail formDetail = (FormDetail) request.getAttribute("FormDetail");

	// context creation
	PagesContext context = (PagesContext) request.getAttribute("FormContext");
	context.setFormName("newInstanceForm");
	context.setFormIndex("0");
	context.setBorderPrinted(false);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<view:looknfeel/>
<% formUpdate.displayScripts(out, context); %>
<script type="text/javascript">
  function sendRequest() {
		if (isCorrectForm()) {
			document.newInstanceForm.submit();
		}
	}
</script>
</head>
<body class="yui-skin-sam">
<view:window>
<view:frame>
<div id="header-OnlineForm">
  <h2 class="title"><%=formDetail.getTitle()%></h2>
</div>
<form name="newInstanceForm" method="post" action="SaveRequest" enctype="multipart/form-data">
	<% 
	formUpdate.display(out, context);
	%>
</form>
</view:frame>

  <view:buttonPane>
    <fmt:message var="buttonValidate" key="formsOnline.request.send"/>
    <fmt:message var="buttonCancel" key="GML.cancel"/>
    <view:button label="${buttonValidate}" action="javascript:onclick=sendRequest();" />
    <view:button label="${buttonCancel}" action="Main" />
  </view:buttonPane>

</view:window>
</body>
</html>