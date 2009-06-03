package com.silverpeas.whitePages.record;

import com.stratelia.webactiv.beans.admin.*;
import com.stratelia.webactiv.util.*;
import com.silverpeas.form.*;
import com.silverpeas.form.fieldType.TextField;
import com.silverpeas.form.form.*;
import com.silverpeas.form.record.*;


public class UserTemplate implements RecordTemplate
{
   private static ResourceLocator label = null;
   private HtmlForm viewForm;

   
    /**
    * A UserTemplate is built from a fileName and a language :
    * use addFieldTemplate for each field.
    * 
    * @see addFieldTemplate
    */
   public UserTemplate(String fileName, String language)
   {
	   	label = new ResourceLocator("com.silverpeas.whitePages.multilang.whitePagesBundle", language);
   		try {
			this.viewForm = new HtmlForm(this);
		} catch (FormException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   		viewForm.setFileName(fileName);
   }


   /**
    * Returns all the field names of the UserRecord built on this template.
    */
   public String[] getFieldNames()
   {
   	  String[] fieldNames = new String[9];
   	  fieldNames[0] = "Id";
   	  fieldNames[1] = "SpecificId";
   	  fieldNames[2] = "DomainId";
   	  fieldNames[3] = "Login";
   	  fieldNames[4] = "FirstName";
   	  fieldNames[5] = "LastName";
   	  fieldNames[6] = "Mail";
   	  fieldNames[7] = "AccessLevel";
   	  fieldNames[8] = "SpecificDetails"; 
   	  
   	  return fieldNames;
   }

   /**
    * Returns all the field templates.
    */
   public FieldTemplate[] getFieldTemplates()
   {
		FieldTemplate[] fieldTemplates = new FieldTemplate[9];
		try {
			fieldTemplates[0] = getFieldTemplate("Id");
			fieldTemplates[1] = getFieldTemplate("SpecificId");
			fieldTemplates[2] = getFieldTemplate("DomainId");
			fieldTemplates[3] = getFieldTemplate("Login");
			fieldTemplates[4] = getFieldTemplate("FirstName");
			fieldTemplates[5] = getFieldTemplate("LastName");
			fieldTemplates[6] = getFieldTemplate("Mail");
			fieldTemplates[7] = getFieldTemplate("AccessLevel");
			fieldTemplates[8] = getFieldTemplate("SpecificDetails");
		} catch (FormException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fieldTemplates;
   }
  

   /**
    * Returns the FieldTemplate of the named field.
    *
    * @throw FormException if the field name is unknown.
    */
   public FieldTemplate getFieldTemplate(String fieldName) throws FormException
   {
   		GenericFieldTemplate fieldTemplate = null;
   		
   		if (! fieldName.equals("SpecificDetails"))
		{
   			fieldTemplate = new GenericFieldTemplate(fieldName, TextField.TYPE);
				fieldTemplate.setLabel(label.getString(fieldName));
				fieldTemplate.setDisplayerName("simpletext");
				fieldTemplate.setReadOnly(true);
		}
		else {
				//pb car on n'a pas de UserDetail pour récupérer le nom
		}
   	  
      	return fieldTemplate;
   }

   /**
    * Returns the field index of the named field.
    *
    * @throw FormException if the field name is unknown.
    */
   public int getFieldIndex(String fieldName) throws FormException
   {
      return -1;
   }

   /**
    * Returns an empty DataRecord built on this template.
    */
   public DataRecord getEmptyRecord()
      throws FormException
   {
      return null;
   }

   /**
    * Returns true if the data record is built on this template
    * and all the constraints are ok.
    */
   public boolean checkDataRecord(DataRecord record)
	{
	   return true; 
	}
	
   /**
    * Returns the Form
    */	
	public Form getViewForm() {
		return viewForm;
	}
	
	/**
    * Returns the UserRecord
    */	
   	public UserRecord getRecord(String idUser) {
   		OrganizationController organisation = new OrganizationController();
   		UserDetail userDetail = organisation.getUserFull(idUser);
   		return new UserRecord(userDetail);
   	}

}


