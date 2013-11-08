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
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@ page import="com.stratelia.webactiv.forums.control.ForumsSessionController"%>
<%@ page import="com.stratelia.webactiv.forums.control.helpers.ForumActionHelper"%>
<%@ page import="com.stratelia.webactiv.forums.control.helpers.ForumHelper"%>
<%@ page import="com.stratelia.webactiv.forums.control.helpers.ForumListHelper"%>
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodeDetail" %>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%
ForumsSessionController fsc = (ForumsSessionController) request.getAttribute(
        "forumsSessionClientController");
    ResourcesWrapper resources = (ResourcesWrapper)request.getAttribute("resources");
    if (fsc == null)
    {
        // No forums session controller in the request -> security exception
        String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator()
            .getString("sessionTimeout");
        getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout)
            .forward(request, response);
        return;
    }

    ResourceLocator resource = new ResourceLocator(
      "com.stratelia.webactiv.forums.multilang.forumsBundle", fsc.getLanguage());

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
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
  	<meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <view:looknfeel />
    <script type="text/javascript" src="<c:url value='/forums/jsp/javaScript/forums.js' />"></script>
    <script type="text/javascript" src="<c:url value='/util/javaScript/animation.js' />"></script>
    <script type="text/javascript">

      function subscribe() {
        window.location.href = "main.jsp?action=20";
      }

      function unsubscribe() {
        window.location.href = "main.jsp?action=19";
      }

      function confirmDeleteForum(forumId)
      {
        if (confirm("<view:encodeJs string="${removeForum}"/>"))
        {
          window.location.href = "main.jsp?action=4&params=" + forumId;
        }
      }

      function confirmDeleteCategory(categoryId)
      {
        if (confirm("<view:encodeJs string="${removeCategory}"/>"))
        {
          window.location.href = "DeleteCategory?CategoryId=" + categoryId;
        }
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
    <c:if test="${isAdmin or isUser or !sessionController.external}">
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
        <c:set var="mail2AdminOperation">javascript:notifyPopup2('<c:out value="${pageContext.request.contextPath}"/>', '<c:out value="${sessionController.componentId}" />','<c:out value="${sessionController.adminIds}" />', '');</c:set>
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
          <c:set var="addCategoryOperation">javascript:notifyPopup2('<c:out value="${pageContext.request.contextPath}"/>','<c:out value="${sessionController.componentId}" />','<c:out value="${sessionController.adminIds}" />', '');</c:set>
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
                    Integer.toString(category.getId()), category.getName(), category.getDescription(), isForumSubscriberByInheritance);
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
  </body>
</html>