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

/* The angularjs application with its dependencies */
var suggestionBox = angular.module('silverpeas.suggestionBox',
    ['silverpeas.services', 'silverpeas.directives']);

/* the main controller of the application */
suggestionBox.controller('mainController',
    ['context', 'SuggestionBox', '$scope', function(context, SuggestionBox, $scope) {
      var suggestionBox = SuggestionBox.get({
        id : context.suggestionBoxId,
        componentInstanceId : context.component});
      $scope.suggestionBox = suggestionBox;
      if (context.suggestionId) {
        suggestionBox.suggestions.get([context.suggestionId]).then(function(suggestion){
          $scope.suggestion = suggestion;
        });
      }
    }]);

/* the not published controller of the application */
suggestionBox.controller('notPublishedController',
    ['context', '$scope', '$rootScope', function(context, $scope, $rootScope) {
      var suggestionBox = $scope.suggestionBox;

      $scope.publish = function(suggestion) {
        suggestionBox.suggestions.publish(suggestion).then(function(suggestionUpdated) {
          $rootScope.$broadcast('suggestionModified', suggestionUpdated)
        });
      };

      $scope.loadNotPublished = function() {
        suggestionBox.suggestions.get(['notPublished']).then(function(theSuggestions) {
          $scope.notPublishedSuggestions = theSuggestions;
        });
      };

      $scope.loadNotPublished();

      $scope.$on("suggestionModified", function(theSuggestionId) {
        $scope.loadNotPublished();
      });
    }]);

/* the pending validation controller of the application */
suggestionBox.controller('pendingValidationController',
    ['context', '$scope', '$rootScope', function(context, $scope, $rootScope) {
      var suggestionBox = $scope.suggestionBox;

      $scope.loadPendingValidation = function() {
        suggestionBox.suggestions.get(['pendingValidation']).then(function(theSuggestions) {
          $scope.pendingValidationSuggestions = theSuggestions;
        });
      };

      $scope.loadPendingValidation();

      $scope.$on("suggestionModified", function(theSuggestionId) {
        $scope.loadPendingValidation();
      });
    }]);

/* the published controller of the application */
suggestionBox.controller('publishedController',
    ['context', '$scope', '$rootScope', function(context, $scope, $rootScope) {
      var suggestionBox = $scope.suggestionBox;

      $scope.loadPublished = function() {
        suggestionBox.suggestions.get(['published']).then(function(theSuggestions) {
          $scope.publishedSuggestions = theSuggestions;
        });
      };

      $scope.loadPublished();

      $scope.$on("suggestionModified", function(theSuggestionId) {
        $scope.loadPublished();
      });
    }]);
