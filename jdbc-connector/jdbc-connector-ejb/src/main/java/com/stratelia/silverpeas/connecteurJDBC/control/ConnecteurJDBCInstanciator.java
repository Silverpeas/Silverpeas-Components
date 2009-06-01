package com.stratelia.silverpeas.connecteurJDBC.control;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.SQLRequest;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Title:        Connecteur JDBC
 * Description:  Ce composant a pour objet de permettre de récupérer rapidement et simplement des données du système d'information de l'entreprise.
 * Copyright:    Copyright (c) 2001
 * Company:      Stratélia
 * @author Eric BURGEL
 * @version 1.0
 */

public class ConnecteurJDBCInstanciator extends SQLRequest implements ComponentsInstanciatorIntf {

	 public ConnecteurJDBCInstanciator() {
		super("com.stratelia.silverpeas.connecteurJDBC");
	 }

	  public void create(Connection connection, String spaceId, String componentId, String userId)
		throws com.stratelia.webactiv.beans.admin.instance.control.InstanciationException {

	  }
	
	
	/**
	* Delete some rows of an instance of a forum.
	* @param con (Connection) the connection to the data base
	* @param spaceId (String) the id of a the space where the component exist.
	* @param componentId (String) the instance id of the Silverpeas component forum.
	* @param userId (String) the owner of the component
	*/
	public void delete(Connection con, String spaceId, String componentId, String userId) throws InstanciationException {
		SilverTrace.info("connecteurJDBC", "ConnecteurJDBCInstanciator.delete()",
		                 "connecteurJDBC.MSG_DELETE_CALLED_FOR_SPACE_ID", 
					     "spaceId : "+spaceId);

		// read the property file which contains all SQL queries to delete rows
		setDeleteQueries();

		deleteDataOfInstance(con, componentId, "connecteurJDBC");
	}
  
	/**
	* Delete all data of one forum instance from the forum table.
	* @param con (Connection) the connection to the data base
	* @param componentId (String) the instance id of the Silverpeas component forum.
	* @param suffixName (String) the suffixe of a Forum table
	*/
	private void deleteDataOfInstance(Connection con, String componentId, String suffixName) throws InstanciationException {
		
		Statement stmt = null;
		

		// get the delete query from the external file
		String deleteQuery = getDeleteQuery(componentId,suffixName);
		
		// execute the delete query
		try {
			stmt = con.createStatement();
		    stmt.executeUpdate(deleteQuery);
		    stmt.close();
		} catch (SQLException se) {
		    InstanciationException ie = new InstanciationException("connecteurJDBCInstanciator.deleteDataOfInstance()", 
				                                                    SilverpeasException.ERROR,
				                                                   "connecteurJDBC.EX_DELETE_DATA_OF_INSTANCE_FAIL",
				                                                    "componentId : "+componentId+"delete query = "+deleteQuery, se);
		    throw ie;
		}
		finally {
			try{
				stmt.close();
			}
			catch (SQLException err_closeStatement){
				InstanciationException ie = new InstanciationException("connecteurJDBCInstanciator.deleteDataOfInstance()", 
				                                                       SilverpeasException.ERROR,
				                                                       "connecteurJDBC.EX_CLOSE_STATEMENT_FAIL",
				                                                       "componentId : "+componentId+"delete query = "+deleteQuery, err_closeStatement);
				throw ie;
			}
		}
		
	}
  
}
