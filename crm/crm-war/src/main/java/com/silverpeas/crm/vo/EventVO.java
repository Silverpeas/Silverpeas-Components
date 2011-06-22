package com.silverpeas.crm.vo;

import com.silverpeas.crm.model.CrmEvent;
import com.stratelia.silverpeas.util.ResourcesWrapper;

public class EventVO extends ElementVO {
  
  private CrmEvent event;
  
  public EventVO(CrmEvent event, ResourcesWrapper resources) {
    super(resources);
    this.event = event;
  }
  
  public CrmEvent getEvent() {
    return event;
  }
  
  public String getAttachments() {
    return getAttachments(event.getAttachments());
  }
  
  public String getOperations() {
    return getOperationLinks("Event", event.getUserName(), event.getPK().getId());
  }
  
  public String getEventDate() {
    return getDate(event.getEventDate());
  }
  
  public String getActionDate() {
    return getDate(event.getActionDate());
  }

}
