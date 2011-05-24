<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache");        //HTTP 1.0
response.setDateHeader ("Expires",-1);          //prevents caching at the proxy server
%>

<%@ page import="java.util.*"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>

<%// En fonction de ce dont vous avez besoin %>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayLine"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayColumn"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayCellText"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayCellLink"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.Icon"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.navigationList.NavigationList"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>

<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.silverpeas.containerManager.*"%>
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>

<%@ page import="com.stratelia.webactiv.util.attachment.model.*"%>
<%@ page import="com.silverpeas.crm.model.*"%>

<%@ page errorPage="../../admin/jsp/errorpageMain.jsp"%>

<%

GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

ResourcesWrapper resources = (ResourcesWrapper)request.getAttribute("resources");
Window window = gef.getWindow();
BrowseBar browseBar = window.getBrowseBar();
OperationPane operationPane = window.getOperationPane();
Frame frame = gef.getFrame();

String[] browseContext = (String[]) request.getAttribute("browseContext");
String spaceLabel = browseContext[0];
String componentLabel = browseContext[1];
String spaceId = browseContext[2];
String componentId = browseContext[3];

    // La barre de naviagtion
    
    browseBar.setDomainName(spaceLabel);
    //browseBar.setComponentName(componentLabel, "client.jsp");


ContainerContext containerContext = (ContainerContext) request.getAttribute("ContainerContext");
String returnURL = (String) request.getAttribute("ReturnURL");
String myURL = (String) request.getAttribute("myComponentURL");

if (containerContext == null) 
{
	if (returnURL != null && returnURL.length() > 0)
		browseBar.setComponentName(componentLabel, m_context+returnURL);
	else
		browseBar.setComponentName(componentLabel, "Main");
} 
else 
{
	browseBar.setComponentName(componentLabel, m_context+containerContext.getReturnURL()); 
}

SimpleDateFormat formatter = new SimpleDateFormat(resources.getString("GML.dateFormat"));  //format Client
SimpleDateFormat formatterBD = new SimpleDateFormat("yyyy/MM/dd");


String boardStart = "<table cellpadding=5 cellspacing=2 border=0 width=\"98%\" class=intfdcolor><tr><td bgcolor=FFFFFF align=center>";
String boardEnd   = "</td></tr></table>";

String cellCenterStart  = "<table cellpadding=0 cellspacing=0 border=0 width=\"100%\"><tr><td align=center>";
String cellCenterEnd  = "</td></tr></table>";
%>
