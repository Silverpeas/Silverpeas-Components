<%@ include file="check.jsp" %>

<%
InfoLetter infoLetter = (InfoLetter) request.getAttribute("InfoLetter");
%>
<HTML>
<HEAD>
<TITLE></TITLE>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="Javascript">
</script>
</HEAD>
<BODY>
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "#");
	
	out.println(window.printBefore());
	out.println(frame.printBefore());
	out.flush();
	
	getServletConfig().getServletContext().getRequestDispatcher("/wysiwyg/jsp/htmlDisplayer.jsp?ObjectId="+InfoLetterPublication.TEMPLATE_ID+infoLetter.getPK().getId()+"&ComponentId="+infoLetter.getInstanceId()).include(request, response);

	out.println(frame.printAfter());
	out.println(window.printAfter());
%>

</BODY>
</HTML>