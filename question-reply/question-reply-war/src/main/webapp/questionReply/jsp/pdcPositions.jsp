<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ include file="checkQuestionReply.jsp" %>
<%@ include file="tabManager.jsp.inc" %>
<%

Question question		= (Question) request.getAttribute("question");
String title			= Encode.javaStringToHtmlString(question.getTitle());
String questionId		= question.getPK().getId();
String url				= (String) request.getAttribute("ReturnURL");
String silverContentId	= (String) request.getAttribute("SilverContentId");
String profil 			= (String) request.getAttribute("Flag");
String		userId		= (String) request.getAttribute("UserId");

%>
<HTML>
<HEAD>
<TITLE></TITLE>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
</HEAD>
<BODY>
<%
	browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel, routerUrl+"MainPDC");

	browseBar.setPath("<a href="+routerUrl+"Main></a>" + title);

	operationPane.addOperation(m_context+"/pdcPeas/jsp/icons/pdcPeas_position_to_add.gif", resource.getString("GML.PDCNewPosition"), "javascript:openSPWindow('"+m_context+"/RpdcClassify/jsp/NewPosition','newposition')");
	operationPane.addOperation(m_context+"/pdcPeas/jsp/icons/pdcPeas_position_to_del.gif", resource.getString("GML.PDCDeletePosition"), "javascript:getSelectedItems()");

	out.println(window.printBefore());

	boolean updateQ = true;
	if (profil.equals("publisher") && !question.getCreatorId().equals(userId))
		updateQ = false;
	if (!profil.equals("user"))
		displayTabs(updateQ, true, questionId, resource, gef, "ViewPdcPositions", routerUrl, out);

	out.println(frame.printBefore());

	out.flush();

	getServletConfig().getServletContext().getRequestDispatcher("/pdcPeas/jsp/positionsInComponent.jsp?SilverObjectId="+silverContentId+"&ComponentId="+componentId+"&ReturnURL="+URLEncoder.encode(url)).include(request, response);

	out.println(frame.printAfter());
	out.println(window.printAfter());
%>

<FORM NAME="toComponent" ACTION="ViewPdcPositions" METHOD=POST>
</FORM>
</BODY>
</HTML>