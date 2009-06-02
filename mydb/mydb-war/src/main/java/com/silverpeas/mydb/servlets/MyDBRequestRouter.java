package com.silverpeas.mydb.servlets;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.mydb.MyDBConstants;
import com.silverpeas.mydb.control.MyDBSessionController;
import com.silverpeas.mydb.control.TableManager;
import com.silverpeas.mydb.data.db.DbColumn;
import com.silverpeas.mydb.data.db.DbUtil;
import com.silverpeas.mydb.data.key.ForeignKey;
import com.silverpeas.mydb.data.key.PrimaryKey;
import com.silverpeas.mydb.data.key.UnicityKey;
import com.silverpeas.mydb.exception.MyDBException;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * MyDB request router.
 * 
 * @author Antoine HEDIN
 */
public class MyDBRequestRouter
	extends ComponentRequestRouter
	implements MyDBConstants
{
	
	/**
	 * @return The session control bean name.
	 */
    public String getSessionControlBeanName()
	{
	    return "MyDB";
	}

    /**
     * Creates a MyDB session control bean.
     *
     * @param mainSessionCtrl The main session control.
     * @param componentContext The context of the component.
     * @return The new created MyDB session control.
     */
    public ComponentSessionController createComponentSessionController(MainSessionController mainSessionCtrl,
    	ComponentContext componentContext)
    {
        return new MyDBSessionController(mainSessionCtrl, componentContext);
    }

    /**
     * This method has to be implemented by the component request rooter. It has to compute a destination page.
     * 
     * @param function The entering request function.
     * @param componentSC The session control component.
     * @return The complete destination URL for a forward.
     */
    public String getDestination(String function, ComponentSessionController componentSC, HttpServletRequest request)
    {
        String destination = "";
        MyDBSessionController myDBSC = (MyDBSessionController)componentSC;
        SilverTrace.info("myDB", "MyDBRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE",
        	"User=" + myDBSC.getUserId() + ", Function=" + function);

        // User's best role.
		request.setAttribute("userRoleLevel", myDBSC.getUserRoleLevel());
		
        try
        {
            if (function.equals(ACTION_MAIN))
            {
            	// Main action.
            	if (myDBSC.hasTableManager())
            	{
            		myDBSC.resetTableManager();            		
            	}
	            destination = PAGE_CONSULTATION;
            }
            else if (function.equals(ACTION_TABLE_SELECTION))
            {
            	// Table selection.
            	destination = getTableSelectionDestination(request, myDBSC);
            }
            else if (function.equals(ACTION_CONNECTION_SETTING))
            {
            	// Connection setting.
	            destination = PAGE_CONNECTION_SETTING;
            }
            else if (function.equals(ACTION_UPDATE_CONNECTION))
            {
            	// Connection parameters validation.
            	destination = getUpdateConnectionDestination(request, myDBSC);
            }
            else if (function.equals(ACTION_UPDATE_DATA))
            {
            	// Database record detail or update (modification or deletion).
            	destination = getUpdateDataDestination(request, myDBSC);
            }
            else if (function.equals(ACTION_UPDATE_LINE))
            {
            	// Validation of the creation/modification of a database record.
            	destination = getUpdateLineDestination(request, myDBSC);
            }
            else if (function.equals(ACTION_ADD_LINE))
            {
            	// New record creation.
            	destination = getAddLineDestination(request, myDBSC);
            }
            else if (function.equals(ACTION_FILTER))
            {
            	// Data filter.
            	destination = getFilterDestination(request, myDBSC);
            }
            else if (function.equals(ACTION_UPDATE_TABLE))
            {
            	// Creation/modification of a table.
            	destination = getUpdateTableDestination(request, myDBSC);
            }
            else
            {
            	// Default.
                destination = PAGE_CONSULTATION;
            }
            
            destination = "/myDB/jsp/" + destination;
        }
        catch (Exception e)
        {
            request.setAttribute("javax.servlet.jsp.jspException", e);
            destination = "/admin/jsp/errorpageMain.jsp";
        }

        SilverTrace.info("myDB", "MyDBRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE",
        	"Destination=" + destination);
        return destination;
    }
    
    /**
     * Table selection.
     * 
     * @param request The HTTP request.
     * @param myDBSC The session control.
     * @return The destination corresponding to a table selection.
     * @throws MyDBException
     */
    private String getTableSelectionDestination(HttpServletRequest request, MyDBSessionController myDBSC)
    	throws MyDBException
    {
    	String tableName = request.getParameter("tableName");
		if (tableName != null && tableName.length() > 0)
		{
			myDBSC.setTableName(tableName);
			myDBSC.initDbFilter();
	    	return (myDBSC.checkTableName() ? PAGE_CONSULTATION : PAGE_TABLE_SELECTION);
		}
		else
		{
			return PAGE_TABLE_SELECTION;
		}
    }
    
    /**
     * Connection setting. The destination depends on the result of the update :<br>
     *   - if the parameters are not correct, returns to the connection setting page.<br>
     *   - else if the new setting is ok but no default table is found, returns the page selection page.<br>
     *   - else (setting and table ok), returns the table detail page.
     * 
     * @param request The HTTP request.
     * @param myDBSC The session control.
     * @return The destination corresponding to a connection setting update.
     */
    private String getUpdateConnectionDestination(HttpServletRequest request, MyDBSessionController myDBSC)
    { 
	    String jdbcDriverName = request.getParameter("jdbcDriverName");
	    String jdbcUrl = request.getParameter("jdbcUrl");
	    String login = request.getParameter("login");
	    String password	= request.getParameter("password");
	    int rowLimit = 0;
	    if (StringUtil.isDefined(request.getParameter("rowLimit")))
	    {
	    	rowLimit = Integer.parseInt(request.getParameter("rowLimit"));
	    }
	    try
	    {
	    	myDBSC.updateConnection(jdbcDriverName, jdbcUrl, login, password, rowLimit);
	    	myDBSC.initJdbcConnectorSetting();
	    }
	    catch (Exception e)
	    {
			SilverTrace.warn("myDB", "MyDBRequestRouter.getDestination()", "myDB.MSG_CONNECTION_NOT_STARTED", e);
	    }
	    if (myDBSC.checkConnection())
	    {
	    	if (myDBSC.checkTableName())
	    	{
	    		return PAGE_CONSULTATION;
	    	}
	    	else
	    	{
	    		return PAGE_TABLE_SELECTION;
	    	}
	    }
	    else
	    {
	    	request.setAttribute("errorMessage", myDBSC.getString("CheckConnectionSetting"));
	    	return PAGE_CONNECTION_SETTING;
	    }
    }
    
    /**
     * Update (modification or deletion) of a database record.
     * 
     * @param request The HTTP request.
     * @param myDBSC The session control.
     * @return The destination corresponding to the record detail page.
     * @throws MyDBException
     * @throws FormException
     */
    private String getUpdateDataDestination(HttpServletRequest request, MyDBSessionController myDBSC)
    	throws MyDBException, FormException
    {
    	final int index = Integer.parseInt(request.getParameter("index"));
    	myDBSC.setLineIndex(index);
    	final String command = request.getParameter("command");
    	if ("view".equals(command))
    	{
    		// Consultation.
			initForm(request, myDBSC, true, false);
			request.setAttribute("consultation", "true");
    		return PAGE_TABLE_LINE;
    	}
    	else if ("modify".equals(command))
    	{
    		// Modification.
    		initForm(request, myDBSC, false, false);
    		return PAGE_TABLE_LINE;
    	}
    	else
    	{
    		// Deletion.
    		String deletionErrorMessage = myDBSC.getLineDeletionErrorMessage();
    		if (deletionErrorMessage == null)
    		{
    			myDBSC.deleteDbData();
    		}
    		if (deletionErrorMessage != null)
    		{
    			request.setAttribute("errorMessage", deletionErrorMessage);
    		}
    		return PAGE_CONSULTATION;
    	}
    }
    
    /**
     * Validation of the update (creation or modification) of a database record.
     * 
     * @param request The HTTP request.
     * @param myDBSC The session control.
     * @return The destination corresponding to the validation of the database record : the record form if an error is
     * 		   detected, the table detail else.
     * @throws MyDBException
     * @throws FormException
     */
    private String getUpdateLineDestination(HttpServletRequest request, MyDBSessionController myDBSC)
    	throws MyDBException, FormException
    {
    	final String command = request.getParameter("command");
    	myDBSC.initFormParameters(request.getParameterMap());
    	
    	String errorMessage = null;
		if ("create".equals(command))
		{
    		errorMessage = myDBSC.getLineCreationErrorMessage();
    		if (errorMessage == null)
    		{
    			errorMessage = myDBSC.createDbLine();
    		}
		}
		else
		{
			errorMessage = myDBSC.updateDbData();
		}
		if (errorMessage != null)
		{
			initForm(request, myDBSC, false, true);
			request.setAttribute("errorMessage", errorMessage);
			request.setAttribute("command", command);
    		return PAGE_TABLE_LINE;
		}
		else
		{
			return PAGE_CONSULTATION;
		}
    }
    
    /**
     * New record creation.
     * 
     * @param request The HTTP request.
     * @param myDBSC The session control.
     * @return The destination corresponding to a record creation form.
     * @throws MyDBException
     * @throws FormException
     */
    private String getAddLineDestination(HttpServletRequest request, MyDBSessionController myDBSC)
    	throws MyDBException, FormException
    {
    	myDBSC.removeLineIndex();
    	myDBSC.initFormParameters(null);
    	initForm(request, myDBSC, false, true);
    	request.setAttribute("command", "create");
    	return PAGE_TABLE_LINE;
    }
    
    /**
     * Data filter.
     * 
     * @param request The HTTP request.
     * @param myDBSC The session control.
     * @return The destination corresponding to the table filtered detail.
     */
    private String getFilterDestination(HttpServletRequest request, MyDBSessionController myDBSC)
    { 
    	String filterColumn = request.getParameter("filterColumn");
    	String filterCompare = request.getParameter("filterCompare");
    	String filterValue = request.getParameter("filterValue");
    	myDBSC.updateDbFilter(filterColumn, filterCompare, filterValue);
    	return PAGE_CONSULTATION;
    }
    
    /**
     * Table mode : creation or modification of a table.
     * For the moment, only creation mode is managed.$
     * 
     * @param request The HTTP request.
     * @param myDBSC The session control.
     * @return The destination corresponding to the different actions linked to a table update.
     * @throws MyDBException
     */
    private String getUpdateTableDestination(HttpServletRequest request, MyDBSessionController myDBSC)
    	throws MyDBException
    {
    	String command = request.getParameter("command");
    	if (command.equals("init"))
    	{
    		// Initialization of the table mode.
    		int mode = Integer.parseInt(request.getParameter("mode"));
    		String originPage = request.getParameter("originPage");
    		myDBSC.initTableManager(mode, originPage);
    		return PAGE_TABLE_UPDATE;
    	}
    	
    	TableManager tableManager = myDBSC.getTableManager();
		if (tableManager.isCreationMode())
		{
			// Table name update.
			String tableName = request.getParameter("tableName");
			if (tableName != null)
			{
				tableManager.getTable().setName(tableName);
			}
		}
		
    	if (command.equals("update"))
    	{
    		// Validation of the creation/modification of the table.
    		if (tableManager.isCreationMode()
    			&& !tableManager.isValidTableName(myDBSC.getTableNames(), myDBSC.getResources()))
    		{
    			return PAGE_TABLE_UPDATE;
    		}
    		if (myDBSC.createTable())
    		{
    			myDBSC.setTableName(tableManager.getTable().getName());
    			myDBSC.resetTableManager();
    			return PAGE_CONSULTATION;
    		}
    		else
    		{
    			myDBSC.dropTable();
    		}
    	}
    	else if (command.equals("cancel"))
    	{
    		// Cancellation of the creation/modification of the table.
    		String destination = tableManager.getOriginPage();
    		myDBSC.resetTableManager();
    		return destination;
    	}
    	else if (command.endsWith("Column"))
    	{
    		// Column command.
    		return getUpdateTableColumnDestination(request, myDBSC);
    	}
    	else if (command.endsWith("PK"))
    	{
    		// Primary key command.
    		return getUpdateTablePrimaryKeyDestination(request, myDBSC);
    	}
    	else if (command.endsWith("UK"))
    	{
    		// Unicity key command.
    		return getUpdateTableUnicityKeyDestination(request, myDBSC);
    	}
    	else if (command.endsWith("FK"))
    	{
    		// Foreign key command.
    		return getUpdateTableForeignKeyDestination(request, myDBSC);
    	}
    	return PAGE_TABLE_UPDATE;
    }
    
    /**
     * Creates the objects used to initialize the XML form describing a database record.
     * 
     * @param request The HTTP request.
     * @param myDBSC The session control.
     * @param consultation Indicates if data have to be displayed as labels or input fields.
     * @param newRecord Indicates if the form corresponds to a creation or a modification of a database record.
     * @throws MyDBException
     * @throws FormException
     */
    private void initForm(HttpServletRequest request, MyDBSessionController myDBSC, boolean consultation,
    		boolean newRecord)
    	throws MyDBException, FormException
    {
		Form form = myDBSC.getForm(consultation, newRecord, getSessionControlBeanName());
		request.setAttribute("form", form);
		
		PagesContext context = new PagesContext("detail", "0", myDBSC.getLanguage());
		request.setAttribute("context", context);
		
		DataRecord data = myDBSC.getRecord(newRecord);
		request.setAttribute("data", data);
    }
    
    /**
     * Table mode : operation on a column.
     * 
     * @param request The HTTP request.
     * @param myDBSC The session control.
     * @return The destination corresponding to the current column operation.
     * @throws MyDBException
     */
    private String getUpdateTableColumnDestination(HttpServletRequest request, MyDBSessionController myDBSC)
		throws MyDBException
	{
		String command = request.getParameter("command");
		TableManager tableManager = myDBSC.getTableManager();
    	if (command.equals("displayColumn"))
    	{
    		// Column detail (creation or modification).
    		request.setAttribute("index", request.getParameter("index"));
    		if ("true".equals(request.getParameter("errorColumn")))
    		{
    			request.setAttribute("errorColumn", "true");
    		}
    		return PAGE_TABLE_COLUMN;
    	}
    	else if (command.equals("validateColumn"))
    	{
    		// Validation of the creation/modification of a column.
    		int index = Integer.parseInt(request.getParameter("index"));
    		String columnName = request.getParameter("columnName");
    		String param = request.getParameter("columnType");
    		int columnType = (param != null ? Integer.parseInt(param) : DbColumn.DEFAULT_DATA_TYPE);
    		param = request.getParameter("columnSize");
    		int columnSize = (param != null ? Integer.parseInt(param) : DbColumn.DEFAULT_DATA_SIZE);
    		boolean columnNotNull = "true".equals(request.getParameter("columnNotNull"));
    		String columnDefaultValue = request.getParameter("columnDefaultValue");
    		DbColumn temporaryColumn = new DbColumn(
    			columnName, columnType, columnSize, !columnNotNull, columnDefaultValue);
    		boolean isValidColumn = tableManager.isValidColumn(temporaryColumn, myDBSC.getResources(), index);
    		if (isValidColumn)
    		{
        		if (index == -1)
        		{
        			tableManager.getTable().addColumn(temporaryColumn);
        		}
        		else
        		{
        			tableManager.updateColumn(temporaryColumn, index);
        		}
    		}
    		else
    		{
    			tableManager.setErrorColumn(temporaryColumn);
    			request.setAttribute("errorColumn", "true");
    			request.setAttribute("index", String.valueOf(index));
    		}
    	}
    	else if (command.equals("modifyColumn"))
    	{
    		// Column update, in order to satisfy the definition of a foreign key (type or size of a column describing a
    		// foreign key).
    		String name = request.getParameter("columnName");
    		int type = Integer.parseInt(request.getParameter("type"));
    		String value = request.getParameter("correctedValue");
    		tableManager.updateColumn(name, type, value);
    	}
    	else if (command.equals("removeColumn"))
    	{
    		// Column deletion.
    		int index = Integer.parseInt(request.getParameter("index"));
    		tableManager.removeColumn(index);
    	}
    	return PAGE_TABLE_UPDATE;
	}
    
    /**
     * Table mode : operation on a primary key.
     * 
     * @param request The HTTP request.
     * @param myDBSC The session control.
     * @return The destination corresponding to the current primary key operation.
     * @throws MyDBException
     */
    private String getUpdateTablePrimaryKeyDestination(HttpServletRequest request, MyDBSessionController myDBSC)
		throws MyDBException
	{
    	String command = request.getParameter("command");
    	TableManager tableManager = myDBSC.getTableManager();
    	if (command.equals("displayPK"))
    	{
    		// Creation/modification of the primary key.
    		return PAGE_PRIMARY_KEY;
    	}
    	else if (command.equals("validatePK"))
    	{
    		// Validation of the creation/modification of the primary key.
    		String name = request.getParameter("name");
    		PrimaryKey currentPrimaryKey = new PrimaryKey(tableManager.getTable());
    		currentPrimaryKey.update(name, request.getParameterMap());
    		if (tableManager.isValidKeyName(currentPrimaryKey, myDBSC.getResources(), -1))
    		{
    			tableManager.updatePrimaryKey(currentPrimaryKey);
    		}
    		else
    		{
    			request.setAttribute("primaryKey", currentPrimaryKey);
    			return PAGE_PRIMARY_KEY;
    		}
    	}
    	else if (command.equals("removePK"))
    	{
    		// Primary key deletion.
    		tableManager.getPrimaryKey().clear();
    	}
    	return PAGE_TABLE_UPDATE;
	}
    
    /**
     * Table mode : operation on a unicity key.
     * 
     * @param request The HTTP request.
     * @param myDBSC The session control.
     * @return The destination corresponding to the current unicity key operation.
     * @throws MyDBException
     */
    private String getUpdateTableUnicityKeyDestination(HttpServletRequest request, MyDBSessionController myDBSC)
		throws MyDBException
	{
    	String command = request.getParameter("command");
    	TableManager tableManager = myDBSC.getTableManager();
    	if (command.equals("displayUK"))
    	{
    		// Creation/modification of a unicity key.
    		request.setAttribute("index", request.getParameter("index"));
    		return PAGE_UNICITY_KEY;
    	}
    	else if (command.equals("validateUK"))
    	{
    		// Validation of the creation/modification of a unicity key.
    		int index = Integer.parseInt(request.getParameter("index"));
    		String name = request.getParameter("name");
    		UnicityKey currentUnicityKey = new UnicityKey(name);
    		currentUnicityKey.update(request.getParameterMap(), tableManager.getTable());
    		if (tableManager.isValidKeyName(currentUnicityKey, myDBSC.getResources(), index))
    		{
    			tableManager.getUnicityKeys().update(currentUnicityKey, index);
    		}
    		else
    		{
    			request.setAttribute("unicityKey", currentUnicityKey);
    			request.setAttribute("index", String.valueOf(index));
    			return PAGE_UNICITY_KEY;
    		}
    	}
    	else if (command.equals("removeUK"))
    	{
    		// Unicity key deletion.
    		int index = Integer.parseInt(request.getParameter("index"));
    		tableManager.getUnicityKeys().remove(index);
    	}
    	return PAGE_TABLE_UPDATE;
	}
    
    /**
     * Table mode : operation on a foreign key.
     * 
     * @param request The HTTP request.
     * @param myDBSC The session control.
     * @return The destination corresponding to the current foreign key operation.
     * @throws MyDBException
     */
    private String getUpdateTableForeignKeyDestination(HttpServletRequest request, MyDBSessionController myDBSC)
		throws MyDBException
	{
    	String command = request.getParameter("command");
    	TableManager tableManager = myDBSC.getTableManager();
    	if (command.equals("displayFK"))
    	{
    		// Creation/modification of a foreign key.
    		request.setAttribute("index", request.getParameter("index"));
    		return PAGE_FOREIGN_KEY;
    	}
    	else if (command.equals("validateFK"))
    	{
    		// Validation of the creation/modification of a foreign key.
    		int index = Integer.parseInt(request.getParameter("index"));
    		String name = request.getParameter("name");
    		String foreignTable = request.getParameter("foreignTable");
			String[] columnsNames;
			DbColumn[] foreignColumns;
    		
    		String foreignColumnsNamesKey = request.getParameter("foreignColumnsNames");
    		if (foreignColumnsNamesKey.length() > 0)
    		{
        		String[] foreignColumnsNames = DbUtil.getListFromKeys(foreignColumnsNamesKey);
        		int columnsCount = foreignColumnsNames.length;
    			columnsNames = new String[columnsCount];
    			foreignColumns = new DbColumn[columnsCount];
    			String foreignColumnName;
    			int foreignColumnType;
    			int foreignColumnSize;
    			for (int i = 0; i < columnsCount; i++)
    			{
    				columnsNames[i] = request.getParameter("column_" + i);
    				foreignColumnName = foreignColumnsNames[i];
            		foreignColumnType = Integer.parseInt(request.getParameter("foreignColumnType_" + i));
            		foreignColumnSize = Integer.parseInt(request.getParameter("foreignColumnSize_" + i));
            		foreignColumns[i] = new DbColumn(foreignColumnName, foreignColumnType, foreignColumnSize);
    			}
    		}
    		else
    		{
    			columnsNames = new String[] {request.getParameter("column_0")};
    			foreignColumns = new DbColumn[0];
    		}

    		ForeignKey currentForeignKey = new ForeignKey(name, columnsNames, foreignTable, foreignColumns);
    		if ((!"true".equals(request.getParameter("refreshForeignColumns")))
    			&& (tableManager.isValidKeyName(currentForeignKey, myDBSC.getResources(), index)))
    		{
    			tableManager.getForeignKeys().update(currentForeignKey, index);
    		}
    		else
    		{
    			request.setAttribute("foreignKey", currentForeignKey);
    			request.setAttribute("index", String.valueOf(index));
    			return PAGE_FOREIGN_KEY;
    		}
    	}
    	else if (command.equals("removeFK"))
    	{
    		// Foreign key deletion.
    		int index = Integer.parseInt(request.getParameter("index"));
    		tableManager.getForeignKeys().remove(index);
    	}
    	return PAGE_TABLE_UPDATE;
	}

}