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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
    response.setHeader("Pragma", "no-cache"); //HTTP 1.0
    response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@ include file="checkForums.jsp"%>
<%
    int messageId = getIntParameter(request, "params");
    Message message = fsc.getMessage(messageId);
    int forumId = message.getForumId();
    String text = message.getText();
    String title = message.getTitle();

    ResourceLocator settings = fsc.getSettings();
    String configFile = settings.getString("configFile",
        URLManager.getApplicationURL() + "/wysiwyg/jsp/javaScript/myconfig.js");

    //Icons
    String mandatoryField = context + "/util/icons/mandatoryField.gif";
%>
<html>
<head>
  <title></title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><%
    out.println(graphicFactory.getLookStyleSheet());
%>
    <script type="text/javascript" src="<%=context%>/util/javaScript/checkForm.js"></script>
    <script type="text/javascript" src="<%=context%>/forums/jsp/javaScript/forums.js"></script>
    <script type="text/javascript">

    function validateMessage() {
    	document.refusalForm.submit();
    }

    function cancelMessage() {

    }

    </script>
</head>

<body>
<%
    Window window = graphicFactory.getWindow();
    Frame frame=graphicFactory.getFrame();
    Board board = graphicFactory.getBoard();

    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(fsc.getSpaceLabel());
    browseBar.setComponentName(fsc.getComponentLabel(), ActionUrl.getUrl("main", -1, forumId));
    browseBar.setPath(ForumListHelper.navigationBar(forumId, resource, fsc));

    out.println(window.printBefore());
    out.println(frame.printBefore());
    out.println(board.printBefore());

    %>

    <FORM NAME="refusalForm" Action="RefuseMessage" Method="POST">
	    <TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=intfdcolor4>
	      <TR>
	         <TD></TD>
	         <TD valign="top"><%=EncodeHelper.javaStringToHtmlString(message.getTitle())%></TD>
	      <TR>
	         <TD class="txtlibform" valign=top><%=resource.getString("RefusalMotive")%> :</TD>
	         <TD>
	            <textarea name="Motive" rows="5" cols="60"></textarea>&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5">
	            <input type="hidden" name="params" value="<%=messageId%>">
	         </TD>
	      </TR>
	      <TR>
	         <TD colspan="2">
	           ( <img border="0" src="<%=mandatoryField%>" width="5" height="5"> : <%=resources.getString("GML.requiredField")%> )
	         </TD>
	      </TR>
	   </TABLE>
    </FORM>
    <%

 	  out.println(board.printAfter());
	  out.println("<br/>");

	  ButtonPane msgButtonPane = graphicFactory.getButtonPane();
    msgButtonPane.addButton(graphicFactory.getFormButton(
        resource.getString("valider"), "javascript:validateMessage();", false));
    String backUrl = ActionUrl.getUrl("viewMessage", "viewForum", 1, messageId, forumId);
    msgButtonPane.addButton(graphicFactory.getFormButton(resource.getString("annuler"), backUrl, false));
    msgButtonPane.setHorizontalPosition();
    out.println("<center>" + msgButtonPane.print()+ "</center>");


    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</body>
</html>
