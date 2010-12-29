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

<%@ include file="check.jsp" %>

<%
TaskDetail 		task 		= (TaskDetail) request.getAttribute("Task");
String 			url 		= (String) request.getAttribute("URL");
String 			userId 		= (String) request.getAttribute("UserId");
Boolean			showAttTab 	= (Boolean) request.getAttribute("AbleToAddAttachments");
%>
<html>
<head>
<title></title>
<%
    out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">
</script>

</head>
<body>
<%
    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel, "Main");
    browseBar.setExtraInformation(task.getNom());
    
    out.println(window.printBefore());
    
    TabbedPane tabbedPane = gef.getTabbedPane(1);
    
    tabbedPane.addTab(resource.getString("projectManager.Definition"), "ViewTask?Id="+task.getId(), false);
    if (showAttTab.booleanValue())
		tabbedPane.addTab(resource.getString("GML.attachments"), "ToTaskAttachments", false);
	tabbedPane.addTab(resource.getString("projectManager.Commentaires"), "#", true);
	
    out.println(tabbedPane.print());
    
    out.println(frame.printBefore());
    out.flush();

    getServletConfig().getServletContext().getRequestDispatcher("/comment/jsp/comments.jsp?id="+task.getId()+"&component_id="+task.getInstanceId()+"&userid="+userId+"&url="+url).include(request, response);

    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</body>
</html>