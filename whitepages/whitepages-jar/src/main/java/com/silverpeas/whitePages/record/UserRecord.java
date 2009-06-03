package com.silverpeas.whitePages.record;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.FormException;
import com.silverpeas.form.fieldType.TextFieldImpl;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;

public class UserRecord implements DataRecord
{
   private UserDetail user = null;

   
   /**
    * A UserRecord is built from a UserDetail
    */
   public UserRecord(UserDetail user)
   {
      this.user = user;
   }

   public UserDetail getUserDetail()
   {
		return this.user;
   }
   /**
    * Returns the data record id.
	 *
	 * The record is known by its external id.
    */
   public String getId()
   {
      return user.getId();
   }

   /**
    * Gives an id to the record.
	 *
	 * Caution ! the record is known by its external id.
    */
   public void setId(String id)
   {
   }

   /**
    * Returns all the fields
    */
   public Field[] getFields()
   {
   	
      return null;
   }


   /**
    * Returns the named field.
    *
    * @throw FormException when the fieldName is unknown.
    */
   public Field getField(String fieldName) throws FormException
   {
	   	TextFieldImpl text = new TextFieldImpl();
	   	
	   	if (fieldName.equals("Id"))
	  		text.setStringValue(user.getId());
	  	else if (fieldName.equals("SpecificId"))
	  		text.setStringValue(user.getSpecificId());
	  	else if (fieldName.equals("DomainId"))
	  		text.setStringValue(user.getDomainId());
		else if (fieldName.equals("Login"))
	  		text.setStringValue(user.getLogin());
	  	else if (fieldName.equals("FirstName"))
	  		text.setStringValue(user.getFirstName());
	  	else if (fieldName.equals("LastName"))
	  		text.setStringValue(user.getLastName());
	  	else if (fieldName.equals("Mail"))
	  		text.setStringValue(user.geteMail());
	  	else if (fieldName.equals("AccessLevel"))
	  		text.setStringValue(user.getAccessLevel());
	  	else if (fieldName.startsWith("DC.")) {
	  		String specificDetail = fieldName.substring( fieldName.indexOf(".") + 1 );
	  		if (user instanceof UserFull)
        	{
	  			text.setStringValue(((UserFull)user).getValue(specificDetail));
	  		}
	  	}

        return text;
   }

  /**
   * Returns the field at the index position in the record.
   *
   * @throw FormException when the fieldIndex is unknown.
   */
  public Field getField(int fieldIndex) throws FormException
  {
     return null;
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
      return false;
   }

   /**
    * Gets the internal id.
    *
    * May be used only by a package class !
    */
   int getInternalId()
   {
	   return -1;
   }

   /**
    * Sets the internal id.
    *
    * May be used only by a package class !
    */
   void setInternalId(int id)
   {
   }

   public String getLanguage() {
	   return I18NHelper.defaultLanguage;
   }

   public void setLanguage(String language) {
	   
   }   
}
