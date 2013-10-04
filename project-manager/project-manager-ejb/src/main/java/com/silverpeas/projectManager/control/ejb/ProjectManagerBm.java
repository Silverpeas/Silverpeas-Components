/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.projectManager.control.ejb;

import java.util.Date;
import java.util.List;

import javax.ejb.Local;

import com.silverpeas.projectManager.model.Filtre;
import com.silverpeas.projectManager.model.HolidayDetail;
import com.silverpeas.projectManager.model.TaskDetail;

@Local
public interface ProjectManagerBm {

  public List<TaskDetail> getProjects(String instanceId);

  public int addTask(TaskDetail task);

  public void removeTask(int id, String instanceId);

  public void updateTask(TaskDetail task, String userId);

  public void calculateAllTasksDates(String instanceId, int projectId, String userId);

  public List<TaskDetail> getTasksByMotherId(String instanceId, int motherId);

  public List<TaskDetail> getTasksByMotherId(String instanceId, int motherId, Filtre filtre);

  public List<TaskDetail> getTasksNotCancelledByMotherId(String instanceId, int motherId,
      Filtre filtre);

  public List<TaskDetail> getTasksByMotherIdAndPreviousId(String instanceId, int motherId,
      int previousId);

  public List<TaskDetail> getAllTasks(String instanceId, Filtre filtre);

  public TaskDetail getTask(int id);

  public TaskDetail getTaskByTodoId(String todoId);

  public TaskDetail getMostDistantTask(String instanceId, int taskId);

  // Gestion des jours non travailles
  public boolean isHolidayDate(HolidayDetail date);

  public List<Date> getHolidayDates(String instanceId);

  public List<Date> getHolidayDates(String instanceId, Date beginDate, Date endDate);

  public void addHolidayDate(HolidayDetail holiday);

  public void addHolidayDates(List<HolidayDetail> holidayDates);

  public void removeHolidayDate(HolidayDetail holiday);

  public void removeHolidayDates(List<HolidayDetail> holidayDates);

  public Date processEndDate(TaskDetail task);

  public Date processEndDate(float charge, String instanceId, Date dateDebut);

  public void index(String instanceId);

  public int getOccupationByUser(String userId, Date dateDeb, Date dateFin);

  public int getOccupationByUser(String userId, Date dateDeb, Date dateFin, int excludedTaskId);
}
