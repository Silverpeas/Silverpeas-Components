<%--
  ~ Copyright (C) 2000 - 2024 Silverpeas
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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<c:set var="language" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle basename="org.silverpeas.kmelia.multilang.kmeliaBundle"/>
<view:setBundle basename="org.silverpeas.util.attachment.multilang.attachment" var="attachment"/>

<fmt:message key="GML.language" var="languageLabel"/>
<fmt:message var="publicationNameLabel" key="GML.name"/>
<fmt:message var="publicationDescriptionLabel" key="GML.description"/>
<fmt:message var="publicationKeywordLabel" key="PubMotsCles"/>
<fmt:message var="validatorLabel" key="kmelia.Valideur"/>

<!-- ########################################################################################### -->
<silverpeas-component-template name="kmelia-file-adding-management">
  <div class="kmelia-file-adding-management">
    <silverpeas-add-files-popin
        v-on:api="addFilesPopinApi = $event"
        v-bind:is-i18n-content="context.isI18nContent"
        v-bind:i18n-content-language="context.i18nContentLanguage"
        v-bind:component-instance-id="componentInstanceId">
      <silverpeas-attachment-form v-on:api="addAttFormApi = $event" v-if="context"
                                  v-bind:context="context"></silverpeas-attachment-form>
    </silverpeas-add-files-popin>
    <kmelia-file-upload-validation-popin
        v-on:api="validationPopinApi = $event"
        v-bind:component-instance-id="componentInstanceId"
        v-bind:is-description-visible="isDescriptionVisible"
        v-bind:is-description-mandatory="isDescriptionMandatory"
        v-bind:is-keywords-visible="isKeywordsVisible"
        v-bind:is-publication-state-confirmation="isPublicationStateConfirmation"
        v-bind:is-version-active="isVersionActive"
        v-bind:is-validation-mandatory="isValidationMandatory"
        v-bind:is-single-target-validation="isSingleTargetValidation"
        v-bind:i18n-content="i18nContent"
        v-bind:i18n-content-language="i18nContentLanguage"></kmelia-file-upload-validation-popin>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="kmelia-file-upload-validation-popin">
  <div class="kmelia-validation-management">
    <silverpeas-popin v-on:api="setPopinApi"
                      v-bind:title="title"
                      type="validation">
      <silverpeas-form-pane v-on:api="setFormPaneApi"
                            v-bind:manual-actions="true"
                            v-bind:mandatory-legend="formApi && formApi.isAtLeastOnMandatoryInput()">
        <kmelia-file-upload-validation-form
            v-on:api="setFormApi"
            v-bind:is-description-visible="isDescriptionVisible"
            v-bind:is-description-mandatory="isDescriptionMandatory"
            v-bind:is-keywords-visible="isKeywordsVisible"
            v-bind:is-publication-state-confirmation="isPublicationStateConfirmation"
            v-bind:is-version-active="isVersionActiveEnabled"
            v-bind:is-validation-mandatory="isValidationMandatory"
            v-bind:is-single-target-validation="isSingleTargetValidation"
            v-bind:i18n-content="i18nContentEnabled"
            v-bind:i18n-content-language="i18nContentLanguage"
            v-bind:several-files-to-upload="severalFilesToUpload"></kmelia-file-upload-validation-form>
      </silverpeas-form-pane>
    </silverpeas-popin>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="kmelia-file-upload-validation-form">
  <div class="kmelia-validation-form form-container">
    <div v-if="i18nContent">
      <br/>
      <silverpeas-label for="kct-ddLangCreateId" class="label">${languageLabel}</silverpeas-label>
      <div class="champ-ui-dialog">
        <silverpeas-select-language id="kct-ddLangCreateId" name="kct-ddLangCreate"
                                    v-model="contentLanguage"></silverpeas-select-language>
      </div>
      <div style="height: 2px"></div>
    </div>
    <div class="creationModeBlock" v-if="severalFilesToUpload">
      <br/>
      <span class="label"><fmt:message key="kmelia.publication.dragAndDrop.question.publication"/></span>
      <div>
        <silverpeas-radio-input value="onePerFile" name="creationMode" id="kct-severalCreations"
                                v-model="creationMode"></silverpeas-radio-input>
        <silverpeas-label for="kct-severalCreations" class="value"><fmt:message key="kmelia.publication.dragAndDrop.question.publication.several"/></silverpeas-label>
        <div class="oneCreationBlock">
          <silverpeas-radio-input value="oneForAll" name="creationMode" id="kct-oneCreation"
                                  v-model="creationMode"></silverpeas-radio-input>
          <silverpeas-label for="kct-oneCreation" class="value"><fmt:message key="kmelia.publication.dragAndDrop.question.publication.one"/></silverpeas-label>
          <div class="publicationBlock" v-if="creationMode === 'oneForAll'">
            <silverpeas-label for="kct-publicationName" class="label"
                              v-bind:mandatory="true">${publicationNameLabel}</silverpeas-label><br/>
            <silverpeas-text-input
                v-sp-focus
                name="publicationName" id="kct-publicationName"
                v-bind:maxlength="400" v-model="publicationName"></silverpeas-text-input>
            <template v-if="isDescriptionVisible">
              <br/>
              <silverpeas-label for="kct-publicationDescription" class="label"
                                v-bind:mandatory="isDescriptionMandatory">${publicationDescriptionLabel}</silverpeas-label><br/>
              <silverpeas-multiline-text-input
                  name="publicationDescription"
                  id="kct-publicationDescription"
                  v-bind:rows="2" v-bind:cols="50"
                  v-model="publicationDescription"></silverpeas-multiline-text-input>
            </template>
            <template v-if="isKeywordsVisible">
              <br/>
              <silverpeas-label for="kct-publicationKeywords" class="label">${publicationKeywordLabel}</silverpeas-label><br/>
              <silverpeas-text-input name="publicationKeywords" id="kct-publicationKeywords"
                                     v-bind:maxlength="150" v-model="publicationKeywords"></silverpeas-text-input>
            </template>
          </div>
        </div>
      </div>
    </div>
    <template v-if="isPublicationStateConfirmation">
      <br/>
      <span class="label"><fmt:message key="kmelia.publication.dragAndDrop.question.state"/></span>
      <div>
        <silverpeas-radio-input
            name="publicationState" id="kct-draftState" value="1"
            v-model="publicationState"></silverpeas-radio-input>
        <silverpeas-label for="kct-draftState" class="value"><fmt:message key="PubStateDraft"/></silverpeas-label><br/>
        <silverpeas-radio-input
            name="publicationState" id="kct-publishedState" value="0"
            v-model="publicationState"></silverpeas-radio-input>
        <silverpeas-label for="kct-publishedState" class="value"><fmt:message key="PubStatePublished"/></silverpeas-label>
      </div>
    </template>
    <template v-if="isVersionActive">
      <div>
        <br/>
        <span class="label"><fmt:message key="attachment.dragAndDrop.question" bundle="${attachment}"/></span>
        <div>
          <silverpeas-radio-input value="0" name="kct-versionType" id="kct-publicVersion"
                                  v-model="versionType"></silverpeas-radio-input>
          <silverpeas-label for="kct-publicVersion" class="value"><fmt:message key="attachment.version_public.label" bundle="${attachment}"/></silverpeas-label><br/>
          <silverpeas-radio-input value="1" type="radio" name="kct-versionType" id="kct-workVersion"
                                  v-model="versionType"></silverpeas-radio-input>
          <silverpeas-label for="kct-workVersion" class="value"><fmt:message key="attachment.version_wip.label" bundle="${attachment}"/></silverpeas-label>
        </div>
      </div>
    </template>
    <template v-if="isValidationMandatory">
      <div>
        <br/>
        <silverpeas-label for="kct-dropValideurId" class="label" v-bind:mandatory="true">${validatorLabel}</silverpeas-label>
        <silverpeas-hidden-input name="DropValideurId" id="kct-dropValideurId"
                                 v-model="dropValideurId"/>
        <div class="validator-block">
          <input v-if="isSingleTargetValidation" name="DropValideur" id="kct-dropValideur" size="50" v-model="dropValideurName" readonly="readonly"/>
          <textarea v-else name="DropValideur" id="kct-dropValideur" rows="3" cols="40" v-model="dropValideurName" readonly="readonly"/>
          <fmt:message var="selectLabel" key="kmelia.SelectValidator"/>
          <a href="javascript:void(0)" onclick="javascript:SP_openWindow('SelectValidator?FormElementName=kct-dropValideur&FormElementId=kct-dropValideurId','selectUser',800,600,'');">
            <img v-bind:src="validatorIconUrl" width="15" height="15" alt="${selectLabel}" title="${selectLabel}" />
          </a>
        </div>
      </div>
    </template>
  </div>
</silverpeas-component-template>