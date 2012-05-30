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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.classifieds.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.webactiv.beans.admin.OrganizationController;

/**
 * @author Yohann Chastagnier
 */
public class ClassifiedSupervisorUserNotification extends AbstractClassifiedUserNotification {

  public ClassifiedSupervisorUserNotification(final ClassifiedDetail resource) {
    super(resource, null, "tovalidate");
  }

  @Override
  protected Collection<String> getUserIdToNotify() {
    final List<String> roles = Collections.singletonList("admin");
    final OrganizationController orga = new OrganizationController();
    return new ArrayList<String>(Arrays.asList(orga.getUsersIdsByRoleNames(getComponentInstanceId(), roles)));
  }

  @Override
  protected String getSubjectKey() {
    return "classifieds.supervisorNotifSubject";
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.PENDING_VALIDATION;
  }

  @Override
  protected boolean isSendImmediatly() {
    return true;
  }
}
