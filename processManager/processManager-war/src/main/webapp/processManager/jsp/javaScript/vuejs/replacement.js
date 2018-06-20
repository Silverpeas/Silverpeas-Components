/*
 * Copyright (C) 2000 - 2018 Silverpeas
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

(function() {

  var __activateRights = function(replacement) {
    replacement.canBeModified = true;
    replacement.canBeDeleted = true;
  };

  Vue.component('workflow-replacement-module', function(resolve) {
    sp.ajaxRequest(webContext + '/processManager/jsp/javaScript/vuejs/replacement-module.jsp').send().then(function(request) {
      resolve({
        mixins : [VuejsApiMixin],
        inject : ['context', 'replacementService'],
        template: request.responseText,
        data : function() {
          return {
            replacementApi : undefined,
            incumbentList : undefined,
            substituteList : undefined,
            allList : undefined
          };
        },
        created : function() {
          this.extendApiWith({
            add : function() {
              this.replacementApi.add();
            },
            reload : function() {
              this.replacementService.getAllAsIncumbent(this.context.currentUser.id).then(
                  function(replacements) {
                    replacements.forEach(__activateRights);
                    this.incumbentList = replacements;
                  }.bind(this));
              this.replacementService.getAllAsSubstitute(this.context.currentUser.id).then(
                  function(replacements) {
                    if (this.context.currentUser.isSupervisor) {
                      replacements.forEach(__activateRights);
                    }
                    this.substituteList = replacements;
                  }.bind(this));
              if (this.context.currentUser.isSupervisor) {
                this.replacementService.getAll(this.context.currentUser.id).then(
                    function(replacements) {
                      replacements.forEach(__activateRights);
                      this.allList = replacements;
                    }.bind(this));
              }
            }
          });
          this.api.reload();
        },
        computed : {
          fullDisplay : function() {
            return this.context.currentUser.isSupervisor;
          }
        }
      });
    });
  });

  var ReplacementEntityMixin = {
    computed : {
      isCreation : function() {
        return StringUtil.isNotDefined(this.replacement.uri);
      }
    }
  };

  Vue.component('workflow-replacement-management', function(resolve) {
    sp.ajaxRequest(webContext + '/processManager/jsp/javaScript/vuejs/replacement-management.jsp').send().then(function(request) {
      resolve({
        mixins : [VuejsApiMixin, VuejsI18nTemplateMixin, ReplacementEntityMixin],
        inject : ['context', 'replacementService'],
        template: request.responseText,
        data : function() {
          return {
            replacement : {},
            addPopinApi : undefined,
            addFormApi : undefined,
            modifyPopinApi : undefined,
            modifyFormApi : undefined,
            deletePopinApi : undefined
          };
        },
        created : function() {
          this.extendApiWith({
            view : function(replacementToView) {
              notyReset();
              this.replacement = replacementToView;
            },
            add : function() {
              this.api.modify()
            },
            modify : function(replacementToModify) {
              notyReset();
              this.replacement =
                  extendsObject(this.replacementService.getTools().newReplacementEntity(),
                      replacementToModify);
              if (this.isCreation) {
                this.replacement.incumbent.id = this.context.currentUser.id;
                this.addPopinApi.open({
                  callback : function() {
                    return this.addFormApi.validate().then(function(replacementToCreate) {
                      this.saveReplacement(replacementToCreate);
                    }.bind(this));
                  }.bind(this)
                });
              } else {
                this.modifyPopinApi.open({
                  callback : function() {
                    return this.modifyFormApi.validate().then(function(replacementToModify) {
                      this.saveReplacement(replacementToModify);
                    }.bind(this));
                  }.bind(this)
                });
              }
            },
            remove : function(replacementToDelete) {
              notyReset();
              this.replacement = replacementToDelete;
              this.deletePopinApi.open({
                callback : function() {
                  return this.deleteReplacement(replacementToDelete);
                }.bind(this)
              });
            }
          });
        },
        methods : {
          saveReplacement : function(replacement) {
            return this.replacementService.saveReplacement(replacement).then(function(replacement) {
              if (this.isCreation) {
                this.$emit('replacement-create', replacement)
              } else {
                this.$emit('replacement-update', replacement)
              }
            }.bind(this));
          },
          deleteReplacement : function(replacement) {
            return this.replacementService.deleteReplacement(replacement).then(function() {
              console.log('replacement-delete', replacement);
              this.$emit('replacement-delete', replacement)
            }.bind(this));
          }
        }
      });
    });
  });

  Vue.component('workflow-replacement-form', function(resolve) {
    sp.ajaxRequest(webContext + '/processManager/jsp/javaScript/vuejs/replacement-form.jsp').send().then(function(request) {
      resolve({
        mixins : [VuejsFormApiMixin, VuejsI18nTemplateMixin, ReplacementEntityMixin],
        inject : ['context'],
        template: request.responseText,
        props : {
          replacement : Object
        },
        data : function() {
          return {
            selectIncumbentApi : undefined,
            selectSubstituteApi : undefined,
            startDateStatus : undefined,
            endDateStatus : undefined
          };
        },
        created : function() {
          this.extendApiWith({
            validate : function() {
              return !SilverpeasError.existsAtLeastOne();
            },
            updateData : function(replacementToUpdate) {
              extendsObject(replacementToUpdate, this.replacement);
            }
          });
        },
        watch : {
          replacement : function() {
            Vue.nextTick(function() {
              this.selectIncumbentApi.refresh();
              this.selectSubstituteApi.refresh();
            }.bind(this));
          }
        },
        methods : {
          incumbentChanged : function(userIds) {
            this.replacement.incumbent.id = userIds.length ? userIds[0] : '';
          },
          substituteChanged : function(userIds) {
            this.replacement.substitute.id = userIds.length ? userIds[0] : '';
          }
        },
        computed : {
          roleFilter : function() {
            var roles = [];
            if (this.context.currentUser.isSupervisor) {
              for (var roleName in this.context.replacementHandledRoles) {
                roles.push(roleName);
              }
            } else {
              roles.push(this.context.currentUser.role);
            }
            return roles;
          }
        }
      });
    });
  });

  Vue.component('workflow-replacement-list-item', function(resolve) {
    sp.ajaxRequest(webContext + '/processManager/jsp/javaScript/vuejs/replacement-list-item.jsp').send().then(function(request) {
      resolve({
        template: request.responseText,
        props : {
          replacement : Object
        },
        computed : {
          isOneDay : function() {
            return this.replacement.startDate === this.replacement.endDate;
          }
        }
      });
    });
  });

  Vue.component('workflow-replacement-list-item-actions', function(resolve) {
    sp.ajaxRequest(webContext + '/processManager/jsp/javaScript/vuejs/replacement-list-item-actions.jsp').send().then(function(request) {
      resolve({
        template: request.responseText,
        props : {
          replacement : {
            'type' : Object,
            'default' : function() {
              return {};
            }
          }
        }
      });
    });
  });
})();