/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.questionreply.index;

import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.components.questionreply.model.Reply;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.kernel.annotation.Technical;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.kernel.logging.SilverLogger;

import java.text.ParseException;
import java.util.Collection;

/**
 * @author ehugonnet
 */
@Technical
@Bean
public class QuestionIndexer {

  public void createIndex(Question question, Collection<Reply> replies) {
    FullIndexEntry indexEntry =
        new FullIndexEntry(new IndexEntryKey(question.getInstanceId(), "Question",
            question.getPK().getId()));
    indexEntry.setTitle(question.getTitle());
    indexEntry.setPreview(question.getContent());
    try {
      indexEntry.setCreationDate(DateUtil.parse(question.getCreationDate()));
    } catch (ParseException e) {
      SilverLogger.getLogger(this).warn(e);
    }
    indexEntry.setCreationUser(question.getCreatorId());
    indexEntry.addTextContent(question.getContent());
    indexEntry.addTextContent(question.getTitle());
    for (Reply reply : replies) {
      indexEntry.addTextContent(reply.getTitle());
      indexEntry.addTextContent(reply.loadWysiwygContent());
    }
    IndexEngineProxy.addIndexEntry(indexEntry);
  }


  public void updateIndex(Question question, Collection<Reply> replies) {
    deleteIndex(question);
    createIndex(question, replies);
  }

  /**
   * Delete index of the specified question.
   * @implNote The index of the replies to the question isn't deleted.
   * @param question a question
   */
  public void deleteIndex(Question question) {
    IndexEntryKey indexEntry =
        new IndexEntryKey(question.getInstanceId(), "Question", question.getPK().getId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }
}
