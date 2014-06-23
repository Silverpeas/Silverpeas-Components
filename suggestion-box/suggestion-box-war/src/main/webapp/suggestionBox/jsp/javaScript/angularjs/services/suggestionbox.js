/*
 * Copyright (C) 2000-2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
var services = angular.module('silverpeas.services');

services.factory('Suggestion', ['RESTAdapter', function(RESTAdapter) {
  return new function() {

    // the type Suggestion
    var Suggestion = function() {
      this.type = 'Suggestion';
    };

    // the type SuggestionComment
    var SuggestionComment = function() {
      this.type = 'SuggestionComment';
    };

    /**
     * Gets the suggestion collector for the suggestion box identified by the specified URI.
     * @param {string} suggestionBoxUri the URI identifying uniquely the suggestion box.
     * @returns {Object} a collector of suggestions from which queries can be performed.
     */
    this.suggestions = function(suggestionBoxUri) {
      return new function() {
        var adapter = RESTAdapter.get(suggestionBoxUri + '/suggestions', Suggestion);
        var adapterForComments = RESTAdapter.get(suggestionBoxUri + '/suggestions/lastComments',
            SuggestionComment);

        /**
         * Gets one or more suggestions according to the arguments.
         *
         * If the argument is just a number or a string, then it is considered as a resource
         * identifier. For example, this can be an identifier of a suggestions or an identifier of
         * all of the published resources. The reserved identifiers are:
         * - published for the published suggestions,
         * - pendingValidation for the suggestions in pending validation,
         * - inDraft for the suggestions in draft (in redaction),
         * - outOfDraft for the suggestions that were validated (published and refused)
         *
         * If the argument is an object or it is made up of an identifier following by an object,
         * hen the object is taken as a criteria to apply to requested resource.
         * @returns {Array} the asked suggestion boxes.
         */
        this.get = function() {
          if (arguments.length === 1 &&
              (typeof arguments[0] === 'number' || typeof arguments[0] === 'string')) {
            return adapter.find(arguments[0]);
          } else {
            var url = adapter.url;
            var criteria = arguments[0];
            if (arguments.length > 1) {
              url += '/' + arguments[0];
              criteria = arguments[1];
            }
            return adapter.find({
              url: url,
              criteria: adapter.criteria(criteria)
            });
          }
        };
        /**
         * Removes the suggestion identified by the specified identifier from the suggestions in the
         * suggestion box.
         * @param {string} id the identifier of the suggestion to remove
         * @returns {string} the id of the deleted suggestion.
         */
        this.remove = function(id) {
          return adapter.remove(id);
        };
        /**
         * Publishes the suggestion identified by the specified identifier from the suggestions
         * in the suggestion box.
         * @param {string} suggestion the suggestion to publish
         * @returns {Suggestion} the updated suggestion.
         */
        this.publish = function(suggestion) {
          return adapter.update(suggestion.id + '/publish', suggestion);
        };
        /**
         * Gets the last comments that were posted on some of the suggestions in the suggestion
         * box.
         * @param {integer} [count] the number of comments to get.
         * @returns {Comment} the last comments on the suggestions.
         */
        this.lastComments = function(count) {
          if (typeof count === 'number')
            return adapterForComments.find(adapterForComments.criteria({count: count}));
          else
            return adapterForComments.find();
        };
      };
    };
  };
}]);

services.factory('SuggestionBox',
    ['context', 'Suggestion', 'RESTAdapter', function(context, Suggestion, RESTAdapter) {
      return new function() {
        var baseUri = webContext + '/services/suggestionbox/' + context.component;

        // the type SuggestionBox
        var SuggestionBox = function(properties) {
          for(var prop in properties) {
            this[prop] = properties[prop];
          }
          this.suggestions = Suggestion.suggestions(baseUri + '/' + this.id);
        };

        /**
         * Gets the suggestion box matching the specified identifiers. These should define
         * at least the unique suggestion box identifier (property named 'id') or the identifier of
         * the component instance (property named 'componentInstanceId') it belongs.
         * @returns {SuggestionBox} the asked suggestion box.
         */
        this.get = function(identifiers) {
          if (typeof identifiers === 'object' &&
              (identifiers.id || identifiers.componentInstanceId)) {
            var criteria = {};
            if (identifiers.id) {
              criteria.id = identifiers.id;
              criteria.componentInstanceId = identifiers.id;
            }
            if (identifiers.componentInstanceId) {
              criteria.componentInstanceId = identifiers.componentInstanceId;
              if (identifiers.id === undefined)
                criteria.id = identifiers.componentInstanceId;
            }
            return new SuggestionBox(criteria);
          } else
            notyError("Error: missing suggestion box id or componentInstanceId property");
        };
      };
    }]);

