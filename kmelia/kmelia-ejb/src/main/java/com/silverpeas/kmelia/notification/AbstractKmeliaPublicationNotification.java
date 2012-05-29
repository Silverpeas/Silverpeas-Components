/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.kmelia.notification;

import java.util.Collection;

import com.silverpeas.notification.model.NotificationResourceData;
import com.silverpeas.util.CollectionUtil;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaHelper;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractKmeliaPublicationNotification extends AbstractKmeliaNotification<PublicationDetail> {

  public AbstractKmeliaPublicationNotification(final PublicationDetail resource, final String fileName,
      final String subject) {
    super(resource, null, null);
  }

  protected abstract Collection<String> getUserIdToNotify();

  protected abstract String getSubjectKey();

  protected abstract String getPath(final String language);

  protected abstract String getSenderName();

  @Override
  protected String getTitle() {
    return getBundle().getString(getSubjectKey());
  }

  @Override
  protected void perform(final PublicationDetail resource) {
    final Collection<String> userIdToNotify = getUserIdToNotify();

    // Stopping the process if no user to notify
    if (stopWhenNoUserToNotify() && CollectionUtil.isEmpty(userIdToNotify)) {
      stop();
    }

    if (CollectionUtil.isNotEmpty(userIdToNotify)) {
      // There is at least one user to notify
      for (final String userId : userIdToNotify) {
        getNotification().addUserRecipient(new UserRecipient(userId));
      }
    }
  }

  protected boolean stopWhenNoUserToNotify() {
    return true;
  }

  @Override
  protected void performTemplateData(final String language, final PublicationDetail resource,
      final SilverpeasTemplate template) {
    getNotification().addLanguage(language, getBundle().getString(getSubjectKey(), getTitle()), "");
    template.setAttribute("path", getPath(language));
    template.setAttribute("publication", resource);
    template.setAttribute("publicationName", resource.getName(language));
    template.setAttribute("publicationDesc", resource.getDescription(language));
    template.setAttribute("publicationKeywords", resource.getKeywords(language));
    template.setAttribute("senderName", getSenderName());
    template.setAttribute("silverpeasURL", getResourceURL(resource));
  }

  @Override
  protected void performNotificationResource(final String language, final PublicationDetail resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setResourceName(resource.getName(language));
  }

  @Override
  protected String getResourceURL(final PublicationDetail resource) {
    return KmeliaHelper.getPublicationUrl(resource);
  }
}
