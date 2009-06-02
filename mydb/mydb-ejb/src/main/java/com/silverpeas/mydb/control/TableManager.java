package com.silverpeas.mydb.control;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;

import com.silverpeas.mydb.data.datatype.DataType;
import com.silverpeas.mydb.data.datatype.DataTypeList;
import com.silverpeas.mydb.data.db.DbColumn;
import com.silverpeas.mydb.data.db.DbTable;
import com.silverpeas.mydb.data.db.DbUtil;
import com.silverpeas.mydb.data.key.ForeignKey;
import com.silverpeas.mydb.data.key.ForeignKeyError;
import com.silverpeas.mydb.data.key.ForeignKeys;
import com.silverpeas.mydb.data.key.PrimaryKey;
import com.silverpeas.mydb.data.key.UnicityKey;
import com.silverpeas.mydb.data.key.UnicityKeys;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.ResourcesWrapper;

/**
 * Manager used to create or modify a database table, not its content but its description.
 * For the moment, only creation mode is managed.
 * 
 * @author Antoine HEDIN
 */
public class TableManager
{
	
	public static final int MODE_CREATION = 0;
	public static final int MODE_UPDATE = 1;
	
	private int mode;
	private String originPage;
	private DbTable table;
	private DbColumn errorColumn;
	private String errorLabel;
	private ArrayList keywords;
	private DataTypeList dataTypeList;
	private PrimaryKey primaryKey;
	private UnicityKeys unicityKeys;
	private ForeignKeys foreignKeys;
	
	public TableManager(int mode, String originPage, ArrayList keywords, DataTypeList dataTypeList)
	{
		this.mode = mode;
		this.originPage = originPage;
		this.keywords = keywords;
		this.dataTypeList = dataTypeList;
		table = new DbTable("");
		primaryKey = table.getPrimaryKey();
		unicityKeys = new UnicityKeys(table);
		foreignKeys = new ForeignKeys(table);
	}
	
	public int getMode()
	{
		return mode;
	}
	
	public String getOriginPage()
	{
		return originPage;
	}
	
	public boolean isCreationMode()
	{
		return (mode == MODE_CREATION);
	}
	
	public DbTable getTable()
	{
		return table;
	}
	
	public void setErrorColumn(DbColumn errorColumn)
	{
		this.errorColumn = errorColumn;
	}
	
	public DbColumn getErrorColumn()
	{
		return errorColumn;
	}
	
	public void setErrorLabel(String errorLabel)
	{
		this.errorLabel = errorLabel;
	}
	
	public String getErrorLabel()
	{
		return errorLabel;
	}
	
	public boolean hasErrorLabel()
	{
		return (errorLabel != null && errorLabel.length() > 0);
	}
	
	public DataTypeList getDataTypeList()
	{
		return dataTypeList;
	}
	
	public PrimaryKey getPrimaryKey()
	{
		return primaryKey;
	}
	
	public UnicityKeys getUnicityKeys()
	{
		return unicityKeys;
	}
	
	public ForeignKeys getForeignKeys()
	{
		return foreignKeys;
	}
	
	/**
	 * Updates the primary key. Forces the corresponding table's columns to be not nullable.
	 * 
	 * @param newPrimaryKey The reference primary key.
	 */
	public void updatePrimaryKey(PrimaryKey newPrimaryKey)
	{
		primaryKey.update(newPrimaryKey);
		table.forceColumnsNotNull(primaryKey.getColumns());
	}
	
	/**
	 * Updates the column corresponding to the column and the index given as parameters. Updates the keys which
	 * reference it.
	 * 
	 * @param column The reference column.
	 * @param index The index of the column to update.
	 */
	public void updateColumn(DbColumn column, int index)
	{
		DbColumn columnToUpdate = table.getColumn(index);
		String oldColumnName = columnToUpdate.getName();
		String newColumnName = column.getName();
		primaryKey.replaceColumn(oldColumnName, newColumnName);
		unicityKeys.replace(oldColumnName, newColumnName);
		foreignKeys.replace(oldColumnName, newColumnName);
		columnToUpdate.update(column);
		if (primaryKey.isPrimaryKey(newColumnName) || unicityKeys.isUnicityKey(newColumnName))
		{
			table.forceColumnNotNull(newColumnName);
		}
		if (column.hasDefaultValue()
			&& (foreignKeys.isForeignKey(newColumnName) || unicityKeys.isUnicityKey(newColumnName)))
		{
			table.forceColumnNoDefaultValue(newColumnName);
		}
	}
	
	/**
	 * Removes from the table the column corresponding to the index.
	 * 
	 * @param index The index of the column to delete.
	 */
	public void removeColumn(int index)
	{
		String columnName = table.getColumn(index).getName();
		unicityKeys.remove(columnName);
		foreignKeys.remove(columnName);
		table.removeColumn(index);
	}
	
	/**
	 * Checks if the current table's name is valid. Fills the error label if an error is detected.
	 * 
	 * @param tableNames The other tables names.
	 * @param resources The resources wrapper.
	 * @return True if the current table's name is valid.
	 */
	public boolean isValidTableName(String[] tableNames, ResourcesWrapper resources)
	{
		String tableName = table.getName();
		if (tableName == null || tableName.length() == 0)
		{
			errorLabel = resources.getString("ErrorTableNameRequired");
			return false;
		}
		tableName = tableName.toUpperCase();
		for (int i = 0, n = tableNames.length; i < n; i++)
		{
			if (tableName.equals(tableNames[i].toUpperCase()))
			{
				errorLabel = resources.getString("ErrorTableNameExisting");
				return false;
			}
		}
		errorLabel = "";
		return true;
	}
	
	/**
	 * Checks if the column is valid. Fills the error label if an error is detected. The following characteristics of
	 * the columns are checked : name, data type, default value.
	 *  
	 * @param column The column to check.
	 * @param resources The resources wrapper.
	 * @param exceptedIndex The index of the column (to avoid comparing the column with itself).
	 * @return True if the column is valid.
	 */
	public boolean isValidColumn(DbColumn column, ResourcesWrapper resources, int exceptedIndex)
	{
		errorLabel = "";
		return (isValidKeyName(column, resources, exceptedIndex)
			&& isValidColumnType(column, resources)
			&& isValidColumnDefaultValue(column, resources));
	}
	
	/**
	 * @param column The column to check.
	 * @param resources The resources wrapper.
	 * @return True if the data type of the column has been set.
	 */
	private boolean isValidColumnType(DbColumn column, ResourcesWrapper resources)
	{
		if (column.getDataType() == DbColumn.DEFAULT_DATA_TYPE)
		{
			errorLabel = resources.getString("ErrorColumnTypeRequired");
			return false;
		}
		return true;
	}
	
	/**
	 * @param column The column to check.
	 * @param resources The resources wrapper.
	 * @return True if the default value of the column corresponds to its data type.
	 */
	private boolean isValidColumnDefaultValue(DbColumn column, ResourcesWrapper resources)
	{
		Class clazz = null;
		if (column.hasDefaultValue())
		{
			String defaultValue = column.getDefaultValue();
			DataType dataType = dataTypeList.get(column.getDataType());
			clazz = dataType.getJavaType();
			Constructor constructor = null;
			Method method = null;
			try
			{
				if (!clazz.getPackage().getName().equals("java.sql"))
				{
					constructor = clazz.getConstructor(new Class[] {String.class});
				}
				else
				{
					method = clazz.getMethod("parse", new Class[] {String.class});
				}
			}
			catch (Exception e)
			{
				SilverTrace.warn("myDB", "TableManager.isValidColumnDefaultValue()",
					"myDB.MSG_CANNOT_GET_CONSTRUCTOR_OR_METHOD", "Class=" + clazz.getName(), e);
			}
			try
			{
				if (constructor != null)
				{
					constructor.newInstance(new String[] {defaultValue});
				}
				else if (method != null)
				{
					method.invoke(null, new String[] {defaultValue});
				}
			}
			catch (Exception e)
			{
				errorLabel = resources.getString("ErrorColumnDefaultValue");
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks the validity of the object's name :<br>
	 *     - the name has to be valorized.<br>
	 *     - it has to be different from the database keywords.<br>
	 *     - it must not be the same of an other column or key of the table.
	 * 
	 * @param object The object (column or key) to check.
	 * @param resources The resources wrapper.
	 * @param index The index of the object (to avoid comparing the object with itself).
	 * @return True if the name of the object is valid.
	 */
	public boolean isValidKeyName(Object object, ResourcesWrapper resources, int index)
	{
		errorLabel = "";
		String name = "";
		String errorLabelPrefix = "Error";
		int exceptedColumnIndex = -1;
		int exceptedPKIndex = -1;
		int exceptedUKIndex = -1;
		int exceptedFKIndex = -1;
		if (object instanceof DbColumn)
		{
			name = ((DbColumn)object).getName();
			errorLabelPrefix += "Column";
			exceptedColumnIndex = index;
		}
		else if (object instanceof PrimaryKey)
		{
			name = ((PrimaryKey)object).getName();
			errorLabelPrefix += "PrimaryKey";
			exceptedPKIndex = 0;
		}
		else if (object instanceof UnicityKey)
		{
			name = ((UnicityKey)object).getName();
			errorLabelPrefix += "UnicityKey";
			exceptedUKIndex = index;
		}
		else if (object instanceof ForeignKey)
		{
			name = ((ForeignKey)object).getName();
			errorLabelPrefix += "ForeignKey";
			exceptedFKIndex = index;
		}
		
		// Name valorization.
		if (name == null || name.length() == 0)
		{
			errorLabel = resources.getString(errorLabelPrefix + "NameRequired");
			return false;
		}
		
		String nameUpperCase = name.toUpperCase();
		
		// Database keywords.
		if (keywords.contains(nameUpperCase))
		{
			errorLabel = MessageFormat.format(
				resources.getString(errorLabelPrefix + "NameKeyword"), new String[] {nameUpperCase});
			return false;
		}
		
		// Columns.
		String[] columnsNames = table.getColumnsNames();
		for (int i = 0, n = columnsNames.length; i < n; i++)
		{
			if (i != exceptedColumnIndex && nameUpperCase.equals(columnsNames[i].toUpperCase()))
			{
				errorLabel = resources.getString("ErrorColumnNameExisting");
				return false;	
			}
		}
		
		// Primary key.
		if (exceptedPKIndex != 0 && !primaryKey.isEmpty())
		{
			if (nameUpperCase.equals(primaryKey.getName().toUpperCase()))
			{
				errorLabel = resources.getString("ErrorPrimaryKeyNameExisting");
				return false;
			}
		}
		
		// Unicity keys.
		for (int i = 0, n = unicityKeys.getSize(); i < n; i++)
		{
			if (i != exceptedUKIndex && nameUpperCase.equals(unicityKeys.get(i).getName().toUpperCase()))
			{
				errorLabel = resources.getString("ErrorUnicityKeyNameExisting");
				return false;	
			}
		}
		
		// Foreign keys.
		for (int i = 0, n = foreignKeys.getSize(); i < n; i++)
		{
			if (i != exceptedFKIndex && nameUpperCase.equals(foreignKeys.get(i).getName().toUpperCase()))
			{
				errorLabel = resources.getString("ErrorForeignKeyNameExisting");
				return false;	
			}
		}
		
		return true;
	}
	
	/**
	 * @return The SQL query to call to create the current table (columns and primary key).
	 */
	public String getTableCreationQuery()
	{
		StringBuffer querySb = new StringBuffer(200)
			.append("CREATE TABLE ").append(table.getName()).append(" (");
		DbColumn[] columns = table.getColumns();
		DbColumn column;
		for (int i = 0, n = columns.length; i < n; i++)
		{
			column = columns[i];
			if (i > 0)
			{
				querySb.append(", ");
			}
			querySb.append(column.getName()).append(" ").append(dataTypeList.getDataTypeName(column.getDataType()));
			if (column.hasDataSize())
			{
				querySb.append("(").append(column.getDataSize()).append(")");
			}
			querySb.append(" ").append(column.isNullable() ? "NULL" : "NOT NULL");
			if (column.hasDefaultValue())
			{
				querySb.append(" DEFAULT '").append(column.getDefaultValue()).append("'");
			}
		}
		if (!primaryKey.isEmpty())
		{
			querySb.append(", CONSTRAINT ").append(primaryKey.getName()).append(" PRIMARY KEY ").append("(")
				.append(DbUtil.getListAsString(primaryKey.getColumns())).append(")");
		}
		querySb.append(")");
		return querySb.toString();
	}
	
	/**
	 * @return The list of SQL queries to call to create the table's unicity keys.
	 */
	public String[] getUnicityKeysQueries()
	{
		ArrayList queries = new ArrayList();
		StringBuffer querySb;
		UnicityKey unicityKey;
		for (int i = 0, n = unicityKeys.getSize(); i < n; i++)
		{
			unicityKey = unicityKeys.get(i);
			querySb = new StringBuffer(200).append("ALTER TABLE ").append(table.getName())
				.append(" ADD CONSTRAINT ").append(unicityKey.getName()).append(" UNIQUE ").append("(")
				.append(DbUtil.getListAsString(unicityKey.getColumns())).append(")");
			queries.add(querySb.toString());
		}
		return (String[])queries.toArray(new String[queries.size()]);
	}
	
	/**
	 * @return The list of SQL queries to call to create the table's foreign keys.
	 */
	public String[] getForeignKeysQueries()
	{
		ArrayList queries = new ArrayList();
		StringBuffer querySb;
		ForeignKey foreignKey;
		for (int i = 0, n = foreignKeys.getSize(); i < n; i++)
		{
			foreignKey = foreignKeys.get(i);
			querySb = new StringBuffer(200).append("ALTER TABLE ").append(table.getName())
				.append(" ADD CONSTRAINT ").append(foreignKey.getName()).append(" FOREIGN KEY ").append("(")
				.append(DbUtil.getListAsString(foreignKey.getColumns()))
				.append(")").append(" REFERENCES ").append(foreignKey.getForeignTable()).append(" (")
				.append(DbUtil.getListAsString(foreignKey.getForeignColumnsNames())).append(")");
			queries.add(querySb.toString());
		}
		return (String[])queries.toArray(new String[queries.size()]);
	}
	
	/**
	 * Updates a column consecutively to an error detected in a foreign key which references it. The type or the size of
	 * the column is so modified to correspond to the description of the key.
	 * 
	 * @param name the column's name.
	 * @param errorType The error type.
	 * @param value The new value, corresponding to the new type or size of the column. 
	 */
	public void updateColumn(String name, int errorType, String value)
	{
		DbColumn column = table.getColumn(name);
		switch (errorType)
		{
			case ForeignKeyError.ERROR_TYPE :
				int dataType = Integer.parseInt(value);
				column.setDataType(dataType);
				if (!dataTypeList.isSizeEnabled(dataType))
				{
					column.removeDataSize();
				}
				break;
			case ForeignKeyError.ERROR_SIZE :
				column.setDataSize(Integer.parseInt(value));
				break;
		}
	}
	
	/**
	 * Each line of the returned table contains informations concerning a column of the table :
	 *     - the name of the column
	 *     - the names of the keys which would be modified if the column is removed
	 *     - the names of the keys which would be removed if the column is removed
	 * 
	 * @return A list of the links which exist between the columns of the table and its different keys. This list is
	 * 		   used to propose warning messages to the user when he decides to delete a column, to inform him about the
	 * 		   consequences of deleting a column on table's keys (modification or deletion of them).
	 */
	public String[][] getKeysImpacts()
	{
		String[] columnsNames = table.getColumnsNames();
		String keyName;
		int x;
		int y;
		int columnsCount = columnsNames.length;
		String[][] result = new String[columnsCount][3];
		
		// Initialization with columns names.
		for (int i = 0; i < columnsCount; i++)
		{
			result[i][0] = columnsNames[i];
			result[i][1] = "";
			result[i][2] = "";
		}
		
		// Primary key.
		keyName = primaryKey.getName();
		columnsNames = primaryKey.getColumns();
		columnsCount = columnsNames.length;
		for (int i = 0; i < columnsCount; i++)
		{
			x = table.getColumnIndex(columnsNames[i]);
			y = columnsCount > 1 ? 1 : 2;
			result[x][y] += (result[x][y].length() > 0 ? ", " : "") + keyName;
		}
		
		// Unicity keys.
		UnicityKey unicityKey;
		for (int i = 0, n = unicityKeys.getSize(); i < n; i++)
		{
			unicityKey = unicityKeys.get(i);
			keyName = unicityKey.getName();
			columnsNames = unicityKey.getColumns();
			columnsCount = columnsNames.length;
			for (int j = 0; j < columnsCount; j++)
			{
				x = table.getColumnIndex(columnsNames[j]);
				y = columnsCount > 1 ? 1 : 2;
				result[x][y] += (result[x][y].length() > 0 ? ", " : "") + keyName;
			}
		}
		
		// Foreign keys.
		ForeignKey foreignKey;
		for (int i = 0, n = foreignKeys.getSize(); i < n; i++)
		{
			foreignKey = foreignKeys.get(i);
			keyName = foreignKey.getName();
			columnsNames = foreignKey.getColumns();
			columnsCount = columnsNames.length;
			y = 2;
			for (int j = 0; j < columnsCount; j++)
			{
				x = table.getColumnIndex(columnsNames[j]);
				result[x][y] += (result[x][y].length() > 0 ? ", " : "") + keyName;
			}
		}
		
		return result;
	}
	
}