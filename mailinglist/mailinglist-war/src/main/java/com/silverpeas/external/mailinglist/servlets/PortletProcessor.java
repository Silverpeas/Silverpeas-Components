package com.silverpeas.external.mailinglist.servlets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.mailinglist.service.ServicesFactory;
import com.silverpeas.mailinglist.service.model.beans.Activity;
import com.silverpeas.mailinglist.service.model.beans.InternalUserSubscriber;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.silverpeas.mailinglist.service.model.beans.MailingListActivity;

public class PortletProcessor implements MailingListRoutage {

  public static String processActivities(RestRequest rest,
      HttpServletRequest request, String userId) {
    switch (rest.getAction()) {
      case RestRequest.DELETE:
      case RestRequest.UPDATE:
      case RestRequest.FIND:
      default:
        MailingList list = ServicesFactory.getMailingListService()
            .findMailingList(rest.getComponentId());
        MailingListActivity mailingListActivity = ServicesFactory
            .getMessageService().getActivity(list);
        Map years = new HashMap(10);
        Iterator iter = mailingListActivity.getActivities().iterator();
        while (iter.hasNext()) {
          Activity activity = (Activity) iter.next();
          Map month = (Map) years.get("" + activity.getYear());
          if (month == null) {
            month = new HashMap(12);
            years.put("" + activity.getYear(), month);
          }
          month.put("" + activity.getMonth(), "" + activity.getNbMessages());
        }
        if (!list.isSupportRSS()) {
          request.removeAttribute(RSS_URL_ATT);
        }
        request.setAttribute(IS_USER_SUBSCRIBER_ATT, new Boolean(isSubscriber(
            list, userId)));
        request.setAttribute(ACTIVITY_LIST_ATT, mailingListActivity);
        request.setAttribute(ACTIVITY_MAP_ATT, years);
        List yearsList = new ArrayList();
        yearsList.addAll(years.keySet());
        Collections.sort(yearsList);
        request.setAttribute(ACTIVITY_YEARS_ATT, yearsList);
        request.setAttribute(MAILING_LIST_ATT, list);
        return JSP_BASE + DESTINATION_DISPLAY_PORTLET;
    }
  }

  private static boolean isSubscriber(MailingList list, String userId) {
    Collection subscribers = list.getInternalSubscribers();
    if (subscribers != null && !subscribers.isEmpty()) {
      Iterator iter = subscribers.iterator();
      while (iter.hasNext()) {
        InternalUserSubscriber user = (InternalUserSubscriber) iter.next();
        if (userId.equals(user.getExternalId())) {
          return true;
        }
      }
    }
    return false;
  }
}
