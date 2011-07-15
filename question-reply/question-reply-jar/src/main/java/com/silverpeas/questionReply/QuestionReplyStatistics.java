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

package com.silverpeas.questionReply;

import com.silverpeas.questionReply.control.QuestionManagerFactory;
import com.silverpeas.questionReply.control.SilverpeasQuestionManager;
import com.silverpeas.questionReply.model.Question;
import com.stratelia.silverpeas.silverstatistics.control.ComponentStatisticsInterface;
import com.stratelia.silverpeas.silverstatistics.control.UserIdCountVolumeCouple;
import com.stratelia.webactiv.persistence.PersistenceException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class declaration
 * @author
 */
public class QuestionReplyStatistics implements ComponentStatisticsInterface {

  @Override
  public Collection getVolume(String spaceId, String componentId) throws Exception {
    List<UserIdCountVolumeCouple> myArrayList = new ArrayList<UserIdCountVolumeCouple>();
    Collection<Question> c = getQuestionReplies(componentId);
    for (Question detail : c) {
      UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();

      myCouple.setUserId(detail.getCreatorId());
      myCouple.setCountVolume(1);
      myArrayList.add(myCouple);
    }
    return myArrayList;
  }

  public Collection<Question> getQuestionReplies(String componentId)
      throws QuestionReplyException, PersistenceException {
    return QuestionManagerFactory.getQuestionManager().getQuestions(componentId);
  }

}
