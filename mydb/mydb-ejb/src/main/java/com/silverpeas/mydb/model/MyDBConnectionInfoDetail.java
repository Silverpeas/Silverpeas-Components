package com.silverpeas.mydb.model;

import com.stratelia.webactiv.persistence.SilverpeasBean;

/**
 * Description of a connection to a database.
 * 
 * @author Antoine HEDIN
 */
public class MyDBConnectionInfoDetail
	extends SilverpeasBean
{

	private String jdbcDriverName = "";
	private String jdbcUrl = "";
	private String login = "";
	private String password = "";
	private String tableName = "";
	private int rowLimit;

	public String getJdbcDriverName()
	{
		return jdbcDriverName;
	}

	public void setJdbcDriverName(String jdbcDriverName)
	{
		this.jdbcDriverName = jdbcDriverName;
	}

	public String getJdbcUrl()
	{
		return jdbcUrl;
	}

	public void setJdbcUrl(String jdbcUrl)
	{
		this.jdbcUrl = jdbcUrl;
	}

	public String getLogin()
	{
		return login;
	}

	public void setLogin(String login)
	{
		this.login = login;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getTableName()
	{
		return tableName;
	}

	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}

	public String getInstanceId()
	{
		return getPK().getComponentName();
	}

	public int getRowLimit()
	{
		return rowLimit;
	}

	public void setRowLimit(int rowLimit)
	{
		this.rowLimit = rowLimit;
	}

}