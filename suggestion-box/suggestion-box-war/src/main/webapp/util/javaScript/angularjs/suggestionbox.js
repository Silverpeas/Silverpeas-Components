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
suggestionBox.controller('mainController', ['context', 'SuggestionBox', '$scope', function(context, SuggestionBox, $scope) {
    var suggestionBox = null;
    $scope.suggestions = [{id: '09c5a97e-9c89-4593-8978-18b6e8efd84e', title: 'idée test', content: 'contenu test'}];
    /*SuggestionBox.get().then(function(theBox) {
      suggestionBox = theBox;
      $scope.suggestions = suggestionBox.suggestions.get();
    });*/
}]);


