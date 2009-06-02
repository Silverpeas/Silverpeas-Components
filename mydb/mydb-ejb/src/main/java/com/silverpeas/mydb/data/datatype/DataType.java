package com.silverpeas.mydb.data.datatype;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * Data type of database column.
 * Data types for a specific database driver are defined in the MyDB setting file.<br>
 * example : <code>&lt;DataType name="VARCHAR" sqlType="java.sql.Types.VARCHAR" javaType="java.lang.String"
 * 			 length="true"/&gt;</code>
 * 
 * @author Antoine HEDIN
 */
public class DataType
{

	// Name displayed.
	private String name;
	// Associated SQL type.
	private int sqlType;
	// Associated java type.
	private Class javaType;
	// Indicator of permission or not to set the size of a data defined by this type.
	private boolean isSizeEnabled;
	
	public DataType(String[] params)
	{
		name = params[0];
		setSqlType(params[1]);
		setJavaType(params[2]);
		isSizeEnabled = "true".equals(params[3]);
	}
	
	public String getName()
	{
		return name;
	}
	
	private void setSqlType(String sqlType)
	{
		int index = sqlType.lastIndexOf(".");
		if (index != -1)
		{
			String className = sqlType.substring(0, index);
			String fieldName = sqlType.substring(index + 1);
			try
			{
				Class clazz = Class.forName(className);
				this.sqlType = clazz.getDeclaredField(fieldName).getInt(null);
			}
			catch (Exception e)
			{
				SilverTrace.warn("myDB", "DataType.setSqlType()", "myDB.MSG_CANNOT_SET_SQL_TYPE", "Type=" + sqlType, e);
			}
		}
	}
	
	private void setJavaType(String javaType)
	{
		try
		{
			this.javaType = Class.forName(javaType);
		}
		catch (ClassNotFoundException e)
		{
			SilverTrace.warn("myDB", "DataType.setJavaType()", "myDB.MSG_CANNOT_SET_JAVA_TYPE", "Type=" + javaType, e);
		}
	}

	public int getSqlType()
	{
		return sqlType;
	}

	public Class getJavaType()
	{
		return javaType;
	}

	public boolean isSizeEnabled()
	{
		return isSizeEnabled;
	}
	
}