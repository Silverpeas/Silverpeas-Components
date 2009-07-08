<%@ page import="java.util.*"%>
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.beans.*"%>

<%@ page import="javax.naming.Context,javax.naming.InitialContext,javax.rmi.PortableRemoteObject"%>
<%@ page import="javax.ejb.RemoveException, javax.ejb.CreateException, java.sql.SQLException, javax.naming.NamingException, java.rmi.RemoteException, javax.ejb.FinderException"%>

<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.util.contact.model.*"%>
<%@ page import="com.stratelia.webactiv.util.contact.info.model.*"%>
<%@ page import="com.stratelia.webactiv.util.exception.*"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodePK"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodeDetail"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.Icon"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.navigationList.NavigationList"%>
<%@ page import="com.stratelia.webactiv.beans.admin.*"%>
<%@ page import="com.stratelia.webactiv.searchEngine.control.ejb.* "%>
<%@ page import="com.stratelia.webactiv.searchEngine.model.* "%>
<%@ page import="com.stratelia.webactiv.yellowpages.control.YellowpagesSessionController"%>
<%@ page import="com.stratelia.webactiv.yellowpages.model.*"%>
<%@page import="com.silverpeas.util.StringUtil"%>

<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%
ResourcesWrapper 				resources 		= (ResourcesWrapper)request.getAttribute("resources");
YellowpagesSessionController 	yellowpagesScc 	= (YellowpagesSessionController) request.getAttribute("yellowpagesScc");
GraphicElementFactory 			gef 			= (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
String[] 						browseContext 	= (String[]) request.getAttribute("browseContext");

if (yellowpagesScc == null) {
    // No session controller in the request -> security exception
    String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
    getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
    return;
}

String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
final int ROOT_TOPIC = 0;
final String TRASHCAN_ID = "1";

String spaceLabel = browseContext[0];
String componentLabel = browseContext[1];
String spaceId = browseContext[2];
String componentId = browseContext[3];
%>