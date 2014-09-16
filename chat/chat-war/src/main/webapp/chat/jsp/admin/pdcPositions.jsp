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
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="java.io.File"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>

<%@ include file="../checkChat.jsp" %>

<%
	String url = chatUrl+"pdcPositions.jsp?PubId="+request.getParameter("PubId");
%>
<HTML>
<HEAD>
<TITLE></TITLE>
<view:looknfeel/>
</HEAD>
<BODY>
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	browseBar.setPath(resource.getString("chat.openChatroom"));

	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resource.getString("chat.chatRoomList"),"Main",false);
	tabbedPane.addTab(resource.getString("chat.administration"),"chatroom.jsp?todo=manage&id="+request.getParameter("PubId"),false);
	tabbedPane.addTab(resource.getString("PdcClassification"),"#",true);

	Board board = gef.getBoard();

	operationPane.addOperation(m_context+"/pdcPeas/jsp/icons/pdcPeas_position_to_add.gif", resource.getString("NewPdcPosition"), "javascript:openSPWindow('"+m_context+"/RpdcClassify/jsp/NewPosition','newposition')");
	operationPane.addOperation(m_context+"/pdcPeas/jsp/icons/pdcPeas_position_to_del.gif", resource.getString("DeletePdcPosition"), "javascript:getSelectedItems()");

	out.println(window.printBefore());
	out.println(tabbedPane.print());
	out.println(frame.printBefore());

	out.flush();

	getServletConfig().getServletContext().getRequestDispatcher("/pdcPeas/jsp/positionsInComponent.jsp?SilverObjectId=" + ((String) request.getAttribute("silverObjectId"))+"&ComponentId="+componentId+"&ReturnURL="+URLEncoder.encode(url)).include(request, response);

	out.println(frame.printAfter());
	out.println(window.printAfter());
%>

<FORM NAME="toComponent" ACTION="pdcPositions.jsp" METHOD=POST >
	<input type="hidden" name="Action" value="ViewPdcPositions">
	<input type="hidden" name="PubId" value="<%=request.getParameter("PubId")%>">
</FORM>
</BODY>
</HTML>