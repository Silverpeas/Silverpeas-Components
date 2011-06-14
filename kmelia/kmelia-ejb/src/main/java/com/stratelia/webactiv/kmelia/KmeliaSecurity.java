/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.stratelia.webactiv.kmelia;

import com.silverpeas.util.StringUtil;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.silverpeas.util.security.ComponentSecurity;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ObjectType;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaHelper;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
  private NodeBm nodeBm;
  private OrganizationController controller = null;
  private Map<String, Boolean> cache = Collections.synchronizedMap(new HashMap<String, Boolean>());
  private volatile boolean cacheEnabled = false;
  private ResourceLocator kmeliaSettings = new ResourceLocator("com.stratelia.webactiv.kmelia.settings.kmeliaSettings", "fr");

  public KmeliaSecurity() {
    this.controller = new OrganizationController();
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
      cache.put(objectId + objectType + componentId, Boolean.valueOf(available));
    }
  }

  private Boolean readFromCache(String objectId, String objectType, String componentId) {
    if (cacheEnabled) {
      return cache.get(objectId + objectType + componentId);
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
    if (!controller.isComponentAvailable(componentId, userId)) {
      return false;
    }

    if (isKmeliaObjectType(objectType)) {
      PublicationPK pk = new PublicationPK(objectId, componentId);
      // First, check if publication is available
      if (!isPublicationAvailable(pk, userId)) {
        return false;
      }
      // Then, check the publication's status
      PublicationDetail publication = null;
      try {
        publication = getPublicationBm().getDetail(pk);
      } catch (Exception e) {
        throw new KmeliaRuntimeException("KmeliaSecurity.isAccessAuthorized()",
            SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DOBTENIR_LA_PUBLICATION", e);
      }
      if (publication != null) {
        if (publication.isValid()) {
          return true;
        }
        if (publication.isValidationRequired()) {
          String profile = getProfile(userId, pk);
          if (user.isInRole(profile)) {
            return false;
          }
          if (writer.isInRole(profile)) {
            return publication.isPublicationEditor(userId);
          }
          return true;
        }
        if (publication.isRefused()) {
          String profile = getProfile(userId, pk);
          if (!user.isInRole(profile)) {
            return publication.isPublicationEditor(userId) || admin.isInRole(profile)
                || publisher.isInRole(profile);
          }
          return false;
        }
        if (publication.isDraft()) {
          String profile = getProfile(userId, pk);
          if (!user.isInRole(profile)) {
            if (isCoWritingEnable(componentId)
                  && isDraftVisibleWithCoWriting()
                  && !reader.isInRole(profile)) {
              return true;
            }
            else {
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
    String param = controller.getComponentParameterValue(componentId,CO_WRITING_PARAM);
    return StringUtil.getBooleanValue(param);
  }


  protected boolean isPublicationAvailable(PublicationPK pk, String userId) {
    Boolean fromCache = readFromCache(pk.getId(), PUBLICATION_TYPE, pk.getInstanceId());
    if (fromCache != null) {
      // Availabily already processed
      return fromCache.booleanValue();
    }

    boolean objectAvailable = false;
    if (isRightsOnTopicsEnabled(pk.getInstanceId())) {
      Collection<NodePK> fatherPKs = null;
      try {
        fatherPKs = getPublicationBm().getAllFatherPK(pk);
      } catch (Exception e) {
        SilverTrace.info("kmelia", "KmeliaSecurity.isPublicationAvailable",
            "kmelia.EX_IMPOSSIBLE_DOBTENIR_LA_PUBLICATION", "PubId = " + pk.toString());
        objectAvailable = false;
      }
      for (NodePK fatherPK : fatherPKs) {
        if (!fatherPK.isTrash()) {
          objectAvailable = isNodeAvailable(fatherPK, userId);
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

  private boolean isNodeAvailable(NodePK nodePK, String userId) {
    Boolean fromCache = readFromCache(nodePK.getId(), NODE_TYPE, nodePK.getInstanceId());
    if (fromCache != null) {
      // Availabily already processed
      return fromCache.booleanValue();
    }
    boolean objectAvailable = false;
    if (isRightsOnTopicsEnabled(nodePK.getInstanceId())) {
      NodeDetail node = null;
      try {
        node = getNodeBm().getHeader(nodePK, false);
      } catch (RemoteException e) {
        throw new KmeliaRuntimeException("KmeliaSecurity.isNodeAvailable()",
            SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DOBTENIR_LA_PUBLICATION", e);
      }
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
      profiles = controller.getUserProfiles(userId, pubPK.getInstanceId());
    } else {
      // get topic-level profiles
      Collection<NodePK> nodePKs;
      try {
        nodePKs = getPublicationBm().getAllFatherPK(pubPK);
      } catch (RemoteException e) {
        throw new KmeliaRuntimeException("KmeliaSecurity.getProfile()",
            SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DOBTENIR_LA_PUBLICATION", e);
      }
      List<String> lProfiles = new ArrayList<String>();
      for (NodePK nodePK : nodePKs) {
        NodeDetail node = null;
        try {
          node = getNodeBm().getHeader(nodePK);
        } catch (RemoteException e) {
          throw new KmeliaRuntimeException("KmeliaSecurity.getProfile()",
              SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DOBTENIR_LA_PUBLICATION", e);
        }
        if (node != null) {
          SilverTrace.debug("kmelia", "KmeliaSecurity.getProfile",
              "root.MSG_GEN_PARAM_VALUE", "nodePK = " + nodePK.toString());
          if (!node.haveRights()) {
            lProfiles.addAll(
                Arrays.asList(controller.getUserProfiles(userId, pubPK.getInstanceId())));
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

  private PublicationBm getPublicationBm() {
    if (publicationBm == null) {
      try {
        PublicationBmHome publicationBmHome = (PublicationBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.PUBLICATIONBM_EJBHOME, PublicationBmHome.class);
        setPublicationBm(publicationBmHome.create());
      } catch (Exception e) {
        throw new KmeliaRuntimeException("KmeliaSecurity.getPublicationBm()",
            SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_PUBLICATIONBM_HOME",
            e);
      }
    }
    return publicationBm;
  }

  public NodeBm getNodeBm() {
    if (nodeBm == null) {
      try {
        NodeBmHome nodeBmHome = (NodeBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
        setNodeBm(nodeBmHome.create());
      } catch (Exception e) {
        throw new KmeliaRuntimeException("KmeliaSecurity.getNodeBm()",
            SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_NODEBM_HOME", e);
      }
    }
    return nodeBm;
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
   * @param nodeBm the nodeBm to set
   */
  void setNodeBm(NodeBm nodeBm) {
    this.nodeBm = nodeBm;
  }
}
