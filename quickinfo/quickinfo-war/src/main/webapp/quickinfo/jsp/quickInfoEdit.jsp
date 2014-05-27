<%--

    Copyright (C) 2000 - 2013 Silverpeas

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
<%@page import="com.silverpeas.thumbnail.model.ThumbnailDetail"%>
<%@page import="org.silverpeas.components.quickinfo.model.News"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<c:set var="curQuickInfo" value="${requestScope['info']}"/>
<c:set var="thumbnailSettings" value="${requestScope['ThumbnailSettings']}"/>

<%@ include file="checkQuickInfo.jsp" %>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager" %>
<%@ page import="com.silverpeas.util.EncodeHelper" %>

<%
News news = (News) request.getAttribute("info");
PublicationDetail quickInfoDetail = null;
ThumbnailDetail thumbnail = null;
if (news != null) {
  quickInfoDetail = news.getPublication();
  thumbnail = news.getThumbnail();
}

String codeHtml = "";
String title = "";
String beginDate = "";
String endDate = "";
String broadcastMajorChecked = "";
String broadcastTickerChecked = "";
String broadcastBlockingChecked = "";
if (quickInfoDetail != null) {
 	title = quickInfoDetail.getTitle();
	codeHtml = quickInfoDetail.getWysiwyg();
	if (quickInfoDetail.getBeginDate() != null) {
    	beginDate = resources.getInputDate(quickInfoDetail.getBeginDate());
  	}
  	if (quickInfoDetail.getEndDate() != null) {
    	endDate = resources.getInputDate(quickInfoDetail.getEndDate());
  	}
  	if (news.getBroadcastModes().contains(News.BROADCAST_MAJOR)) {
  	  broadcastMajorChecked = "checked=\"checked\"";
  	}
  	if (news.getBroadcastModes().contains(News.BROADCAST_TICKER)) {
  	  broadcastTickerChecked = "checked=\"checked\"";
  	}
  	if (news.getBroadcastModes().contains(News.BROADCAST_BLOCKING)) {
  	  broadcastBlockingChecked = "checked=\"checked\"";
  	}
}

ButtonPane buttonPane = gef.getButtonPane();
buttonPane.addButton(gef.getFormButton(resources.getString("GML.validate"), "javascript:onclick=saveNews()", false));
buttonPane.addButton(gef.getFormButton(resources.getString("GML.cancel"), "Main", false));

pageContext.setAttribute("thumbnailBackURL", URLManager.getFullApplicationURL(request)+URLManager.getURL("useless", componentId), PageContext.PAGE_SCOPE);
%>

<c:set var="thumbnailBackURL" value="${pageScope.thumbnailBackURL}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>QuickInfo - Edition</title>
<link type="text/css" href="<%=m_context%>/util/styleSheets/fieldset.css" rel="stylesheet" />
<view:looknfeel/>
<view:includePlugin name="datepicker"/>
<view:includePlugin name="wysiwyg"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="js/quickinfo.js"></script>
<script type="text/javascript">
function isCorrectForm() {
 	var errorMsg = "";
 	var errorNb = 0;
 	
	if (isWhitespace($("#Name").val())) {
  	  errorMsg +=" - '<%=resources.getString("GML.title")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
  	  errorNb++; 
    }
       
	var beginDate = {dateId : 'BeginDate'};
	var endDate = {dateId : 'EndDate'};
	var dateErrors = isPeriodEndingInFuture(beginDate, endDate);
	$(dateErrors).each(function(index, error) {
  	  errorMsg += " - " + error.message + "\n";
  	  errorNb++;
	});

    <view:pdcValidateClassification errorCounter="errorNb" errorMessager="errorMsg"/>
  
  	var error = {
      msg: errorMsg, 
      nb: errorNb
    };
  	checkThumbnail(error);
    
 	switch(error.nb) {
    	case 0 :
        	result = true;
        	break;
    	case 1 :
      		error.msg = "<%=resources.getString("GML.ThisFormContains")%> 1 <%=resources.getString("GML.error")%> : \n" + error.msg;
        	window.alert(error.msg);
        	result = false;
        	break;
    	default :
      		error.msg = "<%=resources.getString("GML.ThisFormContains")%> " + error.nb + " <%=resources.getString("GML.errors")%> :\n" + error.msg;
    	    window.alert(error.msg);
    	    result = false;
    	    break;
 	}
 	return result;
}

function saveNews() {
	if (isCorrectForm()) {
  		<c:if test="${empty curQuickInfo}">
    		<view:pdcPositions setIn="document.newsForm.Positions.value"/>;
   		</c:if>
		document.newsForm.submit();
	}
}

$(document).ready(function() {
	<view:wysiwyg replace="Content" language="<%=language%>" width="98%" height="300" toolbar="quickInfo" displayFileBrowser="${false}"/>
});
</script>
</head>
<body id="quickinfo">
<div id="<%=componentId %>">
<fmt:message var="broweBarMsg" key="edition"/>
<view:browseBar extraInformations="${broweBarMsg}" />
<c:if test="${not empty curQuickInfo}">
	<view:operationPane>
	  <fmt:message var="deleteMsg" key="GML.delete"/>
	  <fmt:message var="deleteConfirmMsg" key="supprimerQIConfirmation"/>
	  <view:operation altText="${deleteMsg}" icon="${deleteIconUrl}" action="javascript:onclick=confirmDelete(${news.id}, '${deleteConfirmMsg}')"/>
	</view:operationPane>
</c:if>

<view:window>
<form name="newsForm" action="Save" method="post" enctype="multipart/form-data">
  <input type="hidden" name="Positions" />
  <c:if test="${not empty curQuickInfo}">
    <input type="hidden" name="Id" value="${curQuickInfo.id}"/>
  </c:if>
<view:frame>

<fieldset id="infoFieldset" class="skinFieldset">
  <legend><fmt:message key="quickinfo.header.fieldset.info" /></legend>
  <!-- Quick info form -->
  <div class="fields">
    <!-- Quick info name -->
    <div class="field" id="nameArea">
      <label class="txtlibform" for="name"><fmt:message key="GML.title" /> </label>
      <div class="champs">
        <c:if test="${not empty curQuickInfo}"><c:set var="curName" value="${curQuickInfo.title}"/></c:if>
        <input type="text" name="Name" size="50" id="Name" maxlength="<%=DBUtil.getTextFieldLength()%>" value="<view:encodeHtmlParagraph string="${curName}"/>" />
        &nbsp;<img border="0" src="<%=m_context%>/util/icons/mandatoryField.gif" width="5" height="5"/>
      </div>
    </div>
    
    <div class="field" id="descriptionArea">
      <label class="txtlibform" for="description"><fmt:message key="GML.description" /> </label>
      <div class="champs">
        <c:if test="${not empty curQuickInfo}"><c:set var="curDesc" value="${curQuickInfo.description}"/></c:if>
        <textarea name="Description" cols="50" rows="3" id="Description"><c:out value="${curDesc}"/></textarea>
      </div>
    </div>
    
    <div class="field" id="broadcastArea">
      <label class="txtlibform"><fmt:message key="quickinfo.news.broadcast.mode" /> </label>
      <div class="champs">
      	<input type="checkbox" name="BroadcastMode" value="<%=News.BROADCAST_MAJOR%>" <%=broadcastMajorChecked %>/> <fmt:message key="quickinfo.news.broadcast.mode.major" />
      	<input type="checkbox" name="BroadcastMode" value="<%=News.BROADCAST_TICKER%>" <%=broadcastTickerChecked %>/> <fmt:message key="quickinfo.news.broadcast.mode.ticker" />
      	<input type="checkbox" name="BroadcastMode" value="<%=News.BROADCAST_BLOCKING%>" <%=broadcastBlockingChecked %>/> <fmt:message key="quickinfo.news.broadcast.mode.blocking" />
      </div>
    </div>

    <div class="field" id="contentArea">
      <label class="txtlibform" for="content"><fmt:message key="quickinfo.news.content" /> </label>
      <div class="champs">
        <textarea name="Content" id="Content" rows="50" cols="10"><%=codeHtml%></textarea>
      </div>
    </div>
    
  </div>
</fieldset>

<div class="table">
	<div class="cell">
		<fieldset id="datesFieldset" class="skinFieldset">
	  		<legend><fmt:message key="quickinfo.header.fieldset.period" /></legend>
	  		<div class="fields">
	    		<div class="field" id="BeginDateArea">
		      		<label for="BeginDate" class="txtlibform"><fmt:message key="GML.dateBegin" /></label>
		      		<div class="champs">
		        		<input type="text" class="dateToPick" id="BeginDate" name="BeginDate" size="12" value="<%=beginDate%>" maxlength="<%=DBUtil.getDateFieldLength()%>"/>
		        		<span class="txtnote">(<fmt:message key="GML.dateFormatExemple"/>)</span>
		      		</div>
	    		</div>
	    		<div class="field" id="EndDateArea">
	      			<label for="EndDate" class="txtlibform"><fmt:message key="GML.dateEnd" /></label>
	      			<div class="champs">
	        			<input type="text" class="dateToPick" id="EndDate" name="EndDate" size="12" value="<%=endDate%>" maxlength="<%=DBUtil.getDateFieldLength()%>"/>
	        			<span class="txtnote">(<fmt:message key="GML.dateFormatExemple"/>)</span>
	      			</div>
	    		</div>
	  		</div>
		</fieldset>
	</div>

	<div class="cell">
		<fieldset id="news-thumbnail" class="skinFieldset">
			<legend><fmt:message key="GML.thumbnail" /></legend>
			<c:url var="backURL" value="${thumbnailBackURL}/quickInfoEdit">
				<c:param name="Action" value="Edit"/>
				<c:param name="Id" value="${curQuickInfo.id}"/>
			</c:url>
			<viewTags:displayThumbnail thumbnail="<%=thumbnail %>" mandatory="${thumbnailSettings.mandatory}" componentId="${curQuickInfo.componentInstanceId}" objectId="${curQuickInfo.id}" backURL="${backURL}" objectType="<%=ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE %>" width="${thumbnailSettings.width}" height="${thumbnailSettings.height}"/>
		</fieldset>
	</div>
</div>

<c:if test="${empty curQuickInfo}">
  <view:pdcNewContentClassification componentId="<%=quickinfo.getComponentId()%>" />
</c:if>
<c:if test="${not empty curQuickInfo}">
  <view:pdcClassification componentId="<%=quickinfo.getComponentId()%>" contentId="${curQuickInfo.id}" editable="true" />
</c:if>

<div class="legend">
  <fmt:message key="GML.requiredField" /> : <img src="<%=m_context%>/util/icons/mandatoryField.gif" width="5" height="5" />
</div>

<%=buttonPane.print()%>
    
</view:frame>
</form>

</view:window>
</div>
</body>
</html>