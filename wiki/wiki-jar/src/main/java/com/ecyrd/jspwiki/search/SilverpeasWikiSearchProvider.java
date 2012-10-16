/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.ecyrd.jspwiki.search;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import com.ecyrd.jspwiki.NoRequiredPropertyException;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiPage;
import com.ecyrd.jspwiki.attachment.Attachment;
import com.ecyrd.jspwiki.providers.ProviderException;
import com.ecyrd.jspwiki.providers.WikiAttachmentProvider;
import com.ecyrd.jspwiki.providers.WikiBasicAttachmentProvider;
import com.silverpeas.wiki.control.WikiMultiInstanceManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;
import org.silverpeas.search.indexEngine.model.IndexEngineProxy;
import org.silverpeas.search.indexEngine.model.IndexEntryPK;

public class SilverpeasWikiSearchProvider implements SearchProvider {

  private WikiEngine wikiEngine;

  @Override
  public Collection<?> findPages(String arg0) throws ProviderException,
      IOException {
    return null;
  }

  @Override
  public void pageRemoved(WikiPage page) {
    IndexEntryPK indexEntry = new IndexEntryPK(WikiMultiInstanceManager
        .getComponentId(), "Publication", page.getName());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  @Override
  public void reindexPage(WikiPage page) {
    SilverTrace.info("wiki", "SilverpeasWikiSearchProvider.reindexPage()",
        "root.MSG_GEN_ENTER_METHOD", "page=" + page.getName());

    if (page != null) {
      FullIndexEntry indexEntry = null;
      // Index the Composed Task
      indexEntry = new FullIndexEntry(WikiMultiInstanceManager.getComponentId(), "Publication",
          page.getName());
      indexEntry.setTitle(page.getName());
      indexEntry.setPreView("");

      if (page instanceof Attachment) {
        WikiAttachmentProvider attMgr = wikiEngine.getAttachmentManager().getCurrentProvider();
        if (!(attMgr instanceof WikiBasicAttachmentProvider)) {
          SilverTrace.warn("wiki", "SilverpeasWikiSearchProvider.reindexPage()", "BAD_PROVIDER",
              "SilverpeasWikiSearchProvider implementation works only with WikiBasicAttachmentProvider");
          return;
        }

        WikiBasicAttachmentProvider wikiAttMgr = (WikiBasicAttachmentProvider) attMgr;
        try {
          indexEntry.addFileContent(wikiAttMgr.getAttachmentFilePath((Attachment) page), null,
              null, null);
        } catch (Exception e) {
          SilverTrace.warn("wiki",
              "SilverpeasWikiSearchProvider.reindexPage()",
              "EX_ADD_FILE_CONTENT", e);
        }
      } else {
        String text = wikiEngine.getPureText(page);
        indexEntry.addTextContent(text);
      }

      IndexEngineProxy.addIndexEntry(indexEntry);
    }

  }

  @Override
  public String getProviderInfo() {
    return "Indexeur de Silverpeas (basé sur Lucene)";
  }

  @Override
  public void initialize(WikiEngine engine, Properties props)
      throws NoRequiredPropertyException, IOException {
    this.wikiEngine = engine;

  }

}