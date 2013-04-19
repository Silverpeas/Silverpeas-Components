/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 *
 * User: Yohann Chastagnier
 * Date: 19/04/13
 */

var resourceStatusColor = {};
resourceStatusColor.qtip2Class = [];
resourceStatusColor.qtip2Class['R'] = 'qtip-plain';
resourceStatusColor.qtip2Class['A'] = 'qtip-green';
resourceStatusColor.qtip2Class['V'] = 'qtip-bootstrap';
resourceStatusColor.qtip = [];
resourceStatusColor.qtip['R'] = 'light';
resourceStatusColor.qtip['A'] = 'light';
resourceStatusColor.qtip['V'] = 'light';
resourceStatusColor.event = [];
resourceStatusColor.event['R'] = 'gray';
resourceStatusColor.event['A'] = 'red';
resourceStatusColor.event['V'] = '';
resourceStatusColor.resourceClass = [];
resourceStatusColor.resourceClass['R'] = 'refused';
resourceStatusColor.resourceClass['A'] = 'waitingForValidation';
resourceStatusColor.resourceClass['V'] = 'validated';

/**
 * Useful String function.
 */
if (typeof String.prototype.endsWith !== 'function') {
  String.prototype.endsWith = function(suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1;
  };
}

/**
 * Useful date function.
 */
if (typeof Date.prototype.setDay !== 'function') {
  Date.prototype.setDay = function(year, month, dayOfMonth) {
    this.setFullYear(year);
    this.setMonth(month);
    this.setDate(dayOfMonth);
    return this;
  };
}

/**
 * Prepare calendar event according to view parameters.
 * @param reservationEvents
 * @param labels
 * @param filters
 * @returns {*}
 */
function prepareCalendarEvents(reservationEvents, labels, filters) {
  var result = filters.viewResourceData ? [] : reservationEvents;

  // Saving the title of the reservation to an unique attribute.
  $.each(reservationEvents, function(index, reservationEvent) {
    reservationEvent.reservationId = reservationEvent.id;
    reservationEvent.reservationTitle = reservationEvent.title;
    reservationEvent.reservationStatus = reservationEvent.status;

    // Resource view
    if (filters.viewResourceData) {
      $.each(reservationEvent.resources, function(index, reservedResource) {
        if (filters.resourceUri && !reservedResource.resourceURI.endsWith(filters.resourceUri)) {
          return;
        }
        if (filters.categoryUri && !reservedResource.categoryURI.endsWith(filters.categoryUri)) {
          return;
        }
        var reservedResourceEvent = $.extend({}, reservationEvent);
        reservedResourceEvent.id = reservedResource.id;
        reservedResourceEvent.title = reservedResource.name;
        reservedResourceEvent.status = reservedResource.status;
        result.push(reservedResourceEvent);
      });
    }
  });

  // Setting UI.
  $.each(result, function(index, event) {

    /*
     CALENDAR EVENT
     */

    event.backgroundColor = resourceStatusColor.event[event.status];

    /*
     QTIP
     */

    event.qtip = {};

    // Title
    event.qtip.title = labels.bookedBy + event.bookedBy;

    // Content
    var $content = $('<div>').addClass('event-qtip');
    var $rowEvent = $('<div>').addClass('row').appendTo($content);
    $('<div>').addClass('label').appendTo($rowEvent).append(labels.event + ' : ');
    $('<div>').addClass('value ' +
            resourceStatusColor.resourceClass[event.reservationStatus]).appendTo($rowEvent).append($('<a>',
            {href : "javascript:onClick=goToReservation(" + event.reservationId + ", '" +
                filters.objectView + "', " + filters.isPortlet +
                ")", title : labels.reservationLink}).append(event.reservationTitle));
    if (event.reason) {
      var $rowReason = $('<div>').addClass('row').appendTo($content);
      $('<div>').addClass('label').appendTo($rowReason).append(labels.reason + ' : ');
      $('<div>').addClass('value').appendTo($rowReason).append(event.reason.replace(/\n/, "<br/>"));
    }
    if (event.place) {
      var $rowPlace = $('<div>').addClass('row').appendTo($content);
      $('<div>').addClass('label').appendTo($rowPlace).append(labels.place + ' : ');
      $('<div>').addClass('value').appendTo($rowPlace).append(event.place);
    }
    if (event.resources && event.resources.length > 0) {
      var $rowResources = $('<div>').addClass('row').appendTo($content);
      $('<div>').addClass('label').appendTo($rowResources).append(labels.reservedResources + ' : ');
      var $reservedResources = $('<ul>').appendTo($('<div>').addClass('value').appendTo($rowResources));
      $.each(event.resources, function(index, resource) {
        $('<li>').addClass(resourceStatusColor.resourceClass[resource.status]).appendTo($reservedResources).append($('<a>',
            {href : "javascript:onClick=goToResource(" + resource.id + ", '" + filters.objectView +
                "', " + filters.isPortlet +
                ")", title : labels.resourceLink}).append(resource.name));
      });
    }
    event.qtip.content = $('<div>').append($content).html();

    // Style
    event.qtip.color = resourceStatusColor.qtip[event.status];
    event.qtip.classes = resourceStatusColor.qtip2Class[event.status];
  });

  return result;
}

function displayQTip(event) {
  var $source;
  if (!event) {
    event = null;
  }
  if (event != null && event.$event) {
    $source = $(event.$event);
  } else {
    $source = $('div.fc-event');
  }
  $source.trigger('displayQTip');
}

function hideQTip(event) {
  var $source;
  if (!event) {
    event = null;
  }
  if (event != null && event.$event) {
    $source = $(event.$event);
  } else {
    $source = $('div.fc-event');
  }
  $source.trigger('hideQTip');
}

/**
 * Function called by calendar mechanism.
 * It permits to add some UI handling for each event.
 * @param event
 * @param $event
 */
function calendarEventRender(event, $event) {
  event.$event = $event;
  $event.qtip({
    content : {
      title : {
        text : event.qtip.title,
        button : $('<div>').append($('<img>',
            {src : (webContext + '/util/icons/delete.gif'), alt : labels.close})).html()
      },
      text : event.qtip.content
    },
    style : {
      width : 'auto',
      border : {
        width : 5,
        radius : 5
      },
      padding : 7,
      textAlign : 'left',
      tip : true,
      name : event.qtip.color
    }, position : {
      adjust : { screen : true },
      corner : {
        target : 'topLeft',
        tooltip : 'bottomLeft'
      }
    },
    show : {
      delay : 0,
      when : {
        event : 'displayQTip'
      }
    },
    hide : {
      when : 'hideQTip',
      fixed : true
    }
  });
}

/**
 * Function called by calendar mechanism.
 * It permits to add some UI handling for each event.
 * THIS METHOD IS NOT CALLED FOR NOW. IT WILL BE THE CASE WHEN VERSION 2 OF QTIP PLUGIN WILL BE USED
 * @param event
 * @param $event
 */
function calendarEventRenderV2(event, $event) {
  event.$event = $event;
  $event.qtip({
    content : {
      title : {
        text : event.qtip.title,
        button : labels.close
      },
      text : event.qtip.content
    },
    style : {
      tip : true,
      classes : ('qtip-shadow ' + event.qtip.classes)
    },
    position : {
      adjust : {
        method : "flip flip"
      },
      viewport : $(window),
      at : "top left",
      my : "bottom left"
    },
    show : {
      delay : 0,
      event : "displayQTip"
    },
    hide : {
      fixed : true,
      event : "hideQTip"
    }
  });
}

/**
 * Render the listing of reservations.
 * @param reservationEvents
 * @param labels
 * @param filters
 */
function renderReservationListing(reservationEvents, labels, filters) {
  var $listing = $('<div>').addClass('reservation-listing');
  if (reservationEvents && reservationEvents.length > 0) {
    var oldDate;
    var isFirstReservationOfWeek;
    $.each(reservationEvents, function(index, reservationEvent) {
      var reservationBeginDate = $.fullCalendar.parseISO8601(reservationEvent.start);
      if (index == 0 ||
          $.datepicker.iso8601Week(oldDate) != $.datepicker.iso8601Week(reservationBeginDate)) {
        $listing.append(renderWeekSeparation(reservationBeginDate, labels));
        oldDate = reservationBeginDate;
        isFirstReservationOfWeek = true;
      }
      if (!isFirstReservationOfWeek) {
        $listing.append(renderReservationSeparation());
      }
      $listing.append(renderReservation(reservationEvent, labels, filters));
      isFirstReservationOfWeek = false;
    });
  }
  return $listing
}

/**
 * Render a separation
 * @param date
 * @param labels
 */
function renderWeekSeparation(date, labels) {
  var $separation = $('<div>').addClass('week-separation');
  $separation.append(labels.week + ' ' + $.datepicker.iso8601Week(date));
  return $separation;
}

/**
 * Render a separation
 */
function renderReservationSeparation() {
  return $('<div>').addClass('reservation-separation');
}

/**
 * Render the given reservation.
 * @param reservationEvent
 * @param labels
 * @param filters
 */
function renderReservation(reservationEvent, labels, filters) {
  if (!filters.dateFormat) {
    var dateFormat = $.datepicker.regional[filters.language];
    if (dateFormat) {
      filters.dateFormat = dateFormat.dateFormat;
    } else {
      filters.dateFormat = $.datepicker.regional[''].dateFormat;
    }
  }

  var reservationBeginDate = $.fullCalendar.parseISO8601(reservationEvent.start);
  var reservationEndDate = $.fullCalendar.parseISO8601(reservationEvent.end);
  var $reservation = $('<div>').addClass('reservation');
  var $rowEvent = $('<div>').addClass('row').appendTo($reservation);
  $('<div>').addClass('label').appendTo($rowEvent).append(labels.event + ' : ');
  $('<div>').addClass('value ' +
          resourceStatusColor.resourceClass[reservationEvent.status]).appendTo($rowEvent).append($('<a>',
          {href : "javascript:onClick=goToReservation(" + reservationEvent.id + ", '" +
              filters.objectView + "', " + filters.isPortlet +
              ")", title : labels.reservationLink}).append(reservationEvent.title));
  var $beginDate = $('<div>').addClass('row').appendTo($reservation);
  $('<div>').addClass('label').appendTo($beginDate).append(labels.beginDate + ' : ');
  $('<div>').addClass('value').appendTo($beginDate).append($.datepicker.formatDate(filters.dateFormat,
      reservationBeginDate, null));
  var $endDate = $('<div>').addClass('row').appendTo($reservation);
  $('<div>').addClass('label').appendTo($endDate).append(labels.endDate + ' : ');
  $('<div>').addClass('value').appendTo($endDate).append($.datepicker.formatDate(filters.dateFormat,
      reservationEndDate, null));
  if (reservationEvent.reason) {
    var $rowReason = $('<div>').addClass('row').appendTo($reservation);
    $('<div>').addClass('label').appendTo($rowReason).append(labels.reason + ' : ');
    $('<div>').addClass('value').appendTo($rowReason).append(reservationEvent.reason.replace(/\n/,
        "<br/>"));
  }
  if (reservationEvent.place) {
    var $rowPlace = $('<div>').addClass('row').appendTo($reservation);
    $('<div>').addClass('label').appendTo($rowPlace).append(labels.place + ' : ');
    $('<div>').addClass('value').appendTo($rowPlace).append(reservationEvent.place);
  }
  if (reservationEvent.resources && reservationEvent.resources.length > 0) {
    var $rowResources = $('<div>').addClass('row').appendTo($reservation);
    $('<div>').addClass('label').appendTo($rowResources).append(labels.reservedResources + ' : ');
    var $reservedResources = $('<ul>').appendTo($('<div>').addClass('value').appendTo($rowResources));
    $.each(reservationEvent.resources, function(index, resource) {
      $('<li>').addClass(resourceStatusColor.resourceClass[resource.status]).appendTo($reservedResources).append($('<a>',
          {href : "javascript:onClick=goToResource(" + resource.id + ", '" + filters.objectView +
              "', " + filters.isPortlet + ")", title : labels.resourceLink}).append(resource.name));
    });
  }
  return $reservation;
}

/**
 * Go to the details of the given reservation.
 * Please be careful, this function is compatible with portlet behaviour.
 * @param id
 * @param objectView
 * @param isPortlet
 */
function goToReservation(id, objectView, isPortlet) {
  var link = "ViewReservation?reservationId=" + id + "&objectView=" + objectView;
  if (isPortlet) {
    top.bottomFrame.MyMain.location.href = link;
  } else {
    location.href = link;
  }
}

/**
 * Go to the details of the given resource.
 * Please be careful, this function is compatible with portlet behaviour.
 * @param id
 * @param objectView
 * @param isPortlet
 */
function goToResource(id, objectView, isPortlet) {
  var link = "ViewResource?resourceId=" + id + "&provenance=calendar&objectView=" + objectView;
  if (isPortlet) {
    top.bottomFrame.MyMain.location.href = link;
  } else {
    location.href = link;
  }
}
