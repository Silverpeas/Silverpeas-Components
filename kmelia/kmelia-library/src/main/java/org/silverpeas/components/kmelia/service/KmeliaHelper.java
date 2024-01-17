/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */


package org.silverpeas.components.kmelia.service;

import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.index.indexing.model.IndexManager;

import java.io.Serializable;
import java.util.Collection;

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
  public static final String SPECIALFOLDER_NONVISIBLEPUBS = "notvisibleContributions";

  private KmeliaHelper() {
  }

  public static String getSilverpeasRole(Collection<SilverpeasRole> roles) {
    return getProfile(roles.stream().map(SilverpeasRole::getName).toArray(String[]::new));
  }

  public static String getProfile(String[] profiles) {
    if (ArrayUtil.isEmpty(profiles)) {
      return null;
    }
    SilverpeasRole flag = SilverpeasRole.USER;
    for (String profile : profiles) {
      SilverpeasRole role = SilverpeasRole.fromString(profile);
      switch (role) {
        case ADMIN:
          return SilverpeasRole.ADMIN.toString();
        case PUBLISHER:
          flag = SilverpeasRole.PUBLISHER;
          break;
        case WRITER:
          if (flag != SilverpeasRole.PUBLISHER) {
            flag = SilverpeasRole.WRITER;
          }
          break;
        case SUPERVISOR:
          flag = SilverpeasRole.SUPERVISOR;
          break;
        default:
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
    return url.substring(url.indexOf("Type=") + 5, url.lastIndexOf('&'));
  }

  public static String getPublicationUrl(PublicationDetail pubDetail, NodePK nodePK) {
    if (pubDetail.isAlias() && nodePK != null) {
      // app of the alias have to be defined
      return URLUtil.getSimpleURL(URLUtil.URL_PUBLI, pubDetail.getPK().getId(),
          nodePK.getInstanceId(), false);
    }
    return URLUtil.getSimpleURL(URLUtil.URL_PUBLI, pubDetail.getPK().getId(), false);
  }

  public static String getNodeUrl(NodeDetail nodeDetail) {
    return URLUtil.getSimpleURL(URLUtil.URL_TOPIC, nodeDetail.getNodePK().getId(), nodeDetail
        .getNodePK().getInstanceId(), false);
  }

  public static String getDocumentUrl(PublicationDetail pubDetail, SimpleDocument document, String instanceId) {
    return "/Rkmelia/" + instanceId
        + "/searchResult?Type=Document&Id=" + pubDetail.getId() + "&DocumentId=" + document.
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

  public static boolean isNonVisiblePubsFolder(String id) {
    return SPECIALFOLDER_NONVISIBLEPUBS.equalsIgnoreCase(id);
  }

  public static boolean isSpecialFolder(String id) {
    return isToValidateFolder(id) || isNonVisiblePubsFolder(id);
  }
}
