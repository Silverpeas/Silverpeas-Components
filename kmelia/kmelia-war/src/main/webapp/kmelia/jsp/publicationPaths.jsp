<%--

    Copyright (C) 2000 - 2024 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="p" uri="http://www.silverpeas.com/tld/viewGenerator" %>
<%@ taglib prefix="sp" uri="http://www.silverpeas.com/tld/viewGenerator" %>
<%@ page import="org.silverpeas.components.kmelia.model.KmeliaPublication" %>
<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<%@ include file="checkKmelia.jsp" %>

<%
  KmeliaPublication publication = (KmeliaPublication) request.getAttribute("Publication");
  String linkedPathString = (String) request.getAttribute("LinkedPathString");
  String currentLang = (String) request.getAttribute("Language");

  String pubName = publication.getDetail().getName(currentLang);
  String id = publication.getDetail().getPK().getId();

  boolean toolbox = componentId.startsWith("toolbox");
%>

<c:set var="toolbox" value="<%=toolbox%>"/>
<c:set var="componentId" value="<%=componentId%>"/>
<c:set var="spaceId" value="<%=spaceId%>"/>
<c:set var="linkedPathString" value="<%=linkedPathString%>"/>
<c:set var="pubName" value="<%=pubName%>"/>

<view:sp-page>
  <fmt:setLocale value="${sessionScope[sessionController].language}"/>
  <view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
  <view:sp-head-part>
    <view:includePlugin name="spaceandcomponentbrowser"/>
    <script type="text/javascript">
      $(document).ready(function() {
        displayLocations();
        <c:choose>
        <c:when test="${toolbox}">
        loadComponentLocations("<%=componentId%>");
        </c:when>
        <c:otherwise>
        loadLocalComponentLocations();
        SpVue.createApp({
          methods : {
            componentFilter : function(component) {
              return component.name === 'kmelia';
            },
            reset : function() {
              emptyComponentLocations();
            },
            loadLocations : function(component) {
              loadComponentLocations(component.instanceId);
            }
          }
        }).mount('#browserLocation');
        </c:otherwise>
        </c:choose>
      });

      function manageLocation(nodeId, componentId, checkbox) {
        let locationId = nodeId + "-" + componentId;
        let uri = webContext +
            "/services/private/publications/<%=componentId%>/<%=id%>/locations/" + locationId;
        if (checkbox.checked) {
          addAlias(uri, locationId)
        } else {
          deleteAlias(uri, locationId);
        }
      }

      function loadComponentLocations(componentId) {
        emptyComponentLocations();
        $.get("<%=routerUrl%>ShowAliasTree?ComponentId=" + componentId, function(data) {
          $("#treeviewFolders").html(data);
        });
      }

      function loadLocalComponentLocations() {
        $.get("<%=routerUrl%>ShowAliasTree?ComponentId=<%=componentId%>", function(data) {
          $("#localTreeview").html(data);
        });
      }

      function emptyComponentLocations() {
        $("#treeviewFolders").empty();
      }

      function deleteAlias(aliasURI, locationId) {
        sp.ajaxRequest(aliasURI).byDeleteMethod().send().then(function() {
          $("#currentLocations #" + locationId).remove();
          $("#localTreeview ." + locationId).prop("checked", false);
          $("#treeviewFolders ." + locationId).prop("checked", false);
        });
      }

      function addAlias(aliasURI, locationId) {
        let ajaxRequest = sp.ajaxRequest(aliasURI).byPutMethod();
        ajaxRequest.send().then(function() {
          displayLocations();
          $("#localTreeview ." + locationId).prop("checked", true);
          $("#treeviewFolders ." + locationId).prop("checked", true);
        });
      }

      function displayLocations() {
        let ajaxRequest = sp.ajaxRequest(
            webContext + "/services/private/publications/<%=componentId%>/<%=id%>/locations?lang=" +
            getUserLanguage());
        ajaxRequest.send().then(function(response) {
          let lis = "";
          response.responseAsJson().forEach(function(location) {
            let locationId = location.id + "-" + location.componentId;
            let li = "<li id=\"" + locationId + "\"><span class=\"location-path\">" +
                location.path + "</span>";
            if (location.alias) {
              li += "<span class=\"location-user\">" + location.user.fullName + "</span>";
              li += "<span class=\"location-date\">" + sp.moment.displayAsDateTime(location.date) +
                  "</span>";
              li += "<a class=\"delete-button\" href=\"javascript:void(0)\" onclick=\"deleteAlias('" + location.uri +
                  "', '" + locationId + "')\">Supprimer</a>";
            }
            li += "</li>";
            lis += li;
          });
          $("#currentLocations ul").html(lis);
        });
      }

      function topicGoTo(id) {
        location.href = "GoToTopic?Id=" + id;
      }
    </script>
  </view:sp-head-part>
  <view:sp-body-part cssClass="kmelia path locations">
    <view:browseBar spaceId="${spaceId}" componentId="${componentId}" path="${linkedPathString}" extraInformations="${pubName}"/>
    <view:window>
      <%
        KmeliaDisplayHelper.displayAllOperations(id, kmeliaScc, gef, "ViewPath", resources, out);
        if (publication.getLocation().isTrash()) {
          out.println("<div class=\"inlineMessage\">" + kmeliaScc.getString("kmelia.PubInBasket") +
              "</div>");
        }

      %>
      <view:frame>
        <view:board>
          <div id="currentLocations">
            <div class="header">
              <h3><fmt:message key="kmelia.paths.local"/></h3>
            </div>
            <ul></ul>
          </div>

          <div id="localLocations">
            <div class="header">
              <h3><fmt:message key="kmelia.paths.local.add"/></h3>
            </div>
            <div id="localTreeview" class="accordion"></div>
          </div>

          <div id="browserLocation">
            <c:set var="htmlTreeviewDock">
              <div id="treeview" class="accordion">
                <h4><fmt:message key="GML.folders"/></h4>
                <div id="treeviewFolders"></div>
              </div>
            </c:set>
            <c:choose>
              <c:when test="${toolbox}">
                <div class="space-and-component-browser">
                  <div class="browser">
                    ${htmlTreeviewDock}
                  </div>
                </div>
              </c:when>
              <c:otherwise>
                <div class="header">
                  <h3><fmt:message key="kmelia.paths.external.add"/></h3>
                </div>
                <silverpeas-space-and-component-browser v-bind:component-content-enabled="true"
                                                        v-bind:component-filter="componentFilter"
                                                        v-on:enter-root="reset"
                                                        v-on:enter-space="reset"
                                                        v-on:enter-component="loadLocations">
                  <template v-slot:extend-browser>
                    ${htmlTreeviewDock}
                  </template>
                </silverpeas-space-and-component-browser>
              </c:otherwise>
            </c:choose>
          </div>
        </view:board>
      </view:frame>
    </view:window>
  </view:sp-body-part>
</view:sp-page>