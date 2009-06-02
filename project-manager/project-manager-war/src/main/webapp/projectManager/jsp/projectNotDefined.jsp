<%@ include file="check.jsp" %>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<%
browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel, "Main");

out.println(window.printBefore());

TabbedPane tabbedPane = gef.getTabbedPane();
tabbedPane.addTab(resource.getString("projectManager.Projet"), "#", true);
tabbedPane.addTab(resource.getString("projectManager.Taches"), "#", false);
tabbedPane.addTab(resource.getString("projectManager.Commentaires"), "#", false);
tabbedPane.addTab(resource.getString("projectManager.Gantt"), "#", false);
out.println(tabbedPane.print());

out.println(frame.printBefore());

Board board = gef.getBoard();
out.println(board.printBefore());
%>
<table CELLPADDING=5>
<TR>
	<TD class="txtlibform"><%=resource.getString("projectManager.NotDefined")%></TD>
</TR>
</table>
<%
out.println(board.printAfter());
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>