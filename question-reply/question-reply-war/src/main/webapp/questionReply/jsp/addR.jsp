<%--

    Copyright (C) 2000 - 2012 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
<fmt:setLocale value="{sessionScope.SilverSessionController.favoriteLanguage}" />

<%@ include file="checkQuestionReply.jsp" %>
<%
	Reply reply = (Reply) request.getAttribute("reply");
	boolean usedPrivateReplies = (Boolean) request.getAttribute("UsedPrivateReplies");
%>
<c:set var="usedPrivateReplies" value="${requestScope['UsedPrivateReplies']}"/>
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<link type="text/css" href="<%=m_context%>/util/styleSheets/fieldset.css" rel="stylesheet" />
<view:looknfeel />
<link rel="stylesheet" type="text/css" href="css/question-reply-css.jsp" />
<view:includePlugin name="wysiwyg"/>
<script type="text/javascript">
<!--
function isCorrectForm() {
  var errorMsg = "";
  var errorNb = 0;

	var title = $("#title").val();

	if (isWhitespace(title)) {
    errorMsg+="  - '<%=resource.getString("GML.name")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
    errorNb++;
  }

  switch(errorNb)
  {
    case 0 :
      result = true;
      break;
    case 1 :
      errorMsg = "<%=resource.getString("GML.ThisFormContains")%> 1 <%=resource.getString("GML.error")%> : \n" + errorMsg;
      window.alert(errorMsg);
      result = false;
      break;
    default :
      errorMsg = "<%=resource.getString("GML.ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
      window.alert(errorMsg);
      result = false;
      break;
  }
  return result;

}
function save() {
	if (isCorrectForm()) {
		document.forms[0].submit();
	}
}

$(document).ready(function() {
	<view:wysiwyg replace="content" language="<%=language%>" width="600" height="300" toolbar="questionreply"/>
});
//-->
</script>
</head>
<body id="<%=componentId%>" class="questionReply addR" onload="document.forms[0].title.focus();">

<%
	browseBar.setExtraInformation(resource.getString("questionReply.reponse"));

	tabbedPane.addTab(resource.getString("GML.head"), "#", true, false);
  tabbedPane.addTab(resource.getString("GML.attachments"), "#", false);

	out.println(window.printBefore());
	out.println(tabbedPane.print());
%>

<form method="post" name="myForm" action="<%=routerUrl%>EffectiveCreateR">

<fieldset id="answerFieldset" class="skinFieldset">
  <legend><fmt:message key="questionReply.fieldset.answer" /></legend>
  <div class="fields">

    <div class="field" id="answerArea">
      <label class="txtlibform" for="title"><fmt:message key="questionReply.reponse" /> </label>
      <div class="champs">
        <input type="text" name="title" size="100" id="title" maxlength="100" value="" />
        &nbsp;<img alt="<%=resource.getString("GML.requiredField")%>" src="<%=resource.getIcon("questionReply.mandatory")%>" width="5" height="5" />
      </div>
    </div>

    <div class="field" id="contentArea">
      <label class="txtlibform" for="content"><fmt:message key="GML.description" /> </label>
      <div class="champs">
        <textarea name="content" id="content" cols="120" rows="5" ></textarea>
      </div>
    </div>

<c:choose>
  <c:when test="${usedPrivateReplies}">
    <div class="field" id="privateArea">
      <label class="txtlibform" for="private"><fmt:message key="questionReply.Rprivee" /> </label>
      <div class="champs">
        <input type="radio" name="publicReply" value="0" checked />
      </div>
    </div>
    <div class="field" id="publicArea">
      <label class="txtlibform" for="public"><fmt:message key="questionReply.Rpublique" /> </label>
      <div class="champs">
        <input type="radio" name="publicReply" value="1" />
      </div>
    </div>
  </c:when>
  <c:otherwise>
    <input type="hidden" name="publicReply" value="1" />
  </c:otherwise>
</c:choose>

  </div>
</fieldset>

<div class="legend">
  <fmt:message key="GML.requiredField" /> : <img src="<%=m_context%>/util/icons/mandatoryField.gif" width="5" height="5" />
</div>

</form>
<br/>
<%
  ButtonPane buttonPane = gef.getButtonPane();
  buttonPane.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:save();", false));
  buttonPane.addButton(gef.getFormButton(resource.getString("GML.cancel"), "ConsultQuestionQuery", false));
  out.println(buttonPane.print());
	out.println(window.printAfter());
%>
</body>
</html>