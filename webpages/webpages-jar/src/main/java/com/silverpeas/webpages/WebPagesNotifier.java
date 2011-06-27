/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.webpages;

import com.silverpeas.subscribe.SubscriptionService;
import com.silverpeas.subscribe.SubscriptionServiceFactory;
import com.silverpeas.ui.DisplayI18NHelper;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.util.template.SilverpeasTemplateFactory;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.node.model.NodePK;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class WebPagesNotifier {

  public WebPagesNotifier() {
  }

  public void sendSubscriptionsNotification(NodePK nodePK, String userId) {
    try {

      List<String> subscriberIds = (List<String>) getSubscribeBm().getSubscribers(nodePK);
      if (subscriberIds != null && !subscriberIds.isEmpty()) {
        Map<String, SilverpeasTemplate> templates = new HashMap<String, SilverpeasTemplate>();
        String fileName = "notificationUpdateContent";

        ResourceLocator message = new ResourceLocator(
                "com.silverpeas.webpages.multilang.webPagesBundle",
                DisplayI18NHelper.getDefaultLanguage());
        String subject = message.getString("webPages.subscription");
        NotificationMetaData notifMetaData = new NotificationMetaData(
                NotificationParameters.NORMAL, subject, templates, fileName);
        OrganizationController oc = new OrganizationController();
        String senderName = oc.getUserDetail(userId).getDisplayedName();

        for (String lang : DisplayI18NHelper.getLanguages()) {
          SilverpeasTemplate template = getNewTemplate();
          templates.put(lang, template);
          template.setAttribute("path", "");
          template.setAttribute("senderName", senderName);
          template.setAttribute("silverpeasURL", URLManager.getURL(null, null,
                  nodePK.getInstanceId()) + "Main");
          ResourceLocator localizedMessage = new ResourceLocator(
                  "com.silverpeas.webpages.multilang.webPagesBundle", lang);
          notifMetaData.addLanguage(lang, localizedMessage.getString("webPages.subscription",
                  subject), "");
        }
        notifMetaData.setUserRecipients(subscriberIds);
        notifMetaData.setLink(URLManager.getURL(null, null, nodePK.getInstanceId()) + "Main");
        notifMetaData.setComponentId(nodePK.getInstanceId());
        notifyUsers(notifMetaData, userId);
      }
    } catch (Exception e) {
      SilverTrace.warn("webPages", "WebPagesCallback.sendSubscriptionsNotification()",
              "webPages.EX_IMPOSSIBLE_DALERTER_LES_UTILISATEURS", "nodeId = " + nodePK.getId(), e);
    }
  }

  private void notifyUsers(NotificationMetaData notifMetaData, String senderId) {
    try {
      if (!StringUtil.isDefined(notifMetaData.getSender())) {
        notifMetaData.setSender(senderId);
      }
      getNotificationSender(notifMetaData.getComponentId()).notifyUser(
              notifMetaData);
    } catch (NotificationManagerException e) {
      SilverTrace.warn("webPages", "WebPagesCallback.notifyUsers()",
              "webPages.EX_IMPOSSIBLE_DALERTER_LES_UTILISATEURS", e);
    }
  }

  private NotificationSender getNotificationSender(String componentId) {
    // must return a new instance each time
    // This is to resolve Serializable problems
    return new NotificationSender(componentId);
  }

  public SubscriptionService getSubscribeBm() {
    return SubscriptionServiceFactory.getFactory().getSubscribeService();
  }

  protected SilverpeasTemplate getNewTemplate() {
    ResourceLocator rs =
            new ResourceLocator("com.silverpeas.webpages.settings.webPagesSettings", "");
    Properties templateConfiguration = new Properties();
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR, rs.getString(
            "templatePath"));
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR, rs.getString(
            "customersTemplatePath"));
    return SilverpeasTemplateFactory.createSilverpeasTemplate(templateConfiguration);
  }
}
