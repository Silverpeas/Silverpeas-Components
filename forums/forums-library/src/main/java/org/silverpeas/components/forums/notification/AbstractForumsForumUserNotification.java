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
package org.silverpeas.components.forums.notification;

import org.silverpeas.components.forums.model.ForumDetail;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.MissingResourceException;

/**
 * User: Yohann Chastagnier
 * Date: 10/06/13
 */
public abstract class AbstractForumsForumUserNotification
    extends AbstractForumsUserNotification<ForumDetail> {

  private NotifAction action = null;

  /**
   * Default constructor.
   * @param resource
   */
  public AbstractForumsForumUserNotification(final ForumDetail resource, final NotifAction action) {
    super(resource);
    this.action = action;
  }

  @Override
  protected void perform(final ForumDetail resource) {
    super.perform(resource);
    getNotificationMetaData().displayReceiversInFooter();
  }

  @Override
  protected void performTemplateData(final String language, final ForumDetail resource,
      final SilverpeasTemplate template) {
    String title;
    try {
      title = getBundle(language).getString(getBundleSubjectKey());
    } catch (MissingResourceException ex) {
      SilverLogger.getLogger(this).warn(ex);
      title = getTitle();
    }
    getNotificationMetaData().addLanguage(language, title, "");
    template.setAttribute("title", resource.getName());
  }

  @Override
  protected void performNotificationResource(final String language, final ForumDetail resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setFeminineGender(false);
    notificationResourceData.setResourceName(resource.getName());
  }

  @Override
  protected NotifAction getAction() {
    return action;
  }

  @Override
  protected String getComponentInstanceId() {
    return getResource().getInstanceId();
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "forums.notifForumLinkLabel";
  }
}
