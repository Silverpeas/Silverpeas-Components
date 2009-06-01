<%@ include file="imports.jsp" %>
<%!
	GraphicElementFactory gef;
	Window window;
	
	ConnecteurJDBCSessionController connecteurJDBC;
	ResourceLocator messages = null;

	String flag = "user";

%>
<%
	response.setHeader("Cache-Control","no-store"); //HTTP 1.1
	response.setHeader("Pragma","no-cache"); //HTTP 1.0
	response.setDateHeader ("Expires",-1); //prevents caching at the proxy server

	gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
  	
  	flag = request.getParameter("flag");
  	if (flag == null)
  		flag = "user";
  	

	connecteurJDBC = (ConnecteurJDBCSessionController) request.getAttribute("connecteurJDBC");

	String returnType	= (String) request.getParameter("ReturnType");
	String buttonAction = "javascript:window.close();";
	if (returnType != null && returnType.equals("SameWindow"))
	{
		buttonAction = "javascript:history.back();";
	}
	
	if (connecteurJDBC == null) {
		// No connecteurJDBC session controller in the request -> security exception
		String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
		getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
		return;
	}

  	//objet window
  	window = gef.getWindow();
%>
<%
String graphicPath                            = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
String m_context                              = graphicPath;
%>
<HTML>
<Head>
  <TITLE><%=connecteurJDBC.getString("windowTitleParametrageConnection")%> </TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
</head>
<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">

<%
	Frame frame = gef.getFrame();
	out.println(window.printBefore());
	out.println(frame.printBefore());
%>
<center>
<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
	<tr> 
		<td nowrap>
			<table border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor" width="100%"><!--tabl1-->
				<tr align=center> 
					<td  class="intfdcolor4" valign="baseline" align=center>
					<span class="textePetitBold"><%=connecteurJDBC.getString("erreurParam")%></span>
					</td>
				</tr>
			</table>
		</td>
	</tr>
</table>

<br>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton(connecteurJDBC.getString("boutonFermer"), buttonAction, false));
    out.println(buttonPane.print());
%>
</center>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</Body>
</HTML>
