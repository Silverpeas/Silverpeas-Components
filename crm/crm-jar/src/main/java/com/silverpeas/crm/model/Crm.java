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

import java.util.ArrayList;

import com.stratelia.webactiv.persistence.SilverpeasBean;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import org.silverpeas.util.WAPrimaryKey;

/**
 *
 */
public class Crm extends SilverpeasBean implements Comparable<Object> {

  private static final long serialVersionUID = -3614121345074044927L;

  /** id de l'instance */
  private String instanceId;

  /** nom du client */
  private String clientName;

  /** code projet */
  private String projectCode;

  /** contacts */
  private ArrayList<CrmContact> contacts;

  /** participants */
  private ArrayList<CrmParticipant> participants;

  /** délivrables */
  private ArrayList<CrmDelivery> delivery;

  /** evénements */
  private ArrayList<CrmEvent> events;

  public Crm() {
    super();
    instanceId = "";
    clientName = "";
    projectCode = "";
    contacts = new ArrayList<CrmContact>();
    participants = new ArrayList<CrmParticipant>();
    delivery = new ArrayList<CrmDelivery>();
    events = new ArrayList<CrmEvent>();
  }

  public Crm(WAPrimaryKey pk, String instanceId, String clientName, String projectCode,
      ArrayList<CrmContact> contacts, ArrayList<CrmParticipant> participants,
      ArrayList<CrmDelivery> delivery, ArrayList<CrmEvent> events) {
    super();
    setPK(pk);
    this.instanceId = instanceId;
    this.clientName = clientName;
    this.projectCode = projectCode;
    this.contacts = contacts;
    this.participants = participants;
    this.delivery = delivery;
    this.events = events;
  }

  // Accesseurs

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getClientName() {
    return clientName;
  }

  public void setClientName(String clientName) {
    this.clientName = clientName;
  }

  public String getProjectCode() {
    return projectCode;
  }

  public void setProjectCode(String projectCode) {
    this.projectCode = projectCode;
  }

  public ArrayList<CrmContact> readContatcs() {
    return contacts;
  }

  public void writeContatcs(ArrayList<CrmContact> contacts) {
    this.contacts = contacts;
  }

  public ArrayList<CrmParticipant> readParticipants() {
    return participants;
  }

  public void writeParticipants(ArrayList<CrmParticipant> participants) {
    this.participants = participants;
  }

  public ArrayList<CrmDelivery> readDelivery() {
    return delivery;
  }

  public void writeDelivery(ArrayList<CrmDelivery> delivery) {
    this.delivery = delivery;
  }

  public ArrayList<CrmEvent> readEvents() {
    return events;
  }

  public void writeEvents(ArrayList<CrmEvent> events) {
    this.events = events;
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
    return "SC_CRM_Infos";
  }

}
