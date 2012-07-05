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

import com.silverpeas.notification.model.NotificationResourceData;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaHelper;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractKmeliaPublicationUserNotification extends
    AbstractKmeliaUserNotification<PublicationDetail> {

  public AbstractKmeliaPublicationUserNotification(final PublicationDetail resource, final String fileName,
      final String subject) {
    super(resource, null, null);
  }

  protected abstract String getPath(final String language);

  protected abstract String getSenderName();

  @Override
  protected void performTemplateData(final String language, final PublicationDetail resource,
      final SilverpeasTemplate template) {
    getNotification().addLanguage(language, getBundle(language).getString(getBundleSubjectKey(), getTitle()), "");
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
    notificationResourceData.setResourceDescription(resource.getDescription(language));
  }

  @Override
  protected String getResourceURL(final PublicationDetail resource) {
    return KmeliaHelper.getPublicationUrl(resource);
  }
}
