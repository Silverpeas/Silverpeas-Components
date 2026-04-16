/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.mailinglist.servlets;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import org.silverpeas.components.mailinglist.control.MailingListSessionController;
import org.silverpeas.components.mailinglist.service.model.MailingListService;
import org.silverpeas.components.mailinglist.service.model.beans.InternalSubscriber;
import org.silverpeas.components.mailinglist.service.model.beans.MailingList;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.web.mvc.controller.ComponentSessionController;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.selection.SelectionUsersGroups;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.util.Pair;

import java.util.*;

import static org.silverpeas.components.mailinglist.servlets.MailingListRoutageProperties.*;

@Bean
public class SubscribersProcessor {

  @Inject
  private MailingListService service;
  
  public String processSubscription(RestRequest rest, HttpServletRequest request,
      ComponentSessionController componentSC) {
    MailingList mailingList = service.findMailingList(rest.getComponentId());
    MailingListSessionController controller = (MailingListSessionController) componentSC;
    Selection selection = controller.getSelection();
    switch (rest.getAction()) {
      case RestRequest.DELETE:
        service.unsubscribe(rest.getComponentId(), componentSC.getUserId());
        return request.getScheme() + "://" + request.getServerName() + ':' +
            request.getServerPort() + request.getContextPath() + request.getServletPath() + '/' +
            rest.getComponentId() + '/' + DESTINATION_ACTIVITIES + '/' + rest.getComponentId();
      case RestRequest.FIND:
        service.subscribe(rest.getComponentId(), componentSC.getUserId());
        return request.getScheme() + "://" + request.getServerName() + ':' +
            request.getServerPort() + request.getContextPath() + request.getServletPath() + '/' +
            rest.getComponentId() + '/' + DESTINATION_ACTIVITIES + '/' + rest.getComponentId();
      case RestRequest.UPDATE:
        prepareSelection(selection, controller, mailingList, rest.getComponentId());
        return Selection.getSelectionURL();
      case RestRequest.CREATE:
      default:
        List<String> userIds = Arrays.asList(selection.getSelectedElements());
        List<String> groupIds = Arrays.asList(selection.getSelectedSets());
        service.setInternalSubscribers(rest.getComponentId(), userIds);
        service.setGroupSubscribers(rest.getComponentId(), groupIds);
        selection.resetAll();
        return request.getScheme() + "://" + request.getServerName() + ':' +
            request.getServerPort() + request.getContextPath() + request.getServletPath() + '/' +
            rest.getComponentId() + '/' + DESTINATION_ACTIVITIES + '/' + rest.getComponentId();
    }
  }

  private static void prepareSelection(Selection selection, MailingListSessionController controller,
      MailingList mailingList, String componentId) {
    String webContext = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
    String hostSpaceName = controller.getSpaceLabel();
    Pair<String, String> hostComponentName = new Pair<>(controller.getComponentLabel(),
        webContext + "/Rmailinglist/" + componentId + "/activity/" + componentId);
    String hostUrl =
        webContext + "/Rmailinglist/" + componentId + '/' + DESTINATION_SUBSCRIBERS + '/' +
            componentId;
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
    List<String> profiles = new ArrayList<>(1);
    profiles.add(MailingListService.ROLE_READER);
    extraParams.setProfileNames(profiles);
    selection.setExtraParams(extraParams);
    String[] groups = convertInternalSubscribers(mailingList.getGroupSubscribers());
    String[] users = convertInternalSubscribers(mailingList.getInternalSubscribers());
    selection.setSelectedElements(users);
    selection.setSelectedSets(groups);
  }

  private static String[] convertInternalSubscribers(
      Collection<? extends InternalSubscriber> subscribers) {
    Set<String> result = new HashSet<>(subscribers.size());
    for (InternalSubscriber subscriber : subscribers) {
      if (subscriber.getExternalId() != null) {
        result.add(subscriber.getExternalId());
      }
    }
    return result.toArray(new String[0]);
  }
}
