package com.silverpeas.crm.implementation;

import java.sql.Connection;
import java.util.ArrayList;

import com.silverpeas.crm.CrmException;
import com.silverpeas.crm.model.Crm;
import com.silverpeas.crm.model.CrmContact;
import com.silverpeas.crm.model.CrmDataInterface;
import com.silverpeas.crm.model.CrmDelivery;
import com.silverpeas.crm.model.CrmEvent;
import com.silverpeas.crm.model.CrmParticipant;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.persistence.PersistenceException;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

/**
 * Class declaration
 * @author
 */
public class CrmDataManager implements CrmDataInterface {

  private SilverpeasBeanDAO crmDAO;
  private SilverpeasBeanDAO crmContactDAO;
  private SilverpeasBeanDAO crmParticipantDAO;
  private SilverpeasBeanDAO crmDeliveryDAO;
  private SilverpeasBeanDAO crmEventDAO;

  public CrmDataManager() {
    try {
      crmDAO = SilverpeasBeanDAOFactory.getDAO("com.silverpeas.crm.model.Crm");
      crmContactDAO = SilverpeasBeanDAOFactory.getDAO("com.silverpeas.crm.model.CrmContact");
      crmParticipantDAO = SilverpeasBeanDAOFactory.getDAO("com.silverpeas.crm.model.CrmParticipant");
      crmDeliveryDAO = SilverpeasBeanDAOFactory.getDAO("com.silverpeas.crm.model.CrmDelivery");
      crmEventDAO = SilverpeasBeanDAOFactory.getDAO("com.silverpeas.crm.model.CrmEvent");
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.implementation.CrmDataManager",
        SilverpeasRuntimeException.FATAL, pe.getMessage());
    }
  }

  /**
   * Create a CRM.
   */
  public void createCrm(Crm crm) {
    try {
      SilverTrace.debug("crm", "CrmDataManager.createCrm()", "crm=" + crm);
      WAPrimaryKey pk = crmDAO.add(crm);
      SilverTrace.debug("crm", "CrmDataManager.createCrm()", "add ok=");
      crm.setPK(pk);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.implementation.CrmDataManager",
        SilverpeasRuntimeException.FATAL, pe.getMessage());
    }
  }

  /**
   * Delete a CRM.
   */
  public void deleteCrm(WAPrimaryKey pk) {
    try {
      crmDAO.remove(pk);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.implementation.CrmDataManager",
        SilverpeasRuntimeException.FATAL, pe.getMessage());
    }
  }

  /**
   * Update a CRM
   */
  public void updateCrm(Crm crm) {
    try {
      crmDAO.update(crm);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.implementation.CrmDataManager",
        SilverpeasRuntimeException.FATAL, pe.getMessage());
    }
  }

  /**
   * @return A list of the CRM of the instance which id is given as parameter.
   */
  @SuppressWarnings("unchecked")
  public ArrayList<Crm> getCrms(String instanceId) {
    try {
      String clause = "instanceId = '" + instanceId + "'";
      return (ArrayList<Crm>)crmDAO.findByWhereClause(new IdPK(), clause);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.implementation.CrmDataManager",
        SilverpeasRuntimeException.FATAL, pe.getMessage());
    }
  }

  /**
   * @return The list of CRM contacts corresponding to the CRM primary key given as parameter.
   */
  @SuppressWarnings("unchecked")
  public ArrayList<CrmContact> getCrmContacts(WAPrimaryKey crmPK) {
    try {
      Crm crm = getCrm(crmPK);
      String clause = "instanceId = '" + crm.getInstanceId() + "' and crmId = " + crmPK.getId();
      return (ArrayList<CrmContact>)crmContactDAO.findByWhereClause(crmPK, clause);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.implementation.CrmDataManager",
        SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Creation d'un contact
  public void createCrmContact(CrmContact contact) {
    try {
      WAPrimaryKey pk = crmContactDAO.add(contact);
      contact.setPK(pk);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.implementation.CrmDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Suppression d'un contact
  public void deleteCrmContact(WAPrimaryKey pk, String componentId) {
    try {
      crmContactDAO.remove(pk);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.implementation.CrmDataManager",
        SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Mise a jour d'un contact
  public void updateCrmContact(CrmContact contact) {
    try {
      crmContactDAO.update(contact);
      // crmContentManager.updateSilverContentVisibility(ilp);
    } catch (Exception e) {
      throw new CrmException("com.silverpeas.crm.implementation.CrmDataManager",
          SilverpeasRuntimeException.FATAL, e.getMessage(), e);
    }
  }

  // Recuperation d'un crm par sa clef
  public Crm getCrm(WAPrimaryKey crmPK) {
    Crm crm = null;
    try {
      crm = (Crm) crmDAO.findByPrimaryKey(crmPK);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.implementation.CrmDataManager",
        SilverpeasRuntimeException.FATAL, pe.getMessage());
    }
    return crm;
  }

  // Recuperation d'un contact par sa clef
  public CrmContact getCrmContact(WAPrimaryKey contactPK) {
    CrmContact crmContact = null;
    try {
      crmContact = (CrmContact) crmContactDAO.findByPrimaryKey(contactPK);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.implementation.CrmDataManager",
        SilverpeasRuntimeException.FATAL, pe.getMessage());
    }
    return crmContact;
  }

  // Creation de la lettre par defaut a l'instanciation
  public Crm createDefaultCrm(String spaceId, String componentId) {
    com.stratelia.webactiv.beans.admin.OrganizationController oc =
        new com.stratelia.webactiv.beans.admin.OrganizationController();
    com.stratelia.webactiv.beans.admin.ComponentInst ci = oc.getComponentInst(componentId);
    Crm crm = new Crm();

    SilverTrace.debug("crm", "CrmDataManager.createCrm()",
      "spaceId=" + spaceId + " ; componentId=" + componentId + " ; ci.getLabel()=" + ci.getLabel());
    crm.setInstanceId(componentId);
    crm.setClientName(ci.getLabel());
    createCrm(crm);
    return crm;
  }

  // Recuperation d'un participant par sa clef
  public CrmParticipant getCrmParticipant(WAPrimaryKey participantPK) {
    try {
      return (CrmParticipant) crmParticipantDAO.findByPrimaryKey(participantPK);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.implementation.CrmDataManager",
        SilverpeasRuntimeException.FATAL, pe.getMessage());
    }
  }

  // Recuperation de la liste des participants
  @SuppressWarnings("unchecked")
  public ArrayList<CrmParticipant> getCrmParticipants(WAPrimaryKey crmPK) {
    try {
      Crm crm = getCrm(crmPK);
      String clause = "instanceId = '" + crm.getInstanceId() + "' and crmId = " + crmPK.getId();
      return (ArrayList<CrmParticipant>)crmParticipantDAO.findByWhereClause(crmPK, clause);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.implementation.CrmDataManager",
        SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Creation d'un participant
  public void createCrmParticipant(CrmParticipant participant) {
    try {
      WAPrimaryKey pk = crmParticipantDAO.add(participant);
      participant.setPK(pk);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.implementation.CrmDataManager",
        SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Suppression d'un participant
  public void deleteCrmParticipant(WAPrimaryKey pk, String componentId) {
    try {
      crmParticipantDAO.remove(pk);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.implementation.CrmDataManager",
        SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Mise a jour d'un participant
  public void updateCrmParticipant(CrmParticipant participant) {
    try {
      crmParticipantDAO.update(participant);
      // crmContentManager.updateSilverContentVisibility(ilp);
    } catch (Exception e) {
      throw new CrmException("com.silverpeas.crm.implementation.CrmDataManager",
        SilverpeasRuntimeException.FATAL, e.getMessage(), e);
    }
  }

  // Recuperation d'un event par sa clef
  public CrmEvent getCrmEvent(WAPrimaryKey eventPK) {
    try {
      return (CrmEvent) crmEventDAO.findByPrimaryKey(eventPK);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.implementation.CrmDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage());
    }
  }

  // Recuperation de la liste des events
  @SuppressWarnings("unchecked")
  public ArrayList<CrmEvent> getCrmEvents(WAPrimaryKey crmPK) {
    try {
      Crm crm = getCrm(crmPK);
      String clause = "instanceId = '" + crm.getInstanceId() + "' and crmId = " + crmPK.getId();
      return (ArrayList<CrmEvent>)crmEventDAO.findByWhereClause(crmPK, clause);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.implementation.CrmDataManager",
        SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Creation d'un event
  public void createCrmEvent(CrmEvent event) {
    try {
      WAPrimaryKey pk = crmEventDAO.add(event);
      event.setPK(pk);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.implementation.CrmDataManager",
        SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Suppression d'un event
  public void deleteCrmEvent(WAPrimaryKey pk, String componentId) {
    try {
      crmEventDAO.remove(pk);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.implementation.CrmDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Mise a jour d'un event
  public void updateCrmEvent(CrmEvent event) {
    try {
      crmEventDAO.update(event);
    } catch (Exception e) {
      throw new CrmException("com.silverpeas.crm.implementation.CrmDataManager",
        SilverpeasRuntimeException.FATAL, e.getMessage(), e);
    }
  }

  // Recuperation d'un delivery par sa clef
  public CrmDelivery getCrmDelivery(WAPrimaryKey deliveryPK) {
    try {
      return (CrmDelivery) crmDeliveryDAO.findByPrimaryKey(deliveryPK);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.implementation.CrmDataManager",
        SilverpeasRuntimeException.FATAL, pe.getMessage());
    }
  }

  // Recuperation de la liste des deliverys
  @SuppressWarnings("unchecked")
  public ArrayList<CrmDelivery> getCrmDeliverys(WAPrimaryKey crmPK) {
    try {
      Crm crm = getCrm(crmPK);
      String clause = "instanceId = '" + crm.getInstanceId() + "' and crmId = " + crmPK.getId();
      return (ArrayList<CrmDelivery>)crmDeliveryDAO.findByWhereClause(crmPK, clause);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.implementation.CrmDataManager",
        SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Creation d'un delivery
  public void createCrmDelivery(CrmDelivery delivery) {
    try {
      WAPrimaryKey pk = crmDeliveryDAO.add(delivery);
      delivery.setPK(pk);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.implementation.CrmDataManager",
        SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Suppression d'un delivery
  public void deleteCrmDelivery(WAPrimaryKey pk, String componentId) {
    try {
      crmDeliveryDAO.remove(pk);
    } catch (PersistenceException pe) {
      throw new CrmException("com.silverpeas.crm.implementation.CrmDataManager",
        SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Mise a jour d'un delivery
  public void updateCrmDelivery(CrmDelivery delivery) {
    try {
      crmDeliveryDAO.update(delivery);
    } catch (Exception e) {
      throw new CrmException("com.silverpeas.crm.implementation.CrmDataManager",
        SilverpeasRuntimeException.FATAL, e.getMessage(), e);
    }
  }

  /**
   * Ouverture de la connection vers la source de donnees
   * @return Connection la connection
   * @exception CrmException
   */
  public Connection openConnection() throws CrmException {
    try {
      return DBUtil.makeConnection(JNDINames.CRM_DATASOURCE);
    } catch (Exception e) {
      throw new CrmException("com.silverpeas.crm.implementation.CrmDataManager",
        SilverpeasRuntimeException.FATAL, e.getMessage());
    }
  }
}
