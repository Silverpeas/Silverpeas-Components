<%--
  Copyright (C) 2000 - 2015 Silverpeas
  
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.
  
  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have recieved a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.
  
  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

<%@ attribute name="greatestUserRole" required="true"
              type="com.stratelia.webactiv.SilverpeasRole"
              description="The greatest role the user has" %>
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
<jsp:useBean id="kmeliaCtrl" type="com.stratelia.webactiv.kmelia.control.KmeliaSessionController"/>

<c:set var="dragAndDropEnable" value="${kmeliaCtrl.dragAndDropEnable and kmeliaCtrl.attachmentsEnabled}"/>
<c:if test="${dragAndDropEnable}">

  <view:setConstant var="writerRole" constant="com.stratelia.webactiv.SilverpeasRole.writer"/>
  <jsp:useBean id="writerRole" type="com.stratelia.webactiv.SilverpeasRole"/>
  <c:if test="${greatestUserRole.isGreaterThanOrEquals(writerRole)}">

    <c:set var="_ddIsI18n" value="${silfn:isI18n() && silfn:isDefined(contentLanguage) && not silfn:booleanValue(param.notI18n)}"/>

    <view:componentParam var="publicationAlwaysVisiblePramValue" componentId="${componentInstanceId}" parameter="publicationAlwaysVisible"/>
    <view:componentParam var="isComponentVersioned" componentId="${componentInstanceId}" parameter="versionControl"/>
    <c:set var="isPublicationAlwaysVisible" value="${silfn:booleanValue(publicationAlwaysVisiblePramValue)}"/>
    <c:set var="isVersionActive" value="${not isPublicationAlwaysVisible and silfn:booleanValue(isComponentVersioned)}"/>

    <view:includePlugin name="dragAndDropUpload"/>

    <view:setConstant var="adminRole" constant="com.stratelia.webactiv.SilverpeasRole.admin"/>
    <jsp:useBean id="adminRole" type="com.stratelia.webactiv.SilverpeasRole"/>

    <c:set var="ignoreFolders" value="${not greatestUserRole.isGreaterThanOrEquals(adminRole) or (forceIgnoreFolder != null and forceIgnoreFolder)}"/>
    <c:set var="draftEnabled" value="${kmeliaCtrl.draftEnabled}"/>
    <c:set var="onlyDraftMode" value="${draftEnabled and kmeliaCtrl.pdcUsed and kmeliaCtrl.PDCClassifyingMandatory}"/>
    <c:set var="publicationStateConfirmation" value="${draftEnabled and not onlyDraftMode}"/>

    <c:set var="isDescription" value="${kmeliaCtrl.fieldDescriptionVisible}"/>
    <c:set var="isDescriptionMandatory" value="${kmeliaCtrl.fieldDescriptionMandatory}"/>
    <c:set var="isKeywords" value="${kmeliaCtrl.fieldKeywordsVisible}"/>

    <fmt:message var="publicationNameLabel" key="GML.name"/>
    <fmt:message var="publicationDescriptionLabel" key="GML.description"/>
    <fmt:message var="publicationKeywordLabel" key="PubMotsCles"/>

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

    <div id="validationDialog" class="form-container" style="display: none;">
      <c:if test="${_ddIsI18n}">
        <br/>

        <div>
          <label for="ddLangCreateId" class="label"><fmt:message key="GML.language"/></label>
          <span class="champ-ui-dialog">
            <view:langSelect elementName="ddLangCreate"
                             elementId="ddLangCreateId"
                             langCode="${contentLanguage}"
                             includeLabel="false"/>
          </span>

          <div style="height: 2px"></div>
        </div>
      </c:if>

      <div id="creationModeBlock">
        <br/>
        <span class="label"><fmt:message key="kmelia.publication.dragAndDrop.question.publication"/></span>

        <div>
          <input value="onePerFile" type="radio" name="creationMode" id="severalCreations" checked="checked"/>
          <label for="severalCreations"><fmt:message key="kmelia.publication.dragAndDrop.question.publication.several"/></label>

          <div id="oneCreationBlock">
            <input value="oneForAll" type="radio" name="creationMode" id="oneCreation"/>
            <label for="oneCreation"><fmt:message key="kmelia.publication.dragAndDrop.question.publication.one"/></label>

            <div id="publicationBlock" style="display: none;padding-top: 7px">
              <label for="publicationName" class="label">${publicationNameLabel}</label><br/>
              <input type="text" name="publicationName" id="publicationName" maxlength="150" style="width: 98%"/>
              <c:if test="${isDescription}">
                <br/>
                <label for="publicationDescription" class="label">${publicationDescriptionLabel}</label><br/>
                <textarea cols="50" rows="2" name="publicationDescription" id="publicationDescription" style="width: 97%"></textarea>
              </c:if>
              <c:if test="${isKeywords}">
                <br/>
                <label for="publicationKeywords" class="label">${publicationKeywordLabel}</label><br/>
                <input type="text" name="publicationKeywords" id="publicationKeywords" maxlength="150" style="width: 98%"/>
              </c:if>
            </div>
          </div>
        </div>
      </div>
      <c:if test="${publicationStateConfirmation}">
        <br/>
        <span class="label"><fmt:message key="kmelia.publication.dragAndDrop.question.state"/></span>

        <div>
          <input value="1" type="radio" name="publicationState" id="draftState" checked="checked"/>
          <label for="draftState"><fmt:message key="PubStateDraft"/></label><br/>
          <input value="0" type="radio" name="publicationState" id="publishedState"/>
          <label for="publishedState"><fmt:message key="PubStatePublished"/></label>
        </div>
      </c:if>
      <c:if test="${isVersionActive}">

        <div>
          <br/>
          <span class="label"><fmt:message key="attachment.dragAndDrop.question" bundle="${attachment}"/></span>

          <div>
            <input value="0" type="radio" name="versionType" id="publicVersion" checked="checked"/>
            <label for="publicVersion"><fmt:message key="attachment.version_public.label" bundle="${attachment}"/></label><br/>
            <input value="1" type="radio" name="versionType" id="workVersion"/>
            <label for="workVersion"><fmt:message key="attachment.version_wip.label" bundle="${attachment}"/></label>
          </div>
        </div>
      </c:if>
    </div>

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
        var $creationModeBlock = jQuery('#creationModeBlock');
        var $publicationBlock = jQuery('#publicationBlock', $creationModeBlock);
        var $publicationName = jQuery('input[name=publicationName]', $publicationBlock);
        var $publicationDescription = jQuery('textarea[name=publicationDescription]',
            $publicationBlock);
        var $publicationKeywords = jQuery('input[name=publicationKeywords]', $publicationBlock);
        var $creationModeRadios = jQuery('input[name=creationMode]', $creationModeBlock);
        var $oneCreationBlock = jQuery('#oneCreationBlock', $creationModeBlock);
        $creationModeRadios.on('change', function() {
          if (jQuery(this).val() === 'oneForAll') {
            $publicationBlock.show();
            $oneCreationBlock.addClass('block');
            $publicationName.focus();
          } else {
            $publicationBlock.hide();
            $oneCreationBlock.removeClass('block');
          }
        });

        var options = {
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
          var severalFilesToUpload = fileUpload.uploadSession.severalFilesToUpload &&
              (!fileUpload.uploadSession.existsAtLeastOneFolder || ${ignoreFolders});
          var displayDialog = !fileUpload.uploadSession.id &&
              (severalFilesToUpload || ${isVersionActive} || ${publicationStateConfirmation} || ${_ddIsI18n});
          if (!displayDialog) {
            return Promise.resolve();
          }

          if (severalFilesToUpload) {
            $creationModeBlock.show();
          } else {
            $creationModeBlock.hide();
          }
          return new Promise(function(resolve, reject) {
            jQuery('#validationDialog').popup('validation', {
              title : '<fmt:message key="kmelia.publication.dragAndDrop.title" />',
              buttonDisplayed : true,
              isMaxWidth : true,
              callback : function() {
                var uploadCompletedUrl = '${uploadCompletedUrl}';
                <c:if test="${_ddIsI18n}">
                var contentLanguage = jQuery('select[name=ddLangCreate${domIdSuffix}]', this).val();
                uploadCompletedUrl += '&ContentLanguage=' + contentLanguage;
                </c:if>
                <c:if test="${publicationStateConfirmation}">
                var state = jQuery('input[name=publicationState]:checked', this).val();
                uploadCompletedUrl += '&Draft=' + state;
                </c:if>
                <c:if test="${isVersionActive}">
                var version = jQuery('input[name=versionType]:checked', this).val();
                uploadCompletedUrl += '&VersionType=' + version;
                </c:if>
                if (severalFilesToUpload) {
                  var creationMode = jQuery(':checked', $creationModeBlock).val();
                  if (creationMode === 'oneForAll') {
                    var name = jQuery.trim($publicationName.val());
                    if (StringUtil.isNotDefined(name)) {
                      SilverpeasError.add("<b>${publicationNameLabel}</b> <fmt:message key='GML.MustBeFilled'/>");
                    } else {
                      uploadCompletedUrl += '&PublicationName=' + encodeURIComponent(name);
                    }
                    <c:if test="${isDescription}">
                    var description = jQuery.trim($publicationDescription.val());
                    <c:choose>
                    <c:when test="${isDescriptionMandatory}">
                    if (StringUtil.isNotDefined(description)) {
                      SilverpeasError.add("<b>${publicationDescriptionLabel}</b> <fmt:message key='GML.MustBeFilled'/>");
                    } else {
                      uploadCompletedUrl +=
                          '&PublicationDescription=' + encodeURIComponent(description);
                    }
                    </c:when>
                    <c:otherwise>
                    if (StringUtil.isDefined(description)) {
                      uploadCompletedUrl +=
                          '&PublicationDescription=' + encodeURIComponent(description);
                    }
                    </c:otherwise>
                    </c:choose>
                    </c:if>
                    <c:if test="${isKeywords}">
                    var keywords = jQuery.trim($publicationKeywords.val());
                    if (StringUtil.isDefined(keywords)) {
                      uploadCompletedUrl += '&PublicationKeywords=' + encodeURIComponent(keywords);
                    }
                    </c:if>
                  }
                }
                fileUpload.uploadSession.uploadInstance.context.onCompletedUrl = uploadCompletedUrl;

                if (!SilverpeasError.show()) {
                  $publicationName.val('');
                  $publicationDescription.val('');
                  $publicationKeywords.val('');
                  resolve();
                  return true;
                }
              },
              callbackOnClose : function() {
                reject();
              }
            });
          });
        };

        window.dragAndDropInstanceFromTag = initDragAndDropUploadAndReload(options);
      })();
    </script>
  </c:if>
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