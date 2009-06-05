package com.silverpeas.mailinglist.service.model.beans;

import junit.framework.TestCase;

public class TestMessage extends TestCase {

  public void testHashCode() {
    Message message1 = new Message();
    message1.setId("id");
    message1.setVersion(1);    
    Message message2 = new Message();
    message2.setId("id");
    message2.setVersion(1);    
    assertEquals(message1.hashCode(), message2.hashCode());
    
    message2.setVersion(2);
    assertFalse(message1.hashCode() == message2.hashCode());
    
    message1 = new Message();
    message1.setMessageId("0000001747b40c85");
    message2 = new Message();
    message2.setMessageId("0000001747b40c85");
    assertEquals(message1, message2);
    
    message1 = new Message();
    message1.setId("id");
    message1.setVersion(1);
    message1.setMessageId("0000001747b40c85");    
    message2 = new Message();
    message2.setId("id");
    message2.setVersion(1);
    message2.setMessageId("0000001747b40c85");
    assertEquals(message1.hashCode(), message2.hashCode());
    
    message2.setVersion(2);
    assertFalse(message1.hashCode() == message2.hashCode());
    
    message2.setVersion(1);
    message2.setMessageId("0000001747b40c90");
    assertFalse(message1.hashCode() == message2.hashCode());
  }

  public void testEqualsById() {
    Message message1 = new Message();
    message1.setId("id");
    message1.setVersion(1);    
    Message message2 = new Message();
    message2.setId("id");
    message2.setVersion(1);    
    assertEquals(message1, message2);
    
    message2.setVersion(2);
    assertFalse(message1.equals(message2));
    
    message1 = new Message();
    message1.setMessageId("0000001747b40c85");
    message2 = new Message();
    message2.setMessageId("0000001747b40c85");
    assertEquals(message1, message2);
    
    message1 = new Message();
    message1.setId("id");
    message1.setVersion(1);
    message1.setMessageId("0000001747b40c85");    
    message2 = new Message();
    message2.setId("id");
    message2.setVersion(1);
    message2.setMessageId("0000001747b40c85");
    assertEquals(message1, message2);
    
    message2.setVersion(2);
    assertFalse(message1.equals(message2));
    
    message2.setVersion(1);
    message2.setMessageId("0000001747b40c90");
    assertFalse(message1.equals(message2));
  }

}
