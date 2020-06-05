<%--

    Copyright (C) 2000 - 2020 Silverpeas

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

<%@page import="org.silverpeas.core.contribution.content.form.Form"%>
<%@page import="org.silverpeas.core.contribution.content.form.PagesContext"%>
<%@page import="org.silverpeas.core.contribution.content.form.DataRecord"%>
<%@ page import="org.silverpeas.core.notification.user.NotificationContext" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
	response.setHeader("Pragma", "no-cache"); //HTTP 1.0
	response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<c:set var="language" value="${requestScope.resources.language}"/>

<fmt:setLocale value="${language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<fmt:message var="messageToOwnerTitle" key="GML.notification.message"/>

<c:set var="browseContext" value="${requestScope.browseContext}" />
<c:set var="componentLabel" value="${browseContext[1]}" />

<c:set var="isDraftEnabled" value="${requestScope.IsDraftEnabled}" />
<c:set var="isCommentsEnabled" value="${requestScope.IsCommentsEnabled}" />
<c:set var="profile" value="${requestScope.Profile}" />
<c:set var="creationDate" value="${requestScope.CreationDate}" />
<c:set var="updateDate" value="${requestScope.UpdateDate}" />
<c:set var="validationDate" value="${requestScope.ValidateDate}" />
<c:set var="user" value="${requestScope.User}" />
<c:set var="classified" value="${requestScope.Classified}" />
<c:set var="instanceId" value="${classified.instanceId}" />
<c:set var="creatorId" value="${classified.creatorId}" />
<c:set var="xmlForm" value="${requestScope.Form}" />
<c:set var="xmlData" value="${requestScope.Data}" />
<c:set var="xmlContext" value="${requestScope.Context}" />
<c:set var="title" value="${classified.title}" />
<c:set var="description" value="${classified.description}" />
<c:set var="index" value="${requestScope.Index}"/>
<c:set var="currentScope" value="${requestScope.CurrentScope}"/>
<c:set var="displayedTitle"><view:encodeHtml string="${title}" /></c:set>
<c:set var="displayedDescription"><view:encodeHtmlParagraph string="${description}" /></c:set>
<c:set var="draftOperationsEnabled" value="${user.id == creatorId and isDraftEnabled}"/>
<c:set var="contributionIdKey"><%=NotificationContext.CONTRIBUTION_ID%></c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="${language}">
<head>
  <title></title>
<view:looknfeel />
<fmt:message var="deletionConfirm" key="classifieds.confirmDeleteClassified" />
<script type="text/javascript">
	function deleteConfirm() {
		// confirmation de suppression de l'annonce
    var label = "<c:out value='${deletionConfirm}'/>";
    jQuery.popup.confirm(label, function() {
			document.classifiedForm.action = "DeleteClassified";
			document.classifiedForm.submit();
		});
	}

	function updateClassified() {
		document.classifiedForm.action = "EditClassified?ClassifiedId=${classified.id}";
		document.classifiedForm.submit();
	}

	function draftIn() {
		location.href = "<view:componentUrl componentId='${instanceId}'/>DraftIn?ClassifiedId=${classified.id}";
	}

	function draftOut() {
		location.href = "<view:componentUrl componentId='${instanceId}'/>DraftOut?ClassifiedId=${classified.id}";
	}

	function validate() {
		location.href = "<view:componentUrl componentId='${instanceId}'/>ValidateClassified?ClassifiedId=${classified.id}";
	}

	function refused() {
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
		var errorMsg = "";
		var errorNb = 0;
		var motive = stripInitialWhitespace(document.refusalForm.Motive.value);
		if (isWhitespace(motive)) {
			errorMsg += "  - '<fmt:message key="classifieds.refusalMotive"/>' <fmt:message key="GML.MustBeFilled"/>\n";
			errorNb++;
		}
		switch (errorNb) {
		case 0:
      document.refusalForm.submit();
			break;
		case 1:
			errorMsg = "<fmt:message key="GML.ThisFormContains"/> 1 <fmt:message key="GML.error"/> : \n"
					+ errorMsg;
      jQuery.popup.error(errorMsg);
			break;
		default:
			errorMsg = "<fmt:message key="GML.ThisFormContains"/> " + errorNb
					+ " <fmt:message key="GML.errors"/> :\n" + errorMsg;
      jQuery.popup.error(errorMsg);
		}
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
  		var urlPart = "/size/250x/";
  		var i = url.indexOf(urlPart);
  		if (i != -1) {
    		url = url.substring(0, i) + url.substring(i+urlPart.length, url.length);
  		}
		SP_openWindow(url,'image','700','500','scrollbars=yes, noresize, alwaysRaised');
  	}

  function toNotify() {
    sp.messager.open('${instanceId}', {${contributionIdKey}: '${classified.id}'});
  }

  function toNotifyOwner() {
    $("#messageToOwnerDialog").popup('validation', {
      title : '${messageToOwnerTitle}',
      width : 'auto',
      buttonTextYes : '<fmt:message key="GML.ok"/>',
      buttonTextNo : '<fmt:message key="GML.cancel"/>',
      callback : sendMessageToOwner
    });
  }

  function sendMessageToOwner() {
    SilverpeasError.reset();
    var message = stripInitialWhitespace(document.messageToOwnerForm.Message.value);
    if (isWhitespace(message)) {
      SilverpeasError.add("<b><fmt:message key="GML.notification.message"/></b> <fmt:message key="GML.MustBeFilled"/>\n");
    }
    if (!SilverpeasError.show()) {
      document.messageToOwnerForm.submit();
      return true;
    }
    return false;
  }
</script>
</head>
<body id="classifieds">
<div id="${instanceId}">
  <view:browseBar>
    <c:choose>
      <c:when test="${currentScope == 1}">
        <fmt:message var="classifiedPath" key="classifieds.myClassifieds" />
        <view:browseBarElt label="${classifiedPath}" link="ViewMyClassifieds" />
      </c:when>
      <c:when test="${currentScope == 2}">
        <fmt:message var="classifiedPath" key="classifieds.viewClassifiedToValidate" />
        <view:browseBarElt label="${classifiedPath}" link="ViewClassifiedToValidate" />
      </c:when>
      <c:when test="${currentScope == 3}">
        <fmt:message var="classifiedPath" key="classifieds.classifiedsResult" />
        <view:browseBarElt label="${classifiedPath}" link="Pagination" />
      </c:when>
    </c:choose>
	</view:browseBar>
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
      <c:if test="${user.id == creatorId or profile.name == 'admin'}">
			<view:operation
				action="javascript:updateClassified();"
				altText="${updateOp}" icon="${updateIcon}" />
			<view:operation
				action="javascript:deleteConfirm();"
				altText="${deleteOp}" icon="${deleteIcon}" />

			<c:if test="${draftOperationsEnabled}">
				<view:operationSeparator />
				<c:choose>
					<c:when test="${classified.draft}">
						<fmt:message var="draftOutOp" key="GML.publish" />
						<fmt:message var="draftOutIcon" key="classifieds.draftOut"
							bundle="${icons}" />
						<view:operation
							action="javascript:draftOut();"
							altText="${draftOutOp}" icon="${draftOutIcon}" />
					</c:when>
					<c:otherwise>
						<fmt:message var="draftInOp" key="classifieds.draftIn" />
						<fmt:message var="draftInIcon" key="classifieds.draftIn"
							bundle="${icons}" />
						<view:operation
							action="javascript:draftIn();"
							altText="${draftInOp}" icon="${draftInIcon}" />
					</c:otherwise>
				</c:choose>
			</c:if>
			<c:if
				test="${'admin' == profile.name and classified.toValidate}">
				<view:operationSeparator />
				<fmt:message var="validateOp" key="classifieds.validate" />
				<fmt:message var="validateIcon" key="classifieds.validate"
					bundle="${icons}" />
				<fmt:message var="refuseOp" key="classifieds.refused" />
				<fmt:message var="refuseIcon" key="classifieds.refused"
					bundle="${icons}" />
				<view:operation
					action="javascript:validate();"
					altText="${validateOp}" icon="${validateIcon}" />
				<view:operation
					action="javascript:refused();"
					altText="${refuseOp}" icon="${refuseIcon}" />
			</c:if>
      </c:if>
      <c:if test="${not user.anonymous}">
        <view:operationSeparator/>
        <fmt:message var="notifyOp" key="GML.notify" />
        <view:operation action="javascript:toNotify();" altText="${notifyOp}" />
      </c:if>
		</view:operationPane>

	<view:window>
		<view:frame>
			  <table cellpadding="5" width="100%">
          <caption></caption>
          <th id="classified-view-header"></th>
			  <tr>
            <td valign="top">
              <div class="rightContent">
                <c:if test="${not empty index}">
                  <viewTags:displayIndex nbItems="${index.nbItems}" index="${index.currentIndex}" />
                </c:if>
                <div class="bgDegradeGris" id="classified_info">
                  <div class="paragraphe" id="classified_info_creation">
                   <fmt:message key="classifieds.online" /><br/>
                   <c:if test="${fn:length(validationDate) > 0}">
                     <b><c:out value="${validationDate}" /></b>&nbsp;
                   </c:if>
                   <c:if test="${empty validationDate}">
                      <b><c:out value="${creationDate}" /></b>&nbsp;
                   </c:if>
                   <fmt:message key="classifieds.by" />&nbsp;
                    <view:username userId="${classified.creatorId}" />
                    <div class="profilPhoto"><view:image src="${classified.creator.avatar}" type="avatar.profil" css="defaultAvatar" alt=""/></div><br/>
									 <c:if test="${fn:length(updateDate) > 0}">
									   <fmt:message key="classifieds.updateDate" /> : <b><c:out value="${updateDate}" /></b><br/>
									 </c:if>
									</div>

                  <c:if test="${not user.anonymous && user.id != creatorId}">
                    <div id="classified_contact_link" class="bgDegradeGris">
                     <a href="#" onclick="toNotifyOwner()"><fmt:message key="classifieds.contactAdvertiser"/></a>
                    </div>
                  </c:if>
                  <p></p>
                </div>
              </div>
              <div class="principalContent">
                <c:if test="${draftOperationsEnabled && classified.draft}">
                  <div class="inlineMessage">
                    <fmt:message key="classifieds.draft.info"/>
                  </div>
                </c:if>
                <div class="classified_fiche">
                  <h2 class="classified_title">${displayedTitle}</h2>
                  <c:if test="${not empty classified.images}">
                    <div class="classified_photos">
                      <div class="classified_thumbs">
                      <%
                      int i = 0;
                      %>
                      <c:forEach var="image" items="${classified.images}">
                      <%
                      String select = "";
                      if (i == 0) {
                        select = "class=\"selected\"";
                      }
                      %>
                        <a <%=select%> href="#"><view:image src="${image.attachmentURL}" size="250x"/></a>
                      <%
                      i++;
                      %>
                      </c:forEach>
                      </div>
                      <div class="classified_selected_photo">
                      <c:forEach var="image" items="${classified.images}" begin="0" end="0">
                        <a href="javascript:onclick=openImage(webContext+'${image.attachmentURL}')"><view:image src="${image.attachmentURL}" size="250x"/></a>
                      </c:forEach>
                      </div>
                    </div>
                  </c:if>
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
							<view:comments 	userId="${user.id}" componentId="${instanceId}"
											resourceType="${classified.contributionType}" resourceId="${classified.id}" />
						</c:if>
					</td>
				</tr>
			</table>
		</view:frame>
	</view:window>
	<form name="classifiedForm" action="" method="post">
		<input type="hidden" name="ClassifiedId" value="${classified.id}" />
	</form>
	<div id="refusalModalDialog" title="${refuseOp}" style="display: none;">
		<form name="refusalForm" action="RefusedClassified" method="post">
			<table>
        <caption></caption>
        <th id="refusal-form-header"></th>
				<tr>
					<td>
						<table>
							<tr>
								<td class="txtlibform"><fmt:message key="classifieds.number" /> :</td>
								<td>${classified.id} <input type="hidden" name="ClassifiedId" value="${classified.id}"/></td>
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
								<td colspan="2"><img border="0" src="${pageContext.request.contextPath}<fmt:message key="classifieds.mandatory" bundle="${icons}"/>" width="5" height="5"/> : <fmt:message key="GML.requiredField" /></td>
							</tr>
						</table></td>
				</tr>
			</table>
		</form>
	</div>
  <div id="messageToOwnerDialog" style="display: none;">
    <form name="messageToOwnerForm" action="ToNotifyOwner" method="post">
        <table>
          <caption></caption>
          <th id="messageToOnwerFormHeader"></th>
          <tr>
            <td><textarea name="Message" rows="7" cols="70"></textarea>&nbsp;<img border="0" src="${pageContext.request.contextPath}<fmt:message key="classifieds.mandatory" bundle="${icons}"/>" width="5" height="5"/></td>
          </tr>
          <tr>
            <td><img border="0" src="${pageContext.request.contextPath}<fmt:message key="classifieds.mandatory" bundle="${icons}"/>" width="5" height="5"/> : <fmt:message key="GML.requiredField" /></td>
          </tr>
        </table>
    </form>
  </div>
</div>	
</body>
</html>