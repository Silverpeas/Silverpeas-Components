/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.survey;

import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerHeader;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerPK;
import org.silverpeas.core.questioncontainer.container.service.QuestionContainerService;
import org.silverpeas.core.silverstatistics.volume.model.UserIdCountVolumeCouple;
import org.silverpeas.core.silverstatistics.volume.service.ComponentStatisticsProvider;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Provider
@Named("survey" + ComponentStatisticsProvider.QUALIFIER_SUFFIX)
public class SurveyStatistics implements ComponentStatisticsProvider {

  @Inject
  private QuestionContainerService questionContainerService;

  @Override
  public Collection<UserIdCountVolumeCouple> getVolume(String spaceId, String componentId) {
    List<UserIdCountVolumeCouple> myArrayList = new ArrayList<>();

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

  private QuestionContainerService getQuestionContainerService() {
    return questionContainerService;
  }

  private Collection<QuestionContainerHeader> getOpenedSurveys(String spaceId, String componentId) {
    return getQuestionContainerService()
        .getOpenedQuestionContainers(new QuestionContainerPK(null, spaceId, componentId));
  }

  private Collection<QuestionContainerHeader> getClosedSurveys(String spaceId, String componentId) {
    return getQuestionContainerService()
        .getClosedQuestionContainers(new QuestionContainerPK(null, spaceId, componentId));
  }

  private Collection<QuestionContainerHeader> getInWaitSurveys(String spaceId, String componentId) {
    return getQuestionContainerService()
        .getInWaitQuestionContainers(new QuestionContainerPK(null, spaceId, componentId));
  }
}
