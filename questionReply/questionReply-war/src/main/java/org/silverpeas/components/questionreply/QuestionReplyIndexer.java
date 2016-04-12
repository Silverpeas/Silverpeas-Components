/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.questionreply;

import org.silverpeas.components.questionreply.service.QuestionManager;
import org.silverpeas.components.questionreply.index.QuestionIndexer;
import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.components.questionreply.model.Reply;
import org.silverpeas.core.web.index.components.ComponentIndexation;
import org.silverpeas.core.admin.component.model.ComponentInst;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;

/**
 * @author ehugonnet
 */
@Singleton
@Named("questionReply" + ComponentIndexation.QUALIFIER_SUFFIX)
public class QuestionReplyIndexer implements ComponentIndexation {

  private final QuestionIndexer questionIndexer = new QuestionIndexer();
  @Inject
  private QuestionManager questionManager;

  @Override
  public void index(ComponentInst componentInst) throws Exception {
    Collection<Question> questions = questionManager.getAllQuestions(componentInst.getId());
    for (Question question : questions) {
      Collection<Reply> replies = questionManager
          .getAllReplies(Long.parseLong(question.getPK().getId()), question.getInstanceId());
      questionIndexer.updateIndex(question, replies);
    }
  }
}
