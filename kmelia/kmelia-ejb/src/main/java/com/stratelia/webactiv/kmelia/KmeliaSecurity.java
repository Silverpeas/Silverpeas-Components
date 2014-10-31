/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.stratelia.webactiv.kmelia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silverpeas.core.admin.OrganizationController;
import org.silverpeas.core.admin.OrganizationControllerProvider;

import org.silverpeas.util.StringUtil;
import org.silverpeas.util.security.ComponentSecurity;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.ObjectType;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBm;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaHelper;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import org.silverpeas.util.EJBUtilitaire;
import org.silverpeas.util.JNDINames;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.util.exception.SilverpeasRuntimeException;
import org.silverpeas.util.exception.UtilException;
import com.stratelia.webactiv.node.control.NodeService;
import com.stratelia.webactiv.node.model.NodeDetail;
import com.stratelia.webactiv.node.model.NodePK;
import com.stratelia.webactiv.publication.control.PublicationBm;
import com.stratelia.webactiv.publication.model.PublicationDetail;
import com.stratelia.webactiv.publication.model.PublicationPK;

import static com.stratelia.webactiv.SilverpeasRole.*;

/**
 * Kmelia security provides a way to check the rights of a user to access the content of a Kmelia
 * instance (publications, ...).
 */
public class KmeliaSecurity implements ComponentSecurity {

  private static final String CO_WRITING_PARAM = "coWriting";
  private static final String DRAFT_VISIBLE_WITH_CO_WRITING_PARAM = "draftVisibleWithCoWriting";
  public static final String PUBLICATION_TYPE = "Publication";
  public static final String NODE_TYPE = "Node";
  public static final String RIGHTS_ON_TOPIC_PARAM = "rightsOnTopics";
  private PublicationBm publicationBm;
  private NodeService nodeService = NodeService.getNodeService();
  private KmeliaBm kmeliaBm;
  private OrganizationController controller = null;
  private Map<String, Boolean> cache = Collections.synchronizedMap(new HashMap<String, Boolean>());
  private volatile boolean cacheEnabled = false;
  private ResourceLocator kmeliaSettings = new ResourceLocator(
      "org.silverpeas.kmelia.settings.kmeliaSettings", "fr");

  public KmeliaSecurity() {
    this.controller = OrganizationControllerProvider.getOrganisationController();
  }

  public KmeliaSecurity(OrganizationController controller) {
    this.controller = controller;
  }

  @Override
  public synchronized void enableCache() {
    cache.clear();
    cacheEnabled = true;
  }

  @Override
  public synchronized void disableCache() {
    cache.clear();
    cacheEnabled = false;
  }

  private void writeInCache(String objectId, String objectType, String componentId,
      boolean available) {
    if (cacheEnabled) {
      cache.put(objectId + objectType + componentId, available);
    }
  }

  private Boolean readFromCache(String objectId, String objectType, String componentId) {
    if (cacheEnabled) {
      return cache.get(objectId + objectType + componentId);
    }
    return null;
  }

  private void writeInCache(String componentId, boolean available) {
    if (cacheEnabled) {
      cache.put(componentId, available);
    }
  }

  private Boolean readFromCache(String componentId) {
    if (cacheEnabled) {
      return cache.get(componentId);
    }
    return null;
  }

  @Override
  public boolean isAccessAuthorized(String componentId, String userId, String objectId) {
    return isAccessAuthorized(componentId, userId, objectId, PUBLICATION_TYPE);
  }

  @Override
  public boolean isAccessAuthorized(String componentId, String userId, String objectId,
      String objectType) {
    // first, check if user is able to access to component
    if (!isComponentAvailable(componentId, userId)) {
      return false;
    }

    if (isKmeliaObjectType(objectType)) {
      PublicationPK pk = new PublicationPK(objectId, componentId);
      // First, check if publication is available
      if (!isPublicationAvailable(pk, userId)) {
        return false;
      }
      // Then, check the publication's status and visibility period
      PublicationDetail publication = getPublicationDetail(pk);
      if (publication != null) {
        String profile = getProfile(userId, pk);
        if (!getKmeliaBm().isPublicationVisible(publication, SilverpeasRole.from(profile), userId)) {
          return false;
        }
        if (publication.isValid()) {
          return true;
        }
        if (publication.isValidationRequired()) {
          if (user.isInRole(profile)) {
            return false;
          }
          if (writer.isInRole(profile)) {
            return publication.isPublicationEditor(userId);
          }
          return true;
        }
        if (publication.isRefused()) {
          if (!user.isInRole(profile)) {
            return publication.isPublicationEditor(userId) || admin.isInRole(profile)
                || publisher.isInRole(profile);
          }
          return false;
        }
        if (publication.isDraft()) {
          if (!user.isInRole(profile)) {
            if (isCoWritingEnable(componentId)
                && isDraftVisibleWithCoWriting()
                && !reader.isInRole(profile)) {
              return true;
            } else {
              return publication.isPublicationEditor(userId);
            }
          }
          return false;
        }
      }
    } else if (NODE_TYPE.equalsIgnoreCase(objectType)) {
      NodePK pk = new NodePK(objectId, componentId);
      return isNodeAvailable(pk, userId);
    }
    return true;
  }

  @Override
  public boolean isObjectAvailable(String componentId, String userId, String objectId,
      String objectType) {
    if (isKmeliaObjectType(objectType)) {
      return isPublicationAvailable(new PublicationPK(objectId, componentId), userId);
    }
    if (NODE_TYPE.equalsIgnoreCase(objectType)) {
      return isNodeAvailable(new NodePK(objectId, componentId), userId);
    }
    return true;
  }

  protected boolean isKmeliaObjectType(String objectType) {
    return objectType != null && (PUBLICATION_TYPE.equalsIgnoreCase(objectType)
        || objectType.startsWith("Attachment") || objectType.startsWith("Version"));
  }

  protected boolean isRightsOnTopicsEnabled(String componentId) {
    String param = controller.getComponentParameterValue(componentId, RIGHTS_ON_TOPIC_PARAM);
    return StringUtil.getBooleanValue(param);
  }

  protected boolean isDraftVisibleWithCoWriting() {
    return kmeliaSettings.getBoolean(DRAFT_VISIBLE_WITH_CO_WRITING_PARAM, false);
  }

  protected boolean isCoWritingEnable(String componentId) {
    String param = controller.getComponentParameterValue(componentId, CO_WRITING_PARAM);
    return StringUtil.getBooleanValue(param);
  }

  private boolean isComponentAvailable(String componentId, String userId) {
    Boolean fromCache = readFromCache(componentId);
    if (fromCache != null) {
      // Availabily already processed
      return fromCache;
    }

    boolean available = controller.isComponentAvailable(componentId, userId);
    writeInCache(componentId, available);
    return available;
  }

  protected boolean isPublicationAvailable(PublicationPK pk, String userId) {
    Boolean fromCache = readFromCache(pk.getId(), PUBLICATION_TYPE, pk.getInstanceId());
    if (fromCache != null) {
      // Availabily already processed
      return fromCache;
    }

    boolean objectAvailable = false;
    Collection<NodePK> fatherPKs = getPublicationFolderPKs(pk);
    if (fatherPKs == null) {
      return false;
    }
    if (isInBasket(fatherPKs)) {
      SilverpeasRole profile = SilverpeasRole.from(KmeliaHelper.getProfile(getAppProfiles(userId,
          pk.getInstanceId())));
      if (SilverpeasRole.READER_ROLES.contains(profile)) {
        // readers do not see basket content
        return false;
      } else if (SilverpeasRole.admin == profile) {
        // admins see basket content
        return true;
      } else {
        // others profiles see only theirs publications
        PublicationDetail publication = getPublicationDetail(pk);
        if (publication != null) {
          return publication.isPublicationEditor(userId);
        }
      }
    } else if (isRightsOnTopicsEnabled(pk.getInstanceId())) {
      for (NodePK fatherPK : fatherPKs) {
        if (!fatherPK.isTrash()) {
          try {
            objectAvailable = isNodeAvailable(fatherPK, userId);
          } catch (Exception e) {
            // don't throw exception, log only error
            SilverTrace.error("kmelia", "KmeliaSecurity.isNodeAvailable",
                "root.MSG_GEN_PARAM_VALUE",
                "Node (" + fatherPK.getId() + ", " + fatherPK.getInstanceId()
                + ") no more exist but still referenced by a publication (" + pk.getId() + ", "
                + pk.getInstanceId() + ")");
            objectAvailable = false;
          }
        } else {
          objectAvailable = true;
        }
        if (objectAvailable) {
          break;
        }
      }
    } else {
      objectAvailable = true;
    }
    writeInCache(pk.getId(), PUBLICATION_TYPE, pk.getInstanceId(), objectAvailable);
    return objectAvailable;
  }

  private Collection<NodePK> getPublicationFolderPKs(PublicationPK pk) {
    Collection<NodePK> fatherPKs = null;
    try {
      fatherPKs = getPublicationBm().getAllFatherPK(pk);
    } catch (Exception e) {
      SilverTrace.warn("kmelia", "KmeliaSecurity.getPublicationFolderPKs",
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LA_PUBLICATION", "PubId = " + pk.toString());
    }
    return fatherPKs;
  }

  private boolean isInBasket(Collection<NodePK> pks) {
    for (NodePK pk : pks) {
      return pk.isTrash();
    }
    return false;
  }

  private PublicationDetail getPublicationDetail(PublicationPK pk) {
    try {
      return getPublicationBm().getDetail(pk);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaSecurity.getPublicationDetail()",
          SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DOBTENIR_LA_PUBLICATION", e);
    }
  }

  private boolean isNodeAvailable(NodePK nodePK, String userId) {
    Boolean fromCache = readFromCache(nodePK.getId(), NODE_TYPE, nodePK.getInstanceId());
    if (fromCache != null) {
      // Availabily already processed
      return fromCache;
    }
    boolean objectAvailable = false;
    if (isRightsOnTopicsEnabled(nodePK.getInstanceId())) {
      NodeDetail node = getNodeService().getHeader(nodePK, false);
      if (node != null) {
        if (!node.haveRights()) {
          objectAvailable = true;
        } else {
          objectAvailable = controller.isObjectAvailable(node.getRightsDependsOn(), ObjectType.NODE,
              nodePK.getInstanceId(), userId);
        }
      } else {
        objectAvailable = false;
      }
    } else {
      objectAvailable = true;
    }
    writeInCache(nodePK.getId(), NODE_TYPE, nodePK.getInstanceId(), objectAvailable);
    return objectAvailable;
  }

  private String getProfile(String userId, PublicationPK pubPK) {
    String[] profiles;
    if (!isRightsOnTopicsEnabled(pubPK.getInstanceId())) {
      // get component-level profiles
      profiles = getAppProfiles(userId, pubPK.getInstanceId());
    } else {
      // get topic-level profiles
      Collection<NodePK> nodePKs = getPublicationBm().getAllFatherPK(pubPK);
      List<String> lProfiles = new ArrayList<String>();
      for (NodePK nodePK : nodePKs) {
        NodeDetail node = getNodeService().getHeader(nodePK);
        if (node != null) {
          SilverTrace.debug("kmelia", "KmeliaSecurity.getProfile",
              "root.MSG_GEN_PARAM_VALUE", "nodePK = " + nodePK.toString());
          if (!node.haveRights()) {
            lProfiles.addAll(Arrays.asList(getAppProfiles(userId, pubPK.getInstanceId())));
          } else {
            lProfiles.addAll(Arrays.asList(controller.getUserProfiles(userId,
                pubPK.getInstanceId(), node.getRightsDependsOn(), ObjectType.NODE)));
          }
        }
      }
      profiles = lProfiles.toArray(new String[lProfiles.size()]);
    }
    return KmeliaHelper.getProfile(profiles);
  }

  private String[] getAppProfiles(String userId, String appId) {
    return controller.getUserProfiles(userId, appId);
  }

  private PublicationBm getPublicationBm() {
    if (publicationBm == null) {
      try {
        setPublicationBm(EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
            PublicationBm.class));
      } catch (UtilException e) {
        throw new KmeliaRuntimeException("KmeliaSecurity.getPublicationBm()",
            SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_PUBLICATIONBM_HOME",
            e);
      }
    }
    return publicationBm;
  }

  public NodeService getNodeService() {
    return nodeService;
  }

  public KmeliaBm getKmeliaBm() {
    if (kmeliaBm == null) {
      try {
        setKmeliaBm(EJBUtilitaire.getEJBObjectRef(JNDINames.KMELIABM_EJBHOME, KmeliaBm.class));
      } catch (UtilException e) {
        throw new KmeliaRuntimeException("KmeliaSecurity.getKmeliaBm()",
            SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_KMELIABM_HOME", e);
      }
    }
    return kmeliaBm;
  }

  public synchronized boolean isCacheEnabled() {
    return cacheEnabled;
  }

  /**
   * @param publicationBm the publicationBm to set
   */
  void setPublicationBm(PublicationBm publicationBm) {
    this.publicationBm = publicationBm;
  }

  /**
   * @param kmeliaBm the KmeliaBm to set
   */
  void setKmeliaBm(KmeliaBm kmeliaBm) {
    this.kmeliaBm = kmeliaBm;
  }
}
