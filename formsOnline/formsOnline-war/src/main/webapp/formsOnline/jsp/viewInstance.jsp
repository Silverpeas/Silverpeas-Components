<%--

    Copyright (C) 2000 - 2020 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@page import="org.silverpeas.core.contribution.content.form.Form"%>
<%@page import="org.silverpeas.core.contribution.content.form.PagesContext"%>
<%@ page import="org.silverpeas.core.admin.user.model.User" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/formsOnline" prefix="formsOnline" %>

<c:set var="lang" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<c:set var="currentUser" value="${sessionScope['SilverSessionController'].currentUserDetail}"/>

<fmt:setLocale value="${lang}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<c:set var="userRequest" value="${requestScope['UserRequest']}"/>
<jsp:useBean id="userRequest" type="org.silverpeas.components.formsonline.model.FormInstance"/>
<c:set var="validationEnabled" value="${requestScope['ValidationEnabled']}"/>
<c:set var="form" value="${requestScope['FormDetail']}"/>
<c:set var="origin" value="${requestScope['Origin']}"/>
<c:set var="finalValidator" value="${requestScope['FinalValidator']}"/>

<c:set var="formNameParts" value="${silfn:split(form.xmlFormName, '.')}"/>

<fmt:message var="buttonBack" key="GML.back"/>
<fmt:message var="labelAccept" key="formsOnline.request.action.validate"/>
<fmt:message var="labelCancel" key="formsOnline.request.action.cancel"/>
<fmt:message var="labelCancelConfirm" key="formsOnline.request.action.cancel.confirm"/>
<fmt:message var="labelDelete" key="GML.delete"/>
<fmt:message var="labelDeleteConfirm" key="formsOnline.request.action.delete.confirm"/>
<fmt:message var="labelArchive" key="formsOnline.request.action.archive"/>
<fmt:message var="labelArchiveConfirm" key="formsOnline.request.action.archive.confirm"/>

<%
	Form        formView  = (Form) request.getAttribute("Form");

	// context creation
	PagesContext context = (PagesContext) request.getAttribute("FormContext");
	context.setFormName("newInstanceForm");
	context.setFormIndex("0");
	context.setBorderPrinted(false);
%>

<view:sp-page>
<view:sp-head-part>
  <view:link href="/formsOnline/jsp/styleSheets/formsOnline-print.css" print="true"/>
  <% formView.displayScripts(out, context); %>
  <script type="text/javascript">
	  function validate() {
      document.validationForm.decision.value = "validate";
      <c:choose>
        <c:when test="${not userRequest.pendingValidation.validationType.final}">
          $('#followerMessage').popup('validation', {
            title : "${labelAccept}",
            callback : function() {
              var followerCheckbox = document.getElementById('followerCheckbox');
              if (followerCheckbox.checked) {
                document.validationForm.follower.value = followerCheckbox.value;
              }
              document.validationForm.submit();
            }
          });
        </c:when>
        <c:otherwise>
          document.validationForm.submit();
        </c:otherwise>
      </c:choose>
	  }

	  function refuse() {
		  document.validationForm.decision.value = "refuse";
		  document.validationForm.submit();
	  }

    function deleteRequest() {
      jQuery.popup.confirm("${silfn:escapeJs(labelDeleteConfirm)}", {
        title : "${silfn:escapeJs(labelDelete)}",
        callback : function() {
          document.requestForm.action = "DeleteRequest";
          document.requestForm.submit();
        }
      });
    }

    function archive() {
      jQuery.popup.confirm("${silfn:escapeJs(labelArchiveConfirm)}", {
        title : "${silfn:escapeJs(labelArchive)}",
        callback : function() {
          document.requestForm.action = "ArchiveRequest";
          document.requestForm.submit();
        }
      });
    }

    function cancelRequest() {
	    jQuery.popup.confirm("${silfn:escapeJs(labelCancelConfirm)}", {
        title : "${silfn:escapeJs(labelCancel)}",
        callback : function() {
          document.requestForm.action = "CancelRequest";
          document.requestForm.submit();
        }
      });
    }
  </script>
</view:sp-head-part>
<view:sp-body-part cssClass="${formNameParts[0]}">
    <view:operationPane>
      <c:if test="${userRequest.canBeCanceledBy(currentUser)}">
        <view:operation action="javascript:cancelRequest()" altText="${labelCancel}"/>
      </c:if>
      <c:if test="${userRequest.canBeArchivedBy(currentUser)}">
        <view:operation action="javascript:archive()" altText="${labelArchive}"/>
      </c:if>
      <c:if test="${userRequest.canBeDeletedBy(currentUser)}">
        <view:operation action="javascript:deleteRequest()" altText="${labelDelete}"/>
      </c:if>
      <fmt:message var="opPrint" key="GML.print"/>
      <view:operation action="javascript:window.print()" altText="${opPrint}"/>
    </view:operationPane>
  <view:window>

  <div id="header-OnlineForm">
    <h2 class="title">${form.title}</h2>

    <ul class="steps-OnlineForm">
      <li class="step-OnlineForm ask-by">
        <div class="header-step-onlineForm">
          <div class="validator avatar"><view:image src="${userRequest.creator.avatar}" alt="" type="avatar" /></div>
          <div class="title-step-OnlineForm"><fmt:message key="formsOnline.request.from"/></div>
          <div class="date-step-OnlineForm"><fmt:message key="GML.date.the"/> <view:formatDateTime value="${userRequest.creationDate}"/></div>
          <div class="actor-step-OnlineForm"><fmt:message key="GML.by"/> <view:username userId="${userRequest.creatorId}"/></div>
        </div>
        <div class="forms">
          <viewTags:displayUserExtraProperties user="${userRequest.creator}" readOnly="true" linear="true" includeEmail="true" displayLabels="false"/>
        </div>
      </li>

      <formsOnline:validations userRequest="${userRequest}"/>

    </ul>
  </div>

	<%
	  formView.display(out, context);
	%>

  <c:if test="${validationEnabled}">
    <div class="commentaires">
      <div id="edition-box">
        <p class="title">Commentez votre décision</p>
        <div class="avatar"><view:image src="${currentUser.avatar}" type="avatar"/> </div>
        <form name="validationForm" action="EffectiveValideForm" method="post">
          <input type="hidden" name="Id" value="${userRequest.id}"/>
          <input type="hidden" name="Origin" value="${origin}"/>
          <input type="hidden" name="decision" value=""/>
          <input type="hidden" name="follower" id="follower" value=""/>
          <textarea name="comment"  style="resize: none; overflow-y: hidden; height: 60px;" class="text"></textarea>
        </form>
      </div>
    </div>
    <br/>
    <view:buttonPane>
      <c:if test="${userRequest.canBeCanceledBy(currentUser)}">
        <view:button label="${labelCancel}" action="javascript:cancelRequest();"/>
      </c:if>
      <fmt:message var="buttonValidate" key="GML.accept"/>
      <fmt:message var="buttonDeny" key="GML.refuse"/>
      <view:button label="${buttonValidate}" action="javascript:validate();" />
      <view:button label="${buttonDeny}" action="javascript:refuse();" />
      <view:button label="${buttonBack}" action="${origin}" />
    </view:buttonPane>
  </c:if>
  <c:if test="${not validationEnabled}">
    <br/>
    <view:buttonPane>
      <c:if test="${userRequest.canBeCanceledBy(currentUser)}">
        <view:button label="${labelCancel}" action="javascript:cancelRequest();"/>
      </c:if>
      <view:button label="${buttonBack}" action="${origin}" />
    </view:buttonPane>
  </c:if>

  </view:window>
<form name="requestForm" action="" method="post">
  <input type="hidden" name="Id" value="${userRequest.id}"/>
  <input type="hidden" name="Origin" value="InBox"/>
</form>

<div id="followerMessage" style="display: none">
  <input id="followerCheckbox" type="checkbox" value="true"/> <fmt:message key="formsOnline.request.follow"/>
</div>

</view:sp-body-part>
</view:sp-page>