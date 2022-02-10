<%--
  Copyright (C) 2000 - 2022 Silverpeas
  
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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>

<%@ attribute name="instanceSettings" required="true" type="org.silverpeas.components.classifieds.ClassifiedsComponentSettings" %>
<%@ attribute name="classifieds" required="true" type="java.util.List" %>
<%@ attribute name="language" required="true" type="java.lang.String" %>
<%@ attribute name="emptyListMessage" required="false" type="java.lang.String" %>

<script type="text/javascript">
  function viewClassified(id) {
    $("#ClassifiedId").val(id);
    document.ViewClassifiedForm.submit();
  }
</script>

<c:if test="${not empty classifieds}">
  <ul id="classifieds_rich_list">
    <c:forEach items="${classifieds}" var="classified" varStatus="loopStatus">
      <c:set var="cssClass" value="add-with-image"/>
      <c:if test="${not instanceSettings.photosAllowed or empty classified.images}">
        <c:set var="cssClass" value="add-without-image"/>
      </c:if>

      <li class="${cssClass}" onclick="javascript:viewClassified('${classified.classifiedId}')">
        <c:if test="${instanceSettings.photosAllowed and not empty classified.images}">
          <div class="classified_thumb">
            <c:forEach var="image" items="${classified.images}" begin="0" end="0">
              <a href="#" onclick="javascript:viewClassified('${classified.classifiedId}')"><view:image src="${image.attachmentURL}" size="250x"/></a>
            </c:forEach>
          </div>
        </c:if>

        <div class="classified_info">
          <h4><a href="#" onclick="javascript:viewClassified('${classified.classifiedId}')">${classified.title}</a></h4>
          <div class="classified_type">
            <a href="javascript:viewClassifieds(0, '${classified.searchValueId1}');">${classified.searchValue1}</a>
            <a href="javascript:viewClassifieds(1, '${classified.searchValueId2}');">${classified.searchValue2}</a>
          </div>
        </div>

        <c:if test="${instanceSettings.priceAllowed and classified.price > 0}">
          <div class="classified_price">
              ${classified.price} &euro;
          </div>
        </c:if>

        <div class="classified_creationInfo">
          <c:if test="${not empty classified.validateDate}">
            <view:formatDateTime value="${classified.validateDate}" language="${language}"/>
          </c:if>
          <c:if test="${empty classified.validateDate}">
            <c:if test="${not empty classified.updateDate}">
              <view:formatDateTime value="${classified.updateDate}" language="${language}"/>
            </c:if>
            <c:if test="${empty classified.updateDate}">
              <view:formatDateTime value="${classified.creationDate}" language="${language}"/>
            </c:if>
          </c:if>
        </div>
      </li>
    </c:forEach>
  </ul>
</c:if>
<c:if test="${empty classifieds && not empty emptyListMessage}">
  <div class="inlineMessage">
    ${emptyListMessage}
  </div>
</c:if>

<form name="ViewClassifiedForm" action="ViewClassified" target="MyMain">
  <input type="hidden" name="ClassifiedId" id="ClassifiedId" value=""/>
</form>