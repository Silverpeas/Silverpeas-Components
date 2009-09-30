package com.silverpeas.mydb.model;

import java.io.Serializable;

import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * Primary key for a MyDB database connection.
 * 
 * @author Antoine HEDIN
 */
public class MyDBConnectionInfoPK extends WAPrimaryKey implements Serializable {

  public MyDBConnectionInfoPK(String id) {
    super(id);
  }

  public MyDBConnectionInfoPK(String id, WAPrimaryKey pk) {
    super(id, pk);
  }

  public MyDBConnectionInfoPK(String id, String space, String componentName) {
    super(id, space, componentName);
  }

  public String getRootTableName() {
    return "MyDBConnectionInfo";
  }

  public String getTableName() {
    return "SC_MyDB_ConnectInfo";
  }

  public boolean equals(Object other) {
    return ((other instanceof MyDBConnectionInfoPK)
        && (id.equals(((MyDBConnectionInfoPK) other).getId()))
        && (space.equals(((MyDBConnectionInfoPK) other).getSpace())) && (componentName
        .equals(((MyDBConnectionInfoPK) other).getComponentName())));
  }

  public int hashCode() {
    return toString().hashCode();
  }

}