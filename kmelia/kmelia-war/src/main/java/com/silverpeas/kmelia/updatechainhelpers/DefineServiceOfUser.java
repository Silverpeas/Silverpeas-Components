package com.silverpeas.kmelia.updatechainhelpers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

public class DefineServiceOfUser extends UpdateChainHelperImpl {

  public void execute(UpdateChainHelperContext uchc) 
	{
		// récupération des données
		PublicationDetail pubDetail = uchc.getPubDetail();
		List<String> nodes = new ArrayList<String>();
		
		// Recherche du service de l'utilisateur
		String service = "";
		String userName = pubDetail.getName();
		service = getUserService(userName);
		
		// associer le service au node
		String[] topics = new String[1];
		List<NodeDetail> allTopics = uchc.getAllTopics();
		Iterator<NodeDetail> it = allTopics.iterator();
		while (it.hasNext())
		{
			NodeDetail node = it.next();
			if (node.getName().equals(service))
			{
				// enregistrer 
				topics[0] = node.getId() + "," + node.getNodePK().getInstanceId();
			}
		}
		uchc.setTopics(topics);
	}

  private String getUserService(String userName) {
    Connection con = getConnection();
    String service = "";
    String query = "select service from personnel where (lastname||' '||firstname) = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, userName);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        // récupération du service
        service = rs.getString(1);
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefineServiceOfUser.getUserService()",
          SilverpeasRuntimeException.ERROR, "kmelia.SERVICE_NOT_EXIST", e);
    } finally {
      // fermer la connexion
      freeConnection(con);
    }

    return service;
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
}
