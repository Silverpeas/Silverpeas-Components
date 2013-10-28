/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

package com.silverpeas.mydb.data.key;

import java.util.ArrayList;

import com.silverpeas.mydb.data.db.DbTable;

/**
 * Table unicity keys list.
 * @author Antoine HEDIN
 */
public class UnicityKeys {

  private ArrayList<UnicityKey> unicityKeys;
  private DbTable parentTable;

  public UnicityKeys(DbTable parentTable) {
    unicityKeys = new ArrayList<UnicityKey>();
    this.parentTable = parentTable;
  }

  public boolean isEmpty() {
    return (unicityKeys.size() == 0);
  }

  public int getSize() {
    return unicityKeys.size();
  }

  public void add(UnicityKey unicityKey) {
    unicityKeys.add(unicityKey);
  }

  public void remove(int index) {
    unicityKeys.remove(index);
  }

  public UnicityKey get(int index) {
    return unicityKeys.get(index);
  }

  public String getConstraintName() {
    String tableName = parentTable.getName();
    if (tableName.length() > 0) {
      ArrayList<String> uKeys = new ArrayList<String>();
      for (int i = 0, n = getSize(); i < n; i++) {
        uKeys.add(get(i).getName());
      }
      int unicityIndex = 1;
      tableName = tableName.toLowerCase();
      String unicityKey = "un_" + tableName + "_" + unicityIndex;
      while (uKeys.contains(unicityKey)) {
        unicityIndex++;
        unicityKey = "un_" + tableName + "_" + unicityIndex;
      }
      return unicityKey;
    } else {
      return "";
    }
  }

  public void update(UnicityKey unicityKey, int index) {
    if (index == -1) {
      add(unicityKey);
    } else {
      unicityKeys.set(index, unicityKey);
    }
    parentTable.forceColumnsNotNull(unicityKey.getColumns());
    parentTable.forceColumnsNoDefaultValue(unicityKey.getColumns());
  }

  public void replace(String oldColumnName, String newColumnName) {
    for (int i = 0, n = getSize(); i < n; i++) {
      get(i).replaceColumn(oldColumnName, newColumnName);
    }
  }

  public void remove(String oldColumnName) {
    UnicityKey unicityKey;
    for (int i = (getSize() - 1); i >= 0; i--) {
      unicityKey = get(i);
      unicityKey.removeColumn(oldColumnName);
      if (unicityKey.getColumnsCount() == 0) {
        remove(i);
      }
    }
  }

  public boolean isUnicityKey(String name) {
    for (int i = 0, n = getSize(); i < n; i++) {
      if (get(i).containsColumn(name)) {
        return true;
      }
    }
    return false;
  }

}
