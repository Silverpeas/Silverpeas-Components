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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.formsonline.notification;

import org.silverpeas.components.formsonline.model.FormInstance;
import org.silverpeas.components.formsonline.model.FormInstanceValidation;
import org.silverpeas.components.formsonline.model.FormInstanceValidationType;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.template.SilverpeasTemplate;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Nicolas EYSSERIC
 */
public class FormsOnlineProcessedRequestFollowingUserNotification
    extends FormsOnlineValidationRequestUserNotification {

  private final List<String> usersToBeNotified;

  public FormsOnlineProcessedRequestFollowingUserNotification(final FormInstance resource,
      final NotifAction action) {
    super(resource, action);
    final FormInstanceValidationType latestValidation = resource.getValidations().getLatestValidation()
        .map(FormInstanceValidation::getValidationType)
        .orElse(null);
    // notify previous validators if marked as followers
    this.usersToBeNotified = resource.getPreviousValidations().stream()
        .filter(v -> v.getValidationType() != latestValidation)
        .filter(FormInstanceValidation::isFollower)
        .map(FormInstanceValidation::getValidator)
        .map(User::getId)
        .collect(Collectors.toList());
  }

  @Override
  protected void perform(final FormInstance resource) {
    super.perform(resource);
    getNotificationMetaData().displayReceiversInFooter();
  }

  @Override
  protected String getBundleSubjectKey() {
    return "formsOnline.msgFormProcessed.follower";
  }

  @Override
  protected String getTemplateFileName() {
    return "notificationProcessed";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return usersToBeNotified;
  }

  @Override
  protected void performTemplateData(final String language, final FormInstance resource,
      final SilverpeasTemplate template) {
    super.performTemplateData(language, resource, template);
    getResource().getValidations().getLatestValidation()
        .ifPresent(v -> template.setAttribute("validation", v));
  }
}