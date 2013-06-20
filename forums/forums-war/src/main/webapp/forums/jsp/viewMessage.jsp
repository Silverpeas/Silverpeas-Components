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
<%@page import="org.silverpeas.upload.UploadedFile"%>
<%@page import="org.silverpeas.upload.FileUploadManager"%>
<%
    response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
    response.setHeader("Pragma", "no-cache"); //HTTP 1.0
    response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<c:set var="sessionController" value="${requestScope.forumsSessionClientController}" />
<c:set var="componentId" value="${sessionController.componentId}" />
<c:set var="isReader" value="${sessionController.reader}" />
<c:set var="isUser" value="${sessionController.user}" />
<c:set var="isAdmin" value="${sessionController.admin}" />
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
<%@ page import="java.util.Map"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="com.stratelia.webactiv.forums.control.helpers.ForumHelper"%>
<%@ page import="com.stratelia.webactiv.forums.control.helpers.ForumListHelper"%>
<%@ page import="java.util.Hashtable"%>
<%@ include file="checkForums.jsp"%>
<%
    int messageId = 0;

    int forumId = getIntParameter(request, "forumId");
    String call = request.getParameter("call");
    int action = getIntParameter(request, "action", 1);
    int params = getIntParameter(request, "params");
    int currentMessageId = -1;
    String nbModeratorsString = (String) request.getAttribute("NbModerators");
    int nbModerators = 0;
    if (StringUtil.isDefined(nbModeratorsString)) {
      nbModerators = Integer.parseInt(nbModeratorsString);
    }
    boolean scrollToMessage = false;
    boolean displayAllMessages = true;
    try {
        switch (action) {
            case 1 :
                // Affichage de la liste
                messageId = params;
                if ("true".equals(request.getParameter("addStat"))) {
                    // depuis la page de forums ou de messages
                    int parentId = fsc.getMessageParentId(messageId);
                    while (parentId > 0) {
                        messageId = parentId;
                        parentId = fsc.getMessageParentId(messageId);
                    }
                    fsc.addMessageStat(messageId, userId);
                    messageId = params;
                }
                if ("true".equals(request.getParameter("changeDisplay"))) {
                    // changement du type d'affichage
                    fsc.changeDisplayAllMessages();
                }
                break;

            case 8 :
                int parentId = getIntParameter(request, "parentId", 0);
                String messageTitle = request.getParameter("messageTitle").trim();
                String messageText = request.getParameter("messageText").trim();
                String subscribe = request.getParameter("subscribeMessage");

                if ((messageTitle.length() > 0) && (messageText.length() > 0)) {
                    if (params == -1) {
                      	Collection<UploadedFile> uploadedFiles = FileUploadManager.getUploadedFiles(request, fsc.getUserDetail());
                        int result = fsc.createMessage(messageTitle, userId, forumId, parentId, messageText, null, uploadedFiles);
                        messageId = result;
                    } else {
                        // Modification
                        messageId = params;
                        fsc.updateMessage(messageId, parentId, messageTitle, messageText);
                    }
                    if (subscribe == null) {
                        subscribe = "0";
                    } else {
                        subscribe = "1";
                        if (messageId != 0) {
                            fsc.subscribeMessage(messageId, userId);
                        }
                    }
                    if (parentId > 0) {
                        fsc.deployMessage(parentId);
                    }
                }
                call = "viewForum";
                scrollToMessage = true;
                break;

            case 9 :
                messageId = fsc.getMessageParentId(params);
                fsc.deleteMessage(params);
                call = "viewForum";
                scrollToMessage = "true".equals(request.getParameter("scroll"));
                break;

            case 10 :
                fsc.deployMessage(params);
                messageId = params;
                break;

            case 11 :
                fsc.undeployMessage(params);
                messageId = params;
                break;

            case 13 :
                fsc.unsubscribeMessage(params, userId);
                messageId = params;
                break;

            case 14 :
                fsc.subscribeMessage(params, userId);
                messageId = params;
                break;

            case 15 :
                // Notation d'un message.
                int note = getIntParameter(request, "note", -1);
                if (note > 0)
                {
                    fsc.updateMessageNotation(params, note);
                }
                messageId = params;
                break;
        }
    }
    catch (NumberFormatException nfe) {
      SilverTrace.info("forums", "JSPviewMessage", "root.EX_NO_MESSAGE", null, nfe);
    }
    String backURL = ActionUrl.getUrl(call, -1, forumId);

    Message message = fsc.getMessage(messageId);
    if (forumId == -1) {
        forumId = message.getForumId();
    }

    boolean forumActive = false;

    int[] forumNotes = new int[0];
    if(message != null) {
        int reqForum = (forumId != -1 ? forumId : 0);
        int folderId = message.getForumId();
        boolean isModerator = fsc.isModerator(userId, folderId);
        pageContext.setAttribute("isModerator", isModerator);
        displayAllMessages = fsc.isDisplayAllMessages();

        forumActive = fsc.isForumActive(folderId);

        String folderName = EncodeHelper.javaStringToHtmlString(fsc.getForumName(folderId > 0 ? folderId : params));

        ResourceLocator settings = fsc.getSettings();
        String configFile = settings.getString("configFile",
            URLManager.getApplicationURL() + "/wysiwyg/jsp/javaScript/myconfig.js");

        // Messages
        currentMessageId = messageId;
        int parent = fsc.getMessageParentId(currentMessageId);
        while (parent > 0) {
            currentMessageId = parent;
            parent = fsc.getMessageParentId(currentMessageId);
        }
        pageContext.setAttribute("title", message.getTitle());
        Message[] messages = fsc.getMessagesList(folderId, currentMessageId);
        int messagesCount = messages.length;
%>

<%@page import="java.util.List"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title><c:out value="${pageScope.title}" /></title>
    <view:looknfeel />
    <view:includePlugin name="wysiwyg"/>
    <view:includePlugin name="popup"/>
	<view:includePlugin name="notifier"/>
	<link type="text/css" href="<c:url value='/util/styleSheets/fieldset.css'/>" rel="stylesheet" />
    <script type="text/javascript" src="<c:url value='/util/javaScript/checkForm.js'/>" ></script>
    <script type="text/javascript" src="<c:url value='/forums/jsp/javaScript/forums.js'/>" ></script>
    <script type="text/javascript" src="<c:url value='/forums/jsp/javaScript/viewMessage.js'/>" ></script>
    <script type="text/javascript">
      var wysiwygEditorInstance = null;

      <% if (message == null) { %>
      window.location.href = "Main";
      <% } %>

      function init() {
        parentMessageId = <%=currentMessageId%>;
      }

      function validateMessage() {
        if ($("#messageTitle").val() === "") {
          alert('<%=resource.getString("emptyMessageTitle")%>');
        } else if (!isTextFilled()) {
          alert('<%=resource.getString("emptyMessageText")%>');
        } else {
          document.forumsForm.submit();
        }
      }

        function deleteMessage(messageId, parentId, scroll) {
            if (confirm('<%=resource.getString("confirmDeleteMessage")%>')) {
              window.location.href = (parentId == 0 ? "viewForum.jsp" : "viewMessage.jsp")
                    + "?action=9"  + "&params=" + messageId  + "&forumId=<%=reqForum%>" + "&scroll=" + (scroll && parentId != 0);
            }
        }

        function initCKeditor() {
          if (wysiwygEditorInstance == null) {
            wysiwygEditorInstance = <view:wysiwyg replace="messageText" language="<%=fsc.getLanguage()%>" width="600" height="300" toolbar="forums"/>;
          }
        }

        function callResizeFrame() {
            <%addJsResizeFrameCall(out, fsc);%>
        }

        function loadNotation() {
            if (document.getElementById(NOTATION_PREFIX + "1") == undefined) {
                setTimeout("loadNotation()", 200);
            }
            else {
                var img;
                var i;
                for (i = 1; i <= NOTATIONS_COUNT; i++) {
                    notationFlags[i - 1] = false;
                    img = document.getElementById(NOTATION_PREFIX + i);
                    img.alt = "<%=resource.getString("forums.giveNote")%> " + i + "/" + NOTATIONS_COUNT;
                    img.title = "<%=resource.getString("forums.giveNote")%> " + i + "/" + NOTATIONS_COUNT;
                    if (!readOnly) {
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

        function editMessage(messageId)
        {
            window.location.href = "modifyMessage.jsp?params=" + messageId;
        }

        function valideMessage(messageId)
        {
          if (confirm('<%=resource.getString("confirmValideMessage")%>'))
            {
            window.location.href = "ValidateMessage?params=" + messageId;
            }
        }

        function refuseMessage(messageId)
        {
            window.location.href = "refuseMessage.jsp?params=" + messageId;
        }

    </script>
</head>
<body id="forum<%=forumId%>" class="forum" <%addBodyOnload(out, fsc);%>>
<%

        Window window = graphicFactory.getWindow();
        Frame frame=graphicFactory.getFrame();

        BrowseBar browseBar = window.getBrowseBar();
        browseBar.setDomainName(fsc.getSpaceLabel());
        browseBar.setComponentName(fsc.getComponentLabel(), ActionUrl.getUrl("main"));
        browseBar.setPath(ForumListHelper.navigationBar(reqForum, resource, fsc));

        out.println(window.printBefore());
        out.println(frame.printBefore());

        int previousMessageId = -1;
        int nextMessageId = -1;
        if (!displayAllMessages && messagesCount > 1)
        {
            int i = 0;
            while (i < messagesCount && previousMessageId == -1 && nextMessageId == -1)
            {
                if (messages[i].getId() == messageId)
                {
                    if (i > 0)
                    {
                        previousMessageId = messages[i - 1].getId();
                    }
                    if (i < (messagesCount - 1))
                    {
                    nextMessageId = messages[i + 1].getId();
                    }
                }
                i++;
            }
        }

        // Liste des messages
        String formAction = (reqForum > 0
            ? ActionUrl.getUrl("viewMessage", 8, forumId) : ActionUrl.getUrl("main", 8, -1));
%>
            <div class="notationLine">
                <%
        forumNotes = ForumHelper.displayMessageNotation(out, resources, currentMessageId, fsc, isReader);
              %></div>
            <%
                ForumHelper.displaySingleMessageList(out, resource, userId, isAdmin, isModerator, isReader, false, folderId, messageId, true, "viewForum", fsc, resources);
                %>
           <%

        if (messagesCount > 1) {
%>
        <div class="sousNavBulle">
		<p>
		<%
            if (!displayAllMessages) {
                if (previousMessageId != -1) { %>
                    <a href="<%=ActionUrl.getUrl("viewMessage", "viewForum", 1, previousMessageId, forumId)%>"><%=resource.getString("forums.previous")%></a>
                <% } else { %>
                    <%=resource.getString("forums.previous")%>
                <% } %>
                <% if (nextMessageId != -1) { %>
                    <a href="<%=ActionUrl.getUrl("viewMessage", "viewForum", 1, nextMessageId, forumId)%>"><%=resource.getString("forums.next")%></a>
                <% } else { %>
                    <%=resource.getString("forums.next")%>
                <% } %>
            <% } %>
            <a href="<%=ActionUrl.getUrl("viewMessage", (StringUtil.isDefined(call)? call : "viewForum"), 1, messageId, forumId, false, true)%>"><%=resource.getString(displayAllMessages ? "forums.displayCurrentMessage" : "forums.displayAllMessages")%></a>
            </p>
            </div>
        <% } %>

       	<%


        if (!displayAllMessages)
        {
            messages = new Message[] {message};
        }

        Map<String, Integer> authorNbMessages = new HashMap<String, Integer>();
        int nbMessages;
        for (int i = 0, n = messages.length; i < n; i++)  {
            Message currentMessage = messages[i];
            int currentId = currentMessage.getId();
            int parentId = currentMessage.getParentId();
            String authorId = currentMessage.getAuthor();
            String authorLabel = fsc.getAuthorName(authorId);
            String status = currentMessage.getStatus();
            if (authorLabel == null) {
                authorLabel = resource.getString("inconnu");
            }
            com.stratelia.webactiv.beans.admin.UserDetail author = fsc.getAuthor(authorId);
            String avatar = "/directory/jsp/icons/avatar.png";
            if(author != null) {
               avatar = author.getAvatar();
            }
            String text = currentMessage.getText();
            boolean isSubscriber = fsc.isSubscriber(currentId, userId);
            if (!authorNbMessages.containsKey(authorId)) {
              nbMessages = fsc.getAuthorNbMessages(authorId);
              authorNbMessages.put(authorId, nbMessages);
            }
            else {
              nbMessages = authorNbMessages.get(authorId);
            }
%>

                    <div id="msgContent<%=currentId%>" class="contourintfdcolor">
                        <a name="msg<%=currentId%>"/>
                          <div id="author<%=i%>" class="user">
                            <div class="profilPhoto"><img src="<%=context + avatar%>" alt="<%=authorLabel%>" /></div>
                            <div class="info">
                              <ul>
                                <li class="userName"><%=authorLabel%></li>
                                <li class="nbMessage"><%=nbMessages%></li>
                              </ul>
                            </div>
                          </div>
                              <div class="message">
                                <div class="messageHeader">
                                  <span class="txtnav"><%=currentMessage.getTitle()%></span>&nbsp;<span class="txtnote"><%=convertDate(currentMessage.getDate(), resources)%></span>
                                      <% if (displayAllMessages) { %>
                                          <a href="javascript:scrollTop()"><img src="<%=context%>/util/icons/arrow/arrowUp.gif" align="middle" border="0"/></a>
                                      <% } %>
                                </div>
                                    <div class="messageContent">
                                      <div class="messageAttachment">
                                        <%
                                      	out.flush();
                                        String profile = "user";
                                        if (userId.equals(authorId) || isAdmin || isModerator) {
                                          profile = "admin";
                                        }
                                        pageContext.setAttribute("profile", profile);
                                        pageContext.setAttribute("baseCallbackUrl", "/Rforums/"+ instanceId +"/viewMessage.jsp");
                                        %>
                                        <c:url var="callBackUrl" value="${baseCallbackUrl}" context="/">
                                          <c:param name="call" value="${'viewForum'}" />
                                          <c:param name="action" value="1" />
                                          <c:param name="addStat" value="true" />
                                          <c:param name="params"><%=currentMessage.getId()%></c:param>
                                          <c:param name="forumId"><%=currentMessage.getForumId()%></c:param>
                                        </c:url>
                                        <c:import url="/attachment/jsp/displayAttachedFiles.jsp">
                                          <c:param name="Id"><%=currentMessage.getId()%></c:param>
                                          <c:param name="Profile" value="${profile}" />
                                          <c:param name="ComponentId"><%=instanceId%></c:param>
                                          <c:param name="Context" value="${'attachment'}" />
                                          <c:param name="addFileMenu" value="${'true'}" />
                                          <c:param name="dnd" value="${'false'}" />
                                          <c:param name="notI18n" value="${'true'}" />
                                          <c:param name="CallbackUrl" value="${callBackUrl}" />
                                        </c:import>
                                      </div>
                                      <%=text%>
                                    </div>
                                  <div class="messageFooter">
                                        <input name="checkbox" type="checkbox" <%if (isSubscriber) {%>checked<%}%>
                                                onclick="javascript:window.location.href='viewMessage.jsp?action=<%=(isSubscriber ? 13 : 14)%>&params=<%=currentId%>&forumId=<%=forumId%>'"/>
                                                <span class="texteLabelForm"><%=resource.getString("subscribeMessage")%></span>
                                             <% if (forumActive) { %>
                                              <div class="messageActions">
                                              <% if ((isAdmin || isUser) && STATUS_VALIDATE.equals(status)) { %>
                                                  <a href="javascript:replyMessage(<%=currentId%>)"><img src="<%=context%>/util/icons/reply.gif" align="middle" border="0" alt="<%=resource.getString("replyMessage")%>" title="<%=resource.getString("replyMessage")%>"/></a>&nbsp;
                                              <%  }
                                                if (userId.equals(authorId) || isAdmin || isModerator) {
                                                  if (isModerator && STATUS_FOR_VALIDATION.equals(status)) {
                                            %>
                                                    <a href="javascript:valideMessage(<%=currentId%>)"><img
                                                        src="<%=context%>/util/icons/ok.gif" align="middle" border="0" alt="<%=resource.getString("valideMessage")%>" title="<%=resource.getString("valideMessage")%>"/></a>&nbsp;
                                                    <a href="javascript:refuseMessage(<%=currentId%>)"><img
                                                      src="<%=context%>/util/icons/wrong.gif" align="middle" border="0" alt="<%=resource.getString("refuseMessage")%>" title="<%=resource.getString("refuseMessage")%>"/></a>&nbsp;
                                            <% } %>
                                               <a href="javascript:editMessage(<%=currentId%>)"><img src="<%=context%>/util/icons/update.gif" align="middle" border="0" alt="<%=resource.getString("editMessage")%>" title="<%=resource.getString("editMessage")%>"/></a>&nbsp;
                                               <a href="javascript:deleteMessage(<%=currentId%>, <%=parentId%>, true)"><img src="<%=context%>/util/icons/delete.gif" align="middle" border="0" alt="<%=resource.getString("deleteMessage")%>" title="<%=resource.getString("deleteMessage")%>"/></a>&nbsp;
                                            <% } %>
                                              </div>
                                           <% } %>
                                        </div>
                              </div>
                              <br clear="all"/>
                    </div><%

        }

        if (forumActive) {
%>
                    <div id="responseTable">
                    	<form name="forumsForm" action="<%=formAction%>" method="post">
			              <input type="hidden" name="type" value="sendNotif" />
			              <input type="hidden" name="forumId" value="<%=message.getForumId()%>"/>
                          <input type="hidden" name="parentId" value=""/>

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
								<div class="field" id="messageSubscriptionArea">
									<label for="subscribeMessage" class="txtlibform"><fmt:message key='subscribeMessage'/></label>
									<div class="champs">
										<input type="checkbox" id="subscribeMessage" name="subscribeMessage"/>
									</div>
								</div>
							</div>
						  </fieldset>

						  <view:fileUpload fieldset="true" jqueryFormSelector="form[name='forumsForm']" />

                        </form>
                        <br/>
<%
            ButtonPane msgButtonPane = graphicFactory.getButtonPane();
            msgButtonPane.addButton(graphicFactory.getFormButton(
                resource.getString("valider"), "javascript:validateMessage();", false));
            msgButtonPane.addButton(graphicFactory.getFormButton(
                resource.getString("annuler"), "javascript:cancelMessage();", false));
            msgButtonPane.setHorizontalPosition();
            out.println(msgButtonPane.print());
%>
                    </div><%
        }
%>
    <br />
    <div id="backButton">
<%
        ButtonPane backButtonPane = graphicFactory.getButtonPane();
        backButtonPane.addButton(graphicFactory.getFormButton("Retour", backURL, false));
        backButtonPane.setHorizontalPosition();
        out.println(backButtonPane.print());
%>
    </div>
<%
        out.println(frame.printAfter());
        out.println(window.printAfter());
    }
%>
<% if (!isReader && forumNotes.length > 0) { %>
<form name="notationForm" action="viewMessage" method="post">
  <input name="call" type="hidden" value="viewForum"/>
  <input name="action" type="hidden" value="15"/>
  <input name="forumId" type="hidden" value="<%=forumId%>"/>
  <input name="params" type="hidden" value="<%=currentMessageId%>"/>
  <input name="note" type="hidden" value=""/>
</form>
<script type="text/javascript">
  readOnly = <%=isReader%>;
  currentNote = <%=forumNotes[0]%>;
  userNote = <%=forumNotes[1]%>;
  loadNotation();
</script>
<% } %>
<script type="text/javascript">
  jQuery(document).ready(function() {
    init();
    scrollMessageList(<%=messageId%>);
    <% if (displayAllMessages && scrollToMessage) {%>
    scrollMessage(<%=messageId%>);
    <% } %>
  });
</script>
</body>
</html>
