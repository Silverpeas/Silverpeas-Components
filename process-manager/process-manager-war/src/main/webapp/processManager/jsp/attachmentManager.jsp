<%@ page import="org.silverpeas.util.URLUtils" %>
<%--

    Copyright (C) 2000 - 2012 Silverpeas

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

<%
	ProcessInstance 	process 			= (ProcessInstance) request.getAttribute("process");
	String 				versionning 		= (String) request.getAttribute("isVersionControlled");
	Boolean 			isHistoryTabEnable 	= (Boolean) request.getAttribute("isHistoryTabEnable");
	boolean				isReturnEnabled = ((Boolean) request.getAttribute("isReturnEnabled")).booleanValue();

	boolean isVersionControlled = false;
	if (versionning.equals("1"))
		isVersionControlled = true;

	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel,"listProcess");
	browseBar.setPath(process.getTitle(currentRole, language));

	tabbedPane.addTab(resource.getString("processManager.details"), "viewProcess?processId=" + process.getInstanceId(), false, true);
	tabbedPane.addTab(resource.getString("processManager.attachments"), "", true, false);
	tabbedPane.addTab(resource.getString("processManager.actions"), "listTasks", false, true);
	if (isReturnEnabled) {
		tabbedPane.addTab(resource.getString("processManager.questions"), "listQuestions?processId=" + process.getInstanceId(), false, true);
	}
	if (isHistoryTabEnable.booleanValue())
		tabbedPane.addTab(resource.getString("processManager.history"), "viewHistory?processId=" + process.getInstanceId(), false, true);
%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>

<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
</HEAD>

<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
	out.println(window.printBefore());
	out.println(tabbedPane.print());
	out.println(frame.printBefore());
    out.flush();
%>
<CENTER>

<br><br>

<%

String url = URLManager.getNewComponentURL(spaceId, componentId)+"attachmentManager";
getServletConfig().getServletContext().getRequestDispatcher("/attachment/jsp/editAttachedFiles.jsp?Id="+
    URLUtils.encodeQueryNameOrValue(process.getInstanceId())+"&SpaceId="+URLUtils.encodeQueryNameOrValue(spaceId)+"&ComponentId="URLUtils.encodeQueryNameOrValue(componentId)+"&Context=attachment"+"&Url="+URLUtils.encodeQueryNameOrValue(url)).include(request, response);

%>
</CENTER>
<%
   out.println(frame.printAfter());
   out.println(window.printAfter());
%>
</BODY>
