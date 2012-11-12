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
<%@ include file="check.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<c:set var="parution" value="${requestScope.parution}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<link type="text/css" href="<%=m_context%>/util/styleSheets/fieldset.css" rel="stylesheet" />
<view:looknfeel />
<%
String parution = (String) request.getAttribute("parution");
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
function call_wysiwyg (){
	document.toWysiwyg.submit();
}

function goValidate (){
	if (window.confirm("<%= resource.getString("infoLetter.sendLetter")%>"))
	{
		$.progressMessage();
		document.validateParution.submit();
	}
}

function goView (){
	document.viewParution.submit();
}

function goFiles (){
	document.attachedFiles.submit();
}

function goTemplate (){
	document.template.submit();
}

function submitForm() {
  if (isCorrectForm()) {
    <view:pdcPositions setIn="document.changeParutionHeaders.Positions.value"/>;          
    document.changeParutionHeaders.action = "ChangeParutionHeaders";
    document.changeParutionHeaders.submit();
  }
}
/**
 * This function control form
 * return true if form can be submitted, false else if
 */
function isCorrectForm() {
  var errorMsg = "";
  var errorNb = 0;

  if (!isValidTextArea(document.changeParutionHeaders.description)) {
    errorMsg+="  - <fmt:message key="GML.theField"/> '<fmt:message key="GML.description"/>' <fmt:message key="ContainsTooLargeText"/> <%=DBUtil.getTextAreaLength()%> <fmt:message key="Characters"/>\n";
    errorNb++;
  }
  
  if (isWhitespace(stripInitialWhitespace(document.changeParutionHeaders.title.value))) {
    errorMsg+="  - <fmt:message key="GML.theField"/> '<fmt:message key="infoLetter.name"/>' <fmt:message key="GML.MustBeFilled"/>\n";
    errorNb++;
  }
    
  <view:pdcValidateClassification errorCounter="errorNb" errorMessager="errorMsg"/>

  switch(errorNb) {
  case 0 :
      result = true;
      break;
  case 1 :
      errorMsg = "<fmt:message key="GML.ThisFormContains"/> 1 <fmt:message key="GML.error"/> :\n" + errorMsg;
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

function cancelForm() {
    document.changeParutionHeaders.action = "Accueil";
    document.changeParutionHeaders.submit();
}

function sendLetterToManager (){
	$.progressMessage();
	document.viewParution.action = "SendLetterToManager";
	document.viewParution.submit();
}
</script>
</head>
<body class="infoletter">
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Accueil");
	browseBar.setPath("<a href=\"Accueil\"></a> " + resource.getString("infoLetter.newLetterHeader"));


// Impossible de valider une parution non creee
if (StringUtil.isDefined(parution)) {
	operationPane.addOperation(resource.getIcon("infoLetter.sendLetterToManager"), resource.getString("infoLetter.sendLetterToManager"), "javascript:sendLetterToManager();");	
	operationPane.addLine();
	operationPane.addOperation(resource.getIcon("infoLetter.validLetter"), resource.getString("infoLetter.validLetter"), "javascript:goValidate();");
	operationPane.addLine();
	operationPane.addOperation(resource.getIcon("infoLetter.asTemplate"), 
	resource.getString("infoLetter.saveTemplate"), "javascript:goTemplate();");
}


	out.println(window.printBefore());
 
	//Instanciation du cadre avec le view generator
  TabbedPane tabbedPane = gef.getTabbedPane();
  tabbedPane.addTab(resource.getString("infoLetter.headerLetter"),"#",true);  

// Impossible d'aller sur le WYSIWYG tant que les headers n'ont pas ete valides
if ("".equals(parution)) {
  tabbedPane.addTab(resource.getString("infoLetter.editionLetter"),"#",false);
  tabbedPane.addTab(resource.getString("infoLetter.previewLetter"),"#",false);
  tabbedPane.addTab(resource.getString("infoLetter.attachedFiles"),"#",false);
} else {
  tabbedPane.addTab(resource.getString("infoLetter.editionLetter"),"javascript:call_wysiwyg();",false);
  tabbedPane.addTab(resource.getString("infoLetter.previewLetter"),"javascript:goView();",false);
  tabbedPane.addTab(resource.getString("infoLetter.attachedFiles"),"javascript:goFiles();",false);
}

out.println(tabbedPane.print());
out.println(frame.printBefore());
%>

<%-- Initialize image --%>
<fmt:message key="infoLetter.mandatory" var="mandatoryIcon" bundle="${icons}" />
<c:url var="mandatoryIconUrl" value="${mandatoryIcon}" />

<form name="changeParutionHeaders" action="ChangeParutionHeaders" method="post">
  <input type="hidden" name="parution" value="<%= parution %>"/>
  <input type="hidden" name="Positions" value=""/>



<fieldset id="infoFieldset" class="skinFieldset">
  <legend><fmt:message key="infoletter.header.fieldset.info" /></legend>
  
  <!-- SAISIE DU FORUM -->
  <div class="fields">
    <!-- Info letter title -->
    <div class="field" id="titleArea">
      <label class="txtlibform" for="titleName"><fmt:message key="infoLetter.name" /> :&nbsp;</label>
      <div class="champs">
        <input type="text" name="title" size="50" maxlength="<%=DBUtil.getTextFieldLength()%>" value="<%= (String) request.getAttribute("title") %>" />&nbsp;<img src="${mandatoryIconUrl}" width="5" height="5"/>
      </div>
    </div>
    <!-- Info letter description  -->
    <div class="field" id="descriptionArea">
      <label class="txtlibform" for="description"><fmt:message key="GML.description" /> :&nbsp;</label>
      <div class="champs">
        <textarea name="description" cols="60" rows="6"><%= (String) request.getAttribute("description") %></textarea>
      </div>
    </div>
  </div>
</fieldset>

</form>
<form name="toWysiwyg" action="../../wysiwyg/jsp/htmlEditor.jsp" method="post">
    <input type="hidden" name="SpaceId" value="<%= (String) request.getAttribute("SpaceId") %>"/>
    <input type="hidden" name="SpaceName" value="<%= (String) request.getAttribute("SpaceName") %>"/>
    <input type="hidden" name="ComponentId" value="<%= (String) request.getAttribute("ComponentId") %>"/>
    <input type="hidden" name="ComponentName" value="<%= (String) request.getAttribute("ComponentName") %>"/>
    <input type="hidden" name="BrowseInfo" value="<%= (String) request.getAttribute("BrowseInfo") %>"/> 
    <input type="hidden" name="ObjectId" value="<%= (String) request.getAttribute("ObjectId") %>"/>
    <input type="hidden" name="Language" value="<%= (String) request.getAttribute("Language") %>"/>
    <input type="hidden" name="ReturnUrl" value="<%= (String) request.getAttribute("ReturnUrl") %>"/>
</form>
<c:if test="${empty parution}">
  <view:pdcNewContentClassification componentId="<%=ils.getComponentId()%>" />
</c:if>
<c:if test="${not empty parution}">
  <view:pdcClassification componentId="<%=ils.getComponentId()%>" contentId="${parution}" editable="true" />
</c:if>

<div class="legend">
  <fmt:message key="GML.requiredField" /> : <img src="${mandatoryIconUrl}" width="5" height="5" />
</div>

<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:submitForm();", false));
    buttonPane.addButton(gef.getFormButton(resource.getString("GML.cancel"), "javascript:cancelForm();", false));
    out.println(buttonPane.print());
%>
<form name="validateParution" action="ValidateParution" method="post">			
	<input type="hidden" name="parution" value="<%= parution %>"/>
</form>
<form name="viewParution" action="Preview" method="post">			
	<input type="hidden" name="parution" value="<%= parution %>"/>
  <input type="hidden" name="ReturnUrl" value="ParutionHeaders"/>
</form>
<form name="attachedFiles" action="FilesEdit" method="post">			
	<input type="hidden" name="parution" value="<%= parution %>"/>
</form>
<form name="template" action="UpdateTemplateFromHeaders" method="post">			
	<input type="hidden" name="parution" value="<%= parution %>"/>
</form>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
<view:progressMessage/>
</body>
</html>