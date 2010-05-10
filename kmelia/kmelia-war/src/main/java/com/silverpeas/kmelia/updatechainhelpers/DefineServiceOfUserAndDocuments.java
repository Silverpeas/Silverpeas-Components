/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.kmelia.updatechainhelpers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.ClassifyValue;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.Value;
import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

public class DefineServiceOfUserAndDocuments extends UpdateChainHelperImpl {

  public void execute(UpdateChainHelperContext uchc) {
    KmeliaSessionController kmeliaScc = uchc.getKmeliaScc();

    // récupération des données
    PublicationDetail pubDetail = uchc.getPubDetail();

    // Recherche du service et du matricule de l'utilisateur

    String userName = pubDetail.getName();
    String lastName = getField("lastname", userName);
    String firstName = getField("firstname", userName);
    String service = getField("service", userName);
    String matricule = getField("matricule", userName);

    // associer le service au node
    String[] topics = new String[1];
    List<NodeDetail> allTopics = uchc.getAllTopics();
    Iterator<NodeDetail> it = allTopics.iterator();
    while (it.hasNext()) {
      NodeDetail node = it.next();
      if (node.getName().toUpperCase().equals(service.toUpperCase()))
        // enregistrer
        topics[0] = node.getId() + "," + node.getNodePK().getInstanceId();
    }
    uchc.setTopics(topics);

    // Maj Publication
    pubDetail.setName(matricule + " " + lastName.toUpperCase() + " " + firstName.toUpperCase());
    String newDescription = pubDetail.getDescription().concat(" ").concat(pubDetail.getKeywords());
    pubDetail.setDescription(newDescription);
    String keywords = kmeliaScc.getComponentLabel();
    pubDetail.setKeywords(keywords);

    uchc.setPubDetail(pubDetail);

    // Classer la publication sur le service
    String positionLabel = service;
    int silverObjectId = kmeliaScc.getSilverObjectId(pubDetail.getId());
    try {
      List<Value> axisValues = kmeliaScc.getPdcBm().getAxisValuesByName(positionLabel);
      for (int i = 0; i < axisValues.size(); i++) {
        Value axisValue = axisValues.get(i);
        String selectedPosition = axisValue.getTreeId() + "|" + axisValue.getFullPath();
        ClassifyPosition position = buildPosition(null, selectedPosition);
        kmeliaScc.getPdcBm().addPosition(silverObjectId, position, kmeliaScc.getComponentId(),
            false);
      }
    } catch (PdcException pde) {
      pde.printStackTrace();
    }
  }

  private String getField(String field, String userName) {
    Connection con = getConnection();
    String result = "";

    String query = "select "
        + field
        + " from personnel where (lastname||' '||firstname|| ' '||matricule) = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, userName);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        result = rs.getString(1);
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException(
          "DefineServiceOfUser.getUserServiceMatricule()",
          SilverpeasRuntimeException.ERROR, "kmelia.SERVICE_NOT_EXIST", e);
    } finally {
      // fermer la connexion
      freeConnection(con);
    }
    return result;
  }

  private Connection getConnection() {
    try {
      Connection con = DBUtil.makeConnection(JNDINames.SILVERPEAS_DATASOURCE);
      return con;
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefineServiceOfUser.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  private void freeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        throw new KmeliaRuntimeException("DefineServiceOfUser.getConnection()",
            SilverpeasRuntimeException.ERROR,
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  private ClassifyPosition buildPosition(String positionId, String valuesFromJsp) {
    // valuesFromJsp looks like 12|/0/1/2/,14|/15/34/
    // [axisId|valuePath+valueId]*
    StringTokenizer st = new StringTokenizer(valuesFromJsp, ",");
    String valueInfo = "";
    String axisId = "";
    String valuePath = "";
    ClassifyValue value = null;
    List<ClassifyValue> values = new ArrayList<ClassifyValue>();
    for (; st.hasMoreTokens();) {
      valueInfo = st.nextToken();
      if (valueInfo.length() >= 3) {
        axisId = valueInfo.substring(0, valueInfo.indexOf("|"));
        valuePath = valueInfo.substring(valueInfo.indexOf("|") + 1, valueInfo
            .length());
        value = new ClassifyValue(new Integer(axisId).intValue(), valuePath);
        values.add(value);
      }
    }

    int id = -1;
    if (positionId != null)
      id = new Integer(positionId).intValue();
    ClassifyPosition position = new ClassifyPosition(values);
    position.setPositionId(id);
    return position;
  }

}