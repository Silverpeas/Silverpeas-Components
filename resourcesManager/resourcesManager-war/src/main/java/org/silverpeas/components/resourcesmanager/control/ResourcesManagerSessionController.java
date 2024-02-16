/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.resourcesmanager.control;


import org.silverpeas.components.resourcesmanager.ResourcesManagerProvider;
import org.silverpeas.components.resourcesmanager.model.Category;
import org.silverpeas.components.resourcesmanager.model.Reservation;
import org.silverpeas.components.resourcesmanager.model.Resource;
import org.silverpeas.components.resourcesmanager.model.ResourceStatus;
import org.silverpeas.components.resourcesmanager.model.ResourceValidator;
import org.silverpeas.components.resourcesmanager.service.ResourcesManagerRuntimeException;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.notification.NotificationException;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationParameters;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.Link;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.kernel.util.Pair;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.selection.SelectionUsersGroups;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class ResourcesManagerSessionController extends AbstractComponentSessionController {

  private ReservationTimeWindowViewContext viewContext = null;

  private Reservation reservationCourante;
  private Date beginDateReservation;
  private Date endDateReservation;
  private List<Long> listReservationCurrent;
  private String provenanceResource;
  private Long resourceIdForResource;
  private Long reservationIdForResource;
  private Long categoryIdForResource;
  private Long currentResource;
  private NotificationSender notifSender;
  private MultiSilverpeasBundle resources;

  public ReservationTimeWindowViewContext getViewContext() {
    if (viewContext == null) {
      // Initialization
      viewContext =
          new ReservationTimeWindowViewContext(getComponentId(), getUserDetail(), getLanguage(),
              getPersonalization().getZoneId());
    }
    return viewContext;
  }

  public Long getCurrentResource() {
    return currentResource;
  }

  public void setCurrentResource(Long currentResource) {
    this.currentResource = currentResource;
  }

  public Long getCategoryIdForResource() {
    return categoryIdForResource;
  }

  public void setCategoryIdForResource(Long categoryIdForResource) {
    this.categoryIdForResource = categoryIdForResource;
  }

  public Long getReservationIdForResource() {
    return reservationIdForResource;
  }

  public void setReservationIdForResource(Long reservationIdForResource) {
    this.reservationIdForResource = reservationIdForResource;
  }

  public Long getResourceIdForResource() {
    return resourceIdForResource;
  }

  public void setResourceIdForResource(Long resourceIdForResource) {
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
   *
   */
  public ResourcesManagerSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.resourcesmanager.multilang.resourcesManagerBundle",
        "org.silverpeas.resourcesmanager.settings.resourcesManagerIcons");
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
   * @param category the category to create.
   */
  public void createCategory(Category category) {
    category.setInstanceId(getComponentId());
    category.setCreaterId(getUserId());
    category.setUpdaterId(getUserId());
    ResourcesManagerProvider.getResourcesManager().createCategory(category);
  }

  public void updateCategory(Category category) {
    category.setInstanceId(getComponentId());
    category.setCreaterId(getUserId());
    category.setUpdaterId(getUserId());
    ResourcesManagerProvider.getResourcesManager().updateCategory(category);
  }

  public List<Category> getCategories() {
    return ResourcesManagerProvider.getResourcesManager().getCategories(getComponentId());
  }

  public Category getCategory(Long id) {
    return ResourcesManagerProvider.getResourcesManager().getCategory(id);
  }

  public void deleteCategory(Long id) {
    ResourcesManagerProvider.getResourcesManager().deleteCategory(id, getComponentId());
  }

  /**
   * Create a new resource.
   * @param resource the resource to create
   */
  public void createResource(Resource resource) {
    resource.setInstanceId(getComponentId());
    resource.setCreaterId(getUserId());
    resource.setUpdaterId(getUserId());
    ResourcesManagerProvider.getResourcesManager().createResource(resource);
  }

  public void updateResource(Resource resource, List<Long> managers) {
    resource.setInstanceId(getComponentId());
    resource.setUpdaterId(getUserId());
    ResourcesManagerProvider.getResourcesManager().updateResource(resource, managers);
  }

  public Resource getResource(Long id) {
    return ResourcesManagerProvider.getResourcesManager().getResource(id);
  }

  public void deleteResource(Long id) {
    ResourcesManagerProvider.getResourcesManager().deleteResource(id, getComponentId());
  }

  public List<Resource> getResourcesByCategory(Long categoryId) {
    return ResourcesManagerProvider.getResourcesManager().getResourcesByCategory(categoryId);
  }

  public List<Resource> getResourcesReservable(Date startDate, Date endDate) {
    return ResourcesManagerProvider.getResourcesManager().
        getResourcesReservable(getComponentId(), startDate, endDate);
  }

  public List<Resource> getResourcesofReservation(Long reservationId) {
    List<Resource> reservationResources = ResourcesManagerProvider.getResourcesManager().
        getResourcesOfReservation(getComponentId(), reservationId);
    for (Resource resource : reservationResources) {
      resource.setStatus(ResourcesManagerProvider.getResourcesManager().
          getResourceOfReservationStatus(resource.getIdAsLong(), reservationId));
    }
    return reservationResources;
  }

  public void createReservation(Reservation reservation) {
    reservation.setInstanceId(getComponentId());
    reservation.setUserId(getUserId());
    reservationCourante = reservation;
  }

  public void saveReservation() {
    try {
      // rechercher le statut à mettre sur la reservation
      ResourcesManagerProvider.getResourcesManager()
          .saveReservation(reservationCourante, listReservationCurrent);
      // envoi d'une notification pour validation aux responsables des ressources selectionnées.
      for (Long resourceId : listReservationCurrent) {
        sendNotificationForValidation(resourceId, reservationCourante.getIdAsLong());
      }
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerSessionController.saveReservation()", SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public List<Resource> verifyUnavailableResources(List<Long> aimedResources) {
    return ResourcesManagerProvider.getResourcesManager()
        .getReservedResources(getComponentId(), aimedResources, beginDateReservation,
            endDateReservation);
  }

  public List<Resource> verifyUnavailableResources(List<Long> aimedResources, Date beginDate,
      Date endDate, Long reservationId) {
    return ResourcesManagerProvider.getResourcesManager()
        .getReservedResources(getComponentId(), aimedResources, beginDate, endDate, reservationId);
  }

  public List<Reservation> getReservationUser() {
    return ResourcesManagerProvider.getResourcesManager()
        .getUserReservations(getComponentId(), getUserId());
  }

  public Reservation getReservation(Long reservationId) {
    Reservation reservation = ResourcesManagerProvider.getResourcesManager()
        .getReservation(getComponentId(), reservationId);
    reservation.setUserName(getUserDetail(reservation.getUserId()).getDisplayedName());
    return reservation;
  }

  public void updateReservation(Reservation reservation, List<Long> resourceIds,
      boolean updateDate) {
    try {
      reservation.setUserId(reservationCourante.getUserId());
      reservation.setInstanceId(reservationCourante.getInstanceId());
      reservation.setEvent(reservationCourante.getEvent());
      reservation.setBeginDate(reservationCourante.getBeginDate());
      reservation.setEndDate(reservationCourante.getEndDate());
      reservation.setReason(reservationCourante.getReason());
      reservation.setPlace(reservationCourante.getPlace());
      reservationCourante = reservation;
      ResourcesManagerProvider.getResourcesManager()
          .updateReservation(reservation, resourceIds, updateDate);
      for (Long resourceId : resourceIds) {
        sendNotificationForValidation(resourceId, reservation.getIdAsLong());
      }
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerSessionController.updateReservation()", SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public void deleteReservation(Long id) {
    ResourcesManagerProvider.getResourcesManager().deleteReservation(id, getComponentId());
  }

  public void sendNotificationForValidation(Long resourceId, Long reservationId)
      throws NotificationException {
    Resource resource = getResource(resourceId);
    String status = ResourcesManagerProvider.getResourcesManager()
        .getResourceOfReservationStatus(resourceId, reservationId);
    if (ResourceStatus.STATUS_FOR_VALIDATION.equals(status)) {
      // envoyer une notification aux responsables de la ressource
      OrganizationController orga = OrganizationControllerProvider.getOrganisationController();
      String user = orga.getUserDetail(getUserId()).getDisplayedName();

      LocalizationBundle message = ResourceLocator.getLocalizationBundle(
          "org.silverpeas.resourcesmanager.multilang.resourcesManagerBundle",
          DisplayI18NHelper.getDefaultLanguage());

      StringBuilder messageBody = new StringBuilder();

      // liste des responsables (de la ressource) à notifier
      List<ResourceValidator> validators = ResourcesManagerProvider.getResourcesManager().
          getManagers(resource.getIdAsLong());
      List<UserRecipient> managers = new ArrayList<UserRecipient>(validators.size());
      if (!ResourcesManagerProvider.getResourcesManager()
          .isManager(Long.parseLong(getUserId()), resourceId)) {
        // envoie de la notification seulement si le user courant n'est pas aussi responsable
        for (ResourceValidator validator : validators) {
          managers.add(new UserRecipient(String.valueOf(validator.getManagerId())));
        }
        String url = URLUtil.getURL(null, getComponentId()) +
            "ViewReservation?reservationId=" + reservationId;

        String subject = message.getString("resourcesManager.notifSubject");
        messageBody = messageBody.append(user).append(" ").append(message.getString(
            "resourcesManager.notifBody")).append(" '").append(resource.getName()).append("'");

        NotificationMetaData notifMetaData =
            new NotificationMetaData(NotificationParameters.PRIORITY_NORMAL, subject,
                messageBody.toString());

        for (String language : DisplayI18NHelper.getLanguages()) {
          message = ResourceLocator.getLocalizationBundle(
              "org.silverpeas.resourcesmanager.multilang.resourcesManagerBundle", language);
          subject = message.getString("resourcesManager.notifSubject");
          messageBody = new StringBuilder();
          messageBody = messageBody.append(user).append(" ").append(message.getString(
              "resourcesManager.notifBody")).append(" '").append(resource.getName()).append("'.");
          notifMetaData.addLanguage(language, subject, messageBody.toString());

          Link link = new Link(url, message.getString("resourcesManager.notifReservationLinkLabel"));
          notifMetaData.setLink(link, language);
        }

        notifMetaData.addUserRecipients(managers);
        setMetaData(notifMetaData);
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

  public List<String> getManagerIds(Long resourceId) {
    List<ResourceValidator> validators = listValidators(resourceId);
    List<String> managerIds = new ArrayList<>(validators.size());
    for (ResourceValidator validator : validators) {
      managerIds.add(String.valueOf(validator.getManagerId()));
    }
    return managerIds;
  }

  public List<UserDetail> getManagers(Long resourceId) {
    List<ResourceValidator> validators = listValidators(resourceId);
    // ajouter le nom du responsable
    List<UserDetail> managers = new ArrayList<>(validators.size());
    for (ResourceValidator validator : validators) {
      UserDetail manager = getUserDetail(String.valueOf(validator.getManagerId()));
      managers.add(manager);
    }
    return managers;
  }

  public List<ResourceValidator> listValidators(Long resourceId) {
    return ResourcesManagerProvider.getResourcesManager().getManagers(resourceId);
  }

  public String initUserSelect(Collection<String> currentManagers) {
    String m_context = URLUtil.getApplicationURL();
    String hostUrl =
        m_context + URLUtil.getURL(getSpaceId(), getComponentId()) + "FromUserSelect";

    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName(getSpaceLabel());
    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(hostUrl);

    sel.setMultiSelect(true);
    sel.setPopupMode(false);
    sel.setSetSelectable(false);

    Pair<String, String> hostComponentName = new Pair<>(getComponentLabel(), null);
    sel.setHostPath(null);
    sel.setHostComponentName(hostComponentName);

    ArrayList<String> roles = new ArrayList<>();
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

    return Selection.getSelectionURL();
  }

  public Reservation getReservationCourante() {
    return reservationCourante;
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

  public void setListReservationCurrent(List<Long> listReservationCurrent) {
    this.listReservationCurrent = listReservationCurrent;
  }

  // AJOUT : pour traiter l'affichage des semaines sur 5 ou 7 jours
  public boolean isWeekendNotVisible() {
    String parameterValue = getComponentParameterValue("weekendNotVisible");
    return "yes".equals(parameterValue.toLowerCase());
  }

  public String getDefaultView() {
    String defaultView = getComponentParameterValue("defaultDisplay");
    if (StringUtil.isNotDefined(defaultView)) {
      defaultView = "allReservations";
    }
    return defaultView;
  }

  public String initUPToSelectManager(String pubId) {
    Pair<String, String> hostComponentName = new Pair<>(getComponentLabel(), "");
    Pair<String, String>[] hostPath = new Pair[1];
    hostPath[0] = new Pair<>(getString("resourcesManagerSC.SelectManager"), "");
    String hostUrl =
        URLUtil.getApplicationURL() + URLUtil.getURL("useless", getComponentId()) +
            "SetManager?PubId=" + pubId;
    String cancelUrl =
        URLUtil.getApplicationURL() + URLUtil.getURL("useless", getComponentId()) +
            "SetManager?PubId=" + pubId;

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
    ArrayList<String> profiles = new ArrayList<>(2);
    profiles.add(SilverpeasRole.PUBLISHER.toString());
    profiles.add(SilverpeasRole.ADMIN.toString());
    sug.setProfileNames(profiles);
    sel.setExtraParams(sug);
    return Selection.getSelectionURL();
  }

  public String initUserPanelOtherPlanning() {
    Pair<String, String> hostComponentName =
        new Pair<>(getString("resourcesManager.accueil"), URLUtil.
            getApplicationURL() + "/RresourcesManager/jsp/Main");
    Pair<String, String>[] hostPath = new Pair[1];
    hostPath[0] = new Pair<>(getString("resourcesManager.otherPlanning"),
        URLUtil.getApplicationURL() + URLUtil.getURL(null, getComponentId()) + "Main");
    String hostUrl = URLUtil.getApplicationURL() + URLUtil.getURL(null, getComponentId()) +
        "ViewOtherPlanning";
    String cancelUrl = URLUtil.getApplicationURL() + URLUtil.getURL(null, getComponentId()) +
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
    return Selection.getSelectionURL();
  }

  public UserDetail getSelectedUser() {
    Selection sel = getSelection();
    UserDetail selectedUser = null;
    String[] selectedUsers = sel.getSelectedElements();
    if (selectedUsers != null && selectedUsers.length > 0) {
      selectedUser = getUserDetail(selectedUsers[0]);
    }
    return selectedUser;
  }

  public boolean areCommentsEnabled() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("comments"));
  }

  public void validateResource(Long resourceId, Long reservationId)
      throws NotificationException {
    ResourcesManagerProvider.getResourcesManager()
        .updateReservedResourceStatus(reservationId, resourceId, ResourceStatus.STATUS_VALIDATE);
    Reservation reservation = getReservation(reservationId);
    // envoie d'une notification au créateur de la réservation quand cette dernière est totalement
    // validée
    if (reservation.isValidated()) {
      sendNotificationValidateReservation(reservation);
    }
  }

  public void refuseResource(Long resourceId, Long reservationId, String motive)
      throws NotificationException {
    ResourcesManagerProvider.getResourcesManager()
        .updateReservedResourceStatus(reservationId, resourceId, ResourceStatus.STATUS_REFUSED);
    Reservation reservation = getReservation(reservationId);
    // envoie d'une notification au créateur de la réservation si cette desnière est refusée
    if (reservation.isRefused()) {
      sendNotificationRefuseReservation(reservation, resourceId, motive);
    }
  }

  private String getMessageBodyValidReservation(LocalizationBundle message, Reservation reservation) {
    StringBuilder messageBody = new StringBuilder();
    messageBody.append(message.getString("resourcesManager.notifBodyValideBegin")).append(" '");
    messageBody.append(reservation.getEvent()).append("' ");
    messageBody.append(message.getString("resourcesManager.notifBodyValideEnd"));
    return messageBody.toString();
  }

  public void sendNotificationValidateReservation(Reservation reservation)
      throws NotificationException {
    // envoyer une notification au créateur de la réservation
    OrganizationController orga = OrganizationControllerProvider.getOrganisationController();
    String user = orga.getUserDetail(getUserId()).getDisplayedName();
    String url = URLUtil.getURL(null, getComponentId()) +
        "ViewReservation?reservationId=" + reservation.getId();

    LocalizationBundle message = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.resourcesmanager.multilang.resourcesManagerBundle",
        DisplayI18NHelper.getDefaultLanguage());

    String subject = message.getString("resourcesManager.notifSubjectValide");

    NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.PRIORITY_NORMAL,
        subject, getMessageBodyValidReservation(message, reservation));

    for (String language : DisplayI18NHelper.getLanguages()) {
      message = ResourceLocator.getLocalizationBundle(
          "org.silverpeas.resourcesmanager.multilang.resourcesManagerBundle", language);
      subject = message.getString("resourcesManager.notifSubjectValide");
      notifMetaData.addLanguage(language, subject, getMessageBodyValidReservation(message, reservation));

      Link link = new Link(url, message.getString("resourcesManager.notifReservationLinkLabel"));
      notifMetaData.setLink(link, language);
    }

    notifMetaData.addUserRecipient(new UserRecipient(reservation.getUserId()));
    setMetaData(notifMetaData);

    // 2. envoie de la notification
    getNotificationSender().notifyUser(notifMetaData);
  }

  private String getMessageBodyRefusedReservation(LocalizationBundle message, Resource resource,
      Reservation reservation, String motive) {
    StringBuilder messageBody = new StringBuilder();
    messageBody.append(message.getString("resourcesManager.notifBodyRefuseBegin")).append(" '");
    messageBody.append(resource.getName()).append("' ");
    messageBody.append(message.getString("resourcesManager.notifBodyRefuseMiddle")).append(" '");
    messageBody.append(reservation.getEvent()).append("' ");
    messageBody.append(message.getString("resourcesManager.notifBodyRefuseEnd"));
    messageBody.append(" ");
    messageBody.append(message.getString("resourcesManager.notifBodyRefuseMotive"));
    return messageBody.toString();
  }

  public void sendNotificationRefuseReservation(Reservation reservation, Long resourceId,
      String motive) throws NotificationException {
    // envoyer une notification au créateur de la réservation
    OrganizationController orga = OrganizationControllerProvider.getOrganisationController();
    String user = orga.getUserDetail(getUserId()).getDisplayedName();
    String url = URLUtil.getURL(null, getComponentId()) +
        "ViewReservation?reservationId=" + reservation.getId();

    LocalizationBundle message = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.resourcesmanager.multilang.resourcesManagerBundle",
            DisplayI18NHelper.getDefaultLanguage());

    Resource resource = getResource(resourceId);

    String subject = message.getString("resourcesManager.notifSubjectRefuse");

    NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.PRIORITY_NORMAL,
        subject, getMessageBodyRefusedReservation(message, resource, reservation, motive));

    for (String language : DisplayI18NHelper.getLanguages()) {
      message = ResourceLocator.getLocalizationBundle(
          "org.silverpeas.resourcesmanager.multilang.resourcesManagerBundle",
              language);
      subject = message.getString("resourcesManager.notifSubjectRefuse");

      notifMetaData.addLanguage(language, subject,
          getMessageBodyRefusedReservation(message, resource, reservation, motive));
      notifMetaData.addExtraMessage(motive, language);

      Link link = new Link(url, message.getString("resourcesManager.notifReservationLinkLabel"));
      notifMetaData.setLink(link, language);
    }

    notifMetaData.addUserRecipient(new UserRecipient(reservation.getUserId()));
    setMetaData(notifMetaData);

    // 2. envoie de la notification
    getNotificationSender().notifyUser(notifMetaData);
  }

  private void setMetaData(NotificationMetaData notifMetaData) {
    notifMetaData.setComponentId(getComponentId());
    notifMetaData.setSender(getUserId());
  }

  /**
   * Gets the resources associated with this session controller.
   * @return all of the resources (messages, settings, icons, ...)
   */
  public synchronized MultiSilverpeasBundle getResources() {
    if (resources == null) {
      resources =
          new MultiSilverpeasBundle(getMultilang(), getIcon(), getSettings(), getLanguage());
    }
    return resources;
  }
}
