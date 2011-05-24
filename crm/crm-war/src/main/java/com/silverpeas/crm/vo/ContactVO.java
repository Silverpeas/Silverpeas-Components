package com.silverpeas.crm.vo;

import com.silverpeas.crm.model.CrmContact;
import com.stratelia.silverpeas.util.ResourcesWrapper;

public class ContactVO extends ElementVO {
  
  private CrmContact contact;
  
  public ContactVO(CrmContact contact, ResourcesWrapper resources) {
    super(resources);
    this.contact = contact;
  }
  
  public CrmContact getContact() {
    return contact;
  }
  
  public String getAttachments() {
    return getAttachments(contact.getAttachments());
  }
  
  public String getOperations() {
    return getOperationLinks("Contact", contact.getName(), contact.getPK().getId());
  }
  
  public String getActive() {
    return getActive(contact.getActive());
  }

}
