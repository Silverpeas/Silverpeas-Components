package com.silverpeas.mydb.data.key;

/**
 * Error linked to a table foreign key.
 * 
 * @author Antoine HEDIN
 */
public class ForeignKeyError {
	
	public static final int ERROR_TYPE = 0;
	public static final int ERROR_SIZE = 1;
	
	private String column;
	private String label;
	private int type;
	private int correctedValue;

	public ForeignKeyError(String column, String label, int type, int correctedValue)
	{
		this.column = column;
		this.label = label;
		this.type = type;
		this.correctedValue = correctedValue;
	}
	
	public String getColumn()
	{
		return column;
	}
	
	public String getLabel()
	{
		return label;
	}
	
	public int getType()
	{
		return type;
	}
	
	public int getCorrectedValue()
	{
		return correctedValue;
	}
	
}
