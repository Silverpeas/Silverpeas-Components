/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.crm.model;

import com.stratelia.webactiv.persistence.SilverpeasBean;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.util.WAPrimaryKey;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;

import java.util.List;

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

  public List<SimpleDocument> getAttachments() {
    return AttachmentServiceFactory.getAttachmentService().listDocumentsByForeignKey(
        new CrmPK("DELIVERY_" + getPK().getId(), getInstanceId()), null);
  }

  public void deleteAttachments() {
    for(SimpleDocument document : getAttachments()) {
      AttachmentServiceFactory.getAttachmentService().deleteAttachment(document);
    }
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