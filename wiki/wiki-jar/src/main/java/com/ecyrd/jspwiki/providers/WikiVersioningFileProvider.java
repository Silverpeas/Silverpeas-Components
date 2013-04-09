/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.ecyrd.jspwiki.providers;

import com.ecyrd.jspwiki.*;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.wiki.control.WikiException;
import com.silverpeas.wiki.control.WikiMultiInstanceManager;
import com.silverpeas.wiki.control.WikiPageDAO;
import com.silverpeas.wiki.control.model.PageDetail;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import org.apache.commons.io.IOUtils;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;


/**
 * @author Ludovic Bertin
 */
public class WikiVersioningFileProvider extends AbstractFileProvider implements VersioningProvider {

  public static final String PAGEDIR = "OLD";
  public static final String PROPERTYFILE = "page.properties";
  public static final String ID = "ID";
  private CachedProperties m_cachedProperties;
  private WikiPageDAO pageDAO = new WikiPageDAO();

  @Override
  public void initialize(WikiEngine engine, Properties properties) throws
      NoRequiredPropertyException, IOException {
    super.initialize(engine, properties);
  }

  /**
   * Returns the directory where the old versions of the pages are being kept.
   */
  private File findOldPageDir(String page) {
    if (page == null) {
      throw new InternalWikiException("Page may NOT be null in the provider!");
    }

    File oldpages = new File(getPageDirectory(), PAGEDIR);

    return new File(oldpages, mangleName(page));
  }

  /**
   * Goes through the repository and decides which version is the newest one in that directory.
   *
   * @return Latest version number in the repository, or -1, if there is no page in the repository.
   */
  private int findLatestVersion(String page) throws ProviderException {
    int version = -1;

    try {
      Properties props = getPageProperties(page);

      for (Iterator i = props.keySet().iterator(); i.hasNext();) {
        String key = (String) i.next();
        if (key.endsWith(".author")) {
          int cutpoint = key.indexOf('.');
          if (cutpoint > 0) {
            String pageNum = key.substring(0, cutpoint);

            try {
              int res = Integer.parseInt(pageNum);
              if (res > version) {
                version = res;
              }
            } catch (NumberFormatException e) {
            } // It's okay to skip these.
          }
        }
      }
    } catch (IOException e) {
      SilverTrace.error("wiki", "WikiVersioningFileProvider.findLatestVersion()",
          "wiki.EX_FIND_PAGE", e);
    }
    return version;
  }

  /**
   * Reads page properties from the file system.
   */
  private Properties getPageProperties(String page) throws IOException {
    File propertyFile = new File(findOldPageDir(page), PROPERTYFILE);

    if (propertyFile.exists()) {
      long lastModified = propertyFile.lastModified();

      //
      // The profiler showed that when calling the history of a page the
      // propertyfile
      // was read just as much times as there were versions of that file.
      // The loading
      // of a propertyfile is a cpu-intensive jobs. So now hold on to the
      // last propertyfile
      // read because the next method will with a high probability ask for
      // the same propertyfile.
      // The time it took to show a historypage with 267 versions dropped
      // with 300%.
      //

      CachedProperties cp = m_cachedProperties;

      if (cp != null && cp.m_page.equals(page)
          && cp.m_lastModified == lastModified) {
        return cp.m_props;
      }

      InputStream in = null;

      try {
        in = new BufferedInputStream(new FileInputStream(propertyFile));

        Properties props = new Properties();

        props.load(in);

        cp = new CachedProperties();
        cp.m_page = page;
        cp.m_lastModified = lastModified;
        cp.m_props = props;

        m_cachedProperties = cp; // Atomic

        return props;
      } finally {
        if (in != null) {
          in.close();
        }
      }
    }

    return new Properties(); // Returns an empty object
  }

  /**
   * Writes the page properties back to the file system. Note that it WILL overwrite any previous
   * properties.
   */
  private void putPageProperties(String page, Properties properties)
      throws IOException {
    File propertyFile = new File(findOldPageDir(page), PROPERTYFILE);
    OutputStream out = null;

    try {
      out = new FileOutputStream(propertyFile);

      properties.store(out, " JSPWiki page properties for " + page
          + ". DO NOT MODIFY!");
    } finally {
      if (out != null) {
        out.close();
      }
    }
  }

  /**
   * Figures out the real version number of the page and also checks for its existence.
   *
   * @throws NoSuchVersionException if there is no such version.
   */
  private int realVersion(String page, int requestedVersion)
      throws NoSuchVersionException, ProviderException {
    //
    // Quickly check for the most common case.
    //
    if (requestedVersion == WikiProvider.LATEST_VERSION) {
      return -1;
    }

    int latest = findLatestVersion(page);

    if (requestedVersion == latest || (requestedVersion == 1 && latest == -1)) {
      return -1;
    } else if (requestedVersion <= 0 || requestedVersion > latest) {
      throw new NoSuchVersionException("Requested version " + requestedVersion
          + ", but latest is " + latest);
    }

    return requestedVersion;
  }

  public synchronized String getPageText(String page, int version)
      throws ProviderException {
    File dir = findOldPageDir(page);

    version = realVersion(page, version);
    if (version == -1) {
      // We can let the FileSystemProvider take care
      // of these requests.
      return super.getPageText(page, WikiPageProvider.LATEST_VERSION);
    }

    File pageFile = new File(dir, "" + version + FILE_EXT);

    if (!pageFile.exists()) {
      throw new NoSuchVersionException("Version " + version + "does not exist.");
    }

    return readFile(pageFile);
  }

  // FIXME: Should this really be here?
  private String readFile(File pagedata) throws ProviderException {
    String result = null;
    InputStream in = null;

    if (pagedata.exists()) {
      if (pagedata.canRead()) {
        try {
          in = new FileInputStream(pagedata);
          result = FileUtil.readContents(in, m_encoding);
        } catch (IOException e) {
          SilverTrace.error("wiki", "WikiVersioningFileProvider.readFile()",
              "wiki.EX_FAIL_READ", e);

          throw new ProviderException("I/O error: " + e.getMessage());
        } finally {
          try {
            if (in != null) {
              in.close();
            }
          } catch (Exception e) {
            SilverTrace.fatal("wiki", "WikiVersioningFileProvider.readFile()",
                "wiki.EX_CLOSING_FAIL", e);
          }
        }
      } else {
        SilverTrace.warn("wiki", "WikiVersioningFileProvider.readFile()",
            "wiki.EX_FAIL_READ", "Failed to read page from '"
            + pagedata.getAbsolutePath()
            + "', possibly a permissions problem");
        throw new ProviderException("I cannot read the requested page.");
      }
    } else {
      // This is okay.
      // FIXME: is it?
      SilverTrace.info("wiki", "WikiVersioningFileProvider.readFile()",
          "root.INFO", "New page");
    }

    return result;
  }

  // FIXME: This method has no rollback whatsoever.

  /*
   * This is how the page directory should look like: version pagedir olddir none empty empty 1
   * Main.txt (1) empty 2 Main.txt (2) 1.txt 3 Main.txt (3) 1.txt, 2.txt
   */
  public synchronized void putPageText(WikiPage page, String text)
      throws ProviderException {
    int pageId = -1;
    //
    // This is a bit complicated. We'll first need to
    // copy the old file to be the newest file.
    //
    File pageDir = findOldPageDir(page.getName());

    try {
      if (!pageDir.exists()) {
        pageId = pageDAO.createPage(new PageDetail(-1, page.getName(),
            WikiMultiInstanceManager.getComponentId()));
        pageDir.mkdirs();
      } else {
        PageDetail pageDetail = pageDAO.getPage(page.getName(),
            WikiMultiInstanceManager.getComponentId());
        pageId = pageDetail.getId();
      }
    } catch (WikiException e) {
      SilverTrace.error("wiki", "WikiVersioningFileProvider.putPageText()",
          "root.EX_PUT_PAGE_FAILED", e);
      throw new ProviderException("Could not save page text: " + e.getMessage());
    }

    int latest = findLatestVersion(page.getName());

    try {
      //
      // Copy old data to safety, if one exists.
      //
      File oldFile = findPage(page.getName());

      // Figure out which version should the old page be?
      // Numbers should always start at 1.
      // "most recent" = -1 ==> 1
      // "first" = 1 ==> 2

      int versionNumber = (latest > 0) ? latest : 1;

      if (oldFile != null && oldFile.exists()) {
        InputStream in = null;
        OutputStream out = null;

        try {
          in = new BufferedInputStream(new FileInputStream(oldFile));
          File pageFile = new File(pageDir, Integer.toString(versionNumber)
              + FILE_EXT);
          out = new BufferedOutputStream(new FileOutputStream(pageFile));

          FileUtil.copyContents(in, out);

          //
          // We need also to set the date, since we rely on this.
          //
          pageFile.setLastModified(oldFile.lastModified());

          //
          // Kludge to make the property code to work properly.
          //
          versionNumber++;
        } finally {
          if (out != null) {
            out.close();
          }
          if (in != null) {
            in.close();
          }
        }
      }

      //
      // Let superclass handler writing data to a new version.
      //

      super.putPageText(page, text);

      //
      // Finally, write page version data.
      //

      // FIXME: No rollback available.
      Properties props = getPageProperties(page.getName());

      props.setProperty(versionNumber + ".author",
          (page.getAuthor() != null) ? page.getAuthor() : "unknown");
      props.setProperty(versionNumber + ".id", String.valueOf(pageId));

      String changeNote = (String) page.getAttribute(WikiPage.CHANGENOTE);
      if (changeNote != null) {
        props.setProperty(versionNumber + ".changenote", changeNote);
      }

      putPageProperties(page.getName(), props);
    } catch (IOException e) {
      SilverTrace.error("wiki", "WikiVersioningFileProvider.putPageText()",
          "root.EX_PUT_PAGE_FAILED", e);
      throw new ProviderException("Could not save page text: " + e.getMessage());
    }
  }

  public WikiPage getPageInfo(String page, int version)
      throws ProviderException {
    int latest = findLatestVersion(page);
    int realVersion;

    WikiPage p = null;

    if (version == WikiPageProvider.LATEST_VERSION || version == latest
        || (version == 1 && latest == -1)) {
      //
      // Yes, we need to talk to the top level directory
      // to get this version.
      //
      // I am listening to Press Play On Tape's guitar version of
      // the good old C64 "Wizardry" -tune at this moment.
      // Oh, the memories...
      //
      realVersion = (latest >= 0) ? latest : 1;

      p = super.getPageInfo(page, WikiPageProvider.LATEST_VERSION);

      if (p != null) {
        p.setVersion(realVersion);
      }
    } else {
      //
      // The file is not the most recent, so we'll need to
      // find it from the deep trenches of the "OLD" directory
      // structure.
      //
      realVersion = version;
      File dir = findOldPageDir(page);

      if (!dir.exists() || !dir.isDirectory()) {
        return null;
      }

      File file = new File(dir, version + FILE_EXT);

      if (file.exists()) {
        p = new WikiPage(m_engine, page);

        p.setLastModified(new Date(file.lastModified()));
        p.setVersion(version);
      }
    }

    //
    // Get author and other metadata information
    // (Modification date has already been set.)
    //
    if (p != null) {
      try {
        Properties props = getPageProperties(page);
        String author = props.getProperty(realVersion + ".author");
        if (author != null) {
          p.setAuthor(author);
        }

        String changenote = props.getProperty(realVersion + ".changenote");
        if (changenote != null) {
          p.setAttribute(WikiPage.CHANGENOTE, changenote);
        }

        String pageId = props.getProperty(realVersion + ".id");
        p.setAttribute(WikiVersioningFileProvider.ID, pageId);

      } catch (IOException e) {
        SilverTrace.error("wiki", "WikiVersioningFileProvider.getPageInfo()",
            "root.EX_GET_PAGE_INFO", e);
      }
    }

    return p;
  }

  public boolean pageExists(String pageName, int version) {
    File dir = findOldPageDir(pageName);
    if (!dir.exists() || !dir.isDirectory()) {
      return false;
    }
    File file = new File(dir, version + FILE_EXT);
    if (file.exists()) {
      return true;
    }
    return false;
  }

  /**
   * FIXME: Does not get user information.
   */
  public List<?> getVersionHistory(String page) throws ProviderException {
    List<WikiPage> list = new ArrayList<WikiPage>();
    int latest = findLatestVersion(page);
    for (int i = latest; i > 0; i--) {
      WikiPage info = getPageInfo(page, i);
      if (info != null) {
        list.add(info);
      }
    }
    return list;
  }

  /**
   * Removes the relevant page directory under "OLD" -directory as well, but does not remove any
   * extra subdirectories from it. It will only touch those files that it thinks to be WikiPages.
   *
   * @param page
   * @throws ProviderException
   */
  // FIXME: Should log errors.
  @Override
  public void deletePage(String page) throws ProviderException {
    super.deletePage(page);
    int pageId = -1;
    String componentId = WikiMultiInstanceManager.getComponentId();
    try {
      PageDetail currentPage = pageDAO.getPage(page, componentId);
      if (currentPage != null) {
        pageId = currentPage.getId();
        pageDAO.deletePage(page, WikiMultiInstanceManager.getComponentId());
      }
    } catch (WikiException e) {
      SilverTrace.error("wiki", "WikiVersioningFileProvider.deletePage()",
          "wiki.EX_DELETE_PAGE_FAILED", e);
      throw new ProviderException("Could not delete page : " + e.getMessage());
    }

    /*
     * Delete versioning information relative to the deleted page
     */
    try {
      List<SimpleDocument> docs = AttachmentServiceFactory.getAttachmentService().
          listDocumentsByForeignKey(new ForeignPK(String.valueOf(pageId), componentId), null);
      for (SimpleDocument doc : docs) {
        AttachmentServiceFactory.getAttachmentService().deleteAttachment(doc);
      }
    } catch (Exception e) {
      SilverTrace.error("wiki", "WikiVersioningFileProvider.deletePage()",
          "wiki.EX_DELETE_PAGE_FAILED", e);
      throw new ProviderException("Could not delete page : " + e.getMessage());
    }

    File dir = findOldPageDir(page);

    if (dir.exists() && dir.isDirectory()) {
      File[] files = dir.listFiles(new WikiFileFilter());
      for (File file : files) {
        file.delete();
      }
      File propfile = new File(dir, PROPERTYFILE);
      if (propfile.exists()) {
        propfile.delete();
      }
      dir.delete();
    }
  }

  @Override
  public void deleteVersion(String page, int version) throws ProviderException {
    File dir = findOldPageDir(page);
    int latest = findLatestVersion(page);

    if (version == WikiPageProvider.LATEST_VERSION || version == latest
        || (version == 1 && latest == -1)) {      //
      // Delete the properties
      //
      try {
        Properties props = getPageProperties(page);
        props.remove(((latest > 0) ? latest : 1) + ".author");
        putPageProperties(page, props);
      } catch (IOException e) {
        SilverTrace.error("wiki", "WikiVersioningFileProvider.deleteVersion()",
            "wiki.EX_MODIFY_PROPERTIES", e);
        throw new ProviderException("Could not modify page properties");
      }
      // We can let the FileSystemProvider take care
      // of the actual deletion
      super.deleteVersion(page, WikiPageProvider.LATEST_VERSION);

      //
      // Copy the old file to the new location
      //
      latest = findLatestVersion(page);

      File pageDir = findOldPageDir(page);
      File previousFile = new File(pageDir, Integer.toString(latest) + FILE_EXT);

      InputStream in = null;
      OutputStream out = null;

      try {
        if (previousFile.exists()) {
          in = new BufferedInputStream(new FileInputStream(previousFile));
          File pageFile = findPage(page);
          out = new BufferedOutputStream(new FileOutputStream(pageFile));

          FileUtil.copyContents(in, out);

          //
          // We need also to set the date, since we rely on this.
          //
          pageFile.setLastModified(previousFile.lastModified());
        }
      } catch (IOException e) {
        SilverTrace.fatal("wiki", "WikiVersioningFileProvider.deleteVersion()", "root.EXCEPTION",
            "Something wrong with the page directory - you may have just lost data!", e);
      } finally {
        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);
      }

      return;
    }

    File pageFile = new File(dir, "" + version + FILE_EXT);
    if (pageFile.exists()) {
      if (!pageFile.delete()) {
        SilverTrace.error("wiki", "WikiVersioningFileProvider.deleteVersion()",
            "wiki.EX_DELETE_PAGE_FAILED");
      }
    } else {
      throw new NoSuchVersionException("Page " + page + ", version=" + version);
    }
  }

  // FIXME: This is kinda slow, we should need to do this only once.
  @Override
  public Collection<?> getAllPages() throws ProviderException {
    Collection<?> pages = internalGetAllPages();
    Collection<WikiPage> returnedPages = new ArrayList<WikiPage>();

    for (Iterator i = pages.iterator(); i.hasNext();) {
      WikiPage page = (WikiPage) i.next();
      WikiPage info = getPageInfo(page.getName(), WikiProvider.LATEST_VERSION);
      returnedPages.add(info);
    }

    return returnedPages;
  }

  @Override
  public String getProviderInfo() {
    return "";
  }

  @Override
  public void movePage(String from, String to) throws ProviderException {
    // Rename in database
    try {
      pageDAO.renamePage(from, to, WikiMultiInstanceManager.getComponentId());
    } catch (WikiException e) {
      SilverTrace.error("wiki", "WikiVersioningFileProvider.movePage()", "wiki.EX_MOVE_PAGE", e);
      throw new ProviderException("Could not move page");
    }

    // Move the file itself
    File fromFile = findPage(from);
    File toFile = findPage(to);
    fromFile.renameTo(toFile);
    // Move any old versions
    File fromOldDir = findOldPageDir(from);
    File toOldDir = findOldPageDir(to);
    fromOldDir.renameTo(toOldDir);
  }

  private static class CachedProperties {

    String m_page;
    Properties m_props;
    long m_lastModified;
  }

  @Override
  String getPageDirectory() {
    String componentId = WikiMultiInstanceManager.getComponentId();
    if (componentId == null) {
      return super.getPageDirectory();
    } else {
      return FileRepositoryManager.getAbsolutePath(componentId);
    }
  }

  /**
   * Finds a Wiki page from the page repository.
   *
   * @param page
   * @return
   */
  @Override
  protected File findPage(String page) {
    return new File(getPageDirectory(), mangleName(page) + FILE_EXT);
  }

  public Collection<?> internalGetAllPages() throws ProviderException {
    List<WikiPage> set = new ArrayList<WikiPage>();
    File wikipagedir = new File(getPageDirectory());
    File[] wikipages = wikipagedir.listFiles(new WikiFileFilter());
    if (wikipages == null) {
      SilverTrace.error("wiki", "WikiVersioningFileProvider.internalGetAllPages()",
          "wiki.GET_ALL_PAGES_FAILED", "Wikipages directory '" + getPageDirectory()
          + "' does not exist! Please check " + PROP_PAGEDIR + " in jspwiki.properties.");
      throw new InternalWikiException("Page directory does not exist");
    }
    for (int i = 0; i < wikipages.length; i++) {
      String wikiname = wikipages[i].getName();
      int cutpoint = wikiname.lastIndexOf(FILE_EXT);

      WikiPage page = getPageInfo(
          unmangleName(wikiname.substring(0, cutpoint)),
          WikiPageProvider.LATEST_VERSION);
      if (page == null) {
        // This should not really happen.
        // FIXME: Should we throw an exception here?
        SilverTrace.error("wiki", "WikiVersioningFileProvider.internalGetAllPages()",
            "wiki.GET_ALL_PAGES_FAILED", "Page " + wikiname
            + " was found in directory listing, but could not be located individually.");
        continue;
      }
      set.add(page);
    }
    return set;
  }

  @Override
  public int getPageCount() {
    File wikipagedir = new File(getPageDirectory());
    File[] wikipages = wikipagedir.listFiles(new WikiFileFilter());
    return wikipages.length;
  }

  /**
   * Iterates through all WikiPages, matches them against the given query, and returns a Collection
   * of SearchResult objects.
   * @param query
   * @return
   */
  @Override
  public Collection findPages(QueryItem[] query) {
    File wikipagedir = new File(getPageDirectory());
    TreeSet res = new TreeSet(new SearchResultComparator());
    SearchMatcher matcher = new SearchMatcher(m_engine, query);

    File[] wikipages = wikipagedir.listFiles(new WikiFileFilter());

    for (int i = 0; i < wikipages.length; i++) {
      FileInputStream input = null;
      String filename = wikipages[i].getName();
      int cutpoint = filename.lastIndexOf(FILE_EXT);
      String wikiname = filename.substring(0, cutpoint);

      wikiname = unmangleName(wikiname);

      try {
        input = new FileInputStream(wikipages[i]);
        String pagetext = FileUtil.readContents(input, m_encoding);
        SearchResult comparison = matcher.matchPageContent(wikiname, pagetext);
        if (comparison != null) {
          res.add(comparison);
        }
      } catch (IOException e) {
        SilverTrace.error("wiki", "WikiVersioningFileProvider.findPages()",
            "wiki.EX_FIND_PAGES_FAILED", "Failed to read " + filename, e);
      } finally {
        try {
          if (input != null) {
            input.close();
          }
        } catch (IOException e) {
        } // It's fine to fail silently.
      }
    }
    return res;
  }
}
