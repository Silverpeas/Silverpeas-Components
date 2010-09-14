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
TaskDetail 	task 	= (TaskDetail) request.getAttribute("Task");
String 		url 	= (String) request.getAttribute("URL");
String		role	= (String) request.getAttribute("Role");
%>
<HTML>
<HEAD>
<TITLE></TITLE>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">
</script>
</HEAD>
<BODY>
<%
    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel, "Main");
    browseBar.setExtraInformation(task.getNom());

    out.println(window.printBefore());

    TabbedPane tabbedPane = gef.getTabbedPane(1);
    
    tabbedPane.addTab(resource.getString("projectManager.Projet"), "ToProject", false);
	tabbedPane.addTab(resource.getString("projectManager.Taches"), "Main", false);
	if ("admin".equals(role))
		tabbedPane.addTab(resource.getString("GML.attachments"), "ToAttachments", true);
	tabbedPane.addTab(resource.getString("projectManager.Commentaires"), "ToComments", false);
	tabbedPane.addTab(resource.getString("projectManager.Gantt"), "ToGantt", false);
	if ("admin".equals(role))
		tabbedPane.addTab(resource.getString("projectManager.Calendrier"), "ToCalendar", false);
	
    out.println(tabbedPane.print());
    out.println(frame.printBefore());

    out.flush();
    
    getServletConfig().getServletContext().getRequestDispatcher("/attachment/jsp/editAttFiles.jsp?Id="+task.getId()+"&ComponentId="+task.getInstanceId()+"&Url="+url).include(request, response);

    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</BODY>
</HTML>