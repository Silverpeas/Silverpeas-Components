/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.resourcemanager.control;

import com.silverpeas.annotation.Service;
import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import org.silverpeas.util.ForeignPK;
import org.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.date.Period;
import org.silverpeas.resourcemanager.model.Category;
import org.silverpeas.resourcemanager.model.Reservation;
import org.silverpeas.resourcemanager.model.ReservedResource;
import org.silverpeas.resourcemanager.model.Resource;
import org.silverpeas.resourcemanager.model.ResourceStatus;
import org.silverpeas.resourcemanager.model.ResourceValidator;
import org.silverpeas.resourcemanager.services.CategoryService;
import org.silverpeas.resourcemanager.services.ReservationService;
import org.silverpeas.resourcemanager.services.ReservedResourceService;
import org.silverpeas.resourcemanager.services.ResourceService;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;
import org.silverpeas.search.indexEngine.model.IndexEngineProxy;
import org.silverpeas.search.indexEngine.model.IndexEntryPK;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 */
@Service
@Transactional
public class SimpleResourcesManager implements ResourcesManager, Serializable {

  private static final long serialVersionUID = -8053955818376554252L;
  @Inject
  private CategoryService categoryService;
  @Inject
  private ResourceService resourceService;
  @Inject
  private ReservationService reservationService;
  @Inject
  private ReservedResourceService reservedResourceService;

  /**
   * Creating a new resource category.
   * @param category
   */
  @Override
  public void createCategory(Category category) {
    categoryService.createCategory(category);
    createCategoryIndex(category);
  }

  @Override
  public List<Category> getCategories(String instanceId) {
    return categoryService.getCategories(instanceId);
  }

  @Override
  public Category getCategory(Long id) {
    return categoryService.getCategory(id);
  }

  @Override
  public void updateCategory(Category category) {
    categoryService.updateCategory(category);
  }

  @Override
  public void deleteCategory(Long id, String componentId) {
    // First delete all resources of category
    List<Resource> resources = getResourcesByCategory(id);
    for (Resource resource : resources) {
      resourceService.deleteResource(resource.getId());
      deleteIndex(resource.getId(), "Resource", componentId);
    }
    // Then delete category itself
    categoryService.deleteCategory(id);
    deleteIndex(id, "Category", componentId);
  }

  /**
   * @param resource
   * @return
   */
  @Override
  public void createResource(Resource resource) {
    resourceService.createResource(resource);
    createResourceIndex(resource);
  }

  @Override
  public Resource getResource(Long id) {
    return resourceService.getResource(id);
  }

  @Override
  public List<Resource> getResourcesByCategory(Long categoryId) {
    return resourceService.getResourcesByCategory(categoryId);
  }

  @Override
  public void deleteResource(Long id, String componentId) {
    resourceService.deleteResource(id);
    deleteIndex(id, "Resource", componentId);
  }

  @Override
  public List<Resource> getResourcesReservable(String instanceId, Date startDate, Date endDate) {
    return resourceService.listAvailableResources(instanceId, String.valueOf(startDate.getTime()),
        String.valueOf(endDate.getTime()));
  }

  @Override
  public List<Resource> getResourcesOfReservation(String instanceId, Long reservationId) {
    return resourceService.listResourcesOfReservation(reservationId);
  }

  @Override
  public void updateReservation(Reservation reservation, List<Long> resourceIds,
      boolean updateDate) {
    List<ReservedResource> reservedResources = reservedResourceService.
        findAllReservedResourcesOfReservation(reservation.getId());
    Map<Long, ReservedResource> oldReservedResources =
        new HashMap<Long, ReservedResource>(reservedResources.
            size());
    for (ReservedResource reservedResource : reservedResources) {
      oldReservedResources.put(reservedResource.getResourceId(), reservedResource);
    }
    boolean refused = false;
    boolean forValidation = false;
    String reservationStatus = ResourceStatus.STATUS_VALIDATE;
    for (Long resourceId : resourceIds) {
      ReservedResource reservedResource = oldReservedResources.remove(resourceId);
      boolean isCreation = (reservedResource == null);
      if (isCreation || updateDate) {
        if (reservedResource == null) {
          reservedResource = new ReservedResource();
          reservedResource.setReservationId(reservation.getId());
          reservedResource.setResourceId(resourceId);
        }
        if (resourceService.isManager(Long.parseLong(reservation.getUserId()), resourceId)) {
          reservedResource.setStatus(ResourceStatus.STATUS_VALIDATE);
        } else if (resourceService.getManagers(resourceId).isEmpty()) {
          reservedResource.setStatus(ResourceStatus.STATUS_VALIDATE);
        } else {
          reservedResource.setStatus(ResourceStatus.STATUS_FOR_VALIDATION);
        }
        if (isCreation) {
          reservedResourceService.create(reservedResource);
        } else {
          reservedResource.setResource(null);
          reservedResource.setReservation(null);
          reservedResourceService.update(reservedResource);
        }
      }
      if (reservedResource.isValidationRequired()) {
        forValidation = true;
      }
      if (reservedResource.isRefused()) {
        refused = true;
      }
    }
    if (forValidation) {
      reservationStatus = ResourceStatus.STATUS_FOR_VALIDATION;
    }
    if (refused) {
      reservationStatus = ResourceStatus.STATUS_REFUSED;
    }
    for (ReservedResource oldReservedResource : oldReservedResources.values()) {
      reservedResourceService.delete(oldReservedResource);
    }
    reservation.setStatus(reservationStatus);
    reservationService.updateReservation(reservation);
    createReservationIndex(reservation);
  }

  /**
   * Get from the given aimed resources those that are unavailable on the given period.
   * @param instanceId
   * @param resources
   * @param startDate
   * @param endDate
   * @return
   */
  @Override
  public List<Resource> getReservedResources(String instanceId, List<Long> resources,
      Date startDate, Date endDate) {
    return getReservedResources(instanceId, resources, startDate, endDate, null);
  }

  /**
   * Get from the given aimed resources those that are unavailable on the given period. Resources
   * attached to reservationIdToSkip are excluded (but can still be returned if they are attached
   * to
   * another reservation on the given period).
   * @param instanceId
   * @param aimedResourceIds
   * @param startDate
   * @param endDate
   * @param reservationIdToSkip
   * @return
   */
  @Override
  public List<Resource> getReservedResources(String instanceId, List<Long> aimedResourceIds,
      Date startDate, Date endDate, Long reservationIdToSkip) {
    String startPeriod = String.valueOf(startDate.getTime());
    String endPeriod = String.valueOf(endDate.getTime());
    return resourceService
        .findAllReservedResources((reservationIdToSkip != null ? reservationIdToSkip : -1),
            aimedResourceIds, startPeriod, endPeriod);
  }

  @Override
  public List<Reservation> getReservations(String instanceId) {
    return reservationService.findAllReservations(instanceId);
  }

  @Override
  public List<Reservation> getUserReservations(String instanceId, String userId) {
    return reservationService.findAllReservations(instanceId);
  }

  @Override
  public Reservation getReservation(String instanceId, Long reservationId) {
    return reservationService.getReservation(reservationId);
  }

  @Override
  public void deleteReservation(Long id, String componentId) {
    deleteIndex(id, "Reservation", componentId);
    List<SimpleDocument> documents = AttachmentServiceFactory.getAttachmentService()
        .listDocumentsByForeignKey(new ForeignPK(id.toString(), componentId), null);
    for (SimpleDocument document : documents) {
      AttachmentServiceFactory.getAttachmentService().deleteAttachment(document);
    }
    reservationService.deleteReservation(id);
  }

  @Override
  public List<Reservation> getReservationForValidation(String instanceId, String userId,
      final Period period) {
    String[] searchPeriod = buildSearchPeriod(period);
    return reservationService
        .findAllReservationsForValidation(instanceId, Long.parseLong(userId), searchPeriod[0],
            searchPeriod[1]);
  }

  @Override
  public List<Reservation> getReservationOfUser(String instanceId, Integer userId,
      final Period period) {
    String[] searchPeriod = buildSearchPeriod(period);
    return reservationService
        .findAllReservationsInRange(instanceId, userId, searchPeriod[0], searchPeriod[1]);
  }

  @Override
  public List<Reservation> getReservationWithResourcesOfCategory(final String instanceId,
      Integer userId, final Period period, Long categoryId) {
    String[] searchPeriod = buildSearchPeriod(period);
    return reservationService
        .findAllReservationsForCategoryInRange(instanceId, userId, categoryId, searchPeriod[0],
            searchPeriod[1]);
  }

  @Override
  public List<Reservation> getReservationWithResource(final String instanceId, Integer userId,
      final Period period, final Long resourceId) {
    String[] searchPeriod = buildSearchPeriod(period);
    return reservationService
        .findAllReservationsForResourceInRange(instanceId, userId, resourceId, searchPeriod[0],
            searchPeriod[1]);
  }

  /**
   * Construct the period of search.
   * @param period
   */
  private String[] buildSearchPeriod(Period period) {
    return new String[]{String.valueOf(period.getBeginDate().getTime()),
        String.valueOf(period.getEndDate().getTime())};
  }

  @Override
  public String getResourceOfReservationStatus(Long resourceId, Long reservationId) {
    ReservedResource reserved =
        reservedResourceService.getReservedResource(resourceId, reservationId);
    return reserved.getStatus();
  }

  private void createCategoryIndex(Category category) {
    SilverTrace.info("resourceManager", "resourceManagerBmEJB.createIndex_Category()",
        "root.MSG_GEN_ENTER_METHOD", "category = " + category);
    if (category != null) {
      FullIndexEntry indexEntry = new FullIndexEntry(category.getInstanceId(), "Category", category.
          getIdAsString());
      indexEntry.setTitle(category.getName());
      indexEntry.setPreView(category.getDescription());
      if (category.getUpdateDate() != null) {
        indexEntry.setCreationDate(category.getUpdateDate());
      } else {
        indexEntry.setCreationDate(category.getCreationDate());
      }
      indexEntry.setCreationUser(category.getCreaterId());
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  private void createResourceIndex(Resource resource) {
    if (resource != null) {
      SilverTrace.info("resourceManager", "resourceManagerBmEJB.createIndex_Resource()",
          "root.MSG_GEN_ENTER_METHOD", "resource = " + resource.getManagers().toString());
      SilverTrace.info("resourceManager", "resourceManagerBmEJB.createIndex_Resource()",
          "root.MSG_GEN_ENTER_METHOD", "resource = " + resource.toString());
      // Index the Reservation
      FullIndexEntry indexEntry =
          new FullIndexEntry(resource.getInstanceId(), "Resource", resource.getIdAsString());
      indexEntry.setTitle(resource.getName());
      indexEntry.setPreView(resource.getDescription());
      if (resource.getUpdateDate() != null) {
        indexEntry.setCreationDate(resource.getUpdateDate());
      } else {
        indexEntry.setCreationDate(resource.getCreationDate());
      }
      indexEntry.setCreationUser(resource.getCreaterId());
      Long categoryId = resource.getCategoryId();
      if (categoryId != null) {
        Category category = getCategory(categoryId);
        if (category != null) {
          String xmlFormName = category.getForm();
          if (StringUtil.isDefined(xmlFormName)) {
            indexResourceForm(resource, indexEntry, xmlFormName);
          }
        }
      }

      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  private void indexResourceForm(Resource resource, FullIndexEntry indexEntry, String xmlFormName) {
    String xmlFormShortName =
        xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName.indexOf('.'));
    PublicationTemplate pubTemplate;
    try {
      pubTemplate = PublicationTemplateManager.getInstance().getPublicationTemplate(resource.
          getInstanceId() + ":" + xmlFormShortName);
      RecordSet set = pubTemplate.getRecordSet();
      set.indexRecord(String.valueOf(resource.getId()), xmlFormName, indexEntry);
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException("ResourceManagerBmEJB.createIndex_Resource()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_CREATE_INDEX_FAILED", e);
    }
  }

  private void createReservationIndex(Reservation reservation) {
    SilverTrace
        .info("resourceManager", "resourceManagerBmEJB.createIndex()", "root.MSG_GEN_ENTER_METHOD",
            "reservation = " + reservation);
    if (reservation != null) {
      FullIndexEntry indexEntry = new FullIndexEntry(reservation.getInstanceId(), "Reservation",
          reservation.getIdAsString());
      indexEntry.setTitle(reservation.getEvent());
      indexEntry.setPreView(reservation.getReason());
      indexEntry.setCreationDate(reservation.getCreationDate());
      indexEntry.setCreationUser(reservation.getUserId());
      indexEntry.setKeyWords(reservation.getPlace());
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  private void deleteIndex(Long objectId, String objectType, String componentId) {
    if (objectId != null) {
      IndexEntryPK indexEntry = new IndexEntryPK(componentId, objectType, String.valueOf(objectId));
      IndexEngineProxy.removeIndexEntry(indexEntry);
    }
  }

  @Override
  public void indexResourceManager(String instanceId) {
    List<Reservation> listOfReservation = getReservations(instanceId);
    if (listOfReservation != null) {
      for (Reservation reservation : listOfReservation) {
        try {
          createReservationIndex(reservation);
        } catch (Exception e) {
          throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.indexResourceManager()",
              SilverpeasRuntimeException.ERROR, "resourcesManager.MSG_INDEXRESERVATIONS", e);
        }
      }
    }
  }

  @Override
  public List<ResourceValidator> getManagers(long resourceId) {
    return resourceService.getManagers(resourceId);
  }

  @Override
  public void saveReservation(Reservation reservation, List<Long> resourceIds) {
    reservationService.createReservation(reservation, resourceIds);
    createReservationIndex(reservation);
  }

  @Override
  public void updateReservedResourceStatus(long reservationId, long resourceId, String status) {
    ReservedResource reservedResource =
        reservedResourceService.getReservedResource(resourceId, reservationId);
    if (reservedResource != null) {
      reservedResource.setStatus(status);
      reservedResourceService.update(reservedResource);
      Reservation reservation = reservedResource.getReservation();
      reservation.setStatus(reservationService.computeReservationStatus(reservation));
      reservationService.updateReservation(reservation);
    }
  }

  @Override
  public boolean isManager(long userId, long resourceId) {
    return resourceService.isManager(userId, resourceId);
  }

  @Override
  public void updateResource(Resource updatedResource, List<Long> managerIds) {
    Resource resource = getResource(updatedResource.getId());
    resource.merge(updatedResource);
    resource.getManagers().clear();
    for (Long managerId : managerIds) {
      ResourceValidator validator = new ResourceValidator(resource.getId(), managerId);
      if (!resource.getManagers().contains(validator)) {
        resource.getManagers().add(validator);
      }
    }
    resourceService.updateResource(resource);
    createResourceIndex(resource);
  }
}
