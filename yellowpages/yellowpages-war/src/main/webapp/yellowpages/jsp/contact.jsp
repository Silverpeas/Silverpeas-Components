<%--

    Copyright (C) 2000 - 2020 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page import="org.silverpeas.core.contact.model.CompleteContact" %>
<%@page import="org.silverpeas.core.contact.model.ContactDetail" %>
<%@ page import="org.silverpeas.core.contribution.content.form.Form" %>
<%@ page import="org.silverpeas.core.contribution.content.form.PagesContext" %>
<%@ page import="org.silverpeas.core.util.StringUtil" %>
<%@ page import="org.silverpeas.core.util.WebEncodeHelper" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="userLanguage" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${userLanguage}" />
<view:setBundle basename="org.silverpeas.yellowpages.multilang.yellowpagesBundle" />

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<%@ include file="checkYellowpages.jsp" %>

<%
  CompleteContact fullContact = (CompleteContact) request.getAttribute("Contact");
  ContactDetail contact = fullContact.getContactDetail();

  Form formView = fullContact.getViewForm();
  PagesContext context = (PagesContext) request.getAttribute("PagesContext");
%>

<view:link href="/util/styleSheets/fieldset.css"/>
<view:link href="styleSheets/printContact.css"/>

<script type="text/javascript">
  function closePopup() {
    window.contactPopup.close();
  }
</script>
<div id="printSection">
<fieldset id="identity-base" class="skinFieldset">
  <legend class="without-img"><%=WebEncodeHelper
      .javaStringToHtmlString(contact.getFirstName()) %> <%= WebEncodeHelper
      .javaStringToHtmlString(contact.getLastName()) %>
  </legend>

  <div class="oneFieldPerLine">
    <% if (StringUtil.isDefined(contact.getEmail())) { %>
    <div class="field" id="email">
      <label class="txtlibform"><%=resources.getString("GML.eMail")%>
      </label>
      <div class="champs">
        <a href=mailto:"<%=WebEncodeHelper.javaStringToHtmlString(contact.getEmail()) %>"><%=WebEncodeHelper
            .javaStringToHtmlString(WebEncodeHelper.javaStringToHtmlString(contact.getEmail())) %>
        </a></div>
    </div>
    <% } %>
    <% if (StringUtil.isDefined(contact.getPhone())) { %>
    <div class="field" id="phone">
      <label class="txtlibform"><%=resources.getString("GML.phoneNumber")%>
      </label>
      <div class="champs"><%=WebEncodeHelper.javaStringToHtmlString(contact.getPhone()) %>
      </div>
    </div>
    <% } %>
    <% if (StringUtil.isDefined(contact.getFax())) { %>
    <div class="field" id="fax">
      <label class="txtlibform"><%=resources.getString("GML.faxNumber")%>
      </label>
      <div class="champs"><%=WebEncodeHelper.javaStringToHtmlString(contact.getFax()) %>
      </div>
    </div>
    <% } %>
  </div>
</fieldset>

<% if (formView != null) { %>
<fieldset id="identity-extra" class="skinFieldset">
  <legend class="without-img"><%=resources.getString("GML.bloc.further.information")%>
  </legend>
  <%
    formView.display(out, context);
  %>
</fieldset>
<% } %>
</div>
<view:buttonPane>
  <fmt:message key="GML.print" var="labelPrint"/>
  <fmt:message key="GML.close" var="labelClose"/>
  <view:button label="${labelPrint}" action="javascript:window.print()"/>
  <view:button label="${labelClose}" action="javascript:closePopup()"/>
</view:buttonPane>