<%
    response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
    response.setHeader("Pragma", "no-cache"); //HTTP 1.0
    response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@ include file="checkForums.jsp" %>
<%@ include file="forumsListManagerPortlet.jsp" %>
<%@ include file="forumsListActionManager.jsp" %>
<%
    String mailtoAdmin = context + "/util/icons/forums_mailtoAdmin.gif";

    Collection categories = fsc.getAllCategories();
    boolean isModerator = false;
    actionManagement(request, isAdmin, isModerator, userId, resource, out, fsc);
%>
<html>
<head>
    <title>_________________/ Silverpeas - Corporate portal organizer \_________________/</title>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"><%

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

<body marginheight="2" marginwidth="2" leftmargin="2" topmargin="2">
    <center><%

    //displayForumsList(spaceId, instanceId, out, resource, isAdmin, isModerator, 0, "main", fsc);
%>
        <table width="95%" border="0" align="center" cellpadding="4" cellspacing="1" class="testTableau">
            <tr class="enteteTableau">
                <td colspan="2" nowrap="nowrap" align="center"><%=resources.getString("theme")%></td>
                <td nowrap="nowrap" align="center"><%=resources.getString("forums.nbSubjects")%></td>
                <td nowrap="nowrap" align="center"><%=resources.getString("forums.nbMessages")%></td>
                <td nowrap="nowrap" align="center"><%=resources.getString("forums.lastMessage")%></td>
            </tr><%      

    // affichage des catégories et de leurs forums
    if (categories != null)
    {
        Iterator it = (Iterator) categories.iterator();
        while (it.hasNext()) 
        {
            NodeDetail uneCategory = (NodeDetail) it.next();
            int id = uneCategory.getId();
            String nom = uneCategory.getName();
            String description = uneCategory.getDescription();
            displayForumsList(spaceId, instanceId, out, resources, isAdmin, isModerator, 0, "main",
                fsc, Integer.toString(id), nom, description);
        }
    } 

    // liste des forums sans catégories
    displayForumsList(spaceId, instanceId, out, resources, isAdmin, isModerator, 0, "main", fsc,
        null, "", "");
%>
        </table>
    </center>
</body>
</html>