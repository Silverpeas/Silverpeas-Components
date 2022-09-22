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
    "http://www.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ include file="check.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="componentId" value="${requestScope.browseContext[3]}"/>
<c:set var="currentUserLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${currentUserLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<fmt:message key="community.menu.item.subscribe" var="subscribeLabel"/>
<fmt:message key="community.menu.item.unsubscribe" var="unsubscribeLabel"/>

<c:set var="isUserSubscribed" value="${requestScope.isUserSubscribed}"/>

<view:sp-page>
  <view:sp-head-part>
    <script type="application/javascript">
      SUBSCRIPTION_PROMISE.then(function() {
        window.spSubManager = new SilverpeasSubscriptionManager({
          componentInstanceId : '${componentId}', labels : {
            subscribe : '${silfn:escapeJs(subscribeLabel)}',
            unsubscribe : '${silfn:escapeJs(unsubscribeLabel)}'
          }
        });
      });
    </script>
  </view:sp-head-part>
  <view:sp-body-part>
    <view:operationPane>
      <c:if test="${isUserSubscribed != null}">
        <view:operationSeparator/>
        <view:operation altText="<span id='subscriptionMenuLabel'></span>" icon="" action="javascript:spSubManager.switchUserSubscription()"/>
      </c:if>
    </view:operationPane>
    <view:window>
      <view:frame>
        Bienvenue sur l'application community.
      </view:frame>
      <view:frame>
        <view:board>
          Cette instance s'appelle
          <span class="communityName"><c:out value="${requestScope.browseContext[1]}"/></span>.<br/>
          Elle se situe dans l'espace
          <span class="communityName"><c:out value="${requestScope.browseContext[0]}"/></span>.
        </view:board>
      </view:frame>
    </view:window>
  </view:sp-body-part>
</view:sp-page>