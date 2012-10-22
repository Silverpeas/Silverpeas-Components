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
package com.silverpeas.silvercrawler.model;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import org.silverpeas.search.indexEngine.IndexFileManager;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Class declaration
 *
 * @author
 */
public class FileFolder extends Object implements java.io.Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 7637795486882013995L;
  /**
   * A File collection representing files in folder
   */
  private ArrayList<FileDetail> files;
  /**
   * A File collection representing folders in folder
   */
  private ArrayList<FileDetail> folders;
  /**
   * folder name
   */
  private String name;
  /**
   * folder path
   */
  private String path;
  /**
   * is folder writable ?
   */
  private boolean writable;
  /**
   * is folder readable ?
   */
  private boolean readable;

  /**
   * Constructor declaration
   *
   * @param path
   * @see
   */
  public FileFolder(String rootPath, String path) {
    new FileFolder(rootPath, path, false, "");
  }

  public boolean isWritable() {
    return writable;
  }

  public FileFolder(String rootPath, String path, boolean isAdmin, String componentId) {
    files = new ArrayList<FileDetail>(0);
    folders = new ArrayList<FileDetail>(0);

    try {
      SilverTrace.debug("silverCrawler", "FileFolder.FileFolder()", "root.MSG_GEN_PARAM_VALUE",
              "Starting constructor for FileFolder. Path = " + path);
      File f = new File(path);

      SilverTrace.debug("silverCrawler", "FileFolder.FileFolder()", "root.MSG_GEN_PARAM_VALUE",
              "isExists " + f.exists() + " isFile=" + f.isFile());

      writable = f.canWrite();

      if (f.exists()) {
        this.name = f.getName();
        this.readable = f.canRead();
        File[] children = f.listFiles();

        IndexReader reader = null;
        boolean isIndexed = false;

        if (isAdmin) {
          // ouverture de l'index
          Directory indexPath = FSDirectory.open(new File(IndexFileManager.getAbsoluteIndexPath("",
                  componentId)));
          if (IndexReader.indexExists(indexPath)) {
            reader = IndexReader.open(indexPath);
          }
        }
        if (children != null && children.length > 0) {
          for (File childFile : children) {
            SilverTrace.debug("silverCrawler", "FileFolder.FileFolder()",
                    "root.MSG_GEN_PARAM_VALUE", "Name = " + childFile.getName());
            isIndexed = false;
            if (isAdmin) {
              // rechercher si le répertoire (ou le fichier) est indexé
              String pathIndex = componentId + "|";
              if (childFile.isDirectory()) {
                pathIndex = pathIndex + "LinkedDir" + "|";
              } else {
                pathIndex = pathIndex + "LinkedFile" + "|";
              }
              pathIndex = pathIndex + childFile.getPath();
              SilverTrace.debug("silverCrawler", "FileFolder.FileFolder()",
                      "root.MSG_GEN_PARAM_VALUE", "pathIndex = " + pathIndex);

              Term term = new Term("key", pathIndex);
              if (reader != null && reader.docFreq(term) == 1) {
                isIndexed = true;
              }
            }

            if (childFile.isDirectory()) {
              folders.add(new FileDetail(childFile.getName(), childFile.getPath(), childFile.length(),
                      true, isIndexed));
            } else {
              String childPath = childFile.getPath().substring(rootPath.length() + 1);
              files.add(new FileDetail(childFile.getName(), childPath, childFile.length(), false,
                      isIndexed));
            }
          }
        }
        // fermeture de l'index
        if (reader != null && isAdmin) {
          reader.close();
        }

      }
    } catch (Exception e) {
      throw new SilverCrawlerRuntimeException("FileFolder.FileFolder()",
              SilverpeasRuntimeException.ERROR, "silverCrawler.IMPOSSIBLE_DACCEDER_AU_REPERTOIRE", e);
    }
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public Collection<FileDetail> getFiles() {
    return files;
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public Collection<FileDetail> getFolders() {
    return folders;
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public String getName() {
    return name;
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public String getPath() {
    return path;
  }

  /**
   * @param readable the readable to set
   */
  public void setReadable(boolean readable) {
    this.readable = readable;
  }

  /**
   * @return the readable
   */
  public boolean isReadable() {
    return readable;
  }
}