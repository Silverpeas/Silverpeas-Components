/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.external.mailinglist.servlets;

import com.silverpeas.mailinglist.service.MailingListServicesProvider;
import com.silverpeas.mailinglist.service.model.beans.Activity;
import com.silverpeas.mailinglist.service.model.beans.InternalUserSubscriber;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.silverpeas.mailinglist.service.model.beans.MailingListActivity;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivitiesProcessor implements MailingListRoutage {

  public static String processActivities(RestRequest rest, HttpServletRequest request,
      String userId) {
    switch (rest.getAction()) {
      case RestRequest.DELETE:
      case RestRequest.UPDATE:
      case RestRequest.FIND:
      default:
        MailingList list = MailingListServicesProvider.getMailingListService()
            .findMailingList(rest.getComponentId());
        MailingListActivity mailingListActivity =
            MailingListServicesProvider.getMessageService().getActivity(list);
        Map<String, Map<String, String>> years = new HashMap<>(10);
        for (final Activity activity : mailingListActivity.getActivities()) {
          Map<String, String> month = years.get("" + activity.getYear());
          if (month == null) {
            month = new HashMap<>(12);
            years.put("" + activity.getYear(), month);
          }
          month.put("" + activity.getMonth(), "" + activity.getNbMessages());
        }
        if (!list.isSupportRSS()) {
          request.removeAttribute(RSS_URL_ATT);
        }
        request.setAttribute(IS_USER_SUBSCRIBER_ATT, isSubscriber(list, userId));
        request.setAttribute(ACTIVITY_LIST_ATT, mailingListActivity);
        request.setAttribute(ACTIVITY_MAP_ATT, years);
        List<String> yearsList = new ArrayList<>();
        yearsList.addAll(years.keySet());
        Collections.sort(yearsList);
        request.setAttribute(ACTIVITY_YEARS_ATT, yearsList);
        request.setAttribute(MAILING_LIST_ATT, list);
        return JSP_BASE + DESTINATION_DISPLAY_ACTIVITY;
    }
  }

  private static boolean isSubscriber(MailingList list, String userId) {
    Collection<InternalUserSubscriber> subscribers = list.getInternalSubscribers();
    if (subscribers != null && !subscribers.isEmpty()) {
      for (InternalUserSubscriber user : subscribers) {
        if (userId.equals(user.getExternalId())) {
          return true;
        }
      }
    }
    return false;
  }
}
