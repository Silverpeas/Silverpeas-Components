/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.mailinglist.service.model.beans;

import junit.framework.TestCase;

public class TestExternalUser extends TestCase {

  public void testEquals() {
    ExternalUser user1 = new ExternalUser();
    user1.setComponentId("componentId");
    user1.setEmail("bart.simpson@silverpeas.com");
    ExternalUser user2 = new ExternalUser();
    user2.setComponentId("componentId");
    user2.setEmail("bart.simpson@silverpeas.com");
    assertEquals(user1, user2);
    user1.setId("id");
    user1.setVersion(1);
    assertEquals(user1, user2);
    user2.setComponentId("essai");
    assertFalse(user1.equals(user2));
    user2.setComponentId("componentId");
    user2.setId("id");
    user2.setVersion(1);
    assertEquals(user1, user2);
    user2.setVersion(2);
    assertEquals(user1, user2);
    user2.setId("id2");
    user2.setVersion(1);
    assertEquals(user1, user2);    
  }
  
  public void testHashCode() {
    ExternalUser user1 = new ExternalUser();
    user1.setComponentId("componentId");
    user1.setEmail("bart.simpson@silverpeas.com");
    ExternalUser user2 = new ExternalUser();
    user2.setComponentId("componentId");
    user2.setEmail("bart.simpson@silverpeas.com");
    assertEquals(user1.hashCode(), user2.hashCode());
    user1.setId("id");
    user1.setVersion(1);
    assertEquals(user1.hashCode(), user2.hashCode());
    user2.setComponentId("essai");
    assertFalse(user1.hashCode() == user2.hashCode());
    user2.setComponentId("componentId");
    user2.setId("id");
    user2.setVersion(1);
    assertEquals(user1.hashCode(), user2.hashCode());
    user2.setVersion(2);
    assertEquals(user1.hashCode(),user2.hashCode());
    user2.setId("id2");
    user2.setVersion(1);
    assertEquals(user1.hashCode(), user2.hashCode());
  }
}
