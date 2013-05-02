<%--

    Copyright (C) 2000 - 2012 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<%@ include file="check.jsp" %>

<c:set var="language" value="${requestScope.resources.language}"/>
<c:set var="profile" value="${requestScope.Profile}"/>
<c:set var="isResponsible" value="${requestScope.IsResponsible}"/>
<c:set var="viewContext" value="${requestScope.viewContext}"/>
<c:set var="componentInstanceId" value="${viewContext.componentInstanceId}"/>
<c:set var="allReservations" value="allReservations"/>
<c:set var="categoryId" value="${viewContext.categoryId}"/>
<c:set var="resourceId" value="${viewContext.resourceId}"/>
<c:set var="objectView" value="${requestScope.objectView}"/>
<c:set var="allCategories" value="${requestScope.listOfCategories}"/>
<c:set var="allResources" value="${requestScope.listResourcesofCategory}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <view:looknfeel/>
  <view:includePlugin name="qtip"/>
  <view:includePlugin name="calendar"/>
  <view:includePlugin name="datepicker"/>
  <link rel="stylesheet" media="print" type="text/css" href="<c:url value="/resourcesManager/jsp/styleSheets/print_resourcesManager.css"/>">
  <script type="text/javascript" src="<c:url value="/resourcesManager/jsp/javaScript/resourceManager-calendar.js" />"></script>
  <script type="text/javascript" src="<c:url value="/util/javaScript/animation.js" />"></script>
  <script type="text/javascript">

    function viewReservationData() {
      performForm("ViewReservationData");
    }

    function viewResourceData() {
      performForm("ViewResourceData");
    }

    function viewReservationListingData() {
      performForm("ViewReservationListingData");
    }

    function viewByMonth() {
      performForm("ViewByMonth");
    }

    function viewByWeek() {
      performForm("ViewByWeek");
    }

    function nextPeriod() {
      performForm("NextPeriod");
    }

    function previousPeriod() {
      performForm("PreviousPeriod");
    }

    function goToDay(selectedDate) {
      if (!selectedDate) {
        selectedDate = '';
      }
      document.almanachForm.selectedDate.value = selectedDate;
      performForm("GoToday");
    }

    function getReservationsOfCategory(select) {
      document.almanachForm.categoryIdFilter.value = select.value;
      performForm("CategoryIdFilter", 'none');
    }

    function getReservationsOfResource(select) {
      if (select.value.length == 0) {
        getReservationsOfCategory(document.getElementById("selectCategory"));
      } else {
        document.almanachForm.categoryIdFilter.value = $('#selectCategory').val();
        document.almanachForm.resourceIdFilter.value = select.value;
        performForm("ResourceIdFilter", select.value);
      }
    }

    function performForm(action, resourceId, objectView) {
      $.progressMessage();
      if (!resourceId) {
        resourceId = null;
      }
      if (!objectView) {
        objectView = '${objectView}';
      }
      document.almanachForm.action = action;
      <c:if test="${not empty resourceId}">
      document.almanachForm.resourceId.value = ${resourceId};
      </c:if>
      if (resourceId != null) {
        document.almanachForm.resourceId.value = (resourceId != 'none' ? resourceId : null);
      }
      document.almanachForm.objectView.value = (objectView != 'none' ? objectView : null);
      document.almanachForm.submit();
    }

    function clickDay(day) {
      <c:if test="${profile != 'user'}">
      location.href = "NewReservation?objectView=${objectView}&Day=" + day;
      </c:if>
    }

    function viewOtherPlanning() {
      SP_openWindow('ChooseOtherPlanning', 'ChooseOtherPlanning', '750', '550',
          'scrollbars=yes, resizable, alwaysRaised');
    }

    // Labels
    var labels = {
      the : '<fmt:message key="GML.date.the" />',
      hourFrom : '<fmt:message key="GML.date.hour.from" />',
      from : '<fmt:message key="GML.date.from" />',
      to : '<fmt:message key="GML.date.to" />',
      hourTo : '<fmt:message key="GML.date.hour.to" />',
      close : '<fmt:message key="GML.close" />',
      week : '<fmt:message key="GML.week" />',
      bookedBy : '<fmt:message key="resourcesManager.bookedBy" />',
      reservationLink : '<fmt:message key="resourcesManager.reservationLink" />',
      resourceLink : '<fmt:message key="resourcesManager.resourceLink" />'
    };

    <c:set var="refDay" value="${viewContext.referenceDay}" />
    <c:set var="bpDay" value="${viewContext.referencePeriod.beginDate}" />
    <c:set var="epDay" value="${viewContext.referencePeriod.endDate}" />
    // Filters
    var filters = {
      isPortlet : false,
      viewResourceData : ${viewContext.dataViewType.resourcesDataView},
      categoryUri : '${viewContext.categoryUrl}',
      resourceUri : '${viewContext.resourceUrl}',
      monthlyView : ${viewContext.viewType.monthlyView},
      weeklyView : ${viewContext.viewType.weeklyView},
      language : '${language}',
      objectView : '${objectView}',
      currentDay : new Date().setDay(${refDay.year}, ${refDay.month}, ${refDay.dayOfMonth}),
      planningOfUser : ${not empty viewContext.selectedUserId}
    };

    $(document).ready(function() {

      // Calendar
      $.ajax({
        url : '${viewContext.reservationEventUrl}',
        cache : false,
        type : 'GET',
        dataType : 'json'
      }).success(function(data) {
            <c:choose>
            <c:when test="${viewContext.dataViewType.reservationListingDataView}">
            $("#reservationContent").append(renderReservationListing(data, labels, filters));
            </c:when>
            <c:otherwise>
            // Loading calendar
            $("#reservationContent").calendar({
              allDaySlot : false,
              view : '${fn:toLowerCase(viewContext.viewType.name)}',
              weekends : ${viewContext.withWeekend},
              firstDayOfWeek : ${viewContext.firstDayOfWeek},
              currentDate : filters.currentDay,
              events : prepareCalendarEvents(data, labels, filters),
              onday : clickDay,
              onevent : function(event) {
                displayQTip(event);
              },
              eventrender : calendarEventRender
            });
            </c:otherwise>
            </c:choose>
          }).error(function(jqXHR, textStatus, errorThrown) {
            alert(errorThrown);
          });
    });

    $(window).keydown(function(e){
      var keyCode = eval(e.keyCode);
      if (37 == keyCode || keyCode == 39) {
        e.preventDefault();
        if (37 == keyCode) {
          // Previous
          previousPeriod();
        } else if (39 == keyCode) {
          // Next
          nextPeriod();
        }
        return false;
      }
    });

  </script>
</head>
<body id="resourcesManager">

<fmt:message key="resourcesManager.accueil" var="tmp"/>
<view:browseBar path="${tmp}"/>

<view:operationPane>
  <c:if test="${profile != 'user'}">
    <fmt:message key="resourcesManager.creerReservation" var="tmp"/>
    <fmt:message key="resourcesManager.createReservation" var="tmpIcon" bundle="${icons}"/>
    <c:url var="tmpIcon" value="${tmpIcon}"/>
    <view:operationOfCreation altText="${tmp}" action="NewReservation?objectView=${objectView}" icon="${tmpIcon}"/>

    <view:operationSeparator/>
    <fmt:message key="resourcesManager.Reservation" var="tmp"/>
    <fmt:message key="resourcesManager.viewMyReservations" var="tmpIcon" bundle="${icons}"/>
    <c:url var="tmpIcon" value="${tmpIcon}"/>
    <view:operation altText="${tmp}" action="Calendar?objectView=${personalReservations}" icon="${tmpIcon}"/>

    <fmt:message key="resourcesManager.viewUserReservation" var="tmp"/>
    <fmt:message key="resourcesManager.viewUserReservation" var="tmpIcon" bundle="${icons}"/>
    <c:url var="tmpIcon" value="${tmpIcon}"/>
    <view:operation altText="${tmp}" action="javascript:onClick=viewOtherPlanning()" icon="${tmpIcon}"/>

    <fmt:message key="resourcesManager.viewAllReservation" var="tmp"/>
    <fmt:message key="resourcesManager.viewAllReservation" var="tmpIcon" bundle="${icons}"/>
    <c:url var="tmpIcon" value="${tmpIcon}"/>
    <view:operation altText="${tmp}" action="Calendar?objectView=${allReservations}" icon="${tmpIcon}"/>
    <c:if test="${viewContext.dataViewType.reservationListingDataView}">
      <view:operationSeparator/>
      <fmt:message key="GML.print" var="tmp"/>
      <fmt:message key="GML.print" var="tmpIcon" bundle="${icons}"/>
      <c:url var="tmpIcon" value="${tmpIcon}"/>
      <view:operation altText="${tmp}" action="javascript:print()" icon="${tmpIcon}"/>
    </c:if>
  </c:if>
  <c:if test="${isResponsible}">
    <view:operationSeparator/>
    <fmt:message key="resourcesManager.viewReservationForValidation" var="tmp"/>
    <fmt:message key="resourcesManager.viewReservationForValidation" var="tmpIcon" bundle="${icons}"/>
    <c:url var="tmpIcon" value="${tmpIcon}"/>
    <view:operation altText="${tmp}" action="Calendar?objectView=viewForValidation" icon="${tmpIcon}"/>
  </c:if>
  <c:if test="${profile eq 'admin'}">
    <view:operationSeparator/>
    <fmt:message key="resourcesManager.gererCategorieRessource" var="tmp"/>
    <fmt:message key="resourcesManager.gererCategorie" var="tmpIcon" bundle="${icons}"/>
    <c:url var="tmpIcon" value="${tmpIcon}"/>
    <view:operationOfCreation altText="${tmp}" action="ViewCategories" icon="${tmpIcon}"/>
  </c:if>
</view:operationPane>

<view:window>

  <view:areaOfOperationOfCreation/>

  <view:tabs>
    <fmt:message key="GML.week" var="tmp"/>
    <c:set var="tmpAction">javascript:onClick=viewByWeek()</c:set>
    <view:tab label="${tmp}" action="${tmpAction}" selected="${viewContext.viewType.weeklyView}"/>
    <fmt:message key="GML.month" var="tmp"/>
    <c:set var="tmpAction">javascript:onClick=viewByMonth()</c:set>
    <view:tab label="${tmp}" action="${tmpAction}" selected="${viewContext.viewType.monthlyView}"/>
  </view:tabs>

  <view:frame>
    <div class="sousNavBulle">
      <div id="navigation">

        <div id="others">
          <h3 id="planning-context">
            <c:choose>
              <c:when test="${viewContext.forValidation}">
                <fmt:message key="resourcesManager.viewReservationForValidation"/>
              </c:when>
              <c:when test="${empty viewContext.selectedUser}">
                <fmt:message key="resourcesManager.allPlanning"/>
              </c:when>
              <c:when test="${viewContext.selectedUserId eq viewContext.currentUserId}">
                <fmt:message key="resourcesManager.myPlanning"/>
              </c:when>
              <c:otherwise>
                <fmt:message key="resourcesManager.planningFrom"/> ${viewContext.selectedUser.displayedName}
              </c:otherwise>
            </c:choose>
          </h3>

          <p><fmt:message key="GML.view.mode"/>
            <c:choose>
              <c:when test="${not viewContext.dataViewType.reservationListingDataView}">
                <c:set var="hrefTmp" value="#"/>
                <c:set var="classTmp" value=" active"/>
              </c:when>
              <c:otherwise>
                <c:set var="hrefTmp" value="javascript:onClick=viewReservationData()"/>
                <c:set var="classTmp" value=""/>
              </c:otherwise>
            </c:choose>
            <a class="calendar-mode${classTmp}" href="${hrefTmp}" title="<fmt:message key="resourcesManager.calendarViewType"/>"></a>
            <c:choose>
              <c:when test="${viewContext.dataViewType.reservationListingDataView}">
                <c:set var="hrefTmp" value="#"/>
                <c:set var="classTmp" value=" active"/>
              </c:when>
              <c:otherwise>
                <c:set var="hrefTmp" value="javascript:onClick=viewReservationListingData()"/>
                <c:set var="classTmp" value=""/>
              </c:otherwise>
            </c:choose>
            <a class="list-mode${classTmp}" href="${hrefTmp}" title="<fmt:message key="resourcesManager.listViewType"/>"></a>
          </p>
        </div>

        <div id="currentScope">
          <a href="javascript:onClick=previousPeriod()" onfocus="this.blur()"><img align="top" border="0" alt="" src="<c:url value="/util/icons/arrow/arrowLeft.gif"/>"></a>
          <span class="txtnav">${viewContext.referencePeriodLabel}</span>
          <a href="javascript:onClick=nextPeriod()" onfocus="this.blur()"><img align="top" border="0" alt="" src="<c:url value="/util/icons/arrow/arrowRight.gif"/>"></a>
          <span id="today"> <a href="javascript:onClick=goToDay()" onfocus="this.blur()"><fmt:message key="resourcesManager.auJour"/></a></span>
        </div>

        <p>
          <c:if test="${not viewContext.dataViewType.reservationListingDataView}">
            <fmt:message key="GML.Show" var="tmp"/>
            ${fn:replace(tmp, ':', '')}
            <c:set var="classTmp" value=""/>
            <c:if test="${viewContext.dataViewType.reservationsDataView}">
              <c:set var="classTmp" value=" active"/>
            </c:if>
            <a class="${classTmp}" href="javascript:onClick=viewReservationData()"><fmt:message key="resourcesManager.reservationsDataView"/></a>
            <c:set var="classTmp" value=""/>
            <c:if test="${viewContext.dataViewType.resourcesDataView}">
              <c:set var="classTmp" value=" active"/>
            </c:if>
            <a class="${classTmp}" href="javascript:onClick=viewResourceData()"><fmt:message key="resourcesManager.resourcesDataView"/></a>
            -
          </c:if>
          <fmt:message key="GML.filterBy"/>
          <select id="selectCategory" name="selectCategory" onchange="getReservationsOfCategory(this)" class="selectNS">
            <c:if test="${not empty allCategories}">
              <option value="">
                <fmt:message key="resourcesManager.categories"/></option>
              <option value="">-----------------</option>
              <c:forEach items="${allCategories}" var="category">
                <option value="${category.id}" <c:if
                    test="${category.id eq categoryId}">selected="selected"</c:if>>${category.name}</option>
              </c:forEach>
            </c:if>
          </select>
          <c:if test="${not empty allResources}">
            <select name="selectResource" onchange="getReservationsOfResource(this)">
              <option value="">
                <fmt:message key="resourcesManager.allResources"/></option>
              <option value="">-----------------</option>
              <c:forEach items="${allResources}" var="resource">
                <option value="${resource.id}" <c:if
                    test="${resource.id eq resourceId}">selected="selected"</c:if>>${resource.name}</option>
              </c:forEach>
            </select>
          </c:if>
          <span style="line-height: 25px">&nbsp;</span>
        </p>

      </div>
    </div>
    <a id="legendLabelId" class="txtlibform" onclick="$('#legende').toggle()"><fmt:message key="resourcesManager.legend"/></a>
    <ul id="legende" style="display: none">
      <c:choose>
        <c:when test="${not viewContext.dataViewType.reservationListingDataView and viewContext.dataViewType.reservationsDataView}">
          <li><div class="resource validated">&nbsp;</div><fmt:message key="resourcesManager.legend.reservation.validated"/></li>
          <li><div class="resource waitingForValidation">&nbsp;</div><fmt:message key="resourcesManager.legend.reservation.waitingForValidation"/></li>
          <li><div class="resource refused">&nbsp;</div><fmt:message key="resourcesManager.legend.reservation.refused"/></li>
        </c:when>
        <c:otherwise>
          <li><div class="resource validated">&nbsp;</div><fmt:message key="resourcesManager.legend.resource.validated"/></li>
          <li><div class="resource waitingForValidation">&nbsp;</div><fmt:message key="resourcesManager.legend.resource.waitingForValidation"/></li>
          <li><div class="resource refused">&nbsp;</div><fmt:message key="resourcesManager.legend.resource.refused"/></li>
        </c:otherwise>
      </c:choose>
    </ul>
    <div id="reservationContent"></div>
  </view:frame>
</view:window>
<form name="almanachForm" action="" method="post">
  <input type="hidden" name="objectView" value=""/>
  <input type="hidden" name="resourceId" value=""/>
  <input type="hidden" name="idUser" value=""/>
  <input type="hidden" name="firstNameUser" value=""/>
  <input type="hidden" name="lastName" value=""/>
  <input type="hidden" name="selectedDate" value=""/>
  <input type="hidden" name="categoryIdFilter" value=""/>
  <input type="hidden" name="resourceIdFilter" value=""/>
</form>
<view:progressMessage/>
</body>
</html>