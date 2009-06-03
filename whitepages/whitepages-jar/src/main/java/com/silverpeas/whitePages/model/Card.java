package com.silverpeas.whitePages.model;

import com.stratelia.webactiv.persistence.*;
import com.silverpeas.form.*;
import com.silverpeas.whitePages.record.*;

public class Card extends SilverpeasBean
{
   private String userId;
   private int hideStatus = 0;
   private String instanceId;
   private String	creationDate;
   private int		creatorId;

   private boolean readOnly = true;
   private UserRecord userRecord;
   private DataRecord cardRecord;
   private Form userForm;
   private Form cardViewForm;
   private Form cardUpdateForm;
   
   public Card() 
   {
   }

   public Card(String instanceId) 
   {
		setInstanceId(instanceId);
   }
     
   public String getUserId() {
		return this.userId;
   }
   
   public int getHideStatus() {
		return this.hideStatus;
   }
   
   public String getInstanceId() {
		return this.instanceId;
   }
   
   public boolean readReadOnly() {
		return this.readOnly;
   }   
   
   public UserRecord readUserRecord() {
		return this.userRecord;
   }
   
   public DataRecord readCardRecord() {
		return this.cardRecord;
   }
   
   public Form readUserForm() {
		return this.userForm;
   }
   
   public Form readCardViewForm() {
		return this.cardViewForm;
   }

   public Form readCardUpdateForm() {
		return this.cardUpdateForm;
   }
   
   public void setUserId(String userId) {
		this.userId = userId;
   }
   
   public void setHideStatus(int hideStatus) {
		this.hideStatus = hideStatus;
   }
   
   public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
   }   
   
   public void writeReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
   }   
   
   public void writeUserRecord(UserRecord userRecord) {
		this.userRecord = userRecord;
		setUserId(userRecord.getId());
   }
   
   public void writeCardRecord(DataRecord  cardRecord) {
		this.cardRecord = cardRecord;
   }
   
   public void writeUserForm(Form userForm) {
		this.userForm = userForm;
   }

   public void writeCardViewForm(Form cardViewForm) {
		this.cardViewForm = cardViewForm;
   }

   public void writeCardUpdateForm(Form cardUpdateForm) {
		this.cardUpdateForm = cardUpdateForm;
   }
   
  public String _getTableName()
  {
    return "SC_WhitePages_Card";
  }
  
  public int _getConnectionType()
  {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }  
  
  public void setCreationDate(String date) {
	this.creationDate = date;
  }

  public void setCreatorId(int creatorId) {
	this.creatorId = creatorId;
  }

  public String getCreationDate() {
	return this.creationDate;
  }

  public int getCreatorId() {
	return this.creatorId;
  }
  
}
