/*
 * Created on 25 oct. 2004
 *
 */
package com.silverpeas.projectManager.control.ejb;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import com.silverpeas.projectManager.model.Filtre;
import com.silverpeas.projectManager.model.HolidayDetail;
import com.silverpeas.projectManager.model.TaskDetail;

/**
 * @author neysseri
 * 
 */
public interface ProjectManagerBmBusinessSkeleton {

  public List getProjects(String instanceId) throws RemoteException;

  public int addTask(TaskDetail task) throws RemoteException;

  public void removeTask(int id, String instanceId) throws RemoteException;

  public void updateTask(TaskDetail task, String userId) throws RemoteException;

  public void calculateAllTasksDates(String instanceId, int projectId,
      String userId) throws RemoteException;

  // public void putBackTask(TaskDetail task, Date previousTaskEndDate) throws
  // RemoteException;

  public List getTasksByMotherId(String instanceId, int motherId)
      throws RemoteException;

  public List getTasksByMotherId(String instanceId, int motherId, Filtre filtre)
      throws RemoteException;

  public List getTasksNotCancelledByMotherId(String instanceId, int motherId,
      Filtre filtre) throws RemoteException;

  public List getTasksByMotherIdAndPreviousId(String instanceId, int motherId,
      int previousId) throws RemoteException;

  public List getAllTasks(String instanceId, Filtre filtre)
      throws RemoteException;

  public TaskDetail getTask(int id) throws RemoteException;

  public TaskDetail getTaskByTodoId(String todoId) throws RemoteException;

  public TaskDetail getMostDistantTask(String instanceId, int taskId)
      throws RemoteException;

  // Gestion des jours non travaillés
  public boolean isHolidayDate(HolidayDetail date) throws RemoteException;

  public List getHolidayDates(String instanceId) throws RemoteException;

  public List getHolidayDates(String instanceId, Date beginDate, Date endDate)
      throws RemoteException;

  public void addHolidayDate(HolidayDetail holiday) throws RemoteException;

  public void addHolidayDates(List holidayDates) throws RemoteException;

  public void removeHolidayDate(HolidayDetail holiday) throws RemoteException;

  public void removeHolidayDates(List holidayDates) throws RemoteException;

  public Date processEndDate(TaskDetail task) throws RemoteException;

  public Date processEndDate(float charge, String instanceId, Date dateDebut)
      throws RemoteException;

  public void index(String instanceId) throws RemoteException;

  public int getOccupationByUser(String userId, Date dateDeb, Date dateFin)
      throws RemoteException;

  public int getOccupationByUser(String userId, Date dateDeb, Date dateFin,
      int excludedTaskId) throws RemoteException;
}