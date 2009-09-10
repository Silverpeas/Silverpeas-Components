<%@ include file="check.jsp" %>

<% 	
	String		query 	= (String) request.getAttribute("Query");
%>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>

<script language="javascript">

</script>
</head>

<body>
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");

	out.println(window.printBefore());
    out.println(frame.printBefore());
	
    out.println("Query = " + query);
    
	out.println(frame.printAfter());
	out.println(window.printAfter());
	
%>

</body>
</html>