/*
 * Copyright (C) 2000 - 2024 Silverpeas
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

  const reassignmentAsyncComponentRepository = new VueJsAsyncComponentTemplateRepository(webContext +
      '/processManager/jsp/javaScript/vuejs/reassignment-templates.jsp');

  const ReassignmentRoleManagerMixin = {
    mixins : [VuejsI18nTemplateMixin],
    inject : ['context', 'commonService'],
    data : function() {
      return {
        reassignmentData : {
          incumbent : {},
          substitute : {}
        },
        roleManager : undefined
      }
    },
    created : function() {
      this.commonService.promiseRoleManager().then(function(roleManager) {
        this.roleManager = roleManager;
      }.bind(this));
    },
    methods : {
      resetMixinData : function() {
        this.reassignmentData = {
          incumbent : {},
          substitute : {}
        }
      },
      formatRoles : function(roles) {
        return this.$filters.joinWith(this.$filters.mapRoleLabel(roles), {
          separator : ', ',
          lastSeparator : ' ' + this.messages.andLabel + ' '
        });
      }
    },
    computed : {
      incumbentUserId : function() {
        return this.reassignmentData.incumbent && this.reassignmentData.incumbent.id;
      },
      substituteUserId : function() {
        return this.reassignmentData.substitute && this.reassignmentData.substitute.id;
      },
      workflowRoleFilter : function() {
        if (this.roleManager) {
          return this.$filters.mapRoleName(this.roleManager.getRolesOfComponentInstance());
        }
      },
      incumbentRoles : function() {
        if (this.incumbentUserId) {
          return this.roleManager.getRolesOfUser(this.incumbentUserId);
        }
      },
      formattedIncumbentRoles : function() {
        return this.formatRoles(this.incumbentRoles);
      },
      substituteRoleFilter : function() {
        if (this.incumbentUserId) {
          return this.$filters.mapRoleName(this.roleManager.getRolesOfUser(this.incumbentUserId));
        }
      },
      substituteRoles : function() {
        if (this.substituteUserId) {
          return this.roleManager.getRolesOfUser(this.substituteUserId);
        }
      },
      formattedSubstituteRoles : function() {
        return this.formatRoles(this.substituteRoles);
      },
      notMatchingRoles : function() {
        if (this.incumbentRoles && this.substituteRoles) {
          return this.roleManager.getNotMatchingRoles(this.incumbentUserId, this.substituteUserId);
        }
      },
      notMatchingFormattedRoles : function() {
        return this.formatRoles(this.notMatchingRoles);
      }
    }
  };

  SpVue.component('workflow-reassignment-management',
      reassignmentAsyncComponentRepository.get('management', {
        mixins : [VuejsApiMixin, VuejsI18nTemplateMixin, VuejsProgressMessageMixin],
        inject : ['context'],
        data : function() {
          return {
            reassignmentPopinApi : undefined
          };
        },
        created : function() {
          this.extendApiWith({
            openReassignment : this.openReassignment
          });
        },
        methods : {
          openReassignment : function() {
            this.reassignmentPopinApi.open();
          }
        }
      }));

  SpVue.component('workflow-reassignment-popin',
    reassignmentAsyncComponentRepository.get('popin', {
      mixins : [VuejsDefaultFormPanePopinApiMixin, VuejsI18nTemplateMixin, VuejsProgressMessageMixin],
      emits : ['reassignment-applied'],
      inject : ['context'],
      created : function() {
        this.extendApiWith({
          open : this.open,
        });
      },
      methods : {
        open : function() {
          this.formApi.initFormData();
          this.$super(VuejsDefaultFormPanePopinApiMixin).open();
        },
        validate : function() {
          return this.formPaneApi.validate().then(this.applyReassignment);
        },
        applyReassignment : function(formPaneData) {
          this.showProgressMessage();
          const formData = sp.form.toFormData({
            sourceUserId : formPaneData.incumbent.id,
            destinationUserId : formPaneData.substitute.id,
          });
          return sp.ajaxRequest('adminReplaceInAllTasks').byPostMethod().send(formData).then(function() {
            this.$emit('reassignment-applied');
            this.hideProgressMessage();
          }.bind(this))['catch'](function() {
            SilverpeasError.add(sp.i18n.get("GML.error.help")).show();
            this.hideProgressMessage();
          }.bind(this));
        }
      }
    }));

  SpVue.component('workflow-reassignment-form',
    reassignmentAsyncComponentRepository.get('form', {
      mixins : [VuejsFormApiMixin, VuejsI18nTemplateMixin, ReassignmentRoleManagerMixin],
      inject : ['context', 'rootFormMessages'],
      data : function() {
        return {
          selectIncumbentApi : undefined,
          selectSubstituteApi : undefined,
          substituteRoleFilterItems : undefined
        };
      },
      created : function() {
        this.extendApiWith({
          initFormData : this.initFormData,
          validateForm : function() {
            const data = this.reassignmentData;
            if (data.substitute.id === data.incumbent.id) {
              this.rootFormApi.errorMessage().add(
                  this.formatMessage(this.rootFormMessages.mustBeDifferentFrom,
                      [this.messages.substituteLabel, this.messages.incumbentLabel]));
            }
            if (this.notMatchingRoles && this.notMatchingRoles.length > 0) {
              this.rootFormApi.errorMessage().add(
                  this.formatMessage(this.messages.mustHaveSameRoles));
            }
            return this.rootFormApi.errorMessage().none();
          },
          updateFormData : function(formPaneData) {
            formPaneData.incumbent = extendsObject({}, this.reassignmentData.incumbent);
            formPaneData.substitute = extendsObject({}, this.reassignmentData.substitute);
          }
        });
      },
      methods : {
        initFormData : function(){
          this.resetMixinData();
          this.selectIncumbentApi.refresh();
          this.selectSubstituteApi.refresh();
        },
        applyUserFullName : function(userData) {
          userData.fullName = '';
          if (userData.id) {
            User.get(userData.id).then(function(user) {
              userData.fullName = user.fullName;
            });
          }
        },
        incumbentChanged : function(userIds) {
          this.reassignmentData.incumbent.id = userIds.length ? userIds[0] : '';
        },
        substituteChanged : function(userIds) {
          this.reassignmentData.substitute.id = userIds.length ? userIds[0] : '';
          this.applyUserFullName(this.reassignmentData.substitute);
        }
      }
    }));
})();