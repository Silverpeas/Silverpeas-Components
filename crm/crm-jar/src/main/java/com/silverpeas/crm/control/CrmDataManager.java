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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.crm.control;

import com.silverpeas.crm.model.Crm;
import com.silverpeas.crm.model.CrmContact;
import com.silverpeas.crm.model.CrmDelivery;
import com.silverpeas.crm.model.CrmEvent;
import com.silverpeas.crm.model.CrmParticipant;
import com.stratelia.webactiv.util.WAPrimaryKey;

import java.util.List;

/**
 * Interface declaration
 * @author
 */
public interface CrmDataManager {

  /**
   * create a default CRM during instanciation.
   * @param spaceId
   * @param componentId
   * @return
   */
  public Crm createDefaultCrm(String spaceId, String componentId);

  /**
   * Create a new CRM.
   * @param crm the crm to store.
   */
  public void createCrm(Crm crm);

  // Suppression d'un crm
  public void deleteCrm(WAPrimaryKey pk);

  // Mise a jour d'un crm
  public void updateCrm(Crm crm);

  // Recuperation d'un crm par sa clef
  public Crm getCrm(WAPrimaryKey crmPK);

  // Recuperation de la liste des crm
  public List<Crm> listAllCrms(String instanceId);

  // Recuperation de la liste des contatcs
  public List<CrmContact> listContactsOfCrm(WAPrimaryKey crmPK);

  // Creation d'un contact
  public void createCrmContact(CrmContact contact);

  // Suppression d'un contact
  public void deleteCrmContact(WAPrimaryKey pk, String componentId);

  // Mise a jour d'un contact
  public void updateCrmContact(CrmContact contact);

  // Recuperation d'un contact par sa clef
  public CrmContact getCrmContact(WAPrimaryKey contactPK);

  // Recuperation de la liste participants
  public List<CrmParticipant> listCrmParticipantsOfCrm(WAPrimaryKey crmPK);

  // Recuperation d'un participant par sa clef
  public CrmParticipant getCrmParticipant(WAPrimaryKey participantPK);

  // Creation d'un participant
  public void createCrmParticipant(CrmParticipant participant);

  // Suppression d'un participant
  public void deleteCrmParticipant(WAPrimaryKey participantPK, String componentId);

  // Mise a jour d'un contact
  public void updateCrmParticipant(CrmParticipant participant);

  // Recuperation d'un événement par sa clef
  public CrmDelivery getCrmDelivery(WAPrimaryKey deliveryPK);

  // Recuperation de la liste délivrables
  public List<CrmDelivery> listDeliveriesOfCrm(WAPrimaryKey crmPK);

  // Creation d'un délivrable
  public void createCrmDelivery(CrmDelivery delivery);

  // Suppression d'un délivrable
  public void deleteCrmDelivery(WAPrimaryKey deliveryPK, String componentId);

  // Mise a jour d'un délivrable
  public void updateCrmDelivery(CrmDelivery delivery);

  // Recuperation d'un événement par sa clef
  public CrmEvent getCrmEvent(WAPrimaryKey eventPK);

  // Recuperation de la liste événements
  public List<CrmEvent> listEventsOfCrm(WAPrimaryKey crmPK);

  // Creation d'un événement
  public void createCrmEvent(CrmEvent event);

  // Suppression d'un événement
  public void deleteCrmEvent(WAPrimaryKey eventPK, String componentId);

  // Mise a jour d'un événement
  public void updateCrmEvent(CrmEvent event);

}
