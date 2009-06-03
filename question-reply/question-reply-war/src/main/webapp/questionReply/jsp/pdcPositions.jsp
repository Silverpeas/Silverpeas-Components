<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ include file="checkQuestionReply.jsp" %>
<%@ include file="tabManager.jsp.inc" %>
<%

Question question		= (Question) request.getAttribute("question");
String title			= Encode.javaStringToHtmlString(question.getTitle());
String questionId		= question.getPK().getId();
String url				= (String) request.getAttribute("ReturnURL");
String silverContentId	= (String) request.getAttribute("SilverContentId");
String profil 			= (String) request.getAttribute("Flag");
String		userId		= (String) request.getAttribute("UserId");

%>
<HTML>
<HEAD>
<TITLE></TITLE>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
</HEAD>
<BODY>
<%
	browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel, routerUrl+"MainPDC");

	browseBar.setPath("<a href="+routerUrl+"Main></a>" + title);

	operationPane.addOperation(m_context+"/pdcPeas/jsp/icons/pdcPeas_position_to_add.gif", resource.getString("GML.PDCNewPosition"), "javascript:openSPWindow('"+m_context+"/RpdcClassify/jsp/NewPosition','newposition')");
	operationPane.addOperation(m_context+"/pdcPeas/jsp/icons/pdcPeas_position_to_del.gif", resource.getString("GML.PDCDeletePosition"), "javascript:getSelectedItems()");

	out.println(window.printBefore());

	boolean updateQ = true;
	if (profil.equals("publisher") && !question.getCreatorId().equals(userId))
		updateQ = false;
	if (!profil.equals("user"))
		displayTabs(updateQ, true, questionId, resource, gef, "ViewPdcPositions", routerUrl, out);

	out.println(frame.printBefore());

	out.flush();

	getServletConfig().getServletContext().getRequestDispatcher("/pdcPeas/jsp/positionsInComponent.jsp?SilverObjectId="+silverContentId+"&ComponentId="+componentId+"&ReturnURL="+URLEncoder.encode(url)).include(request, response);

	out.println(frame.printAfter());
	out.println(window.printAfter());
%>

<FORM NAME="toComponent" ACTION="ViewPdcPositions" METHOD=POST>
</FORM>
</BODY>
</HTML>