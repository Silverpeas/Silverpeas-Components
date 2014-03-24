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
      // the converter of Suggestion objects from a json stream */
      var converter = function(data) {
          var suggestions;
          if (data instanceof Array) {
            suggestions = [];
            for (var i = 0; i < data.length; i++) {
              suggestions.push(new Suggestion(data[i]));
            }
          } else {
            suggestions = new Suggestion(data);
          }
          return suggestions;
      };

      // the type SuggestionBox
      var Suggestion = function() {
        if (arguments.length > 0) {
          for (var prop in arguments[0]) {
            this[prop] = arguments[0][prop];
          }
        }
      };

      /**
       * Gets the suggestion collector for the suggestion box identified by the specified URI.
       * @param {string} suggestionBoxUri the URI identifying uniquely the suggestion box.
       * @returns {Object} a collector of suggestions from which queries can be performed.
       */
      this.suggestions = function(suggestionBoxUri) {
        return new function() {
          var adapter = RESTAdapter.get(suggestionBoxUri + '/suggestions', converter);

          /**
          * Gets one or more suggestions according to the argument.
          * @param {Array} criteria an array of key-value pairs that made the different criterion.
          * @returns {Array} the asked suggestion boxes.
          */
          this.get = function(criteria) {
            if (criteria.length === 1 && (typeof criteria[0] === 'number' || typeof criteria[0] === 'string')) {
              return adapter.find(criteria[0]);
            } else {
              return adapter.find({
               url: adapter.url,
                criteria: adapter.criteria(criteria[0], {})
              });
            }
          };
          /**
           * Removes the suggestion identified by the specified identifier from the suggestions of the
           * suggestion box.
           * @param {string} id the identifier of the suggestion to remove
           * @returns {string} the id of the deleted suggestion.
           */
          this.remove = function(id) {
            return adapter.delete(id);
          };
        };
      };
    };
}]);

services.factory('SuggestionBox', ['context', 'Suggestion', 'RESTAdapter', function(context, Suggestion, RESTAdapter) {
    return new function() {
      var baseUri = webContext + '/services/suggestionbox/' + context.component;

      // the adapter over the remote REST service used to get one or more suggestions boxes
      var adapter = RESTAdapter.get(webContext + '/services/suggestionbox/' + context.component, function(data) {
          var suggestionboxes;
          if (data instanceof Array) {
            suggestionboxes = [];
            for (var i = 0; i < data.length; i++) {
              data[i].suggestions = Suggestion.suggestions(data[i].id);
              suggestionboxes.push(new SuggestionBox(data[i]));
            }
          } else {
            data.suggestions = Suggestion.suggestions(data.id);
            suggestionboxes = new SuggestionBox(data);
          }
          return suggestionboxes;
      });

      // the type SuggestionBox
      var SuggestionBox = function() {
        if (arguments.length > 0) {
          for (var prop in arguments[0]) {
            this[prop] = arguments[0][prop];
          }
        }
        this.suggestions =  Suggestion.suggestions(baseUri + '/' + this.id);
      };

      /**
       * Gets one or more suggestion boxes according to the argument. The argument is either a
       * suggestion box identifier or a criteria the suggestion boxes has to match.
       * @returns {Array|SuggestionBox} the asked suggestion boxes.
       */
      this.get = function() {
          /* TODO uncomment the code below once the web service is available */
          /*if (arguments.length === 1 && (typeof arguments[0] === 'number' || typeof arguments[0] === 'string')) {
            return adapter.find(arguments[0]);
          } else {
            return adapter.find({
              url: adapter.url,
              criteria: adapter.criteria(arguments[0], defaultParameters)
            });
          }*/

          /* TODO remove the code below once the web service is available */
          return new SuggestionBox(arguments[0]);
        };
    };
}]);

