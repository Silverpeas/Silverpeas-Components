/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
package com.silverpeas.webpages.notification;

import com.silverpeas.subscribe.constant.SubscriberType;
import com.silverpeas.subscribe.service.ResourceSubscriptionProvider;
import com.silverpeas.subscribe.util.SubscriptionSubscriberMapBySubscriberType;
import com.silverpeas.usernotification.builder.AbstractTemplateUserNotificationBuilder;
import com.silverpeas.usernotification.builder.UserSubscriptionNotificationBehavior;
import com.silverpeas.usernotification.builder.helper.UserNotificationHelper;
import com.silverpeas.usernotification.model.NotificationResourceData;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.node.model.NodePK;
import org.silverpeas.core.admin.OrganizationControllerProvider;
import org.silverpeas.util.ServiceProvider;
import org.silverpeas.util.template.SilverpeasTemplate;

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
      SilverTrace.warn("webPages", "WebPagesUserNotifier.notify()",
          "webPages.EX_IMPOSSIBLE_DALERTER_LES_UTILISATEURS", "nodeId = " + resource.getId(), e);
    }
  }

  /**
   * Default constructor
   * @param resource
   */
  public WebPagesUserNotifier(final NodePK resource, final String userId) {
    super(resource, "notificationUpdateContent");
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
    if (spaces != null) {
      for (final SpaceInstLight space : spaces) {
        if (sb.length() > 0) {
          sb.append(NotificationResourceData.LOCATION_SEPARATOR);
        }
        sb.append(space.getName());
      }
    }
    return sb.toString();
  }

  @Override
  protected String getResourceURL(final NodePK resource) {
    return URLManager.getURL(null, null, resource.getInstanceId()) + "Main";
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
