/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.stratelia.webactiv.survey;

import java.util.Collection;
import java.util.Iterator;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.applicationIndexer.control.ComponentIndexerInterface;
import com.stratelia.webactiv.survey.control.SurveySessionController;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerHeader;
/**
 * This class is the main entry point to index the content of survey component
 *
 */
public class SurveyIndexer implements ComponentIndexerInterface {

  private SurveySessionController scc = null;

  /**
   * Implementation of component indexer interface
   */
  public void index(MainSessionController mainSessionCtrl, ComponentContext context)
      throws SurveyException {

    scc = new SurveySessionController(mainSessionCtrl, context);

    indexOpenedSurveys();
    indexClosedSurveys();
    indexInWaitSurveys();
  }

  private void indexOpenedSurveys() throws SurveyException {
    Collection<QuestionContainerHeader> surveys = scc.getOpenedSurveys();
    indexSurveys(surveys);
  }

  private void indexClosedSurveys() throws SurveyException {
    Collection<QuestionContainerHeader> surveys = scc.getClosedSurveys();
    indexSurveys(surveys);
  }

  private void indexInWaitSurveys() throws SurveyException {
    Collection<QuestionContainerHeader> surveys = scc.getInWaitSurveys();
    indexSurveys(surveys);
  }

  private void indexSurveys(Collection<QuestionContainerHeader> surveys) throws SurveyException {
    Iterator<QuestionContainerHeader> it = surveys.iterator();
    while (it.hasNext()) {
      QuestionContainerHeader surveyHeader = it.next();
      scc.updateSurveyHeader(surveyHeader, surveyHeader.getPK().getId());
    }
  }
}