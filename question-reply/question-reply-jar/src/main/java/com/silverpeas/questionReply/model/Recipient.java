package com.silverpeas.questionReply.model;

import com.stratelia.webactiv.persistence.*;
import com.stratelia.webactiv.beans.admin.*;

public class Recipient extends SilverpeasBean
{
   private long questionId;
   private String userId;
   private static OrganizationController organizationController = new OrganizationController();

   public Recipient()
   {
   }
   public Recipient(String userId)
   {
		setUserId(userId);
   }
   public Recipient(long questionId, String userId)
   {
		setQuestionId(questionId);
		setUserId(userId);
   }

   public long getQuestionId() {
		return questionId;
   }  
   public String getUserId() {
		return this.userId;
   }
   public void setQuestionId(long questionId) {
		this.questionId = questionId;
   }   
   public void setUserId(String userId) {
		this.userId = userId;
   }

   public String readRecipientName()
   {
		String name = null;
		UserDetail userDetail = organizationController.getUserDetail(new Integer(getUserId()).toString());
		if (userDetail != null)		
			name = userDetail.getLastName() + " " + userDetail.getFirstName();		
		return name;
   }
   
   
  public String _getTableName()
  {
    return "SC_QuestionReply_Recipient";
  }
  
  public int _getConnectionType()
  {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }  
  
}
