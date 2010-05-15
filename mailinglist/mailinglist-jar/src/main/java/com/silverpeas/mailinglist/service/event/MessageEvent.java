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

package com.silverpeas.mailinglist.service.event;

import java.util.ArrayList;
import java.util.List;

import com.silverpeas.mailinglist.service.model.beans.Message;

/**
 * Event thrown for listener after checking messages on the mail server.
 * @author Emmanuel Hugonnet
 */
public class MessageEvent {
  private final List<Message> messages = new ArrayList<Message>(5);

  public MessageEvent() {
  }

  /**
   * Adds a message to the list of messages.
   * @param message the message to be added.
   */
  public void addMessage(final Message message) {
    this.messages.add(message);
  }

  /**
   * Returns a list of com.silverpeas.mailinglist.model.Message.
   * @return a list of com.silverpeas.mailinglist.model.Message.
   * @see com.silverpeas.mailinglist.service.model.beans.Message
   */
  public List<Message> getMessages() {
    return this.messages;
  }

  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = prime * result + ((messages == null) ? 0 : messages.hashCode());
    return result;
  }

  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final MessageEvent other = (MessageEvent) obj;
    if (messages == null) {
      if (other.messages != null)
        return false;
    } else if (!messages.equals(other.messages))
      return false;
    return true;
  }
}
