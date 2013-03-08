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

<%@ include file="checkKmelia.jsp" %>
<%@ include file="tabManager.jsp.inc" %>

<%
PublicationDetail 	pubDetail 		= (PublicationDetail) request.getAttribute("CurrentPublicationDetail");
String contentLanguage = kmeliaScc.getContentLanguage();

String pubId 	= pubDetail.getPK().getId();
String pubName 	= pubDetail.getName(contentLanguage);
String returnURL = m_context + kmeliaScc.getComponentUrl() + "FromWysiwyg?PubId="+pubId;
	
%>
<HTML>
<HEAD>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">

function goToWysiwyg() {
    document.toWysiwyg.submit();
}

function closeWindows() {
    if (window.publicationWindow != null)
        window.publicationWindow.close();
}
</script>
</HEAD>
<BODY onUnload="closeWindows()">

<form name="toWysiwyg" Action="../../wysiwyg/jsp/htmlEditor.jsp" method="Post">
	<input type="hidden" name="SpaceId" value="<%=spaceId%>">
    <input type="hidden" name="SpaceName" value="<%=spaceLabel%>">
    <input type="hidden" name="ComponentId" value="<%=componentId%>">
    <input type="hidden" name="ComponentName" value="<%=Encode.javaStringToHtmlString(componentLabel)%>">
<% if (kmaxMode) { %>
	<input type="hidden" name="BrowseInfo" value="<%= Encode.javaStringToHtmlString(pubName)%>">
<% } else { %>
	<input type="hidden" name="BrowseInfo" value="<%=kmeliaScc.getSessionPathString()+" > " + Encode.javaStringToHtmlString(pubName)%>">
<% } %>
    <input type="hidden" name="ObjectId" value="<%=pubId%>">
    <input type="hidden" name="Language" value="<%=resources.getLanguage()%>">
    <input type="hidden" name="ContentLanguage" value="<%=contentLanguage%>">
    <input type="hidden" name="ReturnUrl" value="<%=returnURL%>">
    <input type="hidden" name="UserId" value="<%=kmeliaScc.getUserId()%>">
</form>

<script>goToWysiwyg()</script>
</BODY>
</HTML>