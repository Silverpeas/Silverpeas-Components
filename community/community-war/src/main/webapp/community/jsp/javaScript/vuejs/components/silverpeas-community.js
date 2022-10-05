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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

//# sourceURL=/community/jsp/javaScript/services/silverpeas-community.js

(function() {

  const templateRepository = new VueJsAsyncComponentTemplateRepository(webContext +
      '/community/jsp/javaScript/vuejs/components/silverpeas-community-templates.jsp');

  Vue.component('silverpeas-community-management',
      templateRepository.get('community-management', {
        mixins : [VuejsApiMixin, VuejsI18nTemplateMixin],
        inject : ['context', 'communityService'],
        props : {
          displayCharterOnSpaceHomepage : {
            'type' : Boolean,
            'default' : false
          }
        },
        data : function() {
          return {
            adminSpaceHomepagePopinApi : undefined,
            defineCharterPopinApi : undefined,
            defineCharterFormApi : undefined,
            community : undefined
          };
        },
        created : function() {
          this.loadCommunity();
          this.extendApiWith({
            modifySpaceHomepage : function() {
              return this.adminSpaceHomepagePopinApi.open();
            },
            defineCharter : function() {
              return this.defineCharterPopinApi.open({
                callback : this.defineCharterFormApi.validate
              });
            }
          });
        },
        methods : {
          loadCommunity : function() {
            return this.communityService.get().then(function(community) {
              this.community = community;
            }.bind(this));
          },
          saveSpaceHomepage : function(spaceHomepage) {
            const communityToUpdate = extendsObject({}, this.community);
            communityToUpdate.homePageType = spaceHomepage.type;
            communityToUpdate.homePage = spaceHomepage.value;
            return this.communityService.saveSpaceHomepage(communityToUpdate).then(function(community) {
              this.community = community;
              spaceHomepage.deferredSave.resolve();
              this.$emit('space-homepage-saved', community);
            }.bind(this), function() {
              spaceHomepage.deferredSave.reject();
            });
          },
          saveCharter : function(charterData) {
            const communityToUpdate = extendsObject({}, this.community);
            communityToUpdate.charterURL = charterData.charterURL;
            return this.communityService.saveCharter(communityToUpdate, {
              displayCharterOnSpaceHomepage : charterData.displayCharterOnSpaceHomepage
            }).then(function(community) {
              this.community = community;
              notySuccess(this.messages.saveCharterSuccessMsg);
              this.$emit('charter-saved', {
                community : community,
                displayCharterOnSpaceHomepage : charterData.displayCharterOnSpaceHomepage
              });
            }.bind(this));
          }
        },
        computed : {
          spaceId : function() {
            return this.community.spaceId;
          },
          spaceHomepage : function() {
            return {
              'type' : this.community.homePageType,
              'value' : this.community.homePage
            };
          }
        }
      }));

  Vue.component('silverpeas-community-define-charter-form',
      templateRepository.get('community-define-charter-form', {
        mixins : [VuejsFormApiMixin, VuejsI18nTemplateMixin],
        inject : ['context', 'communityService'],
        props : {
          community : {
            'type' : Object,
            'mandatory' : true
          },
          displayCharterOnSpaceHomepage : {
            'type' : Boolean,
            'mandatory' : true
          }
        },
        data : function() {
          return {
            charterURL : undefined,
            displayCharter : undefined
          };
        },
        created : function() {
          this.initFormData();
          this.extendApiWith({
            validateForm : function() {
              return this.rootFormApi.errorMessage().none();
            },
            updateFormData : function(formPaneData) {
              formPaneData.charterURL = this.charterURL;
              formPaneData.displayCharterOnSpaceHomepage = this.displayCharter;
            }
          });
        },
        methods : {
          initFormData : function() {
            this.charterURL = this.community.charterURL;
            this.displayCharter = this.displayCharterOnSpaceHomepage;
          }
        },
        watch : {
          'community.charterURL' : function() {
            this.initFormData();
          },
          'displayCharterOnSpaceHomepage' : function() {
            this.initFormData();
          }
        }
      }));
})();
