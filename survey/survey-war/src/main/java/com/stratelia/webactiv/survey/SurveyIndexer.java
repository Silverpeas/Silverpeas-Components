/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

import com.stratelia.webactiv.applicationIndexer.control.ComponentIndexation;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.questionContainer.control.QuestionContainerService;
import com.stratelia.webactiv.questionContainer.model.QuestionContainerHeader;
import com.stratelia.webactiv.questionContainer.model.QuestionContainerPK;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;

/**
 * This class is the main entry point to index the content of survey component
 *
 */
@Singleton
public class SurveyIndexer implements ComponentIndexation {

  @Inject
  private QuestionContainerService questionContainerService;

  /**
   * Implementation of component indexer interface
   */
  @Override
  public void index(ComponentInst componentInst) {
    QuestionContainerPK pk =
        new QuestionContainerPK(null, componentInst.getSpaceId(), componentInst.getId());
    indexOpenedSurveys(pk);
    indexClosedSurveys(pk);
    indexInWaitSurveys(pk);
  }

  private void indexOpenedSurveys(QuestionContainerPK pk) {
    Collection<QuestionContainerHeader> surveys =
        questionContainerService.getOpenedQuestionContainers(pk);
    indexSurveys(pk, surveys);
  }

  private void indexClosedSurveys(QuestionContainerPK pk) {
    Collection<QuestionContainerHeader> surveys =
        questionContainerService.getClosedQuestionContainers(pk);
    indexSurveys(pk, surveys);
  }

  private void indexInWaitSurveys(QuestionContainerPK pk) {
    Collection<QuestionContainerHeader> surveys =
        questionContainerService.getInWaitQuestionContainers(pk);
    indexSurveys(pk, surveys);
  }

  private void indexSurveys(QuestionContainerPK pk, Collection<QuestionContainerHeader> surveys) {
    for (QuestionContainerHeader surveyHeader : surveys) {
      pk.setId(surveyHeader.getId());
      surveyHeader.setPK(pk);
      questionContainerService.updateQuestionContainerHeader(surveyHeader);
    }
  }
}