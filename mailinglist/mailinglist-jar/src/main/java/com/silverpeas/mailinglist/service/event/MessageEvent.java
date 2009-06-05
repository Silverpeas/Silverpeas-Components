package com.silverpeas.mailinglist.service.event;

import java.util.ArrayList;
import java.util.List;

import com.silverpeas.mailinglist.service.model.beans.Message;

/**
 * Event thrown for listener after checking messages on the mail server.
 * @author Emmanuel Hugonnet
 *
 */
public class MessageEvent {
	private final List<Message> messages = new ArrayList<Message>(5);
	
	public MessageEvent(){
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
