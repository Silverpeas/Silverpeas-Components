<%@ page import="org.silverpeas.core.node.model.NodeDetail" %><%--

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
	Collection<NodeDetail> allCategories = (Collection) request.getAttribute("AllCategories");
	String categoryId = null;
%>
<head>
<title><fmt:message key="GML.popupTitle" /></title>
<view:looknfeel withFieldsetStyle="true"/>
<view:includePlugin name="wysiwyg"/>
<script type="text/javascript">
<!--
function save() {
	var errorMsg = "";
	var errorNb = 0;

	var title = $("#title").val();
	var content = $("#content").val();
	var titleR = $("#titleR").val();

  if (isWhitespace(title)) {
    errorMsg+="  - '<fmt:message key="questionReply.question"/>' <fmt:message key="GML.MustBeFilled"/>\n";
    errorNb++;
  }

  if (!isValidTextArea(content)) {
		errorMsg+="  - '<fmt:message key="GML.description"/>' <fmt:message key="questionReply.containsTooLargeText" /><fmt:message key="questionReply.nbMaxTextArea" /><fmt:message key="questionReply.characters" />\n";
		errorNb++;
  }
	if (isWhitespace(titleR)) {
		errorMsg+="  - '<fmt:message key="questionReply.reponse"/>' <fmt:message key="GML.MustBeFilled"/>\n";
		errorNb++;
  }

  <view:pdcValidateClassification errorCounter="errorNb" errorMessager="errorMsg"/>

  switch(errorNb)
  {
     case 0 :
       sp.editor.wysiwyg.lastBackupManager.clear();
         <view:pdcPositions setIn="document.myForm.Positions.value"/>;
         document.forms[0].submit();
         break;
     case 1 :
         errorMsg = "<fmt:message key="GML.ThisFormContains" /> 1 <fmt:message key="GML.error" /> : \n" + errorMsg;
         jQuery.popup.error(errorMsg);
         break;
     default :
         errorMsg = "<fmt:message key="GML.ThisFormContains" /> " + errorNb + " <fmt:message key="GML.errors" /> :\n" + errorMsg;
         jQuery.popup.error(errorMsg);
  }
}

function cancel() {
  sp.editor.wysiwyg.lastBackupManager.clear();
  sp.formConfig('Main').submit();
}

$(document).ready(function() {
	<view:wysiwyg replace="contentR" language="<%=language%>" width="600" height="300" componentId="<%=componentId%>"
	              toolbar="questionReply" displayFileBrowser="${false}" activateWysiwygBackupManager="true"/>
});
//-->
</script>
</head>
<body id="<%=componentId%>" class="questionReply addQR" onload="document.forms[0].title.focus();">
<fmt:message key="questionReply.addQR" var="currentPathLabel"/>
<view:browseBar extraInformations="${currentPathLabel}"/>
<view:window>

<form method="post" name="myForm" action="<%=routerUrl%>EffectiveCreateQR">
  <input type="hidden" name="Positions" />

<fieldset id="questionFieldset" class="skinFieldset">
  <legend><fmt:message key="questionReply.fieldset.question" /></legend>
  <div class="fields">
    <div class="field" id="categoryArea">
      <label class="txtlibform" for="CategoryId"><fmt:message key="questionReply.category" /> </label>
      <div class="champs">
        <select name="CategoryId">
        <option value=""></option>
        <%
        if (allCategories != null) {
          String selected = "";
            for (NodeDetail uneCategory : allCategories) {
              if (categoryId != null && categoryId.equals(uneCategory.getNodePK().getId())) {
                selected = "selected=\"selected\"";
              }
              %>
              <option value="<%=uneCategory.getNodePK().getId()%>" <%=selected%>><%=uneCategory.getName()%></option>
              <%
              selected = "";
            }
          }
        %>
        </select>
      </div>
    </div>

    <div class="field" id="questionArea">
      <label class="txtlibform" for="title"><fmt:message key="questionReply.question" /> </label>
      <div class="champs">
        <input type="text" name="title" size="100" id="title" maxlength="300" value="" />
        &nbsp;<img alt="<%=resource.getString("GML.requiredField")%>" src="<%=resource.getIcon("questionReply.mandatory")%>" width="5" height="5" />
      </div>
    </div>

    <div class="field" id="contentArea">
      <label class="txtlibform" for="content"><fmt:message key="GML.description" /> </label>
      <div class="champs">
        <textarea name="content" id="content" cols="120" rows="5" ></textarea>
      </div>
    </div>
  </div>
</fieldset>

  <view:pdcNewContentClassification componentId="<%=scc.getComponentId()%>" />

<fieldset id="answerFieldset" class="skinFieldset">
  <legend><fmt:message key="questionReply.fieldset.answer" /></legend>
  <div class="fields">

    <div class="field" id="answerArea">
      <label class="txtlibform" for="titleR"><fmt:message key="questionReply.reponse" /> </label>
      <div class="champs">
        <input type="text" name="titleR" size="100" id="titleR" maxlength="300" value="" />
        &nbsp;<img alt="<%=resource.getString("GML.requiredField")%>" src="<%=resource.getIcon("questionReply.mandatory")%>" width="5" height="5" />
      </div>
    </div>

    <div class="field" id="contentRArea">
      <label class="txtlibform" for="contentR"><fmt:message key="GML.description" /> </label>
      <div class="champs">
        <textarea name="contentR" id="contentR" cols="120" rows="5" ></textarea>
      </div>
    </div>

  </div>
</fieldset>

  <view:fileUpload fieldset="true" jqueryFormSelector="form[name='myForm']" />

<div class="legend">
  <fmt:message key="GML.requiredField" /> : <img src="<%=m_context%>/util/icons/mandatoryField.gif" width="5" height="5" />
</div>

</form>
<br/>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:save();", false));
    buttonPane.addButton(gef.getFormButton(resource.getString("GML.cancel"), "javascript:cancel();", false));
    out.println(buttonPane.print());
%>
</view:window>
</body>
</html>