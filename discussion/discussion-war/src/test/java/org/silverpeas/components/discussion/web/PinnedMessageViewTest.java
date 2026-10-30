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
package org.silverpeas.components.discussion.web;

import jakarta.el.BeanELResolver;
import jakarta.el.ELContext;
import org.junit.jupiter.api.Test;
import org.silverpeas.components.discussion.service.PinnedMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class PinnedMessageViewTest {
  @Test
  void pinnedMessageFieldsAreReadableByJspBeanExpressions() {
    var pin = new PinnedMessage("m1", "Bernard", "Bonjour à tous");
    var resolver = new BeanELResolver();
    var context = mock(ELContext.class);
    assertEquals("m1", resolver.getValue(context, pin, "id"));
    assertEquals("Bernard", resolver.getValue(context, pin, "author"));
    assertEquals("Bonjour à tous", resolver.getValue(context, pin, "excerpt"));
    var image = new org.silverpeas.components.discussion.service.DiscussionImage("photo", "/protected-image", "image.png");
    assertEquals("photo", resolver.getValue(context, image, "id"));
    assertEquals("/protected-image", resolver.getValue(context, image, "url"));
    assertEquals("image.png", resolver.getValue(context, image, "name"));
  }
}
