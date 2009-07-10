package com.silverpeas.resourcesmanager.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class ReservationDetail implements Serializable {

	private String id;
	private String event;
	private Date beginDate;
	private Date endDate;
	private String reason;
	private String place;
	private String userId;
	private Date creationDate;
	private Date updateDate;
	private String instanceId;
	private ArrayList listResourcesReserved;
	
	public ArrayList getListResourcesReserved() {
		return listResourcesReserved;
	}
	public void setListResourcesReserved(ArrayList listResourcesReserved) {
		this.listResourcesReserved = listResourcesReserved;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public Date getBeginDate() {
		return beginDate;
	}
	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
	public String getPlace() {
		return place;
	}
	public void setPlace(String place) {
		this.place = place;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public ReservationDetail(String event, Date beginDate, Date endDate, String reason, String place) {
		super();
		this.event = event;
		this.beginDate = beginDate;
		this.endDate = endDate;
		this.reason = reason;
		this.place = place;
	}
	public ReservationDetail(String id, String event, Date beginDate, Date endDate, String reason, String place, String userId, Date creationDate, Date updateDate, String instanceId) {
		super();
		this.id = id;
		this.event = event;
		this.beginDate = beginDate;
		this.endDate = endDate;
		this.reason = reason;
		this.place = place;
		this.userId = userId;
		this.creationDate = creationDate;
		this.updateDate = updateDate;
		this.instanceId = instanceId;
	}
	
	
}
