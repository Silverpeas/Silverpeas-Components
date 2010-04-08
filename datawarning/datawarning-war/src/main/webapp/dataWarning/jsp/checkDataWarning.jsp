<%
	response.setHeader("Cache-Control","no-store"); //HTTP 1.1
	response.setHeader("Pragma","no-cache"); //HTTP 1.0
	response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.silverpeas.dataWarning.control.DataWarningSessionController"%>
<%@ page import="com.silverpeas.dataWarning.model.*"%>
<%@ page import="com.silverpeas.dataWarning.*"%>
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@ page import="java.util.*"%>
<%@ page import="java.text.*"%>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<%
	GraphicElementFactory gef;
	Window window;
	TabbedPane tabbedPane;
	Frame frame;
	ButtonPane buttonPane;
	OperationPane operationPane;
	
	DataWarningSessionController dataWarningSC;
    ResourcesWrapper resource;

	gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
  	
  	String flag = (String)request.getAttribute("flag");
  	if (flag == null)
  		flag = "user";
  	

	dataWarningSC = (DataWarningSessionController) request.getAttribute("DataWarningSC");
	
	if (dataWarningSC == null) {
		// No DataWarning session controller in the request -> security exception
		String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
		getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
		return;
	}

  	//objet window
  	window = gef.getWindow();
    //browse bar
	String space = dataWarningSC.getSpaceLabel();
	String component = dataWarningSC.getComponentLabel();
	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setDomainName(space);
	browseBar.setComponentName(component, "Main.jsp");
	//frame
	frame = gef.getFrame();
	//buttons
	buttonPane = gef.getButtonPane();
	//operation pane
	operationPane = window.getOperationPane();

	String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
    resource = (ResourcesWrapper)request.getAttribute("resources");
%>
