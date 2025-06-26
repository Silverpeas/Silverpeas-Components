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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<c:set var="language" value="${requestScope.resources.language}"/>

<fmt:setLocale value="${language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<c:set var="fields" value="${requestScope.Fields}"/>

<fmt:message var="dialogTitle" key="classifieds.subscriptionsAdd"/>
<fmt:message var="validateLabel" key="GML.validate"/>
<fmt:message var="cancelLabel" key="GML.cancel"/>

<script type="text/javascript">
  function addSubscription() {
    $( "#subscription-adding" ).dialog( "open" );
  }

  $(function() {
		$( "#subscription-adding" ).dialog({
            title: "${dialogTitle}",
			autoOpen: false,
			height: 'auto',
			width: 500,
			modal: true,
			buttons: {
				'${validateLabel}': function() {
                    sendSubscriptionData();
                },
				'${cancelLabel}' : function() {
					$( this ).dialog( "close" );
				}
			}
		});
	});

	function sendSubscriptionData() {
      $( "#subscription-adding" ).dialog( "close" );
      document.SubscriptionForm.submit();
	}
</script>

<div id="subscription-adding" style="display: none">
	<br/>
	<form name="SubscriptionForm" action="AddSubscription" method="post" enctype="multipart/form-data">
	<c:if test="${not empty fields}">
        <div class="forms mode-search">
            <ul class="fields">
                <c:forEach items="${fields}" var="field">
                <jsp:useBean id="field"
                             type="org.silverpeas.components.classifieds.servlets.SubscriptionField"/>
                <li class="field field_${field.key}" id="form-row-${field.key}">
                    <div>
                        <label for="${field.key}">${field.label}</label>
                    </div>
                    <div class="fieldInput">
                        <select id="${field.key}" name="${field.key}">
                            <option value="" selected></option>
                            <c:forEach items="${field.values}" var="value">
                            <jsp:useBean id="value"
                                         type="org.silverpeas.components.classifieds.servlets.SubscriptionFieldValue"/>
                            <option value="${value.key}">${value.value}</option>
                            </c:forEach>
                        </select>
                    </div>
                </li>
                </c:forEach>
            </ul>
        </div>
	</c:if>
	</form>
</div>