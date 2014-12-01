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

package com.stratelia.webactiv.quizz;

import com.stratelia.webactiv.applicationIndexer.control.ComponentIndexation;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.questionContainer.control.QuestionContainerService;
import com.stratelia.webactiv.questionContainer.model.QuestionContainerHeader;
import com.stratelia.webactiv.questionContainer.model.QuestionContainerPK;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;

@Singleton
@Named("QuizzComponentIndexation")
public class QuizzIndexer implements ComponentIndexation {

  @Inject
  private QuestionContainerService service;

  @Override
  public void index(ComponentInst componentInst) throws QuizzException {
    try {
      QuestionContainerPK pk =
          new QuestionContainerPK(null, componentInst.getSpaceId(), componentInst.getId());
      Collection<QuestionContainerHeader> quizzes = service.getNotClosedQuestionContainers(pk);
      for (QuestionContainerHeader header : quizzes) {
        pk = new QuestionContainerPK(header.getId(), componentInst.getSpaceId(),
            componentInst.getId());
        header.setPK(pk);
        service.updateQuestionContainerHeader(header);
      }
    } catch (Exception e) {
      throw new QuizzException("QuizzIndexer.index", QuizzException.WARNING,
          "Quizz.EX_CANNOT_UPDATE_QUIZZ_HEADER", e);
    }
  }
}