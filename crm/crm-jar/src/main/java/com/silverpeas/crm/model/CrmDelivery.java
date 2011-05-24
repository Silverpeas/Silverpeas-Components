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
public class CrmDelivery extends SilverpeasBean implements Comparable<Object> {

  private static final long serialVersionUID = 444711213520041667L;

  /* id du crm */
  private int crmId;

  /** id de l'instance */
  private String instanceId;

  /** date du livrable */
  private String deliveryDate;

  /** élément livré */
  private String element;

  /** version livrée */
  private String version;

  /** personne qui a livré */
  private String deliveryId;

  /** personne qui a livré */
  private String deliveryName;

  /** personne qui a receptionné */
  private int contactId;

  /** personne qui a receptionné */
  private String contactName;

  /** media de livraison */
  private String media;

  // Constructeurs

  /**
   * Constructeur sans parametres
   */
  public CrmDelivery() {
    super();
    crmId = 0;
    deliveryDate = "";
    element = "";
    version = "";
    deliveryId = "";
    deliveryName = "";
    contactId = 0;
    contactName = "";
    media = "";
  }

  /**
   * Constructeur
   */
  public CrmDelivery(WAPrimaryKey pk, String deliveryDate, String element, String version,
      String deliveryId, String deliveryName, int contactId, String contactName, String media,
      int crmId) {
    super();
    setPK(pk);
    this.deliveryDate = deliveryDate;
    this.element = element;
    this.version = version;
    this.deliveryId = deliveryId;
    this.deliveryName = deliveryName;
    this.contactId = contactId;
    this.contactName = contactName;
    this.media = media;
    this.crmId = crmId;
  }

  // Accesseurs

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getDeliveryDate() {
    return deliveryDate;
  }

  public void setDeliveryDate(String deliveryDate) {
    this.deliveryDate = deliveryDate;
  }

  public String getElement() {
    return element;
  }

  public void setElement(String element) {
    this.element = element;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getDeliveryId() {
    return deliveryId;
  }

  public String getDeliveryName() {
    return deliveryName;
  }

  public void setDeliveryName(String deliveryName) {
    this.deliveryName = deliveryName;
  }

  public void setDeliveryId(String deliveryId) {
    this.deliveryId = deliveryId;
  }

  public void setContactId(int contactId) {
    this.contactId = contactId;
  }

  public void setContactName(String contactName) {
    this.contactName = contactName;
  }

  public int getContactId() {
    return contactId;
  }

  public String getContactName() {
    return contactName;
  }

  public void setMedia(String media) {
    this.media = media;
  }

  public String getMedia() {
    return media;
  }

  public int getCrmId() {
    return crmId;
  }

  public void setCrmId(int crmId) {
    this.crmId = crmId;
  }

  public Vector<AttachmentDetail> getAttachments() {
    return AttachmentController.searchAttachmentByPKAndContext(
      new CrmPK("DELIVERY_" + getPK().getId(), getInstanceId()), "Images");
  }

  public void deleteAttachments() {
    AttachmentController.deleteAttachment(getAttachments());
  }

  public String toString() {
    return new StringBuffer()
      .append("crmId=" + crmId)
      .append("deliveryDate=" + deliveryDate)
      .append("element=" + element)
      .append("version=" + version)
      .append("deliveryId=" + deliveryId)
      .append("deliveryName=" + deliveryName)
      .append("contactId=" + contactId)
      .append("contactName=" + contactName)
      .append("media=" + media)
      .toString();
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
    return "SC_CRM_Delivery";
  }

}