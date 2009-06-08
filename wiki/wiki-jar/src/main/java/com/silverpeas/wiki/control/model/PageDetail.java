package com.silverpeas.wiki.control.model;

public class PageDetail {
	int id = -1;
	String pageName = null;
	String instanceId = null;

	public PageDetail() {
	}	
		
	public PageDetail(int id, String pageName, String instanceId) {
		this.id = id;
		this.pageName = pageName;
		this.instanceId = instanceId;
	}

	/**
	 * @return the instanceId
	 */
	public String getInstanceId() {
		return instanceId;
	}

	/**
	 * @param instanceId
	 *            the instanceId to set
	 */
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the pageName
	 */
	public String getPageName() {
		return pageName;
	}

	/**
	 * @param pageName
	 *            the pageName to set
	 */
	public void setPageName(String pageName) {
		this.pageName = pageName;
	}

}
