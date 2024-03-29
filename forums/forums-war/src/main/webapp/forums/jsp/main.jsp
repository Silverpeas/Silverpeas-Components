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
<%
    response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
    response.setHeader("Pragma", "no-cache"); //HTTP 1.0
    response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<c:set var="componentId" value="${requestScope.componentId}" />
<c:set var="sessionController" value="${requestScope.forumsSessionClientController}" />
<c:set var="isReader" value="${sessionController.reader}" />
<c:set var="isUser" value="${sessionController.user}" />
<c:set var="isAdmin" value="${sessionController.admin}" />
<c:set var="isAccessGuest" value="${sessionController.userDetail.accessGuest}" />

<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<%@ page import="org.silverpeas.core.util.MultiSilverpeasBundle"%>
<%@ page import="org.silverpeas.components.forums.control.ForumsSessionController"%>
<%@ page import="org.silverpeas.components.forums.control.helpers.ForumActionHelper"%>
<%@ page import="org.silverpeas.components.forums.control.helpers.ForumHelper"%>
<%@ page import="org.silverpeas.components.forums.control.helpers.ForumListHelper"%>
<%@ page import="org.silverpeas.kernel.bundle.ResourceLocator"%>
<%@ page import="org.silverpeas.core.node.model.NodeDetail" %>
<%@ page import="org.silverpeas.kernel.bundle.LocalizationBundle" %>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%
ForumsSessionController fsc = (ForumsSessionController) request.getAttribute(
        "forumsSessionClientController");
    MultiSilverpeasBundle resources = (MultiSilverpeasBundle)request.getAttribute("resources");
    if (fsc == null)
    {
        // No forums session controller in the request -> security exception
        String sessionTimeout = ResourceLocator.getGeneralSettingBundle()
            .getString("sessionTimeout");
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
    int forumId = ForumHelper.getIntParameter(request, "forumId", 0);

    boolean isModerator = false;

    fsc.resetDisplayAllMessages();

    ForumActionHelper.actionManagement(request, isAdmin, isModerator, userId, resource, out, fsc);
    boolean isForumSubscriberByInheritance =
      (Boolean) request.getAttribute("isForumSubscriberByInheritance");
%>
<fmt:message key="confirmDeleteForum" var="removeForum" />
<fmt:message key="confirmDeleteCategory" var="removeCategory" />
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="silverpeas.forums">
  <head>
  	<meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <view:looknfeel />
    <view:includePlugin name="rating" />
    <view:includePlugin name="toggle"/>
    <script type="text/javascript" src="<c:url value='/forums/jsp/javaScript/forums.js' />"></script>
    <script type="text/javascript">

      function subscribe() {
        $('#action').val('20');
        $('#forumForm').attr('action', 'main.jsp').submit();
      }

      function unsubscribe() {
        $('#action').val('19');
        $('#forumForm').attr('action', 'main.jsp').submit();
      }

      function subscribeOneForum(forumId, parentForumId) {
        $('#action').val('18');
        $('#params').val(forumId);
        $('#forumId').val(parentForumId);
        $('#forumForm').attr('action', 'main.jsp').submit();
      }

      function unsubscribeOneForum(forumId, parentForumId) {
        $('#action').val('17');
        $('#params').val(forumId);
        $('#forumId').val(parentForumId);
        $('#forumForm').attr('action', 'main.jsp').submit();
      }

      function confirmDeleteForum(forumId)
      {
        jQuery.popup.confirm("<view:encodeJs string="${removeForum}"/>", function() {
          $('#action').val('4');
          $('#params').val(forumId);
          $('#forumForm').attr('action', 'main.jsp').submit();
        });
      }

      function confirmDeleteCategory(categoryId)
      {
        jQuery.popup.confirm("<view:encodeJs string="${removeCategory}"/>", function() {
          $('#CategoryId').val(categoryId);
          $('#forumForm').attr('action', 'DeleteCategory').submit();
        });
      }

      function openSPWindow(fonction, windowName)
      {
        pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400',
        'scrollbars=yes, resizable, alwaysRaised');
      }
    </script>
    <c:if test="${sessionController.useRss}">
      <link rel="alternate" type="application/rss+xml" title="<c:out value="${requestScope.browseContext[1]}" /> : <fmt:message key="forums.rssLast" />" href="<c:url value="${sessionController.RSSUrl}" />" />
    </c:if>
  </head>

  <body id="forum" <%ForumHelper.addBodyOnload(out, fsc);%>>
  <c:set var="isSubscriber" value="${sessionController.componentSubscriber}" />
  <view:browseBar />
    <c:if test="${not isAccessGuest and (isAdmin or isUser or !sessionController.external)}">
      <view:operationPane>
      <c:if test="${isAdmin or isUser}">
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
        <c:if test="${isAdmin}">
          <fmt:message key="newForum" var="addForumAltText" />
          <c:url var="addForumOperation" value="editForumInfo.jsp">
            <c:param name="call" value="main"/>
            <c:param name="action" value="1"/>
            <c:param name="forumId" value="0"/>
            <c:param name="params" value="0"/>
          </c:url>
          <c:url var="addForumIconUrl" value="/util/icons/create-action/add-forum.png" />
          <view:operationOfCreation altText="${addForumAltText}" icon="${addForumIconUrl}" action="${addForumOperation}" />
          <fmt:message key="forums.addCategory" var="addCategoryAltText" />
          <c:set var="addCategoryOperation">javascript:notifyForumPopup('<c:out value="${sessionController.adminIds}" />');</c:set>
          <c:url var="addCategoryIconUrl" value="/util/icons/create-action/add-folder.png" />
          <view:operationOfCreation altText="${addCategoryAltText}" icon="${addCategoryIconUrl}" action="NewCategory" />
        </c:if>
      </c:if>
      <view:operationSeparator/>
        <c:choose>
          <c:when test="${isSubscriber}">
            <fmt:message key="forums.unsubscribe" var="unsubscribeAltText" />
            <view:operation altText="${unsubscribeAltText}" icon="" action="javascript:unsubscribe();" />
          </c:when>
          <c:otherwise>
            <fmt:message key="forums.subscribe" var="subscribeAltText" />
            <view:operation altText="${subscribeAltText}" icon="" action="javascript:subscribe();" />
          </c:otherwise>
        </c:choose>
      </view:operationPane>
    </c:if>
    <view:window>
      <view:frame>
        <view:componentInstanceIntro componentId="${sessionController.componentId}" language="${sessionController.language}"/>
      	  <view:areaOfOperationOfCreation/>
          <table width="100%" border="0" align="center" cellpadding="4" cellspacing="1" class="testTableau">
            <tr>
              <td class="ArrayColumn" colspan="2" nowrap="nowrap"><fmt:message key="theme" /></td>
              <td class="ArrayColumn" nowrap="nowrap"><fmt:message key="forums.nbSubjects" /></td>
              <td class="ArrayColumn" nowrap="nowrap"><fmt:message key="forums.nbMessages" /></td>
              <td class="ArrayColumn" nowrap="nowrap"><fmt:message key="forums.lastMessage" /></td>
              <td class="ArrayColumn" nowrap="nowrap"><fmt:message key="forums.notation" /></td>
              <td class="ArrayColumn" nowrap="nowrap" align="center"><fmt:message key="subscribeMessage" /></td>
              <c:if test="${isAdmin}">
                <td class="ArrayColumn" nowrap="nowrap"><fmt:message key="operations" /></td>
              </c:if>
            </tr>
            <c:forEach var="category" items="${sessionController.allCategories}">
              <%
                NodeDetail category = (NodeDetail) pageContext.getAttribute("category");
                ForumListHelper.displayForumsList(out, resources, isAdmin, isModerator, isReader, forumId, "main", fsc,
                    category.getId(), category.getName(), category.getDescription(), isForumSubscriberByInheritance);
              %>
            </c:forEach>
            <%ForumListHelper.displayForumsList(out, resources, isAdmin, isModerator, isReader, forumId, "main", fsc, null, "", "", isForumSubscriberByInheritance);%>
          </table>
          <c:if test="${sessionController.external || ! isReader}">
            <br />
            <img src="icons/noNewMessage.gif" alt="<fmt:message key="forums.notNewMessageVisite" />" /> <fmt:message key="forums.notNewMessageVisite" />
            <br />
            <img src="icons/newMessage.gif" alt="<fmt:message key="forums.newMessageVisite" />" /> <fmt:message key="forums.newMessageVisite" />
          </c:if>
          <c:if test="${sessionController.useRss}">
            <table align="center">
              <tr>
                <td><a href="<c:url value="${sessionController.RSSUrl}" />"><img src="<c:url value="/util/icons/rss.gif" />" border="0" alt="rss"/></a></td>
              </tr>
            </table>
          </c:if>
      </view:frame>
    </view:window>
    <form id="forumForm" name="forumForm" action="" method="post">
      <input id="action" name="action" type="hidden"/>
      <input id="params" name="params" type="hidden"/>
      <input id="forumId" name="forumId" type="hidden"/>
      <input id="CategoryId" name="CategoryId" type="hidden"/>
    </form>
    <script type="text/javascript">
 	  /* declare the module myapp and its dependencies (here in the silverpeas module) */
 	  var myapp = angular.module('silverpeas.forums', ['silverpeas.services', 'silverpeas.directives']);
 	</script>
  </body>
</html>