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
        for(Attachment attachment : attachments) {
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
