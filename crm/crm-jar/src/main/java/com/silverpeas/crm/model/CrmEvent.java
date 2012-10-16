/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

  public List<SimpleDocument> getAttachments() {
    return AttachmentServiceFactory.getAttachmentService().searchAttachmentsByExternalObject(
        new CrmPK("EVENT_" + getPK().getId(), getInstanceId()), null);
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
    return "SC_CRM_Events";
  }

}