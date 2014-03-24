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
suggestionBox.controller('mainController', ['context', 'SuggestionBox', '$scope', '$rootScope',
  function(context, SuggestionBox, $scope, $rootScope) {
    var suggestionBox = SuggestionBox.get({
      id : context.suggestionBoxId,
      componentInstanceId : context.component});

    $scope.delete = function(suggestion) {
      jQuery('#confirmation').html(context.deleteSuggestionConfirmMessage.replace('@name@',
          suggestion.title));
      jQuery('#confirmation').popup('confirmation', {
        callback : function() {
          suggestionBox.suggestions.remove(suggestion.id).then(function() {
            $rootScope.$broadcast('suggestionModified', suggestion.id)
          });
          return true;
        }
      });
    };

    $scope.suggestions = [
      {id : 'b28c4d6d-ed51-4dad-bfd9-5a330d574575', title : 'idée test', content : 'contenu test'}
    ];
    /*suggestionBox.suggestions().get().then(function(theSuggestions) {
     $scope.suggestions = theSuggestions;
     });*/

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


