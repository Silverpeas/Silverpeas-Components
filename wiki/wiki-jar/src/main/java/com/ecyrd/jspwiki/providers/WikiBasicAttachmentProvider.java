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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ecyrd.jspwiki.FileUtil;
import com.ecyrd.jspwiki.NoRequiredPropertyException;
import com.ecyrd.jspwiki.PageTimeComparator;
import com.ecyrd.jspwiki.QueryItem;
import com.ecyrd.jspwiki.TextUtil;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiPage;
import com.ecyrd.jspwiki.WikiProvider;
import com.ecyrd.jspwiki.attachment.Attachment;
import com.silverpeas.wiki.control.WikiMultiInstanceManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;

/**
 * Provides basic, versioning attachments.
 * 
 * <PRE>
 *   Structure is as follows:
 *      attachment_dir/
 *         ThisPage/
 *            attachment.doc/
 *               attachment.properties
 *               1.doc
 *               2.doc
 *               3.doc
 *            picture.png/
 *               attachment.properties
 *               1.png
 *               2.png
 *         ThatPage/
 *            picture.png/
 *               attachment.properties
 *               1.png
 * 
 * </PRE>
 * 
 * The names of the directories will be URLencoded.
 * <p>
 * "attachment.properties" consists of the following items:
 * <UL>
 * <LI>1.author = author name for version 1 (etc)
 * </UL>
 */
public class WikiBasicAttachmentProvider implements WikiAttachmentProvider {
  private WikiEngine m_engine;

  private String m_storageDir;

  public static final String PROP_STORAGEDIR = "jspwiki.basicAttachmentProvider.storageDir";

  /*
   * Disable client cache for files with patterns since 2.5.96
   */
  private Pattern m_disableCache = null;

  public static final String PROP_DISABLECACHE = "jspwiki.basicAttachmentProvider.disableCache";

  public static final String PROPERTY_FILE = "attachment.properties";

  public static final String DIR_EXTENSION = "-att";

  public static final String ATTDIR_EXTENSION = "-dir";

  public void initialize(WikiEngine engine, Properties properties)
      throws NoRequiredPropertyException, IOException {
    m_engine = engine;
    m_storageDir = WikiEngine.getRequiredProperty(properties, PROP_STORAGEDIR);

    String patternString = engine.getWikiProperties().getProperty(
        PROP_DISABLECACHE);
    if (patternString != null) {
      m_disableCache = Pattern.compile(patternString);
    }

    //
    // Check if the directory exists - if it doesn't, create it.
    //
    File f = new File(m_storageDir);

    if (!f.exists()) {
      f.mkdirs();
    }

    //
    // Some sanity checks
    //
    if (!f.exists())
      throw new IOException(
          "Could not find or create attachment storage directory '"
          + m_storageDir + "'");

    if (!f.canWrite())
      throw new IOException(
          "Cannot write to the attachment storage directory '" + m_storageDir
          + "'");

    if (!f.isDirectory())
      throw new IOException(
          "Your attachment storage points to a file, not a directory: '"
          + m_storageDir + "'");
  }

  /**
   * Finds storage dir, and if it exists, makes sure that it is valid.
   * @param wikipage Page to which this attachment is attached.
   */
  private File findPageDir(String wikipage) throws ProviderException {
    wikipage = mangleName(wikipage);

    File f = new File(getStorageDir(), wikipage + DIR_EXTENSION);

    if (f.exists() && !f.isDirectory()) {
      throw new ProviderException("Storage dir '" + f.getAbsolutePath()
          + "' is not a directory!");
    }

    return f;
  }

  private static String mangleName(String wikiname) {
    String res = TextUtil.urlEncodeUTF8(wikiname);

    return res;
  }

  private static String unmangleName(String filename) {
    return TextUtil.urlDecodeUTF8(filename);
  }

  /**
   * Finds the dir in which the attachment lives.
   */
  private File findAttachmentDir(Attachment att) throws ProviderException {
    File f = new File(findPageDir(att.getParentName()), mangleName(att
        .getFileName() + ATTDIR_EXTENSION));
    if (!f.exists()) {
      File oldf = new File(findPageDir(att.getParentName()), mangleName(att
          .getFileName()));
      if (oldf.exists()) {
        f = oldf;
      } else {
        oldf = new File(findPageDir(att.getParentName()), att.getFileName());

        if (oldf.exists()) {
          f = oldf;
        }
      }
    }
    return f;
  }

  /**
   * Goes through the repository and decides which version is the newest one in that directory.
   * @return Latest version number in the repository, or 0, if there is no page in the repository.
   */
  private int findLatestVersion(Attachment att) throws ProviderException {
    // File pageDir = findPageDir( att.getName() );
    File attDir = findAttachmentDir(att);

    // log.debug("Finding pages in "+attDir.getAbsolutePath());
    String[] pages = attDir.list(new AttachmentVersionFilter());

    if (pages == null) {
      return 0; // No such thing found.
    }

    int version = 0;

    for (int i = 0; i < pages.length; i++) {
      // log.debug("Checking: "+pages[i]);
      int cutpoint = pages[i].indexOf('.');
      String pageNum = (cutpoint > 0) ? pages[i].substring(0, cutpoint)
          : pages[i];

      try {
        int res = Integer.parseInt(pageNum);

        if (res > version) {
          version = res;
        }
      } catch (NumberFormatException e) {
      } // It's okay to skip these.
    }

    return version;
  }

  /**
   * Returns the file extension. For example "test.png" returns "png".
   * <p>
   * If file has no extension, will return "bin"
   */
  protected static String getFileExtension(String filename) {
    String fileExt = "bin";

    int dot = filename.lastIndexOf('.');
    if (dot >= 0 && dot < filename.length() - 1) {
      fileExt = mangleName(filename.substring(dot + 1));
    }

    return fileExt;
  }

  /**
   * Writes the page properties back to the file system. Note that it WILL overwrite any previous
   * properties.
   */
  private void putPageProperties(Attachment att, Properties properties)
      throws IOException, ProviderException {
    File attDir = findAttachmentDir(att);
    File propertyFile = new File(attDir, PROPERTY_FILE);

    OutputStream out = new FileOutputStream(propertyFile);

    properties.store(out, " JSPWiki page properties for " + att.getName()
        + ". DO NOT MODIFY!");

    out.close();
  }

  /**
   * Reads page properties from the file system.
   */
  private Properties getPageProperties(Attachment att) throws IOException,
      ProviderException {
    Properties props = new Properties();

    File propertyFile = new File(findAttachmentDir(att), PROPERTY_FILE);

    if (propertyFile.exists()) {
      InputStream in = new FileInputStream(propertyFile);

      props.load(in);

      in.close();
    }

    return props;
  }

  public void putAttachmentData(Attachment att, InputStream data)
      throws ProviderException, IOException {
    OutputStream out = null;
    File attDir = findAttachmentDir(att);

    if (!attDir.exists()) {
      attDir.mkdirs();
    }

    int latestVersion = findLatestVersion(att);

    // System.out.println("Latest version is "+latestVersion);

    try {
      int versionNumber = latestVersion + 1;

      File newfile = new File(attDir, versionNumber + "."
          + getFileExtension(att.getFileName()));

      SilverTrace.info("wiki",
          "WikiVersioningFileProvider.putAttachmentData()", "root.INFO",
          "Uploading attachment " + att.getFileName() + " to page "
          + att.getParentName());
      SilverTrace.info("wiki",
          "WikiVersioningFileProvider.putAttachmentData()", "root.INFO",
          "Saving attachment contents to " + newfile.getAbsolutePath());
      out = new FileOutputStream(newfile);

      FileUtil.copyContents(data, out);

      out.close();

      Properties props = getPageProperties(att);

      String author = att.getAuthor();

      if (author == null) {
        author = "unknown";
      }

      props.setProperty(versionNumber + ".author", author);

      String changeNote = (String) att.getAttribute(WikiPage.CHANGENOTE);
      if (changeNote != null) {
        props.setProperty(versionNumber + ".changenote", changeNote);
      }

      putPageProperties(att, props);
    } catch (IOException e) {
      SilverTrace.error("wiki",
          "WikiVersioningFileProvider.putAttachmentData()",
          "wiki.EX_SAVE_ATTACHMENT", e);
      throw (IOException) e.fillInStackTrace();
    } finally {
      if (out != null)
        out.close();
    }
  }

  public String getProviderInfo() {
    return "";
  }

  private File findFile(File dir, Attachment att) throws FileNotFoundException,
      ProviderException {
    int version = att.getVersion();

    if (version == WikiProvider.LATEST_VERSION) {
      version = findLatestVersion(att);
    }

    String ext = getFileExtension(att.getFileName());
    File f = new File(dir, version + "." + ext);

    if (!f.exists()) {
      if ("bin".equals(ext)) {
        File fOld = new File(dir, version + ".");
        if (fOld.exists())
          f = fOld;
      }
      if (!f.exists()) {
        throw new FileNotFoundException("No such file: " + f.getAbsolutePath()
            + " exists.");
      }
    }

    return f;
  }

  public String getAttachmentFilePath(Attachment att) throws IOException,
      ProviderException {
    File attDir = findAttachmentDir(att);

    try {
      File f = findFile(attDir, att);

      return f.getAbsolutePath();
    } catch (FileNotFoundException e) {
      SilverTrace.error("wiki",
          "WikiVersioningFileProvider.getAttachmentFilePath()",
          "wiki.EX_GET_ATTACHMENT_FILEPATH", e);
      throw new ProviderException("No such page was found.");
    }

  }

  public InputStream getAttachmentData(Attachment att) throws IOException,
      ProviderException {
    File attDir = findAttachmentDir(att);

    try {
      File f = findFile(attDir, att);

      return new FileInputStream(f);
    } catch (FileNotFoundException e) {
      SilverTrace.error("wiki",
          "WikiVersioningFileProvider.getAttachmentData()",
          "wiki.EX_GET_ATTACHMENT", e);
      throw new ProviderException("No such page was found.");
    }
  }

  public Collection listAttachments(WikiPage page) throws ProviderException {
    Collection result = new ArrayList();

    File dir = findPageDir(page.getName());

    if (dir != null) {
      String[] attachments = dir.list();

      if (attachments != null) {
        //
        // We now have a list of all potential attachments in
        // the directory.
        //
        for (int i = 0; i < attachments.length; i++) {
          File f = new File(dir, attachments[i]);

          if (f.isDirectory()) {
            String attachmentName = unmangleName(attachments[i]);

            //
            // Is it a new-stylea attachment directory? If yes,
            // we'll just deduce the name. If not, however,
            // we'll check if there's a suitable property file
            // in the directory.
            //
            if (attachmentName.endsWith(ATTDIR_EXTENSION)) {
              attachmentName = attachmentName.substring(0, attachmentName
                  .length()
                  - ATTDIR_EXTENSION.length());
            } else {
              File propFile = new File(f, PROPERTY_FILE);

              if (!propFile.exists()) {
                //
                // This is not obviously a JSPWiki attachment,
                // so let's just skip it.
                //
                continue;
              }
            }

            Attachment att = getAttachmentInfo(page, attachmentName,
                WikiProvider.LATEST_VERSION);

            //
            // Sanity check - shouldn't really be happening, unless
            // you mess with the repository directly.
            //
            if (att == null) {
              throw new ProviderException(
                  "Attachment disappeared while reading information:"
                  + " if you did not touch the repository, there is a serious bug somewhere. "
                  + "Attachment = " + attachments[i] + ", decoded = "
                  + attachmentName);
            }

            result.add(att);
          }
        }
      }
    }

    return result;
  }

  public Collection findAttachments(QueryItem[] query) {
    return null;
  }

  // FIXME: Very unoptimized.
  public List listAllChanged(Date timestamp) throws ProviderException {
    File attDir = new File(getStorageDir());

    if (!attDir.exists()) {
      throw new ProviderException("Specified attachment directory "
          + getStorageDir() + " does not exist!");
    }

    ArrayList list = new ArrayList();

    String[] pagesWithAttachments = attDir.list(new AttachmentFilter());

    for (int i = 0; i < pagesWithAttachments.length; i++) {
      String pageId = unmangleName(pagesWithAttachments[i]);
      pageId = pageId.substring(0, pageId.length() - DIR_EXTENSION.length());

      Collection c = listAttachments(new WikiPage(m_engine, pageId));

      for (Iterator it = c.iterator(); it.hasNext();) {
        Attachment att = (Attachment) it.next();

        if (att.getLastModified().after(timestamp)) {
          list.add(att);
        }
      }
    }

    Collections.sort(list, new PageTimeComparator());

    return list;
  }

  private String getStorageDir() {
    if (WikiMultiInstanceManager.getComponentId() == null) {
      return m_storageDir;
    } else
      return FileRepositoryManager.getAbsolutePath(WikiMultiInstanceManager
          .getComponentId());
  }

  public Attachment getAttachmentInfo(WikiPage page, String name, int version)
      throws ProviderException {
    Attachment att = new Attachment(m_engine, page.getName(), name);
    File dir = findAttachmentDir(att);

    if (!dir.exists()) {
      // log.debug(
      // "Attachment dir not found - thus no attachment can exist.");
      return null;
    }

    if (version == WikiProvider.LATEST_VERSION) {
      version = findLatestVersion(att);
    }

    att.setVersion(version);

    // Should attachment be cachable by the client (browser)?
    if (m_disableCache != null) {
      Matcher matcher = m_disableCache.matcher(name);
      if (matcher.matches()) {
        att.setCacheable(false);
      }
    }

    // System.out.println("Fetching info on version "+version);
    try {
      Properties props = getPageProperties(att);

      att.setAuthor(props.getProperty(version + ".author"));

      String changeNote = props.getProperty(version + ".changenote");
      if (changeNote != null) {
        att.setAttribute(WikiPage.CHANGENOTE, changeNote);
      }

      File f = findFile(dir, att);

      att.setSize(f.length());
      att.setLastModified(new Date(f.lastModified()));
    } catch (FileNotFoundException e) {
      return null;
    } catch (IOException e) {
      SilverTrace.error("wiki",
          "WikiVersioningFileProvider.getAttachmentInfo()",
          "wiki.EX_GET_ATTACHMENT_INFO", e);
      throw new ProviderException("Cannot read page properties: "
          + e.getMessage());
    }
    // FIXME: Check for existence of this particular version.

    return att;
  }

  public List getVersionHistory(Attachment att) {
    ArrayList list = new ArrayList();

    try {
      int latest = findLatestVersion(att);

      for (int i = latest; i >= 1; i--) {
        Attachment a = getAttachmentInfo(new WikiPage(m_engine, att
            .getParentName()), att.getFileName(), i);

        if (a != null) {
          list.add(a);
        }
      }
    } catch (ProviderException e) {
      SilverTrace.error("wiki",
          "WikiVersioningFileProvider.getVersionHistory()",
          "wiki.EX_GET_VERSION_HISTORY", "page : " + att, e);
      // FIXME: SHould this fail?
    }

    return list;
  }

  public void deleteVersion(Attachment att) throws ProviderException {
    // FIXME: Does nothing yet.
  }

  public void deleteAttachment(Attachment att) throws ProviderException {
    File dir = findAttachmentDir(att);
    String[] files = dir.list();

    for (int i = 0; i < files.length; i++) {
      File file = new File(dir.getAbsolutePath() + "/" + files[i]);
      file.delete();
    }
    dir.delete();
  }

  /**
   * Returns only those directories that contain attachments.
   */
  public static class AttachmentFilter implements FilenameFilter {
    public boolean accept(File dir, String name) {
      return name.endsWith(DIR_EXTENSION);
    }
  }

  /**
   * Accepts only files that are actual versions, no control files.
   */
  public static class AttachmentVersionFilter implements FilenameFilter {
    public boolean accept(File dir, String name) {
      return !name.equals(PROPERTY_FILE);
    }
  }

  public void moveAttachmentsForPage(String oldParent, String newParent)
      throws ProviderException {
    File srcDir = findPageDir(oldParent);
    File destDir = findPageDir(newParent);

    SilverTrace.debug("wiki",
        "WikiVersioningFileProvider.moveAttachmentsForPage()", "root:INFO",
        "Trying to move all attachments from " + srcDir + " to " + destDir);

    // If it exists, we're overwriting an old page (this has already been
    // confirmed at a higher level), so delete any existing attachments.
    if (destDir.exists()) {
      SilverTrace.error("wiki",
          "WikiVersioningFileProvider.moveAttachmentsForPage()",
          "wiki.EX_MOVE_PAGE_ATTACHMENTS",
          "Page rename failed because target dirctory " + destDir + " exists");
    } else {
      // destDir.getParentFile().mkdir();
      srcDir.renameTo(destDir);
    }
  }
}
