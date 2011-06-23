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
public class CrmContact extends SilverpeasBean implements Comparable<Object> {

  private static final long serialVersionUID = -3813998749507128070L;

  /* id du crm */
  private int crmId;

  /** id de l'instance */
  private String instanceId;

  /** nom du contact */
  private String name;

  /** fonction du contact */
  private String functionContact;

  /** tel du contact */
  private String tel;

  /** email du contact */
  private String email;

  /** adresse du contact */
  private String address;

  /** contact actif */
  private String active;

  // Constructeurs

  /**
   * Constructeur sans parametres
   */
  public CrmContact() {
    super();
    crmId = 0;
    name = "";
    functionContact = "";
    tel = "";
    email = "";
    address = "";
    active = "1";
  }

  /**
   * @param WAPrimaryKey pk
   * @param String title
   * @param String description
   * @param String parutionDate
   * @param int publicationState
   * @param String letterId
   */
  public CrmContact(WAPrimaryKey pk, String name, String functionContact, String tel, String email,
      String address, String active, int crmId) {
    super();
    setPK(pk);
    this.name = name;
    this.functionContact = functionContact;
    this.tel = tel;
    this.email = email;
    this.address = address;
    this.active = active;
    this.crmId = crmId;
  }

  // Assesseurs

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFunctionContact() {
    return functionContact;
  }

  public void setFunctionContact(String functionContact) {
    this.functionContact = functionContact;
  }

  public String getTel() {
    return tel;
  }

  public void setTel(String tel) {
    this.tel = tel;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getEmail() {
    return email;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getAddress() {
    return address;
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

  public Vector<AttachmentDetail> getAttachments() {
    return AttachmentController.searchAttachmentByPKAndContext(new CrmPK("CONTACT_" +
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
    return "SC_CRM_Contacts";
  }

}