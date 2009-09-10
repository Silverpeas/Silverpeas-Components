<%@ include file="check.jsp" %>

<% 
	// récupération des paramètres :
	String url = (String) request.getAttribute("Url");
%>

<%@page import="java.net.URL"%>
<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>

</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<%
	
	out.println(window.printBefore());
    out.println(frame.printBefore());
%>
<table CELLPADDING=5 WIDTH="100%">

		<!-- AFFICHAGE de la photo -->
      	<td> 
			<IMG SRC="<%=url%>">
		</td>

</table>
<% 
  	out.println(frame.printAfter());
	out.println(window.printAfter());
%>	
</body>
</html>