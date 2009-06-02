/*
 * Created on 25 oct. 2004
 *
 */
package com.silverpeas.projectManager.model;

import java.io.Serializable;


public class TaskResourceDetail implements Serializable {

	private int		id;
	private int 	taskId;
	private String	userId;
	private int 	charge;
	private String 	instanceId;
	private int		occupation;
	
	private String userName;
	
	
	public TaskResourceDetail()
	{
	}
	
	public TaskResourceDetail(int id, int taskId, String userId, int charge, String instanceId)
	{
		setId(id);
		setTaskId(taskId);
		setUserId(userId);
		setCharge(charge);
		setInstanceId(instanceId);
	}
	
	public void setId(int id)
	{
		this.id = id;
	}
	
	public int getId()
	{
		return id;
	}
	
	public int getCharge() {
		return charge;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public int getTaskId() {
		return taskId;
	}
	
	public String getUserId() {
		return userId;
	}

	public String getUserName() {
		return userName;
	}
	
	public int getOccupation() {
		return occupation;
	}
	
	public void setCharge(int f) {
		charge = f;
	}
	
	public void setCharge(String f) {
		if (f != null && f.length()>0)
			charge = new Integer(f).intValue();
		else
			charge = 0;
	}
	
	public void setOccupation(int f) {
		occupation = f;
	}
	
	public void setOccupation(String f) {
		if (f != null && f.length()>0)
			occupation = new Integer(f).intValue();
		else
			occupation = 0;
	}

	public void setTaskId(int i) {
		taskId = i;
	}
	
	public void setUserId(String i) {
		userId = i;
	}

	public void setInstanceId(String string) {
		instanceId = string;
	}

	public void setUserName(String s) {
		userName = s;
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("TaskResourceDetail {").append("\n");
		result.append("  id = ").append(getId()).append("\n");
		result.append("  taskId = ").append(getTaskId()).append("\n");
		result.append("  userId = ").append(getUserId()).append("\n");
		result.append("  charge = ").append(getCharge()).append("\n");
		result.append("  instanceId = ").append(getInstanceId()).append("\n");
		result.append("}");
		return result.toString();
	 }	

}