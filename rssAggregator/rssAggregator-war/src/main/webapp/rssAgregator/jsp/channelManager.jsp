<%--

    Copyright (C) 2000 - 2019 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<view:includePlugin name="popup"/>

<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<c:set var="channel" value="${requestScope.Channel}"/>
<c:set var="addMode" value="${channel == null}"/>
<c:set var="deleteMode" value="${not addMode && silfn:booleanValue(requestScope.DeletionMode)}"/>
<c:if test="${not addMode}">
  <jsp:useBean id="channel" type="org.silverpeas.components.rssaggregator.model.SPChannel"/>
</c:if>
<c:set var="displayImageChecked" value="${(not addMode and silfn:booleanValue(channel.displayImage)) ? 'checked' : ''}"/>
<c:set var="safeUrlChecked" value="${(not addMode and silfn:booleanValue(channel.safeUrl)) ? 'checked' : ''}"/>

<c:choose>
  <c:when test="${deleteMode}">
    <c:set var="tmp" value="${channel.url}"/>
    <c:if test="${silfn:isDefined(channel.feed.title)}">
      <c:set var="tmp">${channel.feed.title} <span style="font-weight: normal">(${tmp})</span></c:set>
    </c:if>
    <fmt:message key="GML.confirmation.delete" var="deleationConfirmationMessage">
      <fmt:param>${tmp}</fmt:param>
    </fmt:message>
    <div>${deleationConfirmationMessage}</div>
    <view:form name="deleteChannel" action="DeleteChannel" method="post">
      <input type="hidden" name="Id" value="${channel.getPK().id}"/>
    </view:form>
    <script type="text/javascript">
      function validateChannelForm() {
        document.deleteChannel.submit();
      }
    </script>
  </c:when>
  <c:otherwise>
    <fmt:message var="mandatoryIconPath" key="rss.mandatoryField" bundle="${icons}"/>
    <c:url var="mandatoryIcon" value="${mandatoryIconPath}"/>
    <fmt:message key="GML.theField" var="theFieldLabel"/>
    <fmt:message key="rss.url" var="urlLabel"/>
    <fmt:message key="rss.refreshRate" var="refreshRateLabel"/>
    <fmt:message key="rss.minutes" var="minutesLabel"/>
    <fmt:message key="rss.nbDisplayedItems" var="nbDisplayedItemsLabel"/>
    <fmt:message key="rss.displayImage" var="displayImageLabel"/>
    <fmt:message key="rss.safeUrl" var="safeUrlLabel"/>
    <fmt:message key="rss.safeUrlHelp" var="safeUrlHelp"/>
    <fmt:message key="GML.requiredField" var="requiredFieldMessage"/>
    <fmt:message key="GML.MustBeFilled" var="mustBeFilledMessage"/>
    <fmt:message key="GML.MustContainsNumber" var="mustContainsNumberMessage"/>
    <view:script src="/util/javaScript/checkForm.js"/>
    <script type="text/javascript">
      function validateChannelForm() {
        var url = document.channel.Url.value;
        var refresh = document.channel.RefreshRate.value;
        if (isWhitespace(url)) {
          SilverpeasError.add("${theFieldLabel} <strong>${urlLabel}</strong> ${mustBeFilledMessage}");
        }
        if (isWhitespace(refresh) || !isNumericField(refresh)) {
          SilverpeasError.add("${theFieldLabel} <strong>${refreshRateLabel}</strong> ${mustContainsNumberMessage}");
        }
        if (!SilverpeasError.show()) {
          document.channel.submit();
        }
        return false;
      }
    </script>

    <div id="modal-channel">
      <view:form name="channel" action="${addMode ? 'CreateChannel' : 'UpdateChannel'}" method="post">
        <input type="hidden" name="Id" value="${not addMode ? channel.getPK().id : ''}"/>
        <table>
          <tr>
            <td class="txtlibform"><label for="url-input">${urlLabel} :</label></td>
            <td><input type="text" id="url-input" name="Url" maxlength="1000" size="55" value="${addMode ? '' : channel.url}"/>&nbsp;<img src="${mandatoryIcon}" width="5px" alt=""/></td>
          </tr>
          <tr>
            <td class="txtlibform"><label for="refresh-rate-input">${refreshRateLabel} :</label></td>
            <td><input type="text" id="refresh-rate-input" name="RefreshRate" maxlength="10" size="3" value="${addMode ? '10' : channel.refreshRate}"/>&nbsp;(${minutesLabel})&nbsp;<img src="${mandatoryIcon}" width="5px" alt=""/></td>
          </tr>
          <tr>
            <td class="txtlibform"><label for="nb-items-input">${nbDisplayedItemsLabel} :</label></td>
            <td><input type="text" id ="nb-items-input" name="NbItems" maxlength="10" size="3" value="${addMode ? '10' : channel.nbDisplayedItems}"/></td>
          </tr>
          <tr>
            <td class="txtlibform"><label for="display-image-input">${displayImageLabel} :</label></td>
            <td><input type="checkbox" id="display-image-input" name="DisplayImage" ${displayImageChecked}/></td>
          </tr>
          <tr>
            <td class="txtlibform"><label for="safe-url-input">${safeUrlLabel} :</label></td>
            <td><input type="checkbox" id="safe-url-input" name="SafeUrl" ${safeUrlChecked}/>&nbsp;<img class="infoBulle" title="${safeUrlHelp}" src="<c:url value="/util/icons/help.png"/>" alt="info"/></td>
          </tr>
          <tr>
            <td colspan="2">( <img src="${mandatoryIcon}" width="5px" alt=""/>&nbsp;: ${requiredFieldMessage} )</td>
          </tr>
        </table>
      </view:form>
    </div>
  </c:otherwise>
</c:choose>