/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.components.classifieds.notification;

import org.silverpeas.components.classifieds.ClassifiedUtil;
import org.silverpeas.components.classifieds.model.ClassifiedDetail;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasTemplate;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractClassifiedUserNotification extends
    AbstractTemplateUserNotificationBuilder<ClassifiedDetail> {

  public AbstractClassifiedUserNotification(final ClassifiedDetail resource) {
    super(resource);
  }

  @Override
  protected String getLocalizationBundlePath() {
    return "org.silverpeas.classifieds.multilang.classifiedsBundle";
  }

  @Override
  protected String getTemplatePath() {
    return "classifieds";
  }

  @Override
  protected void performTemplateData(final String language, final ClassifiedDetail resource,
      final SilverpeasTemplate template) {
    getNotificationMetaData().addLanguage(language, getTitle(), "");
    template.setAttribute("classified", resource);
    template.setAttribute("classifiedName", resource.getTitle());
    template.setAttribute("senderName", User.getById(getSender()).getDisplayedName());
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

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "classifieds.notifClassifiedLinkLabel";
  }
}
