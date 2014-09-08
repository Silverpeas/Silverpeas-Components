<%--

    Copyright (C) 2000 - 2013 Silverpeas

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

<%@page import="com.silverpeas.portlets.QuickInfosPortlet"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="javax.portlet.RenderRequest" %>
<%@ page import="javax.portlet.RenderResponse" %>
<%@ page import="javax.portlet.PortletPreferences" %>
<%@ page import="javax.portlet.PortletURL" %>
<%@ page import="com.silverpeas.portlets.FormNames" %>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<portlet:defineObjects/>
<portlet:actionURL var="actionURL"/>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="org.silverpeas.quickinfo.multilang.quickinfo"/>

<style>
.txtlibform {
  padding-right: 10px;
  width: 150px;
}
</style>

<%
RenderRequest pReq = (RenderRequest)request.getAttribute("javax.portlet.request");
RenderResponse rRes = (RenderResponse)request.getAttribute("javax.portlet.response");
PortletPreferences pref = pReq.getPreferences();
    
String slideshowSelected="";
String listSelected = "selected=\"selected\"";;
if (pref.getValue(QuickInfosPortlet.PARAM_DISPLAY, "list").equals("slideshow")) {
  slideshowSelected = "selected=\"selected\"";
  listSelected = "";
}
%>

    <form name="inputForm" target="_self" method="POST" action="<c:out value="${actionURL}" />">
        <table border="0" width="100%">
            <tr>
                <td class="txtlibform"><fmt:message key="quickinfo.portlet.pref.display.mode" /> :</td>
                <td><select name="<%=QuickInfosPortlet.PARAM_DISPLAY%>">
                	<option value="slideshow" <%=slideshowSelected %>><fmt:message key="quickinfo.portlet.pref.display.mode.slideshow" /></option>
                	<option value="list" <%=listSelected %>><fmt:message key="quickinfo.portlet.pref.display.mode.list" /></option>
                </select></td>
            </tr>
            <tr>
                <td colspan="2">
                    <input class="portlet-form-button" name="<%=FormNames.SUBMIT_FINISHED%>" type="submit" value="<fmt:message key="GML.validate"/>"/>
                    <input class="portlet-form-button" name="<%=FormNames.SUBMIT_CANCEL%>" type="submit" value="<fmt:message key="GML.cancel"/>"/>
                </td>
            </tr>
        </table>
    </form>