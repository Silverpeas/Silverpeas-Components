<%--

    Copyright (C) 2000 - 2022 Silverpeas

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

<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<%@ page import="org.silverpeas.core.util.MultiSilverpeasBundle"%>
<%@ page import="org.silverpeas.core.util.ResourceLocator"%>
<%@ page import="org.silverpeas.components.forums.control.ForumsSessionController"%>
<%@ page import="org.silverpeas.core.util.LocalizationBundle" %>
<%@ page import="org.silverpeas.components.forums.control.helpers.ForumListHelper" %>
<%@ page import="org.silverpeas.components.forums.control.helpers.ForumActionHelper" %>
<%@ page import="org.silverpeas.components.forums.control.helpers.ForumHelper" %>
<%
  ForumsSessionController fsc = (ForumsSessionController) request.getAttribute(
      "forumsSessionClientController");
  MultiSilverpeasBundle resources = (MultiSilverpeasBundle)request.getAttribute("resources");
  if (fsc == null) {
    // No forums session controller in the request -> security exception
    String sessionTimeout = ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout");
    getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout)
        .forward(request, response);
    return;
  }
  LocalizationBundle resource = ResourceLocator.getLocalizationBundle(
      "org.silverpeas.forums.multilang.forumsBundle", fsc.getLanguage());
  String userId = fsc.getUserId();
  boolean isAdmin = fsc.isAdmin();
  boolean isUser = fsc.isUser();
  boolean isReader = fsc.isReader();
  fsc.resetDisplayAllMessages();
  int forumId = ForumHelper.getIntParameter(request, "forumId", 0);

  boolean isModerator = fsc.isModerator(userId, forumId);
  ForumActionHelper.actionManagement(request, isAdmin, isModerator, userId, resource, out, fsc);
  boolean isForumSubscriberByInheritance =
      (Boolean) request.getAttribute("isForumSubscriberByInheritance");
%>
<c:set var="isModerator" value="<%=isModerator%>" />
<c:set var="currentForum" value="${requestScope.currentForum}" />
<c:set var="currentForumRaterRatingEntity" value="${requestScope.currentForumRaterRatingEntity}" />
<c:set var="isActive"  value="${requestScope.currentForum.active}" />
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="silverpeas.forums" xml:lang="${sessionScope[sessionController].language}">
<head>
  <title><c:out value="${currentForum.name}" /></title>
  <meta http-equiv="X-UA-Compatible" content="IE=edge" />
  <view:looknfeel />
  <view:includePlugin name="rating" />
  <link type="text/css" rel="stylesheet" href="<c:url value="/forums/jsp/styleSheets/forums.css"/>"/>
  <script type="text/javascript" src="<c:url value="/forums/jsp/javaScript/forums.js" />" ></script>
  <script type="text/javascript">
    <% if (isAdmin || isUser || isModerator) { %>
    <fmt:message key="confirmDeleteForum" var="confirmationSuppressionForum" />
    <fmt:message key="confirmDeleteMessage" var="confirmationSuppressionMessage" />
    function confirmDeleteForum(forumId) {
      jQuery.popup.confirm("<view:encodeJs string="${confirmationSuppressionForum}"/>", function() {
        document.forms['forumForm'].elements['action'].value = 4;
        document.forms['forumForm'].elements['params'].value = forumId;
        document.forms['forumForm'].elements['forumId'].value=<%=forumId%>;
        document.forms['forumForm'].submit();
      });
    }
    function deleteMessage(messageId, parentId, scroll) {
      jQuery.popup.confirm("<view:encodeJs string="${confirmationSuppressionMessage}"/>", function() {
        document.forms['forumForm'].elements['action'].value = 9;
        document.forms['forumForm'].elements['params'].value = messageId;
        document.forms['forumForm'].elements['forumId'].value=<%=forumId%>;
        document.forms['forumForm'].submit();
      });
    }
    <% } %>

    function subscribeOneForum(forumId, parentForumId) {
      document.forms['forumForm'].elements['action'].value = 18;
      document.forms['forumForm'].elements['params'].value = forumId;
      document.forms['forumForm'].elements['forumId'].value=parentForumId;
      document.forms['forumForm'].submit();
    }

    function unsubscribeOneForum(forumId, parentForumId) {
      document.forms['forumForm'].elements['action'].value = 17;
      document.forms['forumForm'].elements['params'].value = forumId;
      document.forms['forumForm'].elements['forumId'].value=parentForumId;
      document.forms['forumForm'].submit();
    }
  </script>
</head>
<body id="forum" <%ForumHelper.addBodyOnload(out, fsc);%>>
<c:set var="viewForumPage">/Rforums/<c:out value="${componentId}" />/viewForum.jsp</c:set>
<view:browseBar>
  <c:forEach items="${requestScope.parents}" var="ancestor">
    <c:if test="${not empty ancestor.id}">
      <c:url var="ancestorLink" value="${viewForumPage}"><c:param name="call" value="viewForum"/><c:param name="forumId" value="${ancestor.id}"/></c:url>
      <view:browseBarElt id="${ancestor.id}" label="${ancestor.name}" link="${ancestorLink}" />
    </c:if>
  </c:forEach>
  <c:url var="forumLink" value="${viewForumPage}"><c:param name="call" value="viewForum"/><c:param name="forumId" value="${param.forumId}"/></c:url>
  <view:browseBarElt id="${currentForum.id}" label="${currentForum.name}" link="${forumLink}" />
</view:browseBar>
<c:if test="${not isAccessGuest and (isAdmin || isUser)}">
  <view:operationPane>
    <c:if test="${isAdmin && sessionController.pdcUsed}">
      <fmt:message key="PDCUtilization" var="pdcUtilisation" />
      <c:set var="pdcUtilisationOperation">javascript:onClick=openSPWindow('<c:url value="/RpdcUtilization/jsp/Main" >
        <c:param name="ComponentId" value="${sessionController.componentId}" /></c:url>', 'utilizationPdc1');</c:set>
      <c:url var="pdcUtilisationIconUrl" value="/pdcPeas/jsp/icons/pdcPeas_paramPdc.gif" />
      <view:operation altText="${pdcUtilisation}" icon="${pdcUtilisationIconUrl}" action="${pdcUtilisationOperation}" />
    </c:if>
    <fmt:message key="mailAdmin" var="mail2AdminAltText" />
    <c:set var="mail2AdminOperation">javascript:notifyForumPopup('<c:out value="${sessionController.adminIds}" />');</c:set>
    <c:url var="mail2AdminIconUrl" value="/util/icons/forums_mailtoAdmin.gif" />
    <view:operation altText="${mail2AdminAltText}" icon="${mail2AdminIconUrl}" action="${mail2AdminOperation}" />
    <c:if test="${isAdmin && sessionController.forumInsideForum}">
      <fmt:message key="newForum" var="addForumAltText" />
      <c:url var="addForumOperation" value="editForumInfo.jsp">
        <c:param name="call" value="viewForum"/>
        <c:param name="action" value="1"/>
        <c:param name="forumId" value="${param.forumId}"/>
        <c:param name="params" value="${param.forumId}"/>
      </c:url>
      <c:url var="addForumIconUrl" value="/util/icons/create-action/add-forum.png" />
      <view:operationOfCreation altText="${addForumAltText}" icon="${addForumIconUrl}" action="${addForumOperation}" />
    </c:if>
    <c:if test="${isActive}">
      <fmt:message key="newMessage" var="newMessageAltText" />
      <c:url var="newMessageIconUrl" value="/util/icons/create-action/add-message.png" />
      <c:set var="newMessagePage">/Rforums/<c:out value="${componentId}" />/editMessage.jsp</c:set>
      <c:url var="newMessageOperation" value="${newMessagePage}"><c:param name="call" value="viewForum"/><c:param name="action" value="1"/><c:param name="forumId" value="${param.forumId}"/><c:param name="params" value="${param.forumId}"/></c:url>
      <view:operationOfCreation altText="${newMessageAltText}" icon="${newMessageIconUrl}" action="${newMessageOperation}" />
    </c:if>
  </view:operationPane>
</c:if>
<view:window>
  <view:frame>
    <view:areaOfOperationOfCreation/>
    <table class="intfdcolor4 tabContainer" border="0">
      <caption></caption>
      <th scope="col"></th>
      <c:if test="${requestScope.nbChildrens > 0}">
        <tr>
          <td>
            <table border="0" class="testTableau">
              <caption></caption>
              <tr class="ArrayColumn">
                <th scope="col"><fmt:message key="theme" /></th>
                <th scope="col"><fmt:message key="forums.nbSubjects" /></th>
                <th scope="col"><fmt:message key="forums.nbMessages" /></th>
                <th scope="col"><fmt:message key="forums.lastMessage" /></th>
                <th scope="col"><fmt:message key="forums.notation" /></th>
                <th scope="col"><fmt:message key="subscribeMessage" /></th>
                <c:if test="${isAdmin || isModerator}">
                  <th scope="col"><fmt:message key="operations" /></th>
                </c:if>
              </tr>
              <%ForumListHelper.displayChildForums(out, resources, isAdmin, isModerator, isReader, forumId, "main", fsc, isForumSubscriberByInheritance);%>
            </table>
          </td>
        </tr>
        <tr>
          <td><br/><br/></td>
        </tr>
      </c:if>
      <tr class="notationLine">
        <td>
          <viewTags:displayContributionRating readOnly="${not(isAdmin or isUser)}" canUserRating="${isAdmin or isUser}" raterRating="${currentForumRaterRatingEntity}"/>
        </td>
      </tr>
      <tr>
        <td>
          <form name="nameForm" action="" method="post">
            <table border="0" class="testTableau">
              <caption></caption>
                <%-- affichage de l'entete des colonnes --%>
              <caption></caption>
              <tr class="ArrayColumn">
                <th scope="col" colspan="3"><fmt:message key="forums.nbSubjects"/></th>
                <th scope="col"><fmt:message key="forums.lastMessage"/></th>
                <th scope="col"><fmt:message key="forums.nbMessages"/></th>
                <th scope="col"><fmt:message key="forums.nbViews"/></th>
                <th scope="col"><fmt:message key="forums.notation"/></th>
                <c:if test="${isAdmin || isModerator}">
                  <th scope="col"><fmt:message key="operations"/></th>
                </c:if>
              </tr><%
              fsc.deployAllMessages(forumId);
              ForumHelper.PrintOutParameters params = new ForumHelper.PrintOutParameters()
                  .setWriter(out)
                  .setTranslations(resource)
                  .setForumId(forumId)
                  .setForumView(true)
                  .setSimpleMode(false)
                  .setCall("viewForum")
                  .setSessionController(fsc)
                  .setResources(resources);
              ForumHelper.RoleMask roleMask = new ForumHelper.RoleMask()
                  .setUserId(userId)
                  .setAdmin(isAdmin)
                  .setModerator(isModerator)
                  .setReader(isReader);
              ForumHelper.displayMessagesList(params, roleMask, isForumSubscriberByInheritance);
            %>
            </table>
          </form>
        </td>
      </tr>
    </table>
    <c:if test="${sessionController.external || ! isReader}">
      <img src="icons/noNewMessage.gif" alt="<fmt:message key="forums.notNewMessageVisite" />" /> <fmt:message key="forums.notNewMessageVisite" />
      <br />
      <img src="icons/newMessage.gif" alt="<fmt:message key="forums.newMessageVisite" />" /> <fmt:message key="forums.newMessageVisite" />
    </c:if>
    <br/>
    <div style="text-align: center;">
      <fmt:message key="accueil" var="btnLabel"/>
      <view:buttonPane>
        <view:button action="main.jsp" label="${btnLabel}" disabled="false" />
      </view:buttonPane>
    </div>
  </view:frame>
</view:window>
<form id="forumForm" name="forumForm" action="viewForum.jsp" method="post">
  <input id="action" name="action" type="hidden"/>
  <input id="params" name="params" type="hidden"/>
  <input id="forumId" name="forumId" type="hidden"/>
</form>
<script type="text/javascript">
  /* declare the module myapp and its dependencies (here in the silverpeas module) */
  var myapp = angular.module('silverpeas.forums', ['silverpeas.services', 'silverpeas.directives']);
</script>
</body>
</html>
