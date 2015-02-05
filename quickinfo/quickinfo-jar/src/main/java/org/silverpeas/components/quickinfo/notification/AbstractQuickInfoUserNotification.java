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

package org.silverpeas.components.quickinfo.notification;

import com.silverpeas.usernotification.builder.AbstractTemplateUserNotificationBuilder;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.components.quickinfo.QuickInfoComponentSettings;

public abstract class AbstractQuickInfoUserNotification<T> extends
    AbstractTemplateUserNotificationBuilder<T> {

  public AbstractQuickInfoUserNotification(T resource) {
    super(resource);
  }
  
  public AbstractQuickInfoUserNotification(final T resource, final String title,
      final String fileName) {
    super(resource, title, fileName);
  }

  @Override
  protected String getMultilangPropertyFile() {
    return QuickInfoComponentSettings.MESSAGES_PATH;
  }

  @Override
  protected String getTemplatePath() {
    return QuickInfoComponentSettings.COMPONENT_NAME;
  }

  /**
   * Gets the name of the sender.
   * @return
   */
  protected String getSenderName() {
    UserDetail sender = getSenderDetail();
    if (sender != null) {
      return sender.getDisplayedName();
    }
    return getSender();
  }

  /**
   * Gets the {@link UserDetail} instance of the sender.
   * @return
   */
  protected final UserDetail getSenderDetail() {
    return UserDetail.getById(getSender());
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "quickinfo.news.notifNewsLinkLabel";
  }

}
