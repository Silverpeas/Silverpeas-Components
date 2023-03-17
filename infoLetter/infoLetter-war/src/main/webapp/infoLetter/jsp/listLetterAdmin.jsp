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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="check.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/infoLetter" prefix="infoLetterTags" %>
<%@ page import="org.silverpeas.core.util.DateUtil" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="org.silverpeas.components.infoletter.model.InfoLetterPublication" %>
<%@ page import="java.util.function.Predicate" %>

<c:set var="resources" value="${requestScope.resources}"/>
<jsp:useBean id="resources" type="org.silverpeas.core.util.MultiSilverpeasBundle"/>
<c:set var="userLanguage" value="${resources.language}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle bundle="${resources.multilangBundle}"/>
<view:setBundle bundle="${resources.iconsBundle}" var="icons"/>

<c:set var="showHeader" value="${requestScope.showHeader}"/>
<jsp:useBean id="showHeader" type="java.lang.Boolean"/>
<c:set var="isSuscriber" value="${requestScope.userIsSuscriber}"/>
<jsp:useBean id="isSuscriber" type="java.lang.Boolean"/>
<c:set var="isAdmin" value="${requestScope.userIsAdmin}"/>
<jsp:useBean id="isAdmin" type="java.lang.Boolean"/>
<c:set var="isPdcUsed" value="${requestScope.isPdcUsed}"/>
<jsp:useBean id="isPdcUsed" type="java.lang.Boolean"/>
<c:set var="isTemplateExist" value="${requestScope.IsTemplateExist}"/>
<jsp:useBean id="isTemplateExist" type="java.lang.Boolean"/>

<c:set var="publications" value="${requestScope.listParutions}"/>
<jsp:useBean id="publications" type="java.util.List<org.silverpeas.components.infoletter.model.InfoLetterPublication>"/>

<c:set var="lastNSent" value="${resources.getSetting('admin.lastNSent', 4)}"/>
<jsp:useBean id="lastNSent" type="java.lang.Integer"/>

<c:set var="isAnonymous" value="${requestScope.isAnonymous}"/>
<c:set var="isAccessGuest" value="${requestScope.isAccessGuest}"/>
<c:set var="letterName" value="${requestScope.letterName}"/>
<c:set var="letterDescription" value="${requestScope.letterDescription}"/>
<c:set var="letterFrequence" value="${requestScope.letterFrequence}"/>
<c:set var="beingEditedPublications" value="<%=publications.stream().filter(Predicate.not(InfoLetterPublication::_isValid)).collect(Collectors.toList())%>"/>
<c:set var="sentPublications" value="<%=publications.stream().filter(InfoLetterPublication::_isValid).collect(Collectors.toList())%>"/>
<jsp:useBean id="sentPublications" type="java.util.List<org.silverpeas.components.infoletter.model.InfoLetterPublication>"/>
<c:set var="lastNSentPublications" value="<%=sentPublications.stream().limit(lastNSent).collect(Collectors.toList())%>"/>

<fmt:message key="infoLetter.modelLink" var="seeTemplateLabel"/>
<fmt:message key="infoLetter.modifyTemplate" var="modifyTemplateLabel"/>
<fmt:message key="infoLetter.modifyTemplate" var="modifyTemplateIcon" bundle="${icons}"/>
<c:url var="modifyTemplateIcon" value="${modifyTemplateIcon}"/>
<fmt:message key="PDCUtilization" var="setPdcUseLabel"/>
<fmt:message key="infoLetter.pdcUtilization" var="setPdcUseIcon" bundle="${icons}"/>
<c:url var="setPdcUseIcon" value="${setPdcUseIcon}"/>
<fmt:message key="infoLetter.modifierHeader" var="modifyHeaderLabel"/>
<fmt:message key="infoLetter.modifierHeader" var="modifyHeaderIcon" bundle="${icons}"/>
<c:url var="modifyHeaderIcon" value="${modifyHeaderIcon}"/>
<fmt:message key="infoLetter.newPubli" var="createNewLabel"/>
<fmt:message key="infoLetter.newPubli" var="createNewIcon" bundle="${icons}"/>
<c:url var="createNewIcon" value="${createNewIcon}"/>
<fmt:message key="GML.delete" var="deleteLabel"/>
<fmt:message key="infoLetter.delPubli" var="deleteIcon" bundle="${icons}"/>
<c:url var="deleteIcon" value="${deleteIcon}"/>
<fmt:message key="infoLetter.access_SilverAbonnes" var="spSubscriberLabel"/>
<fmt:message key="infoLetter.access_SilverAbonnes" var="spSubscriberIcon" bundle="${icons}"/>
<c:url var="spSubscriberIcon" value="${spSubscriberIcon}"/>
<fmt:message key="infoLetter.access_ExternAbonnes" var="extSubscriberLabel"/>
<fmt:message key="infoLetter.access_ExternAbonnes" var="extSubscriberIcon" bundle="${icons}"/>
<c:url var="extSubscriberIcon" value="${extSubscriberIcon}"/>
<fmt:message key="infoLetter.desabonner" var="unsubscribeLabel"/>
<fmt:message key="infoLetter.desabonner" var="unsubscribeIcon" bundle="${icons}"/>
<c:url var="unsubscribeIcon" value="${unsubscribeIcon}"/>
<fmt:message key="infoLetter.abonner" var="subscribeLabel"/>
<fmt:message key="infoLetter.abonner" var="subscribeIcon" bundle="${icons}"/>
<c:url var="subscribeIcon" value="${subscribeIcon}"/>
<fmt:message key="infoLetter.confirmDeleteParution" var="confirmDeleteOneMsg"/>
<fmt:message key="infoLetter.confirmDeleteParutions" var="confirmDeleteMsg"/>
<fmt:message key="infoLetter.section.draft.title" var="draftSectionTitle"/>
<fmt:message key="infoLetter.section.sent.title" var="sentSectionTitle"/>
<fmt:message key="infoLetter.section.lastNSent.title" var="lastNSentSectionTitle">
  <fmt:param value="${lastNSent}"/>
</fmt:message>

<c:set var="componentId" value="<%=componentId%>"/>

<view:sp-page angularJsAppName="silverpeas.infoLetter">
  <view:sp-head-part withCheckFormScript="true">
    <view:includePlugin name="toggle"/>
    <script type="text/javascript">
      let arrayPaneAjaxControl;
      let checkboxMonitor = sp.selection.newCheckboxMonitor('#newsletter-list input[name=selection]');
      let templateWindow = window;

      function deleteNewsletter(id) {
        jQuery.popup.confirm('${silfn:escapeJs(confirmDeleteOneMsg)}', function() {
          const ajaxRequest = sp.ajaxRequest("DeletePublication").withParam('id', id).byPostMethod();
          checkboxMonitor.prepareAjaxRequest(ajaxRequest);
          spProgressMessage.show();
          ajaxRequest.send().then(arrayPaneAjaxControl.refreshFromRequestResponse).then(function(html) {
            return sp.updateTargetWithHtmlContent(['#infoletter-draft', '#infoletter-lastNSent'], html, true);
          });
        });
      }

      <c:if test="${isAdmin}">
      function deleteSelected() {
        jQuery.popup.confirm('${silfn:escapeJs(confirmDeleteMsg)}', function() {
          const ajaxRequest = sp.ajaxRequest("DeletePublications").byPostMethod();
          checkboxMonitor.prepareAjaxRequest(ajaxRequest);
          spProgressMessage.show();
          ajaxRequest.send().then(arrayPaneAjaxControl.refreshFromRequestResponse).then(function(html) {
            return sp.updateTargetWithHtmlContent('#infoletter-lastNSent', html, true);
          });
        });
      }
      </c:if>

      function openEditParution(par) {
        sp.navRequest('Preview').withParam('parution', par).go();
      }

      function openViewParution(par) {
        sp.navRequest('View').withParam('parution', par).go();
      }

      function openSPWindow(fonction, windowName) {
        window.pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400',
            'scrollbars=yes, resizable, alwaysRaised');
      }

    </script>
  </view:sp-head-part>
  <view:sp-body-part>
    <view:operationPane>
      <c:if test="${isAdmin and isPdcUsed}">
        <c:url value="/RpdcUtilization/jsp/Main?ComponentId=${componentId}" var="tmpUrl"/>
        <view:operation altText="${setPdcUseLabel}" icon="${setPdcUseIcon}" action="javascript:openSPWindow('${tmpUrl}','utilizationPdc1')"/>
        <view:operationSeparator/>
      </c:if>
      <c:if test="${showHeader}">
        <view:operation action="LetterHeaders" icon="${modifyHeaderIcon}" altText="${modifyHeaderLabel}"/>
      </c:if>
      <c:if test="${isAdmin}">
        <view:operation action="EditTemplateContent" icon="${modifyTemplateIcon}" altText="${modifyTemplateLabel}"/>
        <view:operationSeparator/>
      </c:if>
      <view:operationOfCreation action="ParutionHeaders" icon="${createNewIcon}" altText="${createNewLabel}"/>
      <c:if test="${isAdmin}">
        <view:operation action="javascript:deleteSelected()" icon="${deleteIcon}" altText="${deleteLabel}"/>
      </c:if>
      <view:operationSeparator/>
      <view:operation action="Suscribers" icon="${spSubscriberIcon}" altText="${spSubscriberLabel}"/>
      <view:operation action="Emails" icon="${extSubscriberIcon}" altText="${extSubscriberLabel}"/>
      <c:if test="${not isAnonymous and not isAccessGuest}">
        <c:choose>
          <c:when test="${isSuscriber}">
            <view:operation action="UnsuscribeMe" icon="${unsubscribeIcon}" altText="${unsubscribeLabel}"/>
          </c:when>
          <c:otherwise>
            <view:operation action="SuscribeMe" icon="${subscribeIcon}" altText="${subscribeLabel}"/>
          </c:otherwise>
        </c:choose>
      </c:if>
    </view:operationPane>
    <view:window>
      <view:frame>
        <view:componentInstanceIntro componentId="${componentId}" language="${userLanguage}"/>
        <c:if test="${showHeader}">
          <div class="headerInfoLetter">
            <h2 class="name">${silfn:escapeHtml(letterName)}</h2>
            <div class="frequence">${silfn:escapeHtml(letterFrequence)}</div>
            <p class="description componentInstanceIntro">${silfn:escapeHtml(letterDescription)}</p>
          </div>
        </c:if>
        <div id="infoletter-home-app">
          <div id="infoletter-draft">
            <view:areaOfOperationOfCreation/>
            <div class="header">
              <h3 class="infoletter-draft-title">${draftSectionTitle}</h3>
            </div>
            <infoLetterTags:infoLetterList newsletters="${beingEditedPublications}"/>
          </div>
          <div id="infoletter-lastNSent">
            <div class="header">
              <h3 class="infoletter-last-title">${lastNSentSectionTitle}</h3>
            </div>
            <infoLetterTags:infoLetterList newsletters="${lastNSentPublications}" readonly="${not isAdmin}"/>
          </div>
          <div id="infoletter-sended">
            <div class="header">
              <h3 class="infoletter-sending-title">${sentSectionTitle}</h3>
            </div>
            <fmt:message key="infoLetter.name" var="nameLabel"/>
            <fmt:message key="GML.date" var="dateLabel"/>
            <fmt:message key="GML.operation" var="operationLabel"/>
            <fmt:message key="infoLetter.minicone" var="newsletterIcon" bundle="${icons}"/>
            <c:url var="newsletterIcon" value="${newsletterIcon}"/>
            <fmt:message key="infoLetter.permalink" var="permlinkIcon" bundle="${icons}"/>
            <c:url var="permlinkIcon" value="${permlinkIcon}"/>
            <fmt:message key="infoLetter.nonParu" var="notReleaseLabel"/>
            <fmt:message key="infoLetter.nonvisible" var="notReleaseIcon" bundle="${icons}"/>
            <c:url var="notReleaseIcon" value="${notReleaseIcon}"/>
            <fmt:message key="infoLetter.paru" var="releaseLabel"/>
            <fmt:message key="infoLetter.visible" var="releaseIcon" bundle="${icons}"/>
            <c:url var="releaseIcon" value="${releaseIcon}"/>
            <div id="newsletter-list">
              <view:arrayPane var="InfoLetter" routingAddress="Main">
                <view:arrayColumn title="" sortable="false"/>
                <view:arrayColumn title="${nameLabel}" compareOn="${n -> n.title}"/>
                <view:arrayColumn title="${dateLabel}" compareOn="${n -> n.parutionDate}"/>
                <c:if test="${isAdmin}">
                  <view:arrayColumn title="${operationLabel}" sortable="false"/>
                </c:if>
                <view:arrayLines var="pub" items="${sentPublications}">
                  <jsp:useBean id="pub" type="org.silverpeas.components.infoletter.model.InfoLetterPublication"/>
                  <c:set var="pubId" value="${pub.getPK().id}"/>
                  <c:set var="accessUrl" value="javascript:open${pub._isValid() ? 'View' : 'Edit'}Parution('${pubId}')"/>
                  <view:arrayLine>
                    <view:arrayCellText>
                      <a href="${accessUrl}">
                        <img src="${newsletterIcon}" alt=""/>
                      </a>
                    </view:arrayCellText>
                    <view:arrayCellText>
                      <a href="${accessUrl}">${silfn:escapeHtml(pub.title)}</a>
                      <a href="${pub._getPermalink()}" class="sp-permalink">
                        <img src="${permlinkIcon}" alt=""/>
                      </a>
                    </view:arrayCellText>
                    <view:arrayCellText>
                      <c:if test="${pub._isValid()}">
                        <c:set var="parutionDate" value="<%=DateUtil.parse(pub.getParutionDate())%>"/>
                        ${silfn:formatDate(parutionDate, userLanguage)}
                      </c:if>
                    </view:arrayCellText>
                    <c:if test="${isAdmin}">
                      <view:arrayCellCheckbox name="selection" value="${pubId}" checked=""/>
                    </c:if>
                  </view:arrayLine>
                </view:arrayLines>
              </view:arrayPane>
              <script type="text/javascript">
                whenSilverpeasReady(function() {
                  checkboxMonitor.pageChanged();
                  arrayPaneAjaxControl = sp.arrayPane.ajaxControls('#newsletter-list', {
                    before : checkboxMonitor.prepareAjaxRequest
                  });
                });
              </script>
            </div>
          </div>
        </div>
      </view:frame>
    </view:window>
    <view:progressMessage/>
  </view:sp-body-part>
</view:sp-page>