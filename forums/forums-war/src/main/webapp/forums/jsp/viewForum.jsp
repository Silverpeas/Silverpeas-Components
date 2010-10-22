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

<%@ page import="java.io.IOException"%>
<%@page import="com.silverpeas.util.EncodeHelper"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodeDetail"%>
<%@ page import="com.stratelia.webactiv.forums.sessionController.helpers.*"%>
<%@ page import="com.stratelia.webactiv.forums.sessionController.ForumsSessionController"%>
<%@ page import="com.stratelia.webactiv.forums.models.Forum"%>
<%@ page import="com.stratelia.webactiv.forums.models.Message"%>
<%
    ForumsSessionController fsc = (ForumsSessionController) request.getAttribute(
        "forumsSessionClientController");
    ResourcesWrapper resources = (ResourcesWrapper)request.getAttribute("resources");
    ResourceLocator resource = new ResourceLocator(
        "com.stratelia.webactiv.forums.multilang.forumsBundle", fsc.getLanguage());
    if (fsc == null) {
        // No forums session controller in the request -> security exception
        String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator()
            .getString("sessionTimeout");
        getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout)
            .forward(request, response);
        return;
    }
    String userId = fsc.getUserId();
    boolean isAdmin = fsc.isAdmin();
    boolean isUser = fsc.isUser();
    boolean isReader = fsc.isReader();
    fsc.resetDisplayAllMessages();
    int forumId = ForumHelper.getIntParameter(request, "forumId", 0);

    boolean isModerator = fsc.isModerator(userId, forumId);
    ForumActionHelper.actionManagement(request, isAdmin, isModerator, userId, resource, out, fsc);
%>
<c:set var="currentForum" value="${requestScope.currentForum}" />
<c:set var="isActive"  value="${requestScope.currentForum.active}" />
<c:set var="globalNote" value="${requestScope.notation.roundGlobalNote}" />
<c:set var="userNote" value="${requestScope.notation.userNote}" />
<html>
  <head>
    <title><c:out value="${currentForum.name}" /></title>
    <view:looknfeel />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <c:if test="${sessionScope.SessionGraphicElementFactory.externalStylesheet == null}">
      <link rel="stylesheet" type="text/css" href="styleSheets/forums.css">
    </c:if>
    <script type="text/javascript" src="<c:url value="/forums/jsp/javaScript/forums.js" />" ></script>
    <script type="text/javascript" src="<c:url value="/util/javaScript/animation.js" />" ></script>
    <script type="text/javascript">
      <c:if test="${isAdmin || isUser || isModerator}">
        function confirmDeleteForum(forumId) {
          if (confirm("<%=EncodeHelper.javaStringToJsString(resource.getString("confirmDeleteForum"))%>"))
          {
            window.location.href = "viewForum.jsp?action=4&params=" + forumId + "&forumId=<%=forumId%>";
          }
        }
        function deleteMessage(messageId, parentId, scroll) {
          if (confirm("<%=EncodeHelper.javaStringToJsString(resource.getString("confirmDeleteMessage"))%>"))
          {
            window.location.href = "viewForum.jsp?action=9&params=" + messageId + "&forumId=<%=forumId%>";
          }
        }
      </c:if>

        function loadNotation()
        {
          if (document.getElementById(NOTATION_PREFIX + "1") == undefined)  {
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
    </script>
  </head>
  <body id="forum" <%ForumHelper.addBodyOnload(out, fsc);%>>
    <c:set var="viewForumPage">/Rforums/<c:out value="${componentId}" />/viewForum.jsp</c:set>
    <view:browseBar>
      <c:forEach items="${requestScope.parents}" var="ancestor">
        <c:url var="ancestorLink" value="${viewForumPage}"><c:param name="call" value="viewForum"/><c:param name="forumId" value="${ancestor.id}"/></c:url>
        <view:browseBarElt id="${ancestor.id}" label="${ancestor.name}" link="${ancestorLink}" />
      </c:forEach>
      <c:url var="forumLink" value="${viewForumPage}"><c:param name="call" value="viewForum"/><c:param name="forumId" value="${param.forumId}"/></c:url>
      <view:browseBarElt id="${currentForum.id}" label="${currentForum.name}" link="${forumLink}" />
    </view:browseBar>
    <c:if test="${isAdmin || isUser}">
      <view:operationPane>
        <c:if test="${isAdmin && sessionController.pdcUsed}">
          <fmt:message key="PDCUtilization" var="pdcUtilisation" />
          <c:set var="pdcUtilisationOperation">javascript:onClick=openSPWindow('<c:url value="/RpdcUtilization/jsp/Main" >
              <c:param name="ComponentId" value="${sessionController.componentId}" /></c:url>', 'utilizationPdc1');</c:set>
          <c:url var="pdcUtilisationIconUrl" value="/pdcPeas/jsp/icons/pdcPeas_paramPdc.gif" />
          <view:operation altText="${pdcUtilisation}" icon="${pdcUtilisationIconUrl}" action="${pdcUtilisationOperation}" />
        </c:if>
        <fmt:message key="mailAdmin" var="mail2AdminAltText" />
        <c:set var="mail2AdminOperation">javascript:notifyPopup2('<c:out value="${pageContext.request.contextPath}"/>', '<c:out value="${sessionController.componentId}" />','<c:out value="${sessionController.adminIds}" />', '');</c:set>
        <c:url var="mail2AdminIconUrl" value="/util/icons/forums_mailtoAdmin.gif" />
        <view:operation altText="${mail2AdminAltText}" icon="${mail2AdminIconUrl}" action="${mail2AdminOperation}" />
        <c:if test="${isAdmin}">
          <fmt:message key="newForum" var="addForumAltText" />
          <c:url var="addForumOperation" value="editForumInfo.jsp">
            <c:param name="call" value="viewForum"/>
            <c:param name="action" value="1"/>
            <c:param name="forumId" value="${param.forumId}"/>
            <c:param name="params" value="${param.forumId}"/>
          </c:url>
          <c:url var="addForumIconUrl" value="/util/icons/forums_to_add.gif" />
          <view:operation altText="${addForumAltText}" icon="${addForumIconUrl}" action="${addForumOperation}" />
          <fmt:message key="forums.addCategory" var="addCategoryAltText" />
          <c:set var="addCategoryOperation">javascript:notifyPopup2('<c:out value="${pageContext.request.contextPath}"/>', '<c:out value="${sessionController.componentId}" />','<c:out value="${sessionController.adminIds}" />', '');</c:set>
          <c:url var="addCategoryIconUrl" value="/util/icons/folderAddBig.gif" />
          <view:operation altText="${addCategoryAltText}" icon="${addCategoryIconUrl}" action="NewCategory" />
        </c:if>
        <c:if test="${isActive}">
          <fmt:message key="newMessage" var="newMessageAltText" />
          <c:url var="newMessageIconUrl" value="/util/icons/forums_addMessage.gif" />
          <c:set var="newMessagePage">/Rforums/<c:out value="${componentId}" />/editMessage.jsp</c:set>
          <c:url var="newMessageOperation" value="${newMessagePage}"><c:param name="call" value="viewForum"/><c:param name="action" value="1"/><c:param name="forumId" value="${param.forumId}"/><c:param name="params" value="${param.forumId}"/></c:url>
          <view:operation altText="${newMessageAltText}" icon="${newMessageIconUrl}" action="${newMessageOperation}" />
        </c:if>
      </view:operationPane>
    </c:if>
    <view:window>
      <view:frame>
        <table class="intfdcolor4" border="0" cellspacing="0" cellpadding="0" width="98%">
          <tr class="notationLine">
            <td align="right">
              <c:url var="starIcon" value="/util/icons/shim.gif"/>
              <span class="txtnote"><fmt:message key="forums.forumNote"/> :
                <c:forEach var="i" begin="1" end="5">
                  <img src="<c:out value="${starIcon}"/>" style="margin-bottom: 0px;" <c:if test="${!isReader}">id="notationImg<c:out value="${i}"/>"</c:if>
                       class="<c:choose><c:when test="${i <= globalNote}">notation_on</c:when><c:otherwise>notation_off</c:otherwise></c:choose>" />
                </c:forEach>
              </span>
            </td>
          </tr>
          <tr>
            <td valign="top">
              <table class="contourintfdcolor" border="0" cellspacing="0" cellpadding="5" width="100%">
                <form name="nameForm" action="" method="post">
                  <tr>
                    <td valign="top" align="center">
                      <table width="100%" border="0" align="center" cellpadding="4" cellspacing="1" class="testTableau">
                        <%-- affichage de l'ent�te des colonnes --%>
                        <tr class="enteteTableau">
                          <td nowrap="nowrap" align="center" colspan="3"><fmt:message key="forums.nbSubjects"/></td>
                          <td nowrap="nowrap" align="center"><fmt:message key="forums.lastMessage"/></td>
                          <td nowrap="nowrap" align="center"><<fmt:message key="forums.nbMessages"/></td>
                          <td nowrap="nowrap" align="center"><fmt:message key="forums.nbViews"/></td>
                          <td nowrap="nowrap" align="center"><fmt:message key="forums.notation"/></td>
                          <c:if test="${isAdmin || isModerator}">
                            <td nowrap="nowrap" align="center"><fmt:message key="operations"/></td>
                          </c:if>
                        </tr><%
fsc.deployAllMessages(forumId);
ForumHelper.displayMessagesList(out, resource, userId, isAdmin, isModerator, isReader, true, forumId, false,
"viewForum", fsc, resources);
                        %>
                      </table>
                    </td>
                  </tr>
                </form>
              </table>
            </td>
          </tr>
        </table>
        <br/>
        <center>
          <view:buttonPane>
            <view:button action="main.jsp" label="Retour" disabled="false" />
          </view:buttonPane>
        </center>
      </view:frame>
    </view:window>
    <c:if test="${!isReader}">
      <form name="notationForm" action="viewForum" method="post">
        <input name="action" type="hidden" value="16"/>
        <input name="forumId" type="hidden" value="<c:out value="${param.forumId}" />"/>
        <input name="note" type="hidden" value=""/>
      </form>
      <script type="text/javascript">
        readOnly = <c:out value="${isReader}"/>;
        currentNote = <c:out value="${globalNote}"/>;
        userNote = <c:out value="${userNote}"/>;
        loadNotation();
      </script>
    </c:if>
  </body>
</html>
