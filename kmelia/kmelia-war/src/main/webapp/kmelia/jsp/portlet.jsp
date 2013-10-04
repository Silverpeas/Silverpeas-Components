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
<%@ include file="topicReport.jsp.inc" %>

<% 
String id = request.getParameter("Id");
String translation = request.getParameter("Translation");
if (translation == null) {
	translation = kmeliaScc.getLanguage();
}

String rootId = "0";
if (id == null) {
  id = rootId;
}

TopicDetail currentTopic = kmeliaScc.getTopic(id);
kmeliaScc.setSessionTopic(currentTopic);
Collection path = currentTopic.getPath();
String linkedPathString = displayPath(path, true, 3, translation);
kmeliaScc.setSessionPath(linkedPathString);

%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title></title>
<% out.println(gef.getLookStyleSheet()); %>
<Script language="JavaScript1.2">
function topicGoTo(id) {
    document.topicDetailForm.Action.value = "Search";
    document.topicDetailForm.Translation.value = "<%=translation%>";
    document.topicDetailForm.Id.value = id;
    document.topicDetailForm.submit();
}

function publicationGoTo(id){
    document.pubForm.Id.value = id;
    document.pubForm.submit();
}
function publicationGoToFromMain(id){
    document.pubForm.CheckPath.value = "1";
    publicationGoTo(id);
}

function sortGoTo(selectedIndex) {
	if (selectedIndex !== 0 && selectedIndex !== 1) {
		var sort = document.publicationsForm.sortBy[selectedIndex].value;
		var ieFix = new Date().getTime();
		$.get('<%=m_context%>/RAjaxPublicationsListServlet', {Index:0,Sort:sort,ComponentId:'<%=componentId%>',IEFix:ieFix},
							function(data){
								$('#pubList').html(data);
							},"html");
		return;
	}
}

function doPagination(index)
{
	var ieFix = new Date().getTime();
	$.get('<%=m_context%>/RAjaxPublicationsListServlet', {Index:index,ComponentId:'<%=componentId%>',IEFix:ieFix},
							function(data){
								$('#pubList').html(data);
							},"html");
}

function displayPublications(id)
{
	//display publications of topic
	var ieFix = new Date().getTime();
	$.get('<%=m_context%>/RAjaxPublicationsListServlet', {Id:id,ComponentId:'<%=componentId%>',IEFix:ieFix},
			function(data){
				$('#pubList').html(data);
			},"html");
}

function init()
{
	displayPublications('<%=id%>');
}
</script>
</head>

<body id="kmelia" onload="init()">
<div id="<%=componentId %>">
<span class="portlet">
<%
		Window window = gef.getWindow();

		BrowseBar browseBar = window.getBrowseBar();
        browseBar.setComponentName(kmeliaScc.getComponentLabel(), "portlet.jsp");
		browseBar.setPath(linkedPathString);
		browseBar.setIgnoreComponentLink(false);
		browseBar.setComponentId(componentId);

		Frame frame = gef.getFrame();

		out.println(window.printBefore());

		if ((!id.equals("1")) && (!id.equals("2"))) {
			displaySessionTopicsToUsers(kmeliaScc, currentTopic, gef, request, session, resources, out);
		}

		out.println("<div id=\"pubList\"/>");
		
		out.println(window.printAfter());
%>
</span>
</div>
<form name="topicDetailForm" action="portlet.jsp" method="post">
  <input type="hidden" name="Action"/>
  <input type="hidden" name="Id" value="<%=id%>"/>
  <input type="hidden" name="Translation"/>
</form>

<form name="pubForm" action="ViewPublication" method="post" target="MyMain">
<input type="hidden" name="Id"/>
<input type="hidden" name="CheckPath"/>
</form>
</body>
</html>