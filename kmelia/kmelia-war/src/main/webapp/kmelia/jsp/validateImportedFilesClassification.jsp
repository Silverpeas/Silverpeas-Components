<%--

    Copyright (C) 2000 - 2011 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle basename="com.stratelia.webactiv.kmelia.multilang.kmeliaBundle"/>
<view:setBundle basename="com.stratelia.silverpeas.pdcPeas.multilang.pdcBundle" var="pdcBundle"/>
<fmt:message key="GML.validate" var="done"/>
<fmt:message key="kmelia.publiClassification" var="classification"/>
<c:set var="importedPublications" value="${requestScope['PublicationsDetails']}"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title><c:out value="${classification}"/></title>
    <link type="text/css" href="<c:url value='/util/styleSheets/fieldset.css'/>" rel="stylesheet" />
    <view:looknfeel />
    <script type="text/javascript" src="<c:url value='/util/javaScript/silverpeas-pdc-widgets.js'/>"></script>
    <script type="text/javascript">
      var classifications = []; // will store the classification of all the imported publications
      
      /**
       * Translations
       */
      var positionErrorMessage = "<fmt:message key='pdcPeas.MustContainsMandatoryAxis' bundle='${pdcBundle}'/>";
      var mandatoryAxisText    = "<fmt:message key='GML.selectAValue'/>";
      var mandatoryAxisLegend  = "<fmt:message key='GML.requiredField'/>";
      var invariantAxisLegend  = "<fmt:message key='pdcPeas.notVariants' bundle='${pdcBundle}'/>";
      var labelOk              = "<fmt:message key='GML.validate'/>";
      var labelCancel          = "<fmt:message key='GML.cancel'/>";
      var labelPosition        = "<fmt:message key='pdcPeas.position' bundle='${pdcBundle}'/>";
      var labelPositions       = "<fmt:message key='pdcPeas.positions' bundle='${pdcBundle}'/>";
      var titleUpdate          = "<fmt:message key='GML.modify'/>";
      var titleAddition        = "<fmt:message key='GML.PDCNewPosition'/>";
      var titleDeletion        = "<fmt:message key='GML.PDCDeletePosition'/>";
      
      /**
       * Loads recursively the classification on the PdC of the publications between startIndex and
       * endIndex in the array of the imported publications.
       * Once the classifications of the specified publications loaded, invoke the function
       * loadingComplete to performe addition tasks requiring the loaded classifications.
       */
      function loadPublicationsClassification(publications, startIndex, endIndex, loadingComplete) {
        loadClassification(publications[startIndex], function(classification) {
          classifications.push(classification);
          if (endIndex > startIndex) {
            loadPublicationsClassification(publications, startIndex + 1, endIndex, loadingComplete);
          } else {
            loadingComplete();
          }
        }, function(classification, error) {
          window.alert(error);
        })
      }
      
      /**
       * Adds recursively the specified position into the classifications on the PdC between
       * startIndex and endIndex in the array of the imported publication's classifications.
       */
      function addPositionInClassifications(position, classifications, startIndex, endIndex) {
        postPosition(classifications[startIndex].uri, position, function(classification) {
          classifications[startIndex] = classification;
          if (endIndex > startIndex) {
            addPositionInClassifications(position, classifications, startIndex + 1, endIndex);
          } else {
            $('#classification-modification').pdcPositions('refresh', classifications[0].positions);
          }
        });
      }
      
      /**
       * Deletes recursively the specified position in the classifications on the PdC between
       * startIndex and endIndex in the array of the imported publication's classifications.
       */
      function deletePositionInClassifications(position, classifications, startIndex, endIndex) {
        var confirmation = null; 
        if (startIndex == 0) confirmation = "<fmt:message key='pdcPeas.confirmDeletePosition' bundle='${pdcBundle}'/>";
        var positionToDelete = findPosition(position.values, classifications[startIndex].positions);
        deletePosition(classifications[startIndex].uri, positionToDelete.position, confirmation, function() {
          classifications[startIndex].positions.splice(positionToDelete.index, 1);
          if (endIndex > startIndex) {
            deletePositionInClassifications(position, classifications, startIndex + 1, endIndex);
          } else {
            $('#classification-modification').pdcPositions('refresh', classifications[0].positions);
          }
        });
      }
      
      /**
       * Updates recursively the specified position with the specified values in the classifications
       * on the PdC between startIndex and endIndex in the array of the imported publication's
       * classifications.
       */
      function updatePositionInClassifications(position, values, classifications, startIndex, endIndex) {
        var positionToUpdate = findPosition(position.values, classifications[startIndex].positions);
        updatePosition(classifications[startIndex].uri, {
          uri: positionToUpdate.position.uri,
          id: positionToUpdate.position.id,
          values: values},
        function(classification) {
          classifications[startIndex] = classification;
          if (endIndex > startIndex) {
            updatePositionInClassifications(position, values, classifications, startIndex + 1, endIndex);
          } else {
            $('#classification-modification').pdcPositions('refresh', classifications[0].positions);
          }
        });
      }
      
      /**
       * Closes this window and go back to the Kmelia main page with the topics.
       */
      function closeWindow() {
        window.opener.location.href="GoToCurrentTopic";
        window.close();
      }
      
      /**
       * Validates the predefined classification on the PdC for all of the imported publications.
       * This function has no side-effect if the classification on the PdC of one or more of the
       * imported publications were previously modified by one of the two others options; the
       * modified classifications on the PdC will be kept.
       */
      function validateDefaultClassificationForAllPublications() {
        $('#classification-modification').hide().children().remove();
      }
      
      /**
       * Modifies the default classifications on the PdC for all of the imported publications. They
       * will have the same classification on the PdC.
       */
      function modifyClassificationForAllPublications() {
        loadPdC(uriOfPdC({
          context: '<c:out value="${requestScope['Context']}"/>',
          content: '<c:out value="${importedPublications[0].id}"/>',
          component: '<c:out value="${importedPublications[0].componentInstanceId}"/>'
        }), function(pdc) {
          $('<legend>').addClass('header').html("<fmt:message key='kmelia.classificationModificationForAllPublications'/>").
            appendTo($("#classification-modification").show());
          $('#classification-modification').pdcPositions({
            title     : labelPositions,
            label     : labelPosition,
            update    : { title: titleUpdate },
            addition  : { title: titleAddition },
            deletion  : { title: titleDeletion },
            positions : classifications[0].positions,
            onAddition: function($this) {
              $('#pdc-edition-box').pdcAxisValuesSelector({
                title              : titleAddition,
                positionError      : positionErrorMessage,
                mandatoryAxisText  : mandatoryAxisText,
                mandatoryAxisLegend: mandatoryAxisLegend,
                invariantAxisLegend: invariantAxisLegend,
                labelOk            : labelOk,
                labelCancel        : labelCancel,
                axis               : pdc.axis,
                onValuesSelected   : function($this, selectedValues) {
                  var position = { values: selectedValues };
                  if (isAlreadyInClassification(position, classifications[0]))
                    alert("<fmt:message key='pdcPeas.positionAlreadyExist' bundle='${pdcBundle}'/>");
                  else
                    addPositionInClassifications(position, classifications, 0, classifications.length - 1);
                }
              });
            },
            onDeletion: function($this, position) {
              deletePositionInClassifications(position, classifications, 0, classifications.length - 1);
            },
            onUpdate  : function($this, position) {
              $('#pdc-edition-box').pdcAxisValuesSelector({
                title              : titleUpdate,
                positionError      : positionErrorMessage,
                mandatoryAxisText  : mandatoryAxisText,
                mandatoryAxisLegend: mandatoryAxisLegend,
                invariantAxisLegend: invariantAxisLegend,
                labelOk            : labelOk,
                labelCancel        : labelCancel,
                axis               : pdc.axis,
                values             : position.values,
                onValuesSelected   : function($this, selectedValues) {
                  if (isAlreadyInClassification({ values: selectedValues }, classifications[0]))
                    alert("<fmt:message key='pdcPeas.positionAlreadyExist' bundle='${pdcBundle}'/>");
                  else
                    updatePositionInClassifications(position, selectedValues, classifications, 0, classifications.length - 1);
                }
              });
            }
          });    
        }, function(pdc, error) {
          alert(error);
        })
      }
      
      /**
       * Modifies the default classifications on the PdC for each of the imported publications.
       * The user then can independently choose different positions on the PdC for each publication.
       */
      function modifyClassificationOfEachPublications() {
        $('#classification-modification').hide().children().remove();
        alert("modify each");
      }
      
      /**
       * Once this HTML document ready, enrichs it with a set of radio buttons through which the user
       * can choose to validate or to modify the classification on the PdC of the imported
       * publications.
       * As the publications have been automatically classified through the import process,
       * their classification on the PdC is loaded.
       */
      $(document).ready(function() {
        var okForAll = 0, modifyForAll = 1, modifyForEach = 2, publicationsURI = [];
        
      <c:forEach items="${importedPublications}" var="publication">
        publicationsURI.push(uriOfPdCClassification({
          context: '<c:out value="${requestScope['Context']}"/>',
          content: '<c:out value="${publication.id}"/>',
          component: '<c:out value="${publication.componentInstanceId}"/>'
        }));
      </c:forEach>
      
        loadPublicationsClassification(publicationsURI, 0, publicationsURI.length - 1, function() {
          $('<legend>').html("<fmt:message key='kmelia.classifyYourPublications'/>").appendTo($('#default-classification'));
          $('#default-classification').pdcPositions({
            id        : 'default-list_pdc_position',
            title     : labelPositions,
            label     : labelPosition,
            update    : { activated: false },
            addition  : { activated: false },
            deletion  : { activated: false },
            positions : classifications[0].positions
          });
          
          $('<div>', {
            id: 'validation'
          }).addClass('field').append($('<input>', {
            type: 'radio', 
            name: 'validation', 
            value: okForAll, 
            checked: true
          })).
            append($('<span>').html("<fmt:message key='kmelia.validateClassificationForAllPublications'/>")).
            append($('<br>')).
            append($('<input>', {
            type: 'radio', 
            name: 'validation', 
            value: modifyForAll,
            checked: false
          })).
            append($('<span>').html("<fmt:message key='kmelia.modifyClassificationForAllPublications'/>")).
            append($('<br>')).
            append($('<input>', {
            type: 'radio', 
            name: 'validation', 
            value: modifyForEach,
            checked: false
          })).
            append($('<span>').html("<fmt:message key='kmelia.modifyClassificationForEachPublication'/>")).
            appendTo($('#default-classification'));
        
          $('#validation.field input:radio').change(function() {
            var userChoice = $('.field input:radio:checked').val();
            if (userChoice == okForAll) validateDefaultClassificationForAllPublications();
            else if (userChoice == modifyForAll) modifyClassificationForAllPublications();
            else modifyClassificationOfEachPublications();
          });
        });
      });
    </script>
  </head>
  <body>
    <view:browseBar clickable="false" path="${classification}"/>
    <view:window browseBarVisible="true">
      <view:frame>
        <div class="inlineMessage">
          <fmt:message key="kmelia.importedPublicationCount">
            <fmt:param value="${fn:length(importedPublications)}"/>
          </fmt:message>
        </div>
        <br clear="all"/>
        <div id="header">

          <fieldset id="default-classification" class="classification skinFieldset">
          </fieldset>

          <div id="pdc-edition-box" class="pdc-edition-box fields" style="display: none;">
          </div>

          <fieldset id="classification-modification" class="classification skinFieldset" style="display: none;">
          </fieldset>
        </div>

        <center>
          <view:buttonPane>
            <view:button label="${done}" action="javascript: closeWindow();"/>
          </view:buttonPane>
        </center>
      </view:frame>
    </view:window>
  </body>
</html>
