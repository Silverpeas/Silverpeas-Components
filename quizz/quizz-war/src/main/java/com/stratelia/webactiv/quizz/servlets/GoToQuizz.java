package com.stratelia.webactiv.quizz.servlets;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.peasUtil.GoTo;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.questionContainer.control.QuestionContainerBm;
import com.stratelia.webactiv.util.questionContainer.control.QuestionContainerBmHome;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerHeader;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerPK;

public class GoToQuizz extends GoTo {
  public String getDestination(String objectId, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    QuestionContainerPK questionContainerPK = new QuestionContainerPK(objectId);
    QuestionContainerHeader quizz = getQuestionContainerBm()
        .getQuestionContainerHeader(questionContainerPK);

    if (quizz != null) {
      String componentId = quizz.getInstanceId();

      SilverTrace.info("quizz", "GoToQuizz.getDestination",
          "root.MSG_GEN_PARAM_VALUE", "quizz = " + quizz.getId()
              + "componentId = " + componentId);

      String gotoURL = URLManager.getURL(null, componentId) + quizz.getURL();

      return "goto=" + URLEncoder.encode(gotoURL, "UTF-8");
    }
    return null;
  }

  private QuestionContainerBm getQuestionContainerBm() {
    QuestionContainerBm currentQuestionContainerBm = null;
    try {
      QuestionContainerBmHome questionContainerBmHome = (QuestionContainerBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.QUESTIONCONTAINERBM_EJBHOME,
              QuestionContainerBmHome.class);
      currentQuestionContainerBm = questionContainerBmHome.create();
    } catch (Exception e) {
      displayError(null);
    }
    return currentQuestionContainerBm;
  }
}