<%--

    Copyright (C) 2000 - 2011 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored ="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ page import="com.silverpeas.form.Form,
         com.silverpeas.form.DataRecord,
         com.silverpeas.form.PagesContext"%>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<c:set var="browseContext" value="${requestScope.browseContext}"/>
<c:set var="componentLabel" value="${browseContext[1]}"/>
<c:set var="isDraftEnabled" value="${requestScope.IsDraftEnabled}"/>
<c:set var="isCommentsEnabled" value="${requestScope.IsCommentsEnabled}"/>
<c:set var="profile"    value="${requestScope.Profile}"/>
<c:set var="creationDate" value="${requestScope.CreationDate}"/>
<c:set var="updateDate" value="${requestScope.UpdateDate}"/>
<c:set var="validationDate" value="${requestScope.ValidateDate}"/>
<c:set var="userId" value="${requestScope.UserId}"/>
<c:set var="classified" value="${requestScope.Classified}"/>
<c:set var="instanceId" value="${classified.instanceId}"/>
<c:set var="creatorId"  value="${classified.creatorId}"/>
<%
  // paramètres du formulaire
  Form xmlForm = (Form) request.getAttribute("Form");
  DataRecord xmlData = (DataRecord) request.getAttribute("Data");
  PagesContext xmlContext = (PagesContext) request.getAttribute("Context");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
  <head>
    <view:looknfeel/>
    <script type="text/javascript" src=" <c:url value='/util/javaScript/animation.js'/>"></script>
    <fmt:message var="deletionConfirm" key="classifieds.confirmDeleteClassified"/>
    <script type="text/javascript">

      var refusalMotiveWindow = window;

      function deleteConfirm(id)
      {
        // confirmation de suppression de l'annonce
        if(window.confirm("<c:out value='${deletionConfirm}'/>"))
        {
          document.classifiedForm.action = "DeleteClassified";
          document.classifiedForm.ClassifiedId.value = id;
          document.classifiedForm.submit();
        }
      }

      function updateClassified(id)
      {
        document.classifiedForm.action = "EditClassified";
        document.classifiedForm.ClassifiedId.value = id;
        document.classifiedForm.submit();
      }

      function draftIn(id) {
        location.href = "<view:componentUrl componentId='${instanceId}'/>DraftIn?ClassifiedId="+id;
      }

      function draftOut(id) {
        location.href = "<view:componentUrl componentId='${instanceId}'/>DraftOut?ClassifiedId="+id;
      }

      function validate(id) {
        location.href = "<view:componentUrl componentId='${instanceId}'/>ValidateClassified?ClassifiedId="+id;
      }

      function refused(id) {
        url = "WantToRefuseClassified?ClassifiedId="+id;
        windowName = "refusalMotiveWindow";
        larg = "550";
        haut = "350";
        windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
        if (!refusalMotiveWindow.closed && refusalMotiveWindow.name== "refusalMotiveWindow")
          refusalMotiveWindow.close();
        refusalMotiveWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
      }


    </script>
  </head>
  <body id="classified-view">
    <fmt:message var="classifiedPath" key="classifieds.classified"/>
    <view:browseBar>
      <view:browseBarElt label="${classifiedPath}" link=""/>
    </view:browseBar>
    <c:if test="${userId == creatorId or profile.name == 'admin'}">
      <fmt:message var="updateOp" key="classifieds.updateClassified"/>
      <fmt:message var="updateIcon"  key="classifieds.update" bundle="${icons}"/>
      <fmt:message var="deleteOp" key="classifieds.deleteClassified"/>
      <fmt:message var="deleteIcon" key="classifieds.delete" bundle="${icons}"/>
      <view:operationPane>
        <view:operation action="javascript:updateClassified('${classified.classifiedId}');"
                        altText="${updateOp}" icon="${updateIcon}"/>
        <view:operation action="javascript:deleteConfirm('${classified.classifiedId}');"
                        altText="${deleteOp}" icon="${deleteIcon}"/>

        <c:if test="${isDraftEnabled}">
          <view:operationSeparator/>
          <c:choose>
            <c:when test="${'Draft' == classified.status}">
              <fmt:message var="draftOutOp" key="classifieds.draftOut"/>
              <fmt:message var="draftOutIcon" key="classifieds.draftOut" bundle="${icons}"/>
              <view:operation action="javascript:draftOut('${classified.classifiedId}');"
                              altText="${draftOutOp}" icon="${draftOutIcon}"/>
            </c:when>
            <c:otherwise>
              <fmt:message var="draftInOp" key="classifieds.draftIn"/>
              <fmt:message var="draftInIcon" key="classifieds.draftIn" bundle="${icons}"/>
              <view:operation action="javascript:draftIn('${classified.classifiedId}');"
                              altText="${draftInOp}" icon="${draftInIcon}"/>
            </c:otherwise>
          </c:choose>
        </c:if>
        <c:if test="${'admin' == profile.name and 'ToValidate' == classified.status}">
          <view:operationSeparator/>
          <fmt:message var="validateOp" key="classifieds.validate"/>
          <fmt:message var="validateIcon" key="classifieds.validate" bundle="${icons}"/>
          <fmt:message var="refuseOp" key="classifieds.refused"/>
          <fmt:message var="refuseIcon" key="classifieds.refused" bundle="${icons}"/>
          <view:operation action="javascript:validate('${classified.classifiedId}');"
                          altText="${validateOp}" icon="${validateIcon}"/>
          <view:operation action="javascript:refused('${classified.classifiedId}');"
                          altText="${refuseOp}" icon="${refuseIcon}"/>
        </c:if>
      </view:operationPane>
    </c:if>

    <view:window>
      <view:frame>
        <table cellpadding="5" width="100%">
          <tr>
            <td>
              <div class="tableBoard" id="classified-view-header">
                <h1 class="titreFenetre" id="classified-title"><c:out value="${classified.title}"/></h1>
                <div id="classified-view-header-owner">
                  <span class="txtlibform"><fmt:message key="classifieds.annonceur"/>: </span>
                  <span class="txtvalform"><c:out value="${classified.creatorName} (${classified.creatorEmail})"/></span>
                </div>
                <div id="classified-view-header-parutionDate">
                  <span class="txtlibform"><fmt:message key="classifieds.parutionDate"/>: </span>
                  <span class="txtvalform"><c:out value="${creationDate}"/></span>
                </div>
                <c:if test="${fn:length(updateDate) > 0}">
                  <div id="classified-view-header-updateDate">
                    <span class="txtlibform"><fmt:message key="classifieds.updateDate"/>: </span>
                    <span class="txtvalform"><c:out value="${updateDate}"/></span>
                  </div>
                </c:if>
                <c:if test="${fn:length(validationDate) > 0 and classified.validatorName != null and fn:length(classified.validatorName) > 0}">
                  <div id="classified-view-header-validateDate">
                    <span class="txtlibform"><fmt:message key="classifieds.validateDate"/>: </span>
                    <span class="txtvalform"><c:out value="${validationDate}"/>&nbsp;<span><fmt:message key="classifieds.by"/></span>&nbsp;<c:out value="${classified.validatorName}"/></span>
                  </div>
                </c:if>
                <hr class="clear" />
              </div>

              <% if (xmlForm != null) {%>
              <div class="tableBoard" id="classified-view-content">
                <!-- AFFICHAGE du formulaire -->
                <%
                  xmlForm.display(out, xmlContext, xmlData);
                %>
                <hr class="clear" />
              </div>
              <% }%>
            </td>
          </tr>
          <tr>
            <td>
              <!--Afficher les commentaires-->
				<c:if test="${isCommentsEnabled}">
              <view:comments userId="${userId}" componentId="${instanceId}" resourceId="${classified.classifiedId}" />
				</c:if>
            </td>
          </tr>
        </table>
      </view:frame>
    </view:window>
    <form name="classifiedForm" action="" method="post">
      <input type="hidden" name="ClassifiedId"/>
    </form>
  </body>
</html>