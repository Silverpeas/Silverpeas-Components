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
<%@ include file="check.jsp" %>

<%
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
<BODY id="blog" onUnload="closeWindows()">
<div id="<%=instanceId %>">
<form name="toWysiwyg" Action="../../wysiwyg/jsp/htmlEditor.jsp" method="Post">
	<input type="hidden" name="SpaceId" value="<%=spaceId%>">
    <input type="hidden" name="SpaceName" value="<%=spaceLabel%>">
    <input type="hidden" name="ComponentId" value="<%=instanceId%>">
    <input type="hidden" name="ComponentName" value="<%=Encode.javaStringToHtmlString(componentLabel)%>">
    <input type="hidden" name="ObjectId" value="<%=instanceId%>">
    <input type="hidden" name="Language" value="<%=resource.getLanguage()%>">
    <input type="hidden" name="ReturnUrl" value="<%=m_context+URLManager.getURL("blog", "useless", instanceId)%>Main">
    <input type="hidden" name="UserId" value="<%=userId%>">
    <input type="hidden" name="IndexIt" value="false" >
</form>

<script>goToWysiwyg()</script>
</div>
</BODY>
</HTML>