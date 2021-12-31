<%--

    Copyright (C) 2000 - 2021 Silverpeas

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
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.silverpeas.components.kmelia.model.KmeliaPublication" %>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkKmelia.jsp" %>

<%
KmeliaPublication publication 		= (KmeliaPublication) request.getAttribute("Publication");
String 				linkedPathString 	= (String) request.getAttribute("LinkedPathString");
String				currentLang 		= (String) request.getAttribute("Language");

String pubName 	= publication.getDetail().getName(currentLang);
String id 		= publication.getDetail().getPK().getId();

boolean toolbox = componentId.startsWith("toolbox");
%>

<c:set var="toolbox" value="<%=toolbox%>"/>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xml:lang="<%=currentLang%>">
<head>
<title></title>
<view:looknfeel/>
<script type="text/javascript">
	$(document).ready(function(){
    displayLocations();
    <c:choose>
      <c:when test="${toolbox}">
        viewComponent("<%=componentId%>");
      </c:when>
      <c:otherwise>
        viewLocalComponent();
        $("#browsingArea").show();
        displaySpaces();
      </c:otherwise>
    </c:choose>
	});

  function manageLocation(nodeId, componentId, checkbox) {
    let locationId = nodeId+"-"+componentId;
    let uri = webContext+"/services/private/publications/<%=componentId%>/<%=id%>/locations/"+locationId;
    if (checkbox.checked) {
      addAlias(uri, locationId)
    } else {
      deleteAlias(uri, locationId);
    }
  }

	function displaySpaces() {
    viewSpace('0');
  }

  const currentPath = new Map();
	currentPath.set('0', '&nbsp;');

	function processPath(spaceId) {
	  if (spaceId) {
      // check if spaceId is not already in path
      if (currentPath.has(spaceId)) {
        let deleteNext = false;
        for (const key of currentPath.keys()) {
          if (deleteNext) {
            currentPath.delete(key);
          } else {
            if (key === spaceId) {
              deleteNext = true;
            }
          }
        }
      } else {
        let spaceLabel = $("#space-" + spaceId).text();
        currentPath.set(spaceId, spaceLabel);
      }

      let path = "";
      for (const key of currentPath.keys()) {
        if (key !== '0') {
          path += " > ";
        }
        let a = "<a href=\"#\" onclick=\"viewSpace('" + key + "'); return false;\">" + currentPath.get(key) + "</a>";
        path += a;
      }
      $("#currentPath").html(path);
    }
  }

  function viewSpace(spaceId) {
    let uri = webContext+"/services/spaces";
    if (spaceId !== '0') {
      uri += "/"+spaceId+"/spaces";
    }

    processPath(spaceId);
    emptyComponentTreeview();
    $("#components ul").empty();

    $("#spaces ul").empty();
    let ajaxRequest = sp.ajaxRequest(uri);
    ajaxRequest.send().then(function(response) {
      response.responseAsJson().forEach(function(space) {
        let li = "<li id=\"space-"+space.id+"\"><a href=\"#\" onclick=\"viewSpace('"+space.id+"'); return false;\">"+space.label+"</a></li>";
        $("#spaces ul").append(li);
      });
    });
    if (spaceId !== '0') {
      displayComponents(spaceId);
    }
  }

  function displayComponents(spaceId) {
    let uri = webContext+"/services/spaces/"+spaceId+"/components";
    $("#components ul").empty();
    let ajaxRequest = sp.ajaxRequest(uri);
    ajaxRequest.send().then(function(response) {
      response.responseAsJson().forEach(function(component) {
        if (component.name === "kmelia") {
          let componentId = component.name+component.id;
          let li = "<li id=\""+componentId+"\"><a href=\"#\" onclick=\"viewComponent('"+componentId+"', true); return false;\">"+component.label+"</a></li>";
          $("#components ul").append(li);
        }
      });
    });
  }

  function viewComponent(componentId, addToPath) {
	  if (addToPath) {
	    let componentLabel = $("#"+componentId).text();
      if (!$('#currentPath').text().includes(componentLabel)) {
        $("#currentPath").append(" > ").append(componentLabel);
      }
    }
	  emptyComponentTreeview();
    $.post("<%=routerUrl%>ShowAliasTree?ComponentId="+componentId, function(data){
      $("#treeviewFolders").html(data);
    });
  }

  function viewLocalComponent() {
    $.post("<%=routerUrl%>ShowAliasTree?ComponentId=<%=componentId%>", function(data){
      $("#localTreeview").html(data);
    });
  }

  function emptyComponentTreeview() {
    $("#treeviewFolders").empty();
  }

	function deleteAlias(aliasURI, locationId) {
    var ajaxRequest = sp.ajaxRequest(aliasURI).byDeleteMethod();
    ajaxRequest.send().then(function() {
      $("#currentLocations #"+locationId).remove();
      $("#localTreeview #"+locationId).prop("checked", false);
      $("#treeviewFolders #"+locationId).prop("checked", false);
    });
  }

  function addAlias(aliasURI, locationId) {
    var ajaxRequest = sp.ajaxRequest(aliasURI).byPutMethod();
    ajaxRequest.send().then(function() {
      displayLocations();
    });
  }

  function displayLocations() {
    var ajaxRequest = sp.ajaxRequest(webContext+"/services/private/publications/<%=componentId%>/<%=id%>/locations?lang="+getUserLanguage());
    ajaxRequest.send().then(function(response) {
      var lis = "";
      response.responseAsJson().forEach(function(location) {
        var locationId = location.id+"-"+location.componentId;
        var li = "<li id=\""+locationId+"\"><span class=\"location-path\">"+location.path+"</span>";
        if (location.alias) {
          li += "<span class=\"location-user\">"+location.user.fullName+"</span>";
          li += "<span class=\"location-date\">"+sp.moment.displayAsDateTime(location.date)+"</span>";
          li += "<a class=\"delete-button\" href=\"#\" onclick=\"deleteAlias('"+location.uri+"', '"+locationId+"')\">Supprimer</a>";
        }
        li += "</li>";
        lis += li;
      });
      $("#currentLocations ul").html(lis);
    });
  }

  function topicGoTo(id) {
    location.href="GoToTopic?Id="+id;
  }
</script>
</head>
<body class="kmelia path locations">
  <%
    Window window = gef.getWindow();
    Frame frame = gef.getFrame();

    BrowseBar browseBar = window.getBrowseBar();

    browseBar.setDomainName(spaceLabel);
      browseBar.setComponentName(componentLabel, "javascript:onClick=topicGoTo('0')");
      browseBar.setPath(linkedPathString);
      browseBar.setExtraInformation(pubName);

    out.println(window.printBefore());

    KmeliaDisplayHelper.displayAllOperations(id, kmeliaScc, gef, "ViewPath", resources, out);

  out.println(frame.printBefore());

        Board board	= gef.getBoard();
        
        out.println(board.printBefore());

    //regarder si la publication est dans la corbeille
    if (publication.getLocation().isTrash()) {
      //la publi est dans la corbeille
      out.println("<div class=\"inlineMessage\">"+kmeliaScc.getString("kmelia.PubInBasket")+"</div>");
    }

        %>
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
  <div id="browsingArea" style="display:none;">
    <div class="header">
      <h3><fmt:message key="kmelia.paths.external.add"/></h3>
    </div>
    <div id="currentPath"></div>
    <div id="spaces">
      <h4><fmt:message key="GML.spaces"/></h4>
      <ul></ul>
    </div>
    <div id="components">
      <h4><fmt:message key="GML.components"/></h4>
      <ul></ul>
    </div>
  </div>
  <div id="treeview" class="accordion">
    <h4><fmt:message key="GML.folders"/></h4>
    <div id="treeviewFolders"></div>
  </div>
  </div>

        <%
    	out.println(board.printAfter());

        out.println(frame.printAfter());
        out.println(window.printAfter());
%>
</body>
</html>
