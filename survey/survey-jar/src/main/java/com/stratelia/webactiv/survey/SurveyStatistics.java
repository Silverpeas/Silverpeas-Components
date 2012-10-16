/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

import com.silverpeas.silverstatistics.ComponentStatisticsInterface;
import com.silverpeas.silverstatistics.UserIdCountVolumeCouple;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.questionContainer.control.QuestionContainerBm;
import com.stratelia.webactiv.util.questionContainer.control.QuestionContainerBmHome;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerHeader;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerPK;

import javax.ejb.EJBException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class declaration
 *
 * @author
 */
public class SurveyStatistics implements ComponentStatisticsInterface {

  private QuestionContainerBm questionContainerBm = null;

  /**
   * Method declaration
   *
   * @param spaceId
   * @param componentId
   * @return
   * @see
   */
  @Override
  public Collection<UserIdCountVolumeCouple> getVolume(String spaceId, String componentId) throws Exception {
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
   for(QuestionContainerHeader surveyHeader : c) {
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
  private QuestionContainerBm getQuestionContainerBm() {
    if (questionContainerBm == null) {
      try {
        QuestionContainerBmHome questionContainerBmHome = EJBUtilitaire.getEJBObjectRef(
            JNDINames.QUESTIONCONTAINERBM_EJBHOME, QuestionContainerBmHome.class);
        questionContainerBm = questionContainerBmHome.create();
      } catch (Exception e) {
        throw new EJBException(e);
      }
    }
    return questionContainerBm;
  }

  /**
   * Method declaration
   *
   * @param spaceId
   * @param componentId
   * @return
   * @throws RemoteException
   * @see
   */

  public Collection getOpenedSurveys(String spaceId, String componentId)
      throws RemoteException {
    Collection<QuestionContainerHeader> result =
        getQuestionContainerBm().getOpenedQuestionContainers(
            new QuestionContainerPK(null, spaceId, componentId));
    return result;
  }

  public Collection<QuestionContainerHeader> getClosedSurveys(String spaceId, String componentId)
      throws RemoteException {
    return getQuestionContainerBm().getClosedQuestionContainers(new QuestionContainerPK(
        null, spaceId, componentId));
  }

  public Collection<QuestionContainerHeader> getInWaitSurveys(String spaceId, String componentId)
      throws RemoteException {
   return getQuestionContainerBm().getInWaitQuestionContainers(new QuestionContainerPK(null,
       spaceId, componentId));
  }
}
