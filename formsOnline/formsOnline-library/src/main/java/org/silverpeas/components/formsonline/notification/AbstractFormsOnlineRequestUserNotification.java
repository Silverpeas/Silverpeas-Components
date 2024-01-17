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
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.ContributionValidation;
import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.util.URLUtil;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.silverpeas.core.notification.user.client.constant.NotifAction.CANCELED;
import static org.silverpeas.core.notification.user.client.constant.NotifAction.PENDING_VALIDATION;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractFormsOnlineRequestUserNotification
    extends AbstractTemplateUserNotificationBuilder<FormInstance> {

  private final NotifAction action;
  private final String senderName;

  protected AbstractFormsOnlineRequestUserNotification(final FormInstance resource,
      final NotifAction action) {
    super(resource);
    this.action = action;
    final Supplier<String> creatorDisplayName = () -> User.getById(resource.getCreatorId()).getDisplayedName();
    if (PENDING_VALIDATION.equals(action) || CANCELED.equals(action)) {
      this.senderName = creatorDisplayName.get();
    } else {
      this.senderName = getLatestRequestValidator()
          .map(User::getDisplayedName)
          .orElseGet(creatorDisplayName);
    }
  }

  @Override
  protected String getLocalizationBundlePath() {
    return "org.silverpeas.formsonline.multilang.formsOnlineBundle";
  }

  @Override
  protected String getTemplatePath() {
    return "formsonline";
  }

  @Override
  protected void performTemplateData(final String language, final FormInstance resource,
      final SilverpeasTemplate template) {
    getNotificationMetaData().addLanguage(language, getTitle(language), "");
    template.setAttribute("form", resource.getForm());
    template.setAttribute("request", resource);
    template.setAttribute("formName", resource.getForm().getName());
    template.setAttribute("senderName", getSenderName());
    template.setAttribute("requester", resource.getCreator());
    template.setAttribute("validator", resource.getValidator());
    template.setAttribute("pendingValidation", resource.getPendingValidation());
  }

  @Override
  protected void performNotificationResource(final String language,
      final FormInstance resource, final NotificationResourceData notificationResourceData) {
    // do nothing ???
  }

  @Override
  protected String getResourceURL(final FormInstance resource) {
    return URLUtil.getComponentInstanceURL(resource.getComponentInstanceId()) +
        "/ViewRequest?Id=" + resource.getId();
  }

  protected String getSenderName() {
    return senderName;
  }

  @Override
  protected NotifAction getAction() {
    return action;
  }

  @Override
  protected String getComponentInstanceId() {
    return getResource().getComponentInstanceId();
  }

  @Override
  protected String getSender() {
    final Supplier<String> creatorIdSupplier = () -> getResource().getCreatorId();
    if (PENDING_VALIDATION.equals(action) || CANCELED.equals(action)) {
      return creatorIdSupplier.get();
    }
    return getLatestRequestValidator()
        .map(User::getId)
        .orElseGet(creatorIdSupplier);
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "formsOnline.notifLinkLabel";
  }

  @Override
  protected boolean isSendImmediately() {
    return true;
  }

  protected Optional<User> getLatestRequestValidator() {
    return getResource().getValidations().getLatestValidation().map(ContributionValidation::getValidator);
  }

  protected List<String> extractGroupIds(final List<Group> groups) {
    return groups.stream().map(Group::getId).collect(Collectors.toList());
  }

  protected List<String> extractUserIds(final List<User> users) {
    return users.stream().map(User::getId).collect(Collectors.toList());
  }
}