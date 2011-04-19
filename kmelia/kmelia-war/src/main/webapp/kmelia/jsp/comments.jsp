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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<c:set var="sessionController" value="${requestScope.kmelia}" />
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkKmelia.jsp" %>
<%@ include file="tabManager.jsp.inc" %>
<%@ include file="topicReport.jsp.inc" %>

<%
PublicationDetail pubDetail  = kmeliaScc.getSessionPublication().getPublication().getPublicationDetail();

String profile = request.getParameter("Profile");
String id = request.getParameter("PubId");
String wizard = (String) request.getAttribute("Wizard");
String currentLang = (String) request.getAttribute("Language");
String pubName = pubDetail.getName(currentLang);
pageContext.setAttribute("pubName", pubName);

//Vrai si le user connecte est le createur de cette publication ou si il est admin
boolean isOwner = kmeliaScc.getSessionOwner();
String linkedPathString = kmeliaScc.getSessionPath();
String user_id = kmeliaScc.getUserId();
%>
<html>
<head>
<title></title>
<view:looknfeel />
<script language="javascript">
function topicGoTo(id) {
  location.href="GoToTopic?Id="+id;
}

function sendOperation(operation) {
  document.pubForm.Action.value = operation;
  document.pubForm.submit();
}
</script>
</head>
<body>
  <view:browseBar path="${sessionController.sessionPath}" extraInformations="${pageScope.pubName}" />
  <view:window>
    <view:frame>
      <script type="text/javascript">
        var commentCount = 0;
      </script>
<%
  Board boardHelp = gef.getBoard();
  if (isOwner){
    displayAllOperations(id, kmeliaScc, gef, "ViewComment", resources, out, kmaxMode);
  } else {
    displayUserOperations(id, kmeliaScc, gef, "ViewComment", resources, out, kmaxMode);
  }
  if ("finish".equals(wizard)) {
    out.println(boardHelp.printBefore());
    out.println("<table border=\"0\"><tr>");
    out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\"" + resources.getIcon("kmelia.info") + "\"></td>");
    out.println("<td>" + kmeliaScc.getString("kmelia.HelpComment") + "</td>");
    out.println("</tr></table>");
    out.println(boardHelp.printAfter());
    out.println("<br/>");
  }
  out.flush();
  String url = kmeliaScc.getComponentUrl() + "Comments?PubId="+id;
  String indexIt = "0";
  if (kmeliaScc.isIndexable(pubDetail)) {
    indexIt = "1";
  }

  String callback = "function( event ) { " +
      "if (event.type === 'listing') { " +
        "commentCount = event.comments.length; $('#comment-tab').html('" + resources.getString("Comments") + " ( ' + event.comments.length + ')'); }" +
      "else if (event.type === 'deletion') { commentCount--; $('#comment-tab').html('" + resources.getString("Comments") + " ( ' + commentCount + ')'); }" +
      "else if (event.type === 'addition') { commentCount++; $('#comment-tab').html('" + resources.getString("Comments") + " ( ' + commentCount + ')'); } }";
%>
<view:board>

  <view:comments userId="<%= user_id%>" componentId="<%= kmeliaScc.getComponentId() %>" resourceId="<%= id %>" indexed="<%= indexIt %>"
                callback="<%= callback %>"/>

</view:board>
 </view:frame>

  </view:window>
</body>
</html>