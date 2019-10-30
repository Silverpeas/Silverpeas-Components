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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.infoletter.notification;

import org.silverpeas.components.infoletter.model.InfoLetterPublicationPdC;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.user.UserSubscriptionNotificationBehavior;
import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.subscription.constant.SubscriberType;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberMapBySubscriberType;
import org.silverpeas.core.template.SilverpeasTemplate;

import java.util.Collection;

import static org.silverpeas.core.subscription.service.ResourceSubscriptionProvider.getSubscribersOfComponent;
import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

/**
 * @author silveryocha
 */
public class InfoLetterSubscriptionPublicationUserNotification extends
    AbstractTemplateUserNotificationBuilder<InfoLetterPublicationPdC>
    implements UserSubscriptionNotificationBehavior {

  private final User sender;
  private final SubscriptionSubscriberMapBySubscriberType subscriberIdsByTypes;

  /**
   * Default constructor
   * @param resource the resource which is the object of the notification.
   * @param sender the identifier of sender user.
   */
  public InfoLetterSubscriptionPublicationUserNotification(final InfoLetterPublicationPdC resource,
      final User sender) {
    super(resource);
    this.sender = sender;
    subscriberIdsByTypes = getSubscribersOfComponent(getComponentInstanceId()).indexBySubscriberType();
  }

  @Override
  protected boolean isUserCanBeNotified(final String userId) {
    return true;
  }

  @Override
  protected boolean isGroupCanBeNotified(final String groupId) {
    return true;
  }

  @Override
  protected void performTemplateData(final String language, final InfoLetterPublicationPdC resource,
      final SilverpeasTemplate template) {
    getNotificationMetaData().addLanguage(language, getTitle(language), "");
    template.setAttribute("infoLetter", getResource());
    template.setAttribute("infoLetterTitle", getResource().getName(language));
    final String desc = defaultStringIfNotDefined(getResource().getDescription(language), null);
    template.setAttribute("infoLetterDesc", desc);
    template.setAttribute("senderName", sender.getDisplayedName());
  }

  @Override
  protected void performNotificationResource(final String language,
      final InfoLetterPublicationPdC resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setResourceName(resource.getName(language));
    notificationResourceData.setResourceDescription(resource.getDescription(language));
  }

  @Override
  protected void perform(final InfoLetterPublicationPdC resource) {
    super.perform(resource);
    getNotificationMetaData().displayReceiversInFooter();
  }

  @Override
  protected String getResourceURL(final InfoLetterPublicationPdC resource) {
    return "/RinfoLetter/" + getComponentInstanceId() + "/View?parution=" + getResource().getPK().getId();
  }

  @Override
  protected String getLocalizationBundlePath() {
    return "org.silverpeas.infoLetter.multilang.infoLetterBundle";
  }

  @Override
  protected String getBundleSubjectKey() {
    return "infoLetter.emailSubject";
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "infoLetter.notifLinkLabel";
  }

  @Override
  protected String getTitle(final String language) {
    return super.getTitle(language) + getResource().getName(language);
  }

  @Override
  protected String getTemplateFileName() {
    return "infoLetterNotification";
  }

  @Override
  protected String getTemplatePath() {
    return "infoLetter";
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.PUBLISHED;
  }

  @Override
  protected String getComponentInstanceId() {
    return getResource().getComponentInstanceId();
  }

  @Override
  protected String getSender() {
    return sender.getId();
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return subscriberIdsByTypes.get(SubscriberType.USER).getAllIds();
  }

  @Override
  protected Collection<String> getGroupIdsToNotify() {
    return subscriberIdsByTypes.get(SubscriberType.GROUP).getAllIds();
  }

  @Override
  protected boolean isSendImmediately() {
    return true;
  }
}
