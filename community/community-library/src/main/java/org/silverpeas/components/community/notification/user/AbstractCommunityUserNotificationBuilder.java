/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.components.community.notification.user;

import org.silverpeas.components.community.CommunityComponentSettings;
import org.silverpeas.components.community.model.CommunityOfUsers;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.SpaceProfile;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.util.MemoizedSupplier;

/**
 * @author silveryocha
 */
public abstract class AbstractCommunityUserNotificationBuilder
    extends AbstractTemplateUserNotificationBuilder<CommunityOfUsers> {

  private final MemoizedSupplier<SpaceInst> communitySpace = new MemoizedSupplier<>(
      () -> OrganizationController.get().getSpaceInstById(getResource().getSpaceId()));

  public AbstractCommunityUserNotificationBuilder(final CommunityOfUsers resource) {
    super(resource);
  }

  @Override
  protected String getLocalizationBundlePath() {
    return CommunityComponentSettings.MESSAGES_PATH;
  }

  @Override
  protected String getTemplatePath() {
    return CommunityComponentSettings.COMPONENT_NAME;
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.REPORT;
  }

  @Override
  protected String getComponentInstanceId() {
    return getResource().getComponentInstanceId();
  }

  @Override
  protected String getSender() {
    return User.getSystemUser().getId();
  }

  protected SpaceInst getSpace() {
    return communitySpace.get();
  }

  @Override
  protected void performNotificationResource(final String language, final CommunityOfUsers resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setResourceName(getSpace().getName(language));
  }

  @Override
  protected void performTemplateData(final String language, final CommunityOfUsers resource,
      final SilverpeasTemplate template) {
    String title = getTitle();
    getNotificationMetaData().addLanguage(language, title, "");
    template.setAttribute("spaceName", getSpace().getName(language));
  }

  @Override
  protected boolean isSendImmediately() {
    return true;
  }

  @Override
  protected String getResourceURL(final CommunityOfUsers resource) {
    return getSpace().getPermalink();
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "community.notifSpaceLinkLabel";
  }

  protected SpaceProfile getSpaceManagerProfile() {
    return OrganizationController.get()
        .getSpaceProfile(getResource().getSpaceId(), SilverpeasRole.MANAGER);
  }
}
