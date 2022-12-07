/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

(function() {

  const templateRepository = new VueJsAsyncComponentTemplateRepository(webContext +
      '/kmelia/jsp/javaScript/vuejs/components/kmelia-contribution-templates.jsp');

  const __kmeliaMixin = {
    mixins : [VuejsApiMixin],
    props : {
      componentInstanceId : {
        'type': String,
        'default': undefined
      },
      isDescriptionVisible : {
        'type' : Boolean,
        'mandatory' : true
      },
      isDescriptionMandatory : {
        'type' : Boolean,
        'mandatory' : true
      },
      isKeywordsVisible : {
        'type' : Boolean,
        'mandatory' : true
      },
      i18nContent : {
        'type' : Boolean,
        'mandatory' : true
      },
      i18nContentLanguage : {
        'type' : String,
        'mandatory' : true
      },
      isPublicationStateConfirmation : {
        'type' : Boolean,
        'mandatory' : true
      },
      isVersionActive : {
        'type' : Boolean,
        'mandatory' : true
      },
      isValidationMandatory : {
        'type' : Boolean,
        'mandatory' : true
      },
      isSingleTargetValidation : {
        'type' : Boolean,
        'mandatory' : true
      }
    }
  };

  Vue.component('kmelia-file-adding-management',
      templateRepository.get('kmelia-file-adding-management', {
        mixins : [__kmeliaMixin],
        data : function() {
          return {
            context : undefined,
            addFilesPopinApi : undefined,
            addAttFormApi : undefined,
            validationPopinApi : undefined
          };
        },
        created : function() {
          this.extendApiWith({
            addFiles : this.openAddFiles,
            openValidationStep : this.openValidationStep
          });
          this.context = {
            isVersionActive : this.isVersionActive,
            indexIt : true,
            isI18nContent : this.i18nContent,
            i18nContentLanguage : this.i18nContentLanguage
          };
        },
        methods : {
          openAddFiles : function(options) {
            const __callback = options.callback;
            options.callback = function(formPaneData) {
              return __callback(formPaneData).then(function() {
                this.addFilesPopinApi.formApi.initFormData();
                this.addAttFormApi.initFormData();
              }.bind(this));
            }.bind(this);
            this.addFilesPopinApi.open(options);
          },
          openValidationStep : function(options) {
            this.validationPopinApi.open(options);
          }
        }
      }));

  Vue.component('kmelia-file-upload-validation-popin',
      templateRepository.get('kmelia-file-upload-validation-popin', {
        mixins : [VuejsDefaultFormPanePopinApiMixin, __kmeliaMixin],
        data : function() {
          return {
            popinOptions : undefined,
            title : '',
            severalFilesToUpload : false
          };
        },
        methods : {
          open : function(options) {
            this.title = options.title || '';
            this.severalFilesToUpload = !!options.severalFilesToUpload;
            this.popinOptions = options;
            this.$super(VuejsDefaultFormPanePopinApiMixin).open({
              callbackOnClose : this.popinOptions.callbackOnClose
            });
          },
          validate : function(formPaneData) {
            const result = this.popinOptions.callback(formPaneData);
            if (sp.promise.isOne(result)) {
              formPaneData.validationFormPromise = result.then(function() {
                this.formApi.initFormData();
              }.bind(this));
            } else {
              this.formApi.initFormData();
            }
          }
        },
        computed : {
          i18nContentEnabled : function() {
            if (this.popinOptions && typeof this.popinOptions.i18nContent === 'boolean') {
              return this.popinOptions.i18nContent;
            }
            return this.i18nContent;
          },
          isVersionActiveEnabled : function() {
            if (this.popinOptions && typeof this.popinOptions.isVersionActive === 'boolean') {
              return this.popinOptions.isVersionActive;
            }
            return this.isVersionActive;
          }
        }
      }));

  Vue.component('kmelia-file-upload-validation-form',
      templateRepository.get('kmelia-file-upload-validation-form', {
        mixins : [__kmeliaMixin, VuejsFormApiMixin, VuejsI18nTemplateMixin],
        props : {
          severalFilesToUpload : {
            'type' : Boolean,
            'mandatory' : true
          }
        },
        data : function() {
          return {
            contentLanguage : undefined,
            creationMode : 'onePerFile',
            publicationName : undefined,
            publicationDescription : undefined,
            publicationKeywords : undefined,
            publicationState : '1',
            versionType : '0',
            dropValideurId : '',
            dropValideurName : ''
          };
        },
        created : function() {
          this.extendApiWith({
            initFormData: this.initFormData,
            isAtLeastOnMandatoryInput : this.isAtLeastOnMandatoryInput,
            validateForm : function() {
              return this.rootFormApi.errorMessage().none();
            },
            updateFormData : function(formPaneData) {
              if (this.i18nContent) {
                formPaneData.ContentLanguage = this.contentLanguage;
              }
              if (this.isPublicationStateConfirmation) {
                formPaneData.Draft = this.publicationState;
              }
              if (this.isVersionActive) {
                formPaneData.VersionType = this.versionType;
              }
              if (this.severalFilesToUpload) {
                if (this.publicationName) {
                  formPaneData.PublicationName = encodeURIComponent(this.publicationName.trim());
                }
                if (this.publicationDescription) {
                  formPaneData.PublicationDescription = encodeURIComponent(this.publicationDescription.trim());
                }
                if (this.publicationKeywords) {
                  formPaneData.PublicationKeywords = encodeURIComponent(this.publicationKeywords.trim());
                }
              }
              if (this.isValidationMandatory) {
                formPaneData.ValidatorIds = this.dropValideurId;
              }
            }
          });
          this.initFormData();
        },
        methods : {
          initFormData : function() {
            this.contentLanguage = this.i18nContentLanguage;
            this.creationMode = 'onePerFile';
            this.publicationName = undefined;
            this.publicationDescription = undefined;
            this.publicationKeywords = undefined;
            this.publicationState = '1';
            this.versionType = '0';
            this.dropValideurId = '';
            this.dropValideurName = '';
          },
          isAtLeastOnMandatoryInput : function() {
            return (this.severalFilesToUpload && this.creationMode === 'oneForAll') || this.isValidationMandatory;
          }
        },
        computed : {
          validatorIconUrl : function() {
            return webContext + '/util/icons/' + (this.isSingleTargetValidation ? 'user.gif' : 'groupe.gif');
          }
        }
      }));
})();
