/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.servlets;

import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.RequestHelper;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.forums.control.ForumsSessionController;
import com.stratelia.webactiv.forums.models.Message;
import com.stratelia.webactiv.forums.url.ActionUrl;
import com.stratelia.webactiv.util.node.model.NodeDetail;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class ForumsRequestRouter extends ComponentRequestRouter<ForumsSessionController> {

  private static final String ROOT_DEST = "/forums/jsp/";
  private static final long serialVersionUID = 4053081577285187038L;

  @Override
  public ForumsSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext context) {
    return new ForumsSessionController(mainSessionCtrl, context);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object
   */
  @Override
  public String getSessionControlBeanName() {
    return "forumsSessionClientController";
  }

  @Override
  public String getDestination(String function, ForumsSessionController forumsSC, HttpServletRequest request) {
    String destination = "";
    if ((function.startsWith("Main")) || (function.startsWith("main"))) {
        String forumId = request.getParameter("forumId");
        if (forumId != null && Integer.parseInt(forumId) > 0) {
          return ROOT_DEST + ActionUrl.getUrl("viewForum", "main", Integer.parseInt(forumId));
        }
      destination = ROOT_DEST + "main.jsp";
    } else if (function.startsWith("external")) {
      forumsSC.setExternal(true);
      String mailType = request.getParameter("mailType");
      if (mailType != null) {
        forumsSC.setMailType(mailType);
      }
      forumsSC.setResizeFrame("true".equals(request.getParameter("resizeFrame")));
      String actionUrl = null;
      String messageId = request.getParameter("id");
      if (messageId != null) {
        Message message = forumsSC.getMessage(Integer.parseInt(messageId));
        if (message != null) {
          actionUrl = ActionUrl.getUrl("viewMessage", "main", 1, message.getId(),
              message.getForumId(), true, false);
        }
      } else {
        String forumId = request.getParameter("forumId");
        if (forumId != null) {
          actionUrl = ActionUrl.getUrl("viewForum", "main", Integer.parseInt(forumId));
        }
      }
      destination = ROOT_DEST + (actionUrl != null ? actionUrl : "main.jsp");
    } else if (function.startsWith("portlet")) {
      destination = ROOT_DEST + "portlet.jsp";
    } else if (function.startsWith("viewForum")) {
      int forumId = RequestHelper.getIntParameter(request, "forumId", 0);
      request.setAttribute("currentForum", forumsSC.getForum(forumId));
      request.setAttribute("notation", forumsSC.getForumNotation(forumId));
      request.setAttribute("parents", forumsSC.getForumAncestors(forumId));
      request.setAttribute("nbChildrens", forumsSC.getForumSonsNb(forumId));
      destination = ROOT_DEST + "viewForum.jsp";
    } else if (function.startsWith("editForumInfo")) {
      destination = ROOT_DEST + "editForumInfo.jsp";
    } else if (function.startsWith("viewMessage")) {
      // mise à jour de la table des consultations
      String messageId = request.getParameter("params");
      if (!StringUtil.isDefined(messageId)) {
        messageId = (String) request.getAttribute("params");
      }
      if (StringUtil.isDefined(messageId)) {
        SilverTrace.info("forums", "ForumsRequestRouter",
            "root.MSG_GEN_PARAM_VALUE", "messageId (pour last visite) = "
            + messageId);
        forumsSC.setLastVisit(forumsSC.getUserId(), Integer.parseInt(messageId));
      }
      int forumId = 0;
      String forumIdS = request.getParameter("forumId");
      if (!StringUtil.isDefined(messageId)) {
        forumIdS = (String) request.getAttribute("forumId");
      }
      if (StringUtil.isDefined(forumIdS)) {
        forumId = Integer.parseInt(forumIdS);
      }
      if (StringUtil.isDefined(messageId)) {
        Message message = forumsSC.getMessage(Integer.parseInt(messageId));
        forumId = message.getForumId();
      }
      List<String> moderators = forumsSC.getModerators(forumId);
      String nbModerators = Integer.toString(moderators.size());
      request.setAttribute("NbModerators", nbModerators);
      destination = ROOT_DEST + "viewMessage.jsp";
    } else if (function.startsWith("editMessageKeywords")) {
      destination = ROOT_DEST + "editMessageKeywords.jsp";
    } else if (function.startsWith("editMessage")) {            
      destination = ROOT_DEST + "editMessage.jsp";
    } else if (function.startsWith("modifyMessage")) {
      destination = ROOT_DEST + "modifyMessage.jsp";
    } else if (function.equals("ValidateMessage")) {
      String messageId = request.getParameter("params");
      forumsSC.validateMessage(Integer.parseInt(messageId));
      destination = getDestination("viewMessage", forumsSC, request);
    } else if (function.equals("RefuseMessage")) {
      String messageId = request.getParameter("params");
      String motive = request.getParameter("Motive");
      forumsSC.refuseMessage(Integer.parseInt(messageId), motive);
      destination = getDestination("viewMessage", forumsSC, request);
    } // gestion des catégories
    // ----------------------
    else if (function.equals("ViewCategory")) {
      destination = ROOT_DEST + "main.jsp";
    } else if (function.equals("NewCategory")) {
      destination = ROOT_DEST + "categoryManager.jsp";
    } else if (function.equals("CreateCategory")) {
      // récupération des paramètres
      String name = request.getParameter("Name");
      String description = request.getParameter("Description");
      NodeDetail category = new NodeDetail("unknown", name, description, null, null, null, "0",
          "unknown");
      forumsSC.createCategory(category);

      destination = getDestination("ViewCategory", forumsSC, request);
    } else if (function.equals("EditCategory")) {
      // récupération des paramètres
      String categoryId = request.getParameter("CategoryId");
      NodeDetail category = forumsSC.getCategory(categoryId);
      request.setAttribute("Category", category);

      destination = ROOT_DEST + "categoryManager.jsp";
    } else if (function.equals("UpdateCategory")) {
      String categoryId = request.getParameter("CategoryId");
      NodeDetail category = forumsSC.getCategory(categoryId);
      String name = request.getParameter("Name");
      category.setName(name);
      String desc = request.getParameter("Description");
      category.setDescription(desc);
      forumsSC.updateCategory(category);

      destination = getDestination("ViewCategory", forumsSC, request);
    } else if (function.equals("DeleteCategory")) {
      String categoryId = request.getParameter("CategoryId");
      SilverTrace.debug("forums", "ForumsRequestRouter",
          "root.MSG_GEN_PARAM_VALUE", "categoryId = " + categoryId);
      forumsSC.deleteCategory(categoryId);
      destination = getDestination("ViewCategory", forumsSC, request);
    } else if (function.startsWith("searchResult")) {
      String id = request.getParameter("Id");
      String type = request.getParameter("Type");
      if (type.equals("Forum")) {
        destination = ROOT_DEST + "viewForum.jsp?forumId=" + id;
      } else if (type.equals("ForumsMessage")) {
        request.setAttribute("params", id);
        destination = getDestination("viewMessage", forumsSC, request);
      } else {
        destination = ROOT_DEST + "viewMessage.jsp?action=1&params=" + id;
      }
    } else {
      destination = ROOT_DEST + function;
    }

    return destination;
  }
}
