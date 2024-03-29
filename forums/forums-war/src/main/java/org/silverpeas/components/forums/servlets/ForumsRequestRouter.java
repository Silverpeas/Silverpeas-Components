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
package org.silverpeas.components.forums.servlets;

import org.silverpeas.components.forums.control.ForumsSessionController;
import org.silverpeas.components.forums.control.helpers.ForumActionHelper;
import org.silverpeas.components.forums.control.helpers.ForumHelper;
import org.silverpeas.components.forums.model.Forum;
import org.silverpeas.components.forums.model.Message;
import org.silverpeas.components.forums.url.ActionUrl;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.webapi.rating.RaterRatingEntity;

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
        if (StringUtil.isInteger(forumId) && Integer.parseInt(forumId) > 0) {
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
        int forumId = 0;
        String param = request.getParameter("forumId");
        if (StringUtil.isDefined(param)) {
          forumId = Integer.parseInt(param.trim());
        }
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

          forumsSC.setLastVisit(forumsSC.getUserId(), Integer.parseInt(messageId));
        }
        destination = ROOT_DEST + "viewMessage.jsp";
      } else if (function.startsWith("editMessageKeywords")) {
        destination = ROOT_DEST + "editMessageKeywords.jsp";
      } else if (function.startsWith("editMessage")) {
        destination = ROOT_DEST + "editMessage.jsp";
      } else if (function.startsWith("modifyMessage")) {
        destination = ROOT_DEST + "modifyMessage.jsp";
      } else if ("ValidateMessage".equals(function)) {
        String messageId = request.getParameter("params");
        forumsSC.validateMessage(Integer.parseInt(messageId));
        destination = getDestination("viewMessage", forumsSC, request);
      } else if ("RefuseMessage".equals(function)) {
        String messageId = request.getParameter("params");
        String motive = request.getParameter("Motive");
        forumsSC.refuseMessage(Integer.parseInt(messageId), motive);
        destination = getDestination("viewMessage", forumsSC, request);
      } else if ("ViewCategory".equals(function)) {
        // gestion des catégories
        destination = ROOT_DEST + "main.jsp";
      } else if ("NewCategory".equals(function)) {
        destination = ROOT_DEST + "categoryManager.jsp";
      } else if ("CreateCategory".equals(function)) {
        // récupération des paramètres
        String name = request.getParameter("Name");
        String description = request.getParameter("Description");
        NodeDetail category =
            new NodeDetail("unknown", name, description, 0, "unknown");
        forumsSC.createCategory(category);

        destination = getDestination("ViewCategory", forumsSC, request);
      } else if ("EditCategory".equals(function)) {
        // récupération des paramètres
        String categoryId = request.getParameter("CategoryId");
        NodeDetail category = forumsSC.getCategory(categoryId);
        request.setAttribute("Category", category);

        destination = ROOT_DEST + "categoryManager.jsp";
      } else if ("UpdateCategory".equals(function)) {
        String categoryId = request.getParameter("CategoryId");
        NodeDetail category = forumsSC.getCategory(categoryId);
        String name = request.getParameter("Name");
        category.setName(name);
        String desc = request.getParameter("Description");
        category.setDescription(desc);
        forumsSC.updateCategory(category);

        destination = getDestination("ViewCategory", forumsSC, request);
      } else if ("DeleteCategory".equals(function)) {
        String categoryId = request.getParameter("CategoryId");
        forumsSC.deleteCategory(categoryId);
        destination = getDestination("ViewCategory", forumsSC, request);
      } else if (function.startsWith("searchResult")) {
        String id = request.getParameter("Id");
        String type = request.getParameter("Type");
        if ("Message".equalsIgnoreCase(type)) {
          destination = URLUtil.getURL(forumsSC.getSpaceId(), forumsSC.getComponentId()) +
              "viewMessage.jsp?action=1&params=" + id;
        } else {
          destination = URLUtil.getURL(forumsSC.getSpaceId(), forumsSC.getComponentId()) +
              "viewForum.jsp?call=main&forumId=" + id;
        }
      } else if (function.startsWith("GoToFilesTab")) {
        String messageId = request.getParameter("Id");
        Message message = null;
        if (StringUtil.isDefined(messageId)) {
          message = forumsSC.getMessage(Integer.parseInt(messageId));
        }
        if (message == null) {
          return ROOT_DEST + "messageNotFound";
        }
        destination = URLUtil.getURL(forumsSC.getSpaceId(), forumsSC.getComponentId()) +
            "viewMessage.jsp?action=1&params=" + messageId;
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
   * @param forumId the forum identifier
   * @return destination page
   */
  private String displayForum(HttpServletRequest request, ForumsSessionController forumsSC,
      int forumId) {
    String destinationPage = "main.jsp";
    int currentForumId = (forumId < 0 ? 0 : forumId);
    if (currentForumId > 0) {
      Forum forum = forumsSC.getForum(currentForumId);
      if (forum == null) {
        return ROOT_DEST + "forumNotFound";
      }
      request.setAttribute("currentForum", forum);
      request.setAttribute("currentForumRaterRatingEntity", RaterRatingEntity.fromRateable(forum));
      request.setAttribute("parents", forumsSC.getForumAncestors(currentForumId));
      request.setAttribute("nbChildrens", forumsSC.getForumSonsNb(currentForumId));
      destinationPage = "viewForum.jsp";
    }

    return ROOT_DEST + destinationPage + "?forumId=" + currentForumId;
  }
}
