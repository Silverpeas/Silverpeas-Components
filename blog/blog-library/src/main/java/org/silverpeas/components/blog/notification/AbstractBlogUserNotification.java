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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.blog.notification;

import org.owasp.encoder.Encode;
import org.silverpeas.components.blog.model.Category;
import org.silverpeas.components.blog.model.PostDetail;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.util.DateUtil;

import static org.silverpeas.kernel.util.StringUtil.defaultStringIfNotDefined;

/**
 * @author Nicolas Eysseric
 */
abstract class AbstractBlogUserNotification
    extends AbstractTemplateUserNotificationBuilder<PostDetail> {

  private final User user;

  AbstractBlogUserNotification(final PostDetail postDetail, final User user) {
    super(postDetail);
    this.user = user;
  }

  @Override
  protected void performTemplateData(final String language, final PostDetail resource,
      final SilverpeasTemplate template) {
    getNotificationMetaData().addLanguage(language,
        defaultStringIfNotDefined(getTitle(language), getTitle()),
        "");
    template.setAttribute("blog", resource);
    template.setAttribute("blogName", Encode.forHtml(resource.getPublication().getName(language)));
    template.setAttribute("blogDate", DateUtil.getOutputDate(resource.getDateEvent(), language));
    final Category categorie = resource.getCategory();
    String categorieName = null;
    if (categorie != null) {
      categorieName = categorie.getName(language);
    }
    template.setAttribute("blogCategorie", categorieName);
    template.setAttribute("senderName", (user != null ? user.getDisplayedName() : ""));
  }

  @Override
  protected void performNotificationResource(final String language, final PostDetail resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setFeminineGender(false);
    notificationResourceData.setResourceName(resource.getPublication().getName(language));
  }

  @Override
  protected void perform(final PostDetail resource) {
    super.perform(resource);
    getNotificationMetaData().displayReceiversInFooter();
  }

  @Override
  protected String getTemplatePath() {
    return "blog";
  }

  @Override
  protected String getComponentInstanceId() {
    return getResource().getComponentInstanceId();
  }

  @Override
  protected String getSender() {
    return user.getId();
  }

  @Override
  protected String getLocalizationBundlePath() {
    return "org.silverpeas.blog.multilang.blogBundle";
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "blog.notifPostLinkLabel";
  }
}