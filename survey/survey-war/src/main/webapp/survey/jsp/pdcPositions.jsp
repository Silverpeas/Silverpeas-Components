<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.File"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ include file="checkSurvey.jsp" %>
<%@ include file="surveyUtils.jsp.inc" %>

<% 
String surveyName		= surveyScc.getSessionSurvey().getHeader().getTitle();
String surveyId			= surveyScc.getSessionSurvey().getHeader().getPK().getId();
String m_context		= GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
String url				= surveyScc.getComponentUrl()+"pdcPositions.jsp";

String profile			= (String) request.getAttribute("Profile");

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
	Window			window			= gef.getWindow();
	Frame			frame			= gef.getFrame();
	OperationPane	operationPane	= window.getOperationPane();
	BrowseBar		browseBar		= window.getBrowseBar();
	
	browseBar.setDomainName(surveyScc.getSpaceLabel());
	browseBar.setComponentName(surveyScc.getComponentLabel(), "surveyList.jsp?Action=View");
	browseBar.setExtraInformation(Encode.javaStringToHtmlString(surveyName));

	operationPane.addOperation(m_context+"/pdcPeas/jsp/icons/pdcPeas_position_to_add.gif", resources.getString("GML.PDCNewPosition"), "javascript:openSPWindow('"+m_context+"/RpdcClassify/jsp/NewPosition','newposition')");
	operationPane.addOperation(m_context+"/pdcPeas/jsp/icons/pdcPeas_position_to_del.gif", resources.getString("GML.PDCDeletePosition"), "javascript:getSelectedItems()");

	out.println(window.printBefore());

	out.println(displayTabs(surveyScc, surveyId, gef, "ViewPdcPositions", profile, resources, pollingStationMode).print());

	out.println(frame.printBefore());

	out.flush();

	getServletConfig().getServletContext().getRequestDispatcher("/pdcPeas/jsp/positionsInComponent.jsp?SilverObjectId="+surveyScc.getSilverObjectId(surveyId)+"&ComponentId="+surveyScc.getComponentId()+"&ReturnURL="+URLEncoder.encode(url)).include(request, response);

	out.println(frame.printAfter());
	out.println(window.printAfter());
%>

<FORM NAME="toComponent" ACTION="pdcPositions.jsp" METHOD=POST >
	<input type="hidden" name="Action" value="ViewPdcPositions">
</FORM>
</BODY>
</HTML>