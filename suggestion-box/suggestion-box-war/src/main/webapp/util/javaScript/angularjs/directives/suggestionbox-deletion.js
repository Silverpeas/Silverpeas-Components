/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

(function() {

  /**
   * suggestionbox-deletion is an HTML element to render a deletion popup by using the AngularJS framework.
   *
   * The following example illustrates two possible use of the directive:
   * @example <suggestionbox-deletion action='refuse'></suggestionbox-deletion>
   * @example <div suggestionbox-deletion></div>
   * (you can replace div by any other HTML element)
   */
  angular.module('silverpeas.directives').directive('suggestionboxDeletion',
      ['context', '$rootScope', function(context, $rootScope) {
        return {
          templateUrl : webContext +
              '/util/javaScript/angularjs/directives/suggestionbox-deletion.jsp',
          link : function postLink(scope, element, attrs) {

            function getClearedDialog(message) {
              jQuery('#suggestionDeletionMessage').html(message);
              return jQuery('#suggestionDeletion');
            }

            scope.delete = function(suggestion, navigationCall) {
              var message = jQuery('#deleteSuggestionConfirmMessage').html().replace('@name@',
                  suggestion.title);
              var $confirm = getClearedDialog(message);
              $confirm.popup('confirmation', {
                callback : function() {
                  if (!navigationCall) {
                    var suggestionBox = scope.suggestionBox;
                    suggestionBox.suggestions.remove(suggestion.id).then(function() {
                      $rootScope.$broadcast('suggestionModified', suggestion.id)
                    });
                    return true;
                  } else {
                    jQuery('#suggestionDeletionForm').attr('action',
                            context.componentUriBase + 'suggestions/' + suggestion.id +
                            '/delete').submit();
                    return true;
                  }
                }
              });
            };
          }
        };
      }]);
})();
