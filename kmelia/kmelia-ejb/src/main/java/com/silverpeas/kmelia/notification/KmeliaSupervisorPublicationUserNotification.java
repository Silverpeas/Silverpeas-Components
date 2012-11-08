/*
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
package com.silverpeas.kmelia.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

/**
 * @author Yohann Chastagnier
 */
public class KmeliaSupervisorPublicationUserNotification extends KmeliaSubscriptionPublicationUserNotification {

  public KmeliaSupervisorPublicationUserNotification(final NodePK nodePK, final PublicationDetail resource) {
    super(nodePK, resource, NotifAction.CREATE);
  }

  @Override
  protected String getBundleSubjectKey() {
    return "kmelia.SupervisorNotifSubject";
  }

  @Override
  protected String getFileName() {
    return "notificationSupervisor";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    final List<String> roles = Collections.singletonList("supervisor");
    final List<String> supervisors =
        new ArrayList<String>(Arrays.asList(getOrganizationController().getUsersIdsByRoleNames(
            getResource().getPK().getInstanceId(), roles)));
    SilverTrace.debug("kmelia", "KmeliaSupervisorPublicationNotification.getUserIdToNotify()",
        "root.MSG_GEN_PARAM_VALUE", supervisors.size() + " users in role supervisor !");
    return supervisors;
  }

  @Override
  protected String getSender() {
    return getResource().getUpdaterId();
  }

  @Override
  protected boolean isSendImmediatly() {
    return true;
  }
}
