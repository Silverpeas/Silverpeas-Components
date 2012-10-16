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
package com.ecyrd.jspwiki.providers;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.ecyrd.jspwiki.NoRequiredPropertyException;
import com.ecyrd.jspwiki.QueryItem;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiPage;
import com.silverpeas.wiki.control.WikiException;
import com.silverpeas.wiki.control.WikiMultiInstanceManager;
import com.silverpeas.wiki.control.WikiPageDAO;
import com.silverpeas.wiki.control.model.PageDetail;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;

public class SilverpeasVersioningFileProvider implements WikiPageProvider, VersioningProvider {

  private Properties props;

  private WikiEngine engine;

  private WikiPageDAO pageDAO;

  private Map<String, VersioningFileProvider> providers;

  /**
   * {@inheritDoc}
   */
  @Override
  public void initialize(WikiEngine engine, Properties properties)
      throws NoRequiredPropertyException, IOException {
    this.props = new Properties(properties);
    this.engine = engine;
    this.providers = new HashMap<String, VersioningFileProvider>();
    this.pageDAO = new WikiPageDAO();
  }

  @Override
  public boolean pageExists(String page, int version) {
    try {
      return this.getRealProvider().pageExists(page, version);
    } catch (ProviderException e) {
      return false;
    }
  }

  @Override
  public void movePage(String from, String to) throws ProviderException {
    try {
      pageDAO.renamePage(from, to, WikiMultiInstanceManager.getComponentId());
    } catch (WikiException e) {
      SilverTrace.error("wiki", "WikiVersioningFileProvider.movePage()",
          "wiki.EX_MOVE_PAGE", e);
      throw new ProviderException("Could not move page");
    }
    this.getRealProvider().movePage(from, to);

  }

  @Override
  public void deletePage(String pageName) throws ProviderException {
    try {
      PageDetail currentPage = pageDAO.getPage(pageName,
          WikiMultiInstanceManager.getComponentId());
      if (currentPage != null) {
        pageDAO.deletePage(pageName, WikiMultiInstanceManager.getComponentId());
      }
    } catch (WikiException e) {
      SilverTrace.error("wiki", "WikiVersioningFileProvider.deletePage()",
          "wiki.EX_DELETE_PAGE_FAILED", e);
      throw new ProviderException("Could not delete page : " + e.getMessage());
    }
    this.getRealProvider().deletePage(pageName);
  }

  @Override
  public void deleteVersion(String pageName, int version)
      throws ProviderException {
    this.getRealProvider().deleteVersion(pageName, version);
  }

  @Override
  public Collection findPages(QueryItem[] query) {
    try {
      return this.getRealProvider().findPages(query);
    } catch (ProviderException e) {
      return null;
    }
  }

  @Override
  public Collection getAllChangedSince(Date date) {
    try {
      return this.getRealProvider().getAllChangedSince(date);
    } catch (ProviderException e) {
      return null;
    }
  }

  @Override
  public Collection getAllPages() throws ProviderException {
    return this.getRealProvider().getAllPages();
  }

  @Override
  public int getPageCount() throws ProviderException {
    return this.getRealProvider().getPageCount();
  }

  @Override
  public WikiPage getPageInfo(String page, int version)
      throws ProviderException {
    return this.getRealProvider().getPageInfo(page, version);
  }

  @Override
  public String getPageText(String page, int version) throws ProviderException {
    return this.getRealProvider().getPageText(page, version);
  }

  @Override
  public List getVersionHistory(String page) throws ProviderException {
    return this.getRealProvider().getVersionHistory(page);
  }

  @Override
  public boolean pageExists(String page) {
    try {
      return this.getRealProvider().pageExists(page);
    } catch (ProviderException e) {
      return false;
    }
  }

  @Override
  public void putPageText(WikiPage page, String text) throws ProviderException {
    boolean isNewPage = !pageExists(page.getName());
    this.getRealProvider().putPageText(page, text);
    try {
      int pageId = -1;
      if (isNewPage) {
        pageId = pageDAO.createPage(new PageDetail(-1, page.getName(),
            WikiMultiInstanceManager.getComponentId()));
      } else {
        PageDetail pageDetail = pageDAO.getPage(page.getName(),
            WikiMultiInstanceManager.getComponentId());
        if(pageDetail != null ) { //We are in the sandbox
           pageId = pageDetail.getId();
        }       
      }
    } catch (WikiException e) {
      SilverTrace.error("wiki", "WikiVersioningFileProvider.putPageText()",
          "root.EX_PUT_PAGE_FAILED", e);
      throw new ProviderException("Could not save page text: " + e.getMessage());
    }
  }

  @Override
  public String getProviderInfo() {
    return "Silverpeas";
  }

  String getPageDirectory() {
    String componentId = WikiMultiInstanceManager.getComponentId();
    return FileRepositoryManager.getAbsolutePath(componentId);
  }

  VersioningFileProvider getRealProvider() throws ProviderException {
    String componentId = WikiMultiInstanceManager.getComponentId();
    String path = FileRepositoryManager.getAbsolutePath(componentId);
    if (providers.containsKey(componentId)) {
      return providers.get(componentId);
    }
    Properties properties = new Properties(props);
    properties.setProperty(VersioningFileProvider.PROP_PAGEDIR, path);
    VersioningFileProvider realProvider = new VersioningFileProvider();
    try {
      realProvider.initialize(engine, properties);
    } catch (NoRequiredPropertyException e) {
      e.printStackTrace();
      SilverTrace.error("wiki", "WikiVersioningFileProvider.getRealProvider()",
          "root.EX_PUT_PAGE_FAILED", e);
      throw new ProviderException("Could not instantiate provider: " + e.getMessage());
    } catch (IOException e) {
      e.printStackTrace();
      SilverTrace.error("wiki", "WikiVersioningFileProvider.getRealProvider()",
          "root.EX_PUT_PAGE_FAILED", e);
      throw new ProviderException("Could not instantiate provider: " + e.getMessage());
    }
    synchronized (providers) {
      this.providers.put(componentId, realProvider);
    }
    return realProvider;

  }
}
