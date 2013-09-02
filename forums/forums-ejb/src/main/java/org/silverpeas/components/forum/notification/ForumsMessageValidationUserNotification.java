/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.forum.notification;

import com.silverpeas.util.template.SilverpeasTemplate;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.webactiv.forums.models.Message;

import java.util.Collection;
import java.util.Collections;

import static com.silverpeas.util.StringUtil.isDefined;

/**
 * User: Yohann Chastagnier
 * Date: 10/06/13
 */
public class ForumsMessageValidationUserNotification extends AbstractForumsMessageUserNotification {

  private final String moderatorId;
  private final String refusalMotive;

  /**
   * Default constructor.
   * @param resource
   * @param moderatorId
   */
  public ForumsMessageValidationUserNotification(final Message resource, final String moderatorId) {
    this(resource, moderatorId, null);
  }

  /**
   * Default constructor.
   * @param resource
   * @param moderatorId
   * @param refusalMotive
   */
  public ForumsMessageValidationUserNotification(final Message resource, final String moderatorId,
      final String refusalMotive) {
    super(resource);
    this.moderatorId = moderatorId;
    this.refusalMotive = refusalMotive;
  }

  @Override
  protected String getBundleSubjectKey() {
    StringBuilder subjectKey = new StringBuilder(getNotificationBundleKeyPrefix());
    if (NotifAction.VALIDATE.equals(getAction())) {
      subjectKey.append("subject.validated");
    } else {
      subjectKey.append("subject.refused");
    }
    return subjectKey.toString();
  }

  @Override
  protected String getFileName() {
    if (NotifAction.VALIDATE.equals(getAction())) {
      return "messageValidated";
    }
    return "messageRefused";
  }

  @Override
  protected void perform(final Message resource) {
    super.perform(resource);
    getNotificationMetaData().setOriginalExtraMessage(refusalMotive);
  }

  @Override
  protected void performTemplateData(final String language, final Message resource,
      final SilverpeasTemplate template) {
    super.performTemplateData(language, resource, template);
    template.setAttribute("refusalMotive", refusalMotive);
  }

  @Override
  protected NotifAction getAction() {
    if (!isDefined(refusalMotive)) {
      return NotifAction.VALIDATE;
    }
    return NotifAction.REFUSE;
  }

  @Override
  protected String getSender() {
    return moderatorId;
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return Collections.singletonList(getResource().getAuthor());
  }

  @Override
  protected boolean isSendImmediatly() {
    return true;
  }
}
