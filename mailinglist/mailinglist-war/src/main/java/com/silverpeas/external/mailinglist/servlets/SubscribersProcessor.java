/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.external.mailinglist.servlets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.mailinglist.control.MailingListSessionController;
import com.silverpeas.mailinglist.service.ServicesFactory;
import com.silverpeas.mailinglist.service.model.MailingListService;
import com.silverpeas.mailinglist.service.model.beans.InternalSubscriber;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

public class SubscribersProcessor implements MailingListRoutage {

  public static String processSubscription(RestRequest rest,
      HttpServletRequest request, ComponentSessionController componentSC) {
    MailingList mailingList = ServicesFactory.getMailingListService()
        .findMailingList(rest.getComponentId());
    MailingListSessionController controller = (MailingListSessionController) componentSC;
    Selection selection = controller.getSelection();
    switch (rest.getAction()) {
      case RestRequest.DELETE:
        ServicesFactory.getMailingListService().unsubscribe(
            rest.getComponentId(), componentSC.getUserId());
        return request.getScheme() + "://" + request.getServerName() + ':'
            + request.getServerPort() + request.getContextPath()
            + request.getServletPath() + '/' + rest.getComponentId() + '/'
            + DESTINATION_ACTIVITIES + '/' + rest.getComponentId();
      case RestRequest.FIND:
        ServicesFactory.getMailingListService().subscribe(
            rest.getComponentId(), componentSC.getUserId());
        return request.getScheme() + "://" + request.getServerName() + ':'
            + request.getServerPort() + request.getContextPath()
            + request.getServletPath() + '/' + rest.getComponentId() + '/'
            + DESTINATION_ACTIVITIES + '/' + rest.getComponentId();
      case RestRequest.UPDATE:
        prepareSelection(selection, controller, mailingList, rest
            .getComponentId());
        return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
      case RestRequest.CREATE:
      default:
        List<String> userIds = Arrays.asList(selection.getSelectedElements());
        List<String> groupIds = Arrays.asList(selection.getSelectedSets());
        ServicesFactory.getMailingListService().setInternalSubscribers(
            rest.getComponentId(), userIds);
        ServicesFactory.getMailingListService().setGroupSubscribers(
            rest.getComponentId(), groupIds);
        selection.resetAll();
        return request.getScheme() + "://" + request.getServerName() + ':'
            + request.getServerPort() + request.getContextPath()
            + request.getServletPath() + '/' + rest.getComponentId() + '/'
            + DESTINATION_ACTIVITIES + '/' + rest.getComponentId();
    }
  }

  private static void prepareSelection(Selection selection,
      MailingListSessionController controller, MailingList mailingList,
      String componentId) {
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");
    String hostSpaceName = controller.getSpaceLabel();
    PairObject hostComponentName = new PairObject(controller
        .getComponentLabel(), m_context + "/Rmailinglist/" + componentId
        + "/activity/" + componentId);
    String hostUrl = m_context + "/Rmailinglist/" + componentId + '/'
        + DESTINATION_SUBSCRIBERS + '/' + componentId;
    selection.resetAll();
    selection.setHostSpaceName(hostSpaceName);
    selection.setHostComponentName(hostComponentName);
    selection.setHostPath(null);

    selection.setGoBackURL(hostUrl);
    selection.setCancelURL(hostUrl);
    // Contraintes
    selection.setMultiSelect(true);
    selection.setPopupMode(false);
    SelectionUsersGroups extraParams = new SelectionUsersGroups();
    extraParams.setComponentId(componentId);
    ArrayList profiles = new ArrayList(1);
    profiles.add(MailingListService.ROLE_READER);
    extraParams.setProfileNames(profiles);
    selection.setExtraParams(extraParams);
    String[] groups = convertInternalSubscribers(mailingList
        .getGroupSubscribers());
    String[] users = convertInternalSubscribers(mailingList
        .getInternalSubscribers());
    selection.setSelectedElements(users);
    selection.setSelectedSets(groups);
    if (users.length == 0 || groups.length == 0) {
      selection.setFirstPage(Selection.FIRST_PAGE_BROWSE);
    } else {
      selection.setFirstPage(Selection.FIRST_PAGE_CART);
    }
  }

  private static String[] convertInternalSubscribers(
      Collection<? extends InternalSubscriber> subscribers) {
    Set<String> result = new HashSet<String>(subscribers.size());
    for (InternalSubscriber subscriber : subscribers) {
      if (subscriber.getExternalId() != null) {
        result.add(subscriber.getExternalId());
      }
    }
    return (String[]) result.toArray(new String[result.size()]);
  }
}