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
package org.silverpeas.components.discussion.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DiscussionModelTest {
  @Test
  void aMessageHasItsOwnConversationAndSequence() {
    var message = new DiscussionMessage("conversation", 42, "  Bonjour  ");
    assertEquals("conversation", message.getConversationId());
    assertEquals(42, message.getSequenceNumber());
    assertEquals("Bonjour", message.getBody());
    message.edit("Modifié");
    assertEquals("Modifié", message.getBody());
  }

  @Test
  void emptyAndOversizedMessagesAreRejected() {
    assertThrows(IllegalArgumentException.class, () -> new DiscussionMessage("c", 1, null));
    assertThrows(IllegalArgumentException.class, () -> new DiscussionMessage("c", 1, " "));
    assertThrows(IllegalArgumentException.class,
        () -> new DiscussionMessage("c", 1, "a".repeat(2001)));
    assertEquals(2000, new DiscussionMessage("c", 1, "a".repeat(2000)).getBody().length());
  }

  @Test
  void deletionErasesTheBodyAndPreservesTheCursor() {
    var message = new DiscussionMessage("c", 42, "private content");
    message.delete();
    assertTrue(message.isDeleted());
    assertTrue(message.getBody().isBlank());
    assertEquals(42, message.getSequenceNumber());
    assertThrows(IllegalArgumentException.class, () -> message.edit("restore"));
  }

  @Test
  void readingCannotMoveBackwardOrPastTheLastMessage() {
    var read = new DiscussionReadState("c", "user");
    read.advanceTo(12, 20);
    read.advanceTo(7, 20);
    assertEquals(12, read.getLastReadSequence());
    assertThrows(IllegalArgumentException.class, () -> read.advanceTo(21, 20));
    assertThrows(IllegalArgumentException.class, () -> read.advanceTo(-1, 20));
  }

  @Test
  void conversationsHaveIndependentSequences() {
    var first = new Conversation("discussion1");
    var second = new Conversation("discussion2");
    assertEquals(1, first.nextSequence());
    assertEquals(2, first.nextSequence());
    assertEquals(1, second.nextSequence());
  }
}
