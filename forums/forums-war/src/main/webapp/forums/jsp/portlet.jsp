<%--

    Copyright (C) 2000 - 2012 Silverpeas

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
<%@page import="com.stratelia.webactiv.forums.control.helpers.ForumListHelper"%>
<%@page import="com.stratelia.webactiv.forums.control.helpers.ForumActionHelper"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
    response.setHeader("Pragma", "no-cache"); //HTTP 1.0
    response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@ include file="checkForums.jsp" %>
<%@ include file="forumsListManagerPortlet.jsp" %>
<%
    String mailtoAdmin = context + "/util/icons/forums_mailtoAdmin.gif";

    Collection categories = fsc.getAllCategories();
    boolean isModerator = false;
    ForumActionHelper.actionManagement(request, isAdmin, isModerator, userId, resource, out, fsc);
%>
<html>
<head>
    <title>_________________/ Silverpeas - Corporate portal organizer \_________________/</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><%

    out.println(graphicFactory.getLookStyleSheet());
    if (!graphicFactory.hasExternalStylesheet())
    {
%>
    <link rel="stylesheet" type="text/css" href="styleSheets/forums.css"><%

    }
%>
    <script type="text/javascript"><%

    if (isAdmin)
    {
%>
        function confirmDeleteForum(forumId, spaceId, instanceId) {
            window.open("../../Rforums/jsp/main.jsp?Space=" + spaceId + "&Component=" + instanceId, "MyMain");
        }<%

    }
%>
        function goto_jsp(url)
        {
            window.open(url, "MyMain");
        }
    </script>
</head>

<body id="forum" marginheight="2" marginwidth="2" leftmargin="2" topmargin="2">
    <center>
        <table width="95%" border="0" align="center" cellpadding="4" cellspacing="1" class="testTableau">
            <tr class="enteteTableau">
                <td colspan="2" nowrap="nowrap" align="center"><%=resources.getString("theme")%></td>
                <td nowrap="nowrap" align="center"><%=resources.getString("forums.nbSubjects")%></td>
                <td nowrap="nowrap" align="center"><%=resources.getString("forums.nbMessages")%></td>
                <td nowrap="nowrap" align="center"><%=resources.getString("forums.lastMessage")%></td>
            </tr><%      

    // affichage des categories et de leurs forums
    if (categories != null)
    {
        Iterator it = categories.iterator();
        while (it.hasNext()) 
        {
            NodeDetail uneCategory = (NodeDetail) it.next();
            int id = uneCategory.getId();
            String nom = uneCategory.getName();
            String description = uneCategory.getDescription();
            ForumListHelper.displayForumsList(out, resources, isAdmin, isModerator, false, 0, "main",
                fsc, Integer.toString(id), nom, description);
        }
    } 

    // liste des forums sans categories
    ForumListHelper.displayForumsList( out, resources, isAdmin, isModerator, false, 0, "main", fsc,
        null, "", "");
%>
        </table>
    </center>
</body>
</html>