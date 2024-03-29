<%--
  Copyright (C) 2000 - 2024 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have received a copy of the text describing
  the FLOSS exception, and it is also available here:
  "https://www.silverpeas.org/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program. If not, see <https://www.gnu.org/licenses/>.
  --%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ page import="org.silverpeas.core.util.WebEncodeHelper"%>
<%@ include file="check.jsp" %>

<fmt:setLocale value="${requestScope.resources.language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<%
Reservation reservation = (Reservation) request.getAttribute("reservation");
Long modifiedReservationId = (Long) request.getAttribute("idReservation");

String evenement = reservation.getEvent();
String raison = WebEncodeHelper.javaStringToHtmlParagraphe(reservation.getReason());
String lieu = reservation.getPlace();

// boutons de validation du formulaire
ButtonPane buttonPane = gef.getButtonPane();
Button validateButton = gef.getFormButton(resource.getString("GML.validate"), "javaScript:verification()", false);
Button cancelButton = gef.getFormButton(resource.getString("GML.cancel"), "Calendar?objectView=" + request.getAttribute("objectView"),false);
buttonPane.addButton(validateButton);
buttonPane.addButton(cancelButton);
%>

<c:set var="reservation" value="<%=reservation%>"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">
<html>
  <head>
    <view:looknfeel />
    <view:includePlugin name="qtip"/>
    <view:includePlugin name="preview"/>
    <script type="text/javascript">

      function ajouterRessource(resourceId, categoryId) {
        var elementResource = document.getElementById(resourceId);
        var elementlisteReservation = document.getElementById("listeReservation");
        var theImage = "image"+resourceId ;
        document.images[theImage].src = "<c:url value="/util/icons/delete.gif" />";
        elementlisteReservation.appendChild(elementResource);
      }

      function enleverRessource(resourceId, categoryId) {
        var elementResource = document.getElementById(resourceId);
        var elementCategory = document.getElementById(categoryId);
        var theImage = "image"+resourceId ;
        document.images[theImage].src = "<c:url value="/util/icons/ok.gif" />";

        elementCategory.appendChild(elementResource);
      }

      function switchResource(resourceId, categoryId) {
        if (isResourceReservee(resourceId)) {
          clearCategory(categoryId);
          enleverRessource(resourceId, categoryId);
        }
        else {
          ajouterRessource(resourceId, categoryId);
          if (isCategoryEmpty(categoryId)) {
            addEmptyResource(categoryId);
          }
        }
      }

      function addEmptyResource(categoryId)
      {
        var emptyElement = document.createElement("div");
        emptyElement.id = "-1";
        emptyElement.innerHTML = "<span class=\"noRessource\"><center><fmt:message key="resourcesManager.noResource" /></center></span>";
        var elementCategory = document.getElementById(categoryId);
        elementCategory.appendChild(emptyElement);
      }

      function clearCategory(categoryId)
      {
        var category = document.getElementById(categoryId);
        var resources = category.childNodes;
        for (var r=0; r<resources.length; r++) {
          if (resources[r].nodeName == 'DIV' && resources[r].id == "-1") {
            category.removeChild(resources[r]);
          }
        }
      }

      function isCategoryEmpty(categoryId) {
        var category = document.getElementById(categoryId);
        var resources = category.childNodes;
        for (var r=0; r<resources.length; r++) {
          if (resources[r].nodeName == 'DIV') {
            return false;
          }
        }
        return true;
      }

      function isResourceReservee(resourceId)
      {
        var listeReservation = document.getElementById("listeReservation");
        var resources = listeReservation.childNodes;
        for (var r=0; r<resources.length; r++) {
          if (resources[r].nodeName == 'DIV' && resources[r].id == resourceId) {
            return true;
          }
        }
        return false;
      }

      function getResourcesReservees() {
        var listeReservation = document.getElementById("listeReservation");
        var resources = listeReservation.childNodes;
        var resourceIds = "";
        for (var r=0; r<resources.length; r++) {
          if (resources[r].nodeName == 'DIV') {
            resourceIds += resources[r].id + ",";
          }
        }
        resourceIds = resourceIds.substring(0, resourceIds.length-1);
        return resourceIds;
      }

      function verification(){
        document.frmResa.listeResa.value = getResourcesReservees();
        if(getResourcesReservees() == "") {
          $( "#dialog-message" ).dialog( "open" );
        } else {
          document.frmResa.submit();
        }
      }

      function retour() {
        window.history.back();
      }

      $(function() {
        $('#accordion').accordion();
        $("#dialog-message" ).dialog({
          modal: true,
          autoOpen: false,
          width: 350,
          resizable: false,
          buttons: {
            Ok: function() {
              $( this ).dialog( "close" );
            }
          }
        });
      });

      whenSilverpeasReady(function() {
        [].slice.call(document.querySelectorAll(".resourceData"), 0).forEach(function(resource) {
          var resourceId = resource.getAttribute("resource-id");
          var resourceLabel = resource.getAttribute("resource-label");
          var description = resource.getAttribute("description-data");
          var formId = resource.getAttribute("form-id");
          new Promise(function(resolve) {
            if (formId.isDefined()) {
              var url = "<c:url value="/services/contribution/${reservation.instanceId}/"/>" +
                  resourceId + '/content/form/' + formId + '?renderView=true';
              silverpeasAjax(url).then(function(request) {
                var formData = request.responseAsJson();
                resolve({
                  "label" : resourceLabel,
                  "text" : description,
                  "form" : formData.renderedView
                });
              });
            } else if (description.isDefined()) {
              resolve({
                "label" : resourceLabel, "text" : description, "form" : ""
              });
            }
          }).then(function(description) {
            var options = {};
            if (description.form) {
              options.hide = {
                event : 'click unfocus'
              };
              options.common = {
                style : {
                  zindex : 0
                }
              };
              options.content = {
                "title" : description.label
              };
              options.position = {
                adjust : {
                  method : "shift flipinvert"
                }
              }
            }

            var infoPanel;
            if (!description.form) {
              infoPanel = description.text;
            } else {
              infoPanel = document.createElement('div');
              infoPanel.classList.add('resource-info-panel');
              if (description.text) {
                var descriptionPanel = document.createElement('div');
                descriptionPanel.classList.add('resource-info-description');
                descriptionPanel.innerHTML = description.text;
                infoPanel.appendChild(descriptionPanel);
              }
              var extraInfoPanel = document.createElement('div');
              extraInfoPanel.classList.add('resource-info-extra');
              extraInfoPanel.innerHTML = description.form;
              infoPanel.appendChild(extraInfoPanel);
            }

            TipManager.simpleInfo(resource, function() {
              return infoPanel;
            }, options);
          });
        });
      });

    </script>
  </head>
  <body class="qtip-no-max-width">
    <%
    browseBar.setPath("<a href=\"javascript:retour()\">"+resource.getString("resourcesManager.reservationParametre")+"</a>");
    browseBar.setExtraInformation(resource.getString("resourcesManager.resourceSelection"));

        out.println(window.printBefore());
        out.println(tabbedPane.print());
        out.println(frame.printBefore());
    %>
    <c:if test="${not empty requestScope.unavailableReservationResources}">
      <div class="inlineMessage-nok" style="text-align: left">
        <h4><fmt:message key="resourcesManager.resourceUnReservable"/></h4>
        <c:forEach items="${requestScope.unavailableReservationResources}" var="unavailableResource">
          <span class="resourceData"
                resource-id="${unavailableResource.id}"
                resource-label="<c:out escapeXml="false" value="${unavailableResource.name}"/>"
                form-id="${unavailableResource.category.form}"
                description-data="<c:out escapeXml="false" value="${silfn:escapeHtmlWhitespaces(unavailableResource.description)}"/>">
            <fmt:message key="resourcesManager.ressourceNom"/> : ${unavailableResource.name}</span><br/>
        </c:forEach>
      </div>
      <br clear="all"/>
    </c:if>
    <view:board>

      <table align="center" cellpadding="3" cellspacing="0" border="0" width="100%">
        <tr>
          <td class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.evenement"));%> :</td>
          <td width="100%"><%=evenement%></td>
        </tr>

        <tr>
          <td class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("GML.dateBegin"));%> :</td>
          <td><%=resource.getOutputDateAndHour(reservation.getBeginDate())%></td>
        </tr>

        <tr>
          <td class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("GML.dateEnd"));%> :</td>
          <td><%=resource.getOutputDateAndHour(reservation.getEndDate())%></td>
        </tr>

        <tr>
          <td class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.raisonReservation"));%> :</td>
          <td><%=raison%></td>
        </tr>

        <tr>
          <td class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.lieuReservation"));%> :</td>
          <td><%=lieu%></td>
        </tr>
      </table></view:board>
      <br />

      <table width="100%" align="center" border="0" cellspacing="5">
        <tr>
          <td width="50%" valign="top">
            <div class="titrePanier"><center><fmt:message key="resourcesManager.clickReservation" /></center></div>
            <div id="accordion">
              <c:forEach items="${requestScope.categories}" var="category">
                <h3><a href="#"><c:out value ="${category.name}" /></a></h3>
              <div id="categ<c:out value ="${category.id}" />">
                <c:choose>
                  <c:when test="${not empty requestScope.mapResourcesReservable[category.idAsLong]}">
                    <c:forEach items="${requestScope.mapResourcesReservable[category.idAsLong]}" var="maResource">
                      <div id="<c:out value ="${maResource.id}" />" onClick="switchResource(<c:out value ="${maResource.id}" />,'categ<c:out value ="${category.id}" />');" style="cursor: pointer;">
                      <table width="100%" cellspacing="0" cellpadding="0" border="0">
                        <tr>
                          <td width="80%" nowrap><span class="resourceData"
                                                       resource-id="${maResource.id}"
                                                       resource-label="<c:out escapeXml="false" value="${maResource.name}"/>"
                                                       form-id="${maResource.category.form}"
                                                       description-data="<c:out escapeXml="false" value="${silfn:escapeHtmlWhitespaces(maResource.description)}"/>"> - ${maResource.name}</span></td>
                          <td><img src="<c:url value="/util/icons/ok.gif" />" id="image<c:out value ="${maResource.id}" />" align="middle"/></td>
                        </tr>
                      </table>
                    </div>
                    </c:forEach>
                  </c:when>
                  <c:otherwise>
                    <div id="-1" class="noRessource">
                      <fmt:message key="resourcesManager.noResource" />
                    </div>
                  </c:otherwise>
                </c:choose>
              </div>
              </c:forEach>
            </div>
        </td>
        <td valign="top" width="50%">
          <div class="titrePanier"><% out.println(resource.getString("resourcesManager.resourcesReserved"));%></div>
          <div id="listeReservation">
            <c:if test="${not empty requestScope.listResourceEverReserved}">
              <c:forEach items="${requestScope.listResourceEverReserved}" var="resource">
                <div id="${resource.id}" onClick="switchResource(${resource.id},'categ${resource.categoryId}');" style="cursor: pointer;">
                  <table width="100%" cellspacing="0" cellpadding="0" border="0">
                    <tr>
                      <td width="80%" nowrap><span class="resourceData"
                                                   resource-id="${resource.id}"
                                                   resource-label="<c:out escapeXml="false" value="${resource.name}"/>"
                                                   form-id="${resource.category.form}"
                                                   description-data="<c:out escapeXml="false" value="${silfn:escapeHtmlWhitespaces(resource.description)}"/>"> - ${resource.name}</span></td>
                      <td>
                        <img src="<c:url value="/util/icons/delete.gif" />" id="image${resource.id}" align="middle" alt=""/>
                      </td>
                    </tr>
                  </table>
                </div>
              </c:forEach>
            </c:if>
          </div>
        </td></tr></table>
        <%
        out.println("<br/><center>"+buttonPane.print()+"</center><br/>");
        out.println(frame.printAfter());
        out.println(window.printAfter());
        %>
    <form name="frmResa" method="post" action="FinalReservation">
      <input type="hidden" name="listeResa" value=""/>
      <input type="hidden" name="newResourceReservation" value=""/>
      <%if(modifiedReservationId != null){ %>
      <input type="hidden" name="modifiedReservationId" value="<%=modifiedReservationId%>"/>
      <%}%>
    </form>
    <div id="dialog-message" title="<fmt:message key="resourcesManager.form.validation.error.title" />">
      <fmt:message key="resourcesManager.noReservedResource" />
    </div>
  </body>
</html>