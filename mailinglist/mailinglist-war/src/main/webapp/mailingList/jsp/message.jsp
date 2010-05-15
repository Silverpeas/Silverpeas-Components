<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1"%>
<%@ include file="check.jsp"%>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
<c:set var="componentId" value="${requestScope.componentId}" />
<c:set var="sessionController">Silverpeas_MailingList_<c:out value="${componentId}" />
</c:set>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
<fmt:message key="mailingList.icons.attachmentBig" var="attachmentIcon" bundle="${icons}" />
<html>
<head>
<c:set var="message" value="${requestScope.currentMessage}" />
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Message</title>
<view:looknfeel />
<script type="text/javascript">
  <c:if test="${(requestScope.currentUserIsAdmin || requestScope.currentUserIsModerator) && ! message.moderated}">
    function deleteMessage(){
      if(confirm('<fmt:message key="mailingList.moderation.delete.confirm"/>')){
        document.moderate.action='<c:url value="/Rmailinglist/${componentId}/message/delete" />';
        document.moderate.submit();
      }
    }
    function moderateMessage(){
      if(confirm('<fmt:message key="mailingList.moderation.moderate.confirm"/>')){
        document.moderate.action='<c:url value="/Rmailinglist/${componentId}/message/put" />';
        document.moderate.submit();
      }
    }
  </c:if>
</script>
</head>
<body>
<c:url value="/Rmailinglist/${componentId}/message/${requestScope.currentMessage.id}" var="messageUrl" />
<c:choose>
  <c:when test="${requestScope.currentFromPath == 'moderation' || requestScope.currentFromPath == 'list' || requestScope.currentFromPath == 'activity'}">
    <c:choose>
      <c:when test="${requestScope.currentFromPath == 'moderation'}">
        <fmt:message key="mailingList.tab.moderation.title" var="browseTitle" />
        <c:url value="/Rmailinglist/${componentId}/moderationList/${componentId}" var="browseUrl" />
      </c:when>
      <c:when test="${requestScope.currentFromPath == 'list'}">
        <fmt:message key="mailingList.tab.list.title" var="browseTitle" />
        <c:url value="/Rmailinglist/${componentId}/list/${componentId}" var="browseUrl" />
      </c:when>
      <c:when test="${requestScope.currentFromPath == 'activity'}">
        <fmt:message key="mailingList.tab.activity.title" var="browseTitle" />
        <c:url value="/Rmailinglist/${componentId}/Main" var="browseUrl" />
      </c:when>
    </c:choose>
    <c:set var="browseBarPath">
      <a href="<c:out value="${browseUrl}"/>"><c:out value="${browseTitle}" /></a>&nbsp;&gt;&nbsp;<c:out value="${requestScope.currentMessage.title}" />
    </c:set>	 
    <view:browseBar>
	  <view:browsebarElt link="${browseUrl}" label="${browseTitle}"  />
	  <view:browsebarElt link="${messageUrl}" label="${requestScope.currentMessage.title}"  />
	</view:browseBar>
  </c:when>
  <c:otherwise>
   <view:browseBar>
	  <view:browsebarElt link="${messageUrl}" label="${requestScope.currentMessage.title}"  />
	</view:browseBar>
  </c:otherwise>
</c:choose>
<c:if test="${(requestScope.currentUserIsAdmin || requestScope.currentUserIsModerator) && ! message.moderated}">
  <fmt:message key="mailingList.icons.message.delete" var="deleteMessageIcon" bundle="${icons}" />
  <fmt:message key="mailingList.icons.message.delete.alt" var="deleteMessageAltText" />
  <fmt:message key="mailingList.icons.message.moderate" var="acceptIcon" bundle="${icons}" />
  <fmt:message key="mailingList.icons.message.moderate.alt" var="acceptMessageAltText" />
  <c:url var="deleteIconUrl" value="${deleteMessageIcon}" />
  <c:url var="acceptIconUrl" value="${acceptIcon}" />
  <view:operationPane>
    <view:operation altText="${deleteMessageAltText}" icon="${deleteIconUrl}" action="javascript: deleteMessage();" />
    <view:operation altText="${acceptMessageAltText}" icon="${acceptIconUrl}" action="javascript: moderateMessage();" />
  </view:operationPane>
  <form name="moderate" id="moderate"><input type="hidden" name="message" id="message" value="<c:out value="${message.id}"/>" />"</form>
</c:if>
<view:window>
  <view:frame>
    <c:set var="attachments" value="${requestScope.currentMessageAttachments}" />
    <table id="message" width="100%" cellspacing="2" cellpadding="2" border="0">
      <tr>
        <td class="txtnav" align="left" colspan="2"><c:out value="${message.title}" /></td>
      </tr>
      <tr>
        <td valign="top" width="100%"><c:out value="${message.body}" escapeXml="false" /></td>
        <td valign="top"><c:if test="${not empty attachments}">
          <table id="attachments" class="tableBoard">
            <tbody>
              <tr>
                <td align="center" valign="top"><img src="<c:url value="${attachmentIcon}" />" /></td>
              </tr>
              <c:forEach items="${attachments}" var="attachment">
                <tr>
                  <td align="left" nowrap="nowrap"><img width="20" src="<c:out value="${attachment.icon}"/>" alt="" /> <a target="_blank" id="<c:out value="${attachment.fileName}" />"
                    href="<c:url value="${attachment.url}"/>"><c:out value="${attachment.fileName}" /></a> <a href="<c:url value="${attachment.url}"/>"><img border="0"
                    title="Pour copier le lien vers ce fichier : Clique droit puis 'Copier le raccourci'" alt="Pour copier le lien vers ce fichier : Clique droit puis 'Copier le raccourci'"
                    src="<c:url value="/util/icons/link.gif"/>" /></a> <br />
                  <c:out value="${attachment.displayableSize}" /><br />
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </c:if></td>
      </tr>
      <tr>
        <td colspan="2" align="center" class="txtBaseline"><c:out value="${message.sender}" /> - <fmt:formatDate value="${message.sentDate}" pattern="dd/MM/yyyy HH:mm:ss" /></td>
      </tr>
    </table>
  </view:frame>
</view:window>
</body>
</html>