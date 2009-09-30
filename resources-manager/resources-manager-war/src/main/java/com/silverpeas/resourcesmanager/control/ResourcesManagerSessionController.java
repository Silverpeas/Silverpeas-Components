/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
import java.util.Date;
import java.util.List;

import com.silverpeas.resourcesmanager.control.ejb.ResourcesManagerBm;
import com.silverpeas.resourcesmanager.control.ejb.ResourcesManagerBmHome;
import com.silverpeas.resourcesmanager.model.CategoryDetail;
import com.silverpeas.resourcesmanager.model.ReservationDetail;
import com.silverpeas.resourcesmanager.model.ResourceDetail;
import com.silverpeas.resourcesmanager.model.ResourceReservableDetail;
import com.silverpeas.resourcesmanager.model.ResourcesManagerRuntimeException;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.MonthCalendar;
import com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.MonthCalendarWA1;

public class ResourcesManagerSessionController extends
    AbstractComponentSessionController {
  private ReservationDetail reservationCourante;
  private Calendar currentDay = Calendar.getInstance();
  private List resourceReserved = new ArrayList();
  private List listReservableResource;
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
   * 
   * @param mainSessionCtrl
   *          The user's profile
   * @param componentContext
   *          The component's profile
   * 
   * @see
   */
  public ResourcesManagerSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.silverpeas.resourcesmanager.multilang.resourcesManagerBundle",
        "com.silverpeas.resourcesmanager.settings.resourcesManagerIcons");
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

  public List getCategories() {
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

  public List getResourcesByCategory(String categoryId) {
    try {
      return getResourcesManagerBm().getResourcesByCategory(categoryId);
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerSessionController.getResourcesByCategory()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public List getResourcesReservable(Date startDate, Date endDate) {
    List listeResources = new ArrayList();

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

  public List getResourcesofReservation(String reservationId) {
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
      getResourcesManagerBm().saveReservation(reservationCourante,
          listReservationCurrent);
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerSessionController.saveReservation()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public List verificationReservation(String listeReservation) {
    try {
      return getResourcesManagerBm().verificationReservation(getComponentId(),
          listeReservation, beginDateReservation, endDateReservation);
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerSessionController.verificationReservation()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public List getResourcesProblemDate(String listeReservation, Date beginDate,
      Date endDate, String reservationId) {
    try {
      return getResourcesManagerBm()
          .verificationNewDateReservation(getComponentId(), listeReservation,
              beginDate, endDate, reservationId);
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerSessionController.getResourcesProblemDate()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public List getReservationUser() {
    try {
      String idUser = getUserId();
      String idComponent = getComponentId();
      List list = getResourcesManagerBm().getReservationUser(idComponent,
          idUser);
      return list;
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
      reservation.setUserName(getUserDetail(reservation.getUserId())
          .getDisplayedName());
      return reservation;
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerSessionController.getReservation()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public void updateReservation(String idModifiedReservation,
      String listReservation) {
    try {
      reservationCourante.setId(idModifiedReservation);
      getResourcesManagerBm().updateReservation(listReservation,
          reservationCourante);
    } catch (RemoteException e) {
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

  private ResourcesManagerBm getResourcesManagerBm() {
    ResourcesManagerBm resourcesmanagerBm = null;
    try {
      ResourcesManagerBmHome resourcesmanagerBmHome = (ResourcesManagerBmHome) EJBUtilitaire
          .getEJBObjectRef("ejb/ResourcesManagerBm",
              ResourcesManagerBmHome.class);
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

  public ArrayList getResourcesReservable() {
    return (ArrayList) listReservableResource;
  }

  public void setResourcesReservable(ArrayList listReservableResource) {
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

  public List getResourceReserved() {
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
    if (isWeekendNotVisible())
      numbersDays = 5;

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

  public List getMonthReservation() {
    try {
      return getResourcesManagerBm().getMonthReservation(getComponentId(),
          getCurrentDay().getTime(), getUserId(), getLanguage());
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerSessionController.getMonthReservation()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public List getMonthReservation(String idUser) {
    try {
      return getResourcesManagerBm().getMonthReservation(getComponentId(),
          this.getCurrentDay().getTime(), idUser, this.getLanguage());
    } catch (RemoteException e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerSessionController.getMonthReservation()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public List getMonthReservationOfCategory(String idCategory) {
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

  public String initUPToSelectValidator(String pubId) {
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");
    PairObject hostComponentName = new PairObject(getComponentLabel(), "");
    PairObject[] hostPath = new PairObject[1];
    hostPath[0] = new PairObject(
        getString("resourcesManagerSC.SelectValidator"), "");
    String hostUrl = m_context + URLManager.getURL("useless", getComponentId())
        + "SetValidator?PubId=" + pubId;
    String cancelUrl = m_context
        + URLManager.getURL("useless", getComponentId())
        + "SetValidator?PubId=" + pubId;

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
    ArrayList profiles = new ArrayList();
    profiles.add("publisher");
    profiles.add("admin");
    sug.setProfileNames(profiles);
    sel.setExtraParams(sug);
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  public String initUserPanelOtherPlanning() {
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");
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
    if (selectedUsers != null)
      selectedUser = getUserDetail(selectedUsers[0]);

    return selectedUser;
  }

  public boolean areCommentsEnabled() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("comments"));
  }
}