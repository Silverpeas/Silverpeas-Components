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
  const Replacement = function() {
    this.type = 'Replacement';
  };

  const __adjustEndDate = function(replacement, forSend) {
    const offset = typeof forSend === 'boolean' && forSend ? 1 : -1;
    const newEndMoment = sp.moment.make(replacement.endDate).add(offset, 'days');
    replacement.endDate = sp.moment.formatAsLocalDate(newEndMoment);
  };

  window.ReplacementService = SilverpeasClass.extend({
    initialize : function(context) {
      this.context = context;
      const baseUri = webContext + '/services/workflow/' + this.context.componentInstanceId + '/replacements';
      this.baseAdapter = RESTAdapter.get(baseUri, Replacement);
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
          const startDate = sp.moment.atZoneIdSimilarLocal(moment(), this.context.currentUser.zoneId);
          const newReplacement = new Replacement();
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
     * @param userIdToExclude (Optional) identifier of a user which has to be excluded from
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
      const data = this.getTools().extractReplacementEntityData(replacement);
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
