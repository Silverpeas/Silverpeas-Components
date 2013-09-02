/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.stratelia.webactiv.quizz;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.EJBException;

import com.silverpeas.silverstatistics.ComponentStatisticsInterface;
import com.silverpeas.silverstatistics.UserIdCountVolumeCouple;

import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.questionContainer.control.QuestionContainerBm;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerHeader;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerPK;

/**
 * Class declaration
 *
 * @author
 */
public class QuizzStatistics implements ComponentStatisticsInterface {

  private QuestionContainerBm questionContainerBm = null;

  @Override
  public Collection<UserIdCountVolumeCouple> getVolume(String spaceId, String componentId)
      throws Exception {
    Collection<QuestionContainerHeader> headers = getQuizz(spaceId, componentId);
    List<UserIdCountVolumeCouple> myArrayList = new ArrayList<UserIdCountVolumeCouple>(headers.
        size());
    for (QuestionContainerHeader qcHeader : headers) {
      UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();
      myCouple.setUserId(qcHeader.getCreatorId());
      myCouple.setCountVolume(1);
      myArrayList.add(myCouple);
    }

    return myArrayList;
  }

  private QuestionContainerBm getQuestionContainerBm() {
    if (questionContainerBm == null) {
      try {
        questionContainerBm = EJBUtilitaire.getEJBObjectRef(JNDINames.QUESTIONCONTAINERBM_EJBHOME,
            QuestionContainerBm.class);
      } catch (Exception e) {
        throw new EJBException(e);
      }
    }
    return questionContainerBm;
  }

  public Collection<QuestionContainerHeader> getQuizz(String spaceId, String componentId)
      throws Exception {
    return getQuestionContainerBm().getNotClosedQuestionContainers(
        new QuestionContainerPK(null, spaceId, componentId));
  }
}
