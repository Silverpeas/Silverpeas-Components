package com.silverpeas.mydb.data.key;

import java.util.ArrayList;

import com.silverpeas.mydb.data.db.DbTable;

/**
 * Table unicity keys list.
 * 
 * @author Antoine HEDIN
 */
public class UnicityKeys {

  private ArrayList unicityKeys;
  private DbTable parentTable;

  public UnicityKeys(DbTable parentTable) {
    unicityKeys = new ArrayList();
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
    return (UnicityKey) unicityKeys.get(index);
  }

  public String getConstraintName() {
    String tableName = parentTable.getName();
    if (tableName.length() > 0) {
      ArrayList uKeys = new ArrayList();
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
