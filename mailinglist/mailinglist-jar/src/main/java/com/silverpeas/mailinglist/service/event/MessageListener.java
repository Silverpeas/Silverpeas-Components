package com.silverpeas.mailinglist.service.event;

/**
 * Listener for MessageEvent.
 * 
 * @author Emmanuel Hugonnet
 * @see com.silverpeas.mailinglist.service.event.MessageEvent
 * 
 */
public interface MessageListener {
  /**
   * Method called when new messages are available.
   * 
   * @param event
   *          the message event.
   */
  public void onMessage(final MessageEvent event);

  /**
   * Returns the Silverpeas component's id. This is used to save attachements.
   * 
   * @return the Silverpeas component's id.
   */
  public String getComponentId();

  /**
   * Checks if the sender is authorized to send to this mailing list.
   * 
   * @return true if the sender is authorized - false otherwise.
   */
  public boolean checkSender(String email);
}
