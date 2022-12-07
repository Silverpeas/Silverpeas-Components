<%--
  ~ Copyright (C) 2000 - 2022 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>

<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="userLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle basename="org.silverpeas.util.attachment.multilang.attachment" var="attachment"/>

<%@ attribute name="highestUserRole" required="true"
              type="org.silverpeas.core.admin.user.model.SilverpeasRole"
              description="The highest role the user has" %>
<%@ attribute name="componentInstanceId" required="true"
              type="java.lang.String"
              description="The component instance id associated to the drag and drop" %>
<%@ attribute name="contentLanguage" required="true"
              type="java.lang.String"
              description="The content language in which the attachment is uploaded" %>
<%@ attribute name="forceIgnoreFolder" required="false"
              type="java.lang.Boolean"
              description="The folder tree is ignored, all the files are moved at root" %>

<c:set var="kmeliaCtrl" value="${requestScope.kmelia}"/>
<jsp:useBean id="kmeliaCtrl" type="org.silverpeas.components.kmelia.control.KmeliaSessionController"/>

<c:set var="dragAndDropEnable" value="${kmeliaCtrl.dragAndDropEnable and kmeliaCtrl.attachmentsEnabled}"/>
<c:if test="${dragAndDropEnable}">

  <view:setConstant var="writerRole" constant="org.silverpeas.core.admin.user.model.SilverpeasRole.WRITER"/>
  <jsp:useBean id="writerRole" type="org.silverpeas.core.admin.user.model.SilverpeasRole"/>

    <c:set var="_ddIsI18n" value="${silfn:isI18n() && silfn:isDefined(contentLanguage)}"/>

    <view:componentParam var="publicationAlwaysVisiblePramValue" componentId="${componentInstanceId}" parameter="publicationAlwaysVisible"/>
    <view:componentParam var="isComponentVersioned" componentId="${componentInstanceId}" parameter="versionControl"/>
    <c:set var="isPublicationAlwaysVisible" value="${silfn:booleanValue(publicationAlwaysVisiblePramValue)}"/>
    <c:set var="isVersionActive" value="${not isPublicationAlwaysVisible and silfn:booleanValue(isComponentVersioned)}"/>

    <c:set var="targetValidationEnabled" value="${kmeliaCtrl.targetValidationEnable || kmeliaCtrl.targetMultiValidationEnable}"/>
    <c:set var="validationMandatory" value="${targetValidationEnabled && highestUserRole.equals(writerRole)}"/>
    <fmt:message var="ValidatorLabel" key="kmelia.Valideur"/>

    <view:includePlugin name="dragAndDropUpload"/>
    <view:includePlugin name="attachment"/>

    <view:setConstant var="adminRole" constant="org.silverpeas.core.admin.user.model.SilverpeasRole.ADMIN"/>
    <jsp:useBean id="adminRole" type="org.silverpeas.core.admin.user.model.SilverpeasRole"/>

    <c:set var="ignoreFolders" value="${not highestUserRole.isGreaterThanOrEquals(adminRole) or (forceIgnoreFolder != null and forceIgnoreFolder)}"/>
    <c:set var="draftEnabled" value="${kmeliaCtrl.draftEnabled}"/>
    <c:set var="onlyDraftMode" value="${draftEnabled and kmeliaCtrl.pdcUsed and kmeliaCtrl.PDCClassifyingMandatory}"/>
    <c:set var="publicationStateConfirmation" value="${draftEnabled and not onlyDraftMode}"/>

    <c:set var="isDescription" value="${kmeliaCtrl.fieldDescriptionVisible}"/>
    <c:set var="isDescriptionMandatory" value="${kmeliaCtrl.fieldDescriptionMandatory}"/>
    <c:set var="isKeywords" value="${kmeliaCtrl.fieldKeywordsVisible}"/>

    <fmt:message var="publicationNameLabel" key="GML.name"/>
    <fmt:message var="publicationDescriptionLabel" key="GML.description"/>
    <fmt:message var="publicationKeywordLabel" key="PubMotsCles"/>
    <fmt:message var="dragAndDropTitle" key="kmelia.publication.dragAndDrop.title" />
    <fmt:message var="addFileTitle" key="kmelia.publication.addFile.title" />

    <c:set var="helpUrl" value="${silfn:fullApplicationURL(pageContext.request)}/upload/Kmelia_${userLanguage}.jsp?dummy"/>
    <c:choose>
      <c:when test="${onlyDraftMode}">
        <c:set var="helpUrl" value="${helpUrl}&mode=onlyDraft"/>
      </c:when>
      <c:when test="${not draftEnabled}">
        <c:set var="helpUrl" value="${helpUrl}&mode=noDraft"/>
      </c:when>
    </c:choose>
    <c:if test="${isVersionActive}">
      <c:set var="helpUrl" value="${helpUrl}&version=yes"/>
    </c:if>

    <c:url var="uploadCompletedUrl" value="/RImportDragAndDrop/jsp/Drop">
      <c:param name="ComponentId" value="${componentInstanceId}"/>
    </c:url>
    <c:if test="${ignoreFolders}">
      <c:set var="uploadCompletedUrl" value="${uploadCompletedUrl}&IgnoreFolders=1"/>
      <c:set var="helpUrl" value="${helpUrl}&folders=ignored"/>
    </c:if>
    <c:if test="${onlyDraftMode}">
      <c:set var="uploadCompletedUrl" value="${uploadCompletedUrl}&Draft=1"/>
    </c:if>
    <view:link href="/kmelia/jsp/javaScript/vuejs/components/kmelia-contribution.css"/>
    <view:script src="/kmelia/jsp/javaScript/vuejs/components/kmelia-contribution.js"/>
    <kmelia-file-adding-management
        id="kmeliaFileAddingManagement"
        v-on:api="manager = $event"
        v-bind:component-instance-id="'${componentInstanceId}'"
        v-bind:is-description-visible="${isDescription}"
        v-bind:is-description-mandatory="${isDescriptionMandatory}"
        v-bind:is-keywords-visible="${isKeywords}"
        v-bind:is-publication-state-confirmation="${publicationStateConfirmation}"
        v-bind:is-version-active="${isVersionActive}"
        v-bind:is-validation-mandatory="${validationMandatory}"
        v-bind:is-single-target-validation="${kmeliaCtrl.targetValidationEnable}"
        v-bind:i18n-content="${_ddIsI18n}"
        v-bind:i18n-content-language="'${contentLanguage}'"></kmelia-file-adding-management>

    <script type="text/JavaScript">
      function uploadCompleted(requestResponse) {
        if (requestResponse.indexOf('pubid=') > -1) {
          validatePublicationClassification(requestResponse);
        } else if (requestResponse.indexOf('newFolder=true') > -1) {
          reloadPage(getCurrentNodeId());
        } else {
          refreshPublications();
        }
        return true;
      }

      (function() {
        const options = {
          componentInstanceId : "${componentInstanceId}",
          onCompletedUrl : "${uploadCompletedUrl}",
          onCompletedUrlSuccess : uploadCompleted,
          helpHighlightSelector : ".tableBoard",
          helpForceDisplay : function() {
            return jQuery('.dragAndDropUpload .tableBoard').length === 0;
          },
          helpContentUrl : "${helpUrl}",
          helpCoverClass : "droparea-cover-help-publication-list"
        };
        options.beforeSend = function(fileUpload) {
          const severalFilesToUpload = fileUpload.uploadSession.severalFilesToUpload &&
              (!fileUpload.uploadSession.existsAtLeastOneFolder || ${ignoreFolders});
          const displayDialog = !fileUpload.uploadSession.id &&
              (severalFilesToUpload || ${validationMandatory} || ${isVersionActive} || ${publicationStateConfirmation} || ${_ddIsI18n});
          if (!displayDialog) {
            return Promise.resolve();
          }
          return new Promise(function(resolve, reject) {
            kmeliaFileAddingVm.manager.openValidationStep({
              title : '${silfn:escapeJs(dragAndDropTitle)}',
              severalFilesToUpload : severalFilesToUpload,
              callback : function(formPaneData) {
                fileUpload.uploadSession.onCompleted.url = sp.url.format('${uploadCompletedUrl}', formPaneData);
                resolve();
                return true;
              },
              callbackOnClose : reject
            });
          });
        };
        window.dragAndDropInstanceFromTag = initDragAndDropUploadAndReload(options);
        window.kmeliaFileAddingVm = new Vue({
          el : '#kmeliaFileAddingManagement',
          data : function() {
            return {
              manager : undefined
            }
          },
          methods : {
            addFiles : function() {
              this.manager.addFiles({
                callback : function(addFormPaneData) {
                  return new Promise(function(resolve, reject) {
                    this.manager.openValidationStep({
                      title : '${silfn:escapeJs(addFileTitle)}',
                      severalFilesToUpload : false,
                      i18nContent : false,
                      isVersionActive : false,
                      callback : function(formPaneData) {
                        formPaneData.ContentLanguage = addFormPaneData.fileLang;
                        formPaneData.VersionType = addFormPaneData.versionType;
                        formPaneData.commentMessage = addFormPaneData.commentMessage;
                        formPaneData.documentTemplateId = addFormPaneData.documentTemplateId;
                        formPaneData.fileName = addFormPaneData.fileName;
                        formPaneData.fileTitle = addFormPaneData.fileTitle;
                        formPaneData.fileDescription = addFormPaneData.fileDescription;
                        const postUrl = sp.url.format('${uploadCompletedUrl}', formPaneData);
                        return sp.ajaxRequest(postUrl)
                          .withHeader("X-UPLOAD-SESSION", addFormPaneData.uploadSessionId)
                          .byPostMethod()
                          .send()
                          .then(function(request) {
                            options.onCompletedUrlSuccess(request.response);
                            resolve();
                          });
                      },
                      callbackOnClose : reject
                    });
                  }.bind(this));
                }.bind(this)
              });
            }
          }
        });
      })();
    </script>
</c:if>
<script type="text/javascript">
  window.muteDragAndDrop = function() {
    if (window.dragAndDropInstanceFromTag) {
      window.dragAndDropInstanceFromTag.mute();
    }
  };
  window.activateDragAndDrop = function() {
    if (window.dragAndDropInstanceFromTag) {
      window.dragAndDropInstanceFromTag.unmute();
    }
  };
</script>