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

<%@ include file="checkKmelia.jsp" %>

<%
String translation = (String) request.getAttribute("Language");
%>

<HTML>
<HEAD>
<meta http-equiv="content-type" content="text/html; charset=utf-8">
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="javaScript/navigation.js"></script>
<script type="text/javascript">

function getCurrentUserId() {
  return "<%=gef.getMainSessionController().getUserId()%>";
}

function getWebContext() {
	return "<%=m_context%>";
}

function getComponentId() {
	return "<%=componentId%>";
}

function getToValidateFolderId() {
	return "<%=KmeliaHelper.SPECIALFOLDER_TOVALIDATE%>";
}

$(document).ready(function() {
	setCurrentNodeId(getToValidateFolderId());
	displayPublications(getToValidateFolderId());
});
</script>
</HEAD>
<BODY id="kmelia" class="yui-skin-sam">
<div id="<%=componentId %>">
<%
	Window window = gef.getWindow();
    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setI18N("GoToCurrentTopic", translation);
    browseBar.setExtraInformation(resources.getString("ToValidate"));

	Frame frame = gef.getFrame();

    out.println(window.printBefore());
    out.println(frame.printBefore());
%>

	<div id="pubList">
	<%
		 Board board = gef.getBoard();
		 out.println("<br/>");
		 out.println(board.printBefore());
		 out.println("<br/><center>"+resources.getString("kmelia.inProgressPublications")+"<br/><br/><img src=\""+resources.getIcon("kmelia.progress")+"\"/></center><br/>");
		 out.println(board.printAfter());
	 %>
	</div>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>

<form name="pubForm" action="ViewPublication" method="post">
	<input type="hidden" name="PubId"/>
	<input type="hidden" name="CheckPath" value="1"/>
</form>

</div>
</BODY>
</HTML>