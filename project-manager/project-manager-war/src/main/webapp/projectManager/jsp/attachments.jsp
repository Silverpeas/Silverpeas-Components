<%@ include file="check.jsp" %>

<%
TaskDetail 	task 	= (TaskDetail) request.getAttribute("Task");
String 		url 	= (String) request.getAttribute("URL");
String		role	= (String) request.getAttribute("Role");
%>
<HTML>
<HEAD>
<TITLE></TITLE>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
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