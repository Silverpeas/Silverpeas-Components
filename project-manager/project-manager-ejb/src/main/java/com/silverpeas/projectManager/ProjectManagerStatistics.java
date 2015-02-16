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
package com.silverpeas.projectManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.silverpeas.projectManager.control.ejb.ProjectManagerBm;
import com.silverpeas.projectManager.model.ProjectManagerRuntimeException;
import com.silverpeas.projectManager.model.TaskDetail;
import com.silverpeas.silverstatistics.ComponentStatisticsInterface;
import com.silverpeas.silverstatistics.UserIdCountVolumeCouple;

import org.silverpeas.util.exception.SilverpeasRuntimeException;

import javax.inject.Inject;

public class ProjectManagerStatistics implements ComponentStatisticsInterface {

  @Inject
  private ProjectManagerBm projectManagerBm = null;

  @Override
  public Collection<UserIdCountVolumeCouple> getVolume(String spaceId, String componentId)
      throws Exception {
    Collection<TaskDetail> tasks = getProjectManagerBm().getAllTasks(componentId, null);
    List<UserIdCountVolumeCouple> myArrayList = new ArrayList<UserIdCountVolumeCouple>(tasks.size());
    for (TaskDetail task : tasks) {
      UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();
      myCouple.setUserId(Integer.toString(task.getOrganisateurId()));
      myCouple.setCountVolume(1);
      myArrayList.add(myCouple);
    }

    return myArrayList;
  }

  private ProjectManagerBm getProjectManagerBm() {
    if (projectManagerBm == null) {
      throw new ProjectManagerRuntimeException("projectManager", SilverpeasRuntimeException.ERROR,
          "ProjectManagerStatistics.getProjectManagerBm()", "CDI bootstrap error");
    }
    return projectManagerBm;
  }
}
