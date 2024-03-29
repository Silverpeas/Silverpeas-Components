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
<%@page import="org.silverpeas.core.io.media.image.thumbnail.model.ThumbnailDetail"%>
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
<c:set var="newOneInProgress" value="${requestScope['NewOneInProgress']}"/>
<c:set var="appSettings" value="${requestScope['AppSettings']}"/>

<%@ include file="checkQuickInfo.jsp" %>
<%@ page import="org.silverpeas.core.util.URLUtil" %>
<%@ page import="org.silverpeas.core.persistence.jdbc.DBUtil" %>
<%@ page import="org.silverpeas.core.contribution.publication.model.PublicationDetail" %>
<%@ page import="org.silverpeas.core.contribution.model.Thumbnail" %>

<%
News news = (News) request.getAttribute("info");
PublicationDetail quickInfoDetail = null;
Thumbnail thumbnail = null;
if (news != null) {
  quickInfoDetail = news.getPublication();
  thumbnail = news.getThumbnail();
}

String codeHtml = "";
String title = "";
String beginDate = "";
String beginHour = "";
String endDate = "";
String endHour = "";
String broadcastMajorChecked = "";
String broadcastTickerChecked = "";
String broadcastBlockingChecked = "";
if (quickInfoDetail != null) {
 	title = quickInfoDetail.getTitle();
	codeHtml = quickInfoDetail.getContent().getRenderer().renderEdition();
	if (quickInfoDetail.getBeginDate() != null) {
    	beginDate = resources.getInputDate(quickInfoDetail.getBeginDate());
    	beginHour = quickInfoDetail.getBeginHour();
  	}
  	if (quickInfoDetail.getEndDate() != null) {
    	endDate = resources.getInputDate(quickInfoDetail.getEndDate());
    	endHour = quickInfoDetail.getEndHour();
  	}
  	if (news.isImportant()) {
  	  broadcastMajorChecked = "checked=\"checked\"";
  	}
  	if (news.isTicker()) {
  	  broadcastTickerChecked = "checked=\"checked\"";
  	}
  	if (news.isMandatory()) {
  	  broadcastBlockingChecked = "checked=\"checked\"";
  	}
  	pageContext.setAttribute("thumbnailBackURL", URLUtil.getFullApplicationURL(request)+
				URLUtil.getURL("useless", componentId)+"View?Id="+news.getId(), PageContext.PAGE_SCOPE);
}
%>

<fmt:message var="buttonOK" key="GML.validate"/>
<fmt:message var="buttonCancel" key="GML.cancel"/>
<fmt:message var="buttonPublish" key="GML.publish"/>
<fmt:message var="buttonSaveDraft" key="GML.draft.save"/>

<c:set var="thumbnailBackURL" value="${pageScope.thumbnailBackURL}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>QuickInfo - Edition</title>
<view:looknfeel withFieldsetStyle="true" withCheckFormScript="true"/>
<view:includePlugin name="datepicker"/>
<view:includePlugin name="wysiwyg"/>
<view:includePlugin name="popup"/>
<script type="text/javascript" src="js/quickinfo.js"></script>
<script type="text/javascript">
function isCorrectForm() {
 	var errorMsg = "";
 	var errorNb = 0;
 	
	if (isWhitespace($("#Name").val())) {
  	  errorMsg +=" - '<fmt:message key="GML.title" />' <fmt:message key="GML.MustBeFilled" />\n";
  	  errorNb++; 
    } else {
      if ($("#Name").val().length > 150) {
   		errorMsg +=" - '<fmt:message key="GML.title" />' <fmt:message key="GML.data.error.message.string.limit"><fmt:param value="150"/></fmt:message>\n";
    	errorNb++; 
      }
    }
    
	if ($("#Description").val().length > 300) {
	  errorMsg +=" - '<fmt:message key="GML.description" />' <fmt:message key="GML.data.error.message.string.limit"><fmt:param value="300"/></fmt:message>\n";
	  errorNb++; 
  	}
       
	var beginDate = {dateId : 'BeginDate', hourId : 'beginHour'};
	var endDate = {dateId : 'EndDate', hourId : 'endHour', defaultDateHour : '23:59'};
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
        	break;
    	case 1 :
      		error.msg = "<%=resources.getString("GML.ThisFormContains")%> 1 <%=resources.getString("GML.error")%> : \n" + error.msg;
          jQuery.popup.error(error.msg);
        	break;
    	default :
      		error.msg = "<%=resources.getString("GML.ThisFormContains")%> " + error.nb + " <%=resources.getString("GML.errors")%> :\n" + error.msg;
          jQuery.popup.error(error.msg);
 	}
 	return (error.nb == 0);
}

function saveNews() {
	if (isCorrectForm()) {
    sp.editor.wysiwyg.lastBackupManager.clear();
      <view:pdcPositions setIn="document.newsForm.Positions.value"/>;
   		$("#newsForm").submit();
	}
}

function publish() {
  if (isCorrectForm()) {
    sp.editor.wysiwyg.lastBackupManager.clear();
    <view:pdcPositions setIn="document.newsForm.Positions.value"/>;
	  $("#newsForm").attr("action", "SaveAndPublish");
	  $("#newsForm").submit();
  }
}

function abortNews() {
  sp.editor.wysiwyg.lastBackupManager.clear();
  $("#newsForm").attr("action", "Remove");
  $("#newsForm").submit();
}

function onDelete(id) {
  sp.editor.wysiwyg.lastBackupManager.clear();
  location.href="Main";
}

function cancel(id) {
  sp.editor.wysiwyg.lastBackupManager.clear();
  location.href="Main";
}

$(document).ready(function() {
	<view:wysiwyg replace="editorContent" language="<%=language%>" width="98%" height="300"
	toolbar="quickInfo" displayFileBrowser="${true}" componentId="${curQuickInfo.componentInstanceId}"
	objectId="${curQuickInfo.publicationId}" activateWysiwygBackupManager="true"/>
});
</script>
</head>
<body id="quickinfo">
<div id="<%=componentId %>">
<fmt:message var="browseBarMsg" key="edition"/>
<c:if test="${newOneInProgress}">
<fmt:message var="browseBarMsg" key="creation.inProgress"/>
</c:if>
<view:browseBar extraInformations="${browseBarMsg}" />
<c:if test="${not newOneInProgress}">
	<view:operationPane>
	  <fmt:message var="deleteMsg" key="GML.delete"/>
	  <fmt:message var="deleteConfirmMsg" key="supprimerQIConfirmation"/>
	  <view:operation altText="${deleteMsg}" icon="${deleteIconUrl}" action="javascript:onclick=confirmDelete('${curQuickInfo.id}', '${curQuickInfo.componentInstanceId}', '${deleteConfirmMsg}', onDelete)"/>
	</view:operationPane>
</c:if>

<view:window>
<form name="newsForm" id="newsForm" action="Save" method="post" enctype="multipart/form-data" accept-charset="UTF-8">
  <input type="hidden" name="Positions" />
  <c:if test="${not empty curQuickInfo}">
    <input type="hidden" name="Id" value="${curQuickInfo.id}"/>
    <input type="hidden" name="PubId" value="${curQuickInfo.publicationId}"/>
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
        <input type="text" name="Name" size="50" id="Name" maxlength="400" value="<view:encodeHtmlParagraph string="${curName}"/>" />
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

    <div class="field" id="keywordsArea">
      <label class="txtlibform" for="keywords"><fmt:message key="quickinfo.news.keywords" /></label>
      <div class="champs">
        <c:if test="${not empty curQuickInfo}"><c:set var="curKeywords" value="${curQuickInfo.keywords}"/></c:if>
        <input type="text" name="Keywords" id="keywords" value="${curKeywords}" size="68" maxlength="1000" />
      </div>
    </div>
    
    <div class="field" id="broadcastArea">
      <label class="txtlibform"><fmt:message key="quickinfo.news.broadcast.mode" /> </label>
      <div class="champs">
      	<input type="checkbox" name="BroadcastImportant" value="true" <%=broadcastMajorChecked %>/> <fmt:message key="quickinfo.news.broadcast.mode.major" />
      	<c:if test="${appSettings.broadcastingByTicker}">
      		<input type="checkbox" name="BroadcastTicker" value="true" <%=broadcastTickerChecked %>/> <fmt:message key="quickinfo.news.broadcast.mode.ticker" />
      	</c:if>
      	<c:if test="${appSettings.broadcastingByBlockingNews}">
      		<input type="checkbox" name="BroadcastMandatory" value="true" <%=broadcastBlockingChecked %>/> <fmt:message key="quickinfo.news.broadcast.mode.blocking" />
      	</c:if>
      </div>
    </div>

    <div class="field" id="contentArea">
      <label class="txtlibform" for="content"><fmt:message key="quickinfo.news.content" /> </label>
      <div class="champs">
        <textarea name="editorContent" id="editorContent" rows="50" cols="10"><%=codeHtml%></textarea>
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
		        		<span class="txtsublibform">&nbsp;<%=resources.getString("quickinfo.ToHour")%>&nbsp;</span>
						<input id="beginHour" class="inputHour" type="text" name="BeginHour" value="<%=beginHour%>" size="5" maxlength="5" /> <i>(hh:mm)</i>
		      		</div>
	    		</div>
	    		<div class="field" id="EndDateArea">
	      			<label for="EndDate" class="txtlibform"><fmt:message key="GML.dateEnd" /></label>
	      			<div class="champs">
	        			<input type="text" class="dateToPick" id="EndDate" name="EndDate" size="12" value="<%=endDate%>" maxlength="<%=DBUtil.getDateFieldLength()%>"/>
	        			<span class="txtsublibform">&nbsp;<%=resources.getString("quickinfo.ToHour")%>&nbsp;</span>
						<input id="endHour" class="inputHour" type="text" name="EndHour" value="<%=endHour %>" size="5" maxlength="5" /> <i>(hh:mm)</i>
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
			<viewTags:displayThumbnail thumbnail="<%=thumbnail %>" mandatory="${thumbnailSettings.mandatory}" width="${thumbnailSettings.width}" height="${thumbnailSettings.height}"/>
		</fieldset>
	</div>
</div>

<c:if test="${newOneInProgress}">
  <view:fileUpload fieldset="true" jqueryFormSelector="form[name='newsForm']" />
</c:if>

<c:choose>
<c:when test="${empty curQuickInfo.taxonomyPositions}">
	<view:pdcNewContentClassification componentId="<%=quickinfo.getComponentId()%>" />
</c:when>
<c:otherwise>
	<view:pdcClassification componentId="<%=quickinfo.getComponentId()%>" contentId="${curQuickInfo.publicationId}" editable="true" externalManagement="true" />
</c:otherwise>
</c:choose>

<div class="legend">
  <fmt:message key="GML.requiredField" /> : <img src="<%=m_context%>/util/icons/mandatoryField.gif" width="5" height="5" />
</div>

<view:buttonPane>
<c:choose>
<c:when test="${curQuickInfo.draft}">
	<view:button label="${buttonPublish}" action="javascript:onclick=publish()"/>
	<view:button label="${buttonSaveDraft}" action="javascript:onclick=saveNews()"/>
</c:when>
<c:otherwise>
	<view:button label="${buttonOK}" action="javascript:onclick=saveNews()">
    <view:handleContributionManagementContext
        contributionId="<%=news != null ? news.getIdentifier() : null%>"
        jsValidationCallbackMethodName="isCorrectForm"
        contributionIndexable="true"/>
  </view:button>
</c:otherwise>
</c:choose>
<view:button label="${buttonCancel}" action="javascript:onclick=cancel()"/>
</view:buttonPane>
    
</view:frame>
</form>

</view:window>
</div>
</body>
</html>