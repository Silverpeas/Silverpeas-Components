<%--

    Copyright (C) 2000 - 2011 Silverpeas

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

<%@ include file="checkProcessManager.jsp" %>

<%
   com.silverpeas.form.Form form = (com.silverpeas.form.Form) request.getAttribute("form");
   PagesContext context = (PagesContext) request.getAttribute("context");
   DataRecord data = (DataRecord) request.getAttribute("data");

  browseBar.setDomainName(spaceLabel);
  browseBar.setComponentName(componentLabel,"listProcess");
  browseBar.setPath(resource.getString("processManager.userSettingsBB"));
  
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton((Button) gef.getFormButton(
	   generalMessage.getString("GML.validate"),
		"javascript:onClick=B_VALIDER_ONCLICK();",
		false));
	buttonPane.addButton((Button) gef.getFormButton(
	   generalMessage.getString("GML.cancel"),
		"javascript:onClick=B_ANNULER_ONCLICK();",
		false));
%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
	form.displayScripts(out, context);
%>
<SCRIPT language="JavaScript">
<!--
	function B_VALIDER_ONCLICK()
	{
		if (isCorrectForm())
		{
			document.<%=context.getFormName()%>.submit();
		}
	}
	
	function B_ANNULER_ONCLICK() {
		location.href = "listProcess";
	}
	
//-->
</SCRIPT>

</HEAD>
<BODY class="yui-skin-sam" marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
   out.println(window.printBefore());
   out.println(frame.printBefore());
%>
<FORM NAME="<%=context.getFormName()%>" METHOD="POST" ACTION="saveUserSettings" ENCTYPE="multipart/form-data">
<CENTER>
	<table CELLPADDING=0 CELLSPACING=0 BORDER=0 WIDTH="98%">
	<tr>
		<td class="intfdcolor" nowrap width="100%">
			<img border="0" src="<%=resource.getIcon("processManager.px") %>" width="5">
			<span class="txtNav">
           <%=resource.getString("processManager.userSettingsHeader")%>
			</span>
		</td>
	</tr>
	</table>
<%
   form.display(out, context, data);
%>
   <BR>
<%
	out.println(buttonPane.print());
%>
</CENTER>
</FORM>
<%
   out.println(frame.printAfter());
   out.println(window.printAfter());
%>
</BODY>
