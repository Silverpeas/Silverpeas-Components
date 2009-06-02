package com.silverpeas.mydb.data.datatype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * List of data types available for a specific database driver.
 * 
 * @author Antoine HEDIN.
 */
public class DataTypeList
{
	
	private static final int DEFAULT_CAPACITY = 10;
	
	private Hashtable dataTypeList;
	
	public DataTypeList()
	{
		dataTypeList = new Hashtable(DEFAULT_CAPACITY);
	}
	
	public DataTypeList(int initialCapacity)
	{
		dataTypeList = new Hashtable(initialCapacity);
	}
	
	public void add(DataType dataType)
	{
		dataTypeList.put(dataType.getName(), dataType);
	}
	
	public DataType get(String name)
	{
		return (DataType)dataTypeList.get(name);
	}
	
	public DataType get(int type)
	{
		DataType dataType;
		String name;
		for (Enumeration en = dataTypeList.keys(); en.hasMoreElements(); )
		{
			name = (String)en.nextElement();
			dataType = get(name);
			if (dataType.getSqlType() == type)
			{
				return dataType;
			}
		}
		return null;
	}
	
	public boolean contains(int type)
	{
		return (get(type) != null);
	}
	
	public DataType[] getDataTypes()
	{
		ArrayList list = new ArrayList(dataTypeList.size());
		String name;
		for (Enumeration en = dataTypeList.keys(); en.hasMoreElements(); )
		{
			name = (String)en.nextElement();
			list.add(get(name));
		}
		Collections.sort(list, new DataTypeComparator());
		return (DataType[])list.toArray(new DataType[list.size()]);
	}
	
	public String getDataTypeName(int type)
	{
		DataType dataType = get(type);
		return (dataType != null ? dataType.getName() : "");
	}
	
	public boolean isSizeEnabled(int type)
	{
		DataType dataType = get(type);
		return (dataType == null || dataType.isSizeEnabled());
	}

}
