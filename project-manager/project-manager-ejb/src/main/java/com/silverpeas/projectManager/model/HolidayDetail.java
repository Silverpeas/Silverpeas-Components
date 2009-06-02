/*
 * Created on 15 déc. 2004
 *
 */
package com.silverpeas.projectManager.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author neysseri
 *
 */
public class HolidayDetail implements Serializable {

	private Date 	holidayDate = null;
	private String 	instanceId 	= null;
	private int 	fatherId	= -1;

	public HolidayDetail(Date holidayDate, int fatherId, String instanceId) {
		setDate(holidayDate);
		setInstanceId(instanceId);
		setFatherId(fatherId);
	}

	/**
	 * @return
	 */
	public int getFatherId() {
		return fatherId;
	}

	/**
	 * @return
	 */
	public Date getDate() {
		return holidayDate;
	}

	/**
	 * @return
	 */
	public String getInstanceId() {
		return instanceId;
	}

	/**
	 * @param i
	 */
	public void setFatherId(int i) {
		fatherId = i;
	}

	/**
	 * @param date
	 */
	public void setDate(Date date) {
		holidayDate = date;
	}

	/**
	 * @param string
	 */
	public void setInstanceId(String string) {
		instanceId = string;
	}
	
}
