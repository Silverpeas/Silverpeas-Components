<%--
  Copyright (C) 2000 - 2021 Silverpeas
  
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.
  
  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have recieved a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.
  
  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="highestUserRole" value="${requestScope.highestUserRole}"/>
<jsp:useBean id="highestUserRole" type="org.silverpeas.core.admin.user.model.SilverpeasRole"/>

<view:setConstant var="adminRole" constant="org.silverpeas.core.admin.user.model.SilverpeasRole.ADMIN"/>

<%@ attribute name="subAlbumList" required="true"
              type="java.util.List"
              description="The album path." %>
<jsp:useBean id="subAlbumList"
             type="java.util.List<org.silverpeas.components.gallery.model.AlbumDetail>"
             scope="page"/>

<script type="text/javascript">

  <c:if test="${highestUserRole.isGreaterThanOrEquals(adminRole)}">
  $(document).ready(function() {
    $("#albumList").sortable({opacity : 0.4, cursor : 'move'});

    $('#albumList').bind('sortupdate', function(event, ui) {
      var reg = new RegExp("album", "g");
      var data = $('#albumList').sortable('serialize');
      data += "&";  // pour que le dernier élément soit de la même longueur que les autres
      var tableau = data.split(reg);
      var param = "";
      for (var i = 0; i < tableau.length; i++) {
        if (i > 0) {
          param += ",";
        }
        param += tableau[i].substring(3, tableau[i].length - 1);
      }
      sortAlbums(param);
    });
  });

  function sortAlbums(orderedList) {
    $.get('<c:url value="/Album"/>', { orderedList : orderedList, Action : 'Sort'},
        function(data) {
          data = data.replace(/^\s+/g, '').replace(/\s+$/g, '');
          if (data == "error") {
            window.console && window.console.log("Error during sort ...");
          }
        }, 'text');
  }
  </c:if>
</script>

<div id="subTopics">
  <ul id="albumList">
    <c:forEach var="subAlbum" items="${subAlbumList}">
      <li id="album_${subAlbum.id}" class="ui-state-default">
        <a href="ViewAlbum?Id=${subAlbum.id}">
          <strong>${subAlbum.name}
            <span>${subAlbum.nbMedia}</span>
          </strong>
          <span>${subAlbum.description}</span>
        </a>
      </li>
    </c:forEach>
  </ul>
</div>
