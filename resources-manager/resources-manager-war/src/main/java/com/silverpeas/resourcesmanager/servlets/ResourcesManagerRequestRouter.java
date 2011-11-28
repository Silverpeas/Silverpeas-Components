/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of
 * the text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.resourcesmanager.servlets;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.resourcesmanager.control.ResourcesManagerSessionController;
import com.silverpeas.resourcesmanager.model.CategoryDetail;
import com.silverpeas.resourcesmanager.model.ReservationDetail;
import com.silverpeas.resourcesmanager.model.ResourceDetail;
import com.silverpeas.resourcesmanager.model.ResourceReservableDetail;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.Event;
import com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.MonthCalendar;
import org.apache.commons.fileupload.FileItem;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ResourcesManagerRequestRouter extends ComponentRequestRouter {

  private static final long serialVersionUID = 1L;
  private static final String root = "/resourcesManager/jsp/";

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  @Override
  public String getSessionControlBeanName() {
    return "ResourcesManager";
  }

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  @Override
  public ComponentSessionController createComponentSessionController(
          MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new ResourcesManagerSessionController(mainSessionCtrl, componentContext);
  }

  private CategoryDetail request2CategoryDetail(HttpServletRequest request) {
    String name = request.getParameter("name");
    String bookable = request.getParameter("bookable");
    String form = request.getParameter("form");
    String responsible = request.getParameter("responsible");
    String description = request.getParameter("description");
    boolean book = (bookable != null && "on".equals(bookable));
    CategoryDetail category = new CategoryDetail(name, book, form, responsible, description);
    if (request.getParameter("id") != null) {
      String categoryId = request.getParameter("id");
      category.setId(categoryId);
    }
    return category;
  }

  private ResourceDetail request2ResourceDetail(List<FileItem> items) {
    String name = FileUploadUtil.getParameter(items, "SPRM_name");
    String bookable = FileUploadUtil.getParameter(items, "SPRM_bookable");
    String responsible = FileUploadUtil.getParameter(items, "SPRM_responsible");
    String description = FileUploadUtil.getParameter(items, "SPRM_description");
    String categoryid = FileUploadUtil.getParameter(items, "SPRM_categoryChoice");
    boolean book = (bookable != null && "on".equals(bookable));
    ResourceDetail resource = new ResourceDetail(name, categoryid, responsible, description, book);
    String resourceId = FileUploadUtil.getParameter(items, "SPRM_resourceId");
    if (StringUtil.isDefined(resourceId)) {
      resource.setId(resourceId);
    }
    return resource;
  }

  private ReservationDetail request2ReservationDetail(HttpServletRequest request,
          ComponentSessionController resourcesManagerSC) {
    ReservationDetail reservation = null;
    try {
      String evenement = request.getParameter("evenement");
      String startDate = request.getParameter("startDate");
      String startHour = request.getParameter("startHour");
      String endHour = request.getParameter("endHour");
      String endDate = request.getParameter("endDate");
      String raison = request.getParameter("raison");
      String lieu = request.getParameter("lieu");
      Date dateDebut = DateUtil.stringToDate(startDate, startHour, resourcesManagerSC.getLanguage());
      Date dateFin = DateUtil.stringToDate(endDate, endHour, resourcesManagerSC.getLanguage());
      reservation = new ReservationDetail(evenement, dateDebut, dateFin, raison, lieu);
      SilverTrace.info("resourcesManager","ResourcesManagerRequestRouter.request2ReservationDetail()",
              "root.MSG_GEN_PARAM_VALUE", "reservation=" + reservation);
      return reservation;
    } catch(ParseException e) {
      SilverTrace.error("resourcesManager","ResourcesManagerRequestRouter.request2ReservationDetail()",
              "root.MSG_GEN_PARAM_VALUE", e);
    }
    return reservation;
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param componentSC The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, ComponentSessionController componentSC, HttpServletRequest request) {
    String categoryId = "";
    String reservationId;
    String resourceId;

    String destination = "";
    ResourcesManagerSessionController resourcesManagerSC = (ResourcesManagerSessionController) componentSC;
    request.setAttribute("rsc", resourcesManagerSC);
    String flag = getFlag(resourcesManagerSC.getUserRoles());
    String userId = resourcesManagerSC.getUserId();
    request.setAttribute("Profile", flag);
    request.setAttribute("UserId", userId);
    try {
      if (function.startsWith("Main")) {
        destination = displayCalendarView(request, resourcesManagerSC);
      }
      else if ("NewCategory".equals(function)) {
        List<PublicationTemplate> listTemplates = getPublicationTemplateManager().getPublicationTemplates();
        request.setAttribute("listTemplates", listTemplates);
        destination = root + "categoryManager.jsp";
      } else if ("SaveCategory".equals(function)) {
        CategoryDetail category = request2CategoryDetail(request);
        resourcesManagerSC.createCategory(category);
        destination = getDestination("ViewCategories", componentSC, request);
      } else if ("EditCategory".equals(function)) {
        categoryId = request.getParameter("id");
        CategoryDetail category = resourcesManagerSC.getCategory(categoryId);
        List<PublicationTemplate> listTemplates = getPublicationTemplateManager().
                getPublicationTemplates();
        request.setAttribute("listTemplates", listTemplates);
        request.setAttribute("category", category);
        destination = root + "categoryManager.jsp";
      } else if ("ModifyCategory".equals(function)) {
        CategoryDetail category = request2CategoryDetail(request);
        resourcesManagerSC.updateCategory(category);
        destination = getDestination("ViewCategories", componentSC, request);
      } else if ("ViewCategories".equals(function)) {
        List<CategoryDetail> list = resourcesManagerSC.getCategories();
        request.setAttribute("categories", list);
        destination = root + "categories.jsp";
      } else if (function.equals("DeleteCategory")) {
        categoryId = request.getParameter("id");
        resourcesManagerSC.deleteCategory(categoryId);
        destination = getDestination("ViewCategories", componentSC, request);
      }
      else if ("NewResource".equals(function)) {
        // categorie pré-séléctionnée
        categoryId = request.getParameter("categoryId");
        List<CategoryDetail> list = resourcesManagerSC.getCategories();
        request.setAttribute("listCategories", list);
        request.setAttribute("categoryId", categoryId);

        resourcesManagerSC.setResourceIdForResource(null);
        resourcesManagerSC.setCategoryIdForResource(categoryId);
        setXMLFormIntoRequest(request, resourcesManagerSC);

        destination = root + "resourceManager.jsp";
      } else if (function.equals("SaveResource")) {
        // récupération des données saisies dans le formulaire
        List<FileItem> items = FileUploadUtil.parseRequest(request);

        ResourceDetail resource = request2ResourceDetail(items);
        String idResource = resourcesManagerSC.createResource(resource);
        List<String> managers = getManagers(items);
        resourcesManagerSC.addManagers(idResource, managers);

        request.setAttribute("resourceId", idResource);
        request.setAttribute("provenance", "resources");

        resourcesManagerSC.setResourceIdForResource(idResource);
        updateXMLForm(resourcesManagerSC, items);

        destination = getDestination("ViewResource", componentSC, request);
      } else if (function.equals("EditResource")) {
        resourceId = request.getParameter("resourceId");
        if (!StringUtil.isDefined(resourceId)) {
          resourceId = resourcesManagerSC.getCurrentResource();
        }
        ResourceDetail resource = resourcesManagerSC.getResource(resourceId);

        // on récupère l'ensemble des catégories pour la liste déroulante
        List<CategoryDetail> list = resourcesManagerSC.getCategories();
        request.setAttribute("listCategories", list);

        request.setAttribute("categoryId", resource.getCategoryId());
        request.setAttribute("resource", resource);

        // liste des responsables sur la ressource
        resourcesManagerSC.setCurrentResource(resourceId);
        List<UserDetail> managers = resourcesManagerSC.getManagers(resourceId);
        request.setAttribute("Managers", managers);

        resourcesManagerSC.setResourceIdForResource(resourceId);
        resourcesManagerSC.setCategoryIdForResource(resource.getCategoryId());
        setXMLFormIntoRequest(request, resourcesManagerSC);

        destination = root + "resourceManager.jsp";
      } else if (function.equals("ModifyResource")) {
        // récupération des données saisies dans le formulaire
        List<FileItem> items = FileUploadUtil.parseRequest(request);

        ResourceDetail resource = request2ResourceDetail(items);
        List<String> managers = getManagers(items);
        resourcesManagerSC.addManagers(resource.getId(), managers);
        resourcesManagerSC.updateResource(resource);

        resourcesManagerSC.setResourceIdForResource(resource.getId());
        updateXMLForm(resourcesManagerSC, items);

        request.setAttribute("id", resource.getCategoryId());
        destination = getDestination("ViewResources", componentSC, request);
      } else if ("ViewResources".equals(function)) {
        if (request.getAttribute("id") != null) {
          categoryId = (String) request.getAttribute("id");
        } else if (request.getParameter("id") != null) {
          categoryId = request.getParameter("id");
        }
        List<ResourceDetail> list = resourcesManagerSC.getResourcesByCategory(categoryId);
        List<CategoryDetail> listcategories = resourcesManagerSC.getCategories();
        request.setAttribute("listCategories", listcategories);
        request.setAttribute("list", list);
        request.setAttribute("categoryId", categoryId);
        destination = root + "resources.jsp";
      } else if ("ViewResource".equals(function)) {
        String provenance;
        if (request.getParameter("provenance") != null) {
          provenance = request.getParameter("provenance");
          resourcesManagerSC.setProvenanceResource(provenance);
        } else if (request.getAttribute("provenance") != null) {
          provenance = (String) request.getAttribute("provenance");
          resourcesManagerSC.setProvenanceResource(provenance);
        } else {
          provenance = resourcesManagerSC.getProvenanceResource();
        }
        String idReservation = request.getParameter("reservationId");
        if (idReservation != null) {
          resourcesManagerSC.setReservationIdForResource(idReservation);
        }
        if (request.getParameter("resourceId") != null) {
          resourceId = request.getParameter("resourceId");
          resourcesManagerSC.setResourceIdForResource(resourceId);
        } else if (request.getAttribute("resourceId") != null) {
          resourceId = (String) request.getAttribute("resourceId");
          resourcesManagerSC.setResourceIdForResource(resourceId);
        } else {
          resourceId = resourcesManagerSC.getResourceIdForResource();
        }
        ResourceDetail resource = resourcesManagerSC.getResource(resourceId);
        List<UserDetail> managers = resourcesManagerSC.getManagers(resourceId);
        request.setAttribute("Managers", managers);
        CategoryDetail category = resourcesManagerSC.getCategory(resource.getCategoryId());

        resourcesManagerSC.setCategoryIdForResource(category.getId());

        if (StringUtil.isDefined(category.getForm())) {
          putXMLDisplayerIntoRequest(resource, request, resourcesManagerSC);
        }
        String objectView = request.getParameter("objectView");
        request.setAttribute("objectView", objectView);
        request.setAttribute("category", category);
        request.setAttribute("provenance", provenance);
        request.setAttribute("resource", resource);
        request.setAttribute("ShowComments", resourcesManagerSC.areCommentsEnabled());
        destination = root + "resource.jsp";
      } else if ("DeleteRessource".equals(function)) {
        resourceId = request.getParameter("resourceId");
        categoryId = request.getParameter("categoryId");
        resourcesManagerSC.deleteResource(resourceId);
        request.setAttribute("id", categoryId);
        destination = getDestination("ViewResources", componentSC, request);
      }
      else if ("NewReservation".equals(function)) {
        String date = request.getParameter("Day");
        if (StringUtil.isDefined(date)) {
          request.setAttribute("DefaultDate", date);
        }
        destination = root + "reservationManager.jsp";
      } else if ("GetAvailableResources".equals(function)) {
        String idReservation = null;
        if (request.getParameter("reservationId") != null) {
          idReservation = request.getParameter("reservationId");
        } else if (request.getAttribute("reservationId") != null) {
          idReservation = (String) request.getAttribute("reservationId");
        }
        List<ResourceDetail> listResourcesProblem = (List<ResourceDetail>) request.getAttribute(
                "listeResourcesProblem");
        List<ResourceDetail> listResourceEverReserved = null;
        // si listResourcesProblem c'est qu'il n y a pas eu de problème
        // d'enregistrement
        if ((listResourcesProblem == null)) {
          ReservationDetail reservation = request2ReservationDetail(request, resourcesManagerSC);
          resourcesManagerSC.createReservation(reservation);
          resourcesManagerSC.setBeginDateReservation(reservation.getBeginDate());
          resourcesManagerSC.setEndDateReservation(reservation.getEndDate());
        }
        if (idReservation != null) {
          SilverTrace.info("resourcesManager",
                  "ResourcesManagerRequestRouter.getDestination()",
                  "root.MSG_GEN_PARAM_VALUE", "dans le if,idReservation="
                  + idReservation);
          listResourceEverReserved = resourcesManagerSC.getResourcesofReservation(idReservation);
          if (listResourceEverReserved != null) {
            // boucle permettant de supprimer les réservations déjà réservées
            // qui posent problème, car elles ont déjà été réservées
            for (int i = 0; i < listResourceEverReserved.size(); i++) {
              ResourceDetail resourceReserved = listResourceEverReserved.get(i);
              if (listResourcesProblem != null) {
                for (ResourceDetail resourceProblem : listResourcesProblem) {
                  if (resourceReserved.equals(resourceProblem)) {
                    listResourceEverReserved.remove(i);
                    break;
                  }
                }
              }
            }
          }
        }
        int nbCategories = resourcesManagerSC.getCategories().size();        
        ReservationDetail reservation = resourcesManagerSC.getReservationCourante();
        List<ResourceReservableDetail> maListResourcesReservable =
                resourcesManagerSC.getResourcesReservable(
                reservation.getBeginDate(), reservation.getEndDate());
        SilverTrace.info("resourcesManager", "ResourcesManagerRequestRouter.getDestination()",
                "root.MSG_GEN_PARAM_VALUE", "listResourcesReservable="
                + maListResourcesReservable.size());
        // on envoie l'id de la réservation et l'ensemble des resources
        // associées à celles -ci
        request.setAttribute("idReservation", idReservation);
        request.setAttribute("listResourceEverReserved", listResourceEverReserved);

        request.setAttribute("listResourcesReservable", maListResourcesReservable);
        request.setAttribute("reservation", reservation);
        request.setAttribute("listResourcesProblem", listResourcesProblem);
        request.setAttribute("nbCategories", nbCategories);
        destination = root + "cart.jsp";
      } else if ("FinalReservation".equals(function)) {
        // listeReservation est la liste complète des ressources réservées lors
        // d'une création ou d'une édition de réservation
        // idModifiedReservation est l'id de la réservation à modifié lors d'une
        // édition de réservation
        // on doit vérifier que celles-ci n'ont pas été prises avant de mettre à
        // jour la réservation.
        String idModifiedReservation = request.getParameter("idModifiedReservation");
        String listeReservation = request.getParameter("listeResa");
        // si idModifiedReservation n'est pas nulle, on est en train de modifier
        // une réservation
        // sinon on est en train de créer une réservation
        if (idModifiedReservation != null) {
          // on vérifie que les nouvelles ressources que l'on veut réserver ne
          // sont pas déjà prises.
          List<ResourceDetail> listeResourcesProblemeReservationTotal =
                  resourcesManagerSC.getResourcesProblemDate(listeReservation,
                  resourcesManagerSC.getBeginDateReservation(),
                  resourcesManagerSC.getEndDateReservation(), idModifiedReservation);
          if (listeResourcesProblemeReservationTotal.isEmpty()) {
            // regarder si les dates ont été modifiées
            ReservationDetail resa = resourcesManagerSC.getReservation(idModifiedReservation);
            boolean updateDate = false;
            if (!resourcesManagerSC.getBeginDateReservation().equals(resa.getBeginDate())
                    || !resourcesManagerSC.getEndDateReservation().equals(resa.getEndDate())) {
              updateDate = true;
            }
            resourcesManagerSC.updateReservation(idModifiedReservation,
                    listeReservation, updateDate);

            // redirection vers la réservation
            request.setAttribute("reservationId", idModifiedReservation);
            destination = getDestination("ViewReservation", componentSC, request);
          } else {
            request.setAttribute("listeResourcesProblem",
                    listeResourcesProblemeReservationTotal);
            request.setAttribute("reservationId", idModifiedReservation);
            destination = getDestination("GetAvailableResources", componentSC,
                    request);
          }
        } else {
          resourcesManagerSC.setListReservationCurrent(listeReservation);
          List<ResourceDetail> listeResourcesProblem = resourcesManagerSC.verificationReservation(
                  listeReservation);
          if (listeResourcesProblem.isEmpty()) {
            resourcesManagerSC.saveReservation();
            request.setAttribute("reservationId",
                    resourcesManagerSC.getReservationCourante().getId());
            destination = getDestination("ViewReservation", componentSC, request);
          } else {
            request.setAttribute("listeResourcesProblem", listeResourcesProblem);
            destination = getDestination("GetAvailableResources", componentSC,
                    request);
          }
        }
      } else if ("EditReservation".equals(function)) {
        String idReservation = "";
        // listResources est la liste des ressources qui peuvent poser problème
        // quand on change la date de réservation
        List listResourcesProblem = null;
        if (request.getParameter("id") != null) {
          idReservation = request.getParameter("id");
        } else if (request.getAttribute("id") != null) {
          idReservation = (String) request.getAttribute("id");
          listResourcesProblem = (List) request.getAttribute("listResources");
        }
        ReservationDetail reservation = resourcesManagerSC.getReservation(idReservation);
        // on envoie la réservation de l'id et la liste des ressources associées
        // ainsi que la liste qui posent problème quand on change les dates
        request.setAttribute("reservation", reservation);
        request.setAttribute("listResourcesProblem", listResourcesProblem);
        destination = root + "reservationManager.jsp";
      } else if ("ViewReservation".equals(function)) {
        reservationId = null;
        if (request.getParameter("reservationId") != null) {
          reservationId = request.getParameter("reservationId");
        } else if (request.getAttribute("reservationId") != null) {
          reservationId = (String) request.getAttribute("reservationId");
        }
        String objectView = request.getParameter("objectView");
        // si on vient de resource.jsp, reservationId a été stocké dans le
        // session controler
        if (reservationId == null) {
          reservationId = resourcesManagerSC.getReservationIdForResource();
        }
        ReservationDetail reservation = resourcesManagerSC.getReservation(reservationId);
        List<ResourceDetail> listResourcesofReservation = resourcesManagerSC.
                getResourcesofReservation(reservationId);
        request.setAttribute("listResourcesofReservation",
                listResourcesofReservation);
        request.setAttribute("reservationId", reservationId);
        request.setAttribute("reservation", reservation);
        request.setAttribute("objectView", objectView);
        destination = root + "viewReservation.jsp";
      } else if ("ViewReservations".equals(function)) {
        List<ReservationDetail> listOfReservation = resourcesManagerSC.getReservationUser();
        request.setAttribute("listOfReservation", listOfReservation);
        destination = root + "viewReservations.jsp";
      } else if (function.equals("DeleteReservation")) {
        resourceId = request.getParameter("id");
        resourcesManagerSC.deleteReservation(resourceId);
        destination = displayCalendarView(request, resourcesManagerSC);
      } else if ("Calendar".equals(function))
        destination = displayCalendarView(request, resourcesManagerSC);
      else if ("PreviousMonth".equals(function)) {
        resourcesManagerSC.previousMonth();
        destination = displayCalendarView(request, resourcesManagerSC);
      } else if ("NextMonth".equals(function)) {
        resourcesManagerSC.nextMonth();
        destination = displayCalendarView(request, resourcesManagerSC);
      } else if ("GoToday".equals(function)) {
        resourcesManagerSC.today();
        destination = displayCalendarView(request, resourcesManagerSC);
      } else if ("Comments".equals(function)) {
        String provenance = resourcesManagerSC.getProvenanceResource();
        String idResource = resourcesManagerSC.getResourceIdForResource();
        ResourceDetail myResource = resourcesManagerSC.getResource(idResource);
        ResourcesWrapper resources = resourcesManagerSC.getResources();
        String chemin = "";
        if ("resources".equals(provenance)) {
          // on vient de resources
          chemin = "<a href=\"ViewCategories\">"
                  + EncodeHelper.javaStringToHtmlString(resources.getString(
                  "resourcesManager.listCategorie")) + "</a>";
          String chemin2 = "<a href=\"ViewResources?id=" + myResource.getCategoryId() + "\">"
                  + EncodeHelper.javaStringToHtmlString(
                  resources.getString("resourcesManager.categorie")) + "</a>";
          chemin = chemin + " > " + chemin2;
        } else if (provenance.equals("reservation")) {
          // on vient du récapitulatif de la réservation
          chemin = "<a href=\"ViewReservation\">"
                  + EncodeHelper.javaStringToHtmlString(resources.getString(
                  "resourcesManager.recapitulatifReservation")) + "</a>";
        }
        request.setAttribute("Path", chemin);
        request.setAttribute("resourceId", idResource);
        request.setAttribute("resourceName", myResource.getName());

        destination = root + "comments.jsp";
      } else if ("SelectManager".equals(function)) {
        destination = resourcesManagerSC.initUPToSelectManager("");
      } else if (function.startsWith("ChooseOtherPlanning")) {
        destination = resourcesManagerSC.initUserPanelOtherPlanning();
      } else if (function.startsWith("ViewOtherPlanning")) {
        // userPanel return
        UserDetail selectedUser = resourcesManagerSC.getSelectedUser();
        String idUser = selectedUser.getId();
        String firstNameUser = selectedUser.getFirstName();
        String lastName = selectedUser.getLastName();
        request.setAttribute("firstNameUser", firstNameUser);
        request.setAttribute("lastName", lastName);
        resourcesManagerSC.setFirstNameUserCalandar(firstNameUser);
        resourcesManagerSC.setLastNameUserCalandar(lastName);
        request.setAttribute("userId", idUser);
        // on indique au rooter qu'on regarde le calandrier de quelqu un
        request.setAttribute("objectView", "PlanningOtherUser");
        destination = displayCalendarView(request, resourcesManagerSC);
      } else if (function.startsWith("searchResult")) {
        // traitement des recherches
        String id = request.getParameter("Id");
        String type = request.getParameter("Type");
        if ("Reservation".equals(type)) {
          // traitement des réservations
          request.setAttribute("reservationId", id);
          destination = getDestination("ViewReservation", resourcesManagerSC,
                  request);
        } else if ("Category".equals(type)) {
          request.setAttribute("objectView", id);
          destination = displayCalendarView(request, resourcesManagerSC);
        } else if ("Resource".equals(type)) {
          request.setAttribute("resourceId", id);
          request.setAttribute("provenance", "calendar");
          destination = getDestination("ViewResource", resourcesManagerSC,
                  request);
        }
      } else if (function.equals("ToSelectManagers")) {
        // récupération de la liste des responsables
        Collection<String> currentManagers = request2Managers(request);
        try {
          destination = resourcesManagerSC.initUserSelect(currentManagers);
        } catch (Exception e) {
          SilverTrace.warn("resourcesManager",
                  "resourcesManagerRequestRouter.getDestination()",
                  "root.EX_USERPANEL_FAILED", "function = " + function, e);
        }
      } else if (function.equals("FromUserSelect")) {
        // récupération des valeurs de userPanel
        SilverTrace.debug("resourcesManager",
                "ResourcesManagerRequestRouter.getDestination()",
                "root.MSG_GEN_PARAM_VALUE", "FromUserSelect:");
        Selection sel = resourcesManagerSC.getSelection();
        // Get users selected in User Panel
        String[] userIds = SelectionUsersGroups.getDistinctUserIds(sel.getSelectedElements(), null);
        SilverTrace.debug("resourcesManager",
                "ResourcesManagerRequestRouter.getDestination()",
                "root.MSG_GEN_PARAM_VALUE", "userIds:" + Arrays.toString(userIds));
        if (userIds.length != 0) {
          SilverTrace.debug("resourcesManager",
                  "ResourcesManagerRequestRouter.getDestination()",
                  "root.MSG_GEN_PARAM_VALUE", "userIds.length():" + userIds.length);

          UserDetail[] userDetails = SelectionUsersGroups.getUserDetails(userIds);
          SilverTrace.debug("resourcesManager",
                  "ResourcesManagerRequestRouter.getDestination()",
                  "root.MSG_GEN_PARAM_VALUE", "userDetails:"
                  + Arrays.toString(userDetails));
          request.setAttribute("Managers", Arrays.asList(userDetails));
        }
        destination = root + "refreshFromUserSelect.jsp";
      } else if (function.equals("ValidateResource")) {
        resourceId = request.getParameter("ResourceId");
        reservationId = request.getParameter("reservationId");
        String objectView = request.getParameter("objectView");
        resourcesManagerSC.validateResource(Integer.parseInt(resourceId), Integer.parseInt(
                reservationId));
        request.setAttribute("reservationId", reservationId);
        request.setAttribute("objectView", objectView);
        destination = getDestination("ViewReservation", componentSC, request);
      } else if (function.equals("ForRefuseResource")) {
        resourceId = request.getParameter("ResourceId");
        reservationId = request.getParameter("reservationId");
        String resourceName = request.getParameter("ResourceName");
        String objectView = request.getParameter("objectView");
        request.setAttribute("reservationId", reservationId);
        request.setAttribute("ResourceId", resourceId);
        request.setAttribute("ResourceName", resourceName);
        request.setAttribute("objectView", objectView);
        destination = root + "refuseResource.jsp";
      } else if (function.equals("RefuseResource")) {
        resourceId = request.getParameter("ResourceId");
        reservationId = request.getParameter("reservationId");
        String motive = request.getParameter("Motive");
        String objectView = request.getParameter("objectView");
        resourcesManagerSC.refuseResource(Integer.parseInt(resourceId), Integer.parseInt(
                reservationId), motive);
        request.setAttribute("reservationId", reservationId);
        request.setAttribute("objectView", objectView);
        destination = getDestination("ViewReservation", componentSC, request);
      } else {
        displayCalendarView(request, resourcesManagerSC);
        destination = root + function + ".jsp";
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    SilverTrace.info("resourcesManager",
            "ResourcesManagerRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
    return destination;
  }

  /**
     * on regarde soit :
   * ses réservations -> myObjectView = myReservation
   * les réservations d'une autre personne -> myObjectView = PlanningOtherUser
   * le planning d'une catégorie -> myObjectView = l'id de la catégorie
   * le planning d'une ressource -> myObjectView = l'id de la catégorie
   * myObjectView représente ce qu'on est en train de visualiser, il est nul à
     * l'initialisation. idUser sert à savoir l'id de la personne dont on regarde le calandrier
   * @param request
   * @param sessionController
   * @return
   */
  private String displayCalendarView(HttpServletRequest request, ResourcesManagerSessionController sessionController) {
    String myObjectView = getView(request);
    String idUser = (String) request.getAttribute("userId");
    List<ReservationDetail> listOfReservation = null;
    List<ReservationDetail> listReservationsOfCategory = null;
    String currentResourceId = null;
    // on regarde le planning d'une catégorie ou d'une ressource
    if ((myObjectView != null) && isNotAnUserView(myObjectView)) {
      listReservationsOfCategory = sessionController.getMonthReservationOfCategory(myObjectView);
      List<ResourceDetail> listResourcesofCategory = sessionController.getResourcesByCategory(
              myObjectView);
      currentResourceId = request.getParameter("resourceId");
      if (StringUtil.isDefined(currentResourceId)) {
        request.setAttribute("resourceId", currentResourceId);
      }
      request.setAttribute("listResourcesofCategory", listResourcesofCategory);
    } else {
      if ("PlanningOtherUser".equals(myObjectView)) {
        // on récupère les réservations de l'utilisateur, ainsi que son nom et prenom
        if (idUser != null) {
          sessionController.setObjectViewForCalandar(idUser);
        } else {
          idUser = sessionController.getObjectViewForCalandar();
        }
        listOfReservation = sessionController.getMonthReservation(idUser);
        request.setAttribute("firstNameUser", sessionController.getFirstNameUserCalandar());
        request.setAttribute("lastName", sessionController.getLastNameUserCalandar());
      } else if ("viewForValidation".equals(myObjectView)) {
        // on regarde les réservations à valider
        listOfReservation = sessionController.getReservationForValidation();
      } else {
        // on regarde ses propres réservations
        listOfReservation = sessionController.getMonthReservation();
      }
      request.setAttribute("listOfReservation", listOfReservation);
    }
    // initialisation d'un MonthCalendar du viewgenerator
    MonthCalendar monthC = sessionController.getMonthCalendar();
    setEvents(monthC, listOfReservation, listReservationsOfCategory, sessionController,
            myObjectView, currentResourceId);
    List<CategoryDetail> listOfCategories = sessionController.getCategories();
    request.setAttribute("idUser", idUser);
    request.setAttribute("listOfCategories", listOfCategories);
    request.setAttribute("idCategory", myObjectView);
    request.setAttribute("monthC", monthC);
    request.setAttribute("IsResponsible", sessionController.isResponsible());
    if(StringUtil.getBooleanValue(request.getParameter("isPortlet"))) {
      return root + "portlet.jsp";
    }
    return root + "almanach.jsp";
  }

  private String getView(HttpServletRequest request) {
    String myObjectView = null;
    if (StringUtil.isDefined(request.getParameter("objectView"))) {
      myObjectView = request.getParameter("objectView");
    } else if (StringUtil.isDefined((String) request.getAttribute("objectView"))) {
      myObjectView = (String) request.getAttribute("objectView");
    }
    return myObjectView;
  }

  private boolean isNotAnUserView(String myObjectView) {
    return (!"myReservation".equals(myObjectView))
        && (!"PlanningOtherUser".equals(myObjectView))
        && (!"viewUser".equals(myObjectView))
        && (!"viewForValidation".equals(myObjectView));
  }

  // recherche du profile de l'utilisateur
  public String getFlag(String[] profiles) {
    String flag = "user";
    for (String profile : profiles) {
      if ("admin".equals(profile)) {
        return profile;
      }
      if ("responsable".equals(profile)) {
        flag = profile;
      } else if ("publisher".equals(profile)) {
        if (!"responsable".equals(flag)) {
          flag = profile;
        }
      }
    }
    return flag;
  }

  private void setXMLFormIntoRequest(HttpServletRequest request,
          ResourcesManagerSessionController resourcesManagerSC) throws Exception {
    String idResource = resourcesManagerSC.getResourceIdForResource();
    String idCategory = resourcesManagerSC.getCategoryIdForResource();
    CategoryDetail category = resourcesManagerSC.getCategory(idCategory);
    String xmlFormName = category.getForm();
    if (StringUtil.isDefined(xmlFormName)) {
      String xmlFormShortName = xmlFormName.substring(
              xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
      // création du PublicationTemplate
      getPublicationTemplateManager().addDynamicPublicationTemplate(
              resourcesManagerSC.getComponentId() + ":" + xmlFormShortName,
              xmlFormName);
      PublicationTemplateImpl pubTemplate =
              (PublicationTemplateImpl) getPublicationTemplateManager().getPublicationTemplate(
              resourcesManagerSC.getComponentId() + ":"
              + xmlFormShortName, xmlFormName);

      // création du formulaire et du DataRecord
      Form formUpdate = pubTemplate.getUpdateForm();
      RecordSet recordSet = pubTemplate.getRecordSet();
      // attention ici ce n est pas categoryId mais resourceId
      DataRecord data = recordSet.getRecord(idResource);
      if (data == null) {
        data = recordSet.getEmptyRecord();
        data.setId(idResource);
      }
      // appel de la jsp avec les paramètres
      request.setAttribute("Form", formUpdate);
      request.setAttribute("Data", data);
      request.setAttribute("XMLFormName", xmlFormName);
    }
  }

  private void putXMLDisplayerIntoRequest(ResourceDetail resource,
          HttpServletRequest request,
          ResourcesManagerSessionController resourcesManagerSC)
          throws PublicationTemplateException, FormException {
    // récupération de l’Id de l’objet en fonction de l’objet "object"
    String resourceId = resource.getId();
    String categoryId = resource.getCategoryId();
    CategoryDetail category = resourcesManagerSC.getCategory(categoryId);

    String xmlFormName = category.getForm();
    String xmlFormShortName = xmlFormName.substring(
            xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));

    // register xmlForm
    getPublicationTemplateManager().addDynamicPublicationTemplate(
            resourcesManagerSC.getComponentId()
            + ":" + xmlFormShortName, xmlFormName);

    // création du PublicationTemplate
    PublicationTemplateImpl pubTemplate =
            (PublicationTemplateImpl) getPublicationTemplateManager().
            getPublicationTemplate(resourcesManagerSC.getComponentId() + ":"
            + xmlFormShortName, xmlFormName);
    // récupération des données
    Form formView = pubTemplate.getViewForm();
    RecordSet recordSet = pubTemplate.getRecordSet();
    DataRecord data = recordSet.getRecord(resourceId);
    if (data == null) {
      data = recordSet.getEmptyRecord();
      data.setId(resourceId);
    }
    // passage des paramètres à la request avec les données du formulaire
    request.setAttribute("XMLForm", formView);
    request.setAttribute("XMLData", data);

    PagesContext context = new PagesContext("myForm", "0", resourcesManagerSC.getLanguage(), false,
            resourcesManagerSC.getComponentId(), resourcesManagerSC.getUserId());
    context.setBorderPrinted(false);
    context.setObjectId(resourceId);
    request.setAttribute("context", context);
  }

  private void updateXMLForm(
          ResourcesManagerSessionController resourcesManagerSC, List<FileItem> items)
          throws Exception {
    // récupération de l’objet et du nom du formulaire
    String idResource = resourcesManagerSC.getResourceIdForResource();
    String idCategory = resourcesManagerSC.getCategoryIdForResource();

    CategoryDetail category = resourcesManagerSC.getCategory(idCategory);

    String xmlFormName = category.getForm();
    if (StringUtil.isDefined(xmlFormName)) {
      String xmlFormShortName = xmlFormName.substring(
              xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
      // récupération des données du formulaire (via le DataRecord)
      PublicationTemplate pub =
              getPublicationTemplateManager().getPublicationTemplate(resourcesManagerSC.
              getComponentId() + ":"
              + xmlFormShortName);
      RecordSet set = pub.getRecordSet();
      Form form = pub.getUpdateForm();
      DataRecord data = set.getRecord(idResource);
      if (data == null) {
        data = set.getEmptyRecord();
        data.setId(idResource);
      }

      // sauvegarde des données du formulaire
      PagesContext context =
              new PagesContext("myForm", "0", resourcesManagerSC.getLanguage(), false,
              resourcesManagerSC.getComponentId(),
              resourcesManagerSC.getUserId());
      context.setObjectId(idResource);
      form.update(items, data, context);
      set.save(data);
    }
  }

  private List<String> request2Managers(HttpServletRequest request) {
    List<String> managers = new ArrayList<String>();
    String managerIds = request.getParameter("ManagerIds");
    if (StringUtil.isDefined(managerIds)) {
      String[] tabResources = managerIds.split(",");
      Collections.addAll(managers, tabResources);
    }
    return managers;
  }

  private List<String> getManagers(List<FileItem> items) {
    List<String> managers = new ArrayList<String>();
    String managerIds = FileUploadUtil.getParameter(items, "managerIds");
    if (StringUtil.isDefined(managerIds)) {
      String[] tabResources = managerIds.split(",");
      Collections.addAll(managers, tabResources);
    }
    return managers;
  }

  /**
   * Gets an instance of PublicationTemplateManager.
   * @return an instance of PublicationTemplateManager.
   */
  private PublicationTemplateManager getPublicationTemplateManager() {
    return PublicationTemplateManager.getInstance();
  }

  private void setEvents(MonthCalendar monthC, List<ReservationDetail> listOfReservation,
          List<ReservationDetail> listReservationsOfCategory,
          ResourcesManagerSessionController resourcesManagerSC, String view,
          String currentResourceId) {
    String objectView = view;
    if (view == null) {
      objectView = "myReservation";
    }
    // transformation des réservations (ReservationDetail) en Event du MonthCalendar
    if (listOfReservation != null) {
      for (ReservationDetail maReservation : listOfReservation) {
        String reservationId = maReservation.getId();
        String event = maReservation.getEvent();
        Event evt = reservation2Event(maReservation, reservationId, event, resourcesManagerSC);
        String color = "black";
        if (maReservation.isValidationRequired()) {
          color = "red";
        } else if (maReservation.isRefused()) {
          color = "gray";
        }
        evt.setColor(color);
        monthC.addEvent(evt);
      }
    } // on affiche le planning d'une catégorie ou d'une ressource
    else if (listReservationsOfCategory != null) {
      for (ReservationDetail maReservation : listReservationsOfCategory) {
        List<ResourceDetail> listResourcesReserved = maReservation.getListResourcesReserved();
        // listResourcesReserved contient la liste des ressources r�serv�es de la r�servation pour
        // la cat�gorie
        if (listResourcesReserved != null) {
          for (ResourceDetail myResource : listResourcesReserved) {
            String categoryId = myResource.getCategoryId();
            // on affiche les ressources de la réservation qui possèdent la même categoryId que la
            // catégorie sélectionnée
            if (categoryId.equals(objectView)) {
              // si currentResourceId est nulle aucune ressource n'a été sélectionnée
              // donc on affiche toutes les ressources de la catégorie
              String resourceId = myResource.getId();
              if (!StringUtil.isDefined(currentResourceId) || currentResourceId.equals(resourceId)) {
                String resourceName = myResource.getName();
                Event evt = reservation2Event(maReservation, resourceId, resourceName,
                        resourcesManagerSC);
                monthC.addEvent(evt);
              }
            }
          }
        }
      }
    }
    // initialisation de monthC avec la date courrante issue de almanach
    monthC.setCurrentMonth(resourcesManagerSC.getCurrentDay().getTime());
  }

  private Event reservation2Event(ReservationDetail reservation, String id, String label,
          ResourcesManagerSessionController sc) {
    Date endDate = reservation.getEndDate();
    Date startDate = reservation.getBeginDate();
    String minuteHourDateBegin = DateUtil.getFormattedTime(reservation.getBeginDate());
    String minuteHourDateEnd = DateUtil.getFormattedTime(reservation.getEndDate());
    Event evt = new Event(id, label, startDate, endDate, null, 0);
    evt.setStartHour(minuteHourDateBegin);
    evt.setEndHour(minuteHourDateEnd);
    evt.setPlace(reservation.getPlace());
    evt.setInstanceId(reservation.getInstanceId());
    evt.setTooltip(sc.getString("resourcesManager.bookedBy")
            + sc.getUserDetail(reservation.getUserId()).getDisplayedName());
    return evt;
  }
}