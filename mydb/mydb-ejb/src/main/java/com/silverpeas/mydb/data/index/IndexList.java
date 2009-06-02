package com.silverpeas.mydb.data.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import com.silverpeas.mydb.data.datatype.DataTypeList;
import com.silverpeas.mydb.data.db.DbColumn;
import com.silverpeas.mydb.data.db.DbColumnComparator;

/**
 * IndexInfo list of a database table.
 * 
 * @author Antoine HEDIN
 */
public class IndexList
{
	
	private ArrayList indexInfos;
	private ArrayList columns;
	
	public IndexList()
	{
		indexInfos = new ArrayList();
		columns = new ArrayList();
	}
	
	public void addIndexInfo(IndexInfo indexInfo)
	{
		indexInfos.add(indexInfo);
	}
	
	public IndexInfo getIndexInfo(int index)
	{
		return (IndexInfo)indexInfos.get(index);
	}
	
	public IndexInfo getIndexInfo(String name)
	{
		IndexInfo indexInfo;
		for (int i = 0, n = getIndexInfosCount(); i < n; i++)
		{
			indexInfo = getIndexInfo(i);
			if (indexInfo.getName().equals(name))
			{
				return indexInfo;
			}
		}
		return null;
	}
	
	public int getIndexInfosCount()
	{
		return indexInfos.size();
	}
	
	public boolean containsIndexInfo(String name)
	{
		for (int i = 0, n = getIndexInfosCount(); i < n; i++)
		{
			if (getIndexInfo(i).getName().equals(name))
			{
				return true;
			}
		}
		return false;
	}
	
	public int getIndexInfoMaxColumnsCount()
	{
		int max = 1;
		for (int i = 0, n = getIndexInfosCount(); i < n; i++)
		{
			max = Math.max(getIndexInfo(i).getColumnsCount(), max);
		}
		return max;
	}
	
	public void addColumn(DbColumn column)
	{
		columns.add(column);
	}
	
	public DbColumn getColumn(int index)
	{
		return (DbColumn)columns.get(index);
	}
	
	public DbColumn getColumn(String name)
	{
		DbColumn column;
		for (int i = 0, n = getColumnsCount(); i < n; i++)
		{
			column = getColumn(i);
			if (column.getName().equals(name))
			{
				return column;
			}
		}
		return null;
	}
	
	public int getColumnsCount()
	{
		return columns.size();
	}
	
	public boolean containsColumn(String name)
	{
		for (int i = 0, n = getColumnsCount(); i < n; i++)
		{
			if (getColumn(i).getName().equals(name))
			{
				return true;
			}
		}
		return false;
	}
	
	public void sortColumns()
	{
		Collections.sort(columns, new DbColumnComparator());
	}
	
	public DbColumn[] getColumns()
	{
		return (DbColumn[])columns.toArray(new DbColumn[columns.size()]);
	}
	
	public void check(DataTypeList dataTypeList)
	{
		IndexInfo indexInfo;
		String[] columnsNames;
		int columnsNamesCount;
		DbColumn column;
		boolean keepIndexInfo;
		Hashtable keptColumnsNames = new Hashtable();
		for (int i = (getIndexInfosCount() - 1); i >= 0; i--)
		{
			indexInfo = getIndexInfo(i);
			columnsNames = indexInfo.getColumns();
			columnsNamesCount = columnsNames.length;
			keepIndexInfo = true;
			for (int j = 0; j < columnsNamesCount; j++)
			{
				column = getColumn(columnsNames[j]);
				keepIndexInfo = (keepIndexInfo && dataTypeList.contains(column.getDataType()));
			}
			if (keepIndexInfo)
			{
				for (int j = 0; j < columnsNamesCount; j++)
				{
					keptColumnsNames.put(columnsNames[j], "");
				}
			}
			else
			{
				indexInfos.remove(i);
			}
		}
		for (int i = (getColumnsCount() - 1); i >= 0; i--)
		{
			column = getColumn(i);
			if (keptColumnsNames.containsKey(column.getName()))
			{
				if (!dataTypeList.isSizeEnabled(column.getDataType()))
				{
					column.removeDataSize();
				}
			}
			else
			{
				columns.remove(i);
			}
		}
	}
	
}