package com.silverpeas.crm;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.silverpeas.admin.components.ComponentsInstanciatorIntf;
import com.silverpeas.admin.components.InstanciationException;
import com.silverpeas.crm.control.ServiceFactory;
import com.silverpeas.crm.model.Crm;
import com.silverpeas.crm.model.CrmContact;
import com.silverpeas.crm.model.CrmDataInterface;
import com.silverpeas.crm.model.CrmDelivery;
import com.silverpeas.crm.model.CrmEvent;
import com.silverpeas.crm.model.CrmParticipant;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.SQLRequest;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;

/**
 * @author nesseric updated by Sébastien Antonio
 */
public class CrmInstanciator extends SQLRequest implements
    ComponentsInstanciatorIntf {

  public CrmInstanciator() {
    super("com.silverpeas.crm");
  }

  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("crm", "CrmInstanciator.create()",
        "root.MSG_GEN_ENTER_METHOD");
    SilverTrace.info("crm", "CrmInstanciator.create()",
        "root.MSG_GEN_PARAM_VALUE", "space = " + spaceId);

    CrmDataInterface dataInterface = ServiceFactory.getCrmData();
    Crm crm = dataInterface.createDefaultCrm(spaceId, componentId);
    FullIndexEntry indexEntry = new FullIndexEntry(componentId, "crm", crm.getPK().getId());
    indexEntry.setTitle(crm.getClientName());
    IndexEngineProxy.addIndexEntry(indexEntry);

    SilverTrace.info("crm", "CrmInstanciator.create()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("crm", "CrmInstanciator.delete()",
        "root.MSG_GEN_ENTER_METHOD");
    SilverTrace.info("crm", "CrmInstanciator.delete()",
        "root.MSG_GEN_PARAM_VALUE", "space = " + spaceId);

    CrmDataInterface dataInterface = ServiceFactory.getCrmData();
    ArrayList<Crm> listCrms = dataInterface.getCrms(componentId);
    Crm defaultCrm = listCrms.get(0);
    IndexEntryPK indexEntry;

    // Remove contacts attachments and indexes
    ArrayList<CrmContact> crmContacts = dataInterface.getCrmContacts(defaultCrm.getPK());
    for (CrmContact crmContact : crmContacts) {
      crmContact.deleteAttachments();
      indexEntry = new IndexEntryPK(componentId, "Contact", crmContact.getPK().getId());
      IndexEngineProxy.removeIndexEntry(indexEntry);
    }

    // Remove participants attachments and indexes
    ArrayList<CrmParticipant> crmParticipants = dataInterface.getCrmParticipants(defaultCrm.getPK());
    for (CrmParticipant crmParticipant : crmParticipants) {
      crmParticipant.deleteAttachments();
      indexEntry = new IndexEntryPK(componentId, "Participant", crmParticipant.getPK().getId());
      IndexEngineProxy.removeIndexEntry(indexEntry);
    }

    // Remove events attachments and indexes
    ArrayList<CrmEvent> crmEvents = dataInterface.getCrmEvents(defaultCrm.getPK());
    for (CrmEvent crmEvent : crmEvents) {
      crmEvent.deleteAttachments();
      indexEntry = new IndexEntryPK(componentId, "Event", crmEvent.getPK().getId());
      IndexEngineProxy.removeIndexEntry(indexEntry);
    }

    // Remove Deliverys attachments and indexes
    ArrayList<CrmDelivery> crmDeliveries = dataInterface.getCrmDeliverys(defaultCrm.getPK());
    for (CrmDelivery crmDelivery : crmDeliveries) {
      crmDelivery.deleteAttachments();
      indexEntry = new IndexEntryPK(componentId, "Delivery", crmDelivery.getPK().getId());
      IndexEngineProxy.removeIndexEntry(indexEntry);
    }

    setDeleteQueries();
    deleteDataOfInstance(con, componentId, "CRMDELIVERY");
    deleteDataOfInstance(con, componentId, "CRMEVENTS");
    deleteDataOfInstance(con, componentId, "CRMCONTACTS");
    deleteDataOfInstance(con, componentId, "CRMPARTICIPANTS");
    deleteDataOfInstance(con, componentId, "CRMINFOS");

    SilverTrace.info("crm", "CrmInstanciator.delete()", "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Delete all data of one crm instance from the crm table.
   * @param con (Connection) the connection to the data base
   * @param componentId (String) the instance id of the Silverpeas component crm.
   * @param suffixName (String) the suffix of a crm table
   */
  private void deleteDataOfInstance(Connection con, String componentId, String suffixName)
  throws InstanciationException {
    // get the delete query from the external file
    String deleteQuery = getDeleteQuery(componentId, suffixName);
    SilverTrace.info("crm", "CrmInstanciator.deleteDataOfInstance()", "root.MSG_GEN_PARAM_VALUE",
      "deleteQuery = " + deleteQuery);

    // execute the delete query
    Statement stmt = null;
    try {
      stmt = con.createStatement();
      stmt.executeUpdate(deleteQuery);
      stmt.close();
    } catch (SQLException se) {
      throw new InstanciationException("CrmInstanciator.deleteDataOfInstance()",
        SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", se);
    } finally {
      try {
        stmt.close();
      } catch (SQLException e) {
        SilverTrace.error("crm", "CrmInstanciator.deleteDataOfInstance()",
          "root.EX_RESOURCE_CLOSE_FAILED", "", e);
      }
    }
  }

}
