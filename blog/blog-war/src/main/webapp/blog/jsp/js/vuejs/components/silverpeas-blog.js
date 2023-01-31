/*
 * Copyright (C) 2000 - 2023 Silverpeas
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
      '/blog/jsp/js/vuejs/components/silverpeas-blog-templates.jsp');

  SpVue.component('silverpeas-blog-management',
      templateRepository.get('blog-management', {
        mixins : [VuejsApiMixin, VuejsI18nTemplateMixin],
        props : {
          wallpaper : {
            'type' : Object,
            'default' : {}
          },
          stylesheet : {
            'type' : Object,
            'default' : {}
          }
        },
        data : function() {
          return {
            personalizationPopin : undefined
          }
        },
        created : function() {
          this.extendApiWith({
            openPersonalization : this.openPersonalization
          });
        },
        methods : {
          openPersonalization : function() {
            this.personalizationPopin.open();
          }
        }
      }));

  SpVue.component('silverpeas-blog-personalization-popin',
      templateRepository.get('blog-personalization-popin', {
        mixins : [VuejsDefaultFormPanePopinApiMixin, VuejsI18nTemplateMixin],
        emits : ['customize-change'],
        props : {
          wallpaper : {
            'type' : Object,
            'default' : undefined
          },
          stylesheet : {
            'type' : Object,
            'default' : undefined
          }
        },
        created : function() {
          this.extendApiWith({
            open : this.open
          });
        },
        methods : {
          open : function() {
            this.formApi.initFormData();
            this.$super(VuejsDefaultFormPanePopinApiMixin).open();
          },
          validate : function(formPaneData) {
            return sp
                .ajaxRequest('Customize')
                .byPostMethod()
                .send(sp.form.toFormData(formPaneData))
                .then(function() {
                  this.$emit('customize-change');
                }.bind(this));
          }
        }
      }));

  SpVue.component('silverpeas-blog-personalization-form',
      templateRepository.get('blog-personalization-form', {
        mixins : [VuejsFormApiMixin, VuejsI18nTemplateMixin],
        props : {
          wallpaper : {
            'type' : Object,
            'required' : true
          },
          stylesheet : {
            'type' : Object,
            'required' : true
          }
        },
        data : function() {
          return {
            wallpaperInput : undefined,
            wallpaperModel : {},
            stylesheetInput : undefined,
            stylesheetModel : {}
          };
        },
        created : function() {
          this.extendApiWith({
            initFormData : this.initFormData,
            validateForm : function() {
              return this.rootFormApi.errorMessage().none();
            },
            updateFormData : function(formPaneData) {
              if (this.wallpaperModel.file) {
                formPaneData[this.wallpaperModel.fileInputName] = this.wallpaperModel.file;
              } else if (this.wallpaperModel.deleteOriginal) {
                formPaneData.removeWallPaperFile = 'yes';
              }
              if (this.stylesheetModel.file) {
                formPaneData[this.stylesheetModel.fileInputName] = this.stylesheetModel.file;
              } else if (this.stylesheetModel.deleteOriginal) {
                formPaneData.removeStyleSheetFile = 'yes';
              }
            }
          });
        },
        methods : {
          initFormData : function(){
            this.wallpaperInput.clear();
            this.wallpaperModel = {};
            this.stylesheetInput.clear();
            this.stylesheetModel = {};
          }
        }
      }));
})();
