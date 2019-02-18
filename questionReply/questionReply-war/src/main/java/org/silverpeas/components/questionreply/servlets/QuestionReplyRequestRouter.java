/*
 * Copyright (C) 2000 - 2018 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.questionreply.servlets;

import org.silverpeas.components.questionreply.control.QuestionReplySessionController;
import org.silverpeas.components.questionreply.model.Category;
import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.components.questionreply.model.Reply;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.importexport.report.ExportReport;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.silverpeas.core.admin.user.model.SilverpeasRole.*;

/**
 * Router class for SuestionReply component
 */
public class QuestionReplyRequestRouter
    extends ComponentRequestRouter<QuestionReplySessionController> {

  private static final long serialVersionUID = 442480445762334578L;

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for questionReply, returns
   * "questionReply"
   */
  @Override
  public String getSessionControlBeanName() {
    return "questionReply";
  }

  /**
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   */
  @Override
  public QuestionReplySessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new QuestionReplySessionController(mainSessionCtrl, componentContext,
        "org.silverpeas.questionReply.multilang.questionReplyBundle",
        "org.silverpeas.questionReply.settings.questionReplyIcons");
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param scc The component Session Control, build and initialised.
   * @param request The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/questionReply/jsp/questionReply.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, QuestionReplySessionController scc,
      HttpRequest request) {

    String destination;
    String flag = scc.getUserProfil();
    SilverpeasRole role = scc.getUserRole();

    try {
      if (function.startsWith("Main")) {
        scc.setUserProfil();
        flag = scc.getUserProfil();
        request.setAttribute("Flag", flag);
        request.setAttribute("Categories", scc.getAllCategories());
        request.setAttribute("PDCUsed", scc.isPDCUsed());
        if (request.getAttribute("QuestionId") != null) {
          Question question =
              scc.getQuestion(Long.parseLong((String) request.getAttribute("QuestionId")));
          String categoryId = question.getCategoryId();
          if (!StringUtil.isDefined(categoryId)) {
            categoryId = "null";
          }
          destination = "/questionReply/jsp/listQuestionsDHTML.jsp?categoryId=" + categoryId +
              "&questionId=" + question.getPK().getId();
        } else {
          destination = "/questionReply/jsp/listQuestionsDHTML.jsp";
        }
      } else if ("DeleteQ".equals(function)) {
        String id = request.getParameter("Id");
        if (StringUtil.isLong(id)) {
          scc.deleteQuestions(Collections.singletonList(Long.valueOf(id)));
        }
        destination = getDestination("Main", scc, request);
      } else if ("DeleteQuestions".equals(function)) {
        if (admin == role || writer == role) {
          String[] checkQuestions = request.getParameterValues("checkedQuestion");
          if (checkQuestions != null) {
            List<Long> listToDelete = new ArrayList<>(checkQuestions.length);
            for (String checkQuestion : checkQuestions) {
              Long questionId = Long.valueOf(checkQuestion);
              listToDelete.add(questionId);
            }
            scc.deleteQuestions(listToDelete);
          }
          destination = getDestination("Main", scc, request);
        } else {
          destination = "/admin/jsp/errorpage.jsp";
        }
      } else if ("OpenQ".equals(function)) {
        String questionId = request.getParameter("Id");
        scc.openQuestion(Long.parseLong(questionId));
        destination = getDestination("Main", scc, request);
      } else if ("CloseQ".equals(function)) {
        String questionId = request.getParameter("Id");
        scc.closeQuestion(Long.parseLong(questionId));
        destination = getDestination("Main", scc, request);
      } else if ("CloseQuestions".equals(function)) {
        if (admin == role || writer == role) {
          String[] checkQuestions = request.getParameterValues("checkedQuestion");
          List<Long> listToClose = new ArrayList<>();
          if (checkQuestions != null) {
            for (String checkQuestion : checkQuestions) {
              Long questionId = Long.valueOf(checkQuestion);
              listToClose.add(questionId);
            }
          }
          scc.closeQuestions(listToClose);
          destination = getDestination("Main", scc, request);
        } else {
          destination = "/admin/jsp/errorpage.jsp";
        }
      } else if ("CloseQuestion".equals(function)) {
        if (admin == role || writer == role) {
          scc.closeQuestion(Long.parseLong(request.getParameter("QuestionId")));
          destination = getDestination("Main", scc, request);
        } else {
          destination = "/admin/jsp/errorpage.jsp";
        }
      } else if ("ConsultQuestionQuery".equals(function)) {
        String questionId;
        if (request.getParameter("questionId") != null) {
          questionId = request.getParameter("questionId");
        } else {
          questionId = (String) request.getAttribute("questionId");
        }
        if (questionId == null) {
          questionId = scc.getCurrentQuestion().getPK().getId();
        }

        Question question = scc.getQuestion(Long.parseLong(questionId));
        scc.setCurrentQuestion(question);
        request.setAttribute("QuestionId", questionId);
        destination = getDestination("Main", scc, request);
      } else if ("UpdateQ".equals(function)) {
        // mettre à jour la question courante
        String questionId = request.getParameter("QuestionId");
        Question question = scc.getQuestion(Long.parseLong(questionId));
        scc.setCurrentQuestion(question);
        request.setAttribute("question", question);
        request.setAttribute("Flag", scc.getUserProfil());
        request.setAttribute("AllCategories", scc.getAllCategories());
        destination = "/questionReply/jsp/updateQ.jsp";
      } else if ("DeleteR".equals(function)) {
        String questionId = request.getParameter("QuestionId");
        if (StringUtil.isDefined(questionId) && StringUtil.isLong(questionId) &&
            canDeleteReply(role)) {
          Question question = scc.getQuestion(Long.parseLong(questionId));
          scc.setCurrentQuestion(question);
          String id = request.getParameter("replyId");
          if (StringUtil.isDefined(id) && StringUtil.isLong(id)) {
            Long replyId = Long.valueOf(id);
            Collection<Long> replies = new ArrayList<>();
            replies.add(replyId);
            scc.deleteR(replies);
          }
        }
        destination = getDestination("Main", scc, request);
      } else if ("EffectiveUpdateQ".equals(function)) {
        scc.updateCurrentQuestion(request.getParameter("title"), request.getParameter("content"),
            request.getParameter("CategoryId"));
        String questionId = request.getParameter("questionId");
        request.setAttribute("QuestionId", questionId);
        destination = getDestination("Main", scc, request);
      } else if ("CreateRQuery".equals(function)) {
        String id = request.getParameter("QuestionId");
        Question question = scc.getQuestion(Long.parseLong(id));
        scc.setCurrentQuestion(question);
        // passer le paramètre pour savoir si on utilise les réponses privées
        Boolean usedPrivateReplies = scc.isPrivateRepliesEnabled();
        request.setAttribute("UsedPrivateReplies", usedPrivateReplies);
        if (("admin".equals(flag)) || ("writer".equals(flag))) {
          request.setAttribute("reply", scc.getNewReply());
          destination = "/questionReply/jsp/addR.jsp";
        } else {
          destination = "/admin/jsp/errorpage.jsp";
        }
      } else if ("EffectiveCreateR".equals(function)) {
        int publicReply = 1;
        if (StringUtil.isInteger(request.getParameter("publicReply"))) {
          // 0 = private, 1 = public
          publicReply = Integer.parseInt(request.getParameter("publicReply"));
        }
        scc.setNewReplyContent(request.getParameter("title"), request.getParameter("content"),
            publicReply, publicReply == 1 ? 0 : 1);
        scc.saveNewReply(request.getUploadedFiles());

        if (scc.getCurrentQuestion() != null) {
          request.setAttribute("QuestionId", scc.getCurrentQuestion().getPK().getId());
        }

        destination = getDestination("Main", scc, request);
      } else if ("UpdateRQuery".equals(function)) {
        request.setAttribute("reply", scc.getCurrentReply());
        destination = "/questionReply/jsp/updateR.jsp";
      } else if ("UpdateR".equals(function)) {
        String questionId = request.getParameter("QuestionId");
        Question question = scc.getQuestion(Long.parseLong(questionId));
        scc.setCurrentQuestion(question);
        String id = request.getParameter("replyId");
        Reply reply = scc.getReply(Long.parseLong(id));
        scc.setCurrentReply(reply);
        request.setAttribute("reply", scc.getCurrentReply());
        destination = "/questionReply/jsp/updateR.jsp";
      } else if ("EffectiveUpdateR".equals(function)) {
        scc.updateCurrentReply(request.getParameter("title"), request.getParameter("content"));
        if (scc.getCurrentQuestion() != null) {
          request.setAttribute("QuestionId", scc.getCurrentQuestion().getPK().getId());
        }

        destination = getDestination("Main", scc, request);
      } else if ("CreateQueryQR".equals(function)) {
        request.setAttribute("question", scc.getNewQuestion());
        request.setAttribute("reply", scc.getNewReply());
        request.setAttribute("AllCategories", scc.getAllCategories());
        destination = "/questionReply/jsp/addQR.jsp";
      } else if ("EffectiveCreateQR".equals(function)) {

        scc.setNewQuestionContent(request.getParameter("title"), request.getParameter("content"),
            request.getParameter("CategoryId"));
        scc.setNewReplyContent(request.getParameter("titleR"), request.getParameter("contentR"), 1,
            0);
        // Get classification positions
        String positions = request.getParameter("Positions");
        long questionId = scc.saveNewFAQ(request.getUploadedFiles());
        String id = Long.toString(questionId);
        scc.classifyQuestionReply(questionId, positions);
        scc.getQuestion(questionId);
        request.setAttribute("QuestionId", id);
        request.setAttribute("contentId", scc.getCurrentQuestionContentId());
        destination = getDestination("Main", scc, request);
      } else if ("CreateQQuery".equals(function)) {
        scc.setUserProfil();
        flag = scc.getUserProfil();
        if ("publisher".equals(flag) || "admin".equals(flag) || "writer".equals(flag)) {
          scc.setUserProfil(publisher.name());
          request.setAttribute("question", scc.getNewQuestion());
          request.setAttribute("AllCategories", scc.getAllCategories());
          destination = "/questionReply/jsp/addQ.jsp";
        } else {
          destination = "/admin/jsp/errorpage.jsp";
        }
      } else if ("EffectiveCreateQ".equals(function)) {
        if ("publisher".equals(flag) || "admin".equals(flag) || "writer".equals(flag)) {
          scc.setNewQuestionContent(request.getParameter("title"), request.getParameter("content"),
              request.getParameter("CategoryId"));
          long questionId = scc.saveNewQuestion();
          String id = Long.toString(questionId);
          String positions = request.getParameter("Positions");
          scc.classifyQuestionReply(questionId, positions);
          request.setAttribute("QuestionId", id);
          destination = getDestination("Main", scc, request);
        } else {
          destination = "/admin/jsp/errorpage.jsp";
        }
      } else if ("ViewAttachments".equals(function)) {
        request.setAttribute("CurrentReply", scc.getCurrentReply());
        request.setAttribute("Language", scc.getLanguage());
        destination = "/questionReply/jsp/attachmentManager.jsp";
      } else if ("CreateCategory".equals(function)) {
        String name = request.getParameter("Name");
        String description = request.getParameter("Description");
        NodeDetail node =
            new NodeDetail("unknown", name, description, 0, "unknown");
        Category category = new Category(node);
        scc.createCategory(category);
        destination = getDestination("Main", scc, request);
      } else if ("UpdateCategory".equals(function)) {
        String categoryId = request.getParameter("CategoryId");
        Category category = scc.getCategory(categoryId);
        String name = request.getParameter("Name");
        category.setName(name);
        String desc = request.getParameter("Description");
        category.setDescription(desc);
        // MAJ base
        scc.updateCategory(category);

        destination = getDestination("Main", scc, request);
      } else if ("DeleteCategory".equals(function)) {
        String categoryId = request.getParameter("CategoryId");
        scc.deleteCategory(categoryId);

        destination = getDestination("Main", scc, request);
      } else if (function.startsWith("searchResult")) {
        // traitement des recherches
        String id = request.getParameter("Id");
        String type = request.getParameter("Type");

        if ("Question".equals(type)) {
          // traitement des questions
          request.setAttribute("QuestionId", id);
          destination = getDestination("Main", scc, request);
        } else if (type.startsWith("Reply")) {
          // traitement des réponses, on arrive sur la question contenant la réponse
          Reply reply = scc.getReply(Long.parseLong(id));
          long questionId = reply.getQuestionId();
          request.setAttribute("QuestionId", Long.toString(questionId));
          destination = getDestination("Main", scc, request);
        } else if (type.startsWith("Publication")) {
          // traitement des fichiers joints
          Reply reply = scc.getReply(Long.valueOf(id));
          long questionId = reply.getQuestionId();
          request.setAttribute("QuestionId", Long.toString(questionId));
          destination = getDestination("Main", scc, request);
        } else {
          if (StringUtil.isDefined(id)) {
            request.setAttribute("QuestionId", id);
          }
          destination = getDestination("Main", scc, request);
        }
      } else if ("Export".equals(function)) {
        MultiSilverpeasBundle resource = (MultiSilverpeasBundle) request.getAttribute("resources");
        ExportReport report = scc.export(resource);
        request.setAttribute("ExportReport", report);
        destination = "/questionReply/jsp/downloadZip.jsp";
      } else if (function.startsWith("portlet")) {
        scc.setUserProfil();
        request.setAttribute("Categories", scc.getAllCategories());
        destination = "/questionReply/jsp/portlet.jsp";
      } else {
        destination = "/questionReply/jsp/" + function;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpage.jsp";
    }

    return destination;
  }

  private boolean canDeleteReply(SilverpeasRole role) {
    return admin == role || writer == role;
  }
}