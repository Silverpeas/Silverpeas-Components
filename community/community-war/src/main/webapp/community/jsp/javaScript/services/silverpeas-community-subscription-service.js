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

//# sourceURL=/community/jsp/javaScript/services/silverpeas-community-subscription-service.js

(function() {

  sp.i18n.load({
    bundle : 'org.silverpeas.components.community.multilang.communityBundle',
    async : true
  });

  /**
   * Constructor of subscription services.
   * @param context is an object containing :
   * context =  {
   *   currentUser : [the current user behind the session] {
   *     isMember : [true if current user is a member],
   *     isMembershipPending : [true if current user has requested to be a member, but the
   *                            request has not been yet validated],
   *     isAdmin : [true if current user has administration role]
   *   }
   *   componentInstanceId : [the identifier of the community component instance],
   *   spaceLabel : [label of the space]
   * }
   * @constructor
   */
  window.CommunitySubscriptionService = function(context) {
    const subscriptionRepository = new CommunitySubscriptionRepository(context);

    /**
     * Gets the membership of current user.
     * @param userId identifier of a user.
     * @returns {*}
     */
    this.getOwnMembershipOf = function() {
      return subscriptionRepository.getMembershipOf(currentUser.id);
    };

    /**
     * Gets all members.
     */
    this.getMembers = function() {
      return subscriptionRepository.getMembers();
    };

    /**
     * Makes the current user behind the session joining the community linked to component instance
     * represented by the identifier used to initialize the service.
     * @returns {*}
     */
    this.join = function() {
      return subscriptionRepository.join();
    }

    /**
     * Validates the request of user to become a member of a community.
     * @param user the user that performed the request.
     * @param accept boolean to indicate the administrator decision. 'true' for accepting, 'false'
     *     otherwise.
     * @param message an additional message completing the administrator decision.
     * @returns {*}
     */
    this.validateJoinRequest = function(user, accept, message) {
      return subscriptionRepository.validateJoinRequest(user, accept, message);
    }

    /**
     * Makes the current user behind the session leaving the community linked to component instance
     * represented by the identifier used to initialize the service.
     * @returns {*}
     */
    this.leave = function() {
      return subscriptionRepository.leave();
    }
  };

  const CommunityMember = function() {
    this.type = 'CommunityMember';
    this.$onInit = function() {
    };
  };

  const CommunitySubscriptionRepository = function(context) {
    const baseUri = webContext + "/services/community/" + context.componentInstanceId;
    const baseAdapter = RESTAdapter.get(baseUri, CommunityMember);
    const ctlBaseUri = webContext + "/Rcommunity/" + context.componentInstanceId;
    const ctlBaseAdapter = RESTAdapter.get(ctlBaseUri, CommunityMember);

    /**
     * Gets the membership of given user.
     * @param userId a user identifier.
     * @returns {*}
     */
    this.getMembershipOf = function(userId) {
      userId = userId === currentUser.id ? 'me' : userId;
      return baseAdapter.find({url : baseAdapter.url + '/members/' + userId});
    };

    /**
     * Gets all members.
     */
    this.getMembers = function() {
      return baseAdapter.find({url : baseAdapter.url + '/members'});
    };

    /**
     * Performs the join of the current user.
     */
    this.join = function() {
      return ctlBaseAdapter.post(ctlBaseAdapter.url + '/members/join', {});
    };

    /**
     * Validates the request of user to become a member of a community.
     * @param user the user that performed the request.
     * @param accept boolean to indicate the administrator decision. 'true' for accepting, 'false'
     *     otherwise.
     * @param message an additional message completing the administrator decision.
     * @returns {*}
     */
    this.validateJoinRequest = function(user, accept, message) {
      const formData = new FormData();
      formData.set("accept", accept);
      formData.set("message", message);
      return ctlBaseAdapter.post(ctlBaseAdapter.url + '/members/join/validate/' + user.id, formData);
    }

    /**
     * Performs the leave of the current user.
     * @returns {*}
     */
    this.leave = function() {
      return ctlBaseAdapter.post(ctlBaseAdapter.url + '/members/leave', {});
    };
  };

  window.CommunitySubscriptionService.prototype.Constants = {
    MembershipStatus : {
      COMMITTED : 'COMMITTED',
      PENDING : 'PENDING',
      REFUSED : 'REFUSED',
      REMOVED : 'REMOVED'
    }
  }
})();
