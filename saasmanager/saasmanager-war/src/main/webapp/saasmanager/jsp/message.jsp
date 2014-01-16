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

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<fmt:setLocale value="${language}"/>
<view:setBundle bundle="${resources}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
	<view:looknfeel/>
</head>

<body>
	<view:window>
		<view:frame>
			<view:board>
				<span class="txtlibform"><fmt:message key="message.${message}"/></span>
				<fmt:message key="back" var="back"/>
				<center style="padding: 10px;">
					<view:buttonPane>
						<view:button label="${back}" action="javascript:document.forms['messageForm'].submit();"/>
					</view:buttonPane>
				</center>
				<form name="messageForm" action="${pageContext.request.contextPath}/Rsaasmanager" method="post">
					<input type="hidden" name="action" value="management"/>
					<input type="hidden" name="function" value=""/>
					<input type="hidden" name="uid" value="${access.uid}"/>
					<input type="hidden" name="userId" value="${userId}"/>
				</form>
			</view:board>
		</view:frame>
	</view:window>
</body>
</html>