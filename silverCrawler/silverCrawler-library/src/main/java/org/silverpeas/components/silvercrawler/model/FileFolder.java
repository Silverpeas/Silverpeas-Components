/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.silvercrawler.model;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.index.indexing.IndexFileManager;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class declaration
 * @author
 */
public class FileFolder implements java.io.Serializable {

  private static final long serialVersionUID = 7637795486882013995L;
  /**
   * A File collection representing files in folder
   */
  private List<FileDetail> files;
  /**
   * A File collection representing folders in folder
   */
  private List<FileDetail> folders;
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
   * @param rootPath
   * @param path
   */
  public FileFolder(String rootPath, String path) {
    this(rootPath, path, false, "");
  }

  public boolean isWritable() {
    return writable;
  }

  public FileFolder(String rootPath, String path, boolean isAdmin, String componentId) {
    this.path = path;
    files = new ArrayList<>(0);
    folders = new ArrayList<>(0);
    IndexReader reader = null;
    try {
      // Check security access : cannot browse inside rootPath
      FileUtil.validateFilename(path, rootPath);
      File f = new File(path);

      writable = f.canWrite();

      if (f.exists()) {
        this.name = f.getName();
        this.readable = f.canRead();
        File[] children = f.listFiles();
        boolean isIndexed = false;

        if (isAdmin) {
          // ouverture de l'index
          Directory indexPath =
              FSDirectory.open(Paths.get(IndexFileManager.getAbsoluteIndexPath(componentId)));
          if (DirectoryReader.indexExists(indexPath)) {
            reader = DirectoryReader.open(indexPath);
          }
        }
        if (children != null && children.length > 0) {
          for (File childFile : children) {
            isIndexed = false;
            if (isAdmin) {
              // rechercher si le répertoire (ou le fichier) est indexé
              StringBuilder pathIndex = new StringBuilder(componentId).append("|");
              if (childFile.isDirectory()) {
                pathIndex.append("LinkedDir").append("|");
              } else {
                pathIndex.append("LinkedFile").append("|");
              }
              pathIndex.append(FilenameUtils.separatorsToUnix(childFile.getPath()));
              Term term = new Term("key", pathIndex.toString());
              if (reader != null && reader.docFreq(term) == 1) {
                isIndexed = true;
              }
            }

            if (childFile.isDirectory()) {
              folders.add(
                  new FileDetail(childFile.getName(), childFile.getPath(), null, childFile.length(),
                      true, isIndexed));
            } else {
              String childPath =
                  FileUtils.getFile(childFile.getPath().substring(rootPath.length())).getPath();
              files.add(new FileDetail(childFile.getName(), childPath, childFile.getPath(),
                  childFile.length(), false, isIndexed));
            }
          }
        }
      }
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e);
    } finally {
      // fermeture de l'index
      if (reader != null && isAdmin) {
        try {
          reader.close();
        } catch (IOException e) {
          SilverLogger.getLogger(this).warn(e);
        }
      }
    }
  }

  /**
   * @return
   */
  public Collection<FileDetail> getFiles() {
    return files;
  }

  /**
   * @return
   */
  public Collection<FileDetail> getFolders() {
    return folders;
  }

  /**
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * @return
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