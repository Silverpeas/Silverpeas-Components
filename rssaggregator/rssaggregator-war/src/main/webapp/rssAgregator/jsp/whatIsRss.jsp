<%--

    Copyright (C) 2000 - 2011 Silverpeas

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

<%@ include file="check.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<c:set var="content" value="${requestScope['Content']}" />
<c:set var="role" value="${requestScope['Role']}"/>

<%
	String 	role = (String) request.getAttribute("Role");
%>

<html>
<head>
<view:looknfeel />
<script type="text/javascript" src="<%=context%>/util/javaScript/animation.js"></script>
<script type="text/javascript">

var addChannelWindow = window;

function addChannel() {
    windowName = "addChannelWindow";
	larg = "600";
	haut = "350";
    windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
    if (!addChannelWindow.closed && addChannelWindow.name== "addChannelWindow") {
        addChannelWindow.close();
    }
    addChannelWindow = SP_openWindow("ToCreateChannel", windowName, larg, haut, windowParams);
}

</script>
</head>
<body>
<view:browseBar></view:browseBar>

<c:if test="${fn:contains(role, 'admin')}">
<fmt:message var="addChannelLabel" key="rss.addChannel" />
<fmt:message var="addChannelIcon" key="rss.addChannel" bundle="${icons}"/>
<view:operationPane>
  <view:operation altText="${addChannelLabel}" icon="${addChannelIcon}" action="javascript:onClick=addChannel()"></view:operation>
</view:operationPane>
</c:if>

<view:window>
  <view:frame>
  <c:out escapeXml="false" value="${content}"  />
  </view:frame>
</view:window>
<form name="refresh" action="Main" method="post"></form>
</body>
</html>