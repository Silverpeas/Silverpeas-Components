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
package com.silverpeas.questionReply;

import com.silverpeas.questionReply.control.QuestionManagerFactory;
import com.silverpeas.questionReply.index.QuestionIndexer;
import com.silverpeas.questionReply.model.Question;
import com.silverpeas.questionReply.model.Reply;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.applicationIndexer.control.ComponentIndexerInterface;
import java.util.Collection;

/**
 *
 * @author ehugonnet
 */
public class QuestionReplyIndexer implements ComponentIndexerInterface {

  private final QuestionIndexer questionIndexer = new QuestionIndexer();

  public QuestionReplyIndexer() {
  }

  @Override
  public void index(MainSessionController mainSessionCtrl, ComponentContext context) throws
      Exception {
    Collection<Question> questions = QuestionManagerFactory.getQuestionManager().getAllQuestions(context.
        getCurrentComponentId());
    for (Question question : questions) {
      Collection<Reply> replies = QuestionManagerFactory.getQuestionManager().getAllReplies(
          Long.parseLong(question.getPK().getId()), question.getInstanceId());
      questionIndexer.updateIndex(question, replies);
    }
  }
}
