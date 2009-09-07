<%
    response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
    response.setHeader("Pragma", "no-cache"); //HTTP 1.0
    response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@ include file="checkForums.jsp"%>
<%@ include file="forumsListManager.jsp"%>
<%@ include file="messagesListManager.jsp"%>
<%
    int messageId = getIntParameter(request, "params");
    Message message = fsc.getMessage(messageId);
    int forumId = message.getForumId();
    String text = message.getText();
    String title = message.getTitle();
    
    ResourceLocator settings = fsc.getSettings();
    String configFile = SilverpeasSettings.readString(settings, "configFile",
        URLManager.getApplicationURL() + "/wysiwyg/jsp/javaScript/myconfig.js");
%>
<html>
<head>
    <title>_________________/ Silverpeas - Corporate portal organizer \_________________/</title>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"><%

    out.println(graphicFactory.getLookStyleSheet());
%>
    <script type="text/javascript" src="<%=context%>/util/javaScript/checkForm.js"></script>
    <script type="text/javascript" src="<%=context%>/forums/jsp/javaScript/forums.js"></script>
    <script type="text/javascript" src="<%=context%>/wysiwyg/jsp/FCKeditor/fckeditor.js"></script>
    <script type="text/javascript">
        function init()
        {
        	var oFCKeditor = new FCKeditor("messageText");
            oFCKeditor.Width = "500";
            oFCKeditor.Height = "300";
            oFCKeditor.BasePath = "<%=URLManager.getApplicationURL()%>/wysiwyg/jsp/FCKeditor/";
            oFCKeditor.DisplayErrors = true;
            oFCKeditor.Config["AutoDetectLanguage"] = false;
            oFCKeditor.Config["DefaultLanguage"] = "<%=fsc.getLanguage()%>";
            oFCKeditor.Config["CustomConfigurationsPath"] = "<%=configFile%>";
            oFCKeditor.ToolbarSet = "quickinfo";
            oFCKeditor.Config["ToolbarStartExpanded"] = true;
            oFCKeditor.ReplaceTextarea();
        }

        function validateMessage()
        {
            if (document.forms["forumsForm"].elements["messageTitle"].value == "")
            {
                alert('<%=resource.getString("emptyMessageTitle")%>');
            }
            else if (!isTextFilled())
            {
                alert('<%=resource.getString("emptyMessageText")%>');
            }
            else
            {
                document.forms["forumsForm"].submit();
            }
        }
    </script>
</head>

<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF" <%addBodyOnload(out, fsc, "init()");%>>
<%
    Window window = graphicFactory.getWindow();
    Frame frame=graphicFactory.getFrame();

    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(fsc.getSpaceLabel());
    browseBar.setComponentName(fsc.getComponentLabel(), ActionUrl.getUrl("main", -1, forumId));
    browseBar.setPath(navigationBar(forumId, resource, fsc));
    
    out.println(window.printBefore());
    out.println(frame.printBefore());
    
    String formAction = "";
%>
    <center>
        <table width="98%" border="0" cellspacing="0" cellpadding="0" class="intfdcolor4">
        <form name="forumsForm" action="viewMessage" method="post">
            <tr>
                <td valign="top">
                    <table width="100%" border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor">
                        <tr>
                            <td valign="top">
                                <table border="0" cellspacing="0" cellpadding="5" width="100%">
                                    <!-- REPONSE -->
                                    <!-- ligne séparatrice
                                    <tr>
                                        <td colspan="2"><img src="<%=context%>/util/icons/colorPix/1px.gif" width="100%" height="1" class="intfdcolor"></td>
                                    </tr>
                                    -->
                                    <tr>
                                        <td colspan="2"><span class="txtnav"><!-- <img src="icons/fo_flechebas.gif" width="11" height="6">&nbsp;<%=resource.getString("repondre")%> --></span></td>
                                    </tr>
                                    <tr>
                                        <td align="left" valign="top"><span class="txtlibform"><%=resource.getString("messageTitle")%> :&nbsp;</span></td>
                                        <td valign="top"><input type="text" name="messageTitle" value="<%=title%>" size="88" maxlength="<%=DBUtil.TextFieldLength%>"></td>
                                    </tr>
                                    <tr>
                                        <td align="left" valign="top"><span class="txtlibform"><%=resource.getString("messageText")%> :&nbsp;</span></td>
                                        <td valign="top"><font size=1><textarea name="messageText" id="messageText"><%=text%></textarea></font></td>
                                    </tr>
                                    <tr>
                                        <td align="left" valign="top"><span class="txtlibform"><%=resource.getString("subscribeMessage")%> :&nbsp;</span></td>
                                        <td valign="top"><input type="checkbox" name="subscribeMessage"></td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                    <br>
                    <center><%

    String backUrl = ActionUrl.getUrl("viewMessage", "viewForum", 1, messageId, forumId);
    ButtonPane buttonPane = graphicFactory.getButtonPane();
    buttonPane.addButton(graphicFactory.getFormButton(
        resource.getString("valider"), "javascript:validateMessage();", false));
    buttonPane.addButton(graphicFactory.getFormButton(
        resource.getString("annuler"), backUrl, false));
    buttonPane.setHorizontalPosition();
    out.println(buttonPane.print());
%>
                    </center>
                </td>
            </tr>
            <input type="hidden" name="action" value="8"/>
            <input type="hidden" name="forumId" value="<%=message.getForumId()%>"/>
            <input type="hidden" name="params" value="<%=messageId%>"/>
            <input type="hidden" name="parentId" value="<%=message.getParentId()%>"/>
        </form>
        </table>
    </center><%

    out.println(frame.printMiddle());
    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</body>
</html>