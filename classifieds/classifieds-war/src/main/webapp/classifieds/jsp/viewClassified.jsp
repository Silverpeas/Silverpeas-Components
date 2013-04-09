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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>

<%@page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.silverpeas.util.EncodeHelper"%>
<%@page import="com.silverpeas.form.Form"%>
<%@page import="com.silverpeas.form.PagesContext"%>
<%@page import="com.silverpeas.form.DataRecord"%>
<%@page import="com.silverpeas.classifieds.model.ClassifiedDetail"%>
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

<c:set var="isDraftEnabled" value="${requestScope.IsDraftEnabled}" />
<c:set var="isCommentsEnabled" value="${requestScope.IsCommentsEnabled}" />
<c:set var="profile" value="${requestScope.Profile}" />
<c:set var="creationDate" value="${requestScope.CreationDate}" />
<c:set var="updateDate" value="${requestScope.UpdateDate}" />
<c:set var="validationDate" value="${requestScope.ValidateDate}" />
<c:set var="userId" value="${requestScope.UserId}" />
<c:set var="classified" value="${requestScope.Classified}" />
<c:set var="instanceId" value="${classified.instanceId}" />
<c:set var="creatorId" value="${classified.creatorId}" />
<c:set var="xmlForm" value="${requestScope.Form}" />
<c:set var="xmlData" value="${requestScope.Data}" />
<c:set var="xmlContext" value="${requestScope.Context}" />
<c:set var="title" value="${classified.title}" />
<c:set var="description" value="${classified.description}" />
<c:set var="displayedTitle"><view:encodeHtml string="${title}" /></c:set>
<c:set var="displayedDescription"><view:encodeHtml string="${description}" /></c:set>
<%
String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel />
<view:includePlugin name="messageme"/>
<script type="text/javascript" src="<c:url value='/util/javaScript/animation.js'/>"></script>
<fmt:message var="deletionConfirm" key="classifieds.confirmDeleteClassified" />
<script type="text/javascript">
	function deleteConfirm(id) {
		// confirmation de suppression de l'annonce
		if (window.confirm("<c:out value='${deletionConfirm}'/>")) {
			document.classifiedForm.action = "DeleteClassified";
			document.classifiedForm.ClassifiedId.value = id;
			document.classifiedForm.submit();
		}
	}

	function updateClassified(id) {
		document.classifiedForm.action = "EditClassified?ClassifiedId="+id;
		document.classifiedForm.ClassifiedId.value = id;
		document.classifiedForm.submit();
	}

	function draftIn(id) {
		location.href = "<view:componentUrl componentId='${instanceId}'/>DraftIn?ClassifiedId=" + id;
	}

	function draftOut(id) {
		location.href = "<view:componentUrl componentId='${instanceId}'/>DraftOut?ClassifiedId=" + id;
	}

	function validate(id) {
		location.href = "<view:componentUrl componentId='${instanceId}'/>ValidateClassified?ClassifiedId=" + id;
	}

	function refused(id) {
		// open modal dialog
		$("#refusalModalDialog").dialog({
			modal: true,
			resizable: false,
			width: 600,
			buttons: {
				"<fmt:message key="GML.ok"/>": function() {
					sendRefusalForm();
				},
				"<fmt:message key="GML.cancel"/>": function() {
					$( this ).dialog( "close" );
				}
			}
		});
	}
	
	function sendRefusalForm() {
		if (isRefusalFormOK()) {
			document.refusalForm.submit();
		}
	}

	function isRefusalFormOK() {
		var errorMsg = "";
		var errorNb = 0;
		var motive = stripInitialWhitespace(document.refusalForm.Motive.value);
		if (isWhitespace(motive)) {
			errorMsg += "  - '<fmt:message key="classifieds.refusalMotive"/>' <fmt:message key="GML.MustBeFilled"/>\n";
			errorNb++;
		}
		switch (errorNb) {
		case 0:
			result = true;
			break;
		case 1:
			errorMsg = "<fmt:message key="GML.ThisFormContains"/> 1 <fmt:message key="GML.error"/> : \n"
					+ errorMsg;
			window.alert(errorMsg);
			result = false;
			break;
		default:
			errorMsg = "<fmt:message key="GML.ThisFormContains"/> " + errorNb
					+ " <fmt:message key="GML.errors"/> :\n" + errorMsg;
			window.alert(errorMsg);
			result = false;
			break;
		}
		return result;
	}
	
	$(document).ready(function() {

		  $('.classified_thumbs a').click(function() {
		        cheminImage=$(this).children('img').attr('src');
		        $('.selected').removeClass('selected');
		        $(this).addClass('selected');
		        $('.classified_selected_photo img').attr('src',cheminImage);
		        $('.classified_selected_photo a').attr('href',"javascript:onClick=openImage('"+cheminImage+"')");
		  });
		});
	
	function openImage(url) {
		  SP_openWindow(url,'image','700','500','scrollbars=yes, noresize, alwaysRaised');
  }
</script>
</head>
<body id="classifieds">
<div id="${instanceId}">
	<fmt:message var="classifiedPath" key="classifieds.classified" />
	<view:browseBar>
		<view:browseBarElt label="${classifiedPath}" link="" />
	</view:browseBar>
	<c:if test="${userId == creatorId or profile.name == 'admin'}">
		<c:if test="${'Unpublished' == classified.status}">
			<fmt:message var="updateOp" key="classifieds.republishClassified" />
		</c:if>
		<c:if test="${'Unpublished' != classified.status}">
			<fmt:message var="updateOp" key="classifieds.updateClassified" />
		</c:if>

		<fmt:message var="updateIcon" key="classifieds.update"
			bundle="${icons}" />
		<fmt:message var="deleteOp" key="classifieds.deleteClassified" />
		<fmt:message var="deleteIcon" key="classifieds.delete"
			bundle="${icons}" />
		<view:operationPane>
			<view:operation
				action="javascript:updateClassified('${classified.classifiedId}');"
				altText="${updateOp}" icon="${updateIcon}" />
			<view:operation
				action="javascript:deleteConfirm('${classified.classifiedId}');"
				altText="${deleteOp}" icon="${deleteIcon}" />

			<c:if test="${isDraftEnabled}">
				<view:operationSeparator />
				<c:choose>
					<c:when test="${'Draft' == classified.status}">
						<fmt:message var="draftOutOp" key="classifieds.draftOut" />
						<fmt:message var="draftOutIcon" key="classifieds.draftOut"
							bundle="${icons}" />
						<view:operation
							action="javascript:draftOut('${classified.classifiedId}');"
							altText="${draftOutOp}" icon="${draftOutIcon}" />
					</c:when>
					<c:otherwise>
						<fmt:message var="draftInOp" key="classifieds.draftIn" />
						<fmt:message var="draftInIcon" key="classifieds.draftIn"
							bundle="${icons}" />
						<view:operation
							action="javascript:draftIn('${classified.classifiedId}');"
							altText="${draftInOp}" icon="${draftInIcon}" />
					</c:otherwise>
				</c:choose>
			</c:if>
			<c:if
				test="${'admin' == profile.name and 'ToValidate' == classified.status}">
				<view:operationSeparator />
				<fmt:message var="validateOp" key="classifieds.validate" />
				<fmt:message var="validateIcon" key="classifieds.validate"
					bundle="${icons}" />
				<fmt:message var="refuseOp" key="classifieds.refused" />
				<fmt:message var="refuseIcon" key="classifieds.refused"
					bundle="${icons}" />
				<view:operation
					action="javascript:validate('${classified.classifiedId}');"
					altText="${validateOp}" icon="${validateIcon}" />
				<view:operation
					action="javascript:refused('${classified.classifiedId}');"
					altText="${refuseOp}" icon="${refuseIcon}" />
			</c:if>
		</view:operationPane>
	</c:if>

	<view:window>
		<view:frame>
			  <table cellpadding="5" width="100%">
			  <tr>
            <td valign="top"> 
              <div id="header_classifieds"> </div>
              <div class="rightContent">
                <div class="bgDegradeGris" id="classified_info">
                  <div class="paragraphe" id="classified_info_creation">
                   <fmt:message key="classifieds.online" /></BR>
                   <c:if test="${fn:length(validationDate) > 0}">
                     <b><c:out value="${validationDate}" /></b>&nbsp;
                   </c:if>
                   <c:if test="${empty validationDate}">
                      <b><c:out value="${creationDate}" /></b>&nbsp;
                   </c:if>
                   <fmt:message key="classifieds.by" />&nbsp;
                    <script language="Javascript" src="${pageContext.request.contextPath}/util/javaScript/silverpeas-profile.js" type="text/javascript"></script>
                    <script language="Javascript" src="${pageContext.request.contextPath}/util/javaScript/silverpeas-messageme.js" type="text/javascript"></script>
                    <script language="Javascript" src="${pageContext.request.contextPath}/util/javaScript/silverpeas-invitme.js" type="text/javascript"></script>
                    <script language="Javascript" src="${pageContext.request.contextPath}/util/javaScript/silverpeas-userZoom.js" type="text/javascript"></script>
                    <span class="userToZoom" rel="${classified.creatorId}">${classified.creatorName}</span>
                    <div class="profilPhoto"><img class="defaultAvatar" alt="" src="${pageContext.request.contextPath}${classified.creator.avatar}"></div></BR>

									 <c:if test="${fn:length(updateDate) > 0}">
									   <fmt:message key="classifieds.updateDate" /> : <b><c:out value="${updateDate}" /></b></BR>
									 </c:if>
									</div>
									<div id="classified_contact_link" class="bgDegradeGris">
									 <a rel="${classified.creatorId},${classified.creatorName}" class="link notification" href="#"><fmt:message key="classifieds.contactAdvertiser"/></a>
									</div>
                  <p></p>
                </div>
              </div>
              <div class="principalContent">
                <div id="menubar-creation-actions"></div>
                <div class="classified_fiche">
                  <h2 class="classified_title">${displayedTitle}</h2>
                  <div class="classified_photos">
                    <div class="classified_thumbs">
                    <%
                    int i = 0;
                    %>
		                <c:forEach var="image" items="${classified.images}">
		                <%
		                SimpleDocument simpleDocument = (SimpleDocument) pageContext.getAttribute("image");
		                String url = m_context +  simpleDocument.getAttachmentURL();
		                String select = "";
		                if (i == 0) {
		                  select = "class=\"selected\"";
		                }
		                %>
		                  <a <%=select%> href="#"><img src="<%=url%>"></a>
		                <%
		                i++;
		                %>
		                </c:forEach>
                    </div>
                    <c:if test="${not empty classified.images}">
                    <div class="classified_selected_photo">
                    <c:forEach var="image" items="${classified.images}" begin="0" end="0">
                    <%
                    SimpleDocument simpleDocument = (SimpleDocument) pageContext.getAttribute("image");
                    String url = m_context +  simpleDocument.getAttachmentURL();
                    %>
                      <a href="javascript:onClick=openImage('<%=url%>')"><img src="<%=url%>"></a>
                    </c:forEach>
                    </div>
                    </c:if>
                  </div>
                  <c:if test="${classified.price > 0}">
                    <div class="classified_price">${classified.price} &euro;</div>
                  </c:if>  
                  <p class="classified_description">${displayedDescription}</p>
                  
                  <!-- <hr class="clear" /> -->
                  <c:if test="${not empty xmlForm}">
                     <div id="classified_content_form">
                <%
                  Form xmlForm = (Form) pageContext.getAttribute("xmlForm");
                  DataRecord data = (DataRecord) pageContext.getAttribute("xmlData");
                  PagesContext context = (PagesContext) pageContext.getAttribute("xmlContext");

                  xmlForm.display(out, context, data);
                %>
                      <hr class="clear" />
                    </div>
                  </c:if>
                </div>
              </div>
             </td>
          </tr>
          
				<tr>
					<td>
						<!--Afficher les commentaires-->
						<c:if test="${isCommentsEnabled}">
							<view:comments 	userId="${userId}" componentId="${instanceId}"
											resourceType="${classified.contributionType}" resourceId="${classified.classifiedId}" />
						</c:if>
					</td>
				</tr>
			</table>
		</view:frame>
	</view:window>
	<form name="classifiedForm" action="" method="post">
		<input type="hidden" name="ClassifiedId" />
	</form>
	<div id="refusalModalDialog" title="${refuseOp}" style="display: none;">
		<form name="refusalForm" action="RefusedClassified" method="post">
			<table>
				<tr>
					<td>
						<table>
							<tr>
								<td class="txtlibform"><fmt:message key="classifieds.number" /> :</td>
								<td>${classified.classifiedId} <input type="hidden" name="ClassifiedId" value="${classified.classifiedId}"/></td>
							</tr>
							<tr>
								<td class="txtlibform"><fmt:message key="GML.title" /> :</td>
								<td valign="top">${displayedTitle}</td>
							</tr>
							<tr>
								<td class="txtlibform" valign="top"><fmt:message key="classifieds.refusalMotive" /> :</td>
								<td><textarea name="Motive" rows="5" cols="55"></textarea>&nbsp;<img border="0" src="${pageContext.request.contextPath}<fmt:message key="classifieds.mandatory" bundle="${icons}"/>" width="5" height="5"/></td>
							</tr>
							<tr>
								<td colspan="2">( <img border="0" src="${pageContext.request.contextPath}<fmt:message key="classifieds.mandatory" bundle="${icons}"/>" width="5" height="5"/> : <fmt:message key="GML.requiredField" /> )</td>
							</tr>
						</table></td>
				</tr>
			</table>
		</form>
	</div>
</div>	
</body>
</html>
