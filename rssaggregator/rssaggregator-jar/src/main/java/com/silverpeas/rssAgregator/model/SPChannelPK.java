/*
 * Created on 20 août 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.silverpeas.rssAgregator.model;

import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * @author neysseri
 * 
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SPChannelPK extends WAPrimaryKey {

  public SPChannelPK(String id) {
    super(id);
  }

  public SPChannelPK(String id, String componentName) {
    super(id, "useless", componentName);
  }

  public SPChannelPK(String id, WAPrimaryKey pk) {
    super(id, pk);
  }

  public String getRootTableName() {
    return "SC_Rss_Channels";
  }

  public String getTableName() {
    return "SC_Rss_Channels";
  }

  public boolean equals(Object other) {
    if (!(other instanceof SPChannelPK)) {
      return false;
    }
    return (id.equals(((SPChannelPK) other).getId()))
        && (space.equals(((SPChannelPK) other).getSpace()))
        && (componentName.equals(((SPChannelPK) other).getComponentName()));
  }

  public int hashCode() {
    return toString().hashCode();
  }

}
