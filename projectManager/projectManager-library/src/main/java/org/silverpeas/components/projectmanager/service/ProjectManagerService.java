/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.components.projectmanager.service;

import org.silverpeas.components.projectmanager.model.Filtre;
import org.silverpeas.components.projectmanager.model.HolidayDetail;
import org.silverpeas.components.projectmanager.model.TaskDetail;
import org.silverpeas.core.io.upload.UploadedFile;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface ProjectManagerService {

  static ProjectManagerService get() {
    return ServiceProvider.getService(ProjectManagerService.class);
  }

  List<TaskDetail> getProjects(String instanceId);

  int addTask(TaskDetail task, final Collection<UploadedFile> uploadedFiles);

  void removeTask(int id, String instanceId);

  void updateTask(TaskDetail task, String userId);

  void calculateAllTasksDates(String instanceId, int projectId, String userId);

  List<TaskDetail> getTasksByMotherId(String instanceId, int motherId);

  List<TaskDetail> getTasksByMotherIdAndPreviousId(String instanceId, int motherId,
      int previousId);

  List<TaskDetail> getAllTasks(String instanceId, Filtre filtre);

  TaskDetail getTask(int id);

  TaskDetail getTaskByTodoId(String todoId);

  TaskDetail getMostDistantTask(String instanceId, int taskId);

  // Gestion des jours non travailles
  boolean isHolidayDate(HolidayDetail date);

  List<Date> getHolidayDates(String instanceId);

  List<Date> getHolidayDates(String instanceId, Date beginDate, Date endDate);

  void addHolidayDate(HolidayDetail holiday);

  void addHolidayDates(List<HolidayDetail> holidayDates);

  void removeHolidayDate(HolidayDetail holiday);

  void removeHolidayDates(List<HolidayDetail> holidayDates);

  Date processEndDate(TaskDetail task);

  Date processEndDate(float charge, String instanceId, Date dateDebut);

  void index(String instanceId);

  int getOccupationByUser(String userId, Date dateDeb, Date dateFin);

  int getOccupationByUser(String userId, Date dateDeb, Date dateFin, int excludedTaskId);
}
