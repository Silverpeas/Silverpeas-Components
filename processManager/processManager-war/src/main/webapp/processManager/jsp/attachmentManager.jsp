<%@ page import="org.silverpeas.core.workflow.api.instance.ProcessInstance" %>
<%@ page import="org.silverpeas.core.util.URLUtil" %>
<%--

    Copyright (C) 2000 - 2019 Silverpeas

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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkProcessManager.jsp" %>

<%
	ProcessInstance process 			= (ProcessInstance) request.getAttribute("process");
	Boolean 			isHistoryTabEnable 	= (Boolean) request.getAttribute("isHistoryTabEnable");
	boolean				isReturnEnabled = (Boolean) request.getAttribute("isReturnEnabled");
  int nbEntriesAboutQuestions = (Integer) request.getAttribute("NbEntriesAboutQuestions");

	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel,"listProcess");
	browseBar.setPath(process.getTitle(currentRole, language));

	tabbedPane.addTab(resource.getString("processManager.details"), "viewProcess?processId=" + process.getInstanceId(), false, true);
	tabbedPane.addTab(resource.getString("processManager.attachments"), "#", true, true);
	if (isReturnEnabled && nbEntriesAboutQuestions > 0) {
		tabbedPane.addTab(resource.getString("processManager.questions")+" ("+nbEntriesAboutQuestions+")", "listQuestions?processId=" + process.getInstanceId(), false, true);
	}
	if (isHistoryTabEnable)
		tabbedPane.addTab(resource.getString("processManager.history"), "viewHistory?processId=" + process.getInstanceId(), false, true);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel/>
</head>
<body class="currentProfile_<%=currentRole%> page_attachments">
<%
	out.println(window.printBefore());
	out.println(tabbedPane.print());
	out.println(frame.printBefore());
    out.flush();

String url = URLUtil.getNewComponentURL(spaceId, componentId)+"attachmentManager";
getServletConfig().getServletContext().getRequestDispatcher("/attachment/jsp/editAttachedFiles.jsp?Id="+
    org.silverpeas.core.util.URLEncoder.encodeQueryNameOrValue(process.getInstanceId())+"&ComponentId="+
		org.silverpeas.core.util.URLEncoder.encodeQueryNameOrValue(componentId)+"&Url="+
		org.silverpeas.core.util.URLEncoder.encodeQueryNameOrValue(url)).include(request, response);

   out.println(frame.printAfter());
   out.println(window.printAfter());
%>
</body>
</html>