/*
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
package org.silverpeas.components.almanach.workflowextensions;

import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.Priority;
import org.silverpeas.core.contribution.content.form.DataRecordUtil;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.model.Parameter;
import org.silverpeas.core.workflow.external.impl.ExternalActionImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.silverpeas.core.SilverpeasExceptionMessages.unknown;
import static org.silverpeas.core.util.StringUtil.getBooleanValue;

/**
 * The aim of this class is to provide a new workflow extension in order to create an almanach event
 * from the process manager application. You must fill the following mandatory trigger parameters :
 * <ul>
 * <li>almanachId</li>
 * <li>name</li>
 * <li>startDate</li>
 * </ul>
 * To get more information you can also fill other trigger parameters : description, startHour,
 * endDate, endHour, place, link, priority<br>
 * Watch {@link AlmanachTriggerParam} to get more trigger parameter information
 * @author ebonnet
 */
@Named("SendInAlmanachHandler")
public class SendInAlmanach extends ExternalActionImpl {

  private String role = "unknown";
  private static final String ADMIN_ID = "0";
  private static final DateTimeFormatter DATE_TIME_FORMATTER = ofPattern("dd/MM/yyyy hh:mm");
  private static final DateTimeFormatter DATE_FORMATTER = ofPattern("dd/MM/yyyy");

  @Inject
  private OrganizationController organizationController;

  @Override
  public void execute() {
    setRole(getEvent().getUserRoleName());

    final Parameter almanachParam =
        getTriggerParameter(AlmanachTriggerParam.APPLICATION_ID.getParameterName());

    final String componentInstanceId = almanachParam.getValue();
    final Period eventPeriod = getEventPeriod();

    // Check workflow export target parameter is valid
    if (isMandatoryTriggerParamValid(almanachParam, eventPeriod)) {

      // Getting almanach main calendar
      final Calendar almanach =
          Calendar.getByComponentInstanceId(componentInstanceId).getMainCalendar().orElseThrow(
              () -> new IllegalStateException(unknown("main calendar on", componentInstanceId)));

      // Set event detail data
      CalendarEvent event = CalendarEvent.on(eventPeriod)
          .withTitle(getFolderValueFromTriggerParam(AlmanachTriggerParam.EVENT_NAME))
          .withDescription(getFolderValueFromTriggerParam(AlmanachTriggerParam.EVENT_DESCRIPTION));
      event.setLocation(getFolderValueFromTriggerParam(AlmanachTriggerParam.PLACE));
      event.getAttributes()
          .set("externalUrl", getFolderValueFromTriggerParam(AlmanachTriggerParam.EVENT_URL));
      boolean priority =
          getBooleanValue(getFolderValueFromTriggerParam(AlmanachTriggerParam.PRIORITY));
      if (priority) {
        event.withPriority(Priority.HIGH);
      }

      event.planOn(almanach);
    } else {
      StringBuilder warnMsg = new StringBuilder();
      warnMsg.append("Workflow export event problem :");
      if (!StringUtil.isDefined(componentInstanceId) ||
          !getOrganizationController().isComponentExist(componentInstanceId)) {
        warnMsg.append("You must set a correct trigger parameter tp_almanachId.");
      }
      if (eventPeriod == null) {
        warnMsg.append("You must set a correct trigger parameter tp_startDate.");
      }
      SilverLogger.getLogger(this).warn(warnMsg.toString());
    }
  }

  /**
   * Check if almanach target application and event start date exist
   * @param almanachParam the almanach target application trigger parameter
   * @param eventPeriod the event period
   * @return true if all mandatory field exist
   */
  private boolean isMandatoryTriggerParamValid(Parameter almanachParam, Period eventPeriod) {
    return almanachParam != null && StringUtil.isDefined(almanachParam.getValue()) &&
        getOrganizationController().isComponentExist(almanachParam.getValue()) &&
        eventPeriod != null;
  }

  private Period getEventPeriod() {
    final String startDayValue = getFolderValueFromTriggerParam(AlmanachTriggerParam.START_DATE);
    final String endDayValue = getFolderValueFromTriggerParam(AlmanachTriggerParam.END_DATE);
    if (startDayValue != null) {
      final String startHourValue = getFolderValueFromTriggerParam(AlmanachTriggerParam.START_HOUR);
      final String endHourValue = getFolderValueFromTriggerParam(AlmanachTriggerParam.END_HOUR);
      try {
        final Temporal start;
        if (StringUtil.isValidHour(startHourValue)) {
          start = OffsetDateTime.parse(startDayValue + " " + startHourValue, DATE_TIME_FORMATTER);
        } else {
          start = LocalDate.parse(startDayValue, DATE_FORMATTER);
        }
        final Temporal end;
        if (endDayValue != null) {
          if (StringUtil.isValidHour(endHourValue)) {
            end = OffsetDateTime.parse(endDayValue + " " + endHourValue, DATE_TIME_FORMATTER);
          } else {
            end = LocalDate.parse(endDayValue, DATE_FORMATTER).plusDays(1);
          }
        } else {
          if (StringUtil.isValidHour(endHourValue)) {
            end = OffsetDateTime.parse(startDayValue + " " + endHourValue, DATE_TIME_FORMATTER);
          } else {
            end = start.plus(1, ChronoUnit.DAYS);
          }
        }
        return Period.between(start, end);
      } catch (DateTimeParseException e) {
        SilverLogger.getLogger(this).warn(e);
      }
    }
    return null;
  }

  private String getFolderValueFromTriggerParam(AlmanachTriggerParam almanachParam) {
    return evaluateFolderValues(retrieveTriggerParamValue(almanachParam));
  }

  private String retrieveTriggerParamValue(AlmanachTriggerParam almanachParam) {
    String triggerParamValue = StringUtil.EMPTY;
    Parameter triggerParam = getTriggerParameter(almanachParam.getParameterName());
    if (triggerParam != null) {
      triggerParamValue = triggerParam.getValue();
    }
    return triggerParamValue;
  }

  /**
   * @param triggerParamValue the trigger parameter value
   * @return the translated string if ${folder.XXX} has been evaluated successfully, the
   * triggerParamValue else if.
   */
  private String evaluateFolderValues(String triggerParamValue) {
    String evaluateValue = triggerParamValue;
    if (StringUtil.isDefined(triggerParamValue)) {
      try {
        evaluateValue =
            DataRecordUtil.applySubstitution(triggerParamValue, getProcessInstance()
                .getAllDataRecord(getRole(), "fr"), "fr");
      } catch (WorkflowException e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
    return evaluateValue;
  }

  private String getRole() {
    return role;
  }

  private void setRole(String role) {
    this.role = role;
  }

  private OrganizationController getOrganizationController() {
    return organizationController;
  }
}