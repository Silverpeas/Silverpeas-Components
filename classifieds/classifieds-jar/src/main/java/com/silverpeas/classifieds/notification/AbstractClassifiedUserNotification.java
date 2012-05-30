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
package com.silverpeas.classifieds.notification;

import java.util.Collection;

import com.silverpeas.classifieds.ClassifiedUtil;
import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.silverpeas.notification.builder.AbstractTemplateUserNotificationBuilder;
import com.silverpeas.notification.model.NotificationResourceData;
import com.silverpeas.util.CollectionUtil;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.stratelia.silverpeas.notificationManager.UserRecipient;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractClassifiedUserNotification extends AbstractTemplateUserNotificationBuilder<ClassifiedDetail> {

  public AbstractClassifiedUserNotification(final ClassifiedDetail resource) {
    super(resource);
  }

  public AbstractClassifiedUserNotification(final ClassifiedDetail resource, final String title, final String fileName) {
    super(resource, title, fileName);
  }

  @Override
  protected String getMultilangPropertyFile() {
    return "com.silverpeas.classifieds.multilang.classifiedsBundle";
  }

  @Override
  protected String getTemplatePath() {
    return "classifieds";
  }

  protected abstract Collection<String> getUserIdToNotify();

  protected abstract String getSubjectKey();

  @Override
  protected String getTitle() {
    return getBundle().getString(getSubjectKey());
  }

  @Override
  protected void perform(final ClassifiedDetail resource) {
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
  protected void performTemplateData(final String language, final ClassifiedDetail resource,
      final SilverpeasTemplate template) {
    getNotification().addLanguage(language, getBundle(language).getString(getSubjectKey(), getTitle()), "");
    template.setAttribute("classified", resource);
    template.setAttribute("classifiedName", resource.getTitle());
    template.setAttribute("silverpeasURL", getResourceURL(resource));
  }

  @Override
  protected void performNotificationResource(final String language, final ClassifiedDetail resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setResourceName(resource.getTitle());
  }

  @Override
  protected String getResourceURL(final ClassifiedDetail resource) {
    return ClassifiedUtil.getClassifiedUrl(resource);
  }

  @Override
  protected String getComponentInstanceId() {
    return getResource().getInstanceId();
  }

  @Override
  protected String getSender() {
    return getResource().getCreatorId();
  }
}
