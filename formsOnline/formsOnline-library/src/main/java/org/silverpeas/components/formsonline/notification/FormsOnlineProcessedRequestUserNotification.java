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
package org.silverpeas.components.formsonline.notification;

import org.silverpeas.components.formsonline.model.FormInstance;
import org.silverpeas.components.formsonline.model.FormInstanceValidation;
import org.silverpeas.components.formsonline.model.FormInstanceValidationType;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.template.SilverpeasTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

/**
 * @author Nicolas EYSSERIC
 */
public class FormsOnlineProcessedRequestUserNotification
    extends FormsOnlineValidationRequestUserNotification {

  private final List<String> usersToBeNotified;
  private final List<String> groupsToBeNotified;

  public FormsOnlineProcessedRequestUserNotification(final FormInstance resource,
      final NotifAction action) {
    super(resource, action);
    final Optional<FormInstanceValidationType> pendingValidation = ofNullable(resource.getPendingValidation())
        .map(FormInstanceValidation::getValidationType);
    if (pendingValidation.filter(FormInstanceValidationType::isFinal).isPresent()) {
      this.usersToBeNotified = extractUserIds(resource.getForm().getReceiversAsUsers());
      this.groupsToBeNotified = extractGroupIds(resource.getForm().getReceiversAsGroups());
    } else if (pendingValidation.filter(FormInstanceValidationType::isIntermediate).isPresent()) {
      this.usersToBeNotified = extractUserIds(resource.getForm().getIntermediateReceiversAsUsers());
      this.groupsToBeNotified = extractGroupIds(resource.getForm().getIntermediateReceiversAsGroups());
    } else {
      this.usersToBeNotified = emptyList();
      this.groupsToBeNotified = emptyList();
    }
  }

  @Override
  protected void perform(final FormInstance resource) {
    super.perform(resource);
    getNotificationMetaData().displayReceiversInFooter();
  }

  @Override
  protected String getBundleSubjectKey() {
    if (getResource().getValidations().getLatestValidation().getValidationType().isFinal()) {
      return "formsOnline.msgFormProcessed";
    }
    return "formsOnline.msgFormToValid";
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
  protected Collection<String> getGroupIdsToNotify() {
    return groupsToBeNotified;
  }

  @Override
  protected void performTemplateData(final String language, final FormInstance resource,
      final SilverpeasTemplate template) {
    super.performTemplateData(language, resource, template);
    template.setAttribute("validation", getResource().getValidations().getLatestValidation());
  }
}