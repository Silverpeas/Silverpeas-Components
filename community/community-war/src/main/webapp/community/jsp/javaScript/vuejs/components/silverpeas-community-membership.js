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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

//# sourceURL=/community/jsp/javaScript/services/silverpeas-community-membership.js

(function() {

  const templateRepository = new VueJsAsyncComponentTemplateRepository(webContext +
      '/community/jsp/javaScript/vuejs/components/silverpeas-community-membership-templates.jsp');

  SpVue.component('silverpeas-community-membership',
      templateRepository.get('community-membership', {
        mixins : [VuejsApiMixin, VuejsI18nTemplateMixin],
        inject : ['context', 'communityService', 'membershipService'],
        emits : ['membership-join', 'membership-pending', 'membership-leave',
          'membership-request-accepted', 'membership-request-refused'],
        props : {
          displayNbMembersForNonMembers : {
            'type' : Boolean,
            'default' : false
          },
          displayCharterOnSpaceHomepage : {
            'type' : Boolean,
            'default' : false
          }
        },
        data : function() {
          return {
            community : undefined,
            acceptCharterPopinApi : undefined,
            validateJoinRequestCtx : {
              popinApi : undefined,
              user : {},
              accept : false,
              message : ''
            },
            leaveCtx : {
              popinApi : undefined,
              formPaneApi : undefined,
              formApi : undefined
            },
            ownMembership : undefined,
            members : undefined
          };
        },
        created : function() {
          this.loadCommunity();
          this.extendApiWith({
            loadCommunity : this.loadCommunity,
            join : this.join,
            leave : this.leave,
            validateJoinRequest : this.validateJoinRequest
          });
          this.refreshOwnMembership();
          this.loadMembers();
        },
        methods : {
          loadCommunity : function(community) {
            if (community) {
              this.community = community;
              return sp.promise.resolveDirectlyWith(community);
            }
            return this.communityService.get().then(function(community) {
              this.community = community;
            }.bind(this));
          },
          loadMembers : function() {
            return this.membershipService.getMembers().then(function(members) {
              this.members = members;
            }.bind(this));
          },
          refreshOwnMembership : function() {
            return this.membershipService.getOwnMembershipOf().then(function(currentUserMembership) {
              if (typeof currentUserMembership === 'object') {
                if (currentUserMembership.status) {
                  this.ownMembership = currentUserMembership;
                } else {
                  this.ownMembership = undefined;
                }
              }
            }.bind(this), function() {
              this.ownMembership = undefined;
            }.bind(this));
          },
          join : function() {
            const _join = function() {
              return this.membershipService.join().then(function() {
                this.refreshOwnMembership().then(function() {
                  if (this.isMember) {
                    this.$emit('membership-join');
                  } else if (this.isMembershipPending) {
                    this.$emit('membership-pending');
                  }
                }.bind(this));
                return this.loadMembers();
              }.bind(this));
            }.bind(this);
            if (this.community.charterURL) {
              return new Promise(function(resolve) {
                this.acceptCharterPopinApi.open({
                  callback : function() {
                    return _join().then(function() {
                      resolve();
                    });
                  }
                });
              }.bind(this));
            }
            return _join();
          },
          validateJoinRequest : function(userId, accept) {
            User.get(userId).then(function(user) {
              const data = this.validateJoinRequestCtx;
              data.user = user;
              data.accept = accept;
              this.validateJoinRequestCtx.popinApi.open({
                callback : function() {
                  this.membershipService.validateJoinRequest(data.user, data.accept, data.message).then(function() {
                    data.message = '';
                    this.loadMembers();
                    this.refreshOwnMembership();
                    if (accept) {
                      this.$emit('membership-request-accepted');
                    } else {
                      this.$emit('membership-request-refused');
                    }
                  }.bind(this));
                }.bind(this)
              });
            }.bind(this));
          },
          leave : function() {
            this.leaveCtx.popinApi.open({
              callback : function() {
                this.leaveCtx.formPaneApi.validate().then(function(formPaneData) {
                  this.membershipService.leave(formPaneData).then(function() {
                    this.ownMembership = undefined;
                    this.leaveCtx.formApi.clearForm();
                    this.$emit('membership-leave');
                    return this.loadMembers();
                  }.bind(this));
                }.bind(this));
              }.bind(this)
            });
          }
        },
        computed : {
          spaceLabel : function() {
            return this.context.spaceLabel;
          },
          isMember : function() {
            return this.ownMembership && this.ownMembership.status === this.membershipService.Constants.MembershipStatus.COMMITTED;
          },
          isMembershipPending : function() {
            return this.ownMembership && this.ownMembership.status === this.membershipService.Constants.MembershipStatus.PENDING;
          },
          nbMembers : function() {
            return this.members.realSize;
          }
        }
      }));

  SpVue.component('silverpeas-community-charter-accept',
      templateRepository.get('charter-accept', {
        mixins : [VuejsApiMixin, VuejsI18nTemplateMixin, VuejsTopPopinMixin, VuejsProgressMessageMixin],
        props : {
          community : {
            'type' : Object,
            'required' : true
          }
        },
        data : function() {
          return {
            topPopinApi : undefined,
            contentApi : undefined
          };
        },
        created : function() {
          this.extendApiWith({
            open : this.open
          });
          this.registerTopPopinApiName('topPopinApi');
          __promiseCharterContentApi(this).then(function(api) {
            this.contentApi = api;
          }.bind(this))
        },
        methods : {
          open : function(params) {
            if (this.contentApi) {
              const popinSettings = extendsObject({
                title : this.messages.acceptCharterTitle,
                minWidth : 680
              }, params);
              this.topPopinApi.open('acceptation', popinSettings, function(ctx) {
                return this.contentApi.renderWith(ctx, this.community);
              }.bind(this));
            }
          }
        }
      }));

  SpVue.component('silverpeas-community-charter-preview',
      templateRepository.get('charter-preview', {
        mixins : [VuejsApiMixin, VuejsI18nTemplateMixin, VuejsTopPopinMixin, VuejsProgressMessageMixin],
        props : {
          community : {
            'type' : Object,
            'required' : true
          }
        },
        data : function() {
          return {
            topPopinApi : undefined,
            contentApi : undefined
          };
        },
        created : function() {
          this.registerTopPopinApiName('topPopinApi');
          __promiseCharterContentApi(this).then(function(api) {
            this.contentApi = api;
          }.bind(this))
        },
        methods : {
          open : function() {
            const popinSettings = {
              title : this.messages.charterLink,
              minWidth : 680
            };
            this.topPopinApi.open('information', popinSettings, function(ctx) {
              return this.contentApi.renderWith(ctx, this.community);
            }.bind(this));
          }
        }
      }));

  const __promiseCharterContentApi = function(component) {
    return templateRepository.getComponentConfiguration('charter-content', {}).then(function(cfg) {
      return new function() {
        const templateAsString = cfg.template;
        this.renderWith = function(ctx, community) {
          setTimeout(component.showProgressMessage, 0);
          const charterContentCss = {
            href: CommunityMembershipSettings.get('c.m.s.u'),
            deferred : sp.promise.deferred()
          };
          ctx.popinSettings.cssHrefUrls.push(charterContentCss);
          const $content = jQuery(templateAsString)[0];
          const $iframe = $content.querySelector('iframe');
          let __onceLoad = function() {
            $iframe.removeEventListener('load', __onceLoad);
            charterContentCss.deferred.promise.then(function() {
              component.hideProgressMessage();
            })
          };
          $iframe.addEventListener('load', __onceLoad);
          $iframe.setAttribute('src', community.charterURL);
          $iframe.removeAttribute('frameborder');
          $iframe.style.width = ctx.popinSettings.minWidth + 'px';
          $iframe.style.height = ctx.popinSettings.maxHeight + 'px';
          ctx.$rootContainer.appendChild($content);
        };
      };
    });
  };

  SpVue.component('silverpeas-community-leave-form',
      templateRepository.get('leave-form', {
        mixins : [VuejsFormApiMixin],
        props : {
        },
        data : function() {
          return {
            reasonLabels : JSON.parse(CommunityMembershipSettings.get('c.m.l.r')),
            reason : 0,
            message : undefined,
            contactInFuture : undefined
          };
        },
        created : function() {
          this.extendApiWith({
            clearForm : function() {
              this.reason = 0;
              this.message = '';
              this.contactInFuture = false;
            },
            validateForm : function() {
              return this.rootFormApi.errorMessage().none();
            },
            updateFormData : function(formPaneData) {
              formPaneData.reason = this.reason;
              formPaneData.message = this.message;
              formPaneData.contactInFuture = this.contactInFuture;
            }
          });
          this.api.clearForm();
        }
      }));
})();
