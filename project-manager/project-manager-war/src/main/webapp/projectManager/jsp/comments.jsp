<%@ include file="check.jsp" %>

<%
String	instanceId 	= (String) request.getAttribute("InstanceId");
String 	url 		= (String) request.getAttribute("URL");
String 	userId 		= (String) request.getAttribute("UserId");
String 	role 		= (String) request.getAttribute("Role");
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
    
    out.println(window.printBefore());
    
    TabbedPane tabbedPane = gef.getTabbedPane();
    
	tabbedPane.addTab(resource.getString("projectManager.Projet"), "ToProject", false);
    tabbedPane.addTab(resource.getString("projectManager.Taches"), "Main", false);
    if ("admin".equals(role))
		tabbedPane.addTab(resource.getString("GML.attachments"), "ToAttachments", false);
	tabbedPane.addTab(resource.getString("projectManager.Commentaires"), "#", true);
	tabbedPane.addTab(resource.getString("projectManager.Gantt"), "ToGantt", false);
	if ("admin".equals(role))
		tabbedPane.addTab(resource.getString("projectManager.Calendrier"), "ToCalendar", false);
	
    out.println(tabbedPane.print());
    
    out.println(frame.printBefore());
    out.flush();

    getServletConfig().getServletContext().getRequestDispatcher("/comment/jsp/comments.jsp?id=-1&component_id="+instanceId+"&userid="+userId+"&url="+url).include(request, response);

    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</BODY>
</HTML>