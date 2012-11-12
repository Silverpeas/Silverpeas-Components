<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

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
