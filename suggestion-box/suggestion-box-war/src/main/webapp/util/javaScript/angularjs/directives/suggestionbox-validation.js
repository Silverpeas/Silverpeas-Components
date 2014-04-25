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
   * suggestionbox-validation is an HTML element to render a validation popup by using the AngularJS framework.
   *
   * The following example illustrates two possible use of the directive:
   * @example <suggestionbox-validation action='refuse'></suggestionbox-validation>
   * @example <div suggestionbox-validation></div>
   * (you can replace div by any other HTML element)
   */
  angular.module('silverpeas.directives').directive('suggestionboxValidation',
      ['context', function(context) {
        return {
          templateUrl : webContext +
              '/util/javaScript/angularjs/directives/suggestionbox-validation.jsp',
          link : function postLink(scope, element, attrs) {

            function getClearedDialog(message) {
              jQuery('#suggestionValidationMessage').html(message);
              jQuery('#suggestionValidationComment').val("");
              jQuery('.validationApproveItem').hide();
              jQuery('.validationRefuseItem').hide();
              return jQuery('#suggestionValidation');
            }

            function validate(isApproving) {
              var label = isApproving ? jQuery('#validationApproveLabel').html() :
                  jQuery('#validationRefuseLabel').html();
              var result = true;
              if (!isApproving && isWhitespace(jQuery('#suggestionValidationComment').val())) {
                notyError(jQuery('#commentMandatoryErrorMessageMsg').html().replace('@name@',
                    label));
                result = false;
              }
              var comment = jQuery('#suggestionValidationComment').val();
              var commentNbChar = (comment ? (comment.split(/\n/).length + comment.length) : 0);
              if (commentNbChar > 2000) {
                notyError(jQuery('#commentNbMaxCharErrorMessageMsg').html().replace('@name@',
                    label));
                result = false;
              }
              return result;
            }

            scope.approve = function(suggestion) {
              var message = jQuery('#suggestionValidationApproveMsg').html().replace('@name@',
                  suggestion.title);
              var $confirm = getClearedDialog(message);
              jQuery('.validationApproveItem').show();
              $confirm.popup('confirmation', {
                callback : function() {
                  if (validate(true)) {
                    jQuery('#suggestionValidationForm').attr('action',
                            context.componentUriBase + 'suggestions/' + suggestion.id +
                            '/approve').submit();
                    return true;
                  }
                  return false;
                }
              });
            };

            scope.refuse = function(suggestion) {
              var message = jQuery('#suggestionValidationRefuseMsg').html().replace('@name@',
                  suggestion.title);
              var $confirm = getClearedDialog(message);
              jQuery('.validationRefuseItem').show();
              $confirm.popup('confirmation', {
                callback : function() {
                  if (validate(false)) {
                    jQuery('#suggestionValidationForm').attr('action',
                            context.componentUriBase + 'suggestions/' + suggestion.id +
                            '/refuse').submit();
                    return true;
                  }
                  return false;
                }
              });
            };
          }
        };
      }]);
})();
