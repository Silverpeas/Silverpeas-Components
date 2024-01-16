/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.delegatednews.notification;

import org.silverpeas.components.delegatednews.model.DelegatedNews;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.util.URLUtil;

import java.util.Collection;
import java.util.Collections;

import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

abstract class AbstractDelegatedNewsUserNotification
    extends AbstractTemplateUserNotificationBuilder<DelegatedNews> {

  private final User user;
  private final PublicationDetail publication;

  AbstractDelegatedNewsUserNotification(final DelegatedNews delegatedNews, final User user) {
    super(delegatedNews);
    this.user = user;
    this.publication = delegatedNews.getPublicationDetail();
  }

  @Override
  protected void performTemplateData(final String language, final DelegatedNews resource,
      final SilverpeasTemplate template) {
    final String title = defaultStringIfNotDefined(getTitle(language), getTitle());
    getNotificationMetaData().addLanguage(language, title, "");
    template.setAttribute("publicationId", publication.getId());
    template.setAttribute("publicationName", publication.getName(language));
    template.setAttribute("senderName", (user != null ? user.getDisplayedName() : ""));
  }

  @Override
  protected void performNotificationResource(final String language, final DelegatedNews resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setFeminineGender(true);
    notificationResourceData.setResourceName(publication.getName(language));
  }

  @Override
  protected void perform(final DelegatedNews resource) {
    super.perform(resource);
    getNotificationMetaData().displayReceiversInFooter();
  }

  @Override
  protected String getTemplatePath() {
    return "delegatednews";
  }

  @Override
  protected String getComponentInstanceId() {
    return getPublication().getInstanceId();
  }

  @Override
  protected String getSender() {
    return user.getId();
  }

  @Override
  protected String getLocalizationBundlePath() {
    return "org.silverpeas.delegatednews.multilang.DelegatedNewsBundle";
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "delegatednews.notifLinkLabel";
  }

  @Override
  protected String getResourceURL(final DelegatedNews delegatedNews) {
    return URLUtil.getSimpleURL(URLUtil.URL_PUBLI, publication.getId(), false);
  }

  @Override
  protected boolean stopWhenNoUserToNotify() {
    return false;
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return Collections.singletonList(getPublication().getUpdaterId());
  }

  protected PublicationDetail getPublication() {
    return publication;
  }

  @Override
  protected boolean isSendImmediately() {
    return true;
  }
}