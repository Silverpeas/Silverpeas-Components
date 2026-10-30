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

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.security.authorization.ComponentAccessController;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class DiscussionAccessTest {
  @TestManagedMock
  private ComponentAccessController controller;
  @TestedBean
  private DiscussionAccess access;

  @Test
  void inaccessibleInstancesAreDeniedEvenWithAWriterProfile() {
    User user = mock(User.class);
    when(user.getId()).thenReturn("user");
    try (MockedStatic<User> current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      assertThrows(SecurityException.class, () -> access.require("discussion1", SilverpeasRole.READER));
    }
  }

  @Test
  void roleAndGuestStatusAreCheckedOnEveryOperation() {
    User user = mock(User.class);
    when(user.getId()).thenReturn("user");
    when(controller.isUserAuthorized("user", "discussion1")).thenReturn(true);
    when(controller.getUserRoles(eq("user"), eq("discussion1"), any()))
        .thenReturn(Set.of(SilverpeasRole.READER));
    try (MockedStatic<User> current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      assertThrows(SecurityException.class, () -> access.require("discussion1", SilverpeasRole.READER));
      assertThrows(SecurityException.class, () -> access.require("discussion1", SilverpeasRole.WRITER));
      when(controller.getUserRoles(eq("user"), eq("discussion1"), any()))
          .thenReturn(Set.of(SilverpeasRole.WRITER));
      assertEquals(SilverpeasRole.WRITER, access.require("discussion1", SilverpeasRole.WRITER));
      when(user.isAccessGuest()).thenReturn(true);
      assertThrows(SecurityException.class, () -> access.require("discussion1", SilverpeasRole.WRITER));
    }
  }
}
