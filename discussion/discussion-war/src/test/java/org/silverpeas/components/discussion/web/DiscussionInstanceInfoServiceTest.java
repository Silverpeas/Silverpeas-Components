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

import org.junit.jupiter.api.Test;
import org.silverpeas.components.discussion.service.DiscussionAccess;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.*;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class DiscussionInstanceInfoServiceTest {
  @TestManagedMock
  private OrganizationController organization;
  @TestManagedMock
  private DiscussionAccess access;
  @TestedBean
  private DiscussionInstanceInfoService service;

  @Test
  void usesTheLocalizedInstanceLabelAndDescriptionRatherThanTheComponentType() {
    var instance = mock(ComponentInstLight.class);
    when(organization.getComponentInstLight("discussion1")).thenReturn(instance);
    when(instance.getName()).thenReturn("discussion");
    when(instance.getLabel("fr")).thenReturn("Équipe produit");
    when(instance.getDescription("fr")).thenReturn("Nos échanges du quotidien");
    assertEquals("Équipe produit", service.info("discussion1", "fr").get("title"));
    assertEquals("Nos échanges du quotidien", service.info("discussion1", "fr").get("description"));
    when(instance.getDescription("fr")).thenReturn(null);
    assertEquals("", service.info("discussion1", "fr").get("description"));
  }
}
