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

  /**
   * Common workflow service.
   * @param componentInstanceId the identifier of the workflow component instance.
   * @param handledRoles array of roles managed by the workflow or by a feature of the workflow.
   * @constructor
   */
  window.WorkflowService = function(componentInstanceId, handledRoles) {

    /**
     * Gets user identifiers mapped with the related given roles.
     * @returns {*}
     */
    this.promiseRoleManager = function() {
      const mappingOfUserRoles = {};
      const promises = [];
      const __load = function(roleName) {
        return User.get({
          component : componentInstanceId,
          userStatesToExclude : ['DEACTIVATED'],
          includeRemoved : true,
          roles : roleName
        }).then(function(users) {
          users.forEach(function(user) {
            let userRoles = mappingOfUserRoles[user.id];
            if (!userRoles) {
              userRoles = [];
              mappingOfUserRoles[user.id] = userRoles;
            }
            userRoles.push(roleName);
          });
        });
      };
      for (let roleName in handledRoles) {
        handledRoles[roleName].name = roleName;
        promises.push(__load(roleName));
      }
      return sp.promise.whenAllResolved(promises).then(function() {
        return new function() {

          /**
           * Gets roles of the component instance into context of a workflow or one of its features.
           * @returns {*}
           */
          this.getRolesOfComponentInstance = function() {
            const roles = [];
            for (let roleName in handledRoles) {
              roles.push(extendsObject({}, handledRoles[roleName]));
            }
            return roles;
          };

          /**
           * Gets roles of the given user into context of a workflow or one of its features.
           * @param userId identifier of a user for which are requested.
           * @returns {*}
           */
          this.getRolesOfUser = function(userId) {
            const roles = [];
            const mappingOfUserRole = mappingOfUserRoles[userId] || [];
            mappingOfUserRole.forEach(function(roleName) {
              roles.push(extendsObject({}, handledRoles[roleName]));
            });
            return roles;
          };

          /**
           * Gets matching roles between two users. No roles if one of both is missing.
           * @param userId the identifier of a first user.
           * @param otherUserId the identifier of a second user.
           * @returns {*}
           */
          this.getMatchingRoles = function(userId, otherUserId) {
            if (userId && otherUserId) {
              const userRoles = this.getRolesOfUser(userId);
              const substituteRoles = this.getRolesOfUser(otherUserId);
              return userRoles.filter(function(role) {
                return substituteRoles.indexOfElement(role, 'name') >= 0;
              });
            }
          };

          /**
           * Gets roles that are attributed to a user (first parameter) but not another one (second
           * parameter). No roles if one of both is missing.
           * @param userId the identifier of a first user.
           * @param otherUserId the identifier of a second user.
           * @returns {*}
           */
          this.getNotMatchingRoles = function(userId, otherUserId) {
            if (userId && otherUserId) {
              const userRoles = this.getRolesOfUser(userId);
              const substituteRoles = this.getRolesOfUser(otherUserId);
              return userRoles.filter(function(role) {
                return substituteRoles.indexOfElement(role, 'name') < 0;
              });
            }
          };
        };
      });
    };
  }
})();
