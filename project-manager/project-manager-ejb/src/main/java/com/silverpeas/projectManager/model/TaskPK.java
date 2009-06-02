/*
 * Created on 13 avr. 2005
 *
 */
package com.silverpeas.projectManager.model;

import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * @author neysseri
 *
 */
public class TaskPK extends WAPrimaryKey {
	
	public TaskPK(String id, String componentId)
	{
		super(id, null, componentId);
	}
	
	public TaskPK(int id, String componentId)
	{
		super(Integer.toString(id), null, componentId);
	}
	
	public boolean equals(Object other) {
		if (!(other instanceof TaskPK)) 
			return false;
		else
			return (id.equals( ((TaskPK) other).getId()) ) &&
		   (componentName.equals(((TaskPK) other).getComponentName()) );
	}
}