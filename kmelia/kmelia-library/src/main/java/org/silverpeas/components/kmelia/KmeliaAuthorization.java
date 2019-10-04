/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.components.kmelia;

import org.silverpeas.components.kmelia.model.KmeliaRuntimeException;
import org.silverpeas.components.kmelia.service.KmeliaHelper;
import org.silverpeas.components.kmelia.service.KmeliaService;
import org.silverpeas.core.admin.ObjectType;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.security.authorization.ComponentAuthorization;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.silverpeas.core.admin.user.model.SilverpeasRole.*;

/**
 * Kmelia security provides a way to check the rights of a user to access the content of a Kmelia
 * instance (publications, ...).
 */
public class KmeliaAuthorization implements ComponentAuthorization {

  private static final String CO_WRITING_PARAM = "coWriting";
  private static final String DRAFT_VISIBLE_WITH_CO_WRITING_PARAM = "draftVisibleWithCoWriting";
  public static final String PUBLICATION_TYPE = "Publication";
  public static final String NODE_TYPE = "Node";
  public static final String RIGHTS_ON_TOPIC_PARAM = "rightsOnTopics";
  private PublicationService publicationService;
  private NodeService nodeService = NodeService.get();
  private KmeliaService kmeliaService;
  private OrganizationController controller = null;
  private Map<String, Boolean> cache = Collections.synchronizedMap(new HashMap<>());
  private volatile boolean cacheEnabled = false;
  private SettingBundle kmeliaSettings = ResourceLocator.getSettingBundle(
      "org.silverpeas.kmelia.settings.kmeliaSettings");

  public KmeliaAuthorization() {
    this.controller = OrganizationControllerProvider.getOrganisationController();
  }

  public KmeliaAuthorization(OrganizationController controller) {
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

  private Optional<Boolean> readFromCache(String objectId, String objectType, String componentId) {
    if (cacheEnabled) {
      return Optional.ofNullable(cache.get(objectId + objectType + componentId));
    }
    return Optional.empty();
  }

  private void writeInCache(String componentId, boolean available) {
    if (cacheEnabled) {
      cache.put(componentId, available);
    }
  }

  private Optional<Boolean> readFromCache(String componentId) {
    if (cacheEnabled) {
      return Optional.ofNullable(cache.get(componentId));
    }
    return Optional.empty();
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
      Optional<Boolean> profile = checkPublicationStatus(componentId, userId, pk, publication);
      if (profile.isPresent()) {
        return profile.get();
      }
    } else if (NODE_TYPE.equalsIgnoreCase(objectType)) {
      NodePK pk = new NodePK(objectId, componentId);
      return isNodeAvailable(pk, userId);
    }
    return true;
  }

  private Optional<Boolean> checkPublicationStatus(final String componentId, final String userId,
      final PublicationPK pk, final PublicationDetail publication) {
    if (publication != null) {
      String profile = getProfile(userId, pk);
      if (!getKmeliaService().isPublicationVisible(publication, SilverpeasRole.from(profile),
          userId)) {
        return Optional.of(false);
      }
      if (publication.isValid()) {
        return Optional.of(true);
      }
      if (publication.isValidationRequired()) {
        return canUserValidate(componentId, userId, publication, profile);
      }
      if (publication.isRefused()) {
        if (!user.isInRole(profile)) {
          return Optional.of(
              publication.isPublicationEditor(userId) || isCoWritingEnable(componentId) ||
                  admin.isInRole(profile) || publisher.isInRole(profile));
        }
        return Optional.of(false);
      }
      if (publication.isDraft()) {
        return checkWenPublicationIsInDraft(componentId, userId, publication, profile);
      }
    }
    return Optional.empty();
  }

  private Optional<Boolean> checkWenPublicationIsInDraft(final String componentId,
      final String userId, final PublicationDetail publication, final String profile) {
    if (!user.isInRole(profile)) {
      if (isCoWritingEnable(componentId) && isDraftVisibleWithCoWriting() &&
          !reader.isInRole(profile)) {
        return Optional.of(true);
      } else {
        return Optional.of(publication.isPublicationEditor(userId));
      }
    }
    return Optional.of(false);
  }

  private Optional<Boolean> canUserValidate(final String componentId, final String userId,
      final PublicationDetail publication, final String profile) {
    if (user.isInRole(profile)) {
      return Optional.of(false);
    }
    if (writer.isInRole(profile)) {
      return Optional.of(
          publication.isPublicationEditor(userId) || isCoWritingEnable(componentId));
    }
    return Optional.of(true);
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
    String key = RIGHTS_ON_TOPIC_PARAM+componentId;
    Boolean enabled = cache.get(key);
    if (enabled == null) {
      enabled = StringUtil.getBooleanValue(
          controller.getComponentParameterValue(componentId, RIGHTS_ON_TOPIC_PARAM));
      if (cacheEnabled) {
        cache.put(key, enabled);
      }
    }
    return enabled;
  }

  protected boolean isDraftVisibleWithCoWriting() {
    return kmeliaSettings.getBoolean(DRAFT_VISIBLE_WITH_CO_WRITING_PARAM, false);
  }

  protected boolean isCoWritingEnable(String componentId) {
    String param = controller.getComponentParameterValue(componentId, CO_WRITING_PARAM);
    return StringUtil.getBooleanValue(param);
  }

  private boolean isComponentAvailable(String componentId, String userId) {
    Optional<Boolean> fromCache = readFromCache(componentId);
    if (fromCache.isPresent()) {
      // Availabily already processed
      return fromCache.get();
    }

    boolean available = controller.isComponentAvailable(componentId, userId);
    writeInCache(componentId, available);
    return available;
  }

  protected boolean isPublicationAvailable(PublicationPK pk, String userId) {
    Optional<Boolean> fromCache = readFromCache(pk.getId(), PUBLICATION_TYPE, pk.getInstanceId());
    if (fromCache.isPresent()) {
      // Availabily already processed
      return fromCache.get();
    }

    boolean objectAvailable = false;
    Collection<NodePK> fatherPKs = getPublicationFolderPKs(pk);
    if (fatherPKs == null) {
      return false;
    }
    if (isInBasket(fatherPKs)) {
      Optional<Boolean> publication = isPublicationAccessibleInBasket(pk, userId);
      if (publication.isPresent()) {
        return publication.get();
      }
    } else if (isRightsOnTopicsEnabled(pk.getInstanceId())) {
      objectAvailable = isPublicationAccessibleInTopic(pk, userId, objectAvailable, fatherPKs);
    } else {
      objectAvailable = true;
    }
    writeInCache(pk.getId(), PUBLICATION_TYPE, pk.getInstanceId(), objectAvailable);
    return objectAvailable;
  }

  private boolean isPublicationAccessibleInTopic(final PublicationPK pk, final String userId,
      boolean objectAvailable, final Collection<NodePK> fatherPKs) {
    for (NodePK fatherPK : fatherPKs) {
      if (!fatherPK.isTrash()) {
        try {
          objectAvailable = isNodeAvailable(fatherPK, userId);
        } catch (Exception e) {
          // don't throw exception, log only error
          SilverLogger.getLogger(this)
              .error("Node (" + fatherPK.getId() + ", " + fatherPK.getInstanceId() +
                  ") no more exist but still referenced by a publication (" + pk.getId() + ", " +
                  pk.getInstanceId() + ")", e);
          objectAvailable = false;
        }
      } else {
        objectAvailable = true;
      }
      if (objectAvailable) {
        break;
      }
    }
    return objectAvailable;
  }

  private Optional<Boolean> isPublicationAccessibleInBasket(final PublicationPK pk, final String userId) {
    SilverpeasRole profile = SilverpeasRole.from(KmeliaHelper.getProfile(getAppProfiles(userId,
        pk.getInstanceId())));
    if (SilverpeasRole.READER_ROLES.contains(profile)) {
      // readers do not see basket content
      return Optional.of(false);
    } else if (SilverpeasRole.admin == profile) {
      // admins see basket content
      return Optional.of(true);
    } else {
      // others profiles see only theirs publications
      PublicationDetail publication = getPublicationDetail(pk);
      if (publication != null) {
        return Optional.of(publication.isPublicationEditor(userId));
      }
    }
    return Optional.empty();
  }

  private Collection<NodePK> getPublicationFolderPKs(PublicationPK pk) {
    Collection<NodePK> fatherPKs = null;
    try {
      fatherPKs = getPublicationService().getAllFatherPKInSamePublicationComponentInstance(pk);
    } catch (Exception e) {
      SilverLogger.getLogger(this).warn(e);
    }
    return fatherPKs;
  }

  private boolean isInBasket(Collection<NodePK> pks) {
    Iterator<NodePK> iterator = pks.iterator();
    if (iterator.hasNext()) {
      return iterator.next().isTrash();
    }
    return false;
  }

  private PublicationDetail getPublicationDetail(PublicationPK pk) {
    try {
      return getPublicationService().getDetail(pk);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  private boolean isNodeAvailable(NodePK nodePK, String userId) {
    Optional<Boolean> fromCache = readFromCache(nodePK.getId(), NODE_TYPE, nodePK.getInstanceId());
    if (fromCache.isPresent()) {
      // Availability already processed
      return fromCache.get();
    }
    boolean objectAvailable;
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
      Collection<NodePK> nodePKs = getPublicationService().getAllFatherPKInSamePublicationComponentInstance(pubPK);
      List<String> lProfiles = new ArrayList<>();
      for (NodePK nodePK : nodePKs) {
        NodeDetail node = getNodeService().getHeader(nodePK);
        if (node != null) {
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

  private PublicationService getPublicationService() {
    if (publicationService == null) {
        setPublicationService(ServiceProvider.getService(PublicationService.class));
    }
    return publicationService;
  }

  public NodeService getNodeService() {
    return nodeService;
  }

  public KmeliaService getKmeliaService() {
    if (kmeliaService == null) {
      kmeliaService = ServiceProvider.getService(KmeliaService.class);
    }
    return kmeliaService;
  }

  public synchronized boolean isCacheEnabled() {
    return cacheEnabled;
  }

  /**
   * @param publicationService the publicationBm to set
   */
  void setPublicationService(PublicationService publicationService) {
    this.publicationService = publicationService;
  }

}
