<%--

    Copyright (C) 2000 - 2009 Silverpeas

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
<%@ include file="check.jsp"%>
  <c:set var="componentId" value="${requestScope.componentId}" />
  <c:set var="browseContext" value="${requestScope.browseContext}" />
  <c:set var="listNews" value="${requestScope.ListNews}"/>
  <fmt:setLocale value="${sessionScope[sessionController].language}" />
  <view:setBundle bundle="${requestScope.resources.multilangBundle}" var="DML"/>
  <view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
  <view:setBundle basename="com.stratelia.webactiv.kmelia.multilang.kmeliaBundle" var="KML"/>
  <view:setBundle basename="com.stratelia.webactiv.multilang.generalMultilang" var="GML"/>
  <fmt:message key="GML.dateFormat" bundle="${GML}" var="dateFormat"/>
  <fmt:message key="GML.hourFormat" bundle="${GML}" var="hourFormat"/>
  <c:set var="dateHourFormat"><c:out value="${dateFormat}" /> <c:out value="${hourFormat}" /></c:set>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <view:looknfeel />
    <script type="text/javascript" src="<c:url value='/util/javaScript/animation.js'/>"></script>
    <script type="text/javascript">
    function openPublication(pubId) {
      url = "OpenPublication?PubId="+pubId;
      SP_openWindow(url,'publication','800','600','scrollbars=yes, noresize, alwaysRaised');
    }
    
    function refuseDelegatedNews(pubId) {
      url = "EditRefuseReason?PubId="+pubId;
      SP_openWindow(url,'refuseReason','500','150','scrollbars=no, noresize, alwaysRaised');
    }
    
    function updateDateDelegatedNews(pubId, BeginDate, BeginHour, EndDate, EndHour) {
      url = "EditUpdateDate?PubId="+pubId+"&BeginDate="+BeginDate+"&BeginHour="+BeginHour+"&EndDate="+EndDate+"&EndHour="+EndHour;
      SP_openWindow(url,'updateDate','500','240','scrollbars=no, noresize, alwaysRaised');
    }
    </script>
  </head>  
  <body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
    <view:window>
      <view:frame>
        <view:board>
          <fmt:message key="delegatednews.listNews" bundle="${DML}"/>
      <view:tabs>
        
      <FORM NAME="listDelegatedNews" ACTION="" METHOD="POST">
      <input type="hidden" name="PubId">
      <input type="hidden" name="RefuseReasonText">
      <input type="hidden" name="BeginDate">
      <input type="hidden" name="BeginHour">
      <input type="hidden" name="EndDate">
      <input type="hidden" name="EndHour">
      
      <table width="100%" border="0" align="center" cellpadding="4" cellspacing="1" class="testTableau">
        <tr class="ArrayColumn">
          <td align="center" nowrap="nowrap"><fmt:message key="PubTitre" bundle="${KML}"/></td>
          <td align="center" nowrap="nowrap"><fmt:message key="delegatednews.updateDate" bundle="${DML}"/></td>
          <td align="center" nowrap="nowrap"><fmt:message key="delegatednews.contributor" bundle="${DML}"/></td>
          <td align="center" nowrap="nowrap"><fmt:message key="PubState" bundle="${KML}"/></td>
          <td align="center" nowrap="nowrap"><fmt:message key="delegatednews.visibilityBeginDate" bundle="${DML}"/></td>
          <td align="center" nowrap="nowrap"><fmt:message key="delegatednews.visibilityEndDate" bundle="${DML}"/></td>
          <%
          boolean isAdmin = newsScc.isAdmin();
          if(isAdmin) {
          %>
      <td align="center" nowrap="nowrap"><fmt:message key="Operations" bundle="${KML}"/></td>
          <%
          }
          %>
        </tr>
        
        <c:if test="${not empty listNews}">
      <c:forEach var="i" begin="0" end="${fn:length(listNews) - 1}" step="1">
      <c:set var="delegatedNews" value="${listNews[i]}"/>
      <c:set var="pubId" value="${delegatedNews.pubId}" />
      <tr>
        <td><a href="javascript:onClick=openPublication('${pubId}')"><c:out value='${delegatedNews.publicationDetail.name}'/></a></td>
        <fmt:formatDate var="updateDate" pattern="${dateFormat}" value="${delegatedNews.publicationDetail.updateDate}" />
        <td><c:out value='${updateDate}'/></td>
        <c:set var="contributorId" value="${delegatedNews.contributorId}" />
        <%
        String theContributorId = (String) pageContext.getAttribute("contributorId");
        String contributorName = newsScc.getUserDetail(theContributorId).getDisplayedName();
        %>
        <td><%=contributorName%></td>
        <c:set var="keyStatus">delegatednews.status.<c:out value="${delegatedNews.status}" /></c:set>
        <td><fmt:message key='${keyStatus}' bundle="${DML}"/></td>
        <fmt:formatDate var="beginDate" pattern="${dateFormat}" value="${delegatedNews.beginDate}" />
        <fmt:formatDate var="beginHour" pattern="${hourFormat}" value="${delegatedNews.beginDate}" />
        <fmt:formatDate var="beginDateHour" pattern="${dateHourFormat}" value="${delegatedNews.beginDate}" />
        <td><c:out value='${beginDateHour}'/></td>
        <fmt:formatDate var="endDate" pattern="${dateFormat}" value="${delegatedNews.endDate}" />
        <fmt:formatDate var="endHour" pattern="${hourFormat}" value="${delegatedNews.endDate}" />
        <fmt:formatDate var="endDateHour" pattern="${dateHourFormat}" value="${delegatedNews.endDate}" />
        <td><c:out value='${endDateHour}'/></td>
        <%
        if(isAdmin) {
        %>
        <td>
        <a href="javascript:onClick=updateDateDelegatedNews('${pubId}', '${beginDate}', '${beginHour}', '${endDate}', '${endHour}')"><img src="/silverpeas/util/icons/update.gif" title="<fmt:message key="GML.modify" bundle="${GML}"/>" alt="<fmt:message key="GML.modify" bundle="${GML}"/>" /></a>
        <a href="ValidateDelegatedNews?PubId=${pubId}"><img src="/silverpeas/util/icons/ok.gif" title="<fmt:message key="Validate" bundle="${KML}"/>" alt="<fmt:message key="Validate" bundle="${KML}"/>" /></a>
        <a href="javascript:onClick=refuseDelegatedNews('${pubId}')"><img src="/silverpeas/util/icons/delete.gif" title="<fmt:message key="PubUnvalidate?" bundle="${KML}"/>" alt="<fmt:message key="PubUnvalidate?" bundle="${KML}"/>" /></a>
        </td>
        <%
        }
        %>
      </tr>
      </c:forEach>
        </c:if>
      </table>
      </FORM>  
       
      </view:tabs>
  
        </view:board>
      </view:frame>
    </view:window>
  </body>
</html>
