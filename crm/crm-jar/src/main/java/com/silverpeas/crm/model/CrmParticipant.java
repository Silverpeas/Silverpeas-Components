package com.silverpeas.crm.model;

import java.util.Vector;

import com.stratelia.webactiv.persistence.SilverpeasBean;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;

/**
 *
 */
public class CrmParticipant extends SilverpeasBean implements Comparable<Object> {

  private static final long serialVersionUID = 4882040982568439598L;

  /* id du crm */
  private int crmId;

  /** id de l'instance */
  private String instanceId;

  /** nom du contact */
  private String userName;

  /** fonction du contact */
  private String functionParticipant;

  /** email du contact */
  private String email;

  /** contact actif */
  private String active;

  /** id user */
  private String userId;

  public CrmParticipant() {
    super();
    crmId = 0;
    userName = "";
    functionParticipant = "";
    email = "";
    active = "1";
    userId = "";
  }

  public CrmParticipant(WAPrimaryKey pk, String name, String function, String email, String active,
      int crmId, String userId) {
    super();
    setPK(pk);
    this.userName = name;
    this.functionParticipant = function;
    this.email = email;
    this.active = active;
    this.crmId = crmId;
    this.userId = userId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String name) {
    this.userName = name;
  }

  public String getFunctionParticipant() {
    return functionParticipant;
  }

  public void setFunctionParticipant(String function) {
    this.functionParticipant = function;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getEmail() {
    return email;
  }

  public String getActive() {
    return active;
  }

  public void setActive(String active) {
    this.active = active;
  }

  public int getCrmId() {
    return crmId;
  }

  public void setCrmId(int crmId) {
    this.crmId = crmId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public Vector<AttachmentDetail> getAttachments() {
    return AttachmentController.searchAttachmentByPKAndContext(new CrmPK("PARTICIPANT_" +
        getPK().getId(), getInstanceId()), "Images");
  }

  public void deleteAttachments() {
    AttachmentController.deleteAttachment(getAttachments());
  }

  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }

  public int compareTo(Object obj) {
    if (!(obj instanceof Crm))
      return 0;
    return (String.valueOf(getPK().getId())).compareTo(String.valueOf(((Crm) obj).getPK().getId()));
  }

  public String _getTableName() {
    return "SC_CRM_Participants";
  }

}