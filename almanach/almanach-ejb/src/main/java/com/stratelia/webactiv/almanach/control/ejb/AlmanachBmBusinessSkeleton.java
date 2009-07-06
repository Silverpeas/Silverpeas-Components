package com.stratelia.webactiv.almanach.control.ejb;

import java.util.Collection;
import java.rmi.RemoteException;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.RRule;

import com.stratelia.webactiv.almanach.model.*;

public interface AlmanachBmBusinessSkeleton
{
	
	/**
	   * Get the events of the month
	   * @author dlesimple
	   * @param pk
	   * @param date
	   * @param String[] of instanceIds
	   * @return Collection of Events
	   */
	  public Collection getMonthEvents(EventPK pk, java.util.Date date, String[] instanceIds) throws RemoteException;

	  /**
	   * Get the events of the month
	   * @author dlesimple
	   * @param pk
	   * @param date
	   * @return Collection of Events
	   */
	  public Collection getMonthEvents(EventPK pk, java.util.Date date) throws RemoteException;
	  
  
  /**
   * this method provide a collection of event
   * @param : EventPk pk, to obtain the space and component
   * @ return: java.util.Collection
   */
  public Collection getAllEvents(EventPK pk) throws RemoteException;
  
  /**
   * Get all events of instanceId Almanachs
   * @param pk
   * @param String[] of instanceIds
   * @return Collection of Events
   */
  public Collection getAllEvents(EventPK pk, String[] instanceIds) throws RemoteException;

  public Collection getEvents(Collection pks) throws RemoteException;

  /**
   * addEvent()
   * add an event entry in the database
   */
  public String addEvent(EventDetail event) throws RemoteException;
  
  
  /**
   * updateEvent()
   * update the event entry, specified by the pk, in the database
   */
  public void updateEvent(EventDetail event) throws RemoteException;
  
  /**
   * removeEvent()
   * remove the Event entry specified by the pk
   */
  public void removeEvent(EventPK pk) throws RemoteException;
  
  /**
   * getEventDetail()
   * returns the EventDetail represented by the pk
   */
  public EventDetail getEventDetail(EventPK pk) throws RemoteException;

  public int getSilverObjectId(EventPK pk) throws RemoteException;
  
  public void createIndex(EventDetail detail) throws RemoteException;
  
  public Collection getNextEvents(String instanceId, int nbReturned) throws RemoteException;
  
  public void addPeriodicity(Periodicity periodicity) throws RemoteException;
  
  public Periodicity getPeriodicity(String eventId) throws RemoteException;
  
  public void removePeriodicity(Periodicity periodicity) throws RemoteException;
  
  public void updatePeriodicity(Periodicity periodicity) throws RemoteException;
  
  public void addPeriodicityException(PeriodicityException exception) throws RemoteException;
  
  public Collection getListPeriodicityException(String periodicityId) throws RemoteException;
  
  public void removeAllPeriodicityException(String periodicityId) throws RemoteException;
  
  public Calendar getICal4jCalendar(Collection events, String language) throws RemoteException;
  public Collection getListRecurrentEvent(Calendar calendarAlmanach, java.util.Calendar currentDay, String spaceId, String instanceId) throws RemoteException;
  public RRule generateRecurrenceRule(Periodicity periodicity) throws RemoteException;
  public ExDate generateExceptionDate(Periodicity periodicity) throws RemoteException;
  
  /**************************************************************************************/
  /* Interface - Fichiers joints	                                                    */
  /**************************************************************************************/
  public Collection getAttachments(EventPK eventPK) throws RemoteException;

  public String getHTMLPath(EventPK eventPK) throws RemoteException;
}