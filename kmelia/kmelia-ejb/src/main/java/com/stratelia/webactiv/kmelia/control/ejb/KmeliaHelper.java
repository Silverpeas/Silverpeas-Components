/*
 * Created on 4 avr. 2005
 *
 */
package com.stratelia.webactiv.kmelia.control.ejb;

import java.io.Serializable;

import com.stratelia.webactiv.util.indexEngine.model.IndexManager;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

/**
 * @author neysseri
 * 
 */
public class KmeliaHelper implements Serializable {

  public static final int PUBLICATION_HEADER = 0;
  public static final int PUBLICATION_CONTENT = 1;

  public static final int VALIDATION_CLASSIC = 0;
  public static final int VALIDATION_TARGET_1 = 1;
  public static final int VALIDATION_TARGET_N = 2;
  public static final int VALIDATION_COLLEGIATE = 3;

  public KmeliaHelper() {
  }

  public static String getProfile(String[] profiles) {
    String flag = "user";
    String profile = "";
    for (int i = 0; i < profiles.length; i++) {
      profile = profiles[i];
      // if admin, return it, we won't find a better profile
      if (profile.equals("admin"))
        return profile;
      if (profile.equals("publisher"))
        flag = profile;
      else if (profile.equals("writer")) {
        if (!flag.equals("publisher"))
          flag = profile;
      } else if (profile.equals("supervisor")) {
        flag = profile;
      }
    }
    return flag;
  }

  public static void checkIndex(PublicationDetail pubDetail) {
    // This publication must be indexed ?
    // Only if it is valid
    if (isIndexable(pubDetail))
      pubDetail.setIndexOperation(IndexManager.ADD);
    else
      pubDetail.setIndexOperation(IndexManager.REMOVE);
  }

  public static boolean isIndexable(PublicationDetail pubDetail) {
    return "Valid".equalsIgnoreCase(pubDetail.getStatus());
  }

  public static String extractObjectIdFromURL(String url) {
    return url.substring(url.indexOf("Id=") + 3, url.length());
  }

  public static String extractObjectTypeFromURL(String url) {
    return url.substring(url.indexOf("Type=") + 5, url.lastIndexOf("&"));
  }

  public static String getPublicationUrl(PublicationDetail pubDetail) {
    return "/Rkmelia/" + pubDetail.getPK().getInstanceId()
        + "/searchResult?Type=Publication&Id=" + pubDetail.getPK().getId();
  }

  public static String getNodeUrl(NodeDetail nodeDetail) {
    return "/Rkmelia/" + nodeDetail.getNodePK().getInstanceId()
        + "/searchResult?Type=Node&Id=" + nodeDetail.getNodePK().getId();
  }

  public static boolean isToolbox(String componentId) {
    return componentId.startsWith("toolbox");
  }

  public static boolean isKmax(String componentId) {
    return componentId.startsWith("kmax");
  }
}