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

<%@ page import="java.util.*"%>

<%@ include file="checkQuestionReply.jsp" %>
<%
	Reply reply = (Reply) request.getAttribute("reply");
	String title = EncodeHelper.javaStringToHtmlString(reply.getTitle());
	String content = EncodeHelper.javaStringToHtmlParagraphe(reply.getContent());
	String date = resource.getOutputDate(reply.getCreationDate());
	String id = reply.getPK().getId();
	String creator = EncodeHelper.javaStringToHtmlString(reply.readCreatorName());
%>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">

<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel);
	browseBar.setPath(resource.getString("questionReply.consult"));
	
	operationPane.addOperation(resource.getIcon("questionReply.delR"), resource.getString("questionReply.delR"), "javascript:DeleteR('"+id+"');");		

	out.println(window.printBefore());
	out.println(frame.printBefore());
	out.println(board.printBefore());
%>

<center>
<table CELLPADDING=5 width="100%">
	<tr align=center> 
		<td class="intfdcolor4" valign="baseline" align=left class="txtlibform" width="100"><%=resource.getString("questionReply.reponse")%> :</td>
		<td class="intfdcolor4" valign="baseline" align=left><%=title%></td>
	</tr>
	<tr align=center> 
		<td class="intfdcolor4" valign="top" align=left class="txtlibform"><%=resource.getString("GML.description")%> :</td>
		<td class="intfdcolor4" valign="top" align=left><%=content%></td>
	</tr>
	<tr align=center> 
		<td class="intfdcolor4" valign="baseline" align=left class="txtlibform"><%=resource.getString("GML.date")%> :</td>
		<td class="intfdcolor4" valign="baseline" align=left><%=date%></td>
	</tr>
	<tr align=center> 
		<td class="intfdcolor4" valign="baseline" align=left class="txtlibform"><%=resource.getString("GML.publisher")%> :</td>
		<td class="intfdcolor4" valign="baseline" align=left><%=creator%></td>
	</tr>
</table>
</CENTER>

<%
out.println(board.printAfter());
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</BODY>
</HTML>