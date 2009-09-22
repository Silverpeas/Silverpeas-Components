<%
    response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
    response.setHeader("Pragma", "no-cache"); //HTTP 1.0
    response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@ include file="checkForums.jsp"
%><%@ include file="forumsListManager.jsp"
%><%@ include file="forumsListActionManager.jsp"
%><%@ include file="messagesListManager.jsp"
%>
<%
    String mailtoAdmin = context + "/util/icons/forums_mailtoAdmin.gif";
    String newMegForum = context + "/util/icons/forums_addMessage.gif";

    int forumId = getIntParameter(request, "forumId", 0);

    boolean isModerator = fsc.isModerator(userId, forumId);
    
    fsc.resetDisplayAllMessages();

    actionManagement(request, isAdmin, isModerator, userId, resource, out, fsc);

    Forum forum = fsc.getForum(forumId);
    boolean forumActive = forum.isActive();
    String categoryId = forum.getCategory();
%>
<html>
<head>
    <title></title>
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
    <script type="text/javascript"><%
        
    if (isAdmin || isModerator) {
%>

        function confirmDeleteForum(forumId)
        {
            if (confirm("<%=Encode.javaStringToJsString(resource.getString("confirmDeleteForum"))%>"))
            {
                window.location.href = "viewForum.jsp?action=4&params=" + forumId
                    + "&forumId=<%=forumId%>";
            }
        }

        function deleteMessage(messageId, parentId, scroll)
        {
            if (confirm("<%=Encode.javaStringToJsString(resource.getString("confirmDeleteMessage"))%>"))
            {
                window.location.href = "viewForum.jsp?action=9&params=" + messageId
                    + "&forumId=<%=forumId%>";
            }
        }<%
        
    }
%>
        function notifyPopup2(context, compoId, users, groups)
        {
            SP_openWindow(context + "/RnotificationUser/jsp/Main.jsp?popupMode=Yes&editTargets=No&compoId="
                + compoId + "&theTargetsUsers=" + users + "&theTargetsGroups=" + groups,
                "notifyUserPopup", "700", "400", "menubar=no,scrollbars=no,statusbar=no");
        }

        function loadNotation()
        {
            if (document.getElementById(NOTATION_PREFIX + "1") == undefined)
            {
                setTimeout("loadNotation()", 200);
            }
            else
            {
                var img;
                var i;
                for (i = 1; i <= NOTATIONS_COUNT; i++)
                {
                    notationFlags[i - 1] = false;
                    img = document.getElementById(NOTATION_PREFIX + i);
                    img.alt = "<%=resource.getString("forums.giveNote")%> " + i + "/" + NOTATIONS_COUNT;
                    img.title = "<%=resource.getString("forums.giveNote")%> " + i + "/" + NOTATIONS_COUNT;
                    if (!readOnly)
                    {
                        img.onclick = function() {notationNote(this);};
                        img.onmouseover = function() {notationOver(this);};
                        img.onmouseout = function() {notationOut(this);};
                    }
                }
            }
        }

        function notationNote(image) {
            var index = getNotationIndex(image);
            var updateNote = false;
            if (userNote > 0) {
                if (index == userNote) {
                    alert("<%=resource.getString("forums.sameNote")%> " + userNote + ".");
                } else {
                	updateNote = confirm("<%=resource.getString("forums.replaceNote")%> " + userNote + " <%=resource.getString("forums.by")%> " + index + ".");
                }
            } else {
            	updateNote = true;
            }
            if (updateNote) {
            	currentNote = index;
                document.forms["notationForm"].elements["note"].value = currentNote;
                document.forms["notationForm"].submit();
            }
        }
    </script>
</head>

<body id="forum" <%addBodyOnload(out, fsc);%>><%

    Window window = graphicFactory.getWindow();

    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(fsc.getSpaceLabel());
    browseBar.setComponentName(fsc.getComponentLabel(), ActionUrl.getUrl("main"));
    browseBar.setPath(navigationBar(forumId, resource, fsc));
    
    if (!isReader)
    {
        OperationPane operationPane = window.getOperationPane();
        operationPane.addOperation(mailtoAdmin, resource.getString("mailAdmin"),
            "javascript:notifyPopup2('" + context + "', '" + fsc.getComponentId() + "', '"
                + fsc.getAdminIds() + "', '');");
        if (isAdmin && fsc.forumInsideForum())
        {
            displayForumsAdminButtons(isModerator, operationPane, forumId, "viewForum", resource);
        }
        if (fsc.isForumActive(forumId))
        {
            operationPane.addOperation(newMegForum, resource.getString("newMessage"),
            	ActionUrl.getUrl("editMessage", "viewForum", 1, forumId, forumId));
        }
    }

    out.println(window.printBefore());
    Frame frame = graphicFactory.getFrame();
    out.println(frame.printBefore());
%>
    <br>
    <center>
        <table class="intfdcolor4" border="0" cellspacing="0" cellpadding="0" width="98%">
            <tr class="notationLine">
                <td align="right"><%

    int[] forumNotes = displayForumNotation(out, resources, forumId, fsc, isReader);

              %></td>
            </tr>
            <tr>
                <td valign="top">
                    <table class="contourintfdcolor" border="0" cellspacing="0" cellpadding="5" width="100%">
                        <form name="nameForm" action="" method="post">
                            <tr>
                                <td valign="top">
                                    <center>
                                        <table width="100%" border="0" align="center" cellpadding="4" cellspacing="1" class="testTableau">
                                            <%-- affichage de l'entête des colonnes --%>
                                            <tr class="enteteTableau">
                                                <td nowrap="nowrap" align="center" colspan="3"><%=resources.getString("forums.nbSubjects")%></td>
                                                <td nowrap="nowrap" align="center"><%=resources.getString("forums.lastMessage")%></td>
                                                <td nowrap="nowrap" align="center"><%=resources.getString("forums.nbMessages")%></td>
                                                <td nowrap="nowrap" align="center"><%=resources.getString("forums.nbViews")%></td>
                                                <td nowrap="nowrap" align="center"><%=resources.getString("forums.notation")%></td><%
    if (isAdmin || isModerator) {
%>
                                                <td nowrap="nowrap" align="center"><%=resources.getString("operations")%></td><%

    }
%>
                                            </tr><%
                                            
    //displayForums(out, resources, isAdmin, isModerator, isReader, forumId, "viewForum", fsc, categoryId);
                                            
    Message[] messages = fsc.getMessagesList(forumId);
    deployAll(messages, fsc);
    displayMessagesList(out, resource, userId, isAdmin, isModerator, isReader, true, forumId, false,
        "viewForum", fsc, resources);
%>
                                        </table>
                                    </center>
                                </td>
                            </tr>
                        </form>
                    </table>
                </td>
            </tr>
        </table>
    </center><%

    out.println(frame.printMiddle());

%>
    <br>
    <center><%

    ButtonPane backButtonPane = graphicFactory.getButtonPane();
    backButtonPane.addButton(graphicFactory.getFormButton("Retour", "main.jsp", false));
    backButtonPane.setHorizontalPosition();
    out.println(backButtonPane.print());
%>
    </center>
    <br><%

    out.println(frame.printAfter());
    out.println(window.printAfter());
    
    if (!isReader && forumNotes.length > 0)
    {
%>
    <form name="notationForm" action="viewForum" method="post">
        <input name="action" type="hidden" value="16"/>
        <input name="forumId" type="hidden" value="<%=forumId%>"/>
        <input name="note" type="hidden" value=""/>
    </form>
    <script type="text/javascript">
        readOnly = <%=isReader%>;
        currentNote = <%=forumNotes[0]%>;
        userNote = <%=forumNotes[1]%>;
        loadNotation();
    </script><%

    }
%>
</body>
</html>