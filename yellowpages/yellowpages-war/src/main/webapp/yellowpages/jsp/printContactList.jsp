<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkYellowpages.jsp" %>
<%@ include file="contactsList.jsp.inc" %>

<%
Collection contacts = (Collection) request.getAttribute("Contacts");
%>

<html>
<head>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
</head>
<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5>
<%
Window window = gef.getWindow();

BrowseBar browseBar=window.getBrowseBar();
browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel);
browseBar.setPath(resources.getString("GML.print"));

OperationPane operationPane = window.getOperationPane();
operationPane.addOperation(resources.getIcon("yellowpages.printPage"), resources.getString("GML.print"), "javaScript:window.print();");

Frame frame=gef.getFrame();
Board board = gef.getBoard();

out.println(window.printBefore());
out.println(frame.printBefore());
out.println(board.printBefore());

displayContactsUser(yellowpagesScc, contacts, null, componentLabel, gef, request, session, resources, out);

out.println(board.printAfter());
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>