<%@ include file="check.jsp" %>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
</head>
<body onload="window.document.redirectForm.submit()">
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel);

	out.println(window.printBefore());
	out.println(frame.printBefore());
	out.println(board.printBefore());
	
	out.print(resource.getString("hyperlink.explications"));
	out.print("<a href=\"GoToURL\" target=\"_blank\">");
	out.print(resource.getString("hyperlink.ici"));
	out.print("</a>");
	
	out.println(board.printAfter());
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
<form name="redirectForm" action="GoToURL" target="_blank">
</form>
</body>
</html>