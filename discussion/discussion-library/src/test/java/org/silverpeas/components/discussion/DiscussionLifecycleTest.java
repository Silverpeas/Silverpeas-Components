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

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.silverpeas.components.discussion.model.Conversation;
import org.silverpeas.components.discussion.repository.ConversationRepository;
import org.silverpeas.components.discussion.repository.DiscussionMessageRepository;
import org.silverpeas.components.discussion.repository.DiscussionReadStateRepository;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class DiscussionLifecycleTest {
  @TestManagedMock
  private ConversationRepository conversations;
  @TestManagedMock
  private DiscussionMessageRepository messages;
  @TestManagedMock
  private DiscussionReadStateRepository readStates;
  @org.silverpeas.core.persistence.datasource.PersistOperation
  @TestManagedMock
  private org.silverpeas.core.persistence.datasource.model.jpa.JpaPersistOperation persistOperation;
  @TestManagedMock
  private org.silverpeas.components.discussion.repository.DiscussionLikeRepository likes;
  @TestedBean
  private DiscussionInstancePostConstruction creation;
  @TestedBean
  private DiscussionInstancePreDestruction destruction;

  @Test
  void creationInitializesAnEmptyMainConversation() {
    User user = mock(User.class);
    when(user.getId()).thenReturn("user");
    try (MockedStatic<User> current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      creation.postConstruct("discussion1");
      var captured = org.mockito.ArgumentCaptor.forClass(Conversation.class);
      verify(conversations).save(captured.capture());
      assertEquals("discussion1", captured.getValue().getComponentInstanceId());
      assertEquals(0, captured.getValue().getLastSequence());
    }
  }

  @Test
  void deletionRemovesChildrenBeforeTheConversation() {
    var conversation = mock(Conversation.class);
    when(conversation.getId()).thenReturn("conversation");
    when(conversations.main("discussion1", true)).thenReturn(conversation);
    destruction.preDestroy("discussion1");
    var order = inOrder(likes, messages, readStates, conversations);
    order.verify(likes).deleteConversation("conversation");
    order.verify(messages).deleteConversation("conversation");
    order.verify(readStates).deleteConversation("conversation");
    order.verify(conversations).delete(conversation);
  }
}
