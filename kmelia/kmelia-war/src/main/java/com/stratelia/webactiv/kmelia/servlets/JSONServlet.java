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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaHelper;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.kmelia.model.TopicDetail;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

public class JSONServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException,
      IOException {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException,
      IOException {
    SilverTrace.info("kmelia", "JSONServlet.doPost", "root.MSG_GEN_ENTER_METHOD");

    res.setContentType("application/json");

    String id = req.getParameter("Id");
    String componentId = req.getParameter("ComponentId");
    String language = req.getParameter("Language");
    String action = req.getParameter("Action");

    KmeliaSessionController kmeliaSC =
        (KmeliaSessionController) req.getSession().getAttribute(
        "Silverpeas_" + "kmelia" + "_" + componentId);

    NodePK nodePK = new NodePK(id, componentId);

    Writer writer = res.getWriter();
    if ("GetSubTopics".equals(action)) {
      TopicDetail topic = kmeliaSC.getTopic(id);
      List<NodeDetail> nodes = (List<NodeDetail>) topic.getNodeDetail().getChildrenDetails();

      if ("0".equals(id) && !KmeliaHelper.isToolbox(componentId) && kmeliaSC.displayNbPublis() &&
          "admin".equals(kmeliaSC.getUserTopicProfile())) {
        int nbPublisToValidate =
            kmeliaSC.getKmeliaBm().getPublicationsToValidate(componentId).size();
        NodeDetail temp = new NodeDetail();
        temp.getNodePK().setId("tovalidate");
        temp.setName("To validate");
        temp.setNbObjects(nbPublisToValidate);
        nodes.add(temp);
      }
      writer.write(getListAsJSONArray(nodes, language, kmeliaSC));
    } else if ("GetPath".equals(action)) {
      List<NodeDetail> nodes = (List<NodeDetail>) getNodeBm().getPath(nodePK);
      writer.write(getListAsJSONArray(nodes, language, kmeliaSC));
    } else if ("Paste".equals(action)) {
      List<Object> pastedItems = kmeliaSC.paste();
      List<NodeDetail> nodes = new ArrayList<NodeDetail>();
      for (int i = 0; i < pastedItems.size(); i++) {
        Object pastedItem = pastedItems.get(i);
        if (pastedItem instanceof NodeDetail) {
          nodes.add((NodeDetail) pastedItem);
        }
      }
      writer.write(getListAsJSONArray(nodes, language, kmeliaSC));
    } else if ("GetTopic".equals(action)) {
      List<NodeDetail> nodes = new ArrayList<NodeDetail>();
      NodeDetail node = getNodeBm().getDetail(nodePK);
      nodes.add(node);
      writer.write(getListAsJSONArray(nodes, language, kmeliaSC));
    }
  }

  private String getListAsJSONArray(List<NodeDetail> nodes, String language,
      KmeliaSessionController kmelia) {
    JSONArray jsonArray = new JSONArray();

    NodeDetail node;
    JSONObject jsonObject;
    for (int i = 0; i < nodes.size(); i++) {
      node = nodes.get(i);
      jsonObject = getNodeAsJSONObject(node, language, kmelia);
      jsonArray.put(jsonObject);
    }

    return jsonArray.toString();
  }

  private JSONObject getNodeAsJSONObject(NodeDetail node, String language,
      KmeliaSessionController kmelia) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("id", node.getNodePK().getId());

    if ("tovalidate".equals(node.getNodePK().getId())) {
      jsonObject.put("name", node.getName());
      jsonObject.put("nbObjects", node.getNbObjects());
    } else {
      jsonObject.put("name", node.getName(language));
      try {
        jsonObject.put("date", DateUtil.getOutputDate(node.getCreationDate(), language));
      } catch (ParseException e) {
        jsonObject.put("date", "error");
      }
      jsonObject.put("creatorId", node.getCreatorId());
      jsonObject.put("creatorName", kmelia.getUserDetail(node.getCreatorId()).getDisplayedName());
      try {
        jsonObject.put("role", kmelia.getUserTopicProfile(node.getNodePK().getId()));
      } catch (RemoteException e) {
        jsonObject.put("role", "error");
      }
      jsonObject.put("nbObjects", node.getNbObjects());
      jsonObject.put("status", node.getStatus());
      jsonObject.put("level", node.getLevel());
      jsonObject.put("updateChain", kmelia.isTopicHaveUpdateChainDescriptor(node.getNodePK()
          .getId()));
    }
    return jsonObject;
  }

  private NodeBm getNodeBm() {
    NodeBm nodeBm = null;
    try {
      NodeBmHome nodeBmHome =
          (NodeBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
      nodeBm = nodeBmHome.create();
    } catch (Exception e) {
      throw new KmeliaRuntimeException("JSONServlet.getNodeBm()", SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_NODEBM_HOME", e);
    }
    return nodeBm;
  }

}
