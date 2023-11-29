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
package org.silverpeas.components.almanach;

import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.Priority;
import org.silverpeas.core.calendar.Recurrence;
import org.silverpeas.core.date.TemporalConverter;
import org.silverpeas.core.date.TemporalConverter.Conversion;
import org.silverpeas.core.pdc.pdc.model.GlobalSilverResult;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.search.AbstractResultDisplayer;
import org.silverpeas.core.web.search.ResultDisplayer;
import org.silverpeas.core.web.search.SearchResultContentVO;

import javax.inject.Named;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Properties;

import static org.silverpeas.core.SilverpeasExceptionMessages.failureOnGetting;

/**
 * <pre>
 * This class implements a ResultDisplayer in order to customize search result display. It uses
 * "Named" annotation to inject dependency.
 * Be careful to not modify this name that uses the following rules : componentName + POSTFIX_BEAN_NAME
 * POSTFIX_BEAN_NAME = ResultDisplayer
 * </pre>
 */
@Named("almanachResultDisplayer")
public class ResultSearchRenderer extends AbstractResultDisplayer implements ResultDisplayer {

  private static final Properties templateConfig = new Properties();
  private static final String TEMPLATE_FILENAME = "event_result_template";

  static {
    SettingBundle settings =
        ResourceLocator.getSettingBundle("org.silverpeas.almanach.settings.almanachSettings");
    templateConfig.setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR, settings
        .getString("templatePath"));
    templateConfig.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR, settings
        .getString("customersTemplatePath"));
  }

  @Override
  public String getResultContent(SearchResultContentVO searchResult) {
    String result = "";
    // Retrieve the event detail from silverResult
    final GlobalSilverResult silverResult = searchResult.getGsr();
    final String id = silverResult.isLinked() ? silverResult.getLinkedResourceId() : silverResult.getId();
    final CalendarEvent event = CalendarEvent.getById(id);
    if (event != null) {
      // Create a SilverpeasTemplate
      final SilverpeasTemplate template = getNewTemplate();
      this.setCommonAttributes(searchResult, template);
      MultiSilverpeasBundle settings = searchResult.getSettings();
      setEventAttributes(event, template, settings);
      final String language = DisplayI18NHelper.getDefaultLanguage();
      result = template.applyFileTemplate(TEMPLATE_FILENAME + '_' + language);
    } else {
      SilverLogger.getLogger(this).warn(failureOnGetting("event", silverResult.getId()));
    }
    return result;
  }

  /**
   * Add event attributes to template given in parameter
   * @param event the current event
   * @param template the template object where we add data
   * @param settings the specific almanach resources wrapper object
   */
  private void setEventAttributes(CalendarEvent event, SilverpeasTemplate template,
      MultiSilverpeasBundle settings) {
    Conversion<LocalDate, Date> convertLocalDate =
        Conversion.of(LocalDate.class,
            d -> Date.from(d.atStartOfDay(event.getCalendar().getZoneId()).toInstant()));
    Conversion<OffsetDateTime, Date> convertOffsetDateTime =
        Conversion.of(OffsetDateTime.class,
            o -> Date.from(o.atZoneSameInstant(event.getCalendar().getZoneId()).toInstant()));

    Date startDate = TemporalConverter.applyByType(event.getStartDate(), convertLocalDate,
        convertOffsetDateTime);
    Date endDate =
        TemporalConverter.applyByType(event.getEndDate(), convertLocalDate, convertOffsetDateTime);
    template.setAttribute("evtStartDate", startDate);
    template.setAttribute("evtEndDate", endDate);
    String location = event.getLocation();
    if (StringUtil.isDefined(location)) {
      template.setAttribute("evtLocation", location);
    }

    if (Priority.HIGH == event.getPriority()) {
      template.setAttribute("evtPriority", settings.getString("prioriteImportante"));
    } else {
      template.setAttribute("evtPriority", settings.getString("prioriteNormale"));
    }
    final Recurrence recurrence = event.getRecurrence();
    final String strPeriod;
    if (recurrence == null) {
      strPeriod = settings.getString("noPeriodicity");
    } else {
      if (recurrence.getFrequency().isDaily()) {
        strPeriod = settings.getString("allDays");
      } else if (recurrence.getFrequency().isWeekly()) {
        strPeriod = settings.getString("allWeeks");
      } else if (recurrence.getFrequency().isMonthly()) {
        strPeriod = settings.getString("allMonths");
      } else {
        strPeriod = settings.getString("allYears");
      }
    }
    if (StringUtil.isDefined(strPeriod)) {
      template.setAttribute("evtPeriodicity", strPeriod);
    }

    event.getAttributes().get("externalUrl")
        .ifPresent(u -> template.setAttribute("evtURL", WebEncodeHelper.javaStringToHtmlString(u)));
  }

  /**
   * @return a new Silverpeas Template
   */
  private SilverpeasTemplate getNewTemplate() {
    return SilverpeasTemplateFactory.createSilverpeasTemplate(templateConfig);
  }
}
