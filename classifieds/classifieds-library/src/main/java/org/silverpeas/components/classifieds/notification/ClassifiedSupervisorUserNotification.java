/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.components.classifieds.notification;

import org.silverpeas.components.classifieds.model.ClassifiedDetail;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Yohann Chastagnier
 */
public class ClassifiedSupervisorUserNotification extends AbstractClassifiedUserNotification {

  public ClassifiedSupervisorUserNotification(final ClassifiedDetail resource) {
    super(resource);
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    final List<String> roles = Collections.singletonList("admin");
    final OrganizationController organizationController =
        OrganizationControllerProvider.getOrganisationController();
    return new ArrayList<>(Arrays
        .asList(organizationController.getUsersIdsByRoleNames(getComponentInstanceId(), roles)));
  }

  @Override
  protected String getTemplateFileName() {
    return "tovalidate";
  }

  @Override
  protected String getBundleSubjectKey() {
    return "classifieds.supervisorNotifSubject";
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.PENDING_VALIDATION;
  }

  @Override
  protected boolean isSendImmediately() {
    return true;
  }

  @Override
  protected void perform(final ClassifiedDetail resource) {
    super.perform(resource);
    getNotificationMetaData().displayReceiversInFooter();
  }
}
