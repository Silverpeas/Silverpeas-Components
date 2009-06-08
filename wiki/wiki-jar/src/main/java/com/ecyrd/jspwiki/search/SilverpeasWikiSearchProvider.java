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
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;

public class SilverpeasWikiSearchProvider implements SearchProvider {

	private WikiEngine wikiEngine;

	public Collection<?> findPages(String arg0) throws ProviderException,
			IOException {
		return null;
	}

	public void pageRemoved(WikiPage page) {
		IndexEntryPK indexEntry = new IndexEntryPK(WikiMultiInstanceManager
				.getComponentId(), "Publication", page.getName());
		IndexEngineProxy.removeIndexEntry(indexEntry);
	}

	public void reindexPage(WikiPage page) {
		SilverTrace.info("wiki", "SilverpeasWikiSearchProvider.reindexPage()",
				"root.MSG_GEN_ENTER_METHOD", "page=" + page.getName());

		if (page != null) {
			FullIndexEntry indexEntry = null;
			// Index the Composed Task
			indexEntry = new FullIndexEntry(WikiMultiInstanceManager
					.getComponentId(), "Publication", page.getName());
			indexEntry.setTitle(page.getName());
			indexEntry.setPreView("");

			if (page instanceof Attachment) {
				WikiAttachmentProvider attMgr = wikiEngine
						.getAttachmentManager().getCurrentProvider();
				if (!(attMgr instanceof WikiBasicAttachmentProvider)) {
					SilverTrace
							.warn(
									"wiki",
									"SilverpeasWikiSearchProvider.reindexPage()",
									"BAD_PROVIDER",
									"SilverpeasWikiSearchProvider implementation works only with WikiBasicAttachmentProvider");
					return;
				}

				WikiBasicAttachmentProvider wikiAttMgr = (WikiBasicAttachmentProvider) attMgr;

				try {
					indexEntry.addFileContent(wikiAttMgr
							.getAttachmentFilePath((Attachment) page), null,
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

	public String getProviderInfo() {
		return "Indexeur de Silverpeas (basé sur Lucene)";
	}

	public void initialize(WikiEngine engine, Properties props)
			throws NoRequiredPropertyException, IOException {
		this.wikiEngine = engine;

	}

}
