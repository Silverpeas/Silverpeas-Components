/*
 * Copyright (C) 2000 - 2020 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.components.kmelia.notification;

import org.silverpeas.core.admin.ProfiledObjectId;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.notification.user.client.constant.NotifAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Yohann Chastagnier
 */
public class KmeliaTopicUserNotification extends AbstractKmeliaFolderUserNotification {

  private final String alertType;

  public KmeliaTopicUserNotification(final NodeDetail node, NotifAction action,
      final String alertType) {
    super(node, action);
    this.alertType = alertType;
  }

  @Override
  protected String getBundleSubjectKey() {
    if (NotifAction.CREATE.equals(getAction())) {
      return "kmelia.notif.subject.folder.create";
    }
    return "kmelia.notif.subject.folder.update";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    boolean haveRights = getResource().haveRights();
    int rightsDependOn = getResource().getRightsDependsOn();
    NodeDetail fatherDetail = getNodeHeader(getResource().getFatherPK());
    if (fatherDetail != null) {
      // Case of creation only
      haveRights = fatherDetail.haveRights();
      rightsDependOn = fatherDetail.getRightsDependsOn();
    }

    final String[] users;
    if (!haveRights) {
      users = getEitherAllOrAdmins();
    } else {
      users = getUsersWithModificationRights(rightsDependOn);
    }

    if (users == null) {
      return Collections.emptyList();
    }
    return Arrays.asList(users);
  }

  private String[] getUsersWithModificationRights(final int rightsDependOn) {
    final String[] users;
    final List<String> profileNames = new ArrayList<>();
    profileNames.add("admin");
    profileNames.add("publisher");
    profileNames.add("writer");

    if (alertType.equals("All")) {
      profileNames.add("user");
      users =
          getOrganisationController().getUsersIdsByRoleNames(getComponentInstanceId(),
              ProfiledObjectId.fromNode(rightsDependOn), profileNames);
    } else if (alertType.equals("Publisher")) {
      users =
          getOrganisationController().getUsersIdsByRoleNames(getComponentInstanceId(),
              ProfiledObjectId.fromNode(rightsDependOn), profileNames);
    } else {
      users = null;
    }
    return users;
  }

  private String[] getEitherAllOrAdmins() {
    final String[] users;
    if ("All".equals(alertType)) {
      final UserDetail[] userDetails = getOrganisationController().getAllUsers(getComponentInstanceId());
      if (userDetails != null) {
        users = new String[userDetails.length];
        int i = 0;
        for (final UserDetail userDetail : userDetails) {
          users[i++] = userDetail.getId();
        }
      } else {
        users = null;
      }
    } else if ("Publisher".equals(alertType)) {
      // Get the list of all publishers and admin
      final List<String> profileNames = new ArrayList<>();
      profileNames.add("admin");
      profileNames.add("publisher");
      profileNames.add("writer");
      users = getOrganisationController().getUsersIdsByRoleNames(getComponentInstanceId(), profileNames);
    } else {
      users = null;
    }
    return users;
  }

  protected String getSenderName() {
    return User.getById(getSender()).getDisplayedName();
  }

  @Override
  protected String getTemplateFileName() {
    if (NotifAction.CREATE.equals(getAction())) {
      return "notificationCreateTopic";
    }
    return "notificationUpdateTopic";
  }

}