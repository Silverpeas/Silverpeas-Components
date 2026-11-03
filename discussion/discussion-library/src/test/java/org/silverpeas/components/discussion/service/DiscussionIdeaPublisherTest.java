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
package org.silverpeas.components.discussion.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.silverpeas.components.suggestionbox.model.SuggestionBoxService;
import org.silverpeas.components.suggestionbox.model.SuggestionCollection;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class DiscussionIdeaPublisherTest {
  @TestManagedMock private SuggestionBoxService boxes;
  @TestedBean private DiscussionIdeaPublisher publisher;

  @Test
  void nativeCollectionAddsAndPublishesAnEncodedSuggestionAsTheCurrentUser() {
    var box = mock(SuggestionBox.class);
    var collection = mock(SuggestionCollection.class);
    when(boxes.getByComponentInstanceId("suggestionBox1")).thenReturn(box);
    when(box.getSuggestions()).thenReturn(collection);
    var actor = mock(User.class);
    when(actor.getId()).thenReturn("actor");
    var result = mock(Suggestion.class);
    when(collection.publish(org.mockito.ArgumentMatchers.any())).thenReturn(result);
    var operation = mock(org.silverpeas.core.persistence.datasource.model.jpa.JpaPersistOperation.class);
    var context = mock(org.silverpeas.core.persistence.datasource.OperationContext.class);
    when(context.getPersistenceOperation(org.silverpeas.core.persistence.datasource.model.jpa.JpaPersistOperation.class)).thenReturn(operation);
    try (var users = mockStatic(User.class); var operations = mockStatic(org.silverpeas.core.persistence.datasource.OperationContext.class)) {
      operations.when(org.silverpeas.core.persistence.datasource.OperationContext::getFromCache).thenReturn(context);
      users.when(User::getCurrentRequester).thenReturn(actor);
      assertSame(result, publisher.publish(new DiscussionIdeaInput("suggestionBox1", "Titre", "<script>\nTexte", "")));
      var captured = ArgumentCaptor.forClass(Suggestion.class);
      var order = inOrder(collection);
      order.verify(collection).add(captured.capture());
      order.verify(collection).publish(captured.getValue());
      assertEquals("Titre", captured.getValue().getTitle());
      assertEquals("<p>&lt;script&gt;<br/>Texte</p>", captured.getValue().getContent());
      verify(operation).setManuallyTechnicalDataFor(org.mockito.ArgumentMatchers.eq(captured.getValue()), org.mockito.ArgumentMatchers.eq(actor), org.mockito.ArgumentMatchers.any(java.util.Date.class));
    }
  }

  @Test
  void removedTargetIsRejectedBeforeNativeCreation() {
    assertThrows(IllegalArgumentException.class, () -> publisher.publish(new DiscussionIdeaInput("suggestionBox1", "Titre", "Texte", "")));
  }
}
