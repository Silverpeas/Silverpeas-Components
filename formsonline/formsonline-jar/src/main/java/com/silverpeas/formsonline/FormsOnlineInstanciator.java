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

package com.silverpeas.formsonline;

import com.silverpeas.admin.components.ComponentsInstanciatorIntf;
import com.silverpeas.admin.components.InstanciationException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class FormsOnlineInstanciator implements ComponentsInstanciatorIntf {

  public FormsOnlineInstanciator() {
  }

  @Override
  public void create(Connection con, String spaceId, String componentId, String userId) throws
      InstanciationException {
    SilverTrace.info("formsOnline", "FormsOnlineInstanciator.create()",
        "root.MSG_GEN_ENTER_METHOD", "space = " + spaceId + ", componentId = " + componentId +
            ", userId =" + userId);
    SilverTrace.info("formsOnline", "FormsOnlineInstanciator.create()", "root.MSG_GEN_EXIT_METHOD");
  }

  @Override
  public void delete(Connection con, String spaceId, String componentId, String userId) throws
      InstanciationException {
    SilverTrace.info("formsOnline", "FormsOnlineInstanciator.delete()",
        "root.MSG_GEN_ENTER_METHOD", "space = " + spaceId + ", componentId = " + componentId +
            ", userId =" + userId);
    deleteFormsData(con, componentId);
    deleteDataOfInstance(con, componentId, "UserRights");
    deleteDataOfInstance(con, componentId, "GroupRights");
    deleteDataOfInstance(con, componentId, "FormInstances");
    deleteDataOfInstance(con, componentId, "Forms");
    SilverTrace.info("formsOnline", "FormsOnlineInstanciator.delete()", "root.MSG_GEN_EXIT_METHOD");
  }

  private void deleteFormsData(Connection con, String componentId) throws InstanciationException {
    List<String> xmlFormNames = new ArrayList<String>();
    String query = "select distinct xmlFormName from SC_FormsOnline_Forms where instanceId = ?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.prepareStatement(query);
      stmt.setString(1, componentId);
      rs = stmt.executeQuery();
      while (rs.next()) {
        xmlFormNames.add(rs.getString("xmlFormName"));
      }
      stmt.close();
    } catch (SQLException se) {
      throw new InstanciationException("FormsOnlineInstanciator.deleteFormsData()",
          SilverpeasException.ERROR, "root.DELETING_DATA_OF_INSTANCE_FAILED", "componentId = "
              + componentId + " deleteQuery = " + query, se);
    } finally {
      try {
        stmt.close();
      } catch (SQLException err_closeStatement) {
        throw new InstanciationException("FormsOnlineInstanciator.deleteFormsData()",
            SilverpeasException.ERROR, "root.EX_RESOURCE_CLOSE_FAILED", "componentId = "
                + componentId + " deleteQuery = " + query, err_closeStatement);
      }
    }
    try {
      // delete records from each template found
      Iterator<String> it = xmlFormNames.iterator();
      while (it.hasNext()) {
        String xmlFormName = it.next();
        String xmlFormShortName =
            xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName.indexOf('.'));
        PublicationTemplateManager.getInstance().removePublicationTemplate(
            componentId + ":" + xmlFormShortName);
      }
    } catch (PublicationTemplateException e) {
      throw new InstanciationException("FormsOnlineInstanciator.deleteFormsData()",
          SilverpeasException.ERROR, "root.DELETING_DATA_OF_INSTANCE_FAILED",
          "componentId = " + componentId, e);
    }
  }

  private void deleteDataOfInstance(Connection con, String componentId, String suffix) throws
      InstanciationException {
    String query = "delete from SC_FormsOnline_" + suffix + " where instanceId = ?";
    PreparedStatement stmt = null;
    try {
      stmt = con.prepareStatement(query);
      stmt.setString(1, componentId);
      stmt.executeUpdate();
      stmt.close();
    } catch (SQLException se) {
      throw new InstanciationException("FormsOnlineInstanciator.removeInstanceData()",
          SilverpeasException.ERROR, "root.DELETING_DATA_OF_INSTANCE_FAILED", "componentId = "
              + componentId + " deleteQuery = " + query, se);
    } finally {
      try {
        stmt.close();
      } catch (SQLException err_closeStatement) {
        throw new InstanciationException("FormsOnlineInstanciator.removeInstanceData()",
            SilverpeasException.ERROR, "root.EX_RESOURCE_CLOSE_FAILED", "componentId = "
                + componentId + " deleteQuery = " + query, err_closeStatement);
      }
    }
  }
}