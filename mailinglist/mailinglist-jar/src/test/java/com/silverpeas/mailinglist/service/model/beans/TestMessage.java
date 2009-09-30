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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
