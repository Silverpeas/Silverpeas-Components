/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
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
package org.silverpeas.resourcesmanager.control;

import static org.silverpeas.resourcemanager.model.ResourceStatus.STATUS_FOR_VALIDATION;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.silverpeas.resourcemanager.ResourcesManagerFactory;
import org.silverpeas.resourcemanager.control.ResourcesManagerRuntimeException;
import org.silverpeas.resourcemanager.model.Category;
import org.silverpeas.resourcemanager.model.Reservation;
import org.silverpeas.resourcemanager.model.ReservedResource;
import org.silverpeas.resourcemanager.model.Resource;
import org.silverpeas.resourcemanager.model.ResourceValidator;

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
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.MonthCalendar;
import com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.MonthCalendarWA1;

import static org.silverpeas.resourcemanager.model.ResourceStatus.*;

public class ResourcesManagerSessionController extends AbstractComponentSessionController {

  private Reservation reservationCourante;
  private Calendar currentDay = Calendar.getInstance();
  private List<ReservedResource> resourceReserved = new ArrayList<ReservedResource>();
  private List<Resource> listReservableResource;
  private Date beginDateReservation;
  private Date endDateReservation;
  private String listReservationCurrent;
  private String provenanceResource;
  private String resourceIdForResource;
  private String reservationIdForResource;
  private String categoryIdForResource;
  private String objectViewForCalandar;
  private String firstNameUserCalandar;
  private String lastNameUserCalandar;
  private String currentCategory;
  private String currentResource;
  private String currentReservation;
  private NotificationSender notifSender;
  private ResourcesWrapper resources;

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
   *
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public ResourcesManagerSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
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

  /**
   * Creating a new Category.
   *
   * @param category the category to create.
   */
  public void createCategory(Category category) {
    category.setInstanceId(getComponentId());
    category.setCreaterId(getUserId());
    category.setUpdaterId(getUserId());
    category.setCreationDate(new Date());
    category.setUpdateDate(category.getCreationDate());
    ResourcesManagerFactory.getResourcesManager().createCategory(category);


  }

  public void updateCategory(Category category) {
    category.setInstanceId(getComponentId());
    category.setCreaterId(getUserId());
    category.setUpdaterId(getUserId());
    category.setUpdateDate(new Date());
    ResourcesManagerFactory.getResourcesManager().updateCategory(category);
  }

  public List<Category> getCategories() {
    return ResourcesManagerFactory.getResourcesManager().getCategories(getComponentId());
  }

  public Category getCategory(String id) {
    return ResourcesManagerFactory.getResourcesManager().getCategory(id);
  }

  public void deleteCategory(String id) {
    ResourcesManagerFactory.getResourcesManager().deleteCategory(id, getComponentId());
  }

  /**
   * Create a new resource.
   *
   * @param resource
   * @return
   */
  public String createResource(Resource resource) {
    resource.setInstanceId(getComponentId());
    resource.setCreaterId(getUserId());
    resource.setUpdaterId(getUserId());
    resource.setCreationDate(new Date());
    resource.setUpdateDate(resource.getCreationDate());
    return ResourcesManagerFactory.getResourcesManager().createResource(resource);
  }

  public void updateResource(Resource resource) {
    resource.setInstanceId(getComponentId());
    resource.setUpdaterId(getUserId());
    resource.setUpdateDate(new Date());
    ResourcesManagerFactory.getResourcesManager().updateResource(resource);
  }

  public void updateResource(Resource resource, List<Long> managers) {
    resource.setInstanceId(getComponentId());
    resource.setUpdaterId(getUserId());
    resource.setUpdateDate(new Date());
    ResourcesManagerFactory.getResourcesManager().updateResource(resource, managers);
  }

  public Resource getResource(String id) {
    return ResourcesManagerFactory.getResourcesManager().getResource(id);
  }

  public void deleteResource(String id) {
    ResourcesManagerFactory.getResourcesManager().deleteResource(id, getComponentId());
  }

  public List<Resource> getResourcesByCategory(String categoryId) {
    return ResourcesManagerFactory.getResourcesManager().getResourcesByCategory(categoryId);
  }

  public List<Resource> getResourcesReservable(Date startDate, Date endDate) {
    this.listReservableResource = ResourcesManagerFactory.getResourcesManager().
        getResourcesReservable(getComponentId(), startDate, endDate);
    return listReservableResource;

  }

  public List<Resource> getResourcesofReservation(String reservationId) {
    List<Resource> reservationResources = ResourcesManagerFactory.getResourcesManager().
        getResourcesofReservation(getComponentId(), reservationId);
    for (Resource resource : reservationResources) {
      resource.setStatus(ResourcesManagerFactory.getResourcesManager().
          getResourceOfReservationStatus(resource.getId(), reservationId));
    }
    return reservationResources;
  }

  public void createReservation(Reservation reservation) {
    reservation.setInstanceId(getComponentId());
    reservation.setUserId(getUserId());
    reservation.setCreationDate(new Date());
    reservation.setUpdateDate(reservation.getCreationDate());
    reservationCourante = reservation;
  }

  public void saveReservation() {
    try {
      // rechercher le statut à mettre sur la reservation
      ResourcesManagerFactory.getResourcesManager().saveReservation(reservationCourante,
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

  public List<Resource> verificationReservation(String listeReservation) {
    return ResourcesManagerFactory.getResourcesManager().verificationReservation(
        getComponentId(), listeReservation, beginDateReservation, endDateReservation);
  }

  public List<Resource> getResourcesProblemDate(String listeReservation, Date beginDate,
      Date endDate, String reservationId) {
    return ResourcesManagerFactory.getResourcesManager().verificationNewDateReservation(
        getComponentId(), listeReservation, beginDate, endDate, reservationId);
  }

  public List<Reservation> getReservationUser() {
    return ResourcesManagerFactory.getResourcesManager().getUserReservations(getComponentId(),
        getUserId());
  }

  public Reservation getReservation(String reservationId) {
    Reservation reservation = ResourcesManagerFactory.getResourcesManager().getReservation(
        getComponentId(), reservationId);
    reservation.setUserName(getUserDetail(reservation.getUserId()).getDisplayedName());
    return reservation;
  }

  public void updateReservation(String idReservation, String listReservation, boolean updateDate) {
    try {
      reservationCourante.setId(idReservation);
      ResourcesManagerFactory.getResourcesManager().updateReservation(reservationCourante,
          listReservation,
          updateDate);
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
    ResourcesManagerFactory.getResourcesManager().deleteReservation(id, getComponentId());
  }

  public void sendNotificationForValidation(String resourceId, String reservationId)
      throws RemoteException, NotificationManagerException {
    Resource resource = getResource(resourceId);
    String status = ResourcesManagerFactory.getResourcesManager().getResourceOfReservationStatus(
        resourceId, reservationId);
    if (STATUS_FOR_VALIDATION.equals(status)) {
      // envoyer une notification aux responsables de la ressource
      OrganizationController orga = new OrganizationController();
      String user = orga.getUserDetail(getUserId()).getDisplayedName();

      ResourceLocator message = new ResourceLocator(
          "com.silverpeas.resourcesmanager.multilang.resourcesManagerBundle", "fr");
      ResourceLocator message_en = new ResourceLocator(
          "com.silverpeas.resourcesmanager.multilang.resourcesManagerBundle", "en");

      StringBuffer messageBody = new StringBuffer();
      StringBuffer messageBody_en = new StringBuffer();

      // liste des responsables (de la ressource) à notifier
      List<ResourceValidator> validators = ResourcesManagerFactory.getResourcesManager().
          getManagers(resource.getIntegerId());
      List<UserRecipient> managers = new ArrayList<UserRecipient>(validators.size());
      if (!ResourcesManagerFactory.getResourcesManager().isManager(Long.parseLong(getUserId()),
          Long.parseLong(resourceId))) {
        // envoie de la notification seulement si le user courant n'est pas aussi responsable
        for (ResourceValidator validator : validators) {
          managers.add(new UserRecipient(String.valueOf(validator.getManagerId())));
        }

        // french notifications
        String subject = message.getString("resourcesManager.notifSubject");
        messageBody = messageBody.append(user).append(" ").append(message.getString(
            "resourcesManager.notifBody")).append(" ").append(resource.getName());

        // english notifications
        String subject_en = message_en.getString("resourcesManager.notifSubject");
        messageBody_en = messageBody_en.append(user).append(" ").append(
            message.getString("resourcesManager.notifBody")).append(" ").append(
            resource.getName());

        NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.NORMAL,
            subject, messageBody.toString());
        notifMetaData.addLanguage("en", subject_en, messageBody_en.toString());

        notifMetaData.setLink(URLManager.getURL(null, getComponentId()) +
            "ViewReservation?reservationId=" + reservationId);
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
    ResourcesManagerFactory.getResourcesManager().addManager(Long.parseLong(resourceId),
        Long.parseLong(managerId));
  }

  public void removeManager(String resourceId, String managerId) {
    ResourcesManagerFactory.getResourcesManager().removeManager(Long.parseLong(resourceId),
        Long.parseLong(managerId));
  }

  public List<String> getManagerIds(String resourceId) {
    List<ResourceValidator> validators = listValidators(resourceId);
    List<String> managerIds = new ArrayList<String>(validators.size());
    for (ResourceValidator validator : validators) {
      managerIds.add(String.valueOf(validator.getManagerId()));
    }
    return managerIds;
  }

  public List<UserDetail> getManagers(String resourceId) {
    List<ResourceValidator> validators = listValidators(resourceId);
    // ajouter le nom du responsable
    List<UserDetail> managers = new ArrayList<UserDetail>(validators.size());
    for (ResourceValidator validator : validators) {
      UserDetail manager = getUserDetail(String.valueOf(validator.getManagerId()));
      managers.add(manager);
    }
    return managers;
  }

  public List<ResourceValidator> listValidators(String resourceId) {
    return ResourcesManagerFactory.getResourcesManager().getManagers(Long.parseLong(resourceId));
  }

  public String initUserSelect(Collection<String> currentManagers) {
    String m_context = URLManager.getApplicationURL();
    String hostUrl = m_context + URLManager.getURL(getSpaceId(), getComponentId()) + "FromUserSelect";

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
    for (String currentManager : currentManagers) {
      users[i] = currentManager;
      i++;
    }
    sel.setSelectedElements(users);

    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  public Reservation getReservationCourante() {
    return reservationCourante;
  }

  public void setReservationCourante(Reservation reservationCourante) {
    this.reservationCourante = reservationCourante;
  }

  public List<Resource> getResourcesReservable() {
    return listReservableResource;
  }

  public void setResourcesReservable(List<Resource> listReservableResource) {
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

  public List<ReservedResource> getResourceReserved() {
    return resourceReserved;
  }

  public void setResourceReserved(ReservedResource resource) {
    this.resourceReserved.add(resource);
  }

  public String getListReservationCurrent() {
    return listReservationCurrent;
  }

  public void setListReservationCurrent(String listReservationCurrent) {
    this.listReservationCurrent = listReservationCurrent;
  }

  /**
   * Gestion de l'almanach *
   */
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

  public List<Reservation> getReservationForValidation() {
    return ResourcesManagerFactory.getResourcesManager().getReservationForValidation(
        getComponentId(), getCurrentDay().getTime(), getUserId());
  }

  public List<Reservation> getMonthReservation() {
    return ResourcesManagerFactory.getResourcesManager().getMonthReservationOfUser(getComponentId(),
        getCurrentDay().getTime(),  Integer.parseInt(getUserId()));
  }

  public List<Reservation> getMonthReservation(String idUser) {
    return ResourcesManagerFactory.getResourcesManager().getMonthReservationOfUser(getComponentId(),
        this.getCurrentDay().getTime(), Integer.parseInt(idUser));
  }

  public List<Reservation> getMonthReservationOfCategory(String idCategory) {
    return ResourcesManagerFactory.getResourcesManager().
        listReservationsOfMonthInCategoryForUser(getCurrentDay().getTime(), idCategory,
        getUserId());
  }

  public String initUPToSelectManager(String pubId) {
    PairObject hostComponentName = new PairObject(getComponentLabel(), "");
    PairObject[] hostPath = new PairObject[1];
    hostPath[0] = new PairObject(getString("resourcesManagerSC.SelectManager"), "");
    String hostUrl = URLManager.getApplicationURL() + URLManager.getURL("useless", getComponentId()) +
        "SetManager?PubId=" + pubId;
    String cancelUrl = URLManager.getApplicationURL() + URLManager.getURL("useless",
        getComponentId()) + "SetManager?PubId=" + pubId;

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
    ArrayList<String> profiles = new ArrayList<String>(2);
    profiles.add(SilverpeasRole.publisher.toString());
    profiles.add(SilverpeasRole.admin.toString());
    sug.setProfileNames(profiles);
    sel.setExtraParams(sug);
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  public String initUserPanelOtherPlanning() {
    PairObject hostComponentName = new PairObject(getString("resourcesManager.accueil"), URLManager.
        getApplicationURL() + "/RresourcesManager/jsp/Main");
    PairObject[] hostPath = new PairObject[1];
    hostPath[0] = new PairObject(getString("resourcesManager.otherPlanning"),
        URLManager.getApplicationURL() + URLManager.getURL(null, getComponentId()) + "Main");
    String hostUrl = URLManager.getApplicationURL() + URLManager.getURL(null, getComponentId()) +
        "ViewOtherPlanning";
    String cancelUrl =
        URLManager.getApplicationURL() + URLManager.getURL(null, getComponentId()) +
        "Main";
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
    ResourcesManagerFactory.getResourcesManager().updateReservedResourceStatus(reservationId,
        resourceId, STATUS_VALIDATE);
    Reservation reservation = getReservation(Integer.toString(reservationId));
    // envoie d'une notification au créateur de la réservation quand cette dernière est totalement
    // validée
    if (reservation.isValidated()) {
      sendNotificationValidateReservation(reservation);
    }
  }

  public void refuseResource(int resourceId, int reservationId, String motive)
      throws RemoteException, NotificationManagerException {
    ResourcesManagerFactory.getResourcesManager().updateReservedResourceStatus(reservationId,
        resourceId, STATUS_REFUSED);
    Reservation reservation = getReservation(Integer.toString(reservationId));
    // envoie d'une notification au créateur de la réservation si cette desnière est refusée
    if (reservation.isRefused()) {
      sendNotificationRefuseReservation(reservation, Integer.toString(resourceId), motive);
    }
  }

  public void sendNotificationValidateReservation(Reservation reservation)
      throws NotificationManagerException {
    // envoyer une notification au créateur de la réservation
    OrganizationController orga = new OrganizationController();
    String user = orga.getUserDetail(getUserId()).getDisplayedName();

    ResourceLocator message = new ResourceLocator(
        "com.silverpeas.resourcesmanager.multilang.resourcesManagerBundle", "fr");
    ResourceLocator message_en = new ResourceLocator(
        "com.silverpeas.resourcesmanager.multilang.resourcesManagerBundle", "en");

    StringBuilder messageBody = new StringBuilder();
    StringBuilder messageBody_en = new StringBuilder();

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

    notifMetaData.setLink(URLManager.getURL(null, getComponentId()) +
        "ViewReservation?reservationId=" + reservation.getId());
    notifMetaData.setComponentId(getComponentId());
    notifMetaData.addUserRecipient(new UserRecipient(reservation.getUserId()));
    notifMetaData.setSender(user);
    // 2. envoie de la notification
    getNotificationSender().notifyUser(notifMetaData);
  }

  public void sendNotificationRefuseReservation(Reservation reservation, String resourceId,
      String motive) throws NotificationManagerException {
    // envoyer une notification au créateur de la réservation
    OrganizationController orga = new OrganizationController();
    String user = orga.getUserDetail(getUserId()).getDisplayedName();

    ResourceLocator message =
        new ResourceLocator("com.silverpeas.resourcesmanager.multilang.resourcesManagerBundle",
        "fr");
    ResourceLocator message_en =
        new ResourceLocator("com.silverpeas.resourcesmanager.multilang.resourcesManagerBundle",
        "en");

    Resource resource = getResource(resourceId);
    StringBuilder messageBody = new StringBuilder(512);
    StringBuilder messageBody_en = new StringBuilder(512);

    // french notifications
    String subject = message.getString("resourcesManager.notifSubjectRefuse");
    messageBody.append(message.getString("resourcesManager.notifBodyRefuseBegin")).append(" '");
    messageBody.append(resource.getName()).append("' ");
    messageBody.append(message.getString("resourcesManager.notifBodyRefuseMiddle")).append(" '");
    messageBody.append(reservation.getEvent()).append("' ");
    messageBody.append(message.getString("resourcesManager.notifBodyRefuseEnd"));
    messageBody.append(message.getString("resourcesManager.notifBodyRefuseMotive")).append(" ");
    messageBody.append(motive);

    // english notifications
    String subject_en = message_en.getString("resourcesManager.notifSubjectRefuse");
    messageBody_en.append(message_en.getString("resourcesManager.notifBodyRefuseBegin"));
    messageBody_en.append(" '").append(resource.getName()).append("' ");
    messageBody_en.append(message_en.getString("resourcesManager.notifBodyRefuseMiddle"));
    messageBody_en.append(" '").append(reservation.getEvent()).append("' ");
    messageBody_en.append(message_en.getString("resourcesManager.notifBodyRefuseEnd"));
    messageBody_en.append(message_en.getString("resourcesManager.notifBodyRefuseMotive"));
    messageBody_en.append(" ").append(motive);

    NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.NORMAL,
        subject, messageBody.toString());
    notifMetaData.addLanguage("en", subject_en, messageBody_en.toString());

    notifMetaData.setLink(URLManager.getURL(null, getComponentId()) +
        "ViewReservation?reservationId=" + reservation.getId());
    notifMetaData.setComponentId(getComponentId());
    notifMetaData.addUserRecipient(new UserRecipient(reservation.getUserId()));
    notifMetaData.setSender(user);
    // 2. envoie de la notification
    getNotificationSender().notifyUser(notifMetaData);
  }

  /**
   * Gets the resources associated with this session controller.
   *
   * @return all of the resources (messages, settings, icons, ...)
   */
  public synchronized ResourcesWrapper getResources() {
    if (resources == null) {
      resources = new ResourcesWrapper(getMultilang(), getIcon(), getSettings(), getLanguage());
    }
    return resources;
  }
}
