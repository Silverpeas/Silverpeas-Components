<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
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
<%
    response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
    response.setHeader("Pragma", "no-cache"); //HTTP 1.0
    response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@ include file="checkForums.jsp" %>
<%@ include file="tabManager.jsp" %>
<%@page import="java.net.URLEncoder"%>
<%
    int forumId = getIntParameter(request, "forumId");
    int params = getIntParameter(request, "params");
    String url = fsc.getComponentUrl() + ActionUrl.getUrl("pdcPositions", -1, params, forumId);
%>
<html>
<head>
    <title></title>
    <%
    	out.println(graphicFactory.getLookStyleSheet());
	%>
    <script type="text/javascript" src="<%=context%>/forums/jsp/javaScript/forums.js"></script>
    <script type="text/javascript" src="<%=context%>/util/javaScript/animation.js"></script>
</head>

<body>
<%
    Window window = graphicFactory.getWindow();
    Frame frame = graphicFactory.getFrame();

    if (!isReader)
    {
    	OperationPane operationPane = window.getOperationPane();
        operationPane.addOperation(context + "/pdcPeas/jsp/icons/pdcPeas_position_to_add.gif",
            resources.getString("GML.PDCNewPosition"), "javascript:openSPWindow('" + context
                + "/RpdcClassify/jsp/NewPosition','newposition')");
        operationPane.addOperation(context + "/pdcPeas/jsp/icons/pdcPeas_position_to_del.gif",
        	resources.getString("GML.PDCDeletePosition"), "javascript:getSelectedItems()");
    }
    
    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(fsc.getSpaceLabel());
    browseBar.setComponentName(fsc.getComponentLabel(), ActionUrl.getUrl("main"));

    out.println(window.printBefore());

    displayTabs(params, forumId, fsc, graphicFactory, "ViewPdcPositions", out);
    
    out.println(frame.printBefore());

    out.flush();

    getServletConfig().getServletContext().getRequestDispatcher(
            "/pdcPeas/jsp/positionsInComponent.jsp"
            + "?SilverObjectId=" + fsc.getSilverObjectId(params)
            + "&ComponentId=" + fsc.getComponentId()
            + "&ReturnURL=" + URLEncoder.encode(url, "ISO-8859-1"))
        .include(request, response);

    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
    <form name="toComponent" action="pdcPositions.jsp" method="post">
        <input type="hidden" name="params" value="<%=params%>">
        <input type="hidden" name="forumId" value="<%=forumId%>">
    </form>
</body>
</html>