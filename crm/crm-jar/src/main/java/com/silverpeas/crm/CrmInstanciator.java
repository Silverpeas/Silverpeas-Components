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

package com.silverpeas.crm;

import com.silverpeas.admin.components.ComponentsInstanciatorIntf;
import com.silverpeas.admin.components.InstanciationException;
import com.silverpeas.crm.control.CrmDataManager;
import com.silverpeas.crm.control.ServiceFactory;
import com.silverpeas.crm.model.Crm;
import com.silverpeas.crm.model.CrmContact;
import com.silverpeas.crm.model.CrmDelivery;
import com.silverpeas.crm.model.CrmEvent;
import com.silverpeas.crm.model.CrmParticipant;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.SQLRequest;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;
import org.silverpeas.search.indexEngine.model.IndexEngineProxy;
import org.silverpeas.search.indexEngine.model.IndexEntryPK;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author nesseric updated by Sébastien Antonio
 */
public class CrmInstanciator extends SQLRequest implements ComponentsInstanciatorIntf {

  public CrmInstanciator() {
    super("com.silverpeas.crm");
  }

  @Override
  public void create(Connection con, String spaceId, String componentId, String userId) throws
      InstanciationException {
    SilverTrace.info("crm", "CrmInstanciator.create()", "root.MSG_GEN_ENTER_METHOD");
    SilverTrace.info("crm", "CrmInstanciator.create()", "root.MSG_GEN_PARAM_VALUE",
        "space = " + spaceId);
    CrmDataManager dataManager = ServiceFactory.getCrmData();
    Crm crm = dataManager.createDefaultCrm(spaceId, componentId);
    FullIndexEntry indexEntry = new FullIndexEntry(componentId, "crm", crm.getPK().getId());
    indexEntry.setTitle(crm.getClientName());
    IndexEngineProxy.addIndexEntry(indexEntry);

    SilverTrace.info("crm", "CrmInstanciator.create()", "root.MSG_GEN_EXIT_METHOD");
  }

  @Override
  public void delete(Connection con, String spaceId, String componentId, String userId) throws
      InstanciationException {
    SilverTrace.info("crm", "CrmInstanciator.delete()", "root.MSG_GEN_ENTER_METHOD");
    SilverTrace.info("crm", "CrmInstanciator.delete()", "root.MSG_GEN_PARAM_VALUE",
        "space = " + spaceId);

    CrmDataManager dataManager = ServiceFactory.getCrmData();
    List<Crm> listCrms = dataManager.listAllCrms(componentId);
    Crm defaultCrm = listCrms.get(0);

    // Remove contacts attachments and indexes
    List<CrmContact> crmContacts = dataManager.listContactsOfCrm(defaultCrm.getPK());
    for (CrmContact crmContact : crmContacts) {
      crmContact.deleteAttachments();
      IndexEntryPK indexEntry =
          new IndexEntryPK(componentId, "Contact", crmContact.getPK().getId());
      IndexEngineProxy.removeIndexEntry(indexEntry);
    }

    // Remove participants attachments and indexes
    List<CrmParticipant> crmParticipants = dataManager.listCrmParticipantsOfCrm(defaultCrm.getPK());
    for (CrmParticipant crmParticipant : crmParticipants) {
      crmParticipant.deleteAttachments();
      IndexEntryPK indexEntry =
          new IndexEntryPK(componentId, "Participant", crmParticipant.getPK().getId());
      IndexEngineProxy.removeIndexEntry(indexEntry);
    }

    // Remove events attachments and indexes
    List<CrmEvent> crmEvents = dataManager.listEventsOfCrm(defaultCrm.getPK());
    for (CrmEvent crmEvent : crmEvents) {
      crmEvent.deleteAttachments();
      IndexEntryPK indexEntry = new IndexEntryPK(componentId, "Event", crmEvent.getPK().getId());
      IndexEngineProxy.removeIndexEntry(indexEntry);
    }

    // Remove Deliverys attachments and indexes
    List<CrmDelivery> crmDeliveries = dataManager.listDeliveriesOfCrm(defaultCrm.getPK());
    for (CrmDelivery crmDelivery : crmDeliveries) {
      crmDelivery.deleteAttachments();
      IndexEntryPK indexEntry =
          new IndexEntryPK(componentId, "Delivery", crmDelivery.getPK().getId());
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
