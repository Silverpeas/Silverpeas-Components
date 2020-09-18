/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

package org.silverpeas.components.formsonline.notification;

import org.silverpeas.components.formsonline.model.FormDetail;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.util.URLUtil;

import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

/**
 * @author Nicolas Eysseric
 */
abstract class AbstractFormsOnlineFormUserNotification
    extends AbstractTemplateUserNotificationBuilder<FormDetail> {

  private final User user;

  AbstractFormsOnlineFormUserNotification(final FormDetail form, final User user) {
    super(form);
    this.user = user;
  }

  @Override
  protected void performTemplateData(final String language, final FormDetail resource,
      final SilverpeasTemplate template) {
    getNotificationMetaData().addLanguage(language,
        defaultStringIfNotDefined(getTitle(language), getTitle()),
        "");
    template.setAttribute("form", resource);
    template.setAttribute("senderName", (user != null ? user.getDisplayedName() : ""));
  }

  @Override
  protected void performNotificationResource(final String language, final FormDetail resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setFeminineGender(false);
    notificationResourceData.setResourceName(resource.getName());
  }

  @Override
  protected void perform(final FormDetail resource) {
    super.perform(resource);
    getNotificationMetaData().displayReceiversInFooter();
  }

  @Override
  protected String getResourceURL(final FormDetail resource) {
    return URLUtil.getComponentInstanceURL(resource.getInstanceId()) +
        "/NewRequest?FormId=" + resource.getId();
  }

  @Override
  protected String getTemplatePath() {
    return "formsonline";
  }

  @Override
  protected String getComponentInstanceId() {
    return getResource().getInstanceId();
  }

  @Override
  protected String getSender() {
    return user.getId();
  }

  @Override
  protected String getLocalizationBundlePath() {
    return "org.silverpeas.formsonline.multilang.formsOnlineBundle";
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "formsOnline.Preview";
  }
}