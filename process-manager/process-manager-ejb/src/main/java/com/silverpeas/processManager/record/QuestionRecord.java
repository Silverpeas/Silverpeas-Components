package com.silverpeas.processManager.record;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.FormException;
import com.silverpeas.form.fieldType.TextFieldImpl;

public class QuestionRecord implements DataRecord
{
	String content = null;
	String id = null;
	TextFieldImpl contentField = null;

	/**
	 * A QuestionRecord is built from a Question
	 */
	public QuestionRecord(String content)
	{
		this.content = content;
		contentField = new TextFieldImpl();
		contentField.setStringValue(content);
	}

	/**
	 * Returns the data record id.
	 *
	 * The record is known by its external id.
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * Gives an id to the record.
	 *
	 * Caution ! the record is known by its external id.
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	/**
	 * Returns all the fields
	 */
	public Field[] getFields()
	{
		try
		{
			Field[] fields = new Field[1];
			fields[0] = getField("Content");
			return fields;
		}
		catch (FormException fe)
		{
			return null;
		}
	}


	/**
	* Returns the named field.
	*
	* @throw FormException when the fieldName is unknown.
	*/
	public Field getField(String fieldName) throws FormException
	{
		if (fieldName.equals("Content"))
			return getField(0);
		else
			throw new FormException("QuestionRecord", "workflowEngine.ERR_FIELD_NOT_FOUND");
	}

	/**
	 * Returns the field at the index position in the record.
	 *
	 * @throw FormException when the fieldIndex is unknown.
	 */
	public Field getField(int fieldIndex) throws FormException
	{
		if (fieldIndex == 0)
			return contentField;
		else
			throw new FormException("QuestionRecord", "workflowEngine.ERR_FIELD_NOT_FOUND");
	}
	
	public String[] getFieldNames()
	{
		return null;
	}

	/**
	 * Return true if this record has not been inserted in a RecordSet.
	 */
	public boolean isNew()
	{
		return true;
	}
	
	public String getLanguage() {
		return null;
	}

	public void setLanguage(String language) {
	}
}