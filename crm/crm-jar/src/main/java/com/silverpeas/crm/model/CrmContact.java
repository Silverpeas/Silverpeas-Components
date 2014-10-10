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
import org.silverpeas.util.WAPrimaryKey;
import org.silverpeas.attachment.AttachmentServiceProvider;
import org.silverpeas.attachment.model.SimpleDocument;

import java.util.List;

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
   *
   * @param pk
   * @param name
   * @param functionContact
   * @param tel
   * @param email
   * @param address
   * @param active
   * @param crmId
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

  public List<SimpleDocument> getAttachments() {
    return AttachmentServiceProvider.getAttachmentService().listDocumentsByForeignKey(
        new CrmPK("CONTACT_" + getPK().getId(), getInstanceId()), null);
  }

  public void deleteAttachments() {
    for(SimpleDocument document : getAttachments()) {
      AttachmentServiceProvider.getAttachmentService().deleteAttachment(document);
    }
  }

  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }

  public int compareTo(Object obj) {
    if (!(obj instanceof Crm))  {
      return 0;
    }
    return (String.valueOf(getPK().getId())).compareTo(String.valueOf(((Crm) obj).getPK().getId()));
  }

  public String _getTableName() {
    return "SC_CRM_Contacts";
  }

}