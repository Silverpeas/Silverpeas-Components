/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.projectmanager.servlets;

import org.silverpeas.components.projectmanager.control.ProjectManagerSessionController;
import org.silverpeas.components.projectmanager.model.TaskDetail;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.JSONCodec.JSONArray;
import org.silverpeas.core.util.MimeTypes;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.UnaryOperator;

public class AjaxProjectManagerServlet extends HttpServlet {

  private static final long serialVersionUID = 798968548064856822L;
  private static final String ACTION_LOAD_TASK = "loadTask";
  private static final String ACTION_COLLAPSE_TASK = "collapseTask";
  private static final String REQUEST_PARAM_TASKID = "TaskId";
  private static final String REQUEST_PARAM_BEGINDATE = "BeginDate";

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
    HttpSession session = req.getSession(true);
    String componentId = req.getParameter("ComponentId");
    // Retrieve action parameter
    String action = req.getParameter("Action");
    ProjectManagerSessionController projectManagerSC = (ProjectManagerSessionController) session
        .getAttribute("Silverpeas_projectManager_" + componentId);
    if (projectManagerSC == null) {
      return;
    }
    String output = "";
    if ("ProcessUserOccupation".equals(action)) {
      // mise à jour de la charge en tenant compte de la modification des dates de début et fin
      String taskId = req.getParameter(REQUEST_PARAM_TASKID);
      String userId = req.getParameter("UserId");
      String userCharge = req.getParameter("UserCharge");
      String sBeginDate = req.getParameter(REQUEST_PARAM_BEGINDATE);
      String sEndDate = req.getParameter("EndDate");

      Date beginDate = parseDate(sBeginDate, projectManagerSC);
      Date endDate = parseDate(sEndDate, projectManagerSC);

      int occupation = 0;
      if (StringUtil.isDefined(taskId)) {
        occupation = projectManagerSC.checkOccupation(taskId, userId, beginDate, endDate);
      } else {
        occupation = projectManagerSC.checkOccupation(userId, beginDate, endDate);
      }

      occupation += Integer.parseInt(userCharge);
      output = formatOccupation(occupation);
    } else if ("ProcessEndDate".equals(action)) {
      String taskId = req.getParameter(REQUEST_PARAM_TASKID);
      String charge = req.getParameter("Charge");
      String sBeginDate = req.getParameter(REQUEST_PARAM_BEGINDATE);

      Date beginDate = parseDate(sBeginDate, projectManagerSC);
      if (beginDate == null) {
        beginDate = new Date();
      }

      Date endDate;
      if (StringUtil.isDefined(taskId)) {
        endDate = projectManagerSC.processEndDate(taskId, charge, beginDate);
      } else {
        endDate = projectManagerSC.processEndDate(charge, beginDate, componentId);
      }

      output = WebEncodeHelper.escapeXml(projectManagerSC.date2UIDate(endDate));
    } else if (ACTION_LOAD_TASK.equals(action)) {
      String taskId = req.getParameter(REQUEST_PARAM_TASKID);
      List<TaskDetail> tasks = projectManagerSC.getTasks(taskId);
      output = JSONCodec.encodeObject(jsonResult -> {
        jsonResult.put("success", true);
        jsonResult.put("componentId", projectManagerSC.getComponentId());
        jsonResult.putJSONArray("tasks", getJSONTasks(tasks));
        return jsonResult;
      });
    } else if (ACTION_COLLAPSE_TASK.equals(action)) {
      String taskId = req.getParameter(REQUEST_PARAM_TASKID);
      output = JSONCodec.encodeObject(jsonResult -> {
        jsonResult.put("success", true);
        jsonResult.put("componentId", projectManagerSC.getComponentId());
        List<Integer> listTaskIds = new ArrayList<>();
        jsonResult.putJSONArray("tasks", convertCollapsedTaskIdsIntoJSON(
            getCollapsedTaskIds(projectManagerSC, taskId, listTaskIds)));
        return jsonResult;
      });
    }

    res.setContentType(MimeTypes.XML_MIME_TYPE);
    res.setHeader("charset", Charsets.UTF_8.name());
    Writer writer = res.getWriter();
    writer.write(output);
  }

  private UnaryOperator<JSONArray> convertCollapsedTaskIdsIntoJSON(
      List<Integer> taskIds) {
    return (jsonTaskIds -> {
      for (Integer taskId : taskIds) {
        jsonTaskIds.addJSONObject(jsonTask -> jsonTask.put("id", taskId));
      }
      return jsonTaskIds;
    });
  }


  /**
   * Recursive method which build the list of tasks id that are child of the task identifier given
   * in parameter
   * @param projectManagerSC the project manager session controller
   * @param taskId the current task identifier we need to have task childs
   * @param taskIds the list of ids to build
   */
  private List<Integer> getCollapsedTaskIds(ProjectManagerSessionController projectManagerSC,
      String taskId, List<Integer> taskIds) {
    List<TaskDetail> tasks = projectManagerSC.getTasks(taskId);
    for (TaskDetail curTask : tasks) {
      taskIds.add(curTask.getId());
      if (curTask.getEstDecomposee() == 1) {
        this.getCollapsedTaskIds(projectManagerSC, Integer.toString(curTask.getId()), taskIds);
      }
    }
    return taskIds;
  }

  /**
   * @param tasks the list of tasks to convert into JSON
   * @return JSONArray of list of tasks
   */
  private UnaryOperator<JSONArray> getJSONTasks(List<TaskDetail> tasks) {
    return (jsonTasks -> {
      for (TaskDetail curTask : tasks) {
        jsonTasks.addJSONObject(jsonTask -> {
          jsonTask.put("id", curTask.getId());
          jsonTask.put("status", curTask.getStatut());
          // level is not used : need to use another element
          jsonTask.put("level", curTask.getLevel());
          jsonTask.put("containsSubTask", curTask.getEstDecomposee());
          jsonTask.put("name", curTask.getNom());
          jsonTask.put("manager", curTask.getResponsableFullName());
          jsonTask.put("startDate", DateUtil.formatDate(curTask.getDateDebut(), "yyyyMMdd"));
          jsonTask.put("endDate", DateUtil.formatDate(curTask.getDateFin(), "yyyyMMdd"));
          float conso = curTask.getConsomme();
          if (conso != 0) {
            jsonTask.put("percentage", (conso / (conso + curTask.getRaf())) * 100);
          } else {
            jsonTask.put("percentage", conso);
          }
          return jsonTask;
        });
      }
      return jsonTasks;
    });
  }

  private Date parseDate(String date, ProjectManagerSessionController projectManagerSC) {
    try {
      return projectManagerSC.uiDate2Date(date);
    } catch (ParseException ignored) {
      SilverLogger.getLogger(this).debug("Error when parsing date "+date);
    }
    return null;
  }

  private String formatOccupation(int occupation) {
    String font = "<font color=\"green\">";
    if (occupation > 100) {
      font = "<font color=\"red\">";
    }
    return font + occupation + " %</font>";
  }
}