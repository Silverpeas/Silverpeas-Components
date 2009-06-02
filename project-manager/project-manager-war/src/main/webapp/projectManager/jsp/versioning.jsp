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