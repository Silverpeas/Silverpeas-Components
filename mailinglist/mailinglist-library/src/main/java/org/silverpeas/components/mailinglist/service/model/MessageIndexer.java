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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.mailinglist.service.model;

import org.silverpeas.components.mailinglist.service.model.beans.Attachment;
import org.silverpeas.components.mailinglist.service.model.beans.Message;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;

import java.util.Set;

public class MessageIndexer {

  public static void indexMessage(Message message) {
    if (message.isModerated()) {
      unindexMessage(message);
      FullIndexEntry index =
          new FullIndexEntry(message.getComponentId(), "message", message.getId());
      index.setTitle(message.getTitle());
      index.setCreationDate(message.getSentDate());
      index.setPreview(message.getSummary());
      index.addTextContent(message.getTitle());
      index.addTextContent(message.getSender());
      index.addTextContent(message.getBody());
      Set<Attachment> attachments = message.getAttachments();
      if (attachments != null && !attachments.isEmpty()) {
        for (Attachment attachment : attachments) {
          index.addFileContent(attachment.getPath(), "UTF-8", attachment.getContentType(), "fr");
        }
      }
      IndexEngineProxy.addIndexEntry(index);
    }
  }

  public static void unindexMessage(Message message) {
    IndexEntryKey indexEntry =
        new IndexEntryKey(message.getComponentId(), "message", message.getId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  private MessageIndexer() {
  }
}
