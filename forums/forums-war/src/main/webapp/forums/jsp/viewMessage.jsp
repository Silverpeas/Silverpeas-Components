<%--

    Copyright (C) 2000 - 2024 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page import="org.silverpeas.components.forums.control.helpers.ForumHelper"%>
<%@page import="org.silverpeas.components.forums.control.helpers.ForumListHelper"%>
<%
    response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
    response.setHeader("Pragma", "no-cache"); //HTTP 1.0
    response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>
<c:set var="sessionController" value="${requestScope.forumsSessionClientController}" />
<c:set var="componentId" value="${sessionController.componentId}" />
<c:set var="isReader" value="${sessionController.reader}" />
<c:set var="isUser" value="${sessionController.user}" />
<c:set var="isAdmin" value="${sessionController.admin}" />
<c:set var="isAccessGuest" value="${sessionController.userDetail.accessGuest}" />
<jsp:useBean id="isAccessGuest" type="java.lang.Boolean"/>

<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
<%@ page import="org.silverpeas.core.io.upload.FileUploadManager"%>
<%@ page import="org.silverpeas.core.io.upload.UploadedFile"%>
<%@ page import="org.silverpeas.core.notification.message.MessageNotifier"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="java.util.Map" %>
<%@ page import="org.silverpeas.core.webapi.rating.RaterRatingEntity" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window" %>
<%@ page import="org.silverpeas.core.admin.user.model.User" %>
<%@ page import="org.silverpeas.core.web.http.HttpRequest" %>
<%@ page import="org.silverpeas.core.admin.user.model.SilverpeasRole" %>
<%@ include file="checkForums.jsp"%>
<%
    int messageId = 0;

    int forumId = getIntParameter(request, "forumId");
    String call = request.getParameter("call");
    int action = getIntParameter(request, "action", 1);
    int params = getIntParameter(request, "params");
    int currentMessageId = -1;
    boolean scrollToMessage = false;
    boolean displayAllMessages = true;

    try {
        Message message;
        String bundleKey;
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
                if ("true".equals(request.getParameter("scroll"))) {
                    scrollToMessage = true;
                }
                break;

            case 8 :
                int parentId = getIntParameter(request, "parentId", 0);
                String messageTitle = request.getParameter("messageTitle").trim();
                String messageText = request.getParameter("messageText").trim();
                String subscribe = request.getParameter("subscribeMessage");

                if ((messageTitle.length() > 0) && (messageText.length() > 0)) {
                    if (params == -1) {
                      int result =
                          fsc.createMessage(messageTitle, userId, forumId, parentId,
                              messageText, null, ((HttpRequest)request).getUploadedFiles());
                      messageId = result;
                    } else {
                        // Modification
                        messageId = params;
                        fsc.updateMessage(messageId, messageTitle, messageText);
                    }
                    if (subscribe == null) {
                        subscribe = "0";
                    } else {
                        subscribe = "1";
                        if (messageId != 0) {
                            fsc.subscribeMessage(messageId);
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
                message = fsc.unsubscribeMessage(params);
                messageId = params;
                bundleKey = message.isSubject() ? "forums.subject.unsubscribe.success" :
                    "forums.message.unsubscribe.success";
                MessageNotifier
                  .addSuccess(resource.getString(bundleKey), message.getTitle());
                break;

            case 14 :
                message = fsc.subscribeMessage(params);
                messageId = params;
                bundleKey = message.isSubject() ? "forums.subject.subscribe.success" :
                    "forums.message.subscribe.success";
                MessageNotifier.addSuccess(resource.getString(bundleKey), message.getTitle());
                break;
        }
    }
    catch (NumberFormatException nfe) {
      SilverLogger.getLogger(this).error(nfe);
    }
    String backURL = ActionUrl.getUrl(call, -1, forumId);

    Message message = fsc.getMessage(messageId);
    if (forumId == -1) {
        forumId = message.getForumId();
    }

    boolean forumActive = false;

    if(message != null) {
        int reqForum = (forumId != -1 ? forumId : 0);
        int folderId = message.getForumId();
        boolean isModerator = fsc.isModerator(userId, folderId);
        pageContext.setAttribute("isModerator", isModerator);
        displayAllMessages = fsc.isDisplayAllMessages();

        forumActive = fsc.isForumActive(folderId);

        String folderName = WebEncodeHelper.javaStringToHtmlString(fsc.getForumName(folderId > 0 ? folderId : params));

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
        boolean isMessageSubscriberByInheritance =
          fsc.isMessageSubscriberByInheritance(currentMessageId);
        boolean isAllMessageSubscriberByInheritance = isMessageSubscriberByInheritance;
%>
<c:set var="message" value="<%=message%>"/>
<c:set var="messageRaterRatingEntity" value="<%=RaterRatingEntity.fromRateable(message)%>"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="silverpeas.forums" xml:lang="${requestScope.resources.language}">
<head>
    <title><c:out value="${pageScope.title}" /></title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <view:looknfeel withFieldsetStyle="true" withCheckFormScript="true"/>
    <view:includePlugin name="wysiwyg"/>
    <view:includePlugin name="popup"/>
    <view:includePlugin name="rating" />
    <script type="text/javascript" src="<c:url value='/forums/jsp/javaScript/forums.js'/>" ></script>
    <script type="text/javascript" src="<c:url value='/forums/jsp/javaScript/viewMessage.js'/>" ></script>
    <script type="text/javascript">
      let wysiwygEditorInstance = null;

      <% if (message == null) { %>
      window.location.href = "Main";
      <% } %>

      function init() {
        parentMessageId = <%=currentMessageId%>;
      }

      function validateMessage() {
        if ($("#messageTitle").val() === "") {
          jQuery.popup.confirm('<%=resource.getString("emptyMessageTitle")%>');
        } else if (!isTextFilled()) {
          jQuery.popup.confirm('<%=resource.getString("emptyMessageText")%>');
        } else {
          sp.editor.wysiwyg.lastBackupManager.clear();
          $(document.forumsForm).submit();
        }
      }

        function deleteMessage(messageId, parentId, scroll) {
            jQuery.popup.confirm('<%=resource.getString("confirmDeleteMessage")%>', function() {
              window.location.href = (parentId == 0 ? "viewForum.jsp" : "viewMessage.jsp")
                    + "?action=9"  + "&params=" + messageId  + "&forumId=<%=reqForum%>" + "&scroll=" + (scroll && parentId != 0);
            });
        }

        function initCKeditor(messageId) {
          return new Promise(function(resolve) {
            if (wysiwygEditorInstance == null) {
              wysiwygEditorInstance = <view:wysiwyg replace="messageText" language="<%=fsc.getLanguage()%>" width="600" height="300" toolbar="forum" displayFileBrowser="${false}"/>;
              wysiwygEditorInstance.on('instanceReady', function() {
                resolve();
              });
            } else {
              resolve();
            }
            sp.editor.wysiwyg.backupManager({
              componentInstanceId : '${componentId}',
              resourceId : messageId
            });
          });
        }

        function callResizeFrame() {
            <%addJsResizeFrameCall(out, fsc);%>
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
        browseBar.setPath(ForumListHelper.navigationBar(reqForum, fsc));

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
              <viewTags:displayContributionRating readOnly="${not(isAdmin or isUser)}" canUserRating="${isAdmin or isUser}" raterRating="${messageRaterRatingEntity}"/>
            </div>
            <%
              ForumHelper.PrintOutParameters viewParams = new ForumHelper.PrintOutParameters()
                  .setWriter(out)
                  .setTranslations(resource)
                  .setForumView(false)
                  .setForumId(folderId)
                  .setMessageId(messageId)
                  .setSimpleMode(true)
                  .setCall("viewForum")
                  .setSessionController(fsc)
                  .setResources(resources);
              ForumHelper.RoleMask roleMask = new ForumHelper.RoleMask()
                  .setUserId(userId)
                  .setAdmin(isAdmin)
                  .setModerator(isModerator)
                  .setReader(isReader);
              ForumHelper
                  .displaySingleMessageList(viewParams, roleMask, isMessageSubscriberByInheritance);
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
        isMessageSubscriberByInheritance = isAllMessageSubscriberByInheritance;
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
            UserDetail author = fsc.getAuthor(authorId);
            String avatar = User.DEFAULT_AVATAR_PATH;
            if(author != null) {
               avatar = author.getAvatar();
            }
            String text = currentMessage.getText();
          boolean isSubscriber = fsc.isMessageSubscriber(currentId);
          if (!isAllMessageSubscriberByInheritance) {
            isMessageSubscriberByInheritance = fsc.isMessageSubscriberByInheritance(currentId);
          }
          if (!authorNbMessages.containsKey(authorId)) {
              nbMessages = fsc.getAuthorNbMessages(authorId);
              authorNbMessages.put(authorId, nbMessages);
            }
            else {
              nbMessages = authorNbMessages.get(authorId);
            }
%>

                    <div id="msgContent<%=currentId%>" class="contourintfdcolor">
                        <a id="msg<%=currentId%>"/>
                          <div id="author<%=i%>" class="user">
                            <div class="profilPhoto"><view:image src="<%=avatar%>" alt="<%=authorLabel%>" type="avatar" /></div>
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
                                          <a href="javascript:scrollTop()"><img alt="" src="<%=context%>/util/icons/arrow/arrowUp.gif" border="0"/></a>
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
                                          <c:param name="scroll" value="true" />
                                          <c:param name="params"><%=currentMessage.getId()%></c:param>
                                          <c:param name="forumId"><%=currentMessage.getForumId()%></c:param>
                                        </c:url>
                                        <viewTags:displayAttachments componentInstanceId="<%=instanceId%>"
                                                                     resourceType="<%=currentMessage.RESOURCE_TYPE%>"
                                                                     resourceId="<%=String.valueOf(currentMessage.getId())%>"
                                                                     highestUserRole="<%=SilverpeasRole.fromString(profile)%>"
                                                                     reloadCallbackUrl="${callBackUrl}"/>
                                      </div>
                                      <div class="rich-content">
                                        <%=text%>
                                      </div>
                                      <viewTags:viewAttachmentsAsContent componentInstanceId="${componentId}"
                                                                         resourceType="<%=currentMessage.RESOURCE_TYPE%>"
                                                                         resourceId="<%=String.valueOf(currentMessage.getId())%>"
                                                                         highestUserRole="<%=SilverpeasRole.fromString(profile)%>"/>
                                    </div>
                                  <div class="messageFooter">
                                        <input name="checkbox" type="checkbox" <%if ((isSubscriber || isMessageSubscriberByInheritance) && !isAccessGuest) {%>checked<%}%>
                                               <%if ((!isSubscriber && isMessageSubscriberByInheritance) || isAccessGuest) {%> disabled <%}%>
                                                onclick="javascript:window.location.href='viewMessage.jsp?action=<%=(isSubscriber ? 13 : 14)%>&params=<%=currentId%>&forumId=<%=forumId%>'"/>
                                                <span class="texteLabelForm"><%=resource.getString("subscribeMessage")%></span>
                                             <% if (forumActive) { %>
                                              <div class="messageActions">
                                              <% if ((isAdmin || isUser) && STATUS_VALIDATE.equals(status)) { %>
                                                  <a href="javascript:replyMessage(<%=currentId%>)"><img src="<%=context%>/util/icons/reply.gif" border="0" alt="<%=resource.getString("replyMessage")%>" title="<%=resource.getString("replyMessage")%>"/></a>&nbsp;
                                              <%  }
                                                if (userId.equals(authorId) || isAdmin || isModerator) {
                                                  if (isModerator && STATUS_FOR_VALIDATION.equals(status)) {
                                            %>
                                                    <a href="javascript:valideMessage(<%=currentId%>)"><img
                                                        src="<%=context%>/util/icons/ok.gif" border="0" alt="<%=resource.getString("valideMessage")%>" title="<%=resource.getString("valideMessage")%>"/></a>&nbsp;
                                                    <a href="javascript:refuseMessage(<%=currentId%>)"><img
                                                      src="<%=context%>/util/icons/wrong.gif" border="0" alt="<%=resource.getString("refuseMessage")%>" title="<%=resource.getString("refuseMessage")%>"/></a>&nbsp;
                                            <% } %>
                                               <a href="javascript:editMessage(<%=currentId%>)"><img src="<%=context%>/util/icons/update.gif" border="0" alt="<%=resource.getString("editMessage")%>" title="<%=resource.getString("editMessage")%>"/></a>&nbsp;
                                               <a href="javascript:deleteMessage(<%=currentId%>, <%=parentId%>, true)"><img src="<%=context%>/util/icons/delete.gif" border="0" alt="<%=resource.getString("deleteMessage")%>" title="<%=resource.getString("deleteMessage")%>"/></a>&nbsp;
                                            <% } %>
                                              </div>
                                           <% } %>
                                        </div>
                              </div>
                              <br />
                    </div>
    <%

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
										<input type="text" id="messageTitle" name="messageTitle" size="88" maxlength="<%=DBUtil.getTextFieldLength()%>"/>&nbsp;<img alt="" src="<%=context%>/util/icons/mandatoryField.gif" width="5" height="5"/>
									</div>
								</div>
								<div class="field" id="messageTextArea">
									<label for="messageText" class="txtlibform"><fmt:message key='messageText'/></label>
									<div class="champs">
										<textarea name="messageText" id="messageText"></textarea>&nbsp;<img alt="" src="<%=context%>/util/icons/mandatoryField.gif" width="5" height="5"/>
									</div>
								</div>
								<div class="field" id="messageSubscriptionArea">
                  <c:if test="${not isAccessGuest}">
                    <label for="subscribeMessage" class="txtlibform"><fmt:message key='subscribeMessage'/></label>
                    <div class="champs">
                      <input type="checkbox" id="subscribeMessage" name="subscribeMessage"/>
                    </div>
                  </c:if>
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
    <div id="backButton" style="text-align: center;">
      <fmt:message key="GML.back" var="btnLabel"/>
      <view:buttonPane>
        <view:button action="<%=backURL%>" label="${btnLabel}" disabled="false" />
      </view:buttonPane>
    </div>
<%
        out.println(frame.printAfter());
        out.println(window.printAfter());
    }
%>
<script type="text/javascript">
  whenSilverpeasReady(function() {
    init();
    <% if (displayAllMessages && scrollToMessage) {%>
    whenSilverpeasEntirelyLoaded(function() {
      const __scroll = function() {
        scrollMessage(<%=messageId%>);
      };
      if (window.AttachmentsAsContentViewer) {
        AttachmentsAsContentViewer.whenAllCurrentAttachmentDisplayed({
          onTimeout : function() {
            spProgressMessage.show();
          },
          callback : function() {
            __scroll();
            spProgressMessage.hide();
          }
        });
      } else {
        __scroll();
      }
    });
    <% } %>
  });
  
  /* declare the module myapp and its dependencies (here in the silverpeas module) */
  var myapp = angular.module('silverpeas.forums', ['silverpeas.services', 'silverpeas.directives']);
</script>
</body>
</html>
