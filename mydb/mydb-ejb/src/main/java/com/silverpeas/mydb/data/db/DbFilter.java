package com.silverpeas.mydb.data.db;

import java.sql.Types;

/**
 * Database data filter.
 * 
 * @author Antoine HEDIN
 */
public class DbFilter
{
	
	public static final String ALL = "*";
	public static final String CONTAINS = "CONTAINS";
	public static final String[] COMPARES_SYMBOLS = {"=", "<=", ">=", "!=", ">", "<"};
	
	private String column;
	private String compare;
	private String value;
	private boolean manualFilter;

	public DbFilter()
	{
		column = ALL;
		compare = ALL;
		value = "";
		manualFilter = false;
	}
	
	public DbFilter(String column, String compare, String value)
	{
		if (column.equals(ALL) || compare.equals(ALL) || value.equals(""))
		{
			this.column = ALL;
			this.compare = ALL;
			this.value = "";
		}
		else
		{
			this.column = column;
			this.compare = compare;
			this.value = value;			
		}
		manualFilter = false;
	}

	public String getColumn()
	{
		return column;
	}

	public String getCompare()
	{
		return compare;
	}

	public String getValue()
	{
		return value;
	}

	public boolean isManualFilter()
	{
		return manualFilter;
	}
	
	public String getQueryFilter(DbTable dbTable)
	{
		String query = "";
		manualFilter = false;
		if ((!ALL.equals(column)) && (!ALL.equals(compare)) && (!"".equals(value)))
		{
			int dataType = dbTable.getColumn(column).getDataType();
			if (((dataType == Types.INTEGER) || (dataType == Types.DOUBLE) || (dataType == Types.FLOAT))
				&& (compare.equals(CONTAINS)))
			{
				// Impossible de filtrer sous la forme 'like' avec un numérique : on positionne l'indicateur de
				// filtrage manuel.
				manualFilter = true;
			}
			else
			{
				StringBuffer queryFilter = new StringBuffer(20).append(" where ").append(column);
				if (compare.equals(CONTAINS))
				{
					queryFilter.append(" like '%").append(value).append("%'");
				}
				else
				{
					queryFilter.append(" ").append(compare).append(" '").append(value).append("'");
				}
				query = queryFilter.toString();
			}
		}
		return query;
	}

}