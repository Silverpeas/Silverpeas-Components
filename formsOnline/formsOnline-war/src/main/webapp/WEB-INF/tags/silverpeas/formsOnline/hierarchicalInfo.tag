<%--
  Copyright (C) 2000 - 2022 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have recieved a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>
<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%-- Set resource bundle --%>
<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<view:setConstant var="VALIDATOR_OK" constant="org.silverpeas.components.formsonline.model.FormDetail.VALIDATOR_OK"/>
<view:setConstant var="VALIDATOR_UNDEFINED" constant="org.silverpeas.components.formsonline.model.FormDetail.VALIDATOR_UNDEFINED"/>
<view:setConstant var="VALIDATOR_NOT_ALLOWED" constant="org.silverpeas.components.formsonline.model.FormDetail.VALIDATOR_NOT_ALLOWED"/>

<%-- Attributes --%>
<%@ attribute name="formDetail" required="true"
              type="org.silverpeas.components.formsonline.model.FormDetail"
              description="The form" %>

<c:set var="hierarchicalValidatorStatus" value="${formDetail.hierarchicalValidatorState}"/>

<script type="text/javascript">
  $(document).ready(function() {
    if (${hierarchicalValidatorStatus != VALIDATOR_OK}) {
      $(".validateButton").hide();
    }
  });
</script>

<c:if test="${formDetail.hierarchicalValidation}">
  <c:choose>
    <c:when test="${hierarchicalValidatorStatus == VALIDATOR_UNDEFINED}">
      <div class="inlineMessage-nok">
        <fmt:message key="formsOnline.request.boss.undefined"/>
      </div>
    </c:when>
    <c:when test="${hierarchicalValidatorStatus == VALIDATOR_NOT_ALLOWED}">
      <div class="inlineMessage-nok">
        <fmt:message key="formsOnline.request.boss.notallowed">
          <fmt:param>
            <view:username userId="${formDetail.hierarchicalValidatorOfCurrentUser}" zoom="true"/>
          </fmt:param>
        </fmt:message>
      </div>
    </c:when>
    <c:otherwise>
      <div class="inlineMessage">
        <fmt:message key="formsOnline.request.boss.ok">
          <fmt:param>
            <view:username userId="${formDetail.hierarchicalValidatorOfCurrentUser}" zoom="true"/>
          </fmt:param>
        </fmt:message>
      </div>
    </c:otherwise>
  </c:choose>
</c:if>