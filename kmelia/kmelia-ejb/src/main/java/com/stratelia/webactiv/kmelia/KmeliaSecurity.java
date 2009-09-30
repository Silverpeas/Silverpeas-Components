package com.stratelia.webactiv.kmelia;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import com.silverpeas.util.security.ComponentSecurity;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ObjectType;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaHelper;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

public class KmeliaSecurity implements ComponentSecurity {

  private PublicationBm publicationBm = null;
  private NodeBm nodeBm = null;
  private OrganizationController controller = null;

  private Hashtable cache = null;
  private boolean cacheEnabled = false;

  public KmeliaSecurity() {
    this.controller = new OrganizationController();
  }

  public KmeliaSecurity(OrganizationController controller) {
    this.controller = controller;
  }

  public void enableCache() {
    if (cache != null)
      cache.clear();
    else
      cache = new Hashtable();
    cacheEnabled = true;
  }

  public void disableCache() {
    if (cache != null)
      cache.clear();
    cacheEnabled = false;
  }

  private Hashtable getCache() {
    if (cache == null)
      cache = new Hashtable();
    return cache;
  }

  private void writeInCache(String objectId, String objectType,
      String componentId, boolean available) {
    if (cacheEnabled)
      getCache().put(objectId + objectType + componentId,
          Boolean.valueOf(available));
  }

  private Boolean readFromCache(String objectId, String objectType,
      String componentId) {
    if (cacheEnabled)
      return (Boolean) getCache().get(objectId + objectType + componentId);

    return null;
  }

  public boolean isAccessAuthorized(String componentId, String userId,
      String objectId) {
    return isAccessAuthorized(componentId, userId, objectId, "Publication");
  }

  public boolean isAccessAuthorized(String componentId, String userId,
      String objectId, String objectType) {
    // first, check if user is able to access to component
    if (!controller.isComponentAvailable(componentId, userId))
      return false;

    if (objectType != null
        && ("Publication".equalsIgnoreCase(objectType)
            || objectType.startsWith("Attachment") || objectType
            .startsWith("Version"))) {
      PublicationPK pk = new PublicationPK(objectId, componentId);

      // First, check if publication is available
      if (!isPublicationAvailable(pk, userId))
        return false;

      // Then, check the publication's status
      PublicationDetail publication = null;
      try {
        publication = getPublicationBm().getDetail(pk);
      } catch (Exception e) {
        throw new KmeliaRuntimeException("KmeliaSecurity.isAccessAuthorized()",
            SilverpeasRuntimeException.ERROR,
            "kmelia.EX_IMPOSSIBLE_DOBTENIR_LA_PUBLICATION", e);
      }
      if (publication != null) {
        String status = publication.getStatus();
        if ("Valid".equalsIgnoreCase(status)) {
          return true;
        } else if ("ToValidate".equalsIgnoreCase(status)) {
          String profile = getProfile(userId, pk);
          if ("user".equalsIgnoreCase(profile))
            return false;
          else if ("writer".equalsIgnoreCase(profile))
            return userId.equals(publication.getUpdaterId())
                || userId.equals(publication.getCreatorId());
          else
            return true;
        } else if ("UnValidate".equalsIgnoreCase(status)
            || "Draft".equalsIgnoreCase(status)) {
          String profile = getProfile(userId, pk);
          if (!"user".equalsIgnoreCase(profile))
            return userId.equals(publication.getUpdaterId())
                || userId.equals(publication.getCreatorId());
          else
            return false;
        }
      }
    } else if (objectType != null && "Node".equalsIgnoreCase(objectType)) {
      NodePK pk = new NodePK(objectId, componentId);
      return isNodeAvailable(pk, userId);
    }
    return true;
  }

  public boolean isObjectAvailable(String componentId, String userId,
      String objectId, String objectType) {
    boolean objectAvailable = false;
    if (objectType != null
        && ("Publication".equalsIgnoreCase(objectType)
            || objectType.startsWith("Attachment") || objectType
            .startsWith("Version"))) {
      objectAvailable = isPublicationAvailable(new PublicationPK(objectId,
          componentId), userId);
    } else if ("Node".equalsIgnoreCase(objectType)) {
      objectAvailable = isNodeAvailable(new NodePK(objectId, componentId),
          userId);
    } else {
      objectAvailable = true;
    }
    return objectAvailable;
  }

  protected boolean isRightsOnTopicsEnable(String componentId) {
    String param = controller.getComponentParameterValue(componentId,
        "rightsOnTopics");
    return ("yes".equalsIgnoreCase(param));
  }

  protected boolean isPublicationAvailable(PublicationPK pk, String userId) {
    Boolean fromCache = readFromCache(pk.getId(), "Publication", pk
        .getInstanceId());
    if (fromCache != null) {
      // Availabily already processed
      return fromCache.booleanValue();
    }

    boolean objectAvailable = false;
    if (isRightsOnTopicsEnable(pk.getInstanceId())) {
      List fatherPKs = null;
      try {
        fatherPKs = (List) getPublicationBm().getAllFatherPK(pk);
      } catch (Exception e) {
        SilverTrace.info("kmelia", "KmeliaSecurity.isPublicationAvailable",
            "kmelia.EX_IMPOSSIBLE_DOBTENIR_LA_PUBLICATION", "PubId = "
                + pk.toString());
        objectAvailable = false;
      }

      NodePK fatherPK = null;
      for (int f = 0; !objectAvailable && fatherPKs != null
          && f < fatherPKs.size(); f++) {
        fatherPK = (NodePK) fatherPKs.get(f);
        if (!fatherPK.getId().equals("1"))
          objectAvailable = isNodeAvailable(fatherPK, userId);
      }
    } else {
      objectAvailable = true;
    }
    writeInCache(pk.getId(), "Publication", pk.getInstanceId(), objectAvailable);
    return objectAvailable;
  }

  private boolean isNodeAvailable(NodePK nodePK, String userId) {
    Boolean fromCache = readFromCache(nodePK.getId(), "Node", nodePK
        .getInstanceId());
    if (fromCache != null) {
      // Availabily already processed
      return fromCache.booleanValue();
    }

    boolean objectAvailable = false;
    if (isRightsOnTopicsEnable(nodePK.getInstanceId())) {
      NodeDetail node = null;

      try {
        node = getNodeBm().getHeader(nodePK, false);
      } catch (RemoteException e) {
        throw new KmeliaRuntimeException("KmeliaSecurity.isNodeAvailable()",
            SilverpeasRuntimeException.ERROR,
            "kmelia.EX_IMPOSSIBLE_DOBTENIR_LA_PUBLICATION", e);
      }

      if (node != null) {
        if (!node.haveRights())
          objectAvailable = true;
        else
          objectAvailable = controller.isObjectAvailable(node
              .getRightsDependsOn(), ObjectType.NODE, nodePK.getInstanceId(),
              userId);
      } else {
        objectAvailable = false;
      }
    } else {
      objectAvailable = true;
    }
    writeInCache(nodePK.getId(), "Node", nodePK.getInstanceId(),
        objectAvailable);
    return objectAvailable;
  }

  private String getProfile(String userId, PublicationPK pubPK) {
    String[] profiles;
    if (!isRightsOnTopicsEnable(pubPK.getInstanceId())) {
      // get component-level profiles
      profiles = controller.getUserProfiles(userId, pubPK.getInstanceId());
    } else {
      // get topic-level profiles
      List nodePKs;
      try {
        nodePKs = (List) getPublicationBm().getAllFatherPK(pubPK);
      } catch (RemoteException e) {
        throw new KmeliaRuntimeException("KmeliaSecurity.getProfile()",
            SilverpeasRuntimeException.ERROR,
            "kmelia.EX_IMPOSSIBLE_DOBTENIR_LA_PUBLICATION", e);
      }

      NodePK nodePK = null;
      NodeDetail node = null;
      List lProfiles = new ArrayList();
      for (int n = 0; nodePKs != null && n < nodePKs.size(); n++) {
        nodePK = (NodePK) nodePKs.get(n);
        try {
          node = getNodeBm().getHeader(nodePK);
        } catch (RemoteException e) {
          throw new KmeliaRuntimeException("KmeliaSecurity.getProfile()",
              SilverpeasRuntimeException.ERROR,
              "kmelia.EX_IMPOSSIBLE_DOBTENIR_LA_PUBLICATION", e);
        }
        if (node != null) {
          SilverTrace.debug("kmelia", "KmeliaSecurity.getProfile",
              "root.MSG_GEN_PARAM_VALUE", "nodePK = " + nodePK.toString());
          if (!node.haveRights()) {
            lProfiles.addAll(Arrays.asList(controller.getUserProfiles(userId,
                pubPK.getInstanceId())));
          } else {
            lProfiles.addAll(Arrays.asList(controller.getUserProfiles(userId,
                pubPK.getInstanceId(), node.getRightsDependsOn(),
                ObjectType.NODE)));
          }
        }
      }
      profiles = (String[]) lProfiles.toArray(new String[0]);
    }
    return KmeliaHelper.getProfile(profiles);
  }

  private PublicationBm getPublicationBm() {
    if (publicationBm == null) {
      try {
        PublicationBmHome publicationBmHome = (PublicationBmHome) EJBUtilitaire
            .getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
                PublicationBmHome.class);
        publicationBm = publicationBmHome.create();
      } catch (Exception e) {
        throw new KmeliaRuntimeException("KmeliaSecurity.getPublicationBm()",
            SilverpeasRuntimeException.ERROR,
            "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_PUBLICATIONBM_HOME", e);
      }
    }
    return publicationBm;
  }

  public NodeBm getNodeBm() {
    if (nodeBm == null) {
      try {
        NodeBmHome nodeBmHome = (NodeBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
        nodeBm = nodeBmHome.create();
      } catch (Exception e) {
        throw new KmeliaRuntimeException("KmeliaSecurity.getNodeBm()",
            SilverpeasRuntimeException.ERROR,
            "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_NODEBM_HOME", e);
      }
    }
    return nodeBm;
  }

}