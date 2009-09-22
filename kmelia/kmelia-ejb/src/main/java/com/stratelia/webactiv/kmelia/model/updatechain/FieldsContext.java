package com.stratelia.webactiv.kmelia.model.updatechain;


public class FieldsContext
{
	public static int ON_UPDATE_REPLACE_EMPTY_VALUES = 0;
	public static int ON_UPDATE_IGNORE_EMPTY_VALUES = 1;
	
	//String 	formName 			= "";
	//String 	formIndex 			= "0";
	String 	currentFieldIndex 	= "0";
	String 	language 			= "fr";
	//boolean printTitle 			= false;
	String 	componentId			= null;
	String 	userId				= null;
	//String	objectId			= null;
	//boolean versioningUsed		= false;
	//boolean printBorder			= true;
	//String 	contentLanguage		= "fr";
	//int		nbFields			= 0;
	//String	nodeId				= null;
	int		lastFieldIndex;
	boolean	useMandatory		= true; //used to modify several objects at the same time.
	//boolean useBlankFields		= false; //display all fields blank
	//boolean ignoreDefaultValues	= false; //do not display default value
	//String	xmlFormName			= "";
	//int		updatePolicy		= ON_UPDATE_REPLACE_EMPTY_VALUES;

	public FieldsContext()
	{
	}

	public FieldsContext(String language, String componentId, String userId)
	{
		setLanguage(language);
		setComponentId(componentId);
		setUserId(userId);
	}

	public String getCurrentFieldIndex()
	{
		return currentFieldIndex;
	}

	public String getLanguage()
	{
		return language;
	}
	
	public void setLanguage(String language)
	{
		this.language = language;
	}
	
	
	public void setCurrentFieldIndex(String currentFieldIndex)
	{
		this.currentFieldIndex = currentFieldIndex;
	}

	public void incCurrentFieldIndex(int increment)
	{
		int currentFieldIndexInt = 0;
		if (currentFieldIndex != null)
			currentFieldIndexInt = new Integer(currentFieldIndex).intValue();
		currentFieldIndexInt = currentFieldIndexInt + increment;
		this.currentFieldIndex = new Integer(currentFieldIndexInt).toString();
	}

		
	public String getUserId() {
		return userId;
	}

	public void setUserId(String string) {
		userId = string;
	}
	
	public int getLastFieldIndex() {
		return lastFieldIndex;
	}

	public void setLastFieldIndex(int lastFieldIndex) {
		this.lastFieldIndex = lastFieldIndex;
	}
	
	public boolean useMandatory() {
		return useMandatory;
	}

	public void setUseMandatory(boolean ignoreMandatory) {
		this.useMandatory = ignoreMandatory;
	}

	public String getComponentId() {
		return componentId;
	}

	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}

}