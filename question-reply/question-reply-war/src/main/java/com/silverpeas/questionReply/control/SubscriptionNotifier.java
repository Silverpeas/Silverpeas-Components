/*
 *  Copyright (C) 2000 - 2011 Silverpeas
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 * 
 *  As a special exception to the terms and conditions of version 3.0 of
 *  the GPL, you may redistribute this Program in connection with Free/Libre
 *  Open Source Software ("FLOSS") applications as described in Silverpeas's
 *  FLOSS exception.  You should have recieved a copy of the text describing
 *  the FLOSS exception, and it is also available here:
 *  "http://www.silverpeas.com/legal/licensing"
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.silverpeas.questionReply.control;

import com.silverpeas.questionReply.QuestionReplyException;
import com.silverpeas.questionReply.model.Question;
import com.silverpeas.questionReply.model.Reply;
import com.silverpeas.subscribe.SubscriptionServiceFactory;
import com.silverpeas.ui.DisplayI18NHelper;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.util.template.SilverpeasTemplateFactory;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author ehugonnet
 */
public class SubscriptionNotifier {

  private final Reply reply;
  private final String componentId;
  private final Question question;
  final NotificationSender notificationSender;
  private static final String BUNDLE_NAME = "com.silverpeas.questionReply.multilang.questionReplyBundle";
  private final String source;

  public SubscriptionNotifier(Question question, Reply reply, String componentId, String source) {
    this.componentId = componentId;
    this.reply = reply;
    this.question = question;
    this.source = source;
    this.notificationSender = new NotificationSender(componentId);
  }

  public void sendNotification() throws QuestionReplyException {
    try {
      // Get default resource bundle
      ResourceLocator message = new ResourceLocator(BUNDLE_NAME, I18NHelper.defaultLanguage);
      Map<String, SilverpeasTemplate> templates = new HashMap<String, SilverpeasTemplate>();
      NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.NORMAL,
              String.format(message.getString("questionReply.subscription.title", "Réponse à :"),
              question.getTitle()), templates, "faq_subscription");
      List<String> languages = DisplayI18NHelper.getLanguages();
      for (String language : languages) {
        message = new ResourceLocator(BUNDLE_NAME, language);
        SilverpeasTemplate template = loadTemplate();
        template.setAttribute("QuestionDetail", question);
        template.setAttribute("questionTitle", question.getTitle());
        template.setAttribute("replyTitle", reply.getTitle());
        template.setAttribute("replyContent", reply.loadWysiwygContent());
        template.setAttribute("url", question._getPermalink());
        templates.put(language, template);
        notifMetaData.addLanguage(language, String.format(message.getString(
                "questionReply.subscription.title", "Réponse à :"), question.getTitle()), "");
      }
      Collection<String> recipients = SubscriptionServiceFactory.getFactory().getSubscribeService().
              getSubscribers(new ForeignPK("0", componentId));
      if (recipients != null && !recipients.isEmpty()) {
        notifMetaData.addUserRecipients(recipients.toArray(new String[recipients.size()]));
        notifMetaData.setSource(source);
        notificationSender.notifyUser(notifMetaData);
      }
    } catch (NotificationManagerException e) {
      throw new QuestionReplyException("QuestionReplySessionController.notify()",
              SilverpeasException.ERROR, "questionReply.EX_NOTIFICATION_MANAGER_FAILED", "", e);
    }
  }

  protected SilverpeasTemplate loadTemplate() {
    ResourceLocator rs = new ResourceLocator(
            "com.silverpeas.questionReply.settings.questionReplySettings", "");
    Properties templateConfiguration = new Properties();
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR, rs.getString(
            "templatePath"));
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR, rs.getString(
            "customersTemplatePath"));
    return SilverpeasTemplateFactory.createSilverpeasTemplate(templateConfiguration);
  }
}
