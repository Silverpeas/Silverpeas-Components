/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.resourcemanager.control;

import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.inject.Inject;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author
 */
@Service
@Transactional
public class SimpleResourcesManager implements ResourcesManager, Serializable {

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
   *
   * @param category
   */
  @Override
  public void createCategory(Category category) {
    String id = categoryService.createCategory(category);
    category.setId(id);
    createCategoryIndex(category);
  }

  @Override
  public List<Category> getCategories(String instanceId) {
    return categoryService.getCategories(instanceId);
  }

  @Override
  public Category getCategory(String id) {
    return categoryService.getCategory(id);
  }

  @Override
  public void updateCategory(Category category) {
    categoryService.updateCategory(category);
  }

  @Override
  public void deleteCategory(String id, String componentId) {
    // First delete all resources of category
    List<Resource> resources = getResourcesByCategory(id);
    for (Resource resource : resources) {
      resourceService.deleteResource(Integer.parseInt(resource.getId()));
      deleteIndex("Resource", resource.getId(), componentId);
    }
    // Then delete category itself
    categoryService.deleteCategory(id);
    deleteIndex(id, "Category", componentId);
  }

  /**
   *
   * @param resource
   * @return
   */
  @Override
  public String createResource(Resource resource) {
    String id = resourceService.createResource(resource);
    resource.setId(id);
    createResourceIndex(resource);
    return id;
  }

  @Override
  public void updateResource(Resource updatedResource) {
    Resource resource = getResource(updatedResource.getId());
    resource.merge(updatedResource);
    resourceService.updateResource(resource);
    createResourceIndex(resource);
  }

  @Override
  public Resource getResource(String id) {
    return resourceService.getResource(Integer.parseInt(id));
  }

  @Override
  public List<Resource> getResourcesByCategory(String categoryId) {
    return resourceService.getResourcesByCategory(Long.parseLong(categoryId));
  }

  @Override
  public void deleteResource(String id, String componentId) {
    resourceService.deleteResource(Integer.parseInt(id));
    deleteIndex(id, "Resource", componentId);
  }

  @Override
  public List<Resource> getResourcesReservable(String instanceId, Date startDate, Date endDate) {
    return resourceService.listAvailableResources(instanceId,
        String.valueOf(startDate.getTime()), String.valueOf(endDate.getTime()));
  }

  @Override
  public List<Resource> getResourcesofReservation(String instanceId, String reservationId) {
    return resourceService.listResourcesOfReservation(Long.parseLong(
        reservationId));

  }

  @Override
  public void updateReservation(Reservation reservationCourante, String listReservation,
      boolean updateDate) {
    List<ReservedResource> reservedResources = reservedResourceService.
        findAllReservedResourcesOfReservation(reservationCourante.getIntegerId());
    Map<Long, ReservedResource> oldReservedResources = new HashMap<Long, ReservedResource>(reservedResources.
        size());
    for (ReservedResource reservedResource : reservedResources) {
      oldReservedResources.put(reservedResource.getResourceId(), reservedResource);
    }
    StringTokenizer tokenizer = new StringTokenizer(listReservation, ",");
    boolean refused = false;
    boolean forValidation = false;
    String reservationStatus = ResourceStatus.STATUS_VALIDATE;
    while (tokenizer.hasMoreTokens()) {
      Long idResource = Long.parseLong(tokenizer.nextToken());
      ReservedResource reservedResource = null;
      if (!updateDate && oldReservedResources.containsKey(idResource)) {
        reservedResource = oldReservedResources.get(idResource);
        oldReservedResources.remove(idResource);
      }
      if (reservedResource == null) {
        reservedResource = new ReservedResource();
        reservedResource.setReservationId(reservationCourante.getIntegerId());
        reservedResource.setResourceId(idResource);
        if (resourceService.isManager(Long.parseLong(reservationCourante.getUserId()), idResource)) {
          reservedResource.setStatus(ResourceStatus.STATUS_VALIDATE);
        } else if (resourceService.getManagers(idResource).isEmpty()) {
          reservedResource.setStatus(ResourceStatus.STATUS_VALIDATE);
        } else {
          reservedResource.setStatus(ResourceStatus.STATUS_FOR_VALIDATION);
        }
        reservedResourceService.create(reservedResource);
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
    reservationCourante.setStatus(reservationStatus);
    reservationService.updateReservation(reservationCourante);
    createReservationIndex(reservationCourante);
  }

  /**
   *
   * @param instanceId
   * @param listeReservation
   * @param startDate
   * @param endDate
   * @return
   */
  @Override
  public List<Resource> verificationReservation(String instanceId, String listeReservation,
      Date startDate, Date endDate) {
    return verificationNewDateReservation(instanceId, listeReservation, startDate, endDate, "-1");
  }

  /**
   *
   * @param instanceId
   * @param listeReservation
   * @param startDate
   * @param endDate
   * @param reservationId
   * @return
   */
  @Override
  public List<Resource> verificationNewDateReservation(String instanceId,
      String listeReservation, Date startDate, Date endDate, String reservationId) {
    StringTokenizer tokenizer = new StringTokenizer(listeReservation, ",");
    int currentReservationId = -1;
    String startPeriod = String.valueOf(startDate.getTime());
    String endPeriod = String.valueOf(endDate.getTime());
    if (StringUtil.isInteger(reservationId)) {
      currentReservationId = Integer.parseInt(reservationId);
    }
    List<Long> futureReservedResourceIds = new ArrayList<Long>();
    while (tokenizer.hasMoreTokens()) {
      futureReservedResourceIds.add(Long.parseLong(tokenizer.nextToken()));
    }
    return resourceService.findAllResourcesWithProblem(
        currentReservationId, futureReservedResourceIds, startPeriod, endPeriod);
  }

  @Override
  public List<Reservation> getReservations(String instanceId) {
    List<Reservation> reservations = reservationService.findAllReservations(
        instanceId);
    return reservations;
  }

  @Override
  public List<Reservation> getUserReservations(String instanceId, String userId) {
    List<Reservation> reservations = reservationService.findAllReservations(
        instanceId);
    return reservations;
  }

  @Override
  public Reservation getReservation(String instanceId, String reservationId) {
    Reservation reservation = reservationService.getReservation(Integer.parseInt(reservationId));
    return reservation;
  }

  @Override
  public void deleteReservation(String id, String componentId) {
    deleteIndex(id, "Reservation", componentId);
    AttachmentController.deleteAttachmentByCustomerPK(new ForeignPK(id, componentId));
    reservationService.deleteReservation(Integer.parseInt(id));
  }

  @Override
  public List<Reservation> getMonthReservation(String instanceId, Date monthDate,
      String userId) {
    String endDate = String.valueOf(DateUtil.getEndDateOfMonth(monthDate).getTime());
    String beginDate = String.valueOf(DateUtil.getFirstDateOfMonth(monthDate).getTime());
    return reservationService.findAllReservationsInRange(instanceId, beginDate, endDate);
  }

  @Override
  public List<Reservation> getReservationForValidation(String instanceId, Date monthDate,
      String userId) {
    String endDate = String.valueOf(DateUtil.getEndDateOfMonth(monthDate).getTime());
    String beginDate = String.valueOf(DateUtil.getFirstDateOfMonth(monthDate).getTime());
    List<Reservation> reservations = reservationService.findAllReservationsForValidation(instanceId,
        Long.parseLong(userId), beginDate, endDate);
    return reservations;
  }

  @Override
  public List<Reservation> getMonthReservationOfUser(String instanceId, Date monthDate,
      Integer userId) {
    String endDate = String.valueOf(DateUtil.getEndDateOfMonth(monthDate).getTime());
    String beginDate = String.valueOf(DateUtil.getFirstDateOfMonth(monthDate).getTime());
    return reservationService.findAllReservationsForUserInRange(instanceId, userId,
        beginDate, endDate);
  }

  @Override
  public List<Reservation> getMonthReservationOfCategory(Date monthDate, String idCategory) {
    String endDate = String.valueOf(DateUtil.getEndDateOfMonth(monthDate).getTime());
    String beginDate = String.valueOf(DateUtil.getFirstDateOfMonth(monthDate).getTime());
    return reservationService.findAllReservationsForCategoryInRange(Long.parseLong(idCategory),
        beginDate, endDate);
  }

  @Override
  public List<Reservation> listReservationsOfMonthInCategoryForUser(Date monthDate,
      String idCategory, String userId) {
    String endDate = String.valueOf(DateUtil.getEndDateOfMonth(monthDate).getTime());
    String beginDate = String.valueOf(DateUtil.getFirstDateOfMonth(monthDate).getTime());
    return reservationService.findAllReservationsForCategoryInRange(Long.parseLong(idCategory),
        beginDate, endDate);
  }

  @Override
  public String getResourceOfReservationStatus(String resourceId, String reservationId) {
    ReservedResource reserved = reservedResourceService.getReservedResource(
        Integer.parseInt(resourceId), Integer.parseInt(reservationId));
    return reserved.getStatus();
  }

  private void createCategoryIndex(Category category) {
    SilverTrace.info("resourceManager", "resourceManagerBmEJB.createIndex_Category()",
        "root.MSG_GEN_ENTER_METHOD", "category = " + category);
    if (category != null) {
      FullIndexEntry indexEntry = new FullIndexEntry(category.getInstanceId(), "Category", category.
          getId());
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
      FullIndexEntry indexEntry = new FullIndexEntry(resource.getInstanceId(), "Resource",
          resource.getId());
      indexEntry.setTitle(resource.getName());
      indexEntry.setPreView(resource.getDescription());
      if (resource.getUpdateDate() != null) {
        indexEntry.setCreationDate(resource.getUpdateDate());
      } else {
        indexEntry.setCreationDate(resource.getCreationDate());
      }
      indexEntry.setCreationUser(resource.getCreaterId());
      String categoryId = resource.getCategoryId();
      if (StringUtil.isDefined(categoryId)) {
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

  private void indexResourceForm(Resource resource, FullIndexEntry indexEntry,
      String xmlFormName) {
    String xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf('/') + 1,
        xmlFormName.indexOf('.'));
    PublicationTemplate pubTemplate;
    try {
      pubTemplate = PublicationTemplateManager.getInstance().getPublicationTemplate(resource.
          getInstanceId() + ":" + xmlFormShortName);
      RecordSet set = pubTemplate.getRecordSet();
      set.indexRecord(resource.getId(), xmlFormName, indexEntry);
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourceManagerBmEJB.createIndex_Resource()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_CREATE_INDEX_FAILED", e);
    }
  }

  private void createReservationIndex(Reservation reservation) {
    SilverTrace.info("resourceManager", "resourceManagerBmEJB.createIndex()",
        "root.MSG_GEN_ENTER_METHOD", "reservation = " + reservation);
    if (reservation != null) {
      FullIndexEntry indexEntry = new FullIndexEntry(reservation.getInstanceId(), "Reservation",
          reservation.getId());
      indexEntry.setTitle(reservation.getEvent());
      indexEntry.setPreView(reservation.getReason());
      indexEntry.setCreationDate(reservation.getCreationDate());
      indexEntry.setCreationUser(reservation.getUserId());
      indexEntry.setKeyWords(reservation.getPlace());
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  private void deleteIndex(String objectId, String objectType, String componentId) {
    IndexEntryPK indexEntry = new IndexEntryPK(componentId, objectType, objectId);
    IndexEngineProxy.removeIndexEntry(indexEntry);
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
  public void addManager(long resourceId, long managerId) {
    resourceService.addManager(new ResourceValidator(resourceId, managerId));
  }

  @Override
  public void removeManager(long resourceId, long managerId) {
    resourceService.removeManager(new ResourceValidator(resourceId, managerId));
  }

  @Override
  public List<ResourceValidator> getManagers(long resourceId) {
    return resourceService.getManagers(resourceId);
  }

  @Override
  public void saveReservation(Reservation reservation, String listReservationCurrent) {
    StringTokenizer tokenizer = new StringTokenizer(listReservationCurrent, ",");
    List<Long> resourcesIds = new ArrayList<Long>(tokenizer.countTokens());
    while (tokenizer.hasMoreTokens()) {
      String id = tokenizer.nextToken();
      if (StringUtil.isLong(id)) {
        resourcesIds.add(Long.parseLong(id));
      }
    }
    String reservationId = reservationService.createReservation(reservation, resourcesIds);
    reservation.setId(reservationId);
    createReservationIndex(reservation);
  }

  @Override
  public void updateReservedResourceStatus(long reservationId, long resourceId, String status) {
    ReservedResource reservedResource = reservedResourceService.getReservedResource(resourceId,
        reservationId);
    if (reservedResource != null) {
      reservedResource.setStatus(status);
      reservedResourceService.update(reservedResource);
    }
    Reservation reservation = reservedResource.getReservation();
    reservation.setStatus(reservationService.computeReservationStatus(
        reservation));
    reservationService.updateReservation(reservation);
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
      ResourceValidator validator = new ResourceValidator(resource.getIntegerId(), managerId);
      if (!resource.getManagers().contains(validator)) {
        resource.getManagers().add(validator);
      }
    }
    resourceService.updateResource(resource);
    createResourceIndex(resource);
  }
}
