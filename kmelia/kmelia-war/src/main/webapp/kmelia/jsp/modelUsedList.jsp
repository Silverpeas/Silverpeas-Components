<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%--

    Copyright (C) 2000 - 2024 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib prefix="slifn" uri="http://www.silverpeas.com/tld/silverFunctions" %>
<%@ include file="checkKmelia.jsp" %>
<fmt:setLocale value="${sessionScope[sessionController].language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<c:set var="spaceId" value="${requestScope.browse[2]}"/>
<c:set var="componentId" value="${requestScope.browse[3]}"/>
<c:set var="kmelia" value="${requestScope.kmelia}"/>
<jsp:useBean id="kmelia" type="org.silverpeas.components.kmelia.control.KmeliaSessionController"/>
<c:set var="templates" value="${requestScope.XMLForms}"/>
<c:set var="usedTemplates" value="${requestScope.ModelUsed}"/>

<fmt:message var="validateLabel" key="GML.validate"/>
<fmt:message var="cancelLabel" key="GML.cancel"/>

<view:sp-page>
    <view:sp-head-part withFieldsetStyle="true">
        <title><fmt:message key="kmelia.ModelUsed"/></title>
        <script language="javaScript">

          function topicGoTo(id) {
            location.href = "GoToTopic?Id=" + id;
          }

          function sendData() {
            document.model.mode.value = 'delete';
            document.model.submit();
          }

          function closeWindows() {
            if (window.publicationWindow != null)
              window.publicationWindow.close();
          }
        </script>
    </view:sp-head-part>
    <view:sp-body-part onUnload="closeWindows()"
                       id="${componentId}">

        <view:browseBar spaceId="${spaceId}"
                        componentId="${componentId}"
                        path="${kmelia.sessionPath}">
        </view:browseBar>
        <view:window>
            <view:frame>
                <view:board>
                    <form name="model" action="SelectModel" method="post">
                        <input type="hidden" name="mode"/>
                        <table cellpadding="5" width="100%" id="templates">
                            <tr>
                                <td colspan="3" class="txtnav">
                                    <fmt:message key="kmelia.ModelList"/>
                                </td>
                            </tr>
                            <tr>
                            <c:set var="idx" value="0"/>
                            <c:forEach var="template" items="${templates}">
                                <jsp:useBean id="template"
                                             type="org.silverpeas.core.contribution.template.publication.PublicationTemplate"/>
                                <c:if test="${idx != 0 && idx % 3 == 0}">
                            </tr><tr>
                                </c:if>
                                <c:set var="idx" value="${idx+1}"/>
                                <c:set var="checked" value=""/>
                                <c:if test="${usedTemplates.contains(template.fileName)}">
                                    <c:set var="checked" value="checked"/>
                                </c:if>
                                <c:set var="thumbnail" value="${template.thumbnail}"/>
                                <c:if test="${slifn:isNotDefined(thumbnail)}">
                                    <c:set var="thumbnail"><%= PublicationTemplate.DEFAULT_THUMBNAIL %></c:set>
                                </c:if>
                                <td class="template">
                                    <img src="${thumbnail}" alt="${template.description}"/><br/>
                                    ${silfn:escapeHtml(template.name)}<br/>
                                    <input type="checkbox" name="modelChoice"
                                           value="${template.fileName}" ${checked} />
                                </td>
                            </c:forEach>
                            <c:if test="${idx != 0 && idx % 3 == 0}">
                            </tr><tr>
                            </c:if>
                            <c:set var="checked" value=""/>
                            <c:if test="${usedTemplates.contains('WYSIWYG')}">
                                <c:set var="checked" value="checked"/>
                            </c:if>
                                <td class="template">
                                    <img src="../../util/icons/model/wysiwyg.gif" alt="Wysiwyg"/><br/>
                                    WYSIWYG<br/>
                                    <input type="checkbox" name="modelChoice" value="WYSIWYG" ${checked}/>
                                </td>
                            </tr>
                        </table>
                    </form>

                </view:board>
                <view:buttonPane cssClass="center">
                    <view:button label="${validateLabel}" action="javascript:onClick=sendData();"/>
                    <view:button label="${cancelLabel}" action="GoToCurrentTopic"/>
                </view:buttonPane>
            </view:frame>
        </view:window>
    </view:sp-body-part>>
</view:sp-page>