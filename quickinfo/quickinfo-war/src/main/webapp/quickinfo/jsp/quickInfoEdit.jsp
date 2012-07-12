<%--

    Copyright (C) 2000 - 2011 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<c:set var="curQuickInfo" value="${requestScope['info']}"/>
<c:set var="curPubId" value="${requestScope['Id']}"/>

<%@ include file="checkQuickInfo.jsp" %>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager" %>
<%@ page import="com.silverpeas.util.EncodeHelper" %>

<%
PublicationDetail quickInfoDetail = (PublicationDetail) request.getAttribute("info");
String pubId   = (String) request.getAttribute("Id");

String routerUrl = URLManager.getApplicationURL() + URLManager.getURL("quickinfo", quickinfo.getSpaceId(), quickinfo.getComponentId());

boolean isNewSubscription = true;
String codeHtml = "";
if (pubId != null && pubId != "-1") {
 	isNewSubscription = false;
	if (quickInfoDetail.getWysiwyg() != null && !"".equals(quickInfoDetail.getWysiwyg())) {
    codeHtml = quickInfoDetail.getWysiwyg();
	} else if (quickInfoDetail.getDescription() != null) {
    codeHtml = EncodeHelper.javaStringToHtmlParagraphe(quickInfoDetail.getDescription());
	}
}
String beginDate = "";
String endDate = "";
if (quickInfoDetail != null) {
  if (quickInfoDetail.getBeginDate() != null) {
    beginDate = resources.getInputDate(quickInfoDetail.getBeginDate());
  }
  if (quickInfoDetail.getEndDate() != null) {
    endDate = resources.getInputDate(quickInfoDetail.getEndDate());
  }
}


%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>QuickInfo - Edition</title>
<link type="text/css" href="<%=m_context%>/util/styleSheets/fieldset.css" rel="stylesheet" />
<view:looknfeel/>
<view:includePlugin name="datepicker"/>
<view:includePlugin name="wysiwyg"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
function isCorrectForm() {
 	var errorMsg = "";
 	var errorNb = 0;
 	var beginDate = $("#BeginDate").val();
  var endDate = $("#EndDate").val();
  var yearBegin = extractYear(beginDate, '<%=quickinfo.getLanguage()%>');
  var monthBegin = extractMonth(beginDate, '<%=quickinfo.getLanguage()%>');
	var dayBegin = extractDay(beginDate, '<%=quickinfo.getLanguage()%>');
	var yearEnd = extractYear(endDate, '<%=quickinfo.getLanguage()%>'); 
	var monthEnd = extractMonth(endDate, '<%=quickinfo.getLanguage()%>');
	var dayEnd = extractDay(endDate, '<%=quickinfo.getLanguage()%>'); 
	var beginDateOK = false;
	var endDateOK = false;

	if (isWhitespace($("#Name").val())) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.title")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
       errorNb++; 
    }
       
    if (! isWhitespace(beginDate)) {
    	if (isCorrectDate(yearBegin, monthBegin, dayBegin)==false) {
            	errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("dateDebut")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
             	errorNb++;
    	}
    	else beginDateOK = true;
    }	
  
    if (! isWhitespace(endDate)) {
    	if (isCorrectDate(yearEnd, monthEnd, dayEnd)==false) {
            	errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("dateFin")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
             	errorNb++;
    	}
    	else endDateOK = true;
    }
    
    if (beginDateOK && endDateOK) {
    		if (isD1AfterD2(yearEnd, monthEnd, dayEnd, yearBegin, monthBegin, dayBegin)==false) {
    			errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("dateFin")%>' <%=resources.getString("MustContainsPostDateToBeginDate")%>\n";
                            errorNb++;	
    		}
  }      	       

  <view:pdcValidateClassification errorCounter="errorNb" errorMessager="errorMsg"/>
    
 	switch(errorNb)
 	{
    	case 0 :
        	result = true;
        	break;
    	case 1 :
        	errorMsg = "<%=resources.getString("GML.ThisFormContains")%> 1 <%=resources.getString("GML.error")%> : \n" + errorMsg;
        	window.alert(errorMsg);
        	result = false;
        	break;
    	default :
    	    errorMsg = "<%=resources.getString("GML.ThisFormContains")%> " + errorNb + " <%=resources.getString("GML.errors")%> :\n" + errorMsg;
    	    window.alert(errorMsg);
    	    result = false;
    	    break;
 	}
 	return result;
}

function reallyAddQuickInfo() {
	if (isCorrectForm()) {
    <view:pdcPositions setIn="document.quickInfoEditForm.Positions.value"/>;    
		document.quickInfoEditForm.Action.value = "ReallyAdd";
		document.quickInfoEditForm.submit();
	}
}

function updateQuickInfo() {
	if (isCorrectForm()) {
		document.quickInfoEditForm.Action.value = "ReallyUpdate";
		document.quickInfoEditForm.submit();
	}
}

function quickInfoDeleteConfirm() {
	if (window.confirm("<%=resources.getString("supprimerQIConfirmation")%>")) {
      document.quickInfoEditForm.Action.value = "ReallyRemove";
      document.quickInfoEditForm.submit();
	}
}

function ClipboardCopyOne() {
	document.quickInfoForm.action = "<%=m_context%><%=quickinfo.getComponentUrl()%>copy.jsp";
	document.quickInfoForm.target = "IdleFrame";
	document.quickInfoForm.submit();
}

$(document).ready(function() {
	<view:wysiwyg replace="Description" language="<%=language%>" width="600" height="300" toolbar="quickinfo"/>
});
</script>
</head>
<body id="quickinfo">
<div id="<%=componentId %>">
<fmt:message var="broweBarMsg" key="edition"/>
<view:browseBar extraInformations="${broweBarMsg}" />
<view:operationPane>
  <c:url var="deleteIconUrl" value="/util/icons/quickInfo_to_del.gif"/>
  <fmt:message var="deleteMsg" key="suppression"/>
  <view:operation altText="${deleteMsg}" icon="${deleteIconUrl}" action="javascript:onClick=quickInfoDeleteConfirm()"/>
  <c:url var="copyIconUrl" value="/util/icons/quickInfo_to_del.gif"/>
  <fmt:message var="copyMsg" key="GML.copy"/>
  <view:operation altText="${copyMsg}" icon="${copyIconUrl}" action="javascript:onClick=ClipboardCopyOne()"/>
</view:operationPane>

<view:window>
<form name="quickInfoEditForm" action="quickInfoEdit" method="post">
  <input type="hidden" name="Positions" />

<view:frame>

<fieldset id="infoFieldset" class="skinFieldset">
  <legend><fmt:message key="quickinfo.header.fieldset.info" /></legend>
  <!-- Quick info form -->
  <div class="fields">
    <!-- Quick info name -->
    <div class="field" id="nameArea">
      <label class="txtlibform" for="name"><fmt:message key="GML.title" /> </label>
      <div class="champs">
        <c:if test="${not empty curQuickInfo}"><c:set var="curName" value="${curQuickInfo.name}"/></c:if>
        <input type="text" name="Name" size="50" id="Name" maxlength="<%=DBUtil.getTextFieldLength()%>" value="<view:encodeHtmlParagraph string="${curName}"/>" />
        &nbsp;<img border="0" src="<%=m_context%>/util/icons/mandatoryField.gif" width="5" height="5"/>
      </div>
    </div>

    <div class="field" id="descriptionArea">
      <label class="txtlibform" for="description"><fmt:message key="GML.description" /> </label>
      <div class="champs">
        <textarea name="Description" id="Description" rows="50" cols="10"><%=codeHtml%></textarea>
      </div>
    </div>
    
  </div>
</fieldset>

<fieldset id="datesFieldset" class="skinFieldset">
  <legend><fmt:message key="quickinfo.header.fieldset.period" /></legend>
  <div class="fields">
    <div class="field" id="BeginDateArea">
      <label for="BeginDate" class="txtlibform"><fmt:message key="dateDebut" /></label>
      <div class="champs">
        <input type="text" class="dateToPick" id="BeginDate" name="BeginDate" size="12" value="<%=beginDate%>" maxlength="<%=DBUtil.getDateFieldLength()%>"/>
        <span class="txtnote">(<fmt:message key="GML.dateFormatExemple"/>)</span>
      </div>
    </div>
    <div class="field" id="EndDateArea">
      <label for="EndDate" class="txtlibform"><fmt:message key="dateFin" /></label>
      <div class="champs">
        <input type="text" class="dateToPick" id="EndDate" name="EndDate" size="12" value="<%=endDate%>" maxlength="<%=DBUtil.getDateFieldLength()%>"/>
        <span class="txtnote">(<fmt:message key="GML.dateFormatExemple"/>)</span>
      </div>
    </div>
  </div>
</fieldset>

<c:if test="${empty curPubId}">
  <view:pdcNewContentClassification componentId="<%=quickinfo.getComponentId()%>" />
</c:if>
<c:if test="${not empty curPubId}">
  <view:pdcClassification componentId="<%=quickinfo.getComponentId()%>" contentId="${curPubId}" editable="true" />
</c:if>

<div class="legend">
  <fmt:message key="GML.requiredField" /> : <img src="<%=m_context%>/util/icons/mandatoryField.gif" width="5" height="5" />
</div>


<%
	
	ButtonPane	buttonPane		= gef.getButtonPane();

%>
  
  <table width="100%" border="0" cellspacing="0" cellpadding="5">
    <tr>
      <td align="right">
        <%
          String link = "javascript:onClick=updateQuickInfo()";
          if (quickInfoDetail == null) {
            link = "javascript:onClick=reallyAddQuickInfo()";
          }
          Button button = gef.getFormButton(resources.getString("GML.validate"), link, false);
					buttonPane.addButton(button);
          button = gef.getFormButton(resources.getString("GML.cancel"), "Main", false);
					buttonPane.addButton(button);
        %>
      <br/><%=buttonPane.print()%><br/>
      </td>
    </tr>
  </table>
    
	<input type="hidden" name="Action"/>
    <% if (quickInfoDetail != null) { %>
    	<input type="hidden" name="Id" value="<%=quickInfoDetail.getPK().getId()%>"/>
  	<% } %>
</view:frame>
</form>

</view:window>

<form name="quickInfoForm" action="quickInfoEdit.jsp" method="post">
  <input type="hidden" name="Action"/>
  <% if (quickInfoDetail != null) { %>
      <input type="hidden" name="Id" value="<%=quickInfoDetail.getPK().getId()%>"/>
  <% } %>
</form>
</div>
</body>
</html>