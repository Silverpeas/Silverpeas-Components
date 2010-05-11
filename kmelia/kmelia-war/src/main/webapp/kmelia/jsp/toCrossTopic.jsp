<%--

    Copyright (C) 2000 - 2009 Silverpeas

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

<%@ include file="checkKmelia.jsp" %>
<%@ include file="topicReport.jsp.inc" %>

<%!
  //Icons
  String folderSrc;
  String topicSrc;
%>

<%
//R�cup�ration des param�tres
List 	treeview 		= (List) request.getAttribute("Treeview");
String  translation 	= (String) request.getAttribute("Language");

TopicDetail currentTopic 		= (TopicDetail) request.getAttribute("CurrentTopic");
String 		pathString 			= (String) request.getAttribute("PathString");
String 		linkedPathString 	= (String) request.getAttribute("LinkedPathString");

Boolean displayNbPublis = new Boolean(false);
String id = currentTopic.getNodeDetail().getNodePK().getId();
String language = kmeliaScc.getLanguage();

boolean useTreeview = (treeview != null);

//Icons
folderSrc			= m_context + "/util/icons/component/kmeliaSmall.gif";
topicSrc			= m_context + "/util/icons/component/kmeliaSmall.gif";
%>

<HTML>
<HEAD>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/i18n.js"></script>

<% if (useTreeview) { %>
	<script type="text/javascript" src="<%=m_context%>/util/javaScript/treeview/TreeView.js"></script>
	<script type="text/javascript" src="<%=m_context%>/util/javaScript/treeview/TreeViewElements.js"></script>
	<link type="text/css" rel="stylesheet" href="<%=m_context%>/util/styleSheets/treeview.css">
<% } %>

<script language="JavaScript1.2">
function topicGoTo(id) {
    document.topicDetailForm.Id.value = id;
    document.topicDetailForm.submit();
}
</script>
</HEAD>

<BODY>
<%	           
        Window window = gef.getWindow();
        BrowseBar browseBar = window.getBrowseBar();
        browseBar.setDomainName(kmeliaScc.getSpaceLabel());
        browseBar.setComponentName(kmeliaScc.getComponentLabel(), "Main");
        browseBar.setPath(linkedPathString);
        browseBar.setI18N("GoToTopic?Id="+id, translation);
        																			
        Frame frame = gef.getFrame();

        out.println(window.printBefore());
        out.println(frame.printBefore());
        
        if (useTreeview) { %>
			<table width="98%" border="0"><tr><td valign="top"><%@ include file="treeview.jsp.inc" %></td><td valign="top" width="100%">
        <%}

        displaySessionTopicsToUsers(kmeliaScc, currentTopic, gef, request, session, resources, out);
					
        if (useTreeview) { %>
			</td></tr></table>
		<% } 

		out.println(frame.printAfter());
		out.println(window.printAfter());
	%>

<FORM name="topicDetailForm" action="GoToTopic" method="POST">
	<input type="hidden" name="Id" value="<%=id%>">
</FORM>

</BODY>
</HTML>