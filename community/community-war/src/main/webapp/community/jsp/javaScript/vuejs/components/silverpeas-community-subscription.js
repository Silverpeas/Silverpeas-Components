/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

//# sourceURL=/community/jsp/javaScript/services/silverpeas-community-subscription.js

(function() {

  const templateRepository = new VueJsAsyncComponentTemplateRepository(webContext +
      '/community/jsp/javaScript/vuejs/components/silverpeas-community-subscription-templates.jsp');

  Vue.component('silverpeas-community-subscription',
      templateRepository.get('community-subscription', {
        mixins : [VuejsApiMixin, VuejsI18nTemplateMixin],
        inject : ['context', 'subscriptionService'],
        provide : function() {
          return {
            subscriptionService: this.subscriptionService
          }
        },
        data : function() {
          return {
            validateJoinRequestPopinApi : undefined,
            validateJoinRequestPopinCtx : {
              user : {},
              accept : false,
              message : ''
            },
            members : undefined
          };
        },
        created : function() {
          this.extendApiWith({
            join : function() {
              return this.subscriptionService.join();
            },
            leave : function() {
              return this.subscriptionService.leave();
            },
            validateJoinRequest : function(userId, accept) {
              this.validateJoinRequest(userId, accept);
            }
          });
          this.loadMembers();
        },
        methods : {
          loadMembers : function() {
            return this.subscriptionService.getMembers().then(function(members) {
              this.members = members;
            }.bind(this));
          },
          refreshOwnMembership : function() {
            return this.subscriptionService.getOwnMembershipOf().then(function(currentUserMembership) {
              if (typeof currentUserMembership === 'object') {
                if (this.subscriptionService.Constants.MembershipStatus.COMMITTED === currentUserMembership.status) {
                  this.context.currentUser.isMember = true;
                  this.context.currentUser.isMembershipPending = false;
                } else if (this.subscriptionService.Constants.MembershipStatus.PENDING === currentUserMembership.status) {
                  this.context.currentUser.isMember = false;
                  this.context.currentUser.isMembershipPending = true;
                } else {
                  this.context.currentUser.isMember = false;
                  this.context.currentUser.isMembershipPending = false;
                }
              }
            }.bind(this));
          },
          join : function() {
            return this.subscriptionService.join().then(function() {
              this.refreshOwnMembership();
              this.$emit('membership-join-request');
              return this.loadMembers();
            }.bind(this));
          },
          validateJoinRequest : function(userId, accept) {
            User.get(userId).then(function(user) {
              const data = this.validateJoinRequestPopinCtx;
              data.user = user;
              data.accept = accept;
              this.validateJoinRequestPopinApi.open({
                callback : function() {
                  this.subscriptionService.validateJoinRequest(data.user, data.accept, data.message).then(function() {
                    data.message = '';
                    this.loadMembers();
                    this.refreshOwnMembership();
                    this.$emit('join-request-validated');
                  }.bind(this));
                }.bind(this)
              });
            }.bind(this));
          },
          leave : function() {
            return this.subscriptionService.leave().then(function() {
              this.context.currentUser.isMember = false;
              this.context.currentUser.isMembershipPending = false;
              return this.loadMembers();
            }.bind(this));
          }
        },
        computed : {
          spaceLabel : function() {
            return this.context.spaceLabel;
          },
          isMember : function() {
            return this.context.currentUser.isMember;
          },
          isMembershipPending : function() {
            return this.context.currentUser.isMembershipPending;
          },
          nbMembers : function() {
            return this.members.length;
          }
        }
      }));
})();
