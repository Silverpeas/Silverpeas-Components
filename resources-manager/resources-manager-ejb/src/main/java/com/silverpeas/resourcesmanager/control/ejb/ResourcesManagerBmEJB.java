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
package com.silverpeas.resourcesmanager.control.ejb;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import org.silverpeas.resourcemanager.model.Category;
import org.silverpeas.resourcemanager.model.Reservation;
import org.silverpeas.resourcemanager.model.ReservedResource;
import org.silverpeas.resourcemanager.model.Resource;
import org.silverpeas.resourcemanager.model.ResourceValidator;
import org.silverpeas.resourcemanager.services.ServicesLocator;

/**
 * @author
 */
public class ResourcesManagerBmEJB implements SessionBean {

  private static final long serialVersionUID = 1L;

  public void ejbCreate() {
    // not implemented
  }

  @Override
  public void setSessionContext(SessionContext context) {
    // not implemented
  }

  @Override
  public void ejbRemove() {
    // not implemented
  }

  @Override
  public void ejbActivate() {
    // not implemented
  }

  @Override
  public void ejbPassivate() {
    // not implemented
  }

  /**
   * Creating a new resource category.
   *
   * @param category
   */
  public void createCategory(Category category) {
    String id = ServicesLocator.getCategoryService().createCategory(category);
    category.setId(id);
    createCategoryIndex(category);
  }

  public List<Category> getCategories(String instanceId) {
    return ServicesLocator.getCategoryService().getCategories(instanceId);
  }

  public Category getCategory(String id) {
    return ServicesLocator.getCategoryService().getCategory(id);
  }

  public void updateCategory(Category category) {
    ServicesLocator.getCategoryService().updateCategory(category);
  }

  public void deleteCategory(String id, String componentId) {
    // First delete all resources of category
    List<Resource> resources = getResourcesByCategory(id);
    for (Resource resource : resources) {
      ServicesLocator.getResourceService().deleteResource(Integer.parseInt(resource.getId()));
      deleteIndex("Resource", resource.getId(), componentId);
    }
    // Then delete category itself
    ServicesLocator.getCategoryService().deleteCategory(id);
    deleteIndex(id, "Category", componentId);
  }

  /**
   *
   * @param resource
   * @return
   */
  public String createResource(Resource resource) {
    String id = ServicesLocator.getResourceService().createResource(resource);
    resource.setId(id);
    createResourceIndex(resource);
    return id;
  }

  public void updateResource(Resource resource) {
    ServicesLocator.getResourceService().updateResource(resource);
    createResourceIndex(resource);
  }

  public Resource getResource(String id) {
    return ServicesLocator.getResourceService().getResource(Integer.parseInt(id));
  }

  public List<Resource> getResourcesByCategory(String categoryId) {
    return ServicesLocator.getResourceService().getResourcesByCategory(Integer.parseInt(categoryId));
  }

  public void deleteResource(String id, String componentId) {
    ServicesLocator.getResourceService().deleteResource(Integer.parseInt(id));
  }

  public List<Resource> getResourcesReservable(String instanceId, Date startDate, Date endDate) {
    return ServicesLocator.getResourceService().listAvailableResources(instanceId,
        String.valueOf(startDate.getTime()), String.valueOf(endDate.getTime()));
  }

  public List<Resource> getResourcesofReservation(String instanceId, String reservationId) {
    return ServicesLocator.getResourceService().listResourcesOfReservation(Integer.parseInt(
        reservationId));

  }

  /**
   *
   * @param reservation
   * @param listReservationCurrent
   */
  public void saveReservation(Reservation reservation, List<Integer> resources) {
    ServicesLocator.getReservationService().createReservation(reservation);
    for (Integer resourceId : resources) {
      Resource resource = ServicesLocator.getResourceService().getResource(resourceId);
      ReservedResource reserved = new ReservedResource();
      reserved.setReservation(reservation);
      reserved.setResource(resource);
      reserved.setStatus(reservation.getStatus());
      ServicesLocator.getReservedResourceService().create(reserved);
    }
  }

  public void updateReservation(Reservation reservationCourante) {
    ServicesLocator.getReservationService().updateReservation(reservationCourante);
  }

  /**
   *
   * @param instanceId
   * @param listeReservation
   * @param startDate
   * @param endDate
   * @return
   */
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
  public List<Resource> verificationNewDateReservation(String instanceId,
      String listeReservation, Date startDate, Date endDate, String reservationId) {
    StringTokenizer tokenizer = new StringTokenizer(listeReservation, ",");
    int currentReservationId = -1;
    String startPeriod = String.valueOf(startDate.getTime());
    String endPeriod = String.valueOf(endDate.getTime());
    if (StringUtil.isInteger(reservationId)) {
      currentReservationId = Integer.parseInt(reservationId);
    }
    List<Integer> futureReservedResourceIds = new ArrayList<Integer>();
    while (tokenizer.hasMoreTokens()) {
      futureReservedResourceIds.add(Integer.parseInt(tokenizer.nextToken()));
    }
    List<ReservedResource> alreadyReservedResources = ServicesLocator.getReservedResourceService().
        findAllReservedResourcesWithProblem(
        currentReservationId, futureReservedResourceIds, startPeriod, endPeriod);
    List resources = new ArrayList(alreadyReservedResources.size());
    for (ReservedResource reservedResource : alreadyReservedResources) {
      resources.add(reservedResource.getResource());
    }
    return resources;
  }

  public List<Reservation> getReservations(String instanceId) {
    return ServicesLocator.getReservationService().findAllReservations(instanceId);
  }

  public Reservation getReservation(String instanceId, String reservationId) {
    return ServicesLocator.getReservationService().getReservation(Integer.parseInt(reservationId));
  }

  public void deleteReservation(String id, String componentId) {
    deleteIndex(id, "Reservation", componentId);
    AttachmentController.deleteAttachmentByCustomerPK(new ForeignPK(id, componentId));
    ServicesLocator.getReservationService().deleteReservation(Integer.parseInt(id));
  }

  public List<Reservation> getMonthReservation(String instanceId, Date monthDate,
      String userId) {
    String endDate = String.valueOf(DateUtil.getEndDateOfMonth(monthDate).getTime());
    String beginDate = String.valueOf(DateUtil.getFirstDateOfMonth(monthDate).getTime());
    return ServicesLocator.getReservationService().findAllReservationsForValidation(instanceId,
        Integer.parseInt(userId), beginDate, endDate);
  }

  public List<Reservation> getReservationForValidation(String instanceId, Date monthDate,
      String userId) {
    String endDate = String.valueOf(DateUtil.getEndDateOfMonth(monthDate).getTime());
    String beginDate = String.valueOf(DateUtil.getFirstDateOfMonth(monthDate).getTime());
    return ServicesLocator.getReservationService().findAllReservationsForValidation(instanceId,
        Integer.parseInt(userId), beginDate, endDate);
  }

  public List<Reservation> getMonthReservationOfCategory(Date monthDate, String idCategory) {
    String endDate = String.valueOf(DateUtil.getEndDateOfMonth(monthDate).getTime());
    String beginDate = String.valueOf(DateUtil.getFirstDateOfMonth(monthDate).getTime());
    return ServicesLocator.getReservationService().findAllReservationsForCategoryInRange(Integer.
        parseInt(idCategory), beginDate, endDate);
  }

  public String getStatusResourceOfReservation(String resourceId, String reservationId) {
    ReservedResource reserved = ServicesLocator.getReservedResourceService().getReservedResource(
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

  public void addManager(int resourceId, int managerId) {
    ServicesLocator.getResourceService().addManager(new ResourceValidator(resourceId, managerId));
  }

  public void addManagers(int resourceId, List<Integer> managers) {
    List<ResourceValidator> validators = new ArrayList<ResourceValidator>(managers.size());
    for (Integer managerId : managers) {
      validators.add(new ResourceValidator(resourceId, managerId));
    }
    ServicesLocator.getResourceService().addManagers(resourceId, validators);
  }

  public void removeManager(int resourceId, int managerId) {
    ServicesLocator.getResourceService().removeManager(new ResourceValidator(resourceId, managerId));
  }

  public List<ResourceValidator> getManagers(int resourceId) {
    Resource resource = ServicesLocator.getResourceService().getResource(resourceId);
    if (resource != null) {
      return resource.getManagers();
    }
    return Collections.EMPTY_LIST;
  }

  public void updateResourceStatus(String status, int resourceId, int reservationId) {
    ReservedResource reserved = ServicesLocator.getReservedResourceService().getReservedResource(
        resourceId, reservationId);
    reserved.setStatus(status);
    ServicesLocator.getReservedResourceService().update(reserved);
  }
}
