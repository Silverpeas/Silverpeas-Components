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
import com.silverpeas.resourcesmanager.model.Reservation;
import org.silverpeas.resourcemanager.model.Category;
import com.silverpeas.resourcesmanager.model.ResourceDao;
import com.silverpeas.resourcesmanager.model.ResourceDetail;
import com.silverpeas.resourcesmanager.model.ResourceReservableDetail;
import com.silverpeas.resourcesmanager.model.ResourcesManagerDAO;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import org.silverpeas.resourcemanager.services.ServicesLocator;

/**
 * @author
 */
public class ResourcesManagerBmEJB implements SessionBean {

  private static final long serialVersionUID = 1L;
  private ResourceDao resourceDao = new ResourceDao();

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
    String id = ServicesLocator.getInstance().getCategoryService().createCategory(category);
    category.setId(id);
    createCategoryIndex(category);
  }

  public List<Category> getCategories(String instanceId) {
    return ServicesLocator.getInstance().getCategoryService().getCategories(instanceId);
  }

  public Category getCategory(String id) {
      return ServicesLocator.getInstance().getCategoryService().getCategory(id);
  }

  public void updateCategory(Category category) {
      ServicesLocator.getInstance().getCategoryService().updateCategory(category);
  }

  public void deleteCategory(String id, String componentId) {
    Connection con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    try {
      // First delete all resources of category
      List<ResourceDetail> resources = getResourcesByCategory(id);
      for (ResourceDetail resource : resources) {
        resourceDao.deleteResource(con, resource.getId());
        deleteIndex("Resource", resource.getId(), componentId);
      }
      // Then delete category itself
      ServicesLocator.getInstance().getCategoryService().deleteCategory(id);
      deleteIndex(id, "Category", componentId);
    } catch (SQLException e) {
      throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.deleteCategory()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_DELETE_CATEGORY", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   *
   * @param resource
   * @return
   */
  public String createResource(ResourceDetail resource) {
    Connection con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    try {
      String id = resourceDao.createResource(con, resource);
      resource.setId(id);
      createResourceIndex(resource);
      return id;
    } catch (SQLException e) {
      throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.createResource()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_CREATE_RESOURCE", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public void updateResource(ResourceDetail resource) {
    Connection con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    try {
      resourceDao.updateResource(con, resource);
      createResourceIndex(resource);
    } catch (SQLException e) {
      throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.updateResource()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_UPDATE_RESOURCE", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public ResourceDetail getResource(String id) {
    Connection con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    try {
      ResourceDetail resource = resourceDao.getResource(con, id);
      return resource;
    } catch (SQLException e) {
      throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.getResource()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_GET_RESOURCE", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public List<ResourceDetail> getResourcesByCategory(String categoryId) {
    Connection con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    try {
      return resourceDao.getResourcesByCategory(con, categoryId);
    } catch (SQLException e) {
      throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.getResourcesByCategory()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_GET_RESOURCES_BY_CATEGORY", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public void deleteResource(String id, String componentId) {
    Connection con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    try {
      resourceDao.deleteResource(con, id);
      deleteIndex(id, "Resource", componentId);
    } catch (SQLException e) {
      throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.deleteResource()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_DELETE_RESOURCE", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public List<ResourceReservableDetail> getResourcesReservable(String instanceId, Date startDate,
      Date endDate) {
    Connection con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    try {
      return ResourcesManagerDAO.getResourcesReservable(con, instanceId, startDate, endDate);
    } catch (SQLException e) {
      throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.getResourcesReservable()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_GET_RESOURCES_RESERVABLE", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public List<ResourceDetail> getResourcesofReservation(String instanceId, String reservationId) {
    Connection con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    try {
      return resourceDao.getResourcesofReservation(con,
          instanceId, reservationId);
    } catch (SQLException e) {
      throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.getResourcesofReservation()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_GET_RESOURCES_RESERVATION", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   *
   * @param reservation
   * @param listReservationCurrent
   */
  public void saveReservation(Reservation reservation, String listReservationCurrent) {
    Connection con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    try {
      String idReservation = ResourcesManagerDAO.saveReservation(con, reservation,
          listReservationCurrent);
      reservation.setId(idReservation);
      createReservationIndex(reservation);
    } catch (SQLException e) {
      throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.saveReservation()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_SAVE_RESERVATION", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   *
   * @param listReservation
   * @param reservationCourante
   * @param updateDate
   */
  public void updateReservation(String listReservation, Reservation reservationCourante,
      boolean updateDate) {
    Connection con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    try {
      ResourcesManagerDAO.updateReservation(con, listReservation, reservationCourante, updateDate);
      createReservationIndex(reservationCourante);
    } catch (SQLException e) {
      throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.updateReservation()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_UPDATE_RESERVATION", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public void updateReservation(Reservation reservationCourante) {
    Connection con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    try {
      ResourcesManagerDAO.updateReservation(con, reservationCourante);
      createReservationIndex(reservationCourante);
    } catch (SQLException e) {
      throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.updateReservation()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_UPDATE_RESERVATION", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   *
   * @param instanceId
   * @param listeReservation
   * @param startDate
   * @param endDate
   * @return
   */
  public List<ResourceDetail> verificationReservation(String instanceId, String listeReservation,
      Date startDate, Date endDate) {
    Connection con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    try {
      return ResourcesManagerDAO.verificationReservation(con, listeReservation, startDate, endDate);
    } catch (SQLException e) {
      throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.verificationReservation()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_CHECK_RESERVATIONS", e);
    } finally {
      DBUtil.close(con);
    }
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
  public List<ResourceDetail> verificationNewDateReservation(String instanceId,
      String listeReservation, Date startDate, Date endDate, String reservationId) {
    Connection con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    try {
      return ResourcesManagerDAO.verificationNewDateReservation(con, listeReservation, startDate,
          endDate, reservationId);
    } catch (SQLException e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.verificationNewDateReservation()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_CHECK_DATE_RESERVATION", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   *
   * @param instanceId
   * @param userId
   * @return
   */
  public List<Reservation> getReservationUser(String instanceId, String userId) {
    Connection con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    try {
      return ResourcesManagerDAO.getReservationUser(con, instanceId, userId);
    } catch (SQLException e) {
      throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.getReservationUser()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_GET_RESERVATIONS_USER", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public List<Reservation> getReservations(String instanceId) {
    Connection con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    try {
      return ResourcesManagerDAO.getReservations(con, instanceId);
    } catch (SQLException e) {
      throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.getReservations()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_GET_RESERVATIONS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public Reservation getReservation(String instanceId, String reservationId) {
    Connection con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    try {
      return ResourcesManagerDAO.getReservation(con, instanceId, reservationId);
    } catch (SQLException e) {
      throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.getReservation()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_GET_RESERVATION", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public void deleteReservation(String id, String componentId) {
    Connection con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    try {
      deleteIndex(id, "Reservation", componentId);
      // delete attached file 
      AttachmentController.deleteAttachmentByCustomerPK(new ForeignPK(id, componentId));
      ResourcesManagerDAO.deleteReservation(con, id);
    } catch (SQLException e) {
      throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.deleteReservation()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_DELETE_RESERVATION", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public List<Reservation> getMonthReservation(String instanceId, Date monthDate,
      String userId) {
    Connection con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    try {
      return ResourcesManagerDAO.getMonthReservation(con, instanceId, monthDate, userId);
    } catch (SQLException e) {
      throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.getMonthReservation()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_GET_MONTHLY_RESERVATIONS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public List<Reservation> getReservationForValidation(String instanceId, Date monthDate,
      String userId) {
    Connection con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    try {
      return ResourcesManagerDAO.getReservationForValidation(con, instanceId, monthDate, userId);
    } catch (SQLException e) {
      throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.getMonthReservation()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_GET_MONTHLY_RESERVATIONS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public List<Reservation> getMonthReservationOfCategory(String instanceId, Date monthDate,
      String userId, String idCategory) {
    Connection con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    try {
      return ResourcesManagerDAO.getMonthReservationOfCategory(con, instanceId, monthDate,
          idCategory);
    } catch (SQLException e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.getMonthReservationOfCategory()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_GET_MONTHLY_RESERVATIONS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public String getStatusResourceOfReservation(String resourceId, String reservationId) {
    Connection con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    try {
      return ResourcesManagerDAO.getStatusResourceOfReservation(con, Integer.parseInt(resourceId),
          Integer.parseInt(reservationId));
    } catch (SQLException e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.getMonthReservationOfCategory()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_GET_MONTHLY_RESERVATIONS", e);
    } finally {
      DBUtil.close(con);
    }
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

  private void createResourceIndex(ResourceDetail resource) {
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

  private void indexResourceForm(ResourceDetail resource, FullIndexEntry indexEntry,
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
    Connection con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    try {
      resourceDao.addManager(con, resourceId, managerId);
    } catch (SQLException e) {
      throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.addManager()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_ADD_MANAGER", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public void addManagers(int resourceId, List<String> managers) {
    Connection con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    try {
      resourceDao.removeAllManagers(con, resourceId);
      for (String manager : managers) {
        String managerId = manager.split("/")[0];
        addManager(resourceId, Integer.parseInt(managerId));
      }
    } catch (SQLException e) {
      throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.addManagers()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_ADD_MANAGER", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public void removeManager(int resourceId, int managerId) {
    Connection con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    try {
      resourceDao.removeManager(con, resourceId, managerId);
    } catch (SQLException e) {
      throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.removeManager()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_REMOVE_MANAGER", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public List<String> getManagers(int resourceId) {
    Connection con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    try {
      return resourceDao.getManagers(con, resourceId);
    } catch (SQLException e) {
      throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.getManagers()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_GET_MANAGERS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public void updateResourceStatus(String status, int resourceId, int reservationId,
      String componentId) {
    Connection con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    try {
      ResourcesManagerDAO.updateResourceStatus(con, status, resourceId, reservationId);
    } catch (SQLException e) {
      throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.updateResourceStatus()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_UPDATE_RESOURCE", e);
    } finally {
      DBUtil.close(con);
    }
  }
}