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
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ page import="org.silverpeas.core.contribution.content.form.DataRecord"%>
<%@ page import="org.silverpeas.core.contribution.content.form.Form"%>
<%@ page import="org.silverpeas.core.contribution.content.form.PagesContext"%>
<%@ page import="org.silverpeas.components.whitepages.model.Card" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane" %>

<%@ include file="checkWhitePages.jsp" %>

<%
	Card card		= (Card) request.getAttribute("card");
	Form			updateForm	= (Form) request.getAttribute("Form");
	PagesContext	context		= (PagesContext) request.getAttribute("context");
	DataRecord		data		= (DataRecord) request.getAttribute("data");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<%
   updateForm.displayScripts(out, context);
%>
<view:includePlugin name="wysiwyg"/>
<script language="JavaScript">
<!--
	function B_VALIDER_ONCLICK(idCard) {
		ifCorrectFormExecute(function() {
			document.myForm.action = "<%=routerUrl%>effectiveUpdate?userCardId="+idCard;
			document.myForm.submit();
		});
	}

	function B_ANNULER_ONCLICK(idCard) {
		self.close();
	}
//-->
</script>
</head>
<body class="yui-skin-sam">
<view:browseBar path='<%=resource.getString("whitePages.usersList") + " > "+ resource.getString("whitePages.editCard")%>'/>
<view:window popup="true">
<view:frame>

<form name="myForm" method="post" enctype="multipart/form-data">

<%
	updateForm.display(out, context, data);
%>
</form>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK('"+card.getPK().getId()+"');", false));
    buttonPane.addButton(gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=B_ANNULER_ONCLICK('"+card.getPK().getId()+"');", false));
    out.println(buttonPane.print());
%>
</view:frame>
</view:window>
</body>
</html>