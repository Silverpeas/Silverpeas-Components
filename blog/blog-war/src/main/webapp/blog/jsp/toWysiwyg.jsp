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
<%@ include file="check.jsp" %>

<%
PublicationDetail 	pub			= (PublicationDetail) request.getAttribute("CurrentPublicationDetail");

String 				pubId 		= pub.getPK().getId();
String 				pubName 	= pub.getName();

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript">

function goToWysiwyg() {
    document.toWysiwyg.submit();
}

function closeWindows() {
    if (window.publicationWindow != null)
        window.publicationWindow.close();
}
</script>
</head>
<body id="blog" onunload="closeWindows()">
<div id="<%=instanceId %>">
<form name="toWysiwyg" action="../../wysiwyg/jsp/htmlEditor.jsp" method="post">
	<input type="hidden" name="SpaceId" value="<%=spaceId%>"/>
    <input type="hidden" name="SpaceName" value="<%=spaceLabel%>"/>
    <input type="hidden" name="ComponentId" value="<%=instanceId%>"/>
    <input type="hidden" name="ComponentName" value="<%=Encode.javaStringToHtmlString(componentLabel)%>"/>
    <input type="hidden" name="ObjectId" value="<%=pubId%>"/>
    <input type="hidden" name="Language" value="<%=resource.getLanguage()%>"/>
    <input type="hidden" name="ReturnUrl" value="<%=m_context+URLManager.getURL("blog", "useless", instanceId)%>FromWysiwyg?PostId=<%=pubId%>"/>
    <input type="hidden" name="UserId" value="<%=userId%>"/>
    <input type="hidden" name="IndexIt" value="false"/>
</form>

<script>goToWysiwyg()</script>
</div>
</body>
</html>