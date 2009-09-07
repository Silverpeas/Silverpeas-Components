<%
    response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
    response.setHeader("Pragma", "no-cache"); //HTTP 1.0
    response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@ include file="checkForums.jsp" %>
<%@ include file="forumsListManager.jsp" %>
<%@ include file="forumsListActionManager.jsp" %>
<%
    Collection categories = fsc.getAllCategories();

    String rssURL = (fsc.isUseRss() ? fsc.getRSSUrl() : null);
    
    String mailtoAdmin = context + "/util/icons/forums_mailtoAdmin.gif";
    String pdcUtilizationSrc = context + "/pdcPeas/jsp/icons/pdcPeas_paramPdc.gif";
    
    boolean isModerator = false;
    
    fsc.resetDisplayAllMessages();

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
    <script type="text/javascript" src="<%=context%>/forums/jsp/javaScript/forums.js"></script>
    <script type="text/javascript" src="<%=context%>/util/javaScript/animation.js"></script>
    <script type="text/javascript">
        function confirmDeleteForum(forumId)
        {
            if (confirm("<%=resources.getString("confirmDeleteForum")%>"))
            {
                window.location.href = "main.jsp?action=4&params=" + forumId;
            }
        }
        
        function confirmDeleteCategory(categoryId)
        {
            if (confirm("<%=resources.getString("confirmDeleteCategory")%>"))
            {
                window.location.href = "DeleteCategory?CategoryId=" + categoryId;
            }
        }
        
        function notifyPopup2(context,compoId,users,groups)
        {
            SP_openWindow(context + '/RnotificationUser/jsp/Main.jsp?popupMode=Yes&editTargets=No&compoId='
                + compoId + '&theTargetsUsers=' + users + '&theTargetsGroups=' + groups,
                'notifyUserPopup', '700', '400', 'menubar=no,scrollbars=no,statusbar=no');
        }
        
        function openSPWindow(fonction, windowName)
        {
            pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400',
                'scrollbars=yes, resizable, alwaysRaised');
        }
    </script><%
    
    if (StringUtil.isDefined(rssURL)) {
%>
    <link rel="alternate" type="application/rss+xml" title="<%=componentLabel%> : <%=resources.getString("forums.rssLast")%>" href="<%=context+rssURL%>"/><%
    
    }
%>
</head>

<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF" <%addBodyOnload(out, fsc);%>>
<% 
    // AFFICHAGE DE LA FENETRE DU COMPOSANT
    Window window = graphicFactory.getWindow();

    // AFFICHAGE DE LA BARRE DE NAVIGATION
    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(fsc.getSpaceLabel()); 
    browseBar.setComponentName(fsc.getComponentLabel(), ActionUrl.getUrl("main"));

    // AFFICHAGE DES OPERATIONS
    if (!isReader)
    {
        OperationPane operationPane = window.getOperationPane();
    
        if (isAdmin && fsc.isPdcUsed()) 
        {    
            operationPane.addOperation(pdcUtilizationSrc, resources.getString("PDCUtilization"),
                "javascript:onClick=openSPWindow('" + context
                    + "/RpdcUtilization/jsp/Main?ComponentId=" + fsc.getComponentId()
                    + "', 'utilizationPdc1')");
            operationPane.addLine();
        }
            
        operationPane.addOperation(mailtoAdmin, resources.getString("mailAdmin"),
            "javascript:notifyPopup2('" + context + "','" + fsc.getComponentId() + "','"
                + fsc.getAdminIds() + "', '');");
        if (isAdmin)
        {
             displayForumsAdminButtonsMain(isModerator, operationPane, 0, "main", resource);
        }
    }
    
    out.println(window.printBefore());
    Frame frame = graphicFactory.getFrame();
    out.println(frame.printBefore());
%>
    <center>
        <table width="95%" border="0" align="center" cellpadding="4" cellspacing="1" class="testTableau">
            <tr class="enteteTableau">
                <td colspan="2" nowrap="nowrap" align="center"><%=resources.getString("theme")%></td>
                <td nowrap="nowrap" align="center"><%=resources.getString("forums.nbSubjects")%></td>
                <td nowrap="nowrap" align="center"><%=resources.getString("forums.nbMessages")%></td>
                <td nowrap="nowrap" align="center"><%=resources.getString("forums.lastMessage")%></td>
                <td nowrap="nowrap" align="center"><%=resources.getString("forums.notation")%></td><%

    if (isAdmin)
    {
%>
                <td nowrap="nowrap" align="center"><%=resources.getString("operations")%></td><%
                
    }
%>
            </tr>
<%
    // affichage des catégories et de leurs forums
    if (categories != null)
    {
        Iterator it = (Iterator) categories.iterator();
        while (it.hasNext()) 
        {
            NodeDetail category = (NodeDetail) it.next();
            displayForumsList(out, resources, isAdmin, isModerator, isReader, 0, "main", fsc,
                Integer.toString(category.getId()), category.getName(), category.getDescription());
        }
    }

    // liste des forums sans catégories
    displayForumsList(out, resources, isAdmin, isModerator, isReader, 0, "main", fsc, null, "", "");
    
%>
        </table><%

    if (!fsc.isExternal() || !isReader)
    {
%>
        <img src="icons/buletColoredGreen.gif"><%=resources.getString("forums.notNewMessageVisite")%>
        <br>
        <img src="icons/buletRed.gif"><%=resources.getString("forums.newMessageVisite")%><%

    }
    
    if (StringUtil.isDefined(rssURL)) {
%>
        <table align="center">
            <tr>
                <td><a href="<%=context+rssURL%>"><img src="icons/rss.gif" border="0"></a></td>
            </tr>
            <link rel="alternate" type="application/rss+xml" title="<%=componentLabel%> : <%=resources.getString("forums.rssLast")%>" href="<%=context+rssURL%>">
        </table><%
        
    } 
%>
    </center>

<%
    out.println(frame.printMiddle());
    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</body>
</html>