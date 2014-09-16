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
<%@taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ include file="check.jsp" %>

<%@page import="com.silverpeas.form.Form"%>
<%@page import="com.silverpeas.form.DataRecord"%>
<%@page import="com.silverpeas.form.PagesContext"%>
<%
	Form formUpdate = (Form) request.getAttribute("Form");
	DataRecord data = (DataRecord) request.getAttribute("Data");
	
	PagesContext context = new PagesContext("myForm", "0", resource.getLanguage(), false, componentId, "useless");
 	context.setObjectId("0");
	context.setBorderPrinted(false);
%>

<html>
<head>
<view:looknfeel/>
<% formUpdate.displayScripts(out, context);%>
<script type="text/javascript">
function save() {
	if (isCorrectForm()) {
    	$.progressMessage();
    	document.myForm.submit();
  	}
}

function cancel() {
	location.href = "Main";
}
</script>
</head>
<body class="yui-skin-sam">
<%
	out.println(window.printBefore());

	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resource.getString("webPages.preview"), "Preview", false);
	tabbedPane.addTab(resource.getString("webPages.edit"), "Edit", true);
	out.println(tabbedPane.print());
	
	out.println(frame.printBefore());

%>
	<table width="100%" border="0">
	<tr><td id="richContent">
		<form name="myForm" method="post" action="UpdateXMLContent" enctype="multipart/form-data" accept-charset="UTF-8">
      	<%
            formUpdate.display(out, context, data);
      	%>
    	</form>
	</td></tr>
	</table>
<%	

	ButtonPane buttonPane = gef.getButtonPane();
  	buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=save();", false));
  	buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=cancel();", false));
	out.println("<br/><center>" + buttonPane.print() + "</center>");

	out.println(frame.printAfter());
	out.println(window.printAfter());
%>	
<view:progressMessage/>
</body>
</html>