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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<c:set var="sessionController" value="${requestScope.forumsSessionClientController}" />
<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<%@ page import="com.stratelia.webactiv.forums.control.helpers.ForumListHelper"%>

<%@ include file="checkForums.jsp"%>
<%
    response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
    response.setHeader("Pragma", "no-cache"); //HTTP 1.0
    response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%!
public void listFolders(JspWriter out, String userId, boolean admin, int rootId, int parentId,
        String indent, ResourceLocator resource, ForumsSessionController fsc)
    throws ForumsException
{
    try
    {
        int[] sonsIds = fsc.getForumSonsIds(rootId);
        int sonId;
        Forum sonForum;
        for (int i = 0; i < sonsIds.length; i++)
        {
        	sonId = sonsIds[i];
        	sonForum = fsc.getForum(sonId);
            if (admin || fsc.isModerator(userId, sonForum.getId()))
            {
                out.print("<option value=\"");
                out.print(sonForum.getId());
                out.print("\">");
                out.print(indent + sonForum.getName());
                out.println("</option>");
            }
            listFolders(out, userId, admin, sonId, parentId, indent + "-", resource, fsc);
        }
    }
    catch (IOException ioe)
    {
        SilverTrace.info("forums", "JSPeditMessage.listFolders()", "root.EX_NO_MESSAGE", null, ioe);
    }
}
%>
<%
    boolean isModerator = false;
    boolean reply = false;
    boolean move = false;
    boolean allowMessagesInRoot = false;

    int reqForum = getIntParameter(request, "forumId", 0);
    String call = request.getParameter("call");
    String backURL = ActionUrl.getUrl(call, -1, reqForum);
    pageContext.setAttribute("backURL", backURL);

    int params = getIntParameter(request, "params");
    int action = getIntParameter(request, "action", 1);

    int folderId = 0;
    int parentId = 0;
    int forumId = 0;
    int messageId = 0;
    try
    {
        switch (action)
        {
            case 1 :
            	folderId = params;
                forumId = reqForum;
                reply = false;
                break;

            case 2 :
            	parentId = params;
                reply = true;
                break;

            case 3 :
            	forumId = reqForum;
                messageId = params;
                move = true;
                break;
        }
    }
    catch (NumberFormatException nfe)
    {
        SilverTrace.warn(
            "forums", "JSPeditMessage", "forums.EXE_PARSE_INT_FAILED", "params = "+params, nfe);
    }

    String parentTitle = "";
    if (reply)
    {
        Message parentMessage = fsc.getMessage(parentId);
        parentTitle = EncodeHelper.javaStringToHtmlString(parentMessage.getTitle());
        folderId = parentMessage.getForumId();
    }

    String folderName = EncodeHelper.javaStringToHtmlString(
        fsc.getForumName(folderId > 0 ? folderId : forumId));
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title></title>
    <view:looknfeel />
    <view:includePlugin name="wysiwyg"/>
    <view:includePlugin name="popup"/>
	<link type="text/css" href="<c:url value='/util/styleSheets/fieldset.css'/>" rel="stylesheet" />
    <script type="text/javascript" src="<%=context%>/util/javaScript/checkForm.js"></script>
    <script type="text/javascript" src="<%=context%>/forums/jsp/javaScript/forums.js"></script>
    <script type="text/javascript">
<% if (move) { %>
function validateMessage() {
    document.forms["forumsForm"].submit();
}
<% } else { %>
function init() {
	<view:wysiwyg replace="messageText" language="<%=fsc.getLanguage()%>" width="600" height="300" toolbar="forum"/>
    document.forms["forumsForm"].elements["messageTitle"].focus();
}

function validateMessage() {
    if (document.forms["forumsForm"].elements["messageTitle"].value == "") {
        alert('<%=resource.getString("emptyMessageTitle")%>');
    } else if (!isTextFilled()) {
        alert('<%=resource.getString("emptyMessageText")%>');
    } else {
        document.forms["forumsForm"].submit();
    }
}
<% } %>
</script>
</head>
<body <%addBodyOnload(out, fsc, (move ? "" : "init();"));%>>
<%
    Window window = graphicFactory.getWindow();

    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(fsc.getSpaceLabel());
    browseBar.setComponentName(fsc.getComponentLabel(), ActionUrl.getUrl("main"));
    browseBar.setPath(ForumListHelper.navigationBar(forumId, resource, fsc));

    out.println(window.printBefore());
    Frame frame=graphicFactory.getFrame();
    out.println(frame.printBefore());
    
    String formAction = ActionUrl.getUrl((reqForum > 0 ? "viewForum" : "main"), (move ? 12 : 8), (reqForum > 0 ? reqForum : -1));
%>

<% if (move) {
        String messageTitle = fsc.getMessageTitle(messageId);%>
        
        <view:board>
    	<form name="forumsForm" action="<%=formAction%>" method="post">

		<input type="hidden" name="messageId" value="<%=messageId%>"/>
        <table border="0" cellspacing="0" cellpadding="5" width="100%">
        	<tr>
            	<td class="txtlibform"><%=resource.getString("forum")%>:</td><td><%=folderName%></td>
			</tr>
            <tr>
            	<td class="txtlibform"><%=resource.getString("message")%>:</td><td><%=messageTitle%></td>
            </tr>
            <tr>
            	<td class="txtlibform"><%=resource.getString("selectMessageFolder")%></td>
            	<td>
					<select name="messageNewFolder">
                    	<option selected value="<%=reqForum%>"><%=resource.getString("selectMessageFolder")%></option>
                        <option value="<%=reqForum%>">---------------------------------------------------------</option>
		               	<% if (isAdmin && allowMessagesInRoot) { %>
		               		<option <%if (parentId == 0) {%>selected <%}%>value="0"><%=resource.getString("racine")%></option>
		               	<% } %>
		               	<%
        					listFolders(out, userId, isAdmin, 0, reqForum, "", resource, fsc);
						%>
                    </select>
				</td>
			</tr>
		</table>
		</form>
		</view:board>
<% } else { %>
	<div id="new-message">
	<form name="forumsForm" action="<%=formAction%>" method="post">
	<input type="hidden" name="forumId" value="<%=String.valueOf(folderId)%>"/>
	
	<% if (reply) { %>
		<input type="hidden" name="parentId" value="<%=parentId%>"/>
	<% } %>
	
	<fieldset id="message" class="skinFieldset">
		<legend><fmt:message key='message'/></legend>
		<div class="fields">
			<div class="field" id="messageTitleArea">
				<label for="messageTitle" class="txtlibform"><fmt:message key='messageTitle'/></label>
				<div class="champs">
					<input type="text" id="messageTitle" name="messageTitle" size="88" maxlength="<%=DBUtil.getTextFieldLength()%>"/>&nbsp;<img src="<%=context%>/util/icons/mandatoryField.gif" width="5" height="5"/>
				</div>
			</div>
			<div class="field" id="messageTextArea">
				<label for="messageText" class="txtlibform"><fmt:message key='messageText'/></label>
				<div class="champs">
					<textarea name="messageText" id="messageText"></textarea>&nbsp;<img src="<%=context%>/util/icons/mandatoryField.gif" width="5" height="5"/>
				</div>
			</div>
			<div class="field" id="messageKeywordsArea">
				<label for="forumKeywords" class="txtlibform"><fmt:message key='forumKeywords'/></label>
				<div class="champs">
					<input type="text" id="forumKeywords" name="forumKeywords" size="50" value=""/>
				</div>
			</div>
			<div class="field" id="messageSubscriptionArea">
				<label for="subscribeMessage" class="txtlibform"><fmt:message key='subscribeMessage'/></label>
				<div class="champs">
					<input type="checkbox" id="subscribeMessage" name="subscribeMessage"/>
				</div>
			</div>
		</div>
	</fieldset>
	
	<view:fileUpload fieldset="true" jqueryFormSelector="form[name='forumsForm']" />
	
	<div class="legend">
		<img alt="obligatoire" src="<%=context%>/util/icons/mandatoryField.gif" width="5" height="5"/> : <fmt:message key='GML.requiredField'/>
	</div>
	</form>
	</div>
<% } %>
    <br/>
        <fmt:message key="valider" var="validate"/>
        <fmt:message key="annuler" var="cancel"/>
          <view:buttonPane>
            <view:button action="javascript:validateMessage();" label="${validate}" disabled="false" />
            <view:button action="${pageScope.backURL}" label="${cancel}" disabled="false" />
          </view:buttonPane>
<%
    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</body>
</html>