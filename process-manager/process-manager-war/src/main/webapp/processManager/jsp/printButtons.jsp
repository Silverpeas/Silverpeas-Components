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

<%@ include file="checkProcessManager.jsp" %>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">

<table CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%">
	<tr>
		<td><img src="<%=resource.getIcon("processManager.px") %>"></td>
	</tr>
	<tr>			
		<td align="center">
<%
		ButtonPane buttonPane = gef.getButtonPane();

		buttonPane.addButton((Button) gef.getFormButton(resource.getString("processManager.print"), "javascript:window.top.contentFrame.printProcess()" , false));			

		buttonPane.addButton((Button) gef.getFormButton(resource.getString("processManager.close"), "javascript:window.top.close()" , false));
		
		out.println(buttonPane.print());
%>  
		</td>
	</tr>
	<tr>
		<td><img src="<%=resource.getIcon("processManager.px") %>"></td>
	</tr>
</table>

</BODY>
</HTML>