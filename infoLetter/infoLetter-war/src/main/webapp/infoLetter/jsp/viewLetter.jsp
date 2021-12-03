<%--
  Copyright (C) 2000 - 2022 Silverpeas

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
  along with this program. If not, see <http://www.gnu.org/licenses/>.
  --%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ include file="check.jsp" %>

<c:set var="origin" value='${silfn:fullApplicationURL(pageContext.request).replaceFirst("(https?://[^/]+)(.*)", "$1")}'/>
<c:set var="componentId" value="<%=componentId%>"/>
<c:set var="parution" value="${requestScope.parution}"/>
<c:set var="parutionTitle" value="${requestScope.parutionTitle}"/>
<c:set var="inlinedCssHtml" value="${requestScope.inlinedCssHtml}"/>

<view:sp-page>
  <view:sp-head-part>
    <view:script src="/infoLetter/jsp/javaScripts/infoLetter.js"/>
    <script type="text/javascript">
      function goFiles() {
        sp.navRequest('FilesView').withParam('parution', ${parution}).go();
      }
    </script>
  </view:sp-head-part>
  <view:sp-body-part>
    <view:browseBar path="${parutionTitle}"/>
    <view:window>
      <view:frame>
        <div class="preview view">
          <div class="rightContent">
            <c:import url="/attachment/jsp/displayAttachedFiles.jsp">
              <c:param name="Id" value="${parution}"/>
              <c:param name="ComponentId" value="${componentId}"/>
              <c:param name="Context" value="${'attachment'}"/>
            </c:import>
          </div>
          <div class="principalContent">
            <div id="inlined-css-html-container"></div>
            <script type="text/javascript">
              whenSilverpeasReady().then(function() {
                monitorHeightOfIsolatedDisplay('${parution}', '${origin}');
              });
            </script>
          </div>
        </div>
      </view:frame>
    </view:window>
  </view:sp-body-part>
</view:sp-page>