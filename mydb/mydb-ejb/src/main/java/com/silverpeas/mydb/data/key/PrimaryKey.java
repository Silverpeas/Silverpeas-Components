package com.silverpeas.mydb.data.key;

import java.util.ArrayList;
import java.util.Map;

import com.silverpeas.mydb.data.db.DbTable;

/**
 * Table primary key.
 * 
 * @author Antoine HEDIN
 */
public class PrimaryKey {
	
	public static final String PRIMARY_KEY_PREFIX = "pk_";
	
	private DbTable parentTable;
	private String name;
	private ArrayList columns;
	
	public PrimaryKey(DbTable parentTable)
	{
		this.parentTable = parentTable;
		this.name = "";
		columns = new ArrayList();
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return ((name != null && name.length() > 0) ? name : getConstraintName());
	}
	
	public String getColumn(int index)
	{
		return (String)columns.get(index);
	}
	
	public void addColumn(String column)
	{
		columns.add(column);
	}
	
	public void setColumn(int index, String column)
	{
		columns.set(index, column);
	}
	
	public boolean isEmpty()
	{
		return (columns.size() == 0);
	}
	
	public boolean isPrimaryKey(String name)
	{
		return (columns.contains(name));
	}
	
	public String[] getColumns()
	{
		return (String[])columns.toArray(new String[columns.size()]);
	}
	
	public void clear()
	{
		setName("");
		clearColumns();
	}
	
	public void clearColumns()
	{
		columns.clear();
	}
	
	public void replaceColumn(String oldColumn, String newColumn)
	{
		if (columns.contains(oldColumn) && !oldColumn.equals(newColumn))
		{
			for (int i = 0, n = columns.size(); i < n; i++)
			{
				if (getColumn(i).equals(oldColumn))
				{
					setColumn(i, newColumn);
				}
			}
		}
	}
	
	public void removeColumn(String column)
	{
		columns.remove(column);
	}
	
	public String getConstraintName()
	{
		String tableName = parentTable.getName();
		return (tableName.length() > 0 ? PRIMARY_KEY_PREFIX + tableName.toLowerCase() : "");
	}
	
	public void update(PrimaryKey primaryKey)
	{
		setName(primaryKey.getName());
		columns = primaryKey.columns;
	}
	
	public void update(String name, Map parameterMap)
	{
		setName(name);
		clearColumns();
		if (parameterMap != null)
		{
			String[] columnsNames = parentTable.getColumnsNames();
			String columnName;
			String fieldKey;
			String value;
			for (int i = 0, n = columnsNames.length; i < n; i++)
			{
				columnName = columnsNames[i];
				fieldKey = PRIMARY_KEY_PREFIX + columnName;
				if (parameterMap.containsKey(fieldKey))
				{
					value = ((String[])parameterMap.get(fieldKey))[0];
					if ("true".equals(value))
					{
						addColumn(columnName);
					}
				}
			}
		}
	}

}