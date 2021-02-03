/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

/* The angularjs application with its dependencies */
var almanachCalendar = angular.module('silverpeas.almanachcalendar',
    ['silverpeas.services', 'silverpeas.components', 'silverpeas.controllers']);

/* the main controller of the application */
almanachCalendar.controller('mainController',
    ['$controller', 'CalendarService', '$scope', function($controller, CalendarService, $scope) {
      $controller('silverpeasCalendarController', {$scope : $scope});

      $scope.getCalendarService = function() {
        return CalendarService;
      };
    }]);

/* the calendar controller of the application */
almanachCalendar.controller('calendarController',
    ['$controller', '$scope', 'context', function($controller, $scope, context) {
      $controller('mainController', {$scope : $scope});

      $scope.participationIds = $scope.participation.getParticipants() || [];
      $scope.viewMyCalendar = function() {
        if ($scope.participationIds.indexOfElement(context.currentUserId)) {
          $scope.participationIds.addElement(context.currentUserId);
        }
      };
      $scope.$watchCollection('participationIds', function(participationIds) {
        $scope.participationIds = angular.copy(participationIds);
        $scope.participation.setParticipants(participationIds);
      });
    }]);

/* the edit controller of the application */
almanachCalendar.controller('editController',
    ['$controller', '$scope', function($controller, $scope) {
      $controller('mainController', {$scope : $scope});

      $scope.loadOccurrenceFromContext(true);
    }]);

/* the view controller of the application */
almanachCalendar.controller('viewController',
    ['$controller', '$scope', function($controller, $scope) {
      $controller('mainController', {$scope : $scope});

      $scope.reloadOccurrenceFromContext();
    }]);


/* the portlet controller of the application */
almanachCalendar.controller('portletController', ['$controller', 'CalendarService', '$scope',
  function($controller, CalendarService, $scope) {
    $controller('silverpeasCalendarController', {$scope : $scope});

    CalendarService.getNextOccurrences().then(function(occurrences) {
      $scope.occurrences = occurrences;
    });
  }]);
