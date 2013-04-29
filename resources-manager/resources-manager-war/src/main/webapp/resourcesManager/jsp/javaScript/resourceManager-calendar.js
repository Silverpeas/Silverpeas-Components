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
resourceStatusColor.event['R'] = '#f15858';
resourceStatusColor.event['A'] = '#a7a7a7';
resourceStatusColor.event['V'] = '#87bd41';
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
 * Useful date function.
 */
if (typeof Date.prototype.getHoursAndMinutes !== 'function') {
  Date.prototype.getHoursAndMinutes = function() {
    var result = this.getHours() + ':';
    if (this.getMinutes() < 10) {
      result += '0';
    }
    result += this.getMinutes();
    return result;
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
    reservationEvent.calendarViewMode = true;
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

    event.borderColor = resourceStatusColor.event[event.status];
    event.backgroundColor = resourceStatusColor.event[event.status];

    /*
     QTIP
     */

    event.qtip = {};

    // Title
    event.qtip.title = labels.bookedBy + event.bookedBy;

    // Content
    var $content = $('<div>').addClass('event-qtip').append(renderReservation(event, labels,
        filters).html());
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
        button : $('<div>').append($('<img>', {src : (webContext +
            '/util/icons/qtip-closed.gif'), alt : labels.close, style : 'margin-top: -2px;'})).html()
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
  var $listing = $('<ul>', {id : 'reservationList'});
  var $periodBloc;
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
        $periodBloc = $('<ul>').addClass('reservationList').appendTo($listing);
      }
      if (!isFirstReservationOfWeek) {
        $listing.append(renderReservationSeparation());
      }
      $periodBloc.append(renderReservation(reservationEvent, labels, filters));
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
  var $separation = $('<h3>').addClass('reservationPeriode');
  $separation.append(labels.week + ' ' + $.datepicker.iso8601Week(date));
  return $('<li>').append($separation);
}

/**
 * Render a separation
 */
function renderReservationSeparation() {
  return $('<div>').addClass('reservation-separation');
}

/**
 * Render the given reservation.
 * @param reservation
 * @param labels
 * @param filters
 */
function renderReservation(reservation, labels, filters) {
  if (!filters.dateFormat) {
    var dateFormat = $.datepicker.regional[filters.language];
    if (dateFormat) {
      filters.dateFormat = dateFormat.dateFormat;
    } else {
      filters.dateFormat = $.datepicker.regional[''].dateFormat;
    }
  }

  // Initialization
  var $reservation = $('<li>').addClass('reservation');
  if (!reservation.calendarViewMode) {
    $reservation.click(function(event) {
      goToReservation(reservation.id, filters.objectView, filters.isPortlet);
      return false;
    });
  }

  // Dates & Author
  if (!reservation.calendarViewMode) {
    $reservation.append(renderReservationAuthor(reservation, labels, filters).attr('title',
        labels.reservationLink));
  }

  // Title
  var goToReservationLink = "javascript:onClick=goToReservation(" + reservation.reservationId +
      ", '" + filters.objectView + "', " + filters.isPortlet + ")";
  if (reservation.calendarViewMode) {
    $reservation.append($('<h4>').append($('<a>',
        {href : goToReservationLink, title : labels.reservationLink}).append(reservation.reservationTitle)));
  } else {
    $reservation.append($('<h4>').append($('<a>').append(reservation.title).attr('title',
        labels.reservationLink)));
  }

  // Place
  if (reservation.place) {
    if (reservation.calendarViewMode) {
      $reservation.append($('<p>').addClass('reservationPlace').append(reservation.place));
    } else {
      var $info = $('<div>').addClass('reservationInfo');
      var $place = $('<div>').addClass('reservationPlace');
      var $bloc = $('<div>').addClass('bloc').append($('<span>').append(reservation.place));
      $reservation.append($info.append($place.append($bloc)));
    }
  }

  // Reason
  $reservation.append($('<p>').addClass('reservationDesc').append(reservation.reason.replace(/\n/,
      "<br/>")));

  // Resources
  if (reservation.resources && reservation.resources.length > 0) {
    var $resources = $('<ul>').addClass('reservationRessources').appendTo($reservation);
    $.each(reservation.resources, function(index, resource) {
      var $resource = $('<li>').addClass(resourceStatusColor.resourceClass[resource.status]).append($('<a>',
          {href : '#', title : labels.resourceLink}).append(resource.name));
      $resources.append($resource);
      if (!reservation.calendarViewMode) {
        $('a', $resource).click(function() {
          goToResource(resource.id, filters.objectView, filters.isPortlet);
          return false;
        });
      } else {
        $('a', $resource).attr('href',
            "javascript:onClick=goToResource(" + resource.id + ", '" + filters.objectView + "', " +
                filters.isPortlet + ")");
      }
    });
  }

  // Clear
  $reservation.append($('<hr>').addClass('clear'))

  // Link
  if (reservation.calendarViewMode) {
    $reservation.append($('<a>',
        {href : goToReservationLink}).addClass('reservation-link').append(labels.reservationLink));
  }

  // The reservation
  return $reservation;
}

/**
 * Render the given reservation author.
 * @param reservation
 * @param labels
 * @param filters
 */
function renderReservationAuthor(reservation, labels, filters) {
  var $author = $('<h5>').append(renderReservationDates(reservation, labels, filters));
  if (!filters.planningOfUser) {
    $author.append(' - ').append($('<span>').addClass('reservationAuthor').append(labels.bookedBy));
    $author.append(' ').append(reservation.bookedBy);
  }
  return $author;
}

/**
 * Render the given reservation author.
 * @param reservation
 * @param labels
 * @param filters
 */
function renderReservationDates(reservation, labels, filters) {
  var $result = $('<span>').addClass('reservationDate');
  var isReservationOnOneDay = true;
  var beginDate = $.fullCalendar.parseISO8601(reservation.start);
  var endDate = $.fullCalendar.parseISO8601(reservation.end);

  // Is the reservation on one day ?
  if (beginDate.getYear() != endDate.getYear() || beginDate.getMonth() != endDate.getMonth() ||
      beginDate.getDate() != endDate.getDate()) {
    $result.append(labels.from);
    isReservationOnOneDay = false;
  } else {
    $result.append(labels.the);
  }

  // First date
  $result.append(' ').append($.datepicker.formatDate(filters.dateFormat, beginDate, null));
  if (isReservationOnOneDay) {
    $result.append(' ').append(labels.hourFrom);
  }

  // First hour
  $result.append(' ').append(beginDate.getHoursAndMinutes());

  // Second date
  if (isReservationOnOneDay) {
    $result.append(' ').append(labels.hourTo);
  } else {
    $result.append(' ').append(labels.to);
    $result.append(' ').append($.datepicker.formatDate(filters.dateFormat, endDate, null));
  }

  // Second hour
  $result.append(' ').append(endDate.getHoursAndMinutes());

  // Result
  return $result;
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
  goToLink(link, objectView, isPortlet);
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
  goToLink(link, objectView, isPortlet);
}

/**
 * Please be careful, this function is compatible with portlet behaviour.
 * @param link
 * @param objectView
 * @param isPortlet
 */
function goToLink(link, objectView, isPortlet) {
  if (isPortlet) {
    top.bottomFrame.MyMain.location.href = link;
  } else {
    location.href = link;
  }
}
