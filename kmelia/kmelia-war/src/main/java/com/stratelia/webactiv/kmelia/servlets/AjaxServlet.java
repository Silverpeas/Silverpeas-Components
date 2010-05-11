/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.kmelia.servlets;

import java.io.IOException;
import java.io.Writer;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.silverpeas.kmelia.KmeliaConstants;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

public class AjaxServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doPost(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession session = req.getSession(true);

    String componentId = req.getParameter("ComponentId");

    KmeliaSessionController kmeliaSC =
        (KmeliaSessionController) session
        .getAttribute("Silverpeas_" + "kmelia" + "_" + componentId);

    String action = getAction(req);
    String result = null;

    if ("Delete".equals(action)) {
      result = deleteTopic(req, kmeliaSC);
    } else if ("GetProfile".equals(action)) {
      result = getProfile(req, kmeliaSC);
    } else if ("SortTopics".equals(action)) {
      result = sortTopics(req, kmeliaSC);
    } else if ("EmptyTrash".equals(action)) {
      result = emptyTrash(kmeliaSC);
    } else if ("UpdateTopicStatus".equals(action)) {
      result = updateTopicStatus(req, kmeliaSC);
    } else if ("GetTopicWysiwyg".equals(action)) {
      result = kmeliaSC.getWysiwygOnTopic(req.getParameter("Id"));
    } else if ("Rename".equals(action)) {
      result = renameTopic(req, kmeliaSC);
    } else if ("bindToPub".equals(action)) {
      updatePubsToLink(req, true);
    } else if ("unbindFromPub".equals(action)) {
      updatePubsToLink(req, false);
    }

    Writer writer = resp.getWriter();
    writer.write(result);
  }

  private void updatePubsToLink(HttpServletRequest request, boolean isToBind) {
    if (StringUtil.isDefined(request.getParameter("TopicToLinkId"))) {

      HashSet<String> list =
          (HashSet) request.getSession().getAttribute(KmeliaConstants.PUB_TO_LINK_SESSION_KEY);
      if (list == null) {
        list = new HashSet<String>();
        request.getSession().setAttribute(KmeliaConstants.PUB_TO_LINK_SESSION_KEY, list);
      }

      if (isToBind) {
        list.add(request.getParameter("TopicToLinkId"));
      } else {
        list.remove(request.getParameter("TopicToLinkId"));
      }

    }
  }

  private String getAction(HttpServletRequest req) {
    return req.getParameter("Action");
  }

  private String getProfile(HttpServletRequest req, KmeliaSessionController kmelia) {
    String id = req.getParameter("Id");

    try {
      return kmelia.getUserTopicProfile(id);
    } catch (RemoteException e) {
      e.printStackTrace();
      return e.getMessage();
    }
  }

  private String deleteTopic(HttpServletRequest req, KmeliaSessionController kmelia) {
    String id = req.getParameter("Id");

    try {
      kmelia.deleteTopic(id);
      return "ok";
    } catch (RemoteException e) {
      e.printStackTrace();
      return e.getMessage();
    }
  }

  private String sortTopics(HttpServletRequest req, KmeliaSessionController kmelia) {
    String orderedList = req.getParameter("OrderedList");
    String componentId = kmelia.getComponentId();

    StringTokenizer tokenizer = new StringTokenizer(orderedList, ",");
    List<NodePK> pks = new ArrayList<NodePK>();
    while (tokenizer.hasMoreTokens()) {
      pks.add(new NodePK(tokenizer.nextToken(), componentId));
    }

    // Save order
    try {
      kmelia.getNodeBm().sortNodes(pks);
      return "ok";
    } catch (Exception e) {
      SilverTrace.error("kmelia", "AjaxServlet.sortTopics", "root.MSG_GEN_PARAM_VALUE", e);
      return e.getMessage();
    }
  }

  private String emptyTrash(KmeliaSessionController kmelia) {
    try {
      kmelia.flushTrashCan();
      return "ok";
    } catch (RemoteException e) {
      SilverTrace.error("kmelia", "AjaxServlet.emptyTrash", "root.MSG_GEN_PARAM_VALUE", e);
      return e.getMessage();
    }
  }

  private String updateTopicStatus(HttpServletRequest req, KmeliaSessionController kmelia) {
    String subTopicId = req.getParameter("Id");
    String newStatus = req.getParameter("Status");
    String recursive = req.getParameter("Recursive");

    try {
      kmelia.changeTopicStatus(newStatus, subTopicId, "1".equals(recursive));
      return "ok";
    } catch (RemoteException e) {
      SilverTrace.error("kmelia", "AjaxServlet.updateTopicStatus", "root.MSG_GEN_PARAM_VALUE", e);
      return e.getMessage();
    }
  }

  private String renameTopic(HttpServletRequest req, KmeliaSessionController kmelia) {
    String topicId = req.getParameter("Id");
    String name = req.getParameter("Name");

    try {
      NodeDetail node = kmelia.getNodeHeader(topicId);
      node.setName(name);
      kmelia.updateTopicHeader(node, "NoAlert");
      return "ok";
    } catch (RemoteException e) {
      SilverTrace.error("kmelia", "AjaxServlet.renameTopic", "root.MSG_GEN_PARAM_VALUE", e);
      return e.getMessage();
    }
  }

}