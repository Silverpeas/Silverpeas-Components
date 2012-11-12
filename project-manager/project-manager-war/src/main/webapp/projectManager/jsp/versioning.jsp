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

<%@ include file="check.jsp" %>

<%
String url 	= (String) request.getAttribute("URL");
String role = (String) request.getAttribute("Role");

String instanceId = browseContext[3];
%>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<%
browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel);

out.println(window.printBefore());

TabbedPane tabbedPane = gef.getTabbedPane();
tabbedPane.addTab(resource.getString("projectManager.Tasks"), "Main", false);
tabbedPane.addTab(resource.getString("projectManager.PVSeances"), "PVs", true);
tabbedPane.addTab(resource.getString("projectManager.Commentaires"), "ToComments", false);
tabbedPane.addTab(resource.getString("projectManager.Gantt"), "ToGantt", false);
out.println(tabbedPane.print());

out.println(frame.printBefore());

out.flush();

if (role.equals("lecteur")) {
	getServletConfig().getServletContext().getRequestDispatcher("/versioningPeas/jsp/readOnly.jsp?Id=-1&ComponentId="+instanceId+"&Url="+URLEncoder.encode(url)+"&SL="+URLEncoder.encode(spaceLabel)+"&CL="+URLEncoder.encode(componentLabel)).include(request, response);
} else {
	getServletConfig().getServletContext().getRequestDispatcher("/versioningPeas/jsp/documents.jsp?Id=-1&ComponentId="+instanceId+"&Url="+URLEncoder.encode(url)+"&SL="+URLEncoder.encode(spaceLabel)+"&CL="+URLEncoder.encode(componentLabel)+"&profile=admin").include(request, response);
}

out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>