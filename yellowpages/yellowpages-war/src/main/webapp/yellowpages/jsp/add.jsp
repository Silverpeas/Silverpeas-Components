<%--

    Copyright (C) 2000 - 2011 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
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
<%@ include file="checkYellowpages.jsp" %>
<%@ include file="topicReport.jsp.inc" %>
<%@ include file="tabManager.jsp.inc" %>

<%
	String newTopicName = (String) request.getParameter("Name");
    String newTopicDescription = (String) request.getParameter("Description");
	NodePK newNodePK = addTopic(yellowpagesScc, newTopicName, newTopicDescription,out);
%>

<HTML>
<HEAD>
<TITLE></TITLE>
<SCRIPT LANGUAGE="JavaScript">

function functionSubmit(){
<%
	if (newNodePK.getId().equals("-1")) {
%>
		self.opener.errorUpdate();
		self.close();
<%
	}
	else{
%>
		window.document.enctypeForm.submit();
<%
	}
%>
}
</SCRIPT>
</HEAD>

<BODY onLoad="functionSubmit()">
	<Form Name="enctypeForm" ACTION="modelManager.jsp" Method="POST" ENCTYPE="multipart/form-data">
		<input type="hidden" name="ContactId" VALUE="<%=newNodePK.getId()%>">
		<input type="hidden" name="Action" VALUE="ModelChoice">
	</Form>
</BODY>
</HTML>
