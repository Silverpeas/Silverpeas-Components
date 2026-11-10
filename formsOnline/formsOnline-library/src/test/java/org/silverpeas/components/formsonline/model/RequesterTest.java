/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 */
package org.silverpeas.components.formsonline.model;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.silverpeas.core.admin.user.model.UserFull;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.silverpeas.components.formsonline.model.Requester.BOSS_FIELDNAME;
import static org.silverpeas.components.formsonline.model.Requester.DELEGATE_APPROVER_FIELDNAME;

class RequesterTest {

  @Test
  void missingRequesterReturnsNull() {
    try (MockedStatic<UserFull> users = mockStatic(UserFull.class)) {
      users.when(() -> UserFull.getById("missing")).thenReturn(null);
      assertNull(Requester.getById("missing"));
      assertNull(Requester.getById(null));
      assertNull(Requester.getById(" "));
    }
  }

  @Test
  void requesterWithoutBossHasNoValidator() {
    final UserFull requester = mockUser("requester");
    when(requester.getValue(BOSS_FIELDNAME)).thenReturn(null);
    assertEquals("", validatorFor(requester));
    when(requester.getValue(BOSS_FIELDNAME)).thenReturn(" ");
    assertEquals("", validatorFor(requester));
  }

  @Test
  void missingBossHasNoValidator() {
    final UserFull requester = mockUser("requester");
    when(requester.getValue(BOSS_FIELDNAME)).thenReturn("missing");
    try (MockedStatic<UserFull> users = mockStatic(UserFull.class)) {
      users.when(() -> UserFull.getById("requester")).thenReturn(requester);
      users.when(() -> UserFull.getById("missing")).thenReturn(null);
      assertEquals("", Requester.getById("requester").getRequestValidator());
    }
  }

  @Test
  void bossWithoutDelegateIsValidator() {
    final UserFull requester = mockUser("requester");
    final UserFull boss = mockUser("boss");
    when(requester.getValue(BOSS_FIELDNAME)).thenReturn("boss");
    when(boss.getValue(DELEGATE_APPROVER_FIELDNAME)).thenReturn(null);
    when(boss.getAllDefinedValues(null)).thenReturn(Collections.emptyMap());
    assertEquals("boss", validatorFor(requester, boss, null));
  }

  @Test
  void validDomainDelegateIsValidator() {
    final UserFull requester = mockUser("requester");
    final UserFull boss = mockUser("boss");
    final UserFull delegate = mockUser("delegate");
    when(requester.getValue(BOSS_FIELDNAME)).thenReturn("boss");
    when(boss.getValue(DELEGATE_APPROVER_FIELDNAME)).thenReturn("delegate");
    assertEquals("delegate", validatorFor(requester, boss, delegate));
  }

  @Test
  void missingDomainDelegateFallsBackToBoss() {
    final UserFull requester = mockUser("requester");
    final UserFull boss = mockUser("boss");
    when(requester.getValue(BOSS_FIELDNAME)).thenReturn("boss");
    when(boss.getValue(DELEGATE_APPROVER_FIELDNAME)).thenReturn("missing");
    assertEquals("boss", validatorFor(requester, boss, null));
  }

  @Test
  void emptyDomainDelegateFallsBackToBoss() {
    final UserFull requester = mockUser("requester");
    final UserFull boss = mockUser("boss");
    when(requester.getValue(BOSS_FIELDNAME)).thenReturn("boss");
    when(boss.getValue(DELEGATE_APPROVER_FIELDNAME)).thenReturn("");
    when(boss.getAllDefinedValues(null)).thenReturn(Map.of(DELEGATE_APPROVER_FIELDNAME, ""));
    assertEquals("boss", validatorFor(requester, boss, null));
  }

  private static UserFull mockUser(final String id) {
    final UserFull user = mock(UserFull.class);
    when(user.getId()).thenReturn(id);
    return user;
  }

  private static String validatorFor(final UserFull requester) {
    try (MockedStatic<UserFull> users = mockStatic(UserFull.class)) {
      users.when(() -> UserFull.getById("requester")).thenReturn(requester);
      return Requester.getById("requester").getRequestValidator();
    }
  }

  private static String validatorFor(final UserFull requester, final UserFull boss,
      final UserFull delegate) {
    try (MockedStatic<UserFull> users = mockStatic(UserFull.class)) {
      users.when(() -> UserFull.getById("requester")).thenReturn(requester);
      users.when(() -> UserFull.getById("boss")).thenReturn(boss);
      if (delegate != null) {
        users.when(() -> UserFull.getById("delegate")).thenReturn(delegate);
      }
      return Requester.getById("requester").getRequestValidator();
    }
  }
}
