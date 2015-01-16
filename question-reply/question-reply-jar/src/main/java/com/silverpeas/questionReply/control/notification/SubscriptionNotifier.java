/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.questionReply.control.notification;

import com.silverpeas.questionReply.QuestionReplyException;
import com.silverpeas.questionReply.model.Question;
import com.silverpeas.questionReply.model.Reply;
import com.silverpeas.ui.DisplayI18NHelper;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.util.Link;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.util.exception.SilverpeasException;
import org.silverpeas.util.template.SilverpeasTemplate;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ehugonnet
 */
public class SubscriptionNotifier extends Notifier {

  private final Reply reply;
  private final Question question;
  final NotificationSender notificationSender;
  private static final String BUNDLE_NAME =
      "org.silverpeas.questionReply.multilang.questionReplyBundle";

  public SubscriptionNotifier(UserDetail sender, Question question, Reply reply) {
    super(sender);
    this.reply = reply;
    this.question = question;
    this.notificationSender = new NotificationSender(question.getInstanceId());
  }

  @Override
  public void sendNotification(Collection<UserRecipient> recipients) throws QuestionReplyException {
    if (recipients != null && !recipients.isEmpty()) {
      try {
        // Get default resource bundle
        ResourceLocator message = new ResourceLocator(BUNDLE_NAME, DisplayI18NHelper.getDefaultLanguage());
        Map<String, SilverpeasTemplate> templates = new HashMap<>();
        NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.NORMAL,
            String.format(message.getString("questionReply.subscription.title", "Réponse à : %1$s"),
                question.getTitle()), templates, "reply_subscription");
        List<String> languages = DisplayI18NHelper.getLanguages();
        for (String language : languages) {
          message = new ResourceLocator(BUNDLE_NAME, language);
          SilverpeasTemplate template = loadTemplate();
          template.setAttribute("userName", getSendername());
          template.setAttribute("QuestionDetail", question);
          template.setAttribute("questionTitle", question.getTitle());
          template.setAttribute("replyTitle", reply.getTitle());
          template.setAttribute("replyContent", reply.loadWysiwygContent());
          template.setAttribute("silverpeasURL", question._getPermalink());
          templates.put(language, template);
          notifMetaData.addLanguage(language, String
              .format(message.getString("questionReply.subscription.title", "Réponse à : %1$s"),
                  question.getTitle()), "");

          Link link = new Link(question._getPermalink(), message.getString("questionReply.notifLinkLabel"));
          notifMetaData.setLink(link, language);
        }
        notifMetaData.addUserRecipients(recipients);
        notifMetaData.setSender(sender.getId());

        notificationSender.notifyUser(notifMetaData);
      } catch (NotificationManagerException e) {
        throw new QuestionReplyException("QuestionReplySessionController.notify()",
            SilverpeasException.ERROR, "questionReply.EX_NOTIFICATION_MANAGER_FAILED", "", e);
      }
    }
  }
}
