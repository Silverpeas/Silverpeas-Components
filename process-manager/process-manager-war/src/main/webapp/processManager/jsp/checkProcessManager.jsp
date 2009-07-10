<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache");        //HTTP 1.0
response.setDateHeader ("Expires",-1);          //prevents caching
%>

<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="java.util.Date"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.util.Hashtable"%>
<%@ page import="java.util.StringTokenizer"%>

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
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>

<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.silverpeas.processManager.*"%>
<%@ page import="com.silverpeas.processManager.record.*"%>
<%@ page import="com.silverpeas.workflow.engine.dataRecord.ProcessInstanceRowRecord"%>
<%@ page import="com.silverpeas.workflow.api.instance.*"%>
<%@ page import="com.silverpeas.workflow.api.model.*"%>
<%@ page import="com.silverpeas.workflow.api.task.*"%>
<%@ page import="com.silverpeas.workflow.api.event.*"%>
<%@ page import="com.silverpeas.workflow.api.user.*"%>
<%@ page import="com.silverpeas.workflow.api.error.*"%>
<%@ page import="com.silverpeas.form.*"%>
<%@ page import="com.silverpeas.form.fieldType.DateField"%>
<%@ page errorPage="../../admin/jsp/errorpageMain.jsp"%>

<%
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
String language = (String) request.getAttribute("language");
ResourcesWrapper resource = (ResourcesWrapper)request.getAttribute("resources");

String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
String[] browseContext = (String[]) request.getAttribute("browseContext");
String spaceLabel = browseContext[0];
String componentLabel = browseContext[1];
String spaceId = browseContext[2];
String componentId = browseContext[3];
String processManagerUrl = browseContext[4];

ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang(language);

Window window = gef.getWindow();
BrowseBar browseBar = window.getBrowseBar();
OperationPane operationPane = window.getOperationPane();
Frame frame = gef.getFrame();
TabbedPane tabbedPane = gef.getTabbedPane();
Board board = gef.getBoard();

// Shared attributes
NamedValue[] roles = (NamedValue[]) request.getAttribute("roles");
if (roles == null) roles = new NamedValue[0];

String currentRole  = (String) request.getAttribute("currentRole");
if (currentRole == null) currentRole = "";
%>
