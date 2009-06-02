package com.silverpeas.mydb.data.key;

import java.util.ArrayList;
import java.util.Map;

import com.silverpeas.mydb.data.db.DbTable;

/**
 * Table unicity key.
 * 
 * @author Antoine HEDIN
 */
public class UnicityKey {
	
	public static final String UNICITY_KEY_PREFIX = "uk_";
	
	private String name;
	private ArrayList columns;

	public UnicityKey(String name)
	{
		this.name = name;
		columns = new ArrayList();
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
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
	
	public String[] getColumns()
	{
		return (String[])columns.toArray(new String[columns.size()]);
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
	
	public int getColumnsCount()
	{
		return columns.size();
	}
	
	public boolean containsColumn(String column)
	{
		return columns.contains(column);
	}
	
	public void update(Map parameterMap, DbTable parentTable)
	{
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
				fieldKey = UNICITY_KEY_PREFIX + columnName;
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
