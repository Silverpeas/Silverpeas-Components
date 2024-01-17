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

//# sourceURL=/community/jsp/javaScript/services/silverpeas-community-membership-service.js

(function() {

  /**
   * Constructor of community membership services.
   * @param context is an object containing :
   * context =  {
   *   currentUser : [the current user behind the session]
   *   componentInstanceId : [the identifier of the community component instance],
   *   spaceId : [identifier of the space],
   *   spaceLabel : [label of the space]
   * }
   * @constructor
   */
  window.CommunityMembershipService = function(context) {
    const repository = new CommunityMembershipRepository(context);

    /**
     * Gets the membership of current user.
     * @param userId identifier of a user.
     * @returns {*}
     */
    this.getOwnMembershipOf = function() {
      return repository.getMembershipOf(currentUser.id);
    };

    /**
     * Gets paginated members.
     */
    this.getMembers = function() {
      return repository.getMembers();
    };

    /**
     * Makes the current user behind the session joining the community linked to component instance
     * represented by the identifier used to initialize the service.
     * @returns {*}
     */
    this.join = function() {
      return repository.join();
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
      return repository.validateJoinRequest(user, accept, message);
    }

    /**
     * Makes the current user behind the session leaving the community linked to component instance
     * represented by the identifier used to initialize the service.
     * @param reason, an object composed of:
     * <code>
     *   {
     *     reason: integer that represents the index into the list of reasons,
     *     message: string representing a message given by the user to explain more precisely its leaving,
     *     contactInFuture: boolean to indicate that the member accepts or not to be contacted in the future about its leaving
     *   }
     * </code>
     * @returns {*}
     */
    this.leave = function(reason) {
      return repository.leave(reason);
    }
  };

  const CommunityMember = function() {
    this.type = 'CommunityMember';
    this.$onInit = function() {
    };
  };

  const CommunityMembershipRepository = function(context) {
    const baseUri = webContext + "/services/community/" + context.componentInstanceId + '/memberships';
    const baseAdapter = RESTAdapter.get(baseUri, CommunityMember);
    const ctlBaseUri = webContext + "/Rcommunity/" + context.componentInstanceId;
    const ctlBaseAdapter = RESTAdapter.get(ctlBaseUri, CommunityMember);

    /**
     * Gets the membership of given user.
     * @param userId a user identifier.
     * @returns {*}
     */
    this.getMembershipOf = function(userId) {
      let targetedUser = userId === currentUser.id ? 'me' : userId;
      return  baseAdapter.find({url : baseAdapter.url + '/users/' + targetedUser});
    };

    /**
     * Gets paginated members.
     */
    this.getMembers = function() {
      // TODO handle the pagination parameter. For instance it is fixed
      let criteria = baseAdapter.criteria({
        page: {
          number: 1,
          size: 1
        }});
      return  baseAdapter.find({url : baseAdapter.url + '/members', criteria: criteria}).then(function(members) {
        const memberships = members.memberships;
        memberships.realSize = members.realSize;
        return memberships;
      });
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
     * @param reason, an object composed of:
     * <code>
     *   {
     *     reason: integer that represents the index into the list of reasons,
     *     message: string representing a message given by the user to explain more precisely its leaving,
     *     contactInFuture: boolean to indicate that the member accepts or not to be contacted in the future about its leaving
     *   }
     * </code>
     * @returns {*}
     */
    this.leave = function(reason) {
      const formData = new FormData();
      formData.set("reason", reason.reason);
      formData.set("message", reason.message);
      formData.set("contactInFuture", reason.contactInFuture);
      return ctlBaseAdapter.post(ctlBaseAdapter.url + '/members/leave', formData);
    };
  };

  window.CommunityMembershipService.prototype.Constants = {
    MembershipStatus : {
      COMMITTED : 'COMMITTED',
      PENDING : 'PENDING',
      REFUSED : 'REFUSED',
      REMOVED : 'REMOVED'
    }
  }
})();
