<%--

    Copyright (C) 2000 - 2013 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
    response.setHeader("Pragma", "no-cache"); //HTTP 1.0
    response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@ include file="checkForums.jsp"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
    int messageId = getIntParameter(request, "params");
    Message message = fsc.getMessage(messageId);
    int forumId = message.getForumId();
    String title = message.getTitle();
    String text = message.getText();
    String keywords = fsc.getMessageKeywords(messageId);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<view:looknfeel/>
<script type="text/javascript" src="<%=context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=context%>/forums/jsp/javaScript/forums.js"></script>
</head>
<body <%addBodyOnload(out, fsc, "document.forms['forumsForm'].elements['forumKeywords'].focus();");%>>
<%
    Window window = graphicFactory.getWindow();

    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(fsc.getSpaceLabel());
    browseBar.setComponentName(fsc.getComponentLabel(), ActionUrl.getUrl("main"));
    browseBar.setPath(ForumListHelper.navigationBar(forumId, fsc));

    out.println(window.printBefore());
    Frame frame=graphicFactory.getFrame();
    out.println(frame.printBefore());

    String formAction = ActionUrl.getUrl("viewForum", 15, forumId);
%>
	<view:board>
	<form name="forumsForm" action="<%=formAction%>" method="post">
	   <input type="hidden" name="messageId" value="<%=messageId%>"/>
	   <table border="0" cellspacing="0" cellpadding="5" width="100%">
	       <tr>
	           <td class="txtlibform"><%=resource.getString("messageTitle")%> :</td>
	           <td><%=message.getTitle()%></td>
	       </tr>
	       <tr>
	           <td class="txtlibform"><%=resource.getString("messageText")%> :</td>
	           <td><%=text%></td>
	       </tr>
	       <tr>
	           <td class="txtlibform"><%=resource.getString("forumKeywords")%> :</td>
	           <td><input type="text" name="forumKeywords" size="50" value="<%=keywords%>"/></td>
	       </tr>
	   </table>
    </form>
    </view:board>
    <br/>
<%
    ButtonPane buttonPane = graphicFactory.getButtonPane();
    buttonPane.addButton(graphicFactory.getFormButton(
        resource.getString("valider"), "javascript:document.forms['forumsForm'].submit();", false));
    buttonPane.addButton(graphicFactory.getFormButton(
        resource.getString("annuler"), ActionUrl.getUrl("viewForum", -1, forumId), false));
    buttonPane.setHorizontalPosition();
    out.println(buttonPane.print());

    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</body>
</html>