package com.silverpeas.whitePages.model;

import com.stratelia.webactiv.beans.admin.*;


public class WhitePagesCard implements Comparable 
{
   private long userCardId = 0;
   private String instanceLabel;
   private String instanceId;
   private static OrganizationController organizationController = new OrganizationController();
   
   public WhitePagesCard() 
   {
   }

   public WhitePagesCard(String label) 
   {
		this.instanceLabel = label;
   }   

   public WhitePagesCard(long userCardId, String instanceId) 
   {
		setInstanceId(instanceId);
		setUserCardId(userCardId);
   }   
   
   public long getUserCardId() {
		return this.userCardId;
   }
   public String getInstanceId() {
		return this.instanceId;
   }
   
   public String readInstanceLabel() {
		return this.instanceLabel;
   }   
   
   public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
		this.instanceLabel = organizationController.getComponentInst(instanceId).getLabel();
   }   

   public void setUserCardId(long userCardId) {
		this.userCardId = userCardId;
   }
   
   public boolean equals(Object theOther)
   {
		return (readInstanceLabel().equals( ( (WhitePagesCard) theOther ).readInstanceLabel()) );
   }

   public int compareTo(Object theOther)
   {
		return (readInstanceLabel().compareTo( ( (WhitePagesCard) theOther ).readInstanceLabel()) );
   }

}
