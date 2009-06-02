package com.silverpeas.mydb.data.index;

/**
 * IndexInfo element of a database table.
 * Used to initialize the IndexInfo list of a table.
 * 
 * @author Antoine HEDIN
 */
public class IndexElement
{
	
	public static final String COLUMN_NAME =		"COLUMN_NAME";
	public static final String INDEX_NAME =			"INDEX_NAME";
	public static final String ORDINAL_POSITION =	"ORDINAL_POSITION";

	private String indexName;
	private String column;
	private short position;
	
	public IndexElement(String indexName, String column, short position)
	{
		this.column = column;
		this.indexName = indexName;
		this.position = position;
	}

	public String getIndexName()
	{
		return indexName;
	}

	public String getColumn()
	{
		return column;
	}

	public short getPosition()
	{
		return position;
	}
	
}
