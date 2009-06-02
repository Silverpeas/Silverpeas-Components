package com.silverpeas.mydb.data.index;

import java.util.ArrayList;

/**
 * IndexInfo of a database table.
 * 
 * @author Antoine HEDIN
 */
public class IndexInfo
{
	
	private String name;
	private ArrayList columns;

	public IndexInfo(String name)
	{
		this.name = name;
		columns = new ArrayList();
	}
	
	public String getName()
	{
		return name;
	}
	
	public void addColumn(String column)
	{
		columns.add(column);
	}
	
	public String getColumn(int index)
	{
		return (String)columns.get(index);
	}
	
	public int getColumnsCount()
	{
		return columns.size();
	}
	
	public String[] getColumns()
	{
		return (String[])columns.toArray(new String[columns.size()]);
	}
	
}