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
package com.stratelia.webactiv.forums.control.helpers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.forums.control.ForumsSessionController;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * @author ehugonnet
 */
public class ForumActionHelper {

  public static final int DEPLOY_FORUM = 1;
  public static final int UNDEPLOY_FORUM = 2;
  public static final int CREATE_FORUM = 3;
  public static final int DELETE_FORUM = 4;
  public static final int LOCK_FORUM = 5;
  public static final int UNLOCK_FORUM = 6;
  public static final int UPDATE_FORUM = 7;
  public static final int CREATE_MESSAGE = 8;
  public static final int DELETE_MESSAGE = 9;
  public static final int DEPLOY_MESSAGE = 10;
  public static final int UNDEPLOY_MESSAGE = 11;
  public static final int MOVE_MESSAGE = 12;
  public static final int UNSUBSCRIBE_THREAD = 13;
  public static final int SUBSCRIBE_THREAD = 14;
  public static final int UPDATE_MESSAGE = 15;
  public static final int EVALUATE_FORUM = 16;

  public static void actionManagement(HttpServletRequest request, boolean isAdmin,
      boolean isModerator, String userId, ResourceLocator resource, JspWriter out,
      ForumsSessionController fsc) {
    int action = ForumHelper.getIntParameter(request, "action");
    if (action != -1) {
      int params = ForumHelper.getIntParameter(request, "params");

      try {
        switch (action) {
          case DEPLOY_FORUM: {
            fsc.deployForum(params);
            break;
          }
          case UNDEPLOY_FORUM: {
            fsc.undeployForum(params);
            break;
          }
          case CREATE_FORUM: {
            String forumName = request.getParameter("forumName").trim();
            String forumDescription = request.getParameter("forumDescription").trim();
            String[] forumModerators = request.getParameterValues("moderators");
            int forumParent = ForumHelper.getIntParameter(request, "forumFolder");
            String categoryId = request.getParameter("CategoryId").trim();
            String keywords = request.getParameter("forumKeywords").trim();
            String positions = request.getParameter("Positions");
            fsc.setForumPositions(positions);
            int forumId = fsc.createForum(forumName, forumDescription, userId, forumParent,
                categoryId, keywords);
            if (forumModerators != null) {
              for (String moderator : forumModerators) {
                fsc.addModerator(forumId, moderator.trim());
              }
            }
            break;
          }
          case DELETE_FORUM: {
            fsc.deleteForum(params);
            break;
          }
          case LOCK_FORUM: {
            if (isAdmin) {
              fsc.lockForum(params, 1);
            } else if (isModerator) {
              fsc.lockForum(params, 2);
            }
            break;
          }
          case UNLOCK_FORUM: {
            boolean success = true;
            if (isAdmin) {
              success = fsc.unlockForum(params, 1) == 0;
            } else if (isModerator) {
              success = fsc.unlockForum(params, 2) == 0;
            }
            if (success) {
              out.println("<script language=\"Javascript\">");
              out.println("alert(\"" + resource.getString("adminTopicLock") + "\");");
              out.println("</script>");
            }
            break;
          }
          case UPDATE_FORUM: {
            String forumName = request.getParameter("forumName").trim();
            String forumDescription = request.getParameter("forumDescription").trim();
            int forumId = ForumHelper.getIntParameter(request, "forumId");
            String keywords = request.getParameter("forumKeywords").trim();
            int forumParent = ForumHelper.getIntParameter(request, "forumFolder");
            String[] forumModerators = request.getParameterValues("moderators");
            fsc.removeAllModerators(forumId);
            if (forumModerators != null) {
              for (String moderator : forumModerators) {
                fsc.addModerator(forumId, moderator.trim());
              }
            }
            String categoryId = request.getParameter("CategoryId").trim();
            fsc
                .updateForum(forumId, forumName, forumDescription, forumParent, categoryId,
                    keywords);
            break;
          }
          case CREATE_MESSAGE: {
            int forumId = ForumHelper.getIntParameter(request, "forumId");
            int parentId = ForumHelper.getIntParameter(request, "parentId", 0);
            String messageTitle = request.getParameter("messageTitle").trim();
            String messageText = request.getParameter("messageText").trim();
            String forumKeywords = request.getParameter("forumKeywords");
            String subscribe = request.getParameter("subscribeMessage");
            if (StringUtil.isDefined(messageTitle) && StringUtil.isDefined(messageText)) {
              int result = fsc.createMessage(messageTitle, userId, forumId, parentId, messageText,
                  forumKeywords);
              if (subscribe == null) {
                subscribe = "0";
              } else {
                subscribe = "1";
                if (result != 0) {
                  fsc.subscribeMessage(result, userId);
                }
              }
              if (parentId > 0) {
                fsc.deployMessage(parentId);
              }
            }
            break;
          }
          case DELETE_MESSAGE: {
            fsc.deleteMessage(params);
            break;
          }
          case DEPLOY_MESSAGE: {
            fsc.deployMessage(params);
            break;
          }
          case UNDEPLOY_MESSAGE: {
            fsc.undeployMessage(params);
            break;
          }

          case MOVE_MESSAGE: {
            int messageId = ForumHelper.getIntParameter(request, "messageId");
            int folderId = ForumHelper.getIntParameter(request, "messageNewFolder");
            fsc.moveMessage(messageId, folderId);
            break;
          }
          case UNSUBSCRIBE_THREAD: {
            fsc.unsubscribeMessage(params, userId);
            break;
          }
          case SUBSCRIBE_THREAD: {
            fsc.subscribeMessage(params, userId);
            break;
          }
          case UPDATE_MESSAGE: {
            int messageId = ForumHelper.getIntParameter(request, "messageId");
            String keywords = request.getParameter("forumKeywords").trim();
            fsc.updateMessageKeywords(messageId, keywords);
            break;
          }
          case EVALUATE_FORUM: {
            int forumId = ForumHelper.getIntParameter(request, "forumId");
            int note = ForumHelper.getIntParameter(request, "note", -1);
            if (note > 0) {
              fsc.updateForumNotation(forumId, note);
            }
            break;
          }
        }
      } catch (NumberFormatException nfe) {
        SilverTrace.info(
            "forums", "JSPforumsListActionManager", "root.EX_NO_MESSAGE", null, nfe);
      } catch (IOException ioe) {
        SilverTrace.info(
            "forums", "JSPforumsListActionManager", "root.EX_NO_MESSAGE", null, ioe);
      }
    }
  }

  private ForumActionHelper() {
  }
}
