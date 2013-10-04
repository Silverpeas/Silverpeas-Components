<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
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
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>
<%@ include file="check.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><fmt:message key="GML.popupTitle" /></title>
<view:looknfeel />
</head>

<%
  String[] emailErrors = (String[])request.getAttribute("EmailErrors");
	String returnUrl = "Accueil";
  if (request.getAttribute("ReturnUrl") != null) {
	  returnUrl = (String) request.getAttribute("ReturnUrl");
  }
%>

<body>
    <view:window>
    <view:frame>
<div class="inlineMessage">
  <fmt:message var="messageSent" key="infoLetter.sended"/>
  <view:encodeJs string="${messageSent}"/>
</div>
<br clear="all"/>
<% if (emailErrors.length > 0) { %>
	<div class="inlineMessage-nok">
		<%=EncodeHelper.javaStringToHtmlString(resource.getString("infoLetter.emailErrors"))%> : <br/>
		<ul>
		<% for (int i = 0; i < emailErrors.length; i++) { %>
			<li><%=EncodeHelper.javaStringToHtmlString(emailErrors[i])%></li>
		<% } %>
		</ul>
	</div>
<% } %>
<br clear="all"/>
<%
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(gef.getFormButton(resource.getString("GML.ok"), returnUrl, false));
	out.println("<center>"+buttonPane.print()+"</center>");
%>      </view:frame>
    </view:window>
</body>
</html>