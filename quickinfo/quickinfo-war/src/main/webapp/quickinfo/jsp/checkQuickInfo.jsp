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
<%@ page import="com.stratelia.webactiv.beans.admin.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.Icon"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.quickinfo.control.*"%>
<%@ page import="com.stratelia.webactiv.util.publication.model.*"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.silverpeas.silvertrace.SilverTrace"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<%
        GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

        QuickInfoSessionController quickinfo = (QuickInfoSessionController) request.getAttribute("quickinfo");
        
        ResourcesWrapper resources = (ResourcesWrapper)request.getAttribute("resources");

        String language = resources.getLanguage();
        ResourceLocator settings = (ResourceLocator) request.getAttribute("settings");
        String[] browseContext = (String[]) request.getAttribute("browseContext");
        String spaceLabel = browseContext[0];
        String componentLabel = browseContext[1];
        String spaceId = browseContext[2];
        String componentId = browseContext[3];
        String quickinfoUrl = browseContext[4];

        if (quickinfo == null)
        {
            // No quickinfo session controller in the request -> security exception
            String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
            getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
            return;
        }        

        String iconsPath = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
	    ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang(quickinfo.getLanguage());
        String m_context = iconsPath;
        
%>