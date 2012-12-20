/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Created on 4 avr. 2005
 *
 */
package com.stratelia.webactiv.kmelia.control.ejb;

import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.webactiv.SilverpeasRole;
import java.io.Serializable;

import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.importExport.attachment.AttachmentDetail;
import org.silverpeas.search.indexEngine.model.IndexManager;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

/**
 * @author neysseri
 */
public class KmeliaHelper implements Serializable {

  private static final long serialVersionUID = 1L;
  public static final int PUBLICATION_HEADER = 0;
  public static final int PUBLICATION_CONTENT = 1;
  public static final int VALIDATION_CLASSIC = 0;
  public static final int VALIDATION_TARGET_1 = 1;
  public static final int VALIDATION_TARGET_N = 2;
  public static final int VALIDATION_COLLEGIATE = 3;
  public static final String ROLE_ADMIN = "admin";
  public static final String ROLE_PUBLISHER = "publisher";
  public static final String ROLE_WRITER = "writer";
  public static final String ROLE_READER = "user";
  public static final String SPECIALFOLDER_TOVALIDATE = "tovalidate";

  public KmeliaHelper() {
  }

  public static String getProfile(String[] profiles) {
    SilverpeasRole flag = SilverpeasRole.user;
    for (String profile : profiles) {
      SilverpeasRole role = SilverpeasRole.valueOf(profile);
      switch (role) {
        case admin:
          return SilverpeasRole.admin.toString();
        case publisher:
          flag = SilverpeasRole.publisher;
          break;
        case writer:
          if (flag != SilverpeasRole.publisher) {
            flag = SilverpeasRole.writer;
          }
          break;
        case supervisor:
          flag = SilverpeasRole.supervisor;
          break;
      }
    }
    return flag.toString();
  }

  public static void checkIndex(PublicationDetail pubDetail) {
    // This publication must be indexed ?
    // Only if it is valid
    if (isIndexable(pubDetail)) {
      pubDetail.setIndexOperation(IndexManager.ADD);
    } else {
      pubDetail.setIndexOperation(IndexManager.REMOVE);
    }
  }

  public static boolean isIndexable(PublicationDetail pubDetail) {
    return pubDetail.isIndexable();
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

  public static String getAttachmentUrl(PublicationDetail pubDetail, AttachmentDetail attDetail) {
    return "/Rkmelia/" + attDetail.getPK().getInstanceId()
        + "/searchResult?Type=Attachment&Id=" + pubDetail.getPK().getId() + "&AttachmentId="
        + attDetail.getPK().getId() + "&FileOpened=0";
  }

  public static String getDocumentUrl(PublicationDetail pubDetail, Document document) {
    return "/Rkmelia/" + document.getPk().getInstanceId()
        + "/searchResult?Type=Document&Id=" + pubDetail.getPK().getId() + "&DocumentId=" + document.
        getPk().getId() + "&FileOpened=0";
  }

  public static String getDocumentUrl(PublicationDetail pubDetail, SimpleDocument document) {
    return "/Rkmelia/" + document.getPk().getInstanceId()
        + "/searchResult?Type=Document&Id=" + pubDetail.getPK().getId() + "&DocumentId=" + document.
        getId() + "&FileOpened=0";
  }

  public static boolean isToolbox(String componentId) {
    return componentId.startsWith("toolbox");
  }

  public static boolean isKmax(String componentId) {
    return componentId.startsWith("kmax");
  }

  public static boolean isKmelia(String componentId) {
    return componentId.startsWith("kmelia");
  }

  public static boolean isToValidateFolder(String id) {
    return SPECIALFOLDER_TOVALIDATE.equalsIgnoreCase(id);
  }
}