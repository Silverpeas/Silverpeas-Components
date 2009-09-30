<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%
    response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
    response.setHeader("Pragma", "no-cache"); //HTTP 1.0
    response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@ include file="checkForums.jsp"%>
<%@ include file="forumsListManager.jsp"%>

<%
    int messageId = getIntParameter(request, "params");
    Message message = fsc.getMessage(messageId);
    int forumId = message.getForumId();
    String title = message.getTitle();
    String text = message.getText();
    String keywords = fsc.getMessageKeywords(messageId);
%>
<html>
<head>
    <title></title>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"><%
    
    out.println(graphicFactory.getLookStyleSheet());
%>
    <script type="text/javascript" src="<%=context%>/util/javaScript/checkForm.js"></script>
    <script type="text/javascript" src="<%=context%>/forums/jsp/javaScript/forums.js"></script>
</head>

<body marginheight="5" marginwidth="5" bgcolor="#FFFFFF" leftmargin="5" topmargin="5" <%addBodyOnload(out, fsc, "document.forms['forumsForm'].elements['forumKeywords'].focus();");%>>
<% 
    Window window = graphicFactory.getWindow();

    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(fsc.getSpaceLabel());
    browseBar.setComponentName(fsc.getComponentLabel(), ActionUrl.getUrl("main"));
    browseBar.setPath(navigationBar(forumId, resource, fsc));

    out.println(window.printBefore());
    Frame frame=graphicFactory.getFrame();
    out.println(frame.printBefore());

    String formAction = ActionUrl.getUrl("viewForum", 15, forumId);
%>
    <center>
        <table class="intfdcolor4" border="0" cellpadding="0" cellspacing="0" width="98%">
        <form name="forumsForm" action="<%=formAction%>" method="post">
            <tr align="center">
                <td valign="top" align="center">
                    <input type="hidden" name="messageId" value="<%=messageId%>">
                    <table border="0" cellspacing="0" cellpadding="5" width="100%" class="contourintfdcolor" align="center">
                        <tr>
                            <td valign="top"><span class="txtlibform"><%=resource.getString("messageTitle")%> :</span></td>
                            <td valign="top"><span class="txtnote"><%=message.getTitle()%></span></td>
                        </tr>
                        <tr>
                            <td valign="top"><span class="txtlibform"><%=resource.getString("messageText")%> :</span></td>
                            <td valign="top" class="msgText"><span class="txtnote"><%=text%></span></td>
                        </tr>
                        <tr>
                            <td valign="top"><span class="txtlibform"><%=resource.getString("forumKeywords")%> : </span></td>
                            <td valign="top"><input type="text" name="forumKeywords" size="50" value="<%=keywords%>"/></td>
                        </tr>
                    </table>
                </td>
            </tr>
        </form>
        </table>
    </center><%

    out.println(frame.printMiddle());
%>
    <br>
    <center><%

    ButtonPane buttonPane = graphicFactory.getButtonPane();
    buttonPane.addButton(graphicFactory.getFormButton(
        resource.getString("valider"), "javascript:document.forms['forumsForm'].submit();", false));
    buttonPane.addButton(graphicFactory.getFormButton(
        resource.getString("annuler"), ActionUrl.getUrl("viewForum", -1, forumId), false));
    buttonPane.setHorizontalPosition();
    out.println(buttonPane.print());
%>
    </center>
    <br><%

    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</body>
</html>