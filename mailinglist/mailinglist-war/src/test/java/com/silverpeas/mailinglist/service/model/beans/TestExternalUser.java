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
