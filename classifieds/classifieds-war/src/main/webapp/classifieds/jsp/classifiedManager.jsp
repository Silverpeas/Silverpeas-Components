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

<%@page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@page import="com.silverpeas.form.Form"%>
<%@page import="com.silverpeas.form.PagesContext"%>
<%@page import="com.silverpeas.form.DataRecord"%>
<%@page import="org.silverpeas.attachment.model.SimpleDocument"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
	response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<c:set var="language" value="${requestScope.resources.language}"/>

<fmt:setLocale value="${language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<c:set var="browseContext" value="${requestScope.browseContext}" />
<c:set var="componentLabel" value="${browseContext[1]}" />

<c:set var="classified" value="${requestScope.Classified}" />
<c:set var="userName" value="${requestScope.UserName}" />
<c:set var="userEmail" value="${requestScope.UserEmail}" />
<c:set var="userId" value="${requestScope.UserId}" />

<c:set var="formUpdate" value="${requestScope.Form}" />
<c:set var="data" value="${requestScope.Data}" />
<c:set var="fieldKey" value="${requestScope.FieldKey}" />
<c:set var="fieldName" value="${requestScope.FieldName}" />

<c:set var="action" value="${(not empty classified) ? 'UpdateClassified' : 'CreateClassified' }" />
<c:set var="creatorName" value="${(not empty classified) ? classified.creatorName : userName }" />
<c:set var="creatorEmail" value="${(not empty classified) ? classified.creatorEmail : userEmail }" />

<c:if test="${not empty classified}">
	<c:set var="classifiedId" value="${classified.classifiedId}" />
	<c:set var="title" value="${classified.title}" />
	<c:set var="description" value="${classified.description}" />
	<c:set var="price" value="${classified.price}" />
	<c:set var="instanceId" value="${classified.instanceId}" />
	<c:set var="creatorId" value="${classified.creatorId}" />
	<c:set var="status" value="${classified.status}" />
	<c:set var="validatorId" value="${classified.validatorId}" />
	<c:set var="validatorName" value="${classified.validatorName}" />
	<c:set var="creationDate" value="${classified.creationDate}" />
	<c:set var="validateDate" value="${classified.validateDate}" />
	<c:set var="updateDate" value="${classified.updateDate}" />
	<c:set var="images" value="${classified.images}" />
</c:if>

<%
  String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
	String language = (String) pageContext.getAttribute("language");
	String instanceId = (String) pageContext.getAttribute("instanceId");
	Form formUpdate = (Form) pageContext.getAttribute("formUpdate");
	DataRecord data = (DataRecord) pageContext.getAttribute("data");

	PagesContext context = new PagesContext("classifiedForm", "11", language, false, instanceId, null, null);
	context.setIgnoreDefaultValues(true);
	context.setBorderPrinted(false);
%>

<html>
<head>
<link type="text/css" href="<%=m_context%>/util/styleSheets/fieldset.css" rel="stylesheet" />
<view:looknfeel/>
<c:if test="${not empty formUpdate}">
	<%
  	formUpdate.displayScripts(out, context);
	%>
</c:if>
<script type="text/javascript" src="${pageContext.request.contextPath}/util/javaScript/checkForm.js"></script>

<fmt:message var="GML_title" key="GML.title"/>
<fmt:message var="GML_MustBeFilled" key="GML.MustBeFilled"/>
<fmt:message var="GML_description" key="GML.description"/>
<fmt:message var="classifieds_msgSize" key="classifieds.msgSize"/>
<fmt:message var="classifieds_price" key="classifieds.price"/>
<fmt:message var="GML_MustContainsNumber" key="GML.MustContainsNumber"/>
<fmt:message var="classifieds_image" key="classifieds.image"/>
<fmt:message var="classifieds_imageFormat" key="classifieds.imageFormat"/>
<fmt:message var="GML_ThisFormContains" key="GML.ThisFormContains"/>
<fmt:message var="GML_error" key="GML.error"/>
<fmt:message var="GML_errors" key="GML.errors"/>

<script type="text/javascript">

	// form validation
	function sendData()
	{
		<c:if test="${not empty formUpdate}">
			if (isCorrectLocalForm() && isCorrectForm()) {
		    	document.classifiedForm.submit();
		    }
		 </c:if>
		<c:if test="${empty formUpdate}">
				if (isCorrectLocalForm()) {
					document.classifiedForm.submit();
		    	}
		</c:if>
	}

	function isCorrectLocalForm()
	{
		  var errorMsg = "";
	   	var errorNb = 0;
	   	var title = stripInitialWhitespace(document.classifiedForm.Title.value);
	   	var description = stripInitialWhitespace(document.classifiedForm.Description.value);

	    if (title == "") {
			 errorMsg+="  - '${GML_title}' ${GML_MustBeFilled}\n";
		   errorNb++;
		  }
	   	
	   	if (description == "") {
	     errorMsg+="  - '${GML_description}' ${GML_MustBeFilled}\n";
	     errorNb++;
	    }
	    if (description.length > 4000) {
	     errorMsg+="  - '${GML_description}' ${classifieds_msgSize}\n";
	     errorNb++;
	    }
	    if (! isInteger(document.classifiedForm.Price.value)) {
	    	errorMsg+="  - '${classifieds_price}' ${GML_MustContainsNumber}\n";
	      errorNb++;
	    }
	    
	    if (!isWhitespace(document.classifiedForm.Image1.value)) {
	     var verif = /[.][jpg,gif,bmp,tiff,tif,jpeg,png,JPG,GIF,BMP,TIFF,TIF,JPEG,PNG]{3,4}$/;
       if (verif.exec(document.classifiedForm.Image1.value) == null) {
        errorMsg+="  - '${classifieds_image}1' : ${classifieds_imageFormat}\n";
        errorNb++;
       }
	    }
	    
	    if (!isWhitespace(document.classifiedForm.Image2.value)) {
       var verif = /[.][jpg,gif,bmp,tiff,tif,jpeg,png,JPG,GIF,BMP,TIFF,TIF,JPEG,PNG]{3,4}$/;
       if (verif.exec(document.classifiedForm.Image2.value) == null) {
        errorMsg+="  - '${classifieds_image}2' : ${classifieds_imageFormat}\n";
        errorNb++;
       }
	    }
	    
	    if (!isWhitespace(document.classifiedForm.Image3.value)) {
       var verif = /[.][jpg,gif,bmp,tiff,tif,jpeg,png,JPG,GIF,BMP,TIFF,TIF,JPEG,PNG]{3,4}$/;
       if (verif.exec(document.classifiedForm.Image3.value) == null) {
        errorMsg+="  - '${classifieds_image}3' : ${classifieds_imageFormat}\n";
        errorNb++;
       }
	    }
	    if (!isWhitespace(document.classifiedForm.Image4.value)) {
	        var verif = /[.][jpg,gif,bmp,tiff,tif,jpeg,png,JPG,GIF,BMP,TIFF,TIF,JPEG,PNG]{3,4}$/;
	        if (verif.exec(document.classifiedForm.Image4.value) == null) {
	         errorMsg+="  - '${classifieds_image}4' : ${classifieds_imageFormat}\n";
	         errorNb++;
	        }
	    }
	   	switch(errorNb)
	   	{
	       	case 0 :
	           	result = true;
	           	break;
	       	case 1 :
	           	errorMsg = "${GML_ThisFormContains} 1 ${GML_error} : \n" + errorMsg;
	           	window.alert(errorMsg);
	           	result = false;
	           	break;
	       	default :
	           	errorMsg = "${GML_ThisFormContains} " + errorNb + " ${GML_errors} :\n" + errorMsg;
	           	window.alert(errorMsg);
	           	result = false;
	           	break;
	   	}
	   	return result;
	}

	function setData()
	{
		<c:if test="${not empty fieldName}">
	      document.classifiedForm.${fieldName}.value = '${fieldKey}';
	    </c:if>
	}
	
	function hideImageFile(idElement) {
		document.getElementById("imageFile"+idElement).style.visibility = "hidden";
		document.classifiedForm["RemoveImageFile"+idElement].value = "yes";
	}

</script>

</head>
<body onload="setData()">
	<fmt:message var="classifiedPath"
		key="${ (action eq 'CreateClassified') ? 'classifieds.addClassified' : 'classifieds.updateClassified'}" />
	<view:browseBar>
		<view:browseBarElt label="${classifiedPath}" link="" />
	</view:browseBar>

<view:window>
<view:frame>

<c:set var="displayedTitle"><view:encodeHtml string="${title}" /></c:set>
<c:set var="displayedDescription"><view:encodeHtml string="${description}" /></c:set>
<c:set var="displayedPrice">
<c:if test="${price > 0}">
<view:encodeHtml string="${price}" />
</c:if>
</c:set>
<c:set var="displayedId"><view:encodeHtml string="${classifiedId}" /></c:set>
<c:set var="displayedEmail"><view:encodeHtml string="${creatorEmail}" /></c:set>

<form name="classifiedForm" class="classifiedForm" action="${action}" method="post" enctype="multipart/form-data" onsubmit="sendData();return false;">
<fieldset id="classifiedInfo" class="skinFieldset">
<legend><fmt:message key="classifieds.mainInfos"/></legend>
<div class="fields">
        
  <c:if test="${action eq 'UpdateClassified'}">
    <div class="field" id="classifiedNumberArea">
      <label for="classifiedNumber" class="txtlibform"><fmt:message key="classifieds.number"/> :</label>
      <div class="champs">
        ${displayedId}
    </div>
	</c:if>
	
  <div class="field" id="classifiedNameArea">
	  <label for="classifiedName" class="txtlibform"><fmt:message key="GML.title"/> :</label>
	  <div class="champs">
	    <input type="text" name="Title" id="classifiedName" size="60" maxlength="100" value="${displayedTitle}"/>
	    &nbsp;<img src="${pageContext.request.contextPath}<fmt:message key="classifieds.mandatory" bundle="${icons}"/>" width="5" height="5" border="0"/>
	    <input type="hidden" name="ClassifiedId" value="${displayedId}"/>
	  </div>
	</div>
	
	<div class="field" id="descriptionArea">
    <label for="classifiedDesc" class="txtlibform"><fmt:message key="GML.description"/> :</label>
    <div class="champs">
      <textarea cols="100" rows="5" name="Description" id="classifiedDesc">${displayedDescription}</textarea>
      &nbsp;<img src="${pageContext.request.contextPath}<fmt:message key="classifieds.mandatory" bundle="${icons}"/>" width="5" height="5" border="0"/>
    </div>
  </div>
  
  <div class="field" id="priceArea">
	  <label for="classifiedPrice" class="txtlibform"><fmt:message key="classifieds.price"/> :</label>
	  <div class="champs">
	    <input type="text" name="Price" size="10" maxlength="8" id="classifiedPrice" value="${displayedPrice}"/> &euro;
	  </div>
	</div>
	
  <c:if test="${action eq 'UpdateClassified'}">
    <div class="field" id="creationDateArea">
      <label class="txtlibform"><fmt:message key="classifieds.creationDate"/> :</label>
      <div class="champs">
        <view:formatDateTime value="${creationDate}"/> <fmt:message key="classifieds.by"/> ${creatorName} (${displayedEmail})
      </div>
    </div>
  </c:if>
  <c:if test="${not empty updateDate}">
    <div class="field" id="updateDateArea">
      <label class="txtlibform"><fmt:message key="classifieds.updateDate"/> :</label>
      <div class="champs">
        <view:formatDateTime value="${updateDate}"/>
      </div>
    </div>
  </c:if>
  <c:if test="${(not empty validateDate) && (not empty validatorName)}">
    <div class="field" id="validationDateArea">
      <label class="txtlibform"><fmt:message key="classifieds.validateDate"/> :</label>
      <div class="champs">
        <view:formatDateTime value="${validateDate}" /> <fmt:message key="classifieds.by"/> ${validatorName}
      </div>
    </div>
  </c:if>
  
  <div class="field" id="mandatoryArea">
    <label class="txtlibform">(<img src="${pageContext.request.contextPath}<fmt:message key="classifieds.mandatory" bundle="${icons}" />" width="5" height="5"/> : <fmt:message key="GML.requiredField"/>)</label>
  </div>
    
  
  </div>    
</fieldset>

<div class="table">      
  <div class="cell">
    <fieldset id="pubThumb" class="skinFieldset">
    <legend><fmt:message key="classifieds.images"/></legend>
    <div class="fields">
    
	   <div class="field thumb">
	     <c:forEach var="image" items="${images}" begin="0" end="0">
       <div class="thumbnailPreviewAndActions" id="imageFile1">
         <div class="thumbnailPreview">
          <%
          SimpleDocument simpleDocument = (SimpleDocument) pageContext.getAttribute("image");
          String url = m_context +  simpleDocument.getAttachmentURL();
          %>
          <img src="<%=url%>" class="thumbnail" id="actualImage1"/>
         </div>
         <div id="thumbnailActions">
          <a href="javascript:onClick=hideImageFile('1');"><img src="${pageContext.request.contextPath}<fmt:message key="classifieds.crossDelete" bundle="${icons}"/>" border="0"></a>
          <input type="hidden" name="IdImage1" value="${image.PK.id}"> 
         </div>
       </div>
       </c:forEach>
     
       <div class="thumbnailInputs">
         <input type="file" name="Image1" size="30" id="Image1"/><br/> <i>(<fmt:message key="classifieds.mainImage"/>)</i>
         <input type="hidden" name="RemoveImageFile1" value="no"/>
       </div>
     </div>
       
     <div class="field thumb">
       <c:forEach var="image" items="${images}" begin="1" end="1">
       <div class="thumbnailPreviewAndActions" id="imageFile2">
         <div class="thumbnailPreview">
          <%
          SimpleDocument simpleDocument = (SimpleDocument) pageContext.getAttribute("image");
          String url = m_context +  simpleDocument.getAttachmentURL();
          %>
          <img src="<%=url%>" class="thumbnail" id="actualImage2"/>
         </div>
         <div id="thumbnailActions">
          <a href="javascript:onClick=hideImageFile('2');"><img src="${pageContext.request.contextPath}<fmt:message key="classifieds.crossDelete" bundle="${icons}"/>" border="0"></a>
          <input type="hidden" name="IdImage2" value="${image.PK.id}"> 
         </div>
       </div>
       </c:forEach>
     
       <div class="thumbnailInputs">
         <input type="file" name="Image2" size="30" id="Image1"/>
         <input type="hidden" name="RemoveImageFile2" value="no"/>
       </div>
     </div>
     
     <div class="field thumb">
       <c:forEach var="image" items="${images}" begin="2" end="2">
       <div class="thumbnailPreviewAndActions" id="imageFile3">
         <div class="thumbnailPreview">
          <%
          SimpleDocument simpleDocument = (SimpleDocument) pageContext.getAttribute("image");
          String url = m_context +  simpleDocument.getAttachmentURL();
          %>
          <img src="<%=url%>" class="thumbnail" id="actualImage3"/>
         </div>
         <div id="thumbnailActions">
          <a href="javascript:onClick=hideImageFile('3');"><img src="${pageContext.request.contextPath}<fmt:message key="classifieds.crossDelete" bundle="${icons}"/>" border="0"></a>
          <input type="hidden" name="IdImage3" value="${image.PK.id}"> 
         </div>
       </div>
       </c:forEach>
     
       <div class="thumbnailInputs">
         <input type="file" name="Image3" size="30" id="Image3"/>
         <input type="hidden" name="RemoveImageFile3" value="no"/>
       </div>
     </div>
     
     <div class="field thumb">
       <c:forEach var="image" items="${images}" begin="3" end="3">
       <div class="thumbnailPreviewAndActions" id="imageFile4">
         <div class="thumbnailPreview">
          <%
          SimpleDocument simpleDocument = (SimpleDocument) pageContext.getAttribute("image");
          String url = m_context +  simpleDocument.getAttachmentURL();
          %>
          <img src="<%=url%>" class="thumbnail" id="actualImage4"/>
         </div>
         <div id="thumbnailActions">
          <a href="javascript:onClick=hideImageFile('4');"><img src="${pageContext.request.contextPath}<fmt:message key="classifieds.crossDelete" bundle="${icons}"/>" border="0"></a>
          <input type="hidden" name="IdImage4" value="${image.PK.id}"> 
         </div>
       </div>
       </c:forEach>
     
       <div class="thumbnailInputs">
         <input type="file" name="Image4" size="30" id="Image4"/>
         <input type="hidden" name="RemoveImageFile4" value="no"/>
       </div>
     </div>
    </div>
    </fieldset>
  </div>
  <div class="cell">
    <fieldset id="specifiedInfo" class="skinFieldset">
    <legend><fmt:message key="classifieds.specificInfos"/></legend>
    <div class="fields">
			<c:if test="${not empty formUpdate}">
			<!-- AFFICHAGE du formulaire -->
			<%
			formUpdate.display(out, context, data);
			%>
			</c:if>
		</div>
  </div>
</div>			
</form>
<center>
<view:buttonPane>
	<fmt:message var="validateLabel" key="GML.validate"/>
	<fmt:message var="cancelLabel" key="GML.cancel"/>

	<view:button label="${validateLabel}" action="javascript:onClick=sendData();" />
	<view:button label="${cancelLabel}" action="Main" />
</view:buttonPane>
</center>
</view:frame>
</view:window>
</body>
</html>