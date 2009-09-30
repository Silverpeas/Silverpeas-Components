package com.stratelia.webactiv.quizz;

import java.util.Collection;
import java.util.Iterator;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.applicationIndexer.control.ComponentIndexerInterface;
import com.stratelia.webactiv.quizz.control.QuizzSessionController;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerHeader;

public class QuizzIndexer implements ComponentIndexerInterface {

  private QuizzSessionController scc = null;

  public void index(MainSessionController mainSessionCtrl,
      ComponentContext context) throws QuizzException {
    try {
      scc = new QuizzSessionController(mainSessionCtrl, context);
      Collection quizzes = scc.getAdminQuizzList();
      Iterator itQ = quizzes.iterator();
      while (itQ.hasNext()) {
        QuestionContainerHeader quizzHeader = (QuestionContainerHeader) itQ
            .next();
        scc.updateQuizzHeader(quizzHeader, quizzHeader.getPK().getId());
      }
    } catch (Exception e) {
      throw new QuizzException("QuizzIndexer.index", QuizzException.WARNING,
          "Quizz.EX_CANNOT_UPDATE_QUIZZ_HEADER", e);
    }
  }
}