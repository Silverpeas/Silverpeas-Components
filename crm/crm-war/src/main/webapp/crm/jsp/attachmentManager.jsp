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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>

<view:setBundle basename="com.silverpeas.crm.multilang.crmBundle"/>

<html>
<head>
	<title></title>
	<view:looknfeel/>
</head>
<body>
	<view:window>
		<view:tabs>
			<fmt:message key="crm.header" var="headerLabel"/>
			<view:tab label="${headerLabel}" selected="" action="${myComponentURL}${param.returnAction}?${param.returnId}=${param.elmtId}"/>
			<fmt:message key="crm.attachment" var="attachmentLabel"/>
			<view:tab label="${attachmentLabel}" selected="true" action="attachmentManager.jsp?elmtId=${param.elmtId}&elmtType=${param.elmtType}&returnAction=${param.returnAction}&returnId=${param.returnId}"/>
		</view:tabs>
		<view:frame><%

    out.flush();
		
	String[] browseContext = (String[]) request.getAttribute("browseContext");
	String spaceId = browseContext[2];
	String componentId = browseContext[3];

	String elmtId = request.getParameter("elmtId");
	String elmtType = request.getParameter("elmtType");
	String returnAction = request.getParameter("returnAction");
	String returnId = request.getParameter("returnId");
	String myURL = (String) request.getAttribute("myComponentURL");
	String url = myURL + "attachmentManager.jsp?elmtId=" + elmtId + "&elmtType=" + elmtType
	    + "&returnAction=" + returnAction + "&returnId=" + returnId;
	String context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
    getServletConfig().getServletContext().getRequestDispatcher(
        "/attachment/jsp/editAttFiles.jsp?Id=" + elmtType +"_" + elmtId + "&SpaceId=" + spaceId
        + "&ComponentId=" + componentId + "&Context=Images"
        + "&Url=" + java.net.URLEncoder.encode(url.substring(context.length()), "UTF-8")).include(request, response);
%>
		</view:frame>
	</view:window>
</body>
</html>
