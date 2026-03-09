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

package org.silverpeas.components.silvercrawler.model;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.silverpeas.core.index.indexing.IndexFileManager;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.kernel.SilverpeasRuntimeException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FileFolder implements java.io.Serializable {

  private static final long serialVersionUID = 7637795486882013995L;
  /**
   * A File collection representing files in folder
   */
  private final List<FileDetail> files;
  /**
   * A File collection representing folders in folder
   */
  private final List<FileDetail> folders;
  /**
   * folder name
   */
  private String name;
  /**
   * folder path
   */
  private final String path;
  /**
   * is folder writable ?
   */
  private final boolean writable;
  /**
   * is folder readable ?
   */
  private boolean readable;

  public FileFolder(String rootPath, String path) {
    this(rootPath, path, false, "");
  }

  public boolean isWritable() {
    return writable;
  }

  public FileFolder(String rootPath, String path, boolean isAdmin, String componentId) {
    this.path = path;
    files = new ArrayList<>();
    folders = new ArrayList<>();
    try {
      // Check security access: cannot browse inside rootPath
      FileUtil.validateFilename(path, rootPath);
      File f = new File(path);
      this.writable = f.canWrite();

      if (f.exists()) {
        this.name = f.getName();
        this.readable = f.canRead();
        lookupForChildren(f, rootPath, isAdmin, componentId);
      }
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  private void lookupForChildren(File f, String rootPath, boolean isAdmin, String componentId) throws IOException {
    File[] children = f.listFiles();
    try (IndexReader reader = openIndexReader(componentId, isAdmin)) {
      if (children != null) {
        for (File childFile : children) {
          boolean isIndexed = isIsIndexed(reader, childFile, isAdmin, componentId);
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
  }

  private static boolean isIsIndexed(IndexReader reader, File childFile, boolean isAdmin,
      String componentId) throws IOException {
    boolean isIndexed = false;
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
    return isIndexed;
  }

  private IndexReader openIndexReader(String componentId, boolean isAdmin) throws IOException {
    IndexReader reader = null;
    if (isAdmin) {
      // ouverture de l'index
      Directory indexPath =
          FSDirectory.open(Paths.get(IndexFileManager.getAbsoluteIndexPath(componentId)));
      if (DirectoryReader.indexExists(indexPath)) {
        reader = DirectoryReader.open(indexPath);
      }
    }
    return reader;
  }

  public Collection<FileDetail> getFiles() {
    return files;
  }

  public Collection<FileDetail> getFolders() {
    return folders;
  }

  public String getName() {
    return name;
  }

  public String getPath() {
    return path;
  }

  public void setReadable(boolean readable) {
    this.readable = readable;
  }

  public boolean isReadable() {
    return readable;
  }
}