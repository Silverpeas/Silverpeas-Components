<%@ include file="checkCrm.jsp" %>

<%@ page import="java.net.URLEncoder"%>
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.*"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%
	String elmtId = request.getParameter("elmtId");
	String elmtType = request.getParameter("elmtType");
	String returnAction = request.getParameter("returnAction");
	String returnId = request.getParameter("returnId");
	
	String url = myURL + "attachmentManager.jsp?elmtId=" + elmtId + "&elmtType=" + elmtType
	    + "&returnAction=" + returnAction + "&returnId=" + returnId;
%>
<html>
<head>
	<title></title><%

	out.println(gef.getLookStyleSheet());
	%>
	<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
</head>
<body>
<%
    out.println(window.printBefore());
     
    TabbedPane tabbedPane = gef.getTabbedPane();
    tabbedPane.addTab("Header", myURL + returnAction + "?" + returnId + "=" + elmtId, false);
    tabbedPane.addTab("Attachments", myURL + "attachmentManager.jsp?elmtId=" + elmtId
        + "&elmtType=" + elmtType + "&returnAction=" + returnAction + "&returnId=" + returnId, true);
    out.println(tabbedPane.print());

    out.println(frame.printBefore());

    out.flush();
    //Attachments links

    getServletConfig().getServletContext().getRequestDispatcher(
        "/attachment/jsp/editAttFiles.jsp?Id=" + elmtType +"_" + elmtId + "&SpaceId=" + spaceId
        + "&ComponentId=" + componentId + "&Context=Images"
        + "&Url=" + URLEncoder.encode(url.substring(m_context.length()))).include(request, response);

    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</body>
</html>
