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

  // the type Replacement
  var Replacement = function() {
    this.type = 'Replacement';
  };

  var __adjustEndDate = function(replacement, forSend) {
    var offset = typeof forSend === 'boolean' && forSend ? 1 : -1;
    var newEndMoment = sp.moment.make(replacement.endDate).add(offset, 'days');
    replacement.endDate = sp.moment.formatAsLocalDate(newEndMoment);
  };

  /**
   * Gets user identifiers mapped with the related given roles.
   * @param context the replacement module context.
   * @returns {*}
   */
  var __initRoleManager = function(context) {
    var mappingOfUserRoles = {};
    var promises = [];
    var __load = function(roleName) {
      return User.get({
        component : context.componentInstanceId,
        userStatesToExclude : ['DEACTIVATED'],
        roles : roleName
      }).then(function(users) {
        users.forEach(function(user) {
          var userRoles = mappingOfUserRoles[user.id];
          if (!userRoles) {
            userRoles = [];
            mappingOfUserRoles[user.id] = userRoles;
          }
          userRoles.push(roleName);
        });
      });
    };
    for (var roleName in context.replacementHandledRoles) {
      context.replacementHandledRoles[roleName].name = roleName;
      promises.push(__load(roleName));
    }
    return sp.promise.whenAllResolved(promises).then(function() {
      return new function() {

        /**
         * Gets roles of the component instance into context of replacement.
         * @returns {*}
         */
        this.getRolesOfComponentInstance = function() {
          var roles = [];
          for (var roleName in context.replacementHandledRoles) {
            roles.push(extendsObject({}, context.replacementHandledRoles[roleName]));
          }
          return roles;
        };

        /**
         * Gets roles of the given user into context of replacement.
         * @param userId identifier of a user for which are requested.
         * @returns {*}
         */
        this.getRolesOfUser = function(userId) {
          var roles = [];
          var mappingOfUserRole = mappingOfUserRoles[userId] || [];
          mappingOfUserRole.forEach(function(roleName) {
            roles.push(extendsObject({}, context.replacementHandledRoles[roleName]));
          });
          return roles;
        };

        /**
         * Gets matching roles between the substitute and the incumbent. No roles if one of both is
         * not filled.
         * @param replacement the object representing replacement data.
         * @returns {*}
         */
        this.getMatchingRoles = function(replacement) {
          var roles;
          if (replacement.incumbent && replacement.incumbent.id &&
              replacement.substitute && replacement.substitute.id) {
            roles = this.getRolesOfUser(replacement.incumbent.id);
            var substituteRoles = this.getRolesOfUser(replacement.substitute.id);
            roles = roles.filter(function(role) {
              return substituteRoles.indexOfElement(role, 'name') >= 0;
            });
          }
          return roles;
        };
      };
    });
  };

  window.ReplacementService = SilverpeasClass.extend({
    initialize : function(context) {
      this.context = context;
      var baseUri = webContext + '/services/workflow/' + this.context.componentInstanceId + '/replacements';
      this.baseAdapter = RESTAdapter.get(baseUri, Replacement);
      this.roleManagerPromise = __initRoleManager(this.context);
    },

    getTools : function() {
      return {

        /**
         * Init a new replacement entity instance.
         */
        newReplacementEntity : function() {
          return this.getTools().extractReplacementEntityData();
        }.bind(this),

        /**
         * Extracts from an UI JavaScript bean the necessary data about the representation of a
         * replacement which can be sent to the server.
         * @param replacement
         */
        extractReplacementEntityData : function(replacement) {
          replacement = replacement ? replacement : {};
          var startDate = sp.moment.atZoneIdSimilarLocal(moment(), this.context.currentUser.zoneId);
          var newReplacement = new Replacement();
          newReplacement.uri = replacement.uri;
          newReplacement.incumbent = replacement.incumbent ? replacement.incumbent : {};
          newReplacement.substitute = replacement.substitute ? replacement.substitute : {};
          newReplacement.startDate = replacement.startDate ? replacement.startDate : startDate.format();
          newReplacement.endDate = replacement.endDate ? replacement.endDate : startDate.format();
          newReplacement.workflowId = replacement.workflowId ? replacement.workflowId : this.context.componentInstanceId;
          return newReplacement;
        }.bind(this)
      };
    },

    /**
     * Gets the role manager.
     * @returns {*}
     */
    promiseRoleManager : function() {
      return this.roleManagerPromise;
    },

    /**
     * Gets all replacement created by a user (so the incumbent) represented by its identifier.
     * @param userId identifier of a user which is the incumbent.
     * @returns {*}
     */
    getAllAsIncumbent : function(userId) {
      return this.baseAdapter.find({
        incumbent : userId
      }).then(function(replacements) {
        replacements.forEach(__adjustEndDate);
        return replacements;
      });
    },

    /**
     * Gets all replacement created by a user (so the incumbent) represented by its identifier.
     * @param userId identifier of a user which is the incumbent.
     * @returns {*}
     */
    getAllAsSubstitute : function(userId) {
      return this.baseAdapter.find({
        substitute : userId
      }).then(function(replacements) {
        replacements.forEach(__adjustEndDate);
        return replacements;
      });
    },

    /**
     * Gets all replacements optionally reduced by excluding those which the given user is
     * incumbent or substitute.
     * @param userIdToExclude (Optional) identifier of a user which has to be exclude from
     *     incumbent and substitute role.
     * @returns {*}
     */
    getAll : function(userIdToExclude) {
      return this.baseAdapter.find().then(function(replacements) {
        if (userIdToExclude) {
          return replacements.filter(function(replacement) {
            __adjustEndDate(replacement);
            return replacement.incumbent.id !== userIdToExclude && replacement.substitute.id !== userIdToExclude
          });
        }
      })
    },

    /**
     * Saves the given replacement.
     * @param replacement data representing a replacement to save.
     * @returns {*}
     */
    saveReplacement : function(replacement) {
      var data = this.getTools().extractReplacementEntityData(replacement);
      data.startDate = sp.moment.formatAsLocalDate(data.startDate);
      data.endDate = sp.moment.formatAsLocalDate(data.endDate);
      __adjustEndDate(data, true);
      return !data.uri ? this.baseAdapter.post(data) : this.baseAdapter.put(data.uri, data);
    },

    /**
     * Deletes the given replacement.
     * @param replacement data representing a replacement to delete.
     * @returns {*}
     */
    deleteReplacement : function(replacement) {
      return this.baseAdapter.remove(replacement.uri);
    }
  });
})();
