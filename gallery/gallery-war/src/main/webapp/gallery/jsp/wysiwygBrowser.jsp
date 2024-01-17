<%--

    Copyright (C) 2000 - 2024 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.silverpeas.core.util.StringUtil" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%
  response.setDateHeader("Expires", -1);
  response.setHeader("Pragma", "no-cache");
  response.setHeader("Cache-control", "no-cache");
%>

<c:set var="componentId" value='<%=StringUtil.defaultStringIfNotDefined(Encode.forUriComponent(request.getParameter("ComponentId")))%>'/>
<c:set var="language" value='<%=StringUtil.defaultStringIfNotDefined(Encode.forUriComponent(request.getParameter("Language")))%>'/>
<c:set var="fieldName" value='<%=StringUtil.defaultStringIfNotDefined(Encode.forUriComponent(request.getParameter("FieldName")))%>'/>
<c:url var="galleryUrl" value="/GalleryInWysiwyg/dummy">
  <c:param name="ComponentId" value="${componentId}"/>
  <c:param name="Language" value="${language}"/>
</c:url>

<view:sp-page>
  <view:sp-head-part noLookAndFeel="true">
    <style type="text/css">

      html, body, div {
        width: 100%;
        height: 100%;
      }

      div {
        display: table-cell;
      }

      div, iframe {
        padding: 0;
        margin: 0;
        border: none;
      }

      body {
        margin: 0;
        padding: 0;
        border: none;
        overflow: hidden;
      }

      #layout {
        display: table;
      }

      #left {
        width: 200px;
      }

      #right {
        width: auto;
      }
    </style>
    <script type="text/javascript">
      function selectImage(url) {
        window.opener.choixImageInGallery${silfn:escapeJs(fieldName)}(url);
        window.close();
      }
    </script>
  </view:sp-head-part>
  <view:sp-body-part>
    <div id="layout">
      <div id="left">
        <iframe src="${galleryUrl}" name="treeview" title="" height="100%" width="100%"></iframe>
      </div>
      <div id="right">
        <iframe src="wysiwygImages.jsp" name="images" title="" height="100%" width="100%"></iframe>
      </div>
    </div>
  </view:sp-body-part>
</view:sp-page>
