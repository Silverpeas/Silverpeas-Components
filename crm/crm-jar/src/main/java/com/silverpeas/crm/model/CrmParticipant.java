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

  public List<SimpleDocument> getAttachments() {
    return AttachmentServiceFactory.getAttachmentService().listDocumentsByForeignKey(
        new CrmPK("PARTICIPANT_" + getPK().getId(), getInstanceId()), null);
  }

  public void deleteAttachments() {
    for(SimpleDocument document : getAttachments()) {
      AttachmentServiceFactory.getAttachmentService().deleteAttachment(document);
    }
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