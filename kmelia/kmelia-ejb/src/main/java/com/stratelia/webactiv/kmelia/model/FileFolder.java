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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.kmelia.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class FileFolder extends Object implements java.io.Serializable {
  private static final long serialVersionUID = 5071147110169726697L;

  /**
   * A File collection representing all items in folder
   */
  private ArrayList children;

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
   * @param path
   * @see
   */
  public FileFolder(String path) {
    this.path = path;
    files = new ArrayList(0);
    folders = new ArrayList(0);
    children = new ArrayList(0);

    try {
      SilverTrace.debug("kmelia", "FileFolder.FileFolder()",
          "root.MSG_GEN_PARAM_VALUE",
          "Starting constructor for FileFolder. Path = " + path);
      File f = new File(path);
      File fChild;

      SilverTrace.debug("kmelia", "FileFolder.FileFolder()",
          "root.MSG_GEN_PARAM_VALUE", "isExists " + f.exists() + " isFile="
          + f.isFile());
      if (f.exists()) {
        this.name = f.getName();
        String[] children_name = f.list();

        for (int i = 0; children_name != null && i < children_name.length; i++) {
          SilverTrace.debug("kmelia", "FileFolder.FileFolder()",
              "root.MSG_GEN_PARAM_VALUE", "Name = " + children_name[i]);
          fChild = new File(path + File.separator + children_name[i]);
          children.add(new FileDetail(fChild.getName(), fChild.getPath(),
              fChild.length(), fChild.isDirectory()));
          if (fChild.isDirectory()) {
            folders.add(new FileDetail(fChild.getName(), fChild.getPath(),
                fChild.length(), fChild.isDirectory()));
          } else {
            files.add(new FileDetail(fChild.getName(), fChild.getPath(), fChild
                .length(), fChild.isDirectory()));
          }
        }
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("FileFolder.FileFolder()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.IMPOSSIBLE_DACCEDER_AU_REPERTOIRE", e);
    }
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public Collection getFiles() {
    return files;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public Collection getFolders() {
    return folders;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getName() {
    return name;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getPath() {
    return path;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public ArrayList getChildren() {
    return children;
  }

}
