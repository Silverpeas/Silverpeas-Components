<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>


<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="java.util.*"%>
<%@ page import="javax.ejb.*,java.sql.SQLException,javax.naming.*,javax.rmi.PortableRemoteObject"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@ page import="com.stratelia.webactiv.beans.admin.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.*"%>
<%@ page import="com.stratelia.silverpeas.silvertrace.SilverTrace"%>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<%
	GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

	ResourcesWrapper resource = (ResourcesWrapper)request.getAttribute("resources");

	Window 		window 		= gef.getWindow();
	BrowseBar 	browseBar 	= window.getBrowseBar();
	Frame 		frame 		= gef.getFrame();
	Board		board		= gef.getBoard();

	String[] browseContext = (String[]) request.getAttribute("browseContext");
	String spaceLabel 		= browseContext[0];
	String componentLabel 	= browseContext[1];
%>
