/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.almanach;

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.subscription.CalendarSubscriptionResource;
import org.silverpeas.core.web.calendar.AbstractCalendarWebController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.webcomponent.annotation.Homepage;
import org.silverpeas.core.web.mvc.webcomponent.annotation.LowestRoleAccess;
import org.silverpeas.core.web.mvc.webcomponent.annotation.NavigationStep;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectTo;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectToInternalJsp;
import org.silverpeas.core.web.mvc.webcomponent.annotation.WebComponentController;
import org.silverpeas.core.web.subscription.SubscriptionContext;
import org.silverpeas.core.webapi.calendar.CalendarEntity;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import static org.silverpeas.components.almanach.AlmanachSettings.getDefaultCalendarView;
import static org.silverpeas.components.almanach.AlmanachSettings.isCalendarWeekendVisible;
import static org.silverpeas.core.web.calendar.CalendarViewType.from;

@WebComponentController(AlmanachSettings.COMPONENT_NAME)
public class AlmanachWebController
    extends AbstractCalendarWebController<AlmanachWebRequestContext> {

  // Some navigation step identifier definitions
  private static final String EVENT_VIEW_NS_ID = "eventViewNavStepIdentifier";

  private AlmanachTimeWindowViewContext timeWindowViewContext;

  /**
   * Standard Session Controller Constructor
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   */
  public AlmanachWebController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, AlmanachSettings.MESSAGES_PATH,
        AlmanachSettings.ICONS_PATH, AlmanachSettings.SETTINGS_PATH);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected AlmanachTimeWindowViewContext getCalendarTimeWindowContext() {
    return timeWindowViewContext;
  }

  @Override
  protected void onInstantiation(final AlmanachWebRequestContext context) {
    final String instanceId = context.getComponentInstanceId();
    timeWindowViewContext =
        new AlmanachTimeWindowViewContext(instanceId, getLanguage(), getZoneId());
    timeWindowViewContext.setViewType(from(getDefaultCalendarView(instanceId)));
    timeWindowViewContext.setWithWeekend(isCalendarWeekendVisible(instanceId));
  }

  @Override
  protected void beforeRequestProcessing(final AlmanachWebRequestContext context) {
    super.beforeRequestProcessing(context);
    Calendar mainCalendar = context.getMainCalendar();
    context.getRequest().setAttribute("mainCalendar", CalendarEntity.fromCalendar(mainCalendar)
        .withURI(context.uri().ofCalendar(mainCalendar)));
    context.getRequest().setAttribute("timeWindowViewContext", timeWindowViewContext);
  }

  /**
   * Prepares the rendering of the home page.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("Main")
  @Homepage
  @RedirectToInternalJsp("almanach.jsp")
  public void home(AlmanachWebRequestContext context) {
    // Nothing to do
  }

  /**
   * Prepares the rendering of the home page.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("portlet")
  @RedirectToInternalJsp("almanachPortlet.jsp")
  public void portlet(AlmanachWebRequestContext context) {
    // Nothing to do
  }

  /**
   * Asks for managing subscriptions of a calendar. It redirects to the transversal subscription
   * management.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("calendars/{id}/subscriptions/manage")
  @RedirectTo("{destination}")
  @LowestRoleAccess(SilverpeasRole.ADMIN)
  public void manageCalendarSubscriptions(AlmanachWebRequestContext context) {
    final String calendarId = context.getPathVariables().get("id");
    final Calendar calendar = Calendar.getById(calendarId);
    final SubscriptionContext subscriptionContext = getSubscriptionContext();
    subscriptionContext.initialize(CalendarSubscriptionResource.from(calendar));
    context.addRedirectVariable("destination", subscriptionContext.getDestinationUrl());
  }

  /**
   * Asks for purposing a new event. It renders an HTML page to input the content of a new
   * event.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("calendars/events/new")
  @RedirectToInternalJsp("almanachOccurrenceEdit.jsp")
  @LowestRoleAccess(SilverpeasRole.PUBLISHER)
  public void newEvent(AlmanachWebRequestContext context) {
    processNewEvent(context);
  }

  /**
   * Asks for purposing a new event. It renders an HTML page to input the content of a new
   * event.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("calendars/occurrences/{occurrenceId}")
  @NavigationStep(identifier = EVENT_VIEW_NS_ID)
  @RedirectToInternalJsp("almanachOccurrenceView.jsp")
  public void viewOccurrence(AlmanachWebRequestContext context) {
    processViewOccurrence(context, EVENT_VIEW_NS_ID);
  }

  /**
   * Asks for purposing a new event. It renders an HTML page to input the content of a new
   * event.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("calendars/occurrences/{occurrenceId}/edit")
  @RedirectToInternalJsp("almanachOccurrenceEdit.jsp")
  @LowestRoleAccess(SilverpeasRole.PUBLISHER)
  public void editOccurrence(AlmanachWebRequestContext context) {
    viewOccurrence(context);
  }
}
