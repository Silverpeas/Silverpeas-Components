package com.stratelia.webactiv.survey.servlets;

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

public class GoToSurvey extends GoTo {
  public String getDestination(String objectId, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    QuestionContainerPK questionContainerPK = new QuestionContainerPK(objectId);
    QuestionContainerHeader survey = getQuestionContainerBm()
        .getQuestionContainerHeader(questionContainerPK);

    if (survey != null) {
      String componentId = survey.getInstanceId();

      SilverTrace.info("survey", "GoToSurvey.getDestination",
          "root.MSG_GEN_PARAM_VALUE", "survey = " + survey.getId()
              + "componentId = " + componentId);

      String gotoURL = URLManager.getURL(null, componentId) + survey.getURL();

      return "goto=" + URLEncoder.encode(gotoURL);
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