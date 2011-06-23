package com.silverpeas.crm.vo;

import com.silverpeas.crm.model.CrmDelivery;
import com.stratelia.silverpeas.util.ResourcesWrapper;

public class DeliveryVO extends ElementVO {
  
  private CrmDelivery delivery;

  public DeliveryVO(CrmDelivery delivery, ResourcesWrapper resources) {
    super(resources);
    this.delivery = delivery;
  }
  
  public CrmDelivery getDelivery() {
    return delivery;
  }
  
  public String getAttachments() {
    return getAttachments(delivery.getAttachments());
  }
  
  public String getOperations() {
    return getOperationLinks("Delivery", delivery.getDeliveryName(), delivery.getPK().getId());
  }
  
  public String getDeliveryDate() {
    return getDate(delivery.getDeliveryDate());
  }
  
}
