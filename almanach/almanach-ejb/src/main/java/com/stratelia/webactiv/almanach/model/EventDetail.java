package com.stratelia.webactiv.almanach.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import com.silverpeas.util.i18n.AbstractI18NBean;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.wysiwyg.WysiwygException;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;

public class EventDetail extends AbstractI18NBean implements SilverContentInterface, Serializable {
	private String _name = null;
	private EventPK _pk = null;
	private Date _startDate = null;
	private Date _endDate = null;
	private String _delegatorId = null;
	private int _priority = 0;
	private String _title = null;
	private String _iconUrl = "";
	private String startHour;
	private String endHour;
	private String place;
	private String eventUrl;
	private Periodicity periodicity;
	
	public String getPlace() {
		return place;
	}
	public void setPlace(String place) {
		this.place = place;
	}
	public EventDetail() {
	}

	public EventDetail(EventPK pk, String name) {
		setPK(pk);
		setNameDescription(name);
	}
	
	public EventDetail(String _name, EventPK _pk, int _priority, String _title, String startHour, String endHour, String place, String eventUrl) {
		this._name = _name;
		this._pk = _pk;
		this._priority = _priority;
		this._title = _title;
		this.startHour = startHour;
		this.endHour = endHour;
		this.place = place;
		this.eventUrl = eventUrl;
	}
	
	public EventPK getPK() {
		return _pk;
	}

	public void setPK(EventPK pk) {
		_pk = pk;
	}

	public String getNameDescription() {
		return _name;
	}

	public String getName() {
		return _title;
	}

	public void setNameDescription(String name) {
		_name = name;
	}

	public String getDelegatorId() {
		return _delegatorId;
	}

	public void setDelegatorId(String delegatorId) {
		_delegatorId = delegatorId;
	}

	public int getPriority() {
		return _priority;
	}

	public void setPriority(int priority) {
		_priority = priority;
	}

	public Date getStartDate() {
		return _startDate;
	}

	public void setStartDate(Date date) {
		_startDate = date;
	}

	public Date getEndDate() {
		return _endDate;
	}

	public void setEndDate(Date date) {
		_endDate = date;
	}

	public String getTitle() {
		return _title;
	}
	public void setTitle(String title) {
		_title = title;
	}

	public String getDescription() {
		return getNameDescription();
	}

	public String getURL() {
		return "searchResult?Type=Event&Id=" + getId();
	}

	public String getId() {
		return getPK().getId();
	}

	public String getInstanceId() {
		return getPK().getComponentName();
	}

	public String getDate() {
		return null;
	}
	
	public String getSilverCreationDate()
	{
		return null;
	}

	public void setIconUrl(String iconUrl) {
		this._iconUrl = iconUrl;
	}

	public String getIconUrl() {
		return this._iconUrl;
	}

	public String getCreatorId() {
		return getDelegatorId();
	}
	public String getEndHour() {
		return endHour;
	}
	public void setEndHour(String endHour) {
		this.endHour = endHour;
	}
	public String getStartHour() {
		return startHour;
	}
	public void setStartHour(String startHour) {
		this.startHour = startHour;
	}
	public String getEventUrl() {
		return eventUrl;
	}
	public void setEventUrl(String eventUrl) {
		this.eventUrl = eventUrl;
	}
	public String getPermalink()
	{
		if (URLManager.displayUniversalLinks())
			return URLManager.getApplicationURL()+"/Event/"+ getId();
		
		return null;
	}
	
	public String getDescription(String language) {
		return getDescription();
	}

	public String getName(String language) {
		return getName();
	}
	
	public Iterator getLanguages()
	{
		return null;
	}

	public String getWysiwyg() throws WysiwygException {
		String wysiwygContent = null;
		wysiwygContent = WysiwygController.loadFileAndAttachment(getPK().getSpace(), getPK().getComponentName(), getPK().getId());
		return wysiwygContent;
	}
	
	public Periodicity getPeriodicity() {
		return periodicity;
	}
	
	public void setPeriodicity(Periodicity periodicity) {
		this.periodicity = periodicity;
	}
	
	public int getNbDaysDuration() { 
		int nbDaysDuration = 0;
		
		if(_endDate != null) {
			Calendar calStartDate = Calendar.getInstance();
			calStartDate.setTime(_startDate);
			
			Calendar calEndDate = Calendar.getInstance();
			calEndDate.setTime(_endDate);
			
			while(! calStartDate.equals(calEndDate)) {
				calStartDate.add(Calendar.DATE, 1);
				nbDaysDuration++;
			}
		}
	
		return nbDaysDuration;
	}
}