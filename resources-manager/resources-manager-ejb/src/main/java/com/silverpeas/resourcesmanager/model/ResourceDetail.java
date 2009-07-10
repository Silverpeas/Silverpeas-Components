package com.silverpeas.resourcesmanager.model;

import java.io.Serializable;
import java.util.Date;

public class ResourceDetail implements Serializable {
	private String id;
	private String categoryId;
	private String name;
	private Date creationDate;
	private Date updateDate;
	private String description;
	private String responsibleId;
	private String createrId;
	private String updaterId;
	private String instanceId;
	private boolean bookable;
	
	public boolean getBookable() {
		return bookable;
	}
	public void setBookable(boolean bookable) {
		this.bookable = bookable;
	}
	public String getCreaterId() {
		return createrId;
	}
	public void setCreaterId(String createrId) {
		this.createrId = createrId;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}
	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getResponsibleId() {
		return responsibleId;
	}
	public void setResponsibleId(String responsibleId) {
		this.responsibleId = responsibleId;
	}
	
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	public String getUpdaterId() {
		return updaterId;
	}
	public void setUpdaterId(String updaterId) {
		this.updaterId = updaterId;
	}
	public ResourceDetail(String name,String categoryId, boolean bookable) {
		super();
		this.name = name;
		this.categoryId = categoryId;
		this.bookable = bookable;
	}
	public ResourceDetail(String name, String categoryId, String responsibleId, String description, boolean bookable) {
		super();
		this.categoryId = categoryId;
		this.name = name;
		this.description = description;
		this.responsibleId = responsibleId;
		this.bookable = bookable;
	}
	public ResourceDetail(String id, String categoryId, String name, Date creationDate, Date updateDate, String description, String responsibleId, String createrId, String updaterId, String instanceId, boolean bookable) {
		super();
		this.id = id;
		this.categoryId = categoryId;
		this.name = name;
		this.creationDate = creationDate;
		this.updateDate = updateDate;
		this.description = description;
		this.responsibleId = responsibleId;
		this.createrId = createrId;
		this.updaterId = updaterId;
		this.instanceId = instanceId;
		this.bookable = bookable;
	}
	public ResourceDetail(String id, String categoryId, String name, String description, String responsibleId, boolean bookable) {
		super();
		this.id = id;
		this.categoryId = categoryId;
		this.name = name;
		this.description = description;
		this.responsibleId = responsibleId;
		this.bookable = bookable;
	}
	
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof ResourceDetail)
		{
			ResourceDetail r = (ResourceDetail) obj;
			return r.getId().equals(this.getId());
		}
		return false;
	}
	
	
	
	
}
