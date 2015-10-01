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
package org.silverpeas.components.forum.notification;

import com.silverpeas.usernotification.builder.AbstractTemplateUserNotificationBuilder;
import com.stratelia.webactiv.forums.forumsManager.ejb.ForumsBM;
import com.stratelia.webactiv.forums.forumsManager.ejb.ForumsServiceProvider;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.util.SettingBundle;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractForumsUserNotification<T>
    extends AbstractTemplateUserNotificationBuilder<T> {
  protected SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.forums.settings.forumsMails");

  protected AbstractForumsUserNotification(final T resource) {
    super(resource);
  }

  @Override
  protected String getMultilangPropertyFile() {
    return "org.silverpeas.forums.multilang.forumsBundle";
  }

  @Override
  protected String getTemplatePath() {
    return "forums";
  }

  /**
   * Gets the services of forum.
   * @return
   */
  protected ForumsBM getForumsService() {
    return ForumsServiceProvider.getForumsService();
  }
}
