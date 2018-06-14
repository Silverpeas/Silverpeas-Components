/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.components.quizz;

import org.silverpeas.core.SilverpeasException;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerHeader;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerPK;
import org.silverpeas.core.questioncontainer.container.service.QuestionContainerService;
import org.silverpeas.core.web.index.components.ComponentIndexation;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;

@Singleton
@Named("quizz" + ComponentIndexation.QUALIFIER_SUFFIX)
public class QuizzIndexer implements ComponentIndexation {

  @Inject
  private QuestionContainerService service;

  @Override
  public void index(SilverpeasComponentInstance componentInst) throws SilverpeasException {
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
      throw new SilverpeasException(e);
    }
  }
}