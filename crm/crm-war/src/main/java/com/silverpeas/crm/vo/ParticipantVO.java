package com.silverpeas.crm.vo;

import com.silverpeas.crm.model.CrmParticipant;
import com.stratelia.silverpeas.util.ResourcesWrapper;

public class ParticipantVO extends ElementVO {
  
  private CrmParticipant participant;

  public ParticipantVO(CrmParticipant participant, ResourcesWrapper resources) {
    super(resources);
    this.participant = participant;
  }
  
  public CrmParticipant getParticipant() {
    return participant;
  }
  
  public String getAttachments() {
    return getAttachments(participant.getAttachments());
  }
  
  public String getOperations() {
    return getOperationLinks("Participant", participant.getUserName(), participant.getPK().getId());
  }
  
  public String getActive() {
    return getActive(participant.getActive());
  }
  
}
