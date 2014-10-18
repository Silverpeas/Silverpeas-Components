/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.webpages.notification;

import org.silverpeas.core.admin.OrganisationControllerProvider;
import com.silverpeas.usernotification.builder.AbstractTemplateUserNotificationBuilder;
import com.silverpeas.usernotification.builder.helper.UserNotificationHelper;
import com.silverpeas.usernotification.model.NotificationResourceData;
import com.silverpeas.subscribe.SubscriptionServiceFactory;
import com.silverpeas.subscribe.constant.SubscriberType;
import com.silverpeas.subscribe.service.ComponentSubscriptionResource;
import com.silverpeas.subscribe.util.SubscriptionUtil;
import org.silverpeas.util.template.SilverpeasTemplate;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.node.model.NodePK;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Yohann Chastagnier
 */
public class WebPagesUserNotifier extends AbstractTemplateUserNotificationBuilder<NodePK> {

  private final String userId;

  private Map<SubscriberType, Collection<String>> subscriberIdsByTypes;

  /**
   * Builds and sends a webpages notification. A warning message is logged when an exception is
   * catched.
   *
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
   *
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
    subscriberIdsByTypes = SubscriptionUtil.indexSubscriberIdsByType(
        SubscriptionServiceFactory.getFactory().getSubscribeService()
            .getSubscribers(ComponentSubscriptionResource.from(getResource().getInstanceId())));
  }

  /*
     * (non-Javadoc)
     * @see
     * com.silverpeas.notification.builder
     * .AbstractTemplateUserNotificationBuilder#performTemplateData
     * (java.lang.String, java.lang.Object, org.silverpeas.util.template.SilverpeasTemplate)
     */
  @Override
  protected void performTemplateData(final String language, final NodePK resource,
      final SilverpeasTemplate template) {
    getNotificationMetaData()
        .addLanguage(language, getBundle(language).getString(getBundleSubjectKey(), getTitle()),
            "");
    template.setAttribute("path", "");
    template.setAttribute("senderName", OrganisationControllerProvider.getOrganisationController().
        getUserDetail(userId).getDisplayedName());
    template.setAttribute("silverpeasURL", getResourceURL(resource));
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.notification.builder.AbstractTemplateUserNotificationBuilder#
   * performNotificationResource(java.lang.String, java.lang.Object,
   * com.silverpeas.notification.model.NotificationResourceData)
   */
  @Override
  protected void performNotificationResource(final String language, final NodePK resource,
      final NotificationResourceData notificationResourceData) {
    // The resource name corresponds at the label of the instantiated application
    notificationResourceData.setResourceName(OrganisationControllerProvider.
        getOrganisationController().getComponentInstLight(getComponentInstanceId()).getLabel());
    notificationResourceData.setResourceId(resource.getId());
    notificationResourceData.setResourceType(getTemplatePath());
    // Exceptionally the resource location is builded at this level
    // Normally, the location is builded by the delayed notification mechanism
    notificationResourceData.setResourceLocation(buildResourceLocation());
  }

  /**
   * Builds the specific location
   *
   * @return the specific location
   */
  private String buildResourceLocation() {
    final StringBuilder sb = new StringBuilder();
    final List<SpaceInstLight> spaces =
        new AdminController(null).getPathToComponent(getComponentInstanceId());
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

  /*
   * (non-Javadoc)
   * @see
   * com.silverpeas.notification.builder.AbstractResourceUserNotificationBuilder#getResourceURL(
   * java.lang.Object)
   */
  @Override
  protected String getResourceURL(final NodePK resource) {
    return URLManager.getURL(null, null, resource.getInstanceId()) + "Main";
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.notification.builder.AbstractUserNotificationBuilder#getAction()
   */
  @Override
  protected NotifAction getAction() {
    return NotifAction.UPDATE;
  }

  /*
   * (non-Javadoc)
   * @see
   * com.silverpeas.notification.builder.AbstractUserNotificationBuilder#getMultilangPropertyFile()
   */
  @Override
  protected String getMultilangPropertyFile() {
    return "com.silverpeas.webpages.multilang.webPagesBundle";
  }

  /*
   * (non-Javadoc)
   * @see
   * com.silverpeas.notification.builder.AbstractTemplateUserNotificationBuilder#getBundleSubjectKey
   * ()
   */
  @Override
  protected String getBundleSubjectKey() {
    return "webPages.subscription";
  }

  /*
   * (non-Javadoc)
   * @see
   * com.silverpeas.notification.builder.AbstractTemplateUserNotificationBuilder#getTemplatePath()
   */
  @Override
  protected String getTemplatePath() {
    return "webpages";
  }

  /*
   * (non-Javadoc)
   * @see
   * com.silverpeas.notification.builder.AbstractUserNotificationBuilder#getComponentInstanceId()
   */
  @Override
  protected String getComponentInstanceId() {
    return getResource().getInstanceId();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.notification.builder.AbstractUserNotificationBuilder#getSender()
   */
  @Override
  protected String getSender() {
    return userId;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.notification.builder.AbstractUserNotificationBuilder#getUserIdsToNotify()
   */
  @Override
  protected Collection<String> getUserIdsToNotify() {
    return subscriberIdsByTypes.get(SubscriberType.USER);
  }

  @Override
  protected Collection<String> getGroupIdsToNotify() {
    return subscriberIdsByTypes.get(SubscriberType.GROUP);
  }
}
