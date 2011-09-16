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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.resourcesmanager.control.ejb;

import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.resourcesmanager.model.CategoryDetail;
import com.silverpeas.resourcesmanager.model.ReservationDetail;
import com.silverpeas.resourcesmanager.model.ResourceDetail;
import com.silverpeas.resourcesmanager.model.ResourceReservableDetail;
import com.silverpeas.resourcesmanager.model.ResourcesManagerDAO;
import com.silverpeas.resourcesmanager.model.ResourcesManagerRuntimeException;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * @author
 */
public class ResourcesManagerBmEJB implements SessionBean {

  private static final long serialVersionUID = 1L;

  public void ejbCreate() {
    // not implemented
  }

  public void setSessionContext(SessionContext context) {
    // not implemented
  }

  public void ejbRemove() {
    // not implemented
  }

  public void ejbActivate() {
    // not implemented
  }

  public void ejbPassivate() {
    // not implemented
  }

  /*** Gestion des catégories ***/
  public void createCategory(CategoryDetail category) {
    Connection con = initCon();
    try {
      int id = ResourcesManagerDAO.createCategory(con, category);

      category.setId(Integer.toString(id));
      createIndex_Category(category);
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.createCategory()",
          SilverpeasRuntimeException.ERROR,
          "resourcesManager.EX_CREATE_CATEGORY", e);
    } finally {
      fermerCon(con);
    }
  }

  public List<CategoryDetail> getCategories(String instanceId) {
    Connection con = initCon();
    try {
      return ResourcesManagerDAO.getCategories(con, instanceId);
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.getCategories()",
          SilverpeasRuntimeException.ERROR,
          "resourcesManager.EX_GET_CATEGORIES", e);
    } finally {
      fermerCon(con);
    }
  }

  public CategoryDetail getCategory(String id) {
    Connection con = initCon();
    try {
      return ResourcesManagerDAO.getCategory(con, id);
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.getCategory()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_GET_CATEGORY",
          e);
    } finally {
      fermerCon(con);
    }
  }

  public void updateCategory(CategoryDetail category) {
    Connection con = initCon();
    try {
      ResourcesManagerDAO.updateCategory(con, category);
      createIndex_Category(category);
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.updateCategory()",
          SilverpeasRuntimeException.ERROR,
          "resourcesManager.EX_UPDATE_CATEGORY", e);
    } finally {
      fermerCon(con);
    }
  }

  public void deleteCategory(String id, String componentId) {
    Connection con = initCon();
    try {
      // First delete all resources of category
      List<ResourceDetail> resources = getResourcesByCategory(id);
      for (ResourceDetail resource : resources) {
        ResourcesManagerDAO.deleteResource(con, resource.getId());
        deleteIndex("Resource", resource.getId(), componentId);
      }
      // Then delete category itself
      ResourcesManagerDAO.deleteCategory(con, id);
      deleteIndex(id, "Category", componentId);
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.deleteCategory()",
          SilverpeasRuntimeException.ERROR,
          "resourcesManager.EX_DELETE_CATEGORY", e);
    } finally {
      fermerCon(con);
    }
  }

  /**** Gestion des ressources ***/
  public String createResource(ResourceDetail resource) {
    Connection con = initCon();
    try {
      String id = ResourcesManagerDAO.createResource(con, resource);

      resource.setId(id);
      createIndex_Resource(resource);

      return id;
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.createResource()",
          SilverpeasRuntimeException.ERROR,
          "resourcesManager.EX_CREATE_RESOURCE", e);
    } finally {
      fermerCon(con);
    }
  }

  public void updateResource(ResourceDetail resource) {
    Connection con = initCon();
    try {
      ResourcesManagerDAO.updateResource(con, resource);
      createIndex_Resource(resource);
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.updateResource()",
          SilverpeasRuntimeException.ERROR,
          "resourcesManager.EX_UPDATE_RESOURCE", e);
    } finally {
      fermerCon(con);
    }
  }

  public ResourceDetail getResource(String id) {
    Connection con = initCon();
    try {
      ResourceDetail resource = ResourcesManagerDAO.getResource(con, id);
      return resource;
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.getResource()",
          SilverpeasRuntimeException.ERROR, "resourcesManager.EX_GET_RESOURCE",
          e);
    } finally {
      fermerCon(con);
    }
  }

  public List<ResourceDetail> getResourcesByCategory(String categoryId) {
    Connection con = initCon();
    try {
      return ResourcesManagerDAO.getResourcesByCategory(con, categoryId);
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.getResourcesByCategory()",
          SilverpeasRuntimeException.ERROR,
          "resourcesManager.EX_GET_RESOURCES_BY_CATEGORY", e);
    } finally {
      fermerCon(con);
    }
  }

  public void deleteResource(String id, String componentId) {
    Connection con = initCon();
    try {
      ResourcesManagerDAO.deleteResource(con, id);
      deleteIndex(id, "Resource", componentId);
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.deleteResource()",
          SilverpeasRuntimeException.ERROR,
          "resourcesManager.EX_DELETE_RESOURCE", e);
    } finally {
      fermerCon(con);
    }
  }

  public List<ResourceReservableDetail> getResourcesReservable(String instanceId, Date startDate,
      Date endDate) {
    Connection con = initCon();
    try {
      return ResourcesManagerDAO.getResourcesReservable(con, instanceId,
          startDate, endDate);
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.getResourcesReservable()",
          SilverpeasRuntimeException.ERROR,
          "resourcesManager.EX_GET_RESOURCES_RESERVABLE", e);
    } finally {
      fermerCon(con);
    }
  }

  public List<ResourceDetail> getResourcesofReservation(String instanceId, String reservationId) {
    Connection con = initCon();
    try {
      return ResourcesManagerDAO.getResourcesofReservation(con,
          instanceId, reservationId);
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.getResourcesofReservation()",
          SilverpeasRuntimeException.ERROR,
          "resourcesManager.EX_GET_RESOURCES_RESERVATION", e);
    } finally {
      fermerCon(con);
    }
  }

  /*** Gestion des réservations **/
  public void saveReservation(ReservationDetail reservation,
      String listReservationCurrent) {
    Connection con = initCon();
    try {
      String idReservation = ResourcesManagerDAO.saveReservation(con,
          reservation, listReservationCurrent);
      reservation.setId(idReservation);
      createIndex(reservation);
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.saveReservation()",
          SilverpeasRuntimeException.ERROR,
          "resourcesManager.EX_SAVE_RESERVATION", e);
    } finally {
      fermerCon(con);
    }
  }

  public void updateReservation(String listReservation,
      ReservationDetail reservationCourante, boolean updateDate) {
    Connection con = initCon();
    try {
      ResourcesManagerDAO.updateReservation(con, listReservation,
          reservationCourante, updateDate);
      createIndex(reservationCourante);
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.updateReservation()",
          SilverpeasRuntimeException.ERROR,
          "resourcesManager.EX_UPDATE_RESERVATION", e);
    } finally {
      fermerCon(con);
    }
  }

  public void updateReservation(ReservationDetail reservationCourante) {
    Connection con = initCon();
    try {
      ResourcesManagerDAO.updateReservation(con, reservationCourante);
      createIndex(reservationCourante);
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.updateReservation()",
          SilverpeasRuntimeException.ERROR,
          "resourcesManager.EX_UPDATE_RESERVATION", e);
    } finally {
      fermerCon(con);
    }
  }

  public List<ResourceDetail> verificationReservation(String instanceId,
      String listeReservation, Date startDate, Date endDate) {
    Connection con = initCon();
    try {
      return ResourcesManagerDAO.verificationReservation(con, instanceId,
          listeReservation, startDate, endDate);
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.verificationReservation()",
          SilverpeasRuntimeException.ERROR,
          "resourcesManager.EX_CHECK_RESERVATIONS", e);
    } finally {
      fermerCon(con);
    }
  }

  public List<ResourceDetail> verificationNewDateReservation(String instanceId,
      String listeReservation, Date startDate, Date endDate,
      String reservationId) {
    Connection con = initCon();
    try {
      return ResourcesManagerDAO.verificationNewDateReservation(con,
          instanceId, listeReservation, startDate, endDate, reservationId);
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.verificationNewDateReservation()",
          SilverpeasRuntimeException.ERROR,
          "resourcesManager.EX_CHECK_DATE_RESERVATION", e);
    } finally {
      fermerCon(con);
    }
  }

  public List<ReservationDetail> getReservationUser(String instanceId, String userId) {
    Connection con = initCon();
    try {
      return ResourcesManagerDAO.getReservationUser(con, instanceId, userId);
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.getReservationUser()",
          SilverpeasRuntimeException.ERROR,
          "resourcesManager.EX_GET_RESERVATIONS_USER", e);
    } finally {
      fermerCon(con);
    }
  }

  public List<ReservationDetail> getReservations(String instanceId) {
    Connection con = initCon();
    try {
      return ResourcesManagerDAO.getReservations(con, instanceId);
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.getReservations()",
          SilverpeasRuntimeException.ERROR,
          "resourcesManager.EX_GET_RESERVATIONS", e);
    } finally {
      fermerCon(con);
    }
  }

  public ReservationDetail getReservation(String instanceId,
      String reservationId) {
    Connection con = initCon();
    try {
      return ResourcesManagerDAO.getReservation(con, instanceId, reservationId);

    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.getReservation()",
          SilverpeasRuntimeException.ERROR,
          "resourcesManager.EX_GET_RESERVATION", e);
    } finally {
      fermerCon(con);
    }
  }

  public void deleteReservation(String id, String componentId) {
    Connection con = initCon();
    try {
      deleteIndex(id, "Reservation", componentId);
      
      // delete attached file 
      AttachmentController.deleteAttachmentByCustomerPK(new ForeignPK(id, componentId));
      
      ResourcesManagerDAO.deleteReservation(con, id);
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.deleteReservation()",
          SilverpeasRuntimeException.ERROR,
          "resourcesManager.EX_DELETE_RESERVATION", e);
    } finally {
      fermerCon(con);
    }
  }

  public List<ReservationDetail> getMonthReservation(String instanceId, Date monthDate,
      String userId, String language) {
    Connection con = initCon();
    try {
      return ResourcesManagerDAO.getMonthReservation(con, instanceId,
          monthDate, userId, language);
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.getMonthReservation()",
          SilverpeasRuntimeException.ERROR,
          "resourcesManager.EX_GET_MONTHLY_RESERVATIONS", e);
    } finally {
      fermerCon(con);
    }
  }

  public List<ReservationDetail> getReservationForValidation(String instanceId, Date monthDate,
      String userId, String language) {
    Connection con = initCon();
    try {
      return ResourcesManagerDAO.getReservationForValidation(con, instanceId, monthDate, userId,
          language);
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.getMonthReservation()",
          SilverpeasRuntimeException.ERROR,
          "resourcesManager.EX_GET_MONTHLY_RESERVATIONS", e);
    } finally {
      fermerCon(con);
    }
  }

  public List<ReservationDetail> getMonthReservationOfCategory(String instanceId, Date monthDate,
      String userId, String language, String idCategory) {
    Connection con = initCon();
    try {
      return ResourcesManagerDAO.getMonthReservationOfCategory(con, instanceId,
          monthDate, userId, language, idCategory);
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.getMonthReservationOfCategory()",
          SilverpeasRuntimeException.ERROR,
          "resourcesManager.EX_GET_MONTHLY_RESERVATIONS", e);
    } finally {
      fermerCon(con);
    }
  }

  public String getStatusResourceOfReservation(String resourceId, String reservationId) {
    Connection con = initCon();
    try {
      return ResourcesManagerDAO.getStatusResourceOfReservation(con, Integer.parseInt(resourceId),
          Integer.parseInt(reservationId));
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.getMonthReservationOfCategory()",
          SilverpeasRuntimeException.ERROR,
          "resourcesManager.EX_GET_MONTHLY_RESERVATIONS", e);
    } finally {
      fermerCon(con);
    }
  }

  private Connection initCon() {
    Connection con;
    // initialisation de la connexion
    try {
      con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    } catch (UtilException e) {
      // traitement des exceptions
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.initCon()", SilverpeasException.ERROR,
          "root.EX_CONNECTION_OPEN_FAILED", e);
    }
    return con;
  }

  private void fermerCon(Connection con) {
    try {
      con.close();
    } catch (SQLException e) {
      // traitement des exceptions
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.fermerCon()", SilverpeasException.ERROR,
          "root.EX_CONNECTION_CLOSE_FAILED", e);
    }
  }

  private void createIndex_Category(CategoryDetail category) {
    SilverTrace.info("resourceManager",
        "resourceManagerBmEJB.createIndex_Category()",
        "root.MSG_GEN_ENTER_METHOD", "category = " + category.toString());
    FullIndexEntry indexEntry = null;

    if (category != null) {
      indexEntry = new FullIndexEntry(category.getInstanceId(), "Category",
          category.getId());
      indexEntry.setTitle(category.getName());
      indexEntry.setPreView(category.getDescription());
      if (category.getUpdateDate() != null)
        indexEntry.setCreationDate(category.getUpdateDate());
      else
        indexEntry.setCreationDate(category.getCreationDate());
      indexEntry.setCreationUser(category.getCreaterId());
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  private void createIndex_Resource(ResourceDetail resource) {
    SilverTrace.info("resourceManager",
        "resourceManagerBmEJB.createIndex_Resource()",
        "root.MSG_GEN_ENTER_METHOD", "resource = " + resource.toString());
    FullIndexEntry indexEntry = null;

    if (resource != null) {
      // Index the Reservation
      indexEntry = new FullIndexEntry(resource.getInstanceId(), "Resource",
          resource.getId());
      indexEntry.setTitle(resource.getName());
      indexEntry.setPreView(resource.getDescription());
      if (resource.getUpdateDate() != null)
        indexEntry.setCreationDate(resource.getUpdateDate());
      else
        indexEntry.setCreationDate(resource.getCreationDate());
      indexEntry.setCreationUser(resource.getCreaterId());

      String categoryId = resource.getCategoryId();
      if (StringUtil.isDefined(categoryId)) {
        CategoryDetail category = getCategory(categoryId);
        if (category != null) {
          String xmlFormName = category.getForm();
          if (StringUtil.isDefined(xmlFormName)) {
            // indéxation du contenu du formulaire XML
            String xmlFormShortName = xmlFormName.substring(xmlFormName
                .indexOf("/") + 1, xmlFormName.indexOf("."));
            PublicationTemplate pubTemplate;
            try {
              pubTemplate = PublicationTemplateManager.getInstance()
                  .getPublicationTemplate(resource.getInstanceId() + ":"
                      + xmlFormShortName);
              RecordSet set = pubTemplate.getRecordSet();
              set.indexRecord(resource.getId(), xmlFormName, indexEntry);
            } catch (Exception e) {
              throw new ResourcesManagerRuntimeException(
                  "ResourceManagerBmEJB.createIndex_Resource()",
                  SilverpeasRuntimeException.ERROR,
                  "resourcesManager.EX_CREATE_INDEX_FAILED", e);
            }
          }
        }
      }

      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  private void createIndex(ReservationDetail reservation) {
    SilverTrace.info("resourceManager", "resourceManagerBmEJB.createIndex()",
        "root.MSG_GEN_ENTER_METHOD", "reservation = " + reservation);
    FullIndexEntry indexEntry = null;

    if (reservation != null) {
      // Index the Reservation
      indexEntry = new FullIndexEntry(reservation.getInstanceId(),
          "Reservation", reservation.getId());
      indexEntry.setTitle(reservation.getEvent());
      indexEntry.setPreView(reservation.getReason());
      indexEntry.setCreationDate(reservation.getCreationDate());
      indexEntry.setCreationUser(reservation.getUserId());
      indexEntry.setKeyWords(reservation.getPlace());
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  private void deleteIndex(String objectId, String objectType,
      String componentId) {
    IndexEntryPK indexEntry = new IndexEntryPK(componentId, objectType,
        objectId);
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  public void indexResourceManager(String instanceId) {
    List<ReservationDetail> listOfReservation = getReservations(instanceId);
    if (listOfReservation != null) {
      for (ReservationDetail reservation : listOfReservation) {
        try {
          createIndex(reservation);
        } catch (Exception e) {
          throw new ResourcesManagerRuntimeException(
              "ResourcesManagerBmEJB.indexResourceManager()",
              SilverpeasRuntimeException.ERROR,
              "resourcesManager.MSG_INDEXRESERVATIONS", e);
        }
      }
    }
  }

  public void addManager(int resourceId, int managerId) {
    Connection con = initCon();
    try {
      ResourcesManagerDAO.addManager(con, resourceId, managerId);
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.addManager()",
          SilverpeasRuntimeException.ERROR,
          "resourcesManager.EX_ADD_MANAGER", e);
    } finally {
      fermerCon(con);
    }
  }

  public void addManagers(int resourceId, List<String> managers) {
    Connection con = initCon();
    try {
      ResourcesManagerDAO.removeAllManagers(con, resourceId);
      for (String manager : managers) {
        String managerId = manager.split("/")[0];
        addManager(resourceId, Integer.parseInt(managerId));
      }
    } catch (SQLException e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.addManagers()",
          SilverpeasRuntimeException.ERROR,
          "resourcesManager.EX_ADD_MANAGER", e);
    } finally {
      fermerCon(con);
    }
  }

  public void removeManager(int resourceId, int managerId) {
    Connection con = initCon();
    try {
      ResourcesManagerDAO.removeManager(con, resourceId, managerId);
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.removeManager()",
          SilverpeasRuntimeException.ERROR,
          "resourcesManager.EX_REMOVE_MANAGER", e);
    } finally {
      fermerCon(con);
    }
  }

  public List<String> getManagers(int resourceId) {
    Connection con = initCon();
    try {
      return ResourcesManagerDAO.getManagers(con, resourceId);
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.getManagers()",
          SilverpeasRuntimeException.ERROR,
          "resourcesManager.EX_GET_MANAGERS", e);
    } finally {
      fermerCon(con);
    }
  }

  public void updateResourceStatus(String status, int resourceId, int reservationId,
      String componentId) {
    Connection con = initCon();
    try {
      ResourcesManagerDAO.updateResourceStatus(con, status, resourceId, reservationId, componentId);
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerBmEJB.updateResourceStatus()",
          SilverpeasRuntimeException.ERROR,
          "resourcesManager.EX_UPDATE_RESOURCE", e);
    } finally {
      fermerCon(con);
    }
  }

}