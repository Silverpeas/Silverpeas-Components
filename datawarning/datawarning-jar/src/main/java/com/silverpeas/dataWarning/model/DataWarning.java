package com.silverpeas.dataWarning.model;

import java.util.*;
import java.sql.*;

import com.stratelia.webactiv.persistence.*;
import com.stratelia.webactiv.util.*;
import com.silverpeas.dataWarning.*;
import com.stratelia.webactiv.util.exception.*;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class DataWarning extends SilverpeasBean
{
	public static final int INCONDITIONAL_QUERY			= 0;
	public static final int TRIGGER_ANALYSIS			= 1;
	public static final int ON_VARIATION_ANALYSIS		= 2; // Not implemented yet...
	public static final int ON_NO_VARIATION_ANALYSIS	= 3; // Not implemented yet...
	
	private String description;
	private String JDBCDriverName;
	private String login;
	private String pwd;
	private int rowLimit;
	private String instanceId;
	private int analysisType;
	
	public DataWarning()
	{
		super();
		analysisType = INCONDITIONAL_QUERY;
	}

    public Object clone()
    {
        DataWarning newOne = new DataWarning();

        newOne.description = description;
        newOne.JDBCDriverName = JDBCDriverName;
        newOne.login = login;
        newOne.pwd = pwd;
        newOne.rowLimit = rowLimit;
        newOne.instanceId = instanceId;
        newOne.analysisType = analysisType;
        newOne.setPK(getPK());

        return newOne;
    }

	public DataWarning(String description, String JDBCDriverName, String login, String password, int rowLimit, String instanceId, int analysisType)
	{
		super();
        this.description = description;
		this.JDBCDriverName = JDBCDriverName;
		this.login = login;
		this.pwd = password;
		this.rowLimit = rowLimit;
		this.instanceId = instanceId;
		this.analysisType = analysisType;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getDescription()
	{
		return getSureString(description);
	}

	public void setJDBCDriverName(String JDBCDriverName)
	{
		this.JDBCDriverName = JDBCDriverName;
	}

	public String getJDBCDriverName()
	{
		return JDBCDriverName;
	}

	public void setLogin(String login)
	{
		this.login = login;
	}

	public String getLogin()
	{
		return getSureString(login);
	}

	public void setPwd(String password)
	{
		this.pwd = password;
	}

	public String getPwd()
	{
		return getSureString(pwd);
	}

	public void setRowLimit(int rowLimit)
	{
		this.rowLimit = rowLimit;
	}

	public int getRowLimit()
	{
		return rowLimit;
	}

	public void setInstanceId(String instanceId)
	{
		this.instanceId = instanceId;
	}

	public String getInstanceId()
	{
		return instanceId;
	}

	public void setAnalysisType(int analysisType)
	{
		this.analysisType = analysisType;
	}

	public int getAnalysisType()
	{
		return analysisType;
	}

	public int _getConnectionType()
	{
		return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
	}

	public String _getTableName()
	{
		return "SC_DataWarning";
	}

  // External DB access....

  /**
   * Ouverture de la connection vers
   * la source de donnees
   * @return Connection la connection
   * @exception DataWarningException
   */
	public Connection openConnection() throws DataWarningException 
	{
        DataWarningDBDriver dbDriver = DataWarningDBDrivers.getDBDriver(JDBCDriverName);
		Connection con = null;
        SilverTrace.info("dataWarning", "DataWarning.openConnection()","root.MSG_GEN_PARAM_VALUE","JDBCDriverName="+JDBCDriverName+" | login="+login);
		try 
		{
			Class.forName(dbDriver.ClassName);
			con = DriverManager.getConnection(dbDriver.JDBCUrl, login, pwd);
		}
		catch (Exception e) 
		{
			throw new DataWarningException("DataWarning.openConnection()", SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e);
		}

		return con;
	}

	public void closeConnection(Connection con)
	{
        if (con != null)
        {
            try
            {
                con.close();
            }
            catch(Exception e)
            {
                SilverTrace.error("dataWarning", "DataWarning.closeConnection()","DataWarning.EX_DATA_ACCESS_FAILED",e);
            }
        }
	}

    /**
	 * return un tableau de string contenant les noms de toutes les tables de la base
	 */
	public String[] getAllTableNames()  throws DataWarningException 
	{
		String[] retour = null;
		Connection con = openConnection();
		ResultSet tables_rs = null;
        ResultSetMetaData tables_rsmd = null;
		try
		{
			DatabaseMetaData dbMetaData = con.getMetaData();
			tables_rs = dbMetaData.getTables(null,null,null,null);
			tables_rsmd = tables_rs.getMetaData();
			List tables = new ArrayList();
			while (tables_rs.next())
			{
    			tables.add(tables_rs.getString("TABLE_NAME"));
			}
			retour = new String[tables.size()];
			for (int i=0; i<tables.size(); i++)
				retour[i] = (String)tables.get(i);
		}
		catch(Exception e)
		{
			throw new DataWarningException("DataWarningDataManager.getAllTableNames()", SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e); 
		}
		finally
		{
            DBUtil.close(tables_rs);
            closeConnection(con);
		}
		return retour;
	}

	/**
	 * return les noms des colonnes de la table passé en parametre
	 */
	public String[] getColumnNames(String tableName)  throws DataWarningException 
	{
		String[] retour = null;
		Connection con = openConnection();
		ResultSet colonnes_rs = null;
		try
		{
			DatabaseMetaData dbMetaData = con.getMetaData();
			colonnes_rs = dbMetaData.getColumns(null, null, tableName, null);
			List colonnes = new ArrayList();
			while (colonnes_rs.next())
			{
    			colonnes.add(colonnes_rs.getString("COLUMN_NAME"));
			}
			retour = new String[colonnes.size()];
			for (int i=0; i<colonnes.size(); i++)
				retour[i] = (String)colonnes.get(i);
		}
		catch(Exception e)
		{
			throw new DataWarningException("DataWarningDataManager.getColumnNames()", SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e); 
		}
		finally
		{
            DBUtil.close(colonnes_rs);
            closeConnection(con);
		}
		return retour;
	}
}
