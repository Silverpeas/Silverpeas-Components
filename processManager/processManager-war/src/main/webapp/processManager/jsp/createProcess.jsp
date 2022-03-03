<%@ page import="org.silverpeas.core.contribution.content.form.Form" %><%--

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

<%@ include file="checkProcessManager.jsp" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
	boolean isSaveButtonEnabled = ((Boolean) request.getAttribute("isSaveButtonEnabled")).booleanValue();

   Form form = (Form) request.getAttribute("form");
   PagesContext context = (PagesContext) request.getAttribute("context");
   DataRecord data = (DataRecord) request.getAttribute("data");

	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel,"listProcess");
	  browseBar.setPath(resource.getString("processManager.createProcessBB"));

	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(gef.getFormButton(generalMessage.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK();", false));
	if (isSaveButtonEnabled) {
		buttonPane.addButton(gef.getFormButton(generalMessage.getString("GML.draft.save"), "javascript:onClick=B_SAUVEGARDER_ONCLICK();", false));
	}
	buttonPane.addButton(gef.getFormButton(generalMessage.getString("GML.cancel"), "javascript:onClick=B_ANNULER_ONCLICK();", false));
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<view:includePlugin name="wysiwyg"/>
<%
	form.displayScripts(out, context);
%>
<script type="text/javascript">
<!--
	function B_VALIDER_ONCLICK() {
    ifCorrectFormExecute(function() {
			$.progressMessage();
		  setTimeout("document.<%=context.getFormName()%>.submit();", 500);
		});
	}

	function B_SAUVEGARDER_ONCLICK() {
		$.progressMessage();
		var field = document.getElementById("isDraft");
		field.value = "yes";
	  setTimeout("document.<%=context.getFormName()%>.submit();", 500);
	}

	function B_ANNULER_ONCLICK() {
		location.href = "listProcess?cancel=true";
	}
//-->
</script>
</head>
<body class="yui-skin-sam currentProfile_<%=currentRole%> page_creation">
<%
	out.println(window.printBefore());
%>
<view:frame>
<form name="<%=context.getFormName()%>" method="post" action="saveCreation" enctype="multipart/form-data">
<input type="hidden" name="processManagertokenId" value="${currentTokenId}"/>
<input type="hidden" id="isDraft" name="isDraft" value="No"/>
<input type="hidden" id="isFirstTimeSaved" name="isFirstTimeSaved" value="yes"/>
<%
   form.display(out, context, data);
%>
   <br/>
<%
	out.println(buttonPane.print());
%>
</form>
</view:frame>
<%
   out.println(window.printAfter());
%>
<view:progressMessage/>
</body>
</html>