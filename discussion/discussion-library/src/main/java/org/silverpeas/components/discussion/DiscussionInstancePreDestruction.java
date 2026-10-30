/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.components.discussion;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import org.silverpeas.components.discussion.model.Conversation;
import org.silverpeas.components.discussion.repository.ConversationRepository;
import org.silverpeas.components.discussion.repository.DiscussionLikeRepository;
import org.silverpeas.components.discussion.repository.DiscussionMessageRepository;
import org.silverpeas.components.discussion.repository.DiscussionReadStateRepository;
import org.silverpeas.core.admin.component.ComponentInstancePreDestruction;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.kernel.annotation.Technical;

@Technical
@Bean
@Named
public class DiscussionInstancePreDestruction implements ComponentInstancePreDestruction {
  @Inject
  private ConversationRepository conversations;
  @Inject
  private DiscussionMessageRepository messages;
  @Inject
  private DiscussionReadStateRepository readStates;

  @Inject
  private DiscussionLikeRepository likes;

  @Inject
  private org.silverpeas.components.discussion.repository.DiscussionSeenRepository seen;

  @Override
  @Transactional
  public void preDestroy(String instanceId) {
    Conversation conversation = conversations.main(instanceId, true);
    seen.deleteConversation(conversation.getId());
    likes.deleteConversation(conversation.getId());
    messages.deleteConversation(conversation.getId());
    readStates.deleteConversation(conversation.getId());
    conversations.delete(conversation);
  }
}
