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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags/silverpeas/util" %>
<%
    response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
    response.setHeader("Pragma", "no-cache"); //HTTP 1.0
    response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@ include file="checkForums.jsp" %>
<%
    Collection<NodeDetail> categories = fsc.getAllCategories();
    boolean isModerator = false;
    ForumActionHelper.actionManagement(request, isAdmin, isModerator, userId, resource, out, fsc);
    boolean isForumSubscriberByInheritance =
      (Boolean) request.getAttribute("isForumSubscriberByInheritance");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="X-UA-Compatible" content="IE=edge" />
  <view:looknfeel />
<%
    out.println(graphicFactory.getLookStyleSheet());
    if (!graphicFactory.hasExternalStylesheet()) {
%>
    <link rel="stylesheet" type="text/css" href="styleSheets/forums.css" />
<% } %>
<script type="text/javascript">
<% if (isAdmin) { %>
        function confirmDeleteForum(forumId, spaceId, instanceId) {
            window.open("../../Rforums/jsp/main.jsp?Space=" + spaceId + "&Component=" + instanceId, "MyMain");
        }
<% } %>
function goto_jsp(url) {
    window.open(url, "MyMain");
}
</script>
</head>
<body id="forum">
<tags:displayNotification/>
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
      for (final NodeDetail uneCategory : categories) {
        int id = uneCategory.getId();
        String nom = uneCategory.getName();
        String description = uneCategory.getDescription();
        ForumListHelper
            .displayForumsList(out, resources, isAdmin, isModerator, false, 0, "main", fsc,
                Integer.toString(id), nom, description, isForumSubscriberByInheritance);
      }
    } 

    // liste des forums sans categories
    ForumListHelper.displayForumsList( out, resources, isAdmin, isModerator, false, 0, "main", fsc,
        null, "", "", isForumSubscriberByInheritance);
%>
        </table>
</body>
</html>