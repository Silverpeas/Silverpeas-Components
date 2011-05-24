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
public class CrmEvent extends SilverpeasBean implements Comparable<Object> {

  private static final long serialVersionUID = -7186044216193294926L;

  /* id du crm */
  private int crmId;

  /** id de l'instance */
  private String instanceId;

  /** date de l'événement */
  private String eventDate;

  /** événement */
  private String eventLib;

  /** action é déclencher */
  private String actionTodo;

  /** personne en action */
  private String userId;

  /** personne en action */
  private String userName;

  /** date de l'action */
  private String actionDate;

  /** état de l'événement */
  private String state;

  // Constructeurs

  /**
   * Constructeur sans parametres
   */
  public CrmEvent() {
    super();
    crmId = 0;
    eventDate = "";
    eventLib = "";
    actionTodo = "";
    userId = "";
    userName = "";
    actionDate = "";
    state = "";
  }

  /**
   * Constructeur
   */
  public CrmEvent(WAPrimaryKey pk, String eventDate, String eventLib, String actionTodo,
      String userId, String userName, String actionDate, String state, int crmId) {
    super();
    setPK(pk);
    this.eventDate = eventDate;
    this.eventLib = eventLib;
    this.actionTodo = actionTodo;
    this.userId = userId;
    this.userName = userName;
    this.actionDate = actionDate;
    this.state = state;
    this.crmId = crmId;
  }

  // Assesseurs

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getEventDate() {
    return eventDate;
  }

  public void setEventDate(String eventDate) {
    this.eventDate = eventDate;
  }

  public String getEventLib() {
    return eventLib;
  }

  public void setEventLib(String eventLib) {
    this.eventLib = eventLib;
  }

  public String getActionTodo() {
    return actionTodo;
  }

  public void setActionTodo(String actionTodo) {
    this.actionTodo = actionTodo;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public void setActionDate(String actionDate) {
    this.actionDate = actionDate;
  }

  public String getActionDate() {
    return actionDate;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getState() {
    return state;
  }

  public int getCrmId() {
    return crmId;
  }

  public void setCrmId(int crmId) {
    this.crmId = crmId;
  }

  public Vector<AttachmentDetail> getAttachments() {
    return AttachmentController.searchAttachmentByPKAndContext(
      new CrmPK("EVENT_" + getPK().getId(), getInstanceId()), "Images");
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
    return "SC_CRM_Events";
  }

}