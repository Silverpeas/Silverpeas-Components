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

//# sourceURL=/community/jsp/javaScript/services/silverpeas-community-service.js

(function() {

  /**
   * Constructor of community services.
   * @param context is an object containing :
   * context =  {
   *   currentUser : [the current user behind the session]
   *   componentInstanceId : [the identifier of the community component instance],
   *   spaceId : [identifier of the space],
   *   spaceLabel : [label of the space]
   * }
   * @constructor
   */
  window.CommunityService = function(context) {
    const communityRepository = new CommunityRepository(context);

    /**
     * Gets the community.
     * @returns {*}
     */
    this.get = function() {
      return communityRepository.get();
    };

    /**
     * Saves the community space homepage.
     * @param community the aimed community with space homepage data.
     * @returns {*}
     */
    this.saveSpaceHomepage = function(community) {
      return communityRepository.save(community);
    };

    /**
     * Saves the community space homepage.
     * @param community the aimed community with space homepage data.
     * @param parameters parameters to update. For now :
     * <code>
     *   parameters = {
     *     displayCharterOnSpaceHomepage :  [boolean]
     *   }
     * </code>
     * @returns {*}
     */
    this.saveCharter = function(community, parameters) {
      return sp.promise.whenAllResolved([
          communityRepository.save(community),
          communityRepository.saveDisplayCharterOnSpaceHomepageParameter(parameters.displayCharterOnSpaceHomepage)]).then(function(results) {
        return results[0];
      });
    };
  };

  const CommunityOfUsers = function() {
    this.type = 'CommunityOsUsers';
    this.$onInit = function() {
    };
  };

  const CommunityRepository = function(context) {
    const baseUri = webContext + "/services/community/" + context.componentInstanceId;
    const baseAdapter = RESTAdapter.get(baseUri, CommunityOfUsers);
    const ctlBaseUri = webContext + "/Rcommunity/" + context.componentInstanceId;
    const ctlBaseAdapter = RESTAdapter.get(ctlBaseUri, function() {});

    /**
     * Gets the community.
     * @returns {*}
     */
    this.get = function() {
      return  baseAdapter.find();
    };

    /**
     * Saves the given community.
     * @param community the community to save.
     * @returns {*}
     */
    this.save = function(community) {
      return baseAdapter.put(community);
    };

    this.saveDisplayCharterOnSpaceHomepageParameter = function(value) {
      const formData = new FormData();
      formData.set("value", !!value);
      return ctlBaseAdapter.post(
          ctlBaseAdapter.url + '/parameters/displayCharterOnSpaceHomepage',
          formData);
    }
  };
})();
