/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

package org.silverpeas.components.kmelia.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FileFolder implements java.io.Serializable {
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
   */
  public FileFolder(String path) {
    this.path = path;
    files = new ArrayList(0);
    folders = new ArrayList(0);
    children = new ArrayList(0);

    try {
      File f = new File(path);
      File fChild;
      if (f.exists()) {
        this.name = f.getName();
        String[] childrenName = f.list();

        for (int i = 0; childrenName != null && i < childrenName.length; i++) {
          fChild = new File(path + File.separator + childrenName[i]);
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
      throw new KmeliaRuntimeException(e);
    }
  }

  /**
   * @return
   */
  public Collection getFiles() {
    return files;
  }

  /**
   * @return
   */
  public Collection getFolders() {
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
   * @return
   */
  public List getChildren() {
    return children;
  }

}
