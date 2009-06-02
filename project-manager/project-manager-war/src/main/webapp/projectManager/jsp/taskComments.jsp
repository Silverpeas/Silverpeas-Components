<%@ include file="check.jsp" %>

<%
TaskDetail 		task 		= (TaskDetail) request.getAttribute("Task");
String 			url 		= (String) request.getAttribute("URL");
String 			userId 		= (String) request.getAttribute("UserId");
Boolean			showAttTab 	= (Boolean) request.getAttribute("AbleToAddAttachments");
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
</BODY>
</HTML>