<%--

    Copyright (C) 2000 - 2013 Silverpeas

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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
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
<%@ page import="java.lang.*"%>
<%@ page import="javax.ejb.*,java.sql.SQLException,javax.naming.*,javax.rmi.PortableRemoteObject"%>

<%@ page import="org.silverpeas.util.*"%>

<%@ page import="org.silverpeas.util.viewGenerator.html.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.Encode"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.board.Board"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.window.Window"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.buttons.Button"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.buttonPanes.ButtonPane"%>

<%@ include file="checkScc.jsp" %>
<%@ include file="util.jsp" %>

<%

String nomSite = (String) request.getParameter("nomSite");
String nomPage = (String) request.getParameter("nomPage");

%>

<HTML>
<HEAD>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<view:looknfeel/>
</HEAD>
<BODY>
<%     
	Window window = gef.getWindow();

	BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel);
	browseBar.setExtraInformation(resources.getString("SuggestionLink"));

	//Le cadre
	Frame frame = gef.getFrame();

	//Le board
	Board board = gef.getBoard();

	//D�but code
    out.println(window.printBefore());
    out.println(frame.printBefore());
	out.print(board.printBefore());
%>
	 <TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=intfdcolor4>
		<tr>            
            <td class="intfdcolor4"><%=resources.getString("ConfirmationSuggestionMessage1") + "'<B>" + nomSite + "</B>' (" + nomPage + ") " + resources.getString("ConfirmationSuggestionMessage2")+"<BR><BR>"%> 
            </td>
        </tr>
	</TABLE>
<%
	//fin du code
	out.print(board.printAfter());
    out.println(frame.printMiddle());

	ButtonPane buttonPane = gef.getButtonPane();
	Button closeButton = (Button) gef.getFormButton(resources.getString("GML.close"), "javascript:onClick=window.close();", false);
	buttonPane.addButton(closeButton);
	buttonPane.setHorizontalPosition(); 

	out.println("<br><center>"+buttonPane.print()+"</center><br>");
	
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>

</BODY>
</HTML>