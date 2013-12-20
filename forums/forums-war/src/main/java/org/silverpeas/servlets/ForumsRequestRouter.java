/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.servlets;

import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.RequestHelper;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.forums.control.ForumsSessionController;
import com.stratelia.webactiv.forums.control.helpers.ForumActionHelper;
import com.stratelia.webactiv.forums.control.helpers.ForumHelper;
import com.stratelia.webactiv.forums.models.Forum;
import com.stratelia.webactiv.forums.models.Message;
import com.stratelia.webactiv.forums.url.ActionUrl;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import org.silverpeas.servlet.HttpRequest;

import javax.servlet.http.HttpServletRequest;

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
  public String getDestination(String function, ForumsSessionController forumsSC,
      HttpRequest request) {
    String destination;
    try {
      if ((function.startsWith("Main")) || (function.startsWith("main"))) {
        String forumId = request.getParameter("forumId");
        if (forumId != null && Integer.parseInt(forumId) > 0) {
          return ROOT_DEST + ActionUrl.getUrl("viewForum", "main", Integer.parseInt(forumId));
        }
        destination = ROOT_DEST + "main.jsp";
      } else if (function.startsWith("external")) {
        forumsSC.setExternal(true);
        forumsSC.setResizeFrame("true".equals(request.getParameter("resizeFrame")));
        String actionUrl = null;
        String messageId = request.getParameter("id");
        if (messageId != null) {
          Message message = forumsSC.getMessage(Integer.parseInt(messageId));
          if (message != null) {
            actionUrl = ActionUrl
                .getUrl("viewMessage", "main", 1, message.getId(), message.getForumId(), true,
                    false);
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
      } else if (function.startsWith("createForum") || function.startsWith("updateForum")) {
        boolean isCreation = function.startsWith("createForum");
        int forumParentId = ForumHelper.getIntParameter(request, "forumFolder");
        if (isCreation) {
          ForumActionHelper.createForumAction(request, forumsSC);
        } else {
          ForumActionHelper.updateForumAction(request, forumsSC);
        }
        destination = displayForum(request, forumsSC, forumParentId);
      } else if (function.startsWith("viewForum")) {
        int forumId = RequestHelper.getIntParameter(request, "forumId", 0);
        destination = displayForum(request, forumsSC, forumId);
      } else if (function.startsWith("editForumInfo")) {
        destination = ROOT_DEST + "editForumInfo.jsp";
      } else if (function.startsWith("viewMessage")) {
        // mise à jour de la table des consultations
        String messageId = request.getParameter("params");
        if (!StringUtil.isDefined(messageId)) {
          messageId = (String) request.getAttribute("params");
        }
        if (StringUtil.isDefined(messageId)) {
          Message message = forumsSC.getMessage(Integer.parseInt(messageId));
          if (message == null) {
            return ROOT_DEST + "messageNotFound";
          }
          SilverTrace.info("forums", "ForumsRequestRouter", "root.MSG_GEN_PARAM_VALUE",
              "messageId (pour last visite) = " + messageId);
          forumsSC.setLastVisit(forumsSC.getUserId(), Integer.parseInt(messageId));
        }
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
        NodeDetail category =
            new NodeDetail("unknown", name, description, null, null, null, "0", "unknown");
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
        SilverTrace.debug("forums", "ForumsRequestRouter", "root.MSG_GEN_PARAM_VALUE",
            "categoryId = " + categoryId);
        forumsSC.deleteCategory(categoryId);
        destination = getDestination("ViewCategory", forumsSC, request);
      } else if (function.startsWith("searchResult")) {
        String id = request.getParameter("Id");
        String type = request.getParameter("Type");
        if ("Forum".equalsIgnoreCase(type)) {
          destination = URLManager.getURL(forumsSC.getSpaceId(), forumsSC.getComponentId()) +
              "viewForum.jsp?call=main&forumId=" + id;
        } else {
          destination = URLManager.getURL(forumsSC.getSpaceId(), forumsSC.getComponentId()) +
              "viewMessage.jsp?action=1&params=" + id;
        }
      } else {
        destination = ROOT_DEST + function;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    return destination;
  }

  /**
   * Prepares data to view a forum and computes the right destination.
   * @param request
   * @param forumsSC
   * @param forumId
   * @return
   */
  private String displayForum(HttpServletRequest request, ForumsSessionController forumsSC,
      int forumId) {
    int currentForumId = (forumId < 0 ? 0 : forumId);
    if (currentForumId > 0) {
      Forum forum = forumsSC.getForum(currentForumId);
      if (forum == null) {
        return ROOT_DEST + "forumNotFound";
      }
    }
    request.setAttribute("currentForum", forumsSC.getForum(currentForumId));
    request.setAttribute("notation", forumsSC.getForumNotation(currentForumId));
    request.setAttribute("parents", forumsSC.getForumAncestors(currentForumId));
    request.setAttribute("nbChildrens", forumsSC.getForumSonsNb(currentForumId));
    return ROOT_DEST +
        (currentForumId == 0 ? "main.jsp" : "viewForum.jsp") + "?forumId=" + currentForumId;
  }
}
