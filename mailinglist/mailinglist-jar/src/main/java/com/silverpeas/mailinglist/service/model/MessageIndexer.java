/**
 * Copyright (C) 2000 - 2009 Silverpeas
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

package com.silverpeas.mailinglist.service.model;

import java.util.Set;

import com.silverpeas.mailinglist.service.model.beans.Attachment;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;

public class MessageIndexer {

  public static void indexMessage(Message message) {
    if (message.isModerated()) {
      unindexMessage(message);
      FullIndexEntry index = new FullIndexEntry(message.getComponentId(),
          "message", message.getId());
      index.setTitle(message.getTitle());
      index.setCreationDate(message.getSentDate());
      index.setPreView(message.getSummary());
      index.addTextContent(message.getTitle());
      index.addTextContent(message.getSender());
      index.addTextContent(message.getBody());
      Set<Attachment> attachments = message.getAttachments();
      if (attachments != null && !attachments.isEmpty()) {
        for (Attachment attachment : attachments) {
          index.addFileContent(attachment.getPath(), "ISO-8859-1", attachment
              .getContentType(), "fr");
        }
      }
      IndexEngineProxy.addIndexEntry(index);
    }
  }

  public static void unindexMessage(Message message) {
    IndexEntryPK indexEntry = new IndexEntryPK(message.getComponentId(),
        "message", message.getId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }
}
