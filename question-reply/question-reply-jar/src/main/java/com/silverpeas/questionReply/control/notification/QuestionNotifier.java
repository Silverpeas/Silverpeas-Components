/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.silverpeas.questionReply.control.notification;

import com.silverpeas.questionReply.QuestionReplyException;
import com.silverpeas.questionReply.model.Question;
import com.silverpeas.ui.DisplayI18NHelper;
import org.silverpeas.util.StringUtil;
import org.silverpeas.util.template.SilverpeasTemplate;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.util.exception.SilverpeasException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ehugonnet
 */
public class QuestionNotifier extends Notifier {

  final Question question;
  final String componentLabel;
  final String subject;
  final String source;
  final NotificationSender notifSender;

  public QuestionNotifier(UserDetail sender, String serverUrl, Question question,
          NotificationData data) {
    super(sender, serverUrl);
    this.question = question;
    this.componentLabel = data.getComponentLabel();
    this.subject = data.getSubject();
    this.source = data.getSource();
    this.notifSender = new NotificationSender(data.getComponentId());
  }

  /**
   * @param question the current question-reply question
   * @param users list of users to notify
   * @throws QuestionReplyException
   */
  @Override
  public void sendNotification(Collection<UserRecipient> users) throws QuestionReplyException {
    try {
      // Get default resource bundle
      String resource = "com.silverpeas.questionReply.multilang.questionReplyBundle";
      ResourceLocator message;
      // Initialize templates
      Map<String, SilverpeasTemplate> templates = new HashMap<String, SilverpeasTemplate>();
      NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.NORMAL,
              subject, templates, "question");

      List<String> languages = DisplayI18NHelper.getLanguages();
      for (String language : languages) {
        // initialize new resource locator
        message = new ResourceLocator(resource, language);
        // Create a new silverpeas template
        SilverpeasTemplate template = loadTemplate();
        template.setAttribute("UserDetail", sender);
        template.setAttribute("userName", getSendername());
        template.setAttribute("QuestionDetail", question);
        template.setAttribute("questionTitle", question.getTitle());
        template.setAttribute("questionContent", question.getContent());
        if (StringUtil.isDefined(serverUrl)) {
          template.setAttribute("url", serverUrl + question._getPermalink());
        } else {
          template.setAttribute("url", question._getPermalink());
        }
        templates.put(language, template);
        notifMetaData.addLanguage(language, message.getString("questionReply.notification", "")
                + componentLabel, "");
      }
      notifMetaData.setSender(sender.getId());
      notifMetaData.addUserRecipients(users);
      if (StringUtil.isDefined(serverUrl)) {
        notifMetaData.setLink(serverUrl + question._getPermalink());
      } else {
        notifMetaData.setLink(question._getPermalink());
      }
      notifSender.notifyUser(notifMetaData);
    } catch (NotificationManagerException e) {
      throw new QuestionReplyException("QuestionReplySessionController.notify()",
              SilverpeasException.ERROR, "questionReply.EX_NOTIFICATION_MANAGER_FAILED", "", e);
    }
  }
}
