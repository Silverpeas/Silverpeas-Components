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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.questionreply.service.notification;

import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.components.questionreply.model.Reply;
import org.silverpeas.core.admin.user.model.User;

import java.util.Collection;

import static java.util.Collections.singleton;

/**
 * @author ehugonnet
 */
public class ReplyNotifier extends AbstractReplyNotifier {

  private final String recipientId;

  public ReplyNotifier(User sender, Question question, Reply reply, String recipientId) {
    super(question, reply, sender);
    this.recipientId = recipientId;
  }

  @Override
  protected String getTemplateFileName() {
    return "reply";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return singleton(recipientId);
  }
}
