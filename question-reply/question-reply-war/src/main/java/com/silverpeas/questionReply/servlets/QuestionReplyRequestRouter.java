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
package com.silverpeas.questionReply.servlets;

import com.silverpeas.importExport.report.ExportReport;
import com.silverpeas.questionReply.control.QuestionReplySessionController;
import com.silverpeas.questionReply.model.Category;
import com.silverpeas.questionReply.model.Question;
import com.silverpeas.questionReply.model.Reply;
import com.silverpeas.subscribe.SubscriptionServiceProvider;
import com.silverpeas.subscribe.service.ComponentSubscription;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.containerManager.ContainerContext;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import org.silverpeas.servlet.HttpRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.stratelia.webactiv.SilverpeasRole.*;

/**
 * Router class for SuestionReply component
 */
public class QuestionReplyRequestRouter extends
    ComponentRequestRouter<QuestionReplySessionController> {

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
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  @Override
  public QuestionReplySessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new QuestionReplySessionController(mainSessionCtrl, componentContext,
        "com.silverpeas.questionReply.multilang.questionReplyBundle",
        "com.silverpeas.questionReply.settings.questionReplyIcons");
  }

  /**
   * Extract the container context from the request and save it in the session controller. If this
   * context is null then get the last one from the session controller. So the containerContext is
   * the same in the request and the session.
   */
  private void resetContainerContext(QuestionReplySessionController scc, HttpServletRequest request) {
    ContainerContext containerContext = (ContainerContext) request.getAttribute("ContainerContext");

    if (containerContext != null) {
      SilverTrace.info("questionReply", "QuestionReplyRequestRouter.resetContainerContext()",
          "root.MSG_GEN_PARAM_VALUE", "returnURL != null");
      scc.setContainerContext(containerContext);
    } else {
      containerContext = scc.getContainerContext();
      request.setAttribute("ContainerContext", containerContext);
    }
  }

  private void resetReturnURL(QuestionReplySessionController scc,
      HttpServletRequest request) {
    String returnURL = request.getParameter("ReturnURL");
    if (StringUtil.isDefined(returnURL)) {
      SilverTrace.info("questionReply",
          "QuestionReplyRequestRouter.resetReturnURL()",
          "root.MSG_GEN_PARAM_VALUE", "returnURL != null");
      scc.setReturnURL(returnURL);
    } else {
      returnURL = scc.getReturnURL();
    }
    request.setAttribute("ReturnURL", returnURL);
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param scc The component Session Control, build and initialised.
   * @param request The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/questionReply/jsp/questionReply.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, QuestionReplySessionController scc,
      HttpRequest request) {
    SilverTrace.info("questionReply", "QuestionReplyRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", function);
    String destination;
    String flag = scc.getUserProfil();
    SilverpeasRole role = scc.getUserRole();
    SilverTrace.info("questionReply", "QuestionReplyRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "flag = " + flag);
    try {
      resetContainerContext(scc, request);
      resetReturnURL(scc, request);
      if (function.startsWith("Main")) {
        scc.setUserProfil();
        flag = scc.getUserProfil();
        Collection<Question> allQuestions = scc.getAllQuestions();
        request.setAttribute("questions", allQuestions);
        request.setAttribute("Flag", flag);
        request.setAttribute("UserId", scc.getUserId());
        request.setAttribute("Categories", scc.getAllCategories());
        request.setAttribute("userAlreadySubscribed",
            SubscriptionServiceProvider.getSubscribeService().existsSubscription(
                new ComponentSubscription(scc.getUserId(), scc.getComponentId())));
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
            List<Long> listToDelete = new ArrayList<Long>(checkQuestions.length);
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
      } else if (function.equals("OpenQ")) {
        String questionId = request.getParameter("Id");
        scc.openQuestion(Long.parseLong(questionId));
        destination = getDestination("Main", scc, request);
      } else if (function.equals("CloseQ")) {
        String questionId = request.getParameter("Id");
        scc.closeQuestion(Long.parseLong(questionId));
        destination = getDestination("Main", scc, request);
      } else if (function.equals("CloseQuestions")) {
        if (admin == role || writer == role) {
          String[] checkQuestions = request.getParameterValues("checkedQuestion");
          List<Long> listToClose = new ArrayList<Long>();
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
      } else if (function.equals("CloseQuestion")) {
        if (admin == role || writer == role) {
          scc.closeQuestion(Long.parseLong(request.getParameter("QuestionId")));
          destination = getDestination("Main", scc, request);
        } else {
          destination = "/admin/jsp/errorpage.jsp";
        }
      } else if (function.equals("ConsultQuestionQuery")) {
        String questionId;
        if (request.getParameter("questionId") != null) {
          questionId = request.getParameter("questionId");
        } else {
          questionId = (String) request.getAttribute("questionId");
        }
        if (questionId == null) {
          questionId = scc.getCurrentQuestion().getPK().getId();
        }
        SilverTrace.info("questionReply", "QuestionReplyRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "questionId = " + questionId);
        Question question = scc.getQuestion(Long.parseLong(questionId));
        scc.setCurrentQuestion(question);
        request.setAttribute("QuestionId", questionId);
        destination = getDestination("Main", scc, request);
      } else if (function.equals("ConsultQuestion")) {
        scc.setUserProfil();
        flag = scc.getUserProfil();
        request.setAttribute("Flag", flag);
        request.setAttribute("UserId", scc.getUserId());
        Question question = scc.getCurrentQuestion();
        request.setAttribute("question", question);
        request.setAttribute("contentId", scc.getCurrentQuestionContentId());
        destination = "/questionReply/jsp/consultQuestion.jsp";
      } else if (function.equals("UpdateQ")) {
        // mettre à jour la question courante
        String questionId = request.getParameter("QuestionId");
        Question question = scc.getQuestion(Long.parseLong(questionId));
        scc.setCurrentQuestion(question);
        request.setAttribute("question", question);
        request.setAttribute("Flag", scc.getUserProfil());
        request.setAttribute("AllCategories", scc.getAllCategories());
        destination = "/questionReply/jsp/updateQ.jsp";
      } else if (function.equals("UpdateQQuery")) {
        if (admin == role || writer == role) {
          request.setAttribute("question", scc.getCurrentQuestion());
          destination = "/questionReply/jsp/updateQ.jsp";
        } else {
          destination = "/admin/jsp/errorpage.jsp";
        }
      } else if ("DeleteReplies".equals(function)) {
        if (canDeleteReply(role)) {
          String[] checkReplies = request.getParameterValues("checkedReply");
          List<Long> listToDelete = new ArrayList<Long>();
          if (checkReplies != null) {
            for (String checkReply : checkReplies) {
              Long replyId = Long.valueOf(checkReply);
              listToDelete.add(replyId);
            }
          }
          scc.deleteReplies(listToDelete);
          destination = getDestination("Main", scc, request);
        } else {
          destination = "/admin/jsp/errorpage.jsp";
        }
      } else if (function.equals("DeleteR")) {
        String questionId = request.getParameter("QuestionId");
        if (StringUtil.isDefined(questionId) && StringUtil.isLong(questionId) &&
            canDeleteReply(role)) {
          Question question = scc.getQuestion(Long.parseLong(questionId));
          scc.setCurrentQuestion(question);
          String id = request.getParameter("replyId");
          if (StringUtil.isDefined(id) && StringUtil.isLong(id)) {
            Long replyId = Long.valueOf(id);
            Collection<Long> replies = new ArrayList<Long>();
            replies.add(replyId);
            scc.deleteR(replies);
          }
        }
        destination = getDestination("Main", scc, request);
      } else if (function.equals("EffectiveUpdateQ")) {
        scc.updateCurrentQuestion(request.getParameter("title"), request.getParameter("content"),
            request.getParameter("CategoryId"));
        String questionId = request.getParameter("questionId");
        request.setAttribute("QuestionId", questionId);
        destination = getDestination("Main", scc, request);
      } else if (function.equals("CreateRQuery")) {
        String id = request.getParameter("QuestionId");
        Question question = scc.getQuestion(Long.parseLong(id));
        scc.setCurrentQuestion(question);
        // passer le paramètre pour savoir si on utilise les réponses privées
        Boolean usedPrivateReplies = scc.isPrivateRepliesEnabled();
        request.setAttribute("UsedPrivateReplies", usedPrivateReplies);
        if ((flag.equals("admin")) || (flag.equals("writer"))) {
          request.setAttribute("reply", scc.getNewReply());
          destination = "/questionReply/jsp/addR.jsp";
        } else {
          destination = "/admin/jsp/errorpage.jsp";
        }
      } else if (function.equals("EffectiveCreateR")) {
        int publicReply = 1;
        if (StringUtil.isInteger(request.getParameter("publicReply"))) {
          publicReply = Integer.parseInt(request.getParameter("publicReply")); // 0 = private, 1 =
                                                                               // public
        }
        scc.setNewReplyContent(request.getParameter("title"), request.getParameter("content"),
            publicReply, publicReply == 1 ? 0 : 1);
        scc.saveNewReply();

        if (scc.getCurrentQuestion() != null) {
          request.setAttribute("QuestionId", scc.getCurrentQuestion().getPK().getId());
        }

        destination = getDestination("Main", scc, request);
      } else if (function.equals("UpdateRQuery")) {
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
      } else if (function.equals("RelaunchQuery")) {
        if ((flag.equals("admin")) || (flag.equals("publisher"))) {
          destination = scc.genericWriters();
        } else {
          destination = "/admin/jsp/errorpage.jsp";
        }
      } else if (function.equals("EffectiveRelaunch")) {
        if ((flag.equals("admin")) || (flag.equals("publisher"))) {
          scc.relaunchRecipients();
          destination = getDestination("ConsultQuestion", scc, request);
        } else {
          destination = "/admin/jsp/errorpage.jsp";
        }
      } else if (function.equals("CreateQueryQR")) {
        request.setAttribute("question", scc.getNewQuestion());
        request.setAttribute("reply", scc.getNewReply());
        request.setAttribute("AllCategories", scc.getAllCategories());
        destination = "/questionReply/jsp/addQR.jsp";
      } else if (function.equals("EffectiveCreateQR")) {

        scc.setNewQuestionContent(request.getParameter("title"), request.getParameter("content"),
            request.getParameter("CategoryId"));
        scc.setNewReplyContent(request.getParameter("titleR"), request.getParameter("contentR"), 1,
            0);
        // Get classification positions
        String positions = request.getParameter("Positions");
        long questionId = scc.saveNewFAQ();
        String id = Long.toString(questionId);
        scc.classifyQuestionReply(questionId, positions);
        scc.getQuestion(questionId);
        request.setAttribute("QuestionId", id);
        request.setAttribute("contentId", scc.getCurrentQuestionContentId());
        destination = getDestination("Main", scc, request);
      } else if (function.equals("CreateQQuery")) {
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
      } else if (function.equals("EffectiveCreateQ")) {
        if (flag.equals("publisher") || flag.equals("admin") || flag.equals("writer")) {
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
      } else if (function.equals("ViewAttachments")) {
        request.setAttribute("CurrentReply", scc.getCurrentReply());
        request.setAttribute("Language", scc.getLanguage());
        destination = "/questionReply/jsp/attachmentManager.jsp";
      } // gestion des catégories
      // ----------------------
      else if (function.equals("ViewCategory")) {
        request.setAttribute("Categories", scc.getAllCategories());
        request.setAttribute("UserId", scc.getUserId());
        destination = getDestination("Main", scc, request);
      } else if (function.equals("NewCategory")) {
        request.setAttribute("UserName", scc.getUserDetail().getDisplayedName());
        destination = "/questionReply/jsp/categoryManager.jsp";
      } else if (function.equals("CreateCategory")) {
        // récupération des paramètres
        String name = request.getParameter("Name");
        String description = request.getParameter("Description");
        NodeDetail node = new NodeDetail("unknown", name, description, null,
            null, null, "0", "unknown");
        Category category = new Category(node);
        scc.createCategory(category);

        destination = getDestination("ViewCategory", scc, request);
      } else if (function.equals("EditCategory")) {
        String categoryId = request.getParameter("CategoryId");
        Category category = scc.getCategory(categoryId);
        request.setAttribute("Category", category);

        destination = "/questionReply/jsp/categoryManager.jsp";
      } else if (function.equals("UpdateCategory")) {
        String categoryId = request.getParameter("CategoryId");
        Category category = scc.getCategory(categoryId);
        String name = request.getParameter("Name");
        category.setName(name);
        String desc = request.getParameter("Description");
        category.setDescription(desc);
        // MAJ base
        scc.updateCategory(category);

        destination = getDestination("ViewCategory", scc, request);
      } else if (function.equals("DeleteCategory")) {
        String categoryId = request.getParameter("CategoryId");
        scc.deleteCategory(categoryId);

        destination = getDestination("ViewCategory", scc, request);
      } else if (function.startsWith("searchResult")) {
        // traitement des recherches
        String id = request.getParameter("Id");
        String type = request.getParameter("Type");

        SilverTrace.info("questionReply", "QuestionReplyRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "type = " + type + " id = " + id);

        if ("Question".equals(type)) {
          // traitement des questions
          request.setAttribute("QuestionId", id);
          destination = getDestination("Main", scc, request);
        } else if (type.startsWith("Reply")) {
          // traitement des réponses, on arrive sur la question contenant la
          // réponse
          Reply reply = scc.getReply(Long.parseLong(id));
          long questionId = reply.getQuestionId();
          request.setAttribute("QuestionId", Long.toString(questionId));

          SilverTrace.info("questionReply", "QuestionReplyRequestRouter.getDestination()",
              "root.MSG_GEN_PARAM_VALUE", "questionId = " + questionId + " replyId = " + id);

          destination = getDestination("Main", scc, request);
        } else if (type.startsWith("Publication")) {
          // traitement des fichiers joints
          Reply reply = scc.getReply(Long.valueOf(id));
          long questionId = reply.getQuestionId();
          request.setAttribute("QuestionId", Long.toString(questionId));

          SilverTrace.info("questionReply",
              "QuestionReplyRequestRouter.getDestination()",
              "root.MSG_GEN_PARAM_VALUE", "questionId = " + questionId +
                  " replyId = " + id);

          destination = getDestination("Main", scc, request);
        } else {
          destination = getDestination("Main", scc, request);
        }
      } else if (function.equals("Export")) {
        ResourcesWrapper resource = (ResourcesWrapper) request.getAttribute("resources");
        ExportReport report = scc.export(resource);
        request.setAttribute("ExportReport", report);
        destination = "/questionReply/jsp/downloadZip.jsp";
      } else if (function.startsWith("portlet")) {
        scc.setUserProfil();
        Collection<Question> allQuestions = scc.getAllQuestions();
        request.setAttribute("questions", allQuestions);
        request.setAttribute("Flag", "user");
        request.setAttribute("UserId", scc.getUserId());
        request.setAttribute("Categories", scc.getAllCategories());
        destination = "/questionReply/jsp/portlet.jsp";
      } else {
        destination = "/questionReply/jsp/" + function;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpage.jsp";
    }
    SilverTrace.info("questionReply",
        "QuestionReplyRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "destination " + destination);
    return destination;
  }

  boolean canDeleteReply(SilverpeasRole role) {
    return (admin == role || writer == role || publisher == role);
  }
}
