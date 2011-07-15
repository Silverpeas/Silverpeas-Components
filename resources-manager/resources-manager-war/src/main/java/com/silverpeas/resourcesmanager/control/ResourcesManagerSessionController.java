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
package com.silverpeas.resourcesmanager.control;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import com.silverpeas.resourcesmanager.control.ejb.ResourcesManagerBm;
import com.silverpeas.resourcesmanager.control.ejb.ResourcesManagerBmHome;
import com.silverpeas.resourcesmanager.model.CategoryDetail;
import com.silverpeas.resourcesmanager.model.ReservationDetail;
import com.silverpeas.resourcesmanager.model.ResourceDetail;
import com.silverpeas.resourcesmanager.model.ResourceReservableDetail;
import com.silverpeas.resourcesmanager.model.ResourcesManagerRuntimeException;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.MonthCalendar;
import com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.MonthCalendarWA1;

public class ResourcesManagerSessionController extends AbstractComponentSessionController {

  private ReservationDetail reservationCourante;
  private Calendar currentDay = Calendar.getInstance();
  private List<ResourceReservableDetail> resourceReserved =
          new ArrayList<ResourceReservableDetail>();
  private List<ResourceReservableDetail> listReservableResource;
  private Date beginDateReservation;
  private Date endDateReservation;
  private String listReservationCurrent;
  private String provenanceResource = null;
  private String resourceIdForResource = null;
  private String reservationIdForResource = null;
  private String categoryIdForResource = null;
  private String objectViewForCalandar = null;
  private String firstNameUserCalandar = null;
  private String lastNameUserCalandar = null;
  private String currentCategory;
  private String currentResource;
  private String currentReservation;
  private NotificationSender notifSender = null;
  private ResourcesWrapper resources = null;

  public String getCurrentCategory() {
    return currentCategory;
  }

  public void setCurrentCategory(String currentCategory) {
    this.currentCategory = currentCategory;
  }

  public String getCurrentResource() {
    return currentResource;
  }

  public void setCurrentResource(String currentResource) {
    this.currentResource = currentResource;
  }

  public String getCurrentReservation() {
    return currentReservation;
  }

  public void setCurrentReservation(String currentReservation) {
    this.currentReservation = currentReservation;
  }

  public String getFirstNameUserCalandar() {
    return firstNameUserCalandar;
  }

  public void setFirstNameUserCalandar(String firstNameUserCalandar) {
    this.firstNameUserCalandar = firstNameUserCalandar;
  }

  public String getLastNameUserCalandar() {
    return lastNameUserCalandar;
  }

  public void setLastNameUserCalandar(String lastNameUserCalandar) {
    this.lastNameUserCalandar = lastNameUserCalandar;
  }

  public String getObjectViewForCalandar() {
    return objectViewForCalandar;
  }

  public void setObjectViewForCalandar(String objectViewForCalandar) {
    this.objectViewForCalandar = objectViewForCalandar;
  }

  public String getCategoryIdForResource() {
    return categoryIdForResource;
  }

  public void setCategoryIdForResource(String categoryIdForResource) {
    this.categoryIdForResource = categoryIdForResource;
  }

  public String getReservationIdForResource() {
    return reservationIdForResource;
  }

  public void setReservationIdForResource(String reservationIdForResource) {
    this.reservationIdForResource = reservationIdForResource;
  }

  public String getResourceIdForResource() {
    return resourceIdForResource;
  }

  public void setResourceIdForResource(String resourceIdForResource) {
    this.resourceIdForResource = resourceIdForResource;
  }

  public String getProvenanceResource() {
    return provenanceResource;
  }

  public void setProvenanceResource(String provenanceResource) {
    this.provenanceResource = provenanceResource;
  }

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public ResourcesManagerSessionController(
          MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
            "com.silverpeas.resourcesmanager.multilang.resourcesManagerBundle",
            "com.silverpeas.resourcesmanager.settings.resourcesManagerIcons");
  }

  public boolean isResponsible() {
    for (String role : getUserRoles()) {
      if ("responsable".equalsIgnoreCase(role)) {
        return true;
      }
    }
    return false;
  }

  /*** Gestion des catégories ***/
  public void createCategory(CategoryDetail category) {
    try {
      category.setInstanceId(getComponentId());
      category.setCreaterId(getUserId());
      category.setUpdaterId(getUserId());
      category.setCreationDate(new Date());
      category.setUpdateDate(new Date());
      getResourcesManagerBm().createCategory(category);
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.createCategory()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public void updateCategory(CategoryDetail category) {
    try {
      category.setInstanceId(getComponentId());
      category.setCreaterId(getUserId());
      category.setUpdaterId(getUserId());
      category.setCreationDate(new Date());
      category.setUpdateDate(new Date());
      getResourcesManagerBm().updateCategory(category);
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.updateCategory()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }

  }

  public List<CategoryDetail> getCategories() {
    try {
      return getResourcesManagerBm().getCategories(getComponentId());
    } catch (RemoteException e) {
      // TODO: handle exception
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.getCategories()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public CategoryDetail getCategory(String id) {
    try {
      CategoryDetail category = getResourcesManagerBm().getCategory(id);
      return category;
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.getCategory()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public void deleteCategory(String id) {
    try {
      getResourcesManagerBm().deleteCategory(id, getComponentId());
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.deleteCategory()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /*** Gestion des ressources ***/
  public String createResource(ResourceDetail resource) {
    try {
      resource.setInstanceId(getComponentId());
      resource.setCreaterId(getUserId());
      resource.setUpdaterId(getUserId());
      resource.setCreationDate(new Date());
      resource.setUpdateDate(new Date());
      return getResourcesManagerBm().createResource(resource);
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.createResource()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public void updateResource(ResourceDetail resource) {
    try {
      resource.setInstanceId(getComponentId());
      resource.setUpdaterId(getUserId());
      resource.setUpdateDate(new Date());
      // addManagers(managers);
      getResourcesManagerBm().updateResource(resource);
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.updateResource()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public ResourceDetail getResource(String id) {
    try {
      ResourceDetail resource = getResourcesManagerBm().getResource(id);
      // ajout des responsables
      resource.setManagers(getManagerIds(id));
      return resource;
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.getResource()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public void deleteResource(String id) {
    try {
      getResourcesManagerBm().deleteResource(id, getComponentId());
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.deleteResource()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public List<ResourceDetail> getResourcesByCategory(String categoryId) {
    try {
      return getResourcesManagerBm().getResourcesByCategory(categoryId);
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.getResourcesByCategory()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public List<ResourceReservableDetail> getResourcesReservable(Date startDate, Date endDate) {
    List<ResourceReservableDetail> listeResources = new ArrayList<ResourceReservableDetail>();

    try {
      listeResources = getResourcesManagerBm().getResourcesReservable(
              getComponentId(), startDate, endDate);
      this.listReservableResource = listeResources;
      return listReservableResource;
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.getResourcesReservable()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public List<ResourceDetail> getResourcesofReservation(String reservationId) {
    try {
      return getResourcesManagerBm().getResourcesofReservation(
              getComponentId(), reservationId);
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.getResourcesofReservation()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /*** Gestion des réservations ***/
  public void createReservation(ReservationDetail reservation) {
    reservation.setInstanceId(getComponentId());
    reservation.setUserId(getUserId());
    reservation.setCreationDate(new Date());
    reservation.setUpdateDate(new Date());
    reservationCourante = reservation;
  }

  public void saveReservation() {
    try {
      // rechercher le statut à mettre sur la reservation
      getResourcesManagerBm().saveReservation(reservationCourante,
              listReservationCurrent);
      // envoi d'une notification pour validation aux responsables des ressources selectionnées.
      StringTokenizer tokenizer = new StringTokenizer(listReservationCurrent, ",");
      while (tokenizer.hasMoreTokens()) {
        String idResource = tokenizer.nextToken();
        sendNotificationForValidation(idResource, reservationCourante.getId());
      }
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.saveReservation()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public List<ResourceDetail> verificationReservation(String listeReservation) {
    try {
      return getResourcesManagerBm().verificationReservation(getComponentId(),
              listeReservation, beginDateReservation, endDateReservation);
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.verificationReservation()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public List<ResourceDetail> getResourcesProblemDate(String listeReservation, Date beginDate,
          Date endDate, String reservationId) {
    try {
      return getResourcesManagerBm().verificationNewDateReservation(getComponentId(),
              listeReservation,
              beginDate, endDate, reservationId);
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.getResourcesProblemDate()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public List<ReservationDetail> getReservationUser() {
    try {
      return getResourcesManagerBm().getReservationUser(getComponentId(),
              getUserId());
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.getReservationUser()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public ReservationDetail getReservation(String reservationId) {
    try {
      ReservationDetail reservation = getResourcesManagerBm().getReservation(
              getComponentId(), reservationId);
      reservation.setUserName(getUserDetail(reservation.getUserId()).getDisplayedName());
      return reservation;
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.getReservation()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public void updateReservation(String idModifiedReservation,
          String listReservation, boolean updateDate) {
    try {
      reservationCourante.setId(idModifiedReservation);
      getResourcesManagerBm().updateReservation(listReservation,
              reservationCourante, updateDate);
      // envoi d'une notification pour validation aux responsables des ressources selectionnées.
      StringTokenizer tokenizer = new StringTokenizer(listReservation, ",");
      while (tokenizer.hasMoreTokens()) {
        String idResource = tokenizer.nextToken();
        sendNotificationForValidation(idResource, reservationCourante.getId());
      }
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.updateReservation()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public void deleteReservation(String id) {
    try {
      getResourcesManagerBm().deleteReservation(id, getComponentId());
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.deleteReservation()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public void sendNotificationForValidation(String resourceId, String reservationId)
          throws RemoteException,
          NotificationManagerException {
    ResourceDetail resource = getResource(resourceId);
    String status =
            getResourcesManagerBm().getStatusResourceOfReservation(resourceId, reservationId);
    if (ReservationDetail.STATUS_FOR_VALIDATION.equals(status)) {
      // envoyer une notification aux responsables de la ressource
      OrganizationController orga = new OrganizationController();
      String user = orga.getUserDetail(getUserId()).getDisplayedName();

      ResourceLocator message =
              new ResourceLocator("com.silverpeas.resourcesmanager.multilang.resourcesManagerBundle",
              "fr");
      ResourceLocator message_en =
              new ResourceLocator("com.silverpeas.resourcesmanager.multilang.resourcesManagerBundle",
              "en");

      StringBuffer messageBody = new StringBuffer();
      StringBuffer messageBody_en = new StringBuffer();

      // liste des responsables (de la ressource) à notifier
      List<String> managerIds = resource.getManagers();
      List<UserRecipient> managers = new ArrayList<UserRecipient>(managerIds.size());
      if (!managerIds.contains(getUserId())) {
        // envoie de la notification seulement si le user courant n'est pas aussi responsable
        Iterator<String> it = managerIds.iterator();
        for (String managerId : managerIds) {
          managers.add(new UserRecipient(managerId));
        }

        // french notifications
        String subject = message.getString("resourcesManager.notifSubject");
        messageBody = messageBody.append(user).append(" ").append(
                message.getString("resourcesManager.notifBody")).append(" ").append(
                resource.getName());

        // english notifications
        String subject_en = message_en.getString("resourcesManager.notifSubject");
        messageBody_en = messageBody_en.append(user).append(" ").append(
                message.getString("resourcesManager.notifBody")).append(" ").append(
                resource.getName());

        NotificationMetaData notifMetaData =
                new NotificationMetaData(NotificationParameters.NORMAL,
                subject, messageBody.toString());
        notifMetaData.addLanguage("en", subject_en, messageBody_en.toString());

        notifMetaData.setLink(URLManager.getURL(null, getComponentId())
                + "ViewReservation?reservationId=" + reservationId);
        notifMetaData.setComponentId(getComponentId());
        notifMetaData.addUserRecipients(managers);
        notifMetaData.setSender(user);
        // 2. envoie de la notification aux responsables
        getNotificationSender().notifyUser(notifMetaData);
      }
    }
  }

  public NotificationSender getNotificationSender() {
    if (notifSender == null) {
      notifSender = new NotificationSender(getComponentId());
    }
    return notifSender;
  }

  public void addManager(String resourceId, String managerId) {
    try {
      getResourcesManagerBm().addManager(Integer.parseInt(resourceId),
              Integer.parseInt(managerId));
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.getManagers()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public void removeManager(String resourceId, String managerId) {
    try {
      getResourcesManagerBm().removeManager(Integer.parseInt(resourceId),
              Integer.parseInt(managerId));
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.removeManager()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public List<String> getManagerIds(String resourceId) {
    try {
      List<String> managers = new ArrayList<String>();
      List<String> managerIds =
              getResourcesManagerBm().getManagers(Integer.parseInt(resourceId));
      Iterator<String> it = managerIds.iterator();
      while (it.hasNext()) {
        String managerId = it.next();
        managers.add(managerId);
      }
      return managers;
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.getManagerIds()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public List<UserDetail> getManagers(String resourceId) {
    try {
      List<UserDetail> managers = new ArrayList<UserDetail>();
      List<String> managerIds =
              getResourcesManagerBm().getManagers(Integer.parseInt(resourceId));
      // ajouter le nom du responsable
      Iterator<String> it = managerIds.iterator();
      while (it.hasNext()) {
        String managerId = it.next();
        UserDetail manager = getUserDetail(managerId);
        managers.add(manager);
      }
      return managers;
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.getManagers()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public void addManagers(String resourceId, List<String> managers) {
    try {
      getResourcesManagerBm().addManagers(Integer.parseInt(resourceId), managers);
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.addManagers()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public String initUserSelect(Collection<String> currentManagers) throws RemoteException {
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString(
            "ApplicationURL");
    String hostUrl = m_context
            + URLManager.getURL(getSpaceId(), getComponentId()) + "FromUserSelect";

    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName(getSpaceLabel());
    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(hostUrl);

    sel.setMultiSelect(true);
    sel.setPopupMode(false);
    sel.setSetSelectable(false);

    PairObject hostComponentName = new PairObject(getComponentLabel(), null);
    sel.setHostPath(null);
    sel.setHostComponentName(hostComponentName);
    sel.setFirstPage(Selection.FIRST_PAGE_DEFAULT);

    ArrayList<String> roles = new ArrayList<String>();
    roles.add("responsable");

    // Add extra params
    SelectionUsersGroups sug = new SelectionUsersGroups();
    sug.setComponentId(getComponentId());
    sug.setProfileNames(roles);
    sel.setExtraParams(sug);

    String[] users = new String[currentManagers.size()];
    int i = 0;
    Iterator<String> it = currentManagers.iterator();
    while (it.hasNext()) {
      users[i] = it.next();
      i++;
    }
    sel.setSelectedElements(users);

    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  private ResourcesManagerBm getResourcesManagerBm() {
    ResourcesManagerBm resourcesmanagerBm = null;
    try {
      ResourcesManagerBmHome resourcesmanagerBmHome = EJBUtilitaire.getEJBObjectRef(
              "ejb/ResourcesManagerBm", ResourcesManagerBmHome.class);
      resourcesmanagerBm = resourcesmanagerBmHome.create();
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.getResourcesManageryBm()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return resourcesmanagerBm;
  }

  public ReservationDetail getReservationCourante() {
    return reservationCourante;
  }

  public void setReservationCourante(ReservationDetail reservationCourante) {
    this.reservationCourante = reservationCourante;
  }

  public List<ResourceReservableDetail> getResourcesReservable() {
    return listReservableResource;
  }

  public void setResourcesReservable(List<ResourceReservableDetail> listReservableResource) {
    this.listReservableResource = listReservableResource;
  }

  public Date getBeginDateReservation() {
    return beginDateReservation;
  }

  public void setBeginDateReservation(Date beginDateReservation) {
    this.beginDateReservation = beginDateReservation;
  }

  public Date getEndDateReservation() {
    return endDateReservation;
  }

  public void setEndDateReservation(Date endDateReservation) {
    this.endDateReservation = endDateReservation;
  }

  public List<ResourceReservableDetail> getResourceReserved() {
    return resourceReserved;
  }

  public void setResourceReserved(ResourceReservableDetail ResourceReservable) {
    this.resourceReserved.add(ResourceReservable);
  }

  public String getListReservationCurrent() {
    return listReservationCurrent;
  }

  public void setListReservationCurrent(String listReservationCurrent) {
    this.listReservationCurrent = listReservationCurrent;
  }

  /*** Gestion de l'almanach **/
  public MonthCalendar getMonthCalendar() {
    int numbersDays = 7;
    if (isWeekendNotVisible()) {
      numbersDays = 5;
    }

    return (new MonthCalendarWA1(getLanguage(), numbersDays));
  }

  // AJOUT : pour traiter l'affichage des semaines sur 5 ou 7 jours
  public boolean isWeekendNotVisible() {
    String parameterValue = getComponentParameterValue("weekendNotVisible");
    return "yes".equals(parameterValue.toLowerCase());
  }

  public Calendar getCurrentDay() {
    return currentDay;
  }

  public void setCurrentDay(Date date) {
    currentDay.setTime(date);
  }

  public void nextMonth() {
    currentDay.add(Calendar.MONTH, 1);
  }

  public void previousMonth() {
    currentDay.add(Calendar.MONTH, -1);
  }

  public void today() {
    currentDay = Calendar.getInstance();
  }

  public List<ReservationDetail> getReservationForValidation() {
    try {
      return getResourcesManagerBm().getReservationForValidation(getComponentId(),
              getCurrentDay().getTime(), getUserId(), getLanguage());
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.getMonthReservation()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public List<ReservationDetail> getMonthReservation() {
    try {
      return getResourcesManagerBm().getMonthReservation(getComponentId(),
              getCurrentDay().getTime(), getUserId(), getLanguage());
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.getMonthReservation()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public List<ReservationDetail> getMonthReservation(String idUser) {
    try {
      return getResourcesManagerBm().getMonthReservation(getComponentId(),
              this.getCurrentDay().getTime(), idUser, this.getLanguage());
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.getMonthReservation()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public List<ReservationDetail> getMonthReservationOfCategory(String idCategory) {
    try {
      return getResourcesManagerBm().getMonthReservationOfCategory(
              getComponentId(), getCurrentDay().getTime(), getUserId(),
              getLanguage(), idCategory);
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.getMonthReservationOfCategory()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public String initUPToSelectManager(String pubId) {
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString(
            "ApplicationURL");
    PairObject hostComponentName = new PairObject(getComponentLabel(), "");
    PairObject[] hostPath = new PairObject[1];
    hostPath[0] = new PairObject(
            getString("resourcesManagerSC.SelectManager"), "");
    String hostUrl = m_context + URLManager.getURL("useless", getComponentId())
            + "SetManager?PubId=" + pubId;
    String cancelUrl = m_context
            + URLManager.getURL("useless", getComponentId())
            + "SetManager?PubId=" + pubId;

    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName(getSpaceLabel());
    sel.setHostComponentName(hostComponentName);
    sel.setHostPath(hostPath);

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);

    sel.setHtmlFormName("pubForm");
    sel.setHtmlFormElementName("Valideur");
    sel.setHtmlFormElementId("ValideurId");

    // Contraintes
    sel.setMultiSelect(false);
    sel.setPopupMode(true);
    sel.setSetSelectable(false);

    // Add extra params
    SelectionUsersGroups sug = new SelectionUsersGroups();
    sug.setComponentId(getComponentId());
    ArrayList<String> profiles = new ArrayList<String>();
    profiles.add("publisher");
    profiles.add("admin");
    sug.setProfileNames(profiles);
    sel.setExtraParams(sug);
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  public String initUserPanelOtherPlanning() {
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString(
            "ApplicationURL");
    PairObject hostComponentName = new PairObject(
            getString("resourcesManager.accueil"), m_context
            + "/RresourcesManager/jsp/Main");
    PairObject[] hostPath = new PairObject[1];
    hostPath[0] = new PairObject(getString("resourcesManager.otherPlanning"),
            m_context + URLManager.getURL(null, getComponentId()) + "Main");
    String hostUrl = m_context + URLManager.getURL(null, getComponentId())
            + "ViewOtherPlanning";
    String cancelUrl = m_context + URLManager.getURL(null, getComponentId())
            + "Main";
    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName("");
    sel.setHostPath(hostPath);
    sel.setHostComponentName(hostComponentName);
    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);
    sel.setPopupMode(true);
    sel.setMultiSelect(false);
    sel.setSetSelectable(false);
    sel.setElementSelectable(true);
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  public UserDetail getSelectedUser() {
    Selection sel = getSelection();
    UserDetail selectedUser = null;
    String[] selectedUsers = sel.getSelectedElements();
    if (selectedUsers != null) {
      selectedUser = getUserDetail(selectedUsers[0]);
    }

    return selectedUser;
  }

  public boolean areCommentsEnabled() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("comments"));
  }

  public void validateResource(int resourceId, int reservationId) throws RemoteException,
          NotificationManagerException {
    getResourcesManagerBm().updateResourceStatus(ReservationDetail.STATUS_VALIDATE, resourceId,
            reservationId,
            getComponentId());
    ReservationDetail reservation = getReservation(Integer.toString(reservationId));
    reservation.setListResourcesReserved(getResourcesofReservation(Integer.toString(reservationId)));
    getResourcesManagerBm().updateReservation(reservation);
    // envoie d'une notification au créateur de la réservation quand cette dernière est totalement
    // validée
    if (ReservationDetail.STATUS_VALIDATE.equals(reservation.getStatus())) {
      sendNotificationValidateReservation(reservation);
    }
  }

  public void refuseResource(int resourceId, int reservationId, String motive)
          throws RemoteException, NotificationManagerException {
    getResourcesManagerBm().updateResourceStatus(ReservationDetail.STATUS_REFUSED, resourceId,
            reservationId, getComponentId());
    ReservationDetail reservation = getReservation(Integer.toString(reservationId));
    reservation.setListResourcesReserved(getResourcesofReservation(Integer.toString(reservationId)));
    getResourcesManagerBm().updateReservation(reservation);
    // envoie d'une notification au créateur de la réservation si cette desnière est refusée
    if (ReservationDetail.STATUS_REFUSED.equals(reservation.getStatus())) {
      sendNotificationRefuseReservation(reservation, Integer.toString(resourceId), motive);
    }
  }

  public void sendNotificationValidateReservation(ReservationDetail reservation)
          throws RemoteException,
          NotificationManagerException {
    // envoyer une notification au créateur de la réservation
    OrganizationController orga = new OrganizationController();
    String user = orga.getUserDetail(getUserId()).getDisplayedName();

    ResourceLocator message =
            new ResourceLocator("com.silverpeas.resourcesmanager.multilang.resourcesManagerBundle",
            "fr");
    ResourceLocator message_en =
            new ResourceLocator("com.silverpeas.resourcesmanager.multilang.resourcesManagerBundle",
            "en");

    StringBuffer messageBody = new StringBuffer();
    StringBuffer messageBody_en = new StringBuffer();

    // french notifications
    String subject = message.getString("resourcesManager.notifSubjectValide");
    messageBody.append(message.getString("resourcesManager.notifBodyValideBegin")).append(" '");
    messageBody.append(reservation.getEvent()).append("' ");
    messageBody.append(message.getString("resourcesManager.notifBodyValideEnd"));

    // english notifications
    String subject_en = message_en.getString("resourcesManager.notifSubjectValide");
    messageBody_en.append(message_en.getString("resourcesManager.notifBodyValideBegin"));
    messageBody_en.append(" '").append(reservation.getEvent()).append("' ");
    messageBody_en.append(message_en.getString("resourcesManager.notifBodyValideEnd"));

    NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.NORMAL,
            subject, messageBody.toString());
    notifMetaData.addLanguage("en", subject_en, messageBody_en.toString());

    notifMetaData.setLink(URLManager.getURL(null, getComponentId())
            + "ViewReservation?reservationId=" + reservation.getId());
    notifMetaData.setComponentId(getComponentId());
    notifMetaData.addUserRecipient(new UserRecipient(reservation.getUserId()));
    notifMetaData.setSender(user);
    // 2. envoie de la notification
    getNotificationSender().notifyUser(notifMetaData);
  }

  public void sendNotificationRefuseReservation(ReservationDetail reservation, String resourceId,
          String motive)
          throws RemoteException,
          NotificationManagerException {
    // envoyer une notification au créateur de la réservation
    OrganizationController orga = new OrganizationController();
    String user = orga.getUserDetail(getUserId()).getDisplayedName();

    ResourceLocator message =
            new ResourceLocator("com.silverpeas.resourcesmanager.multilang.resourcesManagerBundle",
            "fr");
    ResourceLocator message_en =
            new ResourceLocator("com.silverpeas.resourcesmanager.multilang.resourcesManagerBundle",
            "en");

    ResourceDetail resource = getResource(resourceId);
    StringBuffer messageBody = new StringBuffer();
    StringBuffer messageBody_en = new StringBuffer();

    // french notifications
    String subject = message.getString("resourcesManager.notifSubjectRefuse");
    messageBody =
            messageBody.append(message.getString("resourcesManager.notifBodyRefuseBegin")).append(
            " '").append(resource.getName()).append("' ").append(
            message.getString("resourcesManager.notifBodyRefuseMiddle")).append(" '").append(
            reservation.getEvent()).append("' ").append(
            message.getString("resourcesManager.notifBodyRefuseEnd")).append(
            message.getString("resourcesManager.notifBodyRefuseMotive")).append(" ").append(
            motive);

    // english notifications
    String subject_en = message_en.getString("resourcesManager.notifSubjectRefuse");
    messageBody_en =
            messageBody_en.append(message_en.getString("resourcesManager.notifBodyRefuseBegin")).
            append(" '").append(resource.getName()).append("' ").append(
            message_en.getString("resourcesManager.notifBodyRefuseMiddle")).append(" '").append(
            reservation.getEvent()).append("' ").append(
            message_en.getString("resourcesManager.notifBodyRefuseEnd")).append(
            message_en.getString("resourcesManager.notifBodyRefuseMotive")).append(" ").append(
            motive);

    NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.NORMAL,
            subject, messageBody.toString());
    notifMetaData.addLanguage("en", subject_en, messageBody_en.toString());

    notifMetaData.setLink(URLManager.getURL(null, getComponentId())
            + "ViewReservation?reservationId=" + reservation.getId());
    notifMetaData.setComponentId(getComponentId());
    notifMetaData.addUserRecipient(new UserRecipient(reservation.getUserId()));
    notifMetaData.setSender(user);
    // 2. envoie de la notification
    getNotificationSender().notifyUser(notifMetaData);
  }

  /**
   * Gets the resources associated with this session controller.
   * @return all of the resources (messages, settings, icons, ...)
   */
  public synchronized ResourcesWrapper getResources() {
    if (resources == null) {
      resources = new ResourcesWrapper(getMultilang(), getIcon(), getSettings(), getLanguage());
    }
    return resources;
  }
}