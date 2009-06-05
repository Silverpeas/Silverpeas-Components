package com.silverpeas.mailinglist.service.job;

import com.silverpeas.mailinglist.service.event.MessageEvent;
import com.silverpeas.mailinglist.service.event.MessageListener;

public class StubMessageListener implements MessageListener {

  private MessageEvent event;

  private String componentId;

  public StubMessageListener() {
    this.componentId = "componentId";
  }

  public StubMessageListener(String componentId) {
    this.componentId = componentId;
  }

  public String getComponentId() {
    return this.componentId;
  }

  public void onMessage(MessageEvent event) {
    this.event = event;
  }

  public MessageEvent getMessageEvent() {
    return this.event;
  }

  public boolean checkSender(String email) {
    return !"marge.simpson@silverpeas.com".equals(email);
  }

}
