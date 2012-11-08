<%--

    Copyright (C) 2000 - 2012 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<view:includePlugin name="popup"/>

<fmt:setLocale value="${requestScope.resources.language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<fmt:message var="mandatoryIconPath" key="rss.mandatoryField" bundle="${icons}"/>
<c:url var="mandatoryIcon"      value="${mandatoryIconPath}" />
<c:url var="formCheckingScript" value="/util/javaScript/checkForm.js"/>

<script type="text/javascript" src="${formCheckingScript}"></script>
<script type="text/javascript">
function addChannel() {
	  $("#modal-newchannel").popup({
	    title: "<fmt:message key="rss.addChannel" />",
	    callback: function() {
	      var isCorrect = validateChannelForm();
	      if (isCorrect) {
	        document.channel.submit();
	      }
	      return isCorrect;
	    }
	  });
	}

	function validateChannelForm() {
		var errorMsg = "";
		var errorNb = 0;
		var url 	= document.channel.Url.value;
		var refresh = document.channel.RefreshRate.value;
		if (isWhitespace(url)) {
			errorMsg+="  - <fmt:message key="GML.theField"/> '<fmt:message key="rss.url"/>' <fmt:message key="GML.MustBeFilled"/>\n";
			errorNb++; 
		}
		if (isWhitespace(refresh) || !isNumericField(refresh)) {
			errorMsg+="  - <fmt:message key="GML.theField"/> '<fmt:message key="rss.refreshRate"/>' <fmt:message key="GML.MustContainsNumber"/>\n";
			errorNb++; 
		}
		switch(errorNb) {
			case 0 :
				result = true;
				break;
			case 1 :
				errorMsg = "<fmt:message key="GML.ThisFormContains"/> 1 <fmt:message key="GML.error"/> : \n" + errorMsg;
				window.alert(errorMsg);
				result = false;
				break;
			default :
				errorMsg = "<fmt:message key="GML.ThisFormContains"/> " + errorNb + " <fmt:message key="GML.errors"/> :\n" + errorMsg;
				window.alert(errorMsg);
				result = false;
				break;
		}
		return result;
	}
</script>

<div id="modal-newchannel" style="display: none">
<form name="channel" action="CreateChannel" method="post">
<table width="100%" border="0" cellspacing="0" cellpadding="4">
	<tr>
		<td class="txtlibform"><fmt:message key="rss.url"/> :</td>
		<td><input type="text" name="Url" maxlength="1000" size="55"/>&nbsp;<img src="${mandatoryIcon}" width="5px"/></td>
	</tr>
	<tr>
		<td class="txtlibform"><fmt:message key="rss.refreshRate"/> :</td>
		<td><input type="text" name="RefreshRate" maxlength="10" size="3" value="10"/>&nbsp;(<fmt:message key="rss.minutes"/>)&nbsp;<img src="${mandatoryIcon}" width="5px" /></td>
	</tr>
	<tr>
		<td class="txtlibform"><fmt:message key="rss.nbDisplayedItems"/> :</td>
		<td><input type="text" name="NbItems" maxlength="10" size="3" value="10"/></td>
	</tr>
	<tr>
		<td class="txtlibform"><fmt:message key="rss.displayImage"/> :</td>
		<td><input type="checkbox" name="DisplayImage"/></td>
	</tr>
	<tr> 
        <td colspan="2" valign="top">( <img src="${mandatoryIcon}" width="5px"/>&nbsp;: <fmt:message key="GML.requiredField"/> )</td>
    </tr>
</table>
</form>
</div>