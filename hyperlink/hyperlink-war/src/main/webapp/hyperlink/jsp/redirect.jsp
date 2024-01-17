<%--
  ~ Copyright (C) 2000 - 2024 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ include file="check.jsp" %>

<c:set var="url" value="GoToURL"/>

<view:sp-page>
<view:sp-head-part>
  <script type="text/javascript">
    function redirectUser() {
      sp.navRequest('${url}').addParam('fromRedirect', true).toTarget('_blank').go();
    }
  </script>
</view:sp-head-part>
<view:sp-body-part onLoad="redirectUser()">
<view:window>
  <view:frame>
    <div class="inlineMessage">
      <%=resource.getString("hyperlink.explications")%>
      &nbsp;<a href="javascript:void(0)" onclick="redirectUser()"><%=resource.getString("hyperlink.ici")%>
    </a>
    </div>
  </view:frame>
</view:window>
</view:sp-body-part>
</view:sp-page>