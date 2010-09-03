<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="init.jsp" %>
<html>
<head>
<%
	out.println(gef.getLookStyleSheet());

	Form form = (Form)request.getAttribute("form");
	PagesContext context = (PagesContext)request.getAttribute("context");
	DataRecord data = (DataRecord)request.getAttribute("data");
	
	String command = (String)request.getAttribute("command");
	
	form.displayScripts(out, context);
	
	boolean consultation = ("true".equals((String)request.getAttribute("consultation")));
	if (!consultation)
	{
%>
	<script type="text/javascript">
		function processUpdate()
		{
			if (isCorrectForm())
			{
				document.forms["processForm"].submit();
			}
		}

		function updateField(formIndex, fieldName, value)
		{
			var field = document.forms[formIndex].elements[fieldName];
			field.value = value
			field.focus();
		}
	</script>
<%
	}
%>
</head>

<body marginwidth="5" marginheight="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF">
<%
	String pageTitle = new StringBuffer(30)
		.append(resource.getString(consultation ? "PageTitleDataDetail" : "PageTitleDataModification"))
		.append(" (").append(myDBSC.getTableName()).append(")").toString();
	browseBar.setExtraInformation(pageTitle);

	out.println(window.printBefore());
	out.println(frame.printBefore());
%>
	<br>
	<form name="processForm" action="<%=MyDBConstants.ACTION_UPDATE_LINE%>" method="post">
		<input name="command" type="hidden" value="<%=("create".equals(command) ? "create" : "update")%>"/>
		<center><%form.display(out, context, data);%></center>
	</form>
	<br>
	<center>
<%
	ButtonPane buttonPane = gef.getButtonPane();
	if (consultation)
	{
		buttonPane.addButton(gef.getFormButton(resource.getString("ButtonBack"), MyDBConstants.ACTION_MAIN, false));
	}
	else
	{
		buttonPane.addButton(gef.getFormButton(resource.getString("ButtonValidate"), "javascript:processUpdate();", false));
		buttonPane.addButton(gef.getFormButton(resource.getString("ButtonCancel"), MyDBConstants.ACTION_MAIN, false));		
	}
	out.print(buttonPane.print());
%>
	</center>
<%
	String errorMessage = (String)request.getAttribute("errorMessage");
	if (errorMessage != null)
	{
%>
	<br>
	<br>
	<center>
		<a name="err"></a><span class="MessageReadHighPriority"><%=errorMessage%></span>
	</center>
	<script>window.location = "#err";</script>
<%
	}
	
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>