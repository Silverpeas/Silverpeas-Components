<%--

    Copyright (C) 2000 - 2022 Silverpeas

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
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="check.jsp" %>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="inlinedCssHtml" value="${requestScope.inlinedCssHtml}"/>
<c:set var="origin" value='${silfn:fullApplicationURL(pageContext.request).replaceFirst("(https?://[^/]+)(.*)", "$1")}'/>
<view:sp-page>
  <view:sp-head-part noLookAndFeel="true">
  </view:sp-head-part>
  <view:sp-body-part>
    ${inlinedCssHtml}
    <script type="text/javascript">
      (function() {
        function postHeight() {
          const body = document.body, html = document.documentElement;
          const height = Math.max(body.scrollHeight, body.offsetHeight, html.clientHeight, html.scrollHeight, html.offsetHeight);
          window.postMessage(JSON.stringify({'display_height' : height}), '${origin}');
        }
        postHeight();
        document.addEventListener('readystatechange', function() {
          if (document.readyState === 'complete') {
            postHeight();
            setTimeout(postHeight, 0);
          }
        });
      })();
    </script>
  </view:sp-body-part>
</view:sp-page>