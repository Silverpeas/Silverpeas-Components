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
      if (typeof context.suggestionId !== 'undefined') {
        suggestionBox.suggestions.get(context.suggestionId).then(function(suggestion){
          $scope.suggestion = suggestion;
        });
      }

      $scope.goAt = function(url) {
        window.location = url;
      };
    }]);

/* the controller in charge of the user's suggestions in draft */
suggestionBox.controller('suggestionsInDraftController',
    ['context', '$scope', '$rootScope', function(context, $scope, $rootScope) {
      var suggestionBox = $scope.suggestionBox;

      $scope.publish = function(suggestion) {
        suggestionBox.suggestions.publish(suggestion).then(function(suggestionUpdated) {
          $rootScope.$broadcast('suggestionModified', suggestionUpdated);
        });
      };

      $scope.loadInDraft = function() {
        suggestionBox.suggestions.get('inDraft').then(function(theSuggestions) {
          $scope.inDraftSuggestions = theSuggestions;
        });
      };

      $scope.loadInDraft();

      $scope.$on("suggestionModified", function(theSuggestionId) {
        $scope.loadInDraft();
      });
    }]);

/* the controller in charge of the user's published suggestions */
suggestionBox.controller('myPublishedSuggestionsController',
    ['context', '$scope', '$rootScope', function(context, $scope, $rootScope) {
      var suggestionBox = $scope.suggestionBox;

      $scope.loadMyPublished = function() {
        suggestionBox.suggestions.get('published', {author: context.currentUserId}).then(function(theSuggestions) {
          $scope.myPublishedSuggestions = theSuggestions;
        });
      };

      $scope.loadMyPublished();

      $scope.$on("suggestionModified", function(theSuggestionId) {
        $scope.loadMyPublished();
      });
    }]);

/* the controller in charge of the published suggestions of all users */
suggestionBox.controller('publishedSuggestionsController',
    ['context', '$scope', '$rootScope', function(context, $scope, $rootScope) {
      var suggestionBox = $scope.suggestionBox;

      $scope.loadPublished = function() {
        suggestionBox.suggestions.get('published').then(function(theSuggestions) {
          $scope.publishedSuggestions = theSuggestions;
        });
      };

      $scope.loadPublished();

      $scope.$on("suggestionModified", function(theSuggestionId) {
        $scope.loadPublished();
      });
    }]);

  /* the controller in charge of all the published suggestions making the buzz */
  suggestionBox.controller('buzzPublishedSuggestionsController',
    ['context', '$scope', function(context, $scope) {
      var suggestionBox = $scope.suggestionBox;

      $scope.loadBuzzPublished = function() {
        suggestionBox.suggestions.get('published', {page: {number: 1, size: 3}, sortby: 'commentCount'}).then(function(theSuggestions) {
          $scope.buzzPublishedSuggestions = theSuggestions;
        });
      };

      $scope.loadBuzzPublished();

      $scope.$on("suggestionModified", function(theSuggestionId) {
        $scope.loadBuzzPublished();
      });
    }]);

  /* the controller in charge of the last comments on the suggestions */
  suggestionBox.controller('lastCommentsController',
    ['context', '$scope', function(context, $scope) {
      var suggestionBox = $scope.suggestionBox;
      suggestionBox.suggestions.lastComments(3).then(function(theComments) {
        $scope.lastComments = theComments;
      });
    }]);

