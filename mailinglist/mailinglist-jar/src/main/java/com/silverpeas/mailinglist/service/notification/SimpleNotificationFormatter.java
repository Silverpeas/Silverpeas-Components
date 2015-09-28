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
package com.silverpeas.mailinglist.service.notification;

import com.silverpeas.mailinglist.service.model.beans.Message;
import org.silverpeas.util.LocalizationBundle;
import org.silverpeas.util.ResourceLocator;

import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;
import java.text.MessageFormat;

@Singleton
@Alternative
public class SimpleNotificationFormatter extends AbstractNotificationFormatter {

  public static final String TITLE_KEY = "mailinglist.notification.title";
  public static final String TITLE_MODERATION_KEY = "mailinglist.notification.moderation.title";
  public static final String BODY_KEY = "mailinglist.notification.body";
  public static final String BODY_MODERATION_KEY = "mailinglist.notification.moderation.body";
  private MessageFormat titleFormatter;
  private MessageFormat titleModerationFormatter;
  private MessageFormat bodyFormatter;
  private MessageFormat bodyModerationFormatter;

  public SimpleNotificationFormatter() {
    LocalizationBundle resources = ResourceLocator.getLocalizationResource(
        "com.silverpeas.mailinglist.multilang.mailinglistBundle");
    titleFormatter = new MessageFormat(resources.getString(TITLE_KEY));
    titleModerationFormatter = new MessageFormat(resources.getString(TITLE_MODERATION_KEY));
    bodyFormatter = new MessageFormat(resources.getString(BODY_KEY));
    bodyModerationFormatter = new MessageFormat(resources.getString(BODY_MODERATION_KEY));
  }

  @Override
  public String formatTitle(Message message, String mailingListName, String lang,
      boolean moderate) {
    if (moderate) {
      return titleModerationFormatter.format(new String[]{mailingListName, message.getTitle()});
    }
    return titleFormatter.format(new String[]{mailingListName, message.getTitle()});
  }

  @Override
  public String formatMessage(Message message, String lang, boolean moderate) {
    if (moderate) {
      return bodyModerationFormatter.format(
          new String[]{message.getSummary(), prepareUrl(message, moderate), message.getTitle()});
    }
    return bodyFormatter.format(
        new String[]{message.getSummary(), prepareUrl(message, moderate), message.getTitle()});
  }
}
