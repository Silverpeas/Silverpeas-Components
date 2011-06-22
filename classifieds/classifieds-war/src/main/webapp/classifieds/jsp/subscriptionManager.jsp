<%--

    Copyright (C) 2000 - 2011 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@page import="com.silverpeas.form.Form"%>
<%@page import="com.silverpeas.form.PagesContext"%>
<%@page import="com.silverpeas.form.DataRecord"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<c:set var="sessionController" value="Silverpeas_classifieds_${requestScope.InstanceId}" />

<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<c:set var="formSearch" value="${requestScope.Form}" />
<c:set var="data" value="${requestScope.Data}" />
<c:set var="instanceId" value="${requestScope.InstanceId}" />
<c:set var="language" value="${sessionScope[sessionController].language}"/>

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
			width: 400,
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
	<FORM Name="SubscriptionForm" action="AddSubscription" Method="POST" ENCTYPE="multipart/form-data">
	<c:if test="${not empty formSearch}">
		<table border="0" width="100%" align="center">
			<!-- AFFICHAGE du formulaire -->
			<tr>
				<td colspan="2">
					<%
						String language = (String) pageContext.getAttribute("language");
						String instanceId = (String) pageContext.getAttribute("instanceId");
						Form formSearch = (Form) pageContext.getAttribute("formSearch");
						DataRecord data = (DataRecord) pageContext.getAttribute("data");

						PagesContext context = new PagesContext("formSearch", "0", language, false, instanceId, null, null);
					    context.setIgnoreDefaultValues(true);
					    context.setUseMandatory(false);
						formSearch.display(out, context, data);
					%>
				</td>
			</tr>
		</table>
		<br/>
	</c:if>
	</FORM>
</div>