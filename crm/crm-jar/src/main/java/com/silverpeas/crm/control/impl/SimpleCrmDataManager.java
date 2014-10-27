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

package com.silverpeas.crm.control.impl;

import com.silverpeas.crm.CrmException;
import com.silverpeas.crm.control.CrmDataManager;
import com.silverpeas.crm.model.Crm;
import com.silverpeas.crm.model.CrmContact;
import com.silverpeas.crm.model.CrmDelivery;
import com.silverpeas.crm.model.CrmEvent;
import com.silverpeas.crm.model.CrmParticipant;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.persistence.PersistenceException;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;
import org.silverpeas.core.admin.OrganizationController;
import org.silverpeas.core.admin.OrganizationControllerProvider;
import org.silverpeas.util.WAPrimaryKey;
import org.silverpeas.util.exception.SilverpeasRuntimeException;

import java.util.ArrayList;
import java.util.List;

/**
 * Class declaration
 * @author
 */
public class SimpleCrmDataManager implements CrmDataManager {

  private SilverpeasBeanDAO<Crm> crmDAO;
  private SilverpeasBeanDAO<CrmContact> crmContactDAO;
  private SilverpeasBeanDAO<CrmParticipant> crmParticipantDAO;
  private SilverpeasBeanDAO<CrmDelivery> crmDeliveryDAO;
  private SilverpeasBeanDAO<CrmEvent> crmEventDAO;

  public SimpleCrmDataManager() {
    try {
      crmDAO = SilverpeasBeanDAOFactory.getDAO("com.silverpeas.crm.model.Crm");
      crmContactDAO = SilverpeasBeanDAOFactory.getDAO("com.silverpeas.crm.model.CrmContact");
      crmParticipantDAO =
          SilverpeasBeanDAOFactory.getDAO("com.silverpeas.crm.model.CrmParticipant");
      crmDeliveryDAO = SilverpeasBeanDAOFactory.getDAO("com.silverpeas.crm.model.CrmDelivery");
      crmEventDAO = SilverpeasBeanDAOFactory.getDAO("com.silverpeas.crm.model.CrmEvent");
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.control.impl.SimpleCrmDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage());
    }
  }

  /**
   * Create a CRM.
   */
  @Override
  public void createCrm(Crm crm) {
    try {
      SilverTrace.debug("crm", "SimpleCrmDataManager.createCrm()", "crm=" + crm);
      WAPrimaryKey pk = crmDAO.add(crm);
      SilverTrace.debug("crm", "SimpleCrmDataManager.createCrm()", "add ok=");
      crm.setPK(pk);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.control.impl.SimpleCrmDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage());
    }
  }

  /**
   * Delete a CRM.
   */
  @Override
  public void deleteCrm(WAPrimaryKey pk) {
    try {
      crmDAO.remove(pk);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.control.impl.SimpleCrmDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage());
    }
  }

  /**
   * Update a CRM
   */
  @Override
  public void updateCrm(Crm crm) {
    try {
      crmDAO.update(crm);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.control.impl.SimpleCrmDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage());
    }
  }

  /**
   * @return A list of the CRM of the instance which id is given as parameter.
   */
  @SuppressWarnings("unchecked")
  public List<Crm> listAllCrms(String instanceId) {
    try {
      String clause = "instanceId = '" + instanceId + "'";
      return (ArrayList<Crm>) crmDAO.findByWhereClause(new IdPK(), clause);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.control.impl.SimpleCrmDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage());
    }
  }

  /**
   * @return The list of CRM contacts corresponding to the CRM primary key given as parameter.
   */
  @SuppressWarnings("unchecked")
  public List<CrmContact> listContactsOfCrm(WAPrimaryKey crmPK) {
    try {
      Crm crm = getCrm(crmPK);
      String clause = "instanceId = '" + crm.getInstanceId() + "' and crmId = " + crmPK.getId();
      return (ArrayList<CrmContact>) crmContactDAO.findByWhereClause(crmPK, clause);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.control.impl.SimpleCrmDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Creation d'un contact
  @Override
  public void createCrmContact(CrmContact contact) {
    try {
      WAPrimaryKey pk = crmContactDAO.add(contact);
      contact.setPK(pk);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.control.impl.SimpleCrmDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Suppression d'un contact
  @Override
  public void deleteCrmContact(WAPrimaryKey pk, String componentId) {
    try {
      crmContactDAO.remove(pk);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.control.impl.SimpleCrmDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Mise a jour d'un contact
  @Override
  public void updateCrmContact(CrmContact contact) {
    try {
      crmContactDAO.update(contact);
      // crmContentManager.updateSilverContentVisibility(ilp);
    } catch (Exception e) {
      throw new CrmException("com.silverpeas.crm.control.impl.SimpleCrmDataManager",
          SilverpeasRuntimeException.FATAL, e.getMessage(), e);
    }
  }

  // Recuperation d'un crm par sa clef
  @Override
  public Crm getCrm(WAPrimaryKey crmPK) {
    try {
      return crmDAO.findByPrimaryKey(crmPK);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.control.impl.SimpleCrmDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage());
    }
  }

  // Recuperation d'un contact par sa clef
  @Override
  public CrmContact getCrmContact(WAPrimaryKey contactPK) {
    try {
      return crmContactDAO.findByPrimaryKey(contactPK);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.control.impl.SimpleCrmDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage());
    }
  }

  // Creation de la lettre par defaut a l'instanciation
  @Override
  public Crm createDefaultCrm(String spaceId, String componentId) {
    OrganizationController oc = OrganizationControllerProvider.getOrganisationController();
    ComponentInst ci = oc.getComponentInst(componentId);
    Crm crm = new Crm();
    SilverTrace.debug("crm", "SimpleCrmDataManager.createCrm()",
        "spaceId=" + spaceId + " ; componentId=" + componentId + " ; ci.getLabel()=" +
        ci.getLabel());
    crm.setInstanceId(componentId);
    crm.setClientName(ci.getLabel());
    createCrm(crm);
    return crm;
  }

  // Recuperation d'un participant par sa clef
  @Override
  public CrmParticipant getCrmParticipant(WAPrimaryKey participantPK) {
    try {
      return crmParticipantDAO.findByPrimaryKey(participantPK);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.control.impl.SimpleCrmDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage());
    }
  }

  // Recuperation de la liste des participants
  @Override
  public List<CrmParticipant> listCrmParticipantsOfCrm(WAPrimaryKey crmPK) {
    try {
      Crm crm = getCrm(crmPK);
      String clause = "instanceId = '" + crm.getInstanceId() + "' and crmId = " + crmPK.getId();
      return (ArrayList<CrmParticipant>) crmParticipantDAO.findByWhereClause(crmPK, clause);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.control.impl.SimpleCrmDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Creation d'un participant
  @Override
  public void createCrmParticipant(CrmParticipant participant) {
    try {
      WAPrimaryKey pk = crmParticipantDAO.add(participant);
      participant.setPK(pk);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.control.impl.SimpleCrmDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Suppression d'un participant
  @Override
  public void deleteCrmParticipant(WAPrimaryKey pk, String componentId) {
    try {
      crmParticipantDAO.remove(pk);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.control.impl.SimpleCrmDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Mise a jour d'un participant
  @Override
  public void updateCrmParticipant(CrmParticipant participant) {
    try {
      crmParticipantDAO.update(participant);
    } catch (Exception e) {
      throw new CrmException("com.silverpeas.crm.control.impl.SimpleCrmDataManager",
          SilverpeasRuntimeException.FATAL, e.getMessage(), e);
    }
  }

  // Recuperation d'un event par sa clef
  @Override
  public CrmEvent getCrmEvent(WAPrimaryKey eventPK) {
    try {
      return crmEventDAO.findByPrimaryKey(eventPK);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.control.impl.SimpleCrmDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage());
    }
  }

  // Recuperation de la liste des events
  @SuppressWarnings("unchecked")
  public List<CrmEvent> listEventsOfCrm(WAPrimaryKey crmPK) {
    try {
      Crm crm = getCrm(crmPK);
      String clause = "instanceId = '" + crm.getInstanceId() + "' and crmId = " + crmPK.getId();
      return (ArrayList<CrmEvent>) crmEventDAO.findByWhereClause(crmPK, clause);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.control.impl.SimpleCrmDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Creation d'un event
  @Override
  public void createCrmEvent(CrmEvent event) {
    try {
      WAPrimaryKey pk = crmEventDAO.add(event);
      event.setPK(pk);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.control.impl.SimpleCrmDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Suppression d'un event
  @Override
  public void deleteCrmEvent(WAPrimaryKey pk, String componentId) {
    try {
      crmEventDAO.remove(pk);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.control.impl.SimpleCrmDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Mise a jour d'un event
  @Override
  public void updateCrmEvent(CrmEvent event) {
    try {
      crmEventDAO.update(event);
    } catch (Exception e) {
      throw new CrmException("com.silverpeas.crm.control.impl.SimpleCrmDataManager",
          SilverpeasRuntimeException.FATAL, e.getMessage(), e);
    }
  }

  // Recuperation d'un delivery par sa clef
  public CrmDelivery getCrmDelivery(WAPrimaryKey deliveryPK) {
    try {
      return crmDeliveryDAO.findByPrimaryKey(deliveryPK);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.control.impl.SimpleCrmDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage());
    }
  }

  // Recuperation de la liste des deliverys
  @SuppressWarnings("unchecked")
  public List<CrmDelivery> listDeliveriesOfCrm(WAPrimaryKey crmPK) {
    try {
      Crm crm = getCrm(crmPK);
      String clause = "instanceId = '" + crm.getInstanceId() + "' and crmId = " + crmPK.getId();
      return (ArrayList<CrmDelivery>) crmDeliveryDAO.findByWhereClause(crmPK, clause);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.control.impl.SimpleCrmDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Creation d'un delivery
  @Override
  public void createCrmDelivery(CrmDelivery delivery) {
    try {
      WAPrimaryKey pk = crmDeliveryDAO.add(delivery);
      delivery.setPK(pk);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.control.impl.SimpleCrmDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Suppression d'un delivery
  @Override
  public void deleteCrmDelivery(WAPrimaryKey pk, String componentId) {
    try {
      crmDeliveryDAO.remove(pk);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.control.impl.SimpleCrmDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Mise a jour d'un delivery
  public void updateCrmDelivery(CrmDelivery delivery) {
    try {
      crmDeliveryDAO.update(delivery);
    } catch (PersistenceException e) {
      throw new CrmException("com.silverpeas.crm.control.impl.SimpleCrmDataManager",
          SilverpeasRuntimeException.FATAL, e.getMessage(), e);
    }
  }

}
