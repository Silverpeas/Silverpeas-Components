package com.stratelia.webactiv.survey;

import java.util.Collection;
import java.util.Iterator;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.applicationIndexer.control.ComponentIndexerInterface;
import com.stratelia.webactiv.survey.control.SurveySessionController;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerHeader;


public class SurveyIndexer implements ComponentIndexerInterface {
     
    private SurveySessionController scc = null;
    
    public void index(MainSessionController mainSessionCtrl, ComponentContext context) throws SurveyException {

        scc = new SurveySessionController(mainSessionCtrl, context);

        indexOpenedSurveys();
        indexClosedSurveys();
        indexInWaitSurveys();
    }

    private void indexOpenedSurveys() throws SurveyException {
        Collection surveys = scc.getOpenedSurveys();
        indexSurveys(surveys);
    }

    private void indexClosedSurveys() throws SurveyException {
        Collection surveys = scc.getClosedSurveys();
        indexSurveys(surveys);
    }

    private void indexInWaitSurveys() throws SurveyException {
        Collection surveys = scc.getInWaitSurveys();
        indexSurveys(surveys);
    }

    private void indexSurveys(Collection surveys) throws SurveyException {
        Iterator it = surveys.iterator();
        while (it.hasNext()) {
            QuestionContainerHeader surveyHeader = (QuestionContainerHeader) it.next();
            scc.updateSurveyHeader(surveyHeader, surveyHeader.getPK().getId());
        }
    }
}