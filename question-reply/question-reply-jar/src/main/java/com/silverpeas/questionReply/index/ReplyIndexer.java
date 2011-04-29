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

import com.silverpeas.questionReply.model.Reply;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;

/**
 *
 * @author ehugonnet
 */
public class ReplyIndexer {

  public void createReplyIndex(Reply reply) {
    SilverTrace.info("questionReply", "QuestionManager.createReplyIndex()",
        "root.MSG_GEN_ENTER_METHOD", "Reply = " + reply.getTitle());
    if (reply != null) {
      FullIndexEntry indexEntry = new FullIndexEntry(reply.getPK().getInstanceId(), "Reply", reply.
          getPK().getId());
      indexEntry.setTitle(reply.getTitle());
      indexEntry.addTextContent(reply.getTitle());
      indexEntry.setPreView(reply.getContent());
      indexEntry.addTextContent(reply.getContent());
      indexEntry.setCreationDate(reply.getCreationDate());
      indexEntry.setCreationUser(reply.getCreatorId());
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  public void deleteReplyIndex(Reply reply) {
    SilverTrace.info("questionReply", "QuestionManager.deleteReplyIndex()",
        "root.MSG_GEN_ENTER_METHOD", "Reply = " + reply.toString());
    IndexEntryPK indexEntry = new IndexEntryPK(reply.getPK().getInstanceId(), "Reply", reply.getPK().
        getId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  public ReplyIndexer() {
  }
}
