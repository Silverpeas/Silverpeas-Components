<%--

    Copyright (C) 2000 - 2017 Silverpeas

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
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page import="org.silverpeas.components.formsonline.control.RequestUIEntity" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="lang" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<c:set var="controller" value="${requestScope.FormsOnline}"/>
<jsp:useBean id="controller" type="org.silverpeas.components.formsonline.control.FormsOnlineSessionController"/>

<fmt:setLocale value="${lang}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<c:set var="requests" value="${requestScope['Requests']}"/>
<jsp:useBean id="requests" type="org.silverpeas.components.formsonline.model.RequestsByStatus"/>

<fmt:message var="statusReadLabel" key="formsOnline.stateRead"/>
<fmt:message var="statusValidatedLabel" key="formsOnline.stateValidated"/>
<fmt:message var="statusDeniedLabel" key="formsOnline.stateRefused"/>
<fmt:message var="statusArchivedLabel" key="formsOnline.stateArchived"/>
<fmt:message var="statusUnreadLabel" key="formsOnline.stateUnread"/>

<fmt:message var="colStatus" key="GML.status"/>
<fmt:message var="colDate" key="formsOnline.sendDate"/>
<fmt:message var="colSender" key="formsOnline.sender"/>
<fmt:message var="colForm" key="formsOnline.Form"/>
<fmt:message var="colValidator" key="formsOnline.receiver"/>

<fmt:message var="deletionConfirmMessage" key="formsOnline.requests.action.delete.confirm"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title></title>
  <view:looknfeel/>
  <script type="text/javascript">

    var arrayPaneAjaxControl;
    var checkboxMonitor = sp.selection.newCheckboxMonitor('#list input[name=selection]');

    function removeRequests() {
      jQuery.popup.confirm('${silfn:escapeJs(deletionConfirmMessage)}', function() {
        var ajaxRequest = sp.ajaxRequest("DeleteRequests").byPostMethod();
        checkboxMonitor.prepareAjaxRequest(ajaxRequest);
        ajaxRequest.send().then(arrayPaneAjaxControl.refreshFromRequestResponse);
      });
    }
  </script>
</head>
<body>
<fmt:message var="browseBarAll" key="formsOnline.requests.all.breadcrumb"/>
<view:browseBar extraInformations="${browseBarAll}"/>
<view:operationPane>
  <fmt:message var="deleteReq" key="formsOnline.removeFormInstance"/>
  <view:operationOfCreation action="javascript:removeRequests()" icon="" altText="${deleteReq}"/>
</view:operationPane>
<view:window>
  <view:frame>
    <div id="list">
      <c:set var="requestStatusLabelLambda" value="${r ->
              (r.data.read ? statusReadLabel :
              (r.data.validated ? statusValidatedLabel :
              (r.data.denied ? statusDeniedLabel :
              (r.data.archived ? statusArchivedLabel : statusUnreadLabel))))}"/>
      <c:set var="requestItems" value="<%=RequestUIEntity.convertList(requests.getAll(), controller.getSelectedValidatorRequestIds())%>"/>
      <view:arrayPane var="myForms" routingAddress="InBox" numberLinesPerPage="25">
        <view:arrayColumn width="10" sortable="false"/>
        <view:arrayColumn title="${colStatus}" compareOn="${requestStatusLabelLambda}"/>
        <view:arrayColumn title="${colDate}" compareOn="${r -> r.data.creationDate}"/>
        <view:arrayColumn title="${colForm}" compareOn="${r -> r.data.form.title}"/>
        <view:arrayColumn title="${colSender}" compareOn="${r -> r.creator.displayedName}"/>
        <view:arrayColumn title="${colValidator}" compareOn="${r -> r.validator.displayedName}"/>
        <view:arrayLines var="request" items="${requestItems}">
          <view:arrayLine>
            <c:choose>
              <c:when test="${request.data.archived}">
                <view:arrayCellCheckbox name="selection"
                                        checked="${request.selected}"
                                        value="${request.id}"/>
              </c:when>
              <c:otherwise>
                <view:arrayCellText text=""/>
              </c:otherwise>
            </c:choose>
            <view:arrayCellText text="${requestStatusLabelLambda(request)}"/>
            <view:arrayCellText text="${silfn:formatDate(request.data.creationDate, lang)}"/>
            <view:arrayCellText><a href="ViewRequest?Id=${request.id}&Origin=InBox">${request.data.form.title}</a></view:arrayCellText>
            <view:arrayCellText text="${request.creator.displayedName}"/>
            <view:arrayCellText text="${request.validator.displayedName}"/>
          </view:arrayLine>
        </view:arrayLines>
      </view:arrayPane>
      <script type="text/javascript">
        whenSilverpeasReady(function() {
          checkboxMonitor.pageChanged();
          arrayPaneAjaxControl = sp.arrayPane.ajaxControls('#list', {
            before : checkboxMonitor.prepareAjaxRequest
          });
        });
      </script>
    </div>
  </view:frame>
</view:window>
</body>
</html>