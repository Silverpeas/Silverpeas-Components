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
<fmt:message key="GML.close" var="close"/>
<fmt:message key="kmelia.publiClassification" var="classification"/>
<c:set var="importedPublications" value="${requestScope[PublicationsDetails]}"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title><c:out value="${classification}"/></title>
    <link type="text/css" href="<c:url value='/util/styleSheets/fieldset.css'/>" rel="stylesheet" />
    <view:looknfeel />
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
        <div id="header">

          <view:pdcClassification id="default-classification" contentId="" componentId="" editable="false" />
<!--          <fieldset id="default-classification">
            <legend class="header">Classement de vos publications</legend>
            <div class="fields">
              <div id="list_pdc_positions" class="field">
                <label for="allpositions">Positions</label>
                <div id="allpositions" class="champs">
                  <ul></ul>
                </div>
              </div>
            </div>
          </fieldset>
          <br clear="all"/>
          <fieldset id="classificiation-modification" style="display: none;">
            <legend class="header">Modification du classement pour l'ensemble des publications</legend>
          </fieldset>-->



          &nbsp;
          <center>
            <view:buttonPane>
              <view:button label="${close}" action="javascript: window.close();"/>
            </view:buttonPane>
          </center>

        </div>
      </view:frame>
    </view:window>
  </body>
</html>
