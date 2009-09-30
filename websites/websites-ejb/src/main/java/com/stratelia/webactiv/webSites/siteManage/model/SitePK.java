/*
 * SitePK.java
 *
 * Created on 9 Avril 2001, 16:40
 */

package com.stratelia.webactiv.webSites.siteManage.model;

/** 
 *
 * @author  cbonin
 * @version 
 */

import java.io.Serializable;

import com.stratelia.webactiv.util.WAPrimaryKey;

public class SitePK extends WAPrimaryKey implements Serializable {

  public SitePK(String id) {
    super(id);
  }

  public SitePK(String id, String space, String componentName) {
    super(id, space, componentName);
  }

  public SitePK(String id, WAPrimaryKey pk) {
    super(id, pk);
  }

  public String getRootTableName() {
    return "site";
  }

  public boolean equals(Object other) {
    if (!(other instanceof SitePK))
      return false;
    return (id.equals(((SitePK) other).getId()))
        && (space.equals(((SitePK) other).getSpace()))
        && (componentName.equals(((SitePK) other).getComponentName()));
  }

}