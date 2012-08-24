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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.questionReply.index;

import com.silverpeas.questionReply.model.Question;
import com.silverpeas.questionReply.model.Reply;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;
import java.util.Collection;

/**
 *
 * @author ehugonnet
 */
public class QuestionIndexer {

  public void createIndex(Question question, Collection<Reply> replies) {
    SilverTrace.info("questionReply", "QuestionManager.createQuestionIndex()",
        "root.MSG_GEN_ENTER_METHOD", "Question = " + question.getTitle());
    if (question != null) {
      FullIndexEntry indexEntry = new FullIndexEntry(question.getInstanceId(), "Question",
          question.getPK().getId());
      indexEntry.setTitle(question.getTitle());
      indexEntry.setPreView(question.getContent());
      indexEntry.setCreationDate(question.getCreationDate());
      indexEntry.setCreationUser(question.getCreatorId());
      indexEntry.addTextContent(question.getContent());
      indexEntry.addTextContent(question.getTitle());
      for (Reply reply : replies) {
        indexEntry.addTextContent(reply.getTitle());
        indexEntry.addTextContent(reply.loadWysiwygContent());
      }
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }
  
  
  public void updateIndex(Question question, Collection<Reply> replies) {
    deleteIndex(question);
    createIndex(question, replies);
  }

  /**
   * Be carefull we don't delete Replies indexes.
   * @param question 
   */
  public void deleteIndex(Question question) {
    SilverTrace.info("questionReply", "QuestionManager.deleteQuestionIndex()",
        "root.MSG_GEN_ENTER_METHOD", "Question = " + question.toString());
    IndexEntryPK indexEntry = new IndexEntryPK(question.getInstanceId(),
        "Question", question.getPK().getId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  public QuestionIndexer() {
  }
}
