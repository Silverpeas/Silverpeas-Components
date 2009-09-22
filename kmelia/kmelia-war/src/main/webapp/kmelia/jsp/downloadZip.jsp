<%@ include file="checkKmelia.jsp" %>
<%
	List report = (List) request.getAttribute("ZipReport");

	String zipName 	= (String) report.get(0);
	String zipSize 	= (String) report.get(1);
	String zipURL	= (String) report.get(2);
%>
<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
</head>
<body>
<%
Window window = gef.getWindow();
Frame frame = gef.getFrame();
Board board = gef.getBoard();

BrowseBar browseBar = window.getBrowseBar();

browseBar.setComponentName(resources.getString("GML.ExportSucceeded"));

out.println(window.printBefore());
out.println(frame.printBefore());
out.println(board.printBefore());
%>
<table cellpadding="3" border="0">
<tr><td class="txtlibform" style="vertical-align:middle;"><%=resources.getString("GML.file")%> :</td><td><a href="<%=zipURL%>"><%=zipName%></a> <a href="<%=zipURL%>"><img src="<%=FileRepositoryManager.getFileIcon("zip")%>" border="0" align="absmiddle"></a></td></tr>
<tr><td class="txtlibform"><%=resources.getString("GML.size")%> :</td><td><%=FileRepositoryManager.formatFileSize(Long.parseLong(zipSize))%></td></tr>
</table>
<%
out.println(board.printAfter());
ButtonPane buttonPane = gef.getButtonPane();
Button button = (Button) gef.getFormButton(resources.getString("GML.close"), "javaScript:window.close();", false);
buttonPane.addButton(button);
out.println("<br/><center>"+buttonPane.print()+"</center>");
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>