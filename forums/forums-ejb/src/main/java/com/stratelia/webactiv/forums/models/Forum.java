package com.stratelia.webactiv.forums.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import com.stratelia.webactiv.forums.forumEntity.ejb.ForumPK;

public class Forum
	implements Serializable
{
	
	private int id;
	private String name;
	private String description;
	private boolean active;
	private int parentId;
	private String category;
	private String creationDate;
	private String instanceId;
	private ForumPK pk;
	
	public Forum(int id, String name, String description, boolean active, int parentId,
		String category)
	{
		this.id = id;
		this.name = name;
		this.description = description;
		this.active = active;
		this.parentId = parentId;
		this.category = category;
	}
	
	public Forum(int id, String name, String description, boolean active, int parentId,
		String category, String creationDate, String instanceId)
	{
		this(id, name, description, active, parentId, category);
		this.instanceId = instanceId;
		this.creationDate = creationDate;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}

	public int getParentId()
	{
		return parentId;
	}

	public void setParentId(int parentId)
	{
		this.parentId = parentId;
	}

	public String getCategory()
	{
		return category;
	}

	public void setCategory(String category)
	{
		this.category = category;
	}
	
	public String getCreationDate()
	{
		return creationDate;
	}

	public void setCreationDate(String creationDate)
	{
		this.creationDate = creationDate;
	}

	public String getInstanceId()
	{
		return instanceId;
	}

	public void setInstanceId(String instanceId)
	{
		this.instanceId = instanceId;
	}

	public ForumPK getPk()
	{
		return pk;
	}

	public void setPk(ForumPK pk)
	{
		this.pk = pk;
	}

}
