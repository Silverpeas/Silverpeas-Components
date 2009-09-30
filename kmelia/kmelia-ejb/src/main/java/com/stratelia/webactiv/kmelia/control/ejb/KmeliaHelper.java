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