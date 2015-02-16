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

package com.silverpeas.projectManager.servlets;

import java.io.IOException;
import java.io.Writer;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.silverpeas.projectManager.control.ProjectManagerSessionController;
import com.silverpeas.projectManager.model.TaskDetail;
import org.silverpeas.util.EncodeHelper;
import org.silverpeas.util.FileUtil;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.util.DateUtil;

import org.apache.commons.lang3.CharEncoding;
import org.silverpeas.util.JSONCodec;

public class AjaxProjectManagerServlet extends HttpServlet {

  private static final long serialVersionUID = 798968548064856822L;
  private static final String ACTION_LOAD_TASK = "loadTask";
  private static final String ACTION_COLLAPSE_TASK = "collapseTask";

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException,
      IOException {
    HttpSession session = req.getSession(true);
    String elementId = req.getParameter("ElementId");
    String componentId = req.getParameter("ComponentId");
    // Retrieve action parameter
    String action = req.getParameter("Action");
    boolean isJsonResult = false;
    ProjectManagerSessionController projectManagerSC = (ProjectManagerSessionController) session
        .getAttribute("Silverpeas_projectManager_" + componentId);
    if (projectManagerSC != null) {
      String output = "";
      if ("ProcessUserOccupation".equals(action)) {
        // mise à jour de la charge en tenant compte de la modification des
        // dates de début et fin
        String taskId = req.getParameter("TaskId");
        String userId = req.getParameter("UserId");
        String userCharge = req.getParameter("UserCharge");
        String sBeginDate = req.getParameter("BeginDate");
        String sEndDate = req.getParameter("EndDate");
        SilverTrace.info("projectManager", "AjaxProjectManagerServlet", "root.MSG_GEN_PARAM_VALUE",
            "userId = " + userId + ", charge = " + userCharge);
        Date beginDate = null;
        try {
          beginDate = projectManagerSC.uiDate2Date(sBeginDate);
        } catch (ParseException e) {
        }

        Date endDate = null;
        try {
          endDate = projectManagerSC.uiDate2Date(sEndDate);
        } catch (ParseException e) {
        }

        SilverTrace.info("projectManager", "AjaxProjectManagerServlet",
            "root.MSG_GEN_PARAM_VALUE", "beginDate = " + beginDate + ", endDate = " + endDate);
        int occupation = projectManagerSC.checkOccupation(taskId, userId, beginDate, endDate);
        occupation += Integer.parseInt(userCharge);
        if (occupation > 100) {
          output = "<font color=\"red\">" + occupation + " %</font>";
        } else {
          output = "<font color=\"green\">" + occupation + " %</font>";
        }
      } else if (action.equals("ProcessUserOccupationInit")) {
        // mise à jour de la charge en tenant compte de la modification des
        // dates de début et fin
        String userId = req.getParameter("UserId");
        String userCharge = req.getParameter("UserCharge");
        String sBeginDate = req.getParameter("BeginDate");
        String sEndDate = req.getParameter("EndDate");

        Date beginDate = null;
        try {
          beginDate = projectManagerSC.uiDate2Date(sBeginDate);
        } catch (ParseException e) {
        }
        SilverTrace.info("projectManager", "AjaxProjectManagerServlet", "root.MSG_GEN_PARAM_VALUE",
            "userId = " + userId + ", charge = " + userCharge);

        Date endDate = null;
        try {
          endDate = projectManagerSC.uiDate2Date(sEndDate);
        } catch (ParseException e) {
        }
        SilverTrace.info("projectManager", "AjaxProjectManagerServlet", "root.MSG_GEN_PARAM_VALUE",
            "beginDate = " + beginDate + ", endDate = " + endDate);

        int occupation = projectManagerSC.checkOccupation(userId, beginDate, endDate);
        occupation = occupation + Integer.parseInt(userCharge);
        if (occupation > 100) {
          output = "<font color=\"red\">" + occupation + " %</font>";
        } else {
          output = "<font color=\"green\">" + occupation + " %</font>";
        }
      } else if ("ProcessEndDate".equals(action)) {
        String taskId = req.getParameter("TaskId");
        String charge = req.getParameter("Charge");
        String sBeginDate = req.getParameter("BeginDate");

        Date beginDate = null;
        try {
          beginDate = projectManagerSC.uiDate2Date(sBeginDate);
        } catch (ParseException e) {
        }
        if (beginDate == null) {
          beginDate = new Date();
        }

        Date endDate = projectManagerSC.processEndDate(taskId, charge, beginDate);
        output = EncodeHelper.escapeXml(projectManagerSC.date2UIDate(endDate));
      } else if ("ProcessEndDateInit".equals(action)) {
        String charge = req.getParameter("Charge");
        String sBeginDate = req.getParameter("BeginDate");

        Date beginDate = null;
        try {
          beginDate = projectManagerSC.uiDate2Date(sBeginDate);
        } catch (ParseException e) {
        }
        if (beginDate == null) {
          beginDate = new Date();
        }
        Date endDate = projectManagerSC.processEndDate(charge, beginDate, componentId);
        output = EncodeHelper.escapeXml(projectManagerSC.date2UIDate(endDate));
      } else if (ACTION_LOAD_TASK.equals(action)) {
        String taskId = req.getParameter("TaskId");
        List<TaskDetail> tasks = projectManagerSC.getTasks(taskId);
        List<String> jsonTasks = new ArrayList<String>();
        output = JSONCodec.encodeObject(jsonResult -> {
          jsonResult.put("success", true);
          jsonResult.put("componentId", projectManagerSC.getComponentId());

          String listTask = JSONCodec.encodeArray(taskArray -> {

            for (TaskDetail curTask : tasks) {
              String jsonTask = JSONCodec.encodeObject(jsonElt -> {
                jsonElt.put("id", curTask.getId());
                jsonElt.put("status", curTask.getStatut());
                // level is not used : need to use another element
                jsonElt.put("level", curTask.getLevel());
                jsonElt.put("containsSubTask", curTask.getEstDecomposee());
                jsonElt.put("name", curTask.getNom());
                jsonElt.put("manager", curTask.getResponsableFullName());
                jsonElt.put("startDate", DateUtil.formatDate(curTask.getDateDebut(), "yyyyMMdd"));
                jsonElt.put("endDate", DateUtil.formatDate(curTask.getDateFin(), "yyyyMMdd"));
                float conso = curTask.getConsomme();
                if (conso != 0) {
                  jsonElt.put("percentage", (conso / (conso + curTask.getRaf())) * 100);
                } else {
                  jsonElt.put("percentage", conso);
                }
                return jsonElt;
              });
              jsonTasks.add(jsonTask);
            }
            taskArray.addJSONArray(jsonTasks);
            return taskArray;
          });

          jsonResult.put("tasks", listTask);
          return jsonResult;
        });
      } else if (ACTION_COLLAPSE_TASK.equals(action)) {
        String taskId = req.getParameter("TaskId");
        isJsonResult = true;
        output = JSONCodec.encodeObject(jsonResult -> {
          jsonResult.put("success", true);
          jsonResult.put("componentId", projectManagerSC.getComponentId());

          String listTask = JSONCodec.encodeArray(taskArray -> {
            buildJSonTasksId(projectManagerSC, taskId, taskArray);
            return taskArray;
          });

          jsonResult.put("tasks", listTask);
          return jsonResult;
        });
      }
      SilverTrace.info("projectManager", "AjaxProjectManagerServlet", "root.MSG_GEN_PARAM_VALUE",
          "output = " + output);
      res.setContentType(FileUtil.XML_MIME_TYPE);
      res.setHeader("charset", CharEncoding.UTF_8);
      Writer writer = res.getWriter();
      if (isJsonResult) {
        writer.write(output);
      } else {
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.write("<ajax-response>");
        writer.write("<response type=\"element\" id=\"" + elementId + "\">");
        writer.write(output);
        writer.write("</response>");
        writer.write("</ajax-response>");
      }
    }
  }

  /**
   * Recursive method which build the list of tasks id that are child of the task identifier given
   * in parameter
   *
   * @param projectManagerSC the project manager session controller
   * @param taskId the current task identifier we need to have task childs
   * @param taskArray the JSONArray
   */
  private void buildJSonTasksId(ProjectManagerSessionController projectManagerSC, String taskId,
      JSONCodec.JSONArray taskArray) {
    List<TaskDetail> tasks = projectManagerSC.getTasks(taskId);
    List<String> jsonTasks = new ArrayList<String>();
    for (TaskDetail curTask : tasks) {
      String jsonTask = JSONCodec.encodeObject(jsonElt -> {
        jsonElt.put("id", curTask.getId());
        return jsonElt;
      });
      jsonTasks.add(jsonTask);

      if (curTask.getEstDecomposee() == 1) {
        this.buildJSonTasksId(projectManagerSC, Integer.toString(curTask.getId()), taskArray);
      }
    }
    taskArray.addJSONArray(jsonTasks);
  }
}
