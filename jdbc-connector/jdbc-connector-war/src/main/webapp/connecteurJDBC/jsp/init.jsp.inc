<%!
	GraphicElementFactory gef;
	Window window;
	TabbedPane tabbedPane;
	
	ConnecteurJDBCSessionController connecteurJDBC;
	ResourceLocator messages = null;

%>
<%
	response.setHeader("Cache-Control","no-store"); //HTTP 1.1
	response.setHeader("Pragma","no-cache"); //HTTP 1.0
	response.setDateHeader ("Expires",-1); //prevents caching at the proxy server

	gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
  	
	String flag = (String) request.getAttribute("flag");
	if (flag == null)
		flag = "user";
  	
	connecteurJDBC = (ConnecteurJDBCSessionController) request.getAttribute("connecteurJDBC");
	
	if (connecteurJDBC == null) {
		// No connecteurJDBC session controller in the request -> security exception
		String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
		getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
		return;
	}

	window = gef.getWindow();
	String space = connecteurJDBC.getSpaceLabel();
	String component = connecteurJDBC.getComponentLabel();
	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setDomainName(space);
	browseBar.setComponentName(component, "Main");
%>