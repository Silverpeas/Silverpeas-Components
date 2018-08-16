/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.webpages.notification;

import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.notification.user.UserSubscriptionNotificationBehavior;
import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.subscription.constant.SubscriberType;
import org.silverpeas.core.subscription.service.ResourceSubscriptionProvider;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberMapBySubscriberType;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.Collection;
import java.util.List;
import java.util.MissingResourceException;

/**
 * @author Yohann Chastagnier
 */
public class WebPagesUserNotifier extends AbstractTemplateUserNotificationBuilder<NodePK>
    implements UserSubscriptionNotificationBehavior {

  private final String userId;

  private SubscriptionSubscriberMapBySubscriberType subscriberIdsByTypes;

  /**
   * Builds and sends a webpages notification. A warning message is logged when an exception is
   * catched.
   * @param resource
   * @param userId
   */
  public static void notify(final NodePK resource, final String userId) {
    try {
      UserNotificationHelper.buildAndSend(new WebPagesUserNotifier(resource, userId));
    } catch (final Exception e) {
      SilverLogger.getLogger(WebPagesUserNotifier.class).warn(e);
    }
  }

  /**
   * Default constructor
   * @param resource
   */
  public WebPagesUserNotifier(final NodePK resource, final String userId) {
    super(resource);
    this.userId = userId;
  }

  @Override
  protected void initialize() {
    super.initialize();

    // Subscribers
    subscriberIdsByTypes =
        ResourceSubscriptionProvider.getSubscribersOfComponent(getResource().getInstanceId())
            .indexBySubscriberType();
  }

  @Override
  protected void performTemplateData(final String language, final NodePK resource,
      final SilverpeasTemplate template) {
    String title;
    try {
      title = getBundle(language).getString(getBundleSubjectKey());
    } catch (MissingResourceException ex) {
      title = getTitle();
    }
    getNotificationMetaData().addLanguage(language, title, "");
    template.setAttribute("path", "");
    template.setAttribute("senderName", OrganizationControllerProvider.getOrganisationController().
        getUserDetail(userId).getDisplayedName());
  }

  @Override
  protected void performNotificationResource(final String language, final NodePK resource,
      final NotificationResourceData notificationResourceData) {
    // The resource name corresponds at the label of the instantiated application
    notificationResourceData.setResourceName(OrganizationControllerProvider.
        getOrganisationController().getComponentInstLight(getComponentInstanceId()).getLabel());
    notificationResourceData.setResourceId(resource.getId());
    notificationResourceData.setResourceType(getTemplatePath());
    // Exceptionally the resource location is builded at this level
    // Normally, the location is builded by the delayed notification mechanism
    notificationResourceData.setResourceLocation(buildResourceLocation());
  }

  /**
   * Builds the specific location
   * @return the specific location
   */
  private String buildResourceLocation() {
    final StringBuilder sb = new StringBuilder();
    AdminController adminController = ServiceProvider.getService(AdminController.class);
    final List<SpaceInstLight> spaces =
        adminController.getPathToComponent(getComponentInstanceId());
    for (final SpaceInstLight space : spaces) {
      if (sb.length() > 0) {
        sb.append(NotificationResourceData.LOCATION_SEPARATOR);
      }
      sb.append(space.getName());
    }
    return sb.toString();
  }

  @Override
  protected String getResourceURL(final NodePK resource) {
    return URLUtil.getURL(null, null, resource.getInstanceId()) + "Main";
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.UPDATE;
  }

  @Override
  protected String getMultilangPropertyFile() {
    return "org.silverpeas.webpages.multilang.webPagesBundle";
  }

  @Override
  protected String getBundleSubjectKey() {
    return "webPages.subscription";
  }

  @Override
  protected String getTemplatePath() {
    return "webpages";
  }

  @Override
  protected String getTemplateFileName() {
    return "notificationUpdateContent";
  }

  @Override
  protected String getComponentInstanceId() {
    return getResource().getInstanceId();
  }

  @Override
  protected String getSender() {
    return userId;
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return subscriberIdsByTypes.get(SubscriberType.USER).getAllIds();
  }

  @Override
  protected Collection<String> getGroupIdsToNotify() {
    return subscriberIdsByTypes.get(SubscriberType.GROUP).getAllIds();
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "webPages.notifWebPageLinkLabel";
  }
}
