/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.mailinglist.service.event;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.components.mailinglist.service.model.beans.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Event thrown for listener after checking messages on the mail server.
 * @author Emmanuel Hugonnet
 */
public class MessageEvent {

  private final List<Message> messages = new ArrayList<>(5);

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
   * @see Message
   */
  public List<Message> getMessages() {
    return this.messages;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(messages).toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final MessageEvent other = (MessageEvent) obj;
    return new EqualsBuilder().append(messages, other.messages).isEquals();
  }
}
