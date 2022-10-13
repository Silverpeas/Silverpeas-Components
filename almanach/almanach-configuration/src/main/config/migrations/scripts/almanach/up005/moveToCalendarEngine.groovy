/**
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import java.sql.SQLException
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * This script imports all the Almanach events into the new Silverpeas Calendar Engine.
 * If a calendar for an existing Almanach instance cannot be created, the importation stops and
 * throws a SQL exception.
 * If an event from a given Almanach instance cannot be imported, the importation stops and
 * throws a SQL exception.
 */

/* The format of the local datetime read from an existing almanach event */
final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern('yyyy/MM/dd HH:mm')
/* The format of the local date read from an existing almanach event */
final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern('yyyy/MM/dd')

/* The default timezone to use with all the Almanachs' calendars */
Properties almanachSettings = new Properties()
almanachSettings.load(new FileInputStream(
    "${settings.SILVERPEAS_HOME}/properties/org/silverpeas/almanach/settings/almanachSettings.properties"))
final String timeZone = almanachSettings['almanach.timezone']

/* time units to use in the conversion of the periodicity between Almanach and the Calendar Egine */
final def timeUnit = [null, 'DAY', 'WEEK', 'MONTH', 'YEAR']

/* The SQL statements to migrate existing almanach events into the new Silverpeas Calendar Engine */
final def insertCalendar = """
  INSERT INTO SB_Cal_Calendar
    (id,            
    instanceId,    
    title,
    zoneId,        
    createDate,    
    createdBy,     
    lastUpdateDate,
    lastUpdatedBy, 
    version)
  VALUES
    (:id, :instanceId, '###main###', '${timeZone}', :createDate, :creator, :updateDate, :updater, 0)       
"""

final def insertExternalUrl = '''
  INSERT INTO SB_Cal_Attributes
    (id,
    name,
    value)
  VALUES
    (:id, 'externalUrl', :value)
'''

final def insertEventComponent = '''
  INSERT INTO SB_Cal_Components
    (id,
    calendarId,
    startDate,
    endDate,
    inDays,
    title,
    description,
    location,
    priority,
    createDate,
    createdBy,
    lastUpdateDate,
    lastUpdatedBy,
    version)
  VALUES
    (:id, :calendarId, :startDate, :endDate, :inDays, :title, :description, :location, :priority, :createDate, :creator, :createDate, :creator, 0)
'''

final def insertEventRecurrence = '''
  INSERT INTO SB_Cal_Recurrence
    (id,
    recur_periodInterval,
    recur_periodUnit,
    recur_count,
    recur_endDate)
  VALUES
    (:id, :interval, :unit, 0, :endDate)
'''

final def insertEventRecurrenceDayOfWeek = '''
  INSERT INTO SB_Cal_Recurrence_DayOfWeek
    (recurrenceId,
    recur_nth,
    recur_dayOfWeek)
  VALUES
    (:id, :nth, :dayOfWeek)
'''

final def insertEventRecurrenceException = '''
  INSERT INTO SB_Cal_Recurrence_Exception
    (recurrenceId,
    recur_exceptionDate)
  VALUES
    (:id, :date)
'''

final def insertEvent = '''
  INSERT INTO SB_Cal_Event
    (id,
    componentId,
    visibility,
    recurrenceId)
  VALUES
    (:id, :componentId, 'PUBLIC', :recurrenceId)
'''

// converts a local date and time into an OffsetDateTime value expressed in UTC.
def toUTCDateTime = { final String date, final String time ->
  if (time) {
    return LocalDateTime.parse(date + ' ' + time, DATETIME_FORMAT)
        .atZone(ZoneId.of(timeZone))
        .toOffsetDateTime().withOffsetSameInstant(ZoneOffset.UTC)
  } else {
    return LocalDate.parse(date, DATE_FORMAT)
        .atStartOfDay()
        .atOffset(ZoneOffset.UTC)
  }
}

// converts the specified datetime expressed in UTC into a SQL timestamp value without time zone set
// If the specified OffsetDateTime value is null then returns null
def toTimestamp = { final OffsetDateTime dateTime ->
  return dateTime == null ? null : Timestamp.valueOf(dateTime.toLocalDateTime())
}

/*
 * Now we load each existing almanach events to copy them into the Silverpeas Calendar Engine. We
 *  don't remove these events to avoid any data lost if this migration fails or for whatever other
 *  reason.
 */

// first we load all the existing almanachs in order to create their respective main calendar in
// the Silverpeas Calendar Engine
log.info 'Importation of the existing Almanach application\'s calendar into the Silverpeas Calendar Engine...'

long migratedAlmanachCount = 0
List existingAlmanachs = []
def almanachs = sql.rows '''
  SELECT id,
    name,
    createdBy,
    createTime,
    updatedBy,
    updateTime 
    FROM st_componentinstance WHERE componentname = 'almanach' ORDER BY id
'''
almanachs.each { almanach ->
  def currentAlmanach = [instanceId: 'almanach' + almanach.id,
                         calendarId: UUID.randomUUID().toString()]
  existingAlmanachs << currentAlmanach

  Instant creationInstant = almanach.createTime ?
      Instant.ofEpochMilli(almanach.createTime as Long) : Instant.now()
  Timestamp createDate = Timestamp.from(creationInstant)
  String creator = almanach.createdBy ? almanach.createdBy as String : '0'

  try {
    sql.execute insertCalendar,
        id: currentAlmanach.calendarId,
        instanceId: currentAlmanach.instanceId,
        createDate: createDate,
        creator: creator,
        updateDate: almanach.updateTime ?
            Timestamp.from(Instant.ofEpochMilli(almanach.updateTime as Long)) : createDate,
        updater: almanach.updatedBy ? almanach.updatedBy as String : creator

    migratedAlmanachCount++
  } catch (Exception e) {
    throw new SQLException("Cannot create calendar for Almanach ${currentAlmanach.instanceId}", e)
  }
}

log.info "=> Number of imported Almanachs: ${migratedAlmanachCount}"

if (migratedAlmanachCount == 0)
  return 0

// finally we import all the events into the previously created calendar in the Silverpeas Calendar
// Engine
log.info 'Importation of events from all the existing Almanach\'s calendars into the Silverpeas Calendar Engine...'
def events = sql.rows '''
  SELECT eventId,
    eventName,
    eventStartDay,
    eventEndDay,
    eventDelegatorId,
    eventPriority,
    eventTitle,
    instanceId,
    eventStartHour,
    eventEndHour,
    eventPlace,
    eventUrl
    FROM SC_Almanach_Event
    WHERE instanceId in (''' + existingAlmanachs.collect {'\'' + it.instanceId + '\'' }.join(',') +
    ') ORDER BY instanceId, eventId'

migratedAlmanachCount = 0
long migratedEventCount = 0
long currentMigratedEventCount = 0
int idx = events.isEmpty() ? 0 : existingAlmanachs.findIndexOf { it.instanceId == events[0].instanceId }
events.each { event ->
  // next almanach? if true, reset some counters
  if (event.instanceId != existingAlmanachs[idx].instanceId) {
    log.info "=> Number of imported events from ${existingAlmanachs[idx].instanceId}: ${currentMigratedEventCount}"
    currentMigratedEventCount = 0
    migratedAlmanachCount += 1
    idx = existingAlmanachs.findIndexOf(idx) { it.instanceId == event.instanceId }
  }

  // import the event itself
  try {
    /* compute the start and end date time of the event to import in the expected format and by
       taking into account the implicit rules of Almanach in event dates */
    boolean inDays = !(event.eventStartHour && event.eventEndHour)
    OffsetDateTime startDate = toUTCDateTime(event.eventStartDay,
        (inDays ? '' : (event.eventStartHour ? event.eventStartHour : '00 00')))
    OffsetDateTime endDate
    if (!event.eventEndDay || event.eventStartDay == event.eventEndDay) {
      if (event.eventEndHour && event.eventEndHour != event.eventStartHour) {
        endDate = toUTCDateTime(event.eventStartDay, event.eventEndHour)
      } else if (event.eventStartHour) {
        endDate = startDate.plusHours(1)
      } else {
        endDate = startDate.plusDays(1)
      }
    } else {
      if (event.eventEndHour) {
        endDate = toUTCDateTime(event.eventEndDay, event.eventEndHour)
      } else if (event.eventStartHour) {
        endDate = toUTCDateTime(event.eventEndDay, event.eventStartHour)
      } else {
        endDate = toUTCDateTime(event.eventEndDay, '').plusDays(1)
      }
    }

    /* first, we import the event properties as a calendar component into the current calendar */
    def componentId = 'event-' + event.eventId
    sql.execute insertEventComponent,
        id: componentId,
        calendarId: existingAlmanachs[idx].calendarId,
        startDate: toTimestamp(startDate),
        endDate: toTimestamp(endDate),
        inDays: inDays,
        title: (event.eventTitle ? event.eventTitle : ''),
        description: (event.eventName ? event.eventName : ''),
        location: event.eventPlace,
        priority: event.eventPriority,
        creator: event.eventDelegatorId,
        createDate: toTimestamp(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC))

    /* second, we set the event external URL as an attribute of the calendar component */
    if (event.eventUrl) {
      sql.execute insertExternalUrl, id: event.eventId as String, value: event.eventUrl
    }

    /* third, we import the recurrence rule of the event if any */
    def periodicity = sql.firstRow """
      SELECT id, unity, frequency, daysWeekBinary, numWeek, day, untilDatePeriod 
      FROM SC_Almanach_Periodicity WHERE eventId = ${event.eventId}
    """
    String periodicityId = null
    if (periodicity) {
      periodicityId = periodicity.id as String
      OffsetDateTime endRecurDate = null
      if (periodicity.untilDatePeriod) {
        endRecurDate =
            toUTCDateTime(periodicity.untilDatePeriod, (event.eventStartHour ? event.eventStartHour : '00:00'))
      }

      sql.execute insertEventRecurrence, id: periodicityId, interval: periodicity.frequency,
          unit: timeUnit[periodicity.unity], endDate: toTimestamp(endRecurDate)

      if (timeUnit[periodicity.unity] == 'WEEK' && periodicity.daysWeekBinary) {
        periodicity.daysWeekBinary.eachWithIndex { isSet, day ->
          if (isSet == '1') {
            sql.execute insertEventRecurrenceDayOfWeek, id: periodicityId, nth: periodicity.numWeek,
                dayOfWeek: day
          }
        }
      } else if (timeUnit[periodicity.unity] == 'MONTH' && periodicity.numWeek && periodicity.day) {
        sql.execute insertEventRecurrenceDayOfWeek, id: periodicityId,
            dayOfWeek: periodicity.day == 1 ? 6 : periodicity.day - 2,
            nth: periodicity.numWeek == -1 ? -1 : periodicity.numWeek
      }

      def exceptions = sql.rows """
        SELECT id, beginDateException, endDateException 
        FROM SC_Almanach_PeriodicityExcept WHERE periodicityId = ${periodicityId as int}
      """

      if (exceptions) {
        exceptions.each { exception ->
          OffsetDateTime exDate =
              toUTCDateTime(exception.beginDateException, (event.eventStartHour ? event.eventStartHour : '00:00'))
          sql.execute insertEventRecurrenceException, id: periodicityId, date: toTimestamp(exDate)
        }
      }
    }

    /* finally we link the calendar component and the recurrence rule as an event in the
       Silverpeas Calendar Engine */
    sql.execute insertEvent, id: event.eventId as String, componentId: componentId,
        recurrenceId: periodicityId

    currentMigratedEventCount++
    migratedEventCount++
  } catch (Exception e) {
    throw new SQLException("[${existingAlmanachs[idx].instanceId}] Cannot import event ${event.eventId}", e)
  }
}

if (idx < existingAlmanachs.size()) {
  log.info "=> Number of imported events from ${existingAlmanachs[idx].instanceId}: ${currentMigratedEventCount}"
}

log.info "=> Total number of imported events: ${migratedEventCount} from ${migratedAlmanachCount} almanachs"



