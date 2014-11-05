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
package com.stratelia.webactiv.survey;

import com.silverpeas.silverstatistics.ComponentStatisticsInterface;
import com.silverpeas.silverstatistics.UserIdCountVolumeCouple;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.questionContainer.control.QuestionContainerService;
import com.stratelia.webactiv.questionContainer.model.QuestionContainerHeader;
import com.stratelia.webactiv.questionContainer.model.QuestionContainerPK;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class declaration
 *
 * @author
 */
public class SurveyStatistics implements ComponentStatisticsInterface {

  @Inject
  private QuestionContainerService questionContainerService;

  /**
   * Method declaration
   *
   * @param spaceId
   * @param componentId
   * @return
   * @see
   */
  @Override
  public Collection<UserIdCountVolumeCouple> getVolume(String spaceId, String componentId) throws
      Exception {
    List<UserIdCountVolumeCouple> myArrayList = new ArrayList<UserIdCountVolumeCouple>();

    Collection<QuestionContainerHeader> c = getOpenedSurveys(spaceId, componentId);
    addSurveys(c, myArrayList);
    c = getClosedSurveys(spaceId, componentId);
    addSurveys(c, myArrayList);
    c = getInWaitSurveys(spaceId, componentId);
    addSurveys(c, myArrayList);

    return myArrayList;
  }

  private void addSurveys(Collection<QuestionContainerHeader> c, List<UserIdCountVolumeCouple> al) {
    for (QuestionContainerHeader surveyHeader : c) {
      UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();
      myCouple.setUserId(surveyHeader.getCreatorId());
      myCouple.setCountVolume(1);
      al.add(myCouple);
    }
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  private QuestionContainerService getQuestionContainerService() {
    if (questionContainerService == null) {
      SilverTrace.fatal("survey", "SurveyStatistics.getQuestionContainerService()",
          "cannot inject question container BM");
    }
    return questionContainerService;
  }

  /**
   * Method declaration
   *
   * @param spaceId
   * @param componentId
   * @return
   * @see
   */
  public Collection getOpenedSurveys(String spaceId, String componentId) {
    return getQuestionContainerService().getOpenedQuestionContainers(new QuestionContainerPK(null,
        spaceId, componentId));
  }

  public Collection<QuestionContainerHeader> getClosedSurveys(String spaceId, String componentId) {
    return getQuestionContainerService().getClosedQuestionContainers(new QuestionContainerPK(
        null, spaceId, componentId));
  }

  public Collection<QuestionContainerHeader> getInWaitSurveys(String spaceId, String componentId) {
    return getQuestionContainerService().getInWaitQuestionContainers(new QuestionContainerPK(null,
        spaceId, componentId));
  }
}
