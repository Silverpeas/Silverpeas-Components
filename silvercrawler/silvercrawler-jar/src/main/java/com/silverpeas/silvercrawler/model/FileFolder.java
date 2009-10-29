/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
package com.silverpeas.silvercrawler.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class FileFolder extends Object implements java.io.Serializable {
  /**
   * A File collection representing files in folder
   */
  private ArrayList files;

  /**
   * A File collection representing folders in folder
   */
  private ArrayList folders;

  /**
   * folder name
   */
  private String name;

  /**
   * folder path
   */
  private String path;

  /**
   * Constructor declaration
   * 
   * 
   * @param path
   * 
   * @see
   */
  public FileFolder(String rootPath, String path) {
    new FileFolder(rootPath, path, false, "");
  }

  public FileFolder(String rootPath, String path, boolean isAdmin,
      String componentId) {
    files = new ArrayList(0);
    folders = new ArrayList(0);

    String childPath = null;

    try {
      SilverTrace.debug("silverCrawler", "FileFolder.FileFolder()",
          "root.MSG_GEN_PARAM_VALUE",
          "Starting constructor for FileFolder. Path = " + path);
      File f = new File(path);
      File fChild;

      SilverTrace.debug("silverCrawler", "FileFolder.FileFolder()",
          "root.MSG_GEN_PARAM_VALUE", "isExists " + f.exists() + " isFile="
              + f.isFile());
      if (f.exists()) {
        this.name = f.getName();
        String[] children_name = f.list();

        IndexReader reader = null;
        boolean isIndexed = false;

        if (isAdmin) {
          // ouverture de l'index
          String indexPath = FileRepositoryManager.getAbsoluteIndexPath("",
              componentId);

          if (IndexReader.indexExists(indexPath))
            reader = IndexReader.open(indexPath);
        }

        for (int i = 0; children_name != null && i < children_name.length; i++) {
          SilverTrace.debug("silverCrawler", "FileFolder.FileFolder()",
              "root.MSG_GEN_PARAM_VALUE", "Name = " + children_name[i]);
          fChild = new File(path + File.separator + children_name[i]);
          isIndexed = false;
          if (isAdmin) {
            // rechercher si le répertoire (ou le fichier) est indexé
            String pathIndex = componentId + "|";
            if (fChild.isDirectory())
              pathIndex = pathIndex + "LinkedDir" + "|";
            else
              pathIndex = pathIndex + "LinkedFile" + "|";
            pathIndex = pathIndex + fChild.getPath();
            SilverTrace.debug("silverCrawler", "FileFolder.FileFolder()",
                "root.MSG_GEN_PARAM_VALUE", "pathIndex = " + pathIndex);

            Term term = new Term("key", pathIndex);
            if (reader != null && reader.docFreq(term) == 1)
              isIndexed = true;
          }

          if (fChild.isDirectory()) {
            folders.add(new FileDetail(fChild.getName(), fChild.getPath(),
                fChild.length(), true, isIndexed));
          } else {
            childPath = fChild.getPath().substring(rootPath.length() + 1);
            files.add(new FileDetail(fChild.getName(), childPath, fChild
                .length(), false, isIndexed));
          }
        }

        // fermeture de l'index
        if (reader != null && isAdmin)
          reader.close();

      }
    } catch (Exception e) {
      throw new SilverCrawlerRuntimeException("FileFolder.FileFolder()",
          SilverpeasRuntimeException.ERROR,
          "silverCrawler.IMPOSSIBLE_DACCEDER_AU_REPERTOIRE", e);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public Collection getFiles() {
    return files;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public Collection getFolders() {
    return folders;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getName() {
    return name;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getPath() {
    return path;
  }
}