/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.resourcesmanager.servlets;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import org.silverpeas.util.EncodeHelper;
import org.silverpeas.util.StringUtil;
import org.silverpeas.servlet.FileUploadUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.calendar.CalendarViewType;
import org.silverpeas.resourcemanager.model.Category;
import org.silverpeas.resourcemanager.model.Reservation;
import org.silverpeas.resourcemanager.model.Resource;
import org.silverpeas.resourcemanager.util.ResourceUtil;
import org.silverpeas.resourcesmanager.control.ResourceManagerDataViewType;
import org.silverpeas.resourcesmanager.control.ResourcesManagerSessionController;
import org.silverpeas.servlet.HttpRequest;
import org.silverpeas.util.GlobalContext;

public class ResourcesManagerRequestRouter extends ComponentRequestRouter<ResourcesManagerSessionController> {

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
   *
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  @Override
  public ResourcesManagerSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new ResourcesManagerSessionController(mainSessionCtrl, componentContext);
  }

  private Category request2CategoryDetail(ResourcesManagerSessionController controller,
      HttpRequest request) {
    Category category;
    Long categoryId = request.getParameterAsLong("id");
    if (categoryId != null) {
      category = controller.getCategory(categoryId);
    } else {
      category = new Category();
    }
    category.setName(request.getParameter("name"));
    category.setForm(request.getParameter("form"));
    category.setDescription(request.getParameter("description"));
    category.setBookable("on".equals(request.getParameter("bookable")));
    return category;
  }

  private Resource request2ResourceDetail(ResourcesManagerSessionController controller,
      List<FileItem> items) {
    String resourceId = FileUploadUtil.getParameter(items, "SPRM_resourceId");
    Resource resource;
    if (StringUtil.isDefined(resourceId)) {
      resource = controller.getResource(Long.valueOf(resourceId));
    } else {
      resource = new Resource();
    }
    String categoryid = FileUploadUtil.getParameter(items, "SPRM_categoryChoice");
    if (StringUtil.isDefined(categoryid)) {
      resource.setCategory(controller.getCategory(Long.valueOf(categoryid)));
    }
    resource.setName(FileUploadUtil.getParameter(items, "SPRM_name"));
    resource.setBookable("on".equalsIgnoreCase(FileUploadUtil.getParameter(items, "SPRM_bookable")));
    resource.setDescription(FileUploadUtil.getParameter(items, "SPRM_description"));
    return resource;
  }

  private Reservation request2ReservationDetail(HttpRequest request) {
    try {
      String evenement = request.getParameter("evenement");
      Date dateDebut = request.getParameterAsDate("startDate", "startHour");
      Date dateFin = request.getParameterAsDate("endDate", "endHour");
      String raison = request.getParameter("raison");
      String lieu = request.getParameter("lieu");
      Reservation reservation = new Reservation(evenement, dateDebut, dateFin, raison, lieu);
      SilverTrace
          .info("resourcesManager", "ResourcesManagerRequestRouter.request2ReservationDetail()",
              "root.MSG_GEN_PARAM_VALUE", "reservation=" + reservation);
      return reservation;
    } catch (ParseException e) {
      SilverTrace
          .error("resourcesManager", "ResourcesManagerRequestRouter.request2ReservationDetail()",
              "root.MSG_GEN_PARAM_VALUE", e);
    }
    return null;
  }

  /**
   * {@link ComponentRequestRouter#getDestination(String, com.stratelia.silverpeas.peasCore.ComponentSessionController, org.silverpeas.servlet.HttpRequest)}
   *
   * @param function
   * @param resourcesManagerSC
   * @param request
   * @return
   */
  @Override
  public String getDestination(String function,
      ResourcesManagerSessionController resourcesManagerSC, HttpRequest request) {

    Long categoryId;
    Long reservationId;
    Long resourceId;

    String destination = "";
    request.setAttribute("rsc", resourcesManagerSC);
    String flag = getFlag(resourcesManagerSC.getUserRoles());
    String userId = resourcesManagerSC.getUserId();
    request.setAttribute("Profile", flag);
    request.setAttribute("UserId", userId);
    // Reservation view context is allways filled into request
    request.setAttribute("viewContext", resourcesManagerSC.getViewContext());
    resourcesManagerSC.getViewContext().setWithWeekend(!resourcesManagerSC.isWeekendNotVisible());
    try {
      if (function.startsWith("Main")) {
        resourcesManagerSC.getViewContext().resetFilters();
        destination = displayCalendarView(request, resourcesManagerSC);
      } else if ("NewCategory".equals(function)) {
        request.setAttribute("listTemplates", getForms(resourcesManagerSC));
        destination = root + "categoryManager.jsp";
      } else if ("SaveCategory".equals(function)) {
        Category category = request2CategoryDetail(resourcesManagerSC, request);
        resourcesManagerSC.createCategory(category);
        destination = getDestination("ViewCategories", resourcesManagerSC, request);
      } else if ("EditCategory".equals(function)) {
        Category category = resourcesManagerSC.getCategory(request.getParameterAsLong("id"));
        List<PublicationTemplate> listTemplates = getForms(resourcesManagerSC);
        request.setAttribute("listTemplates", listTemplates);
        request.setAttribute("category", category);
        destination = root + "categoryManager.jsp";
      } else if ("ModifyCategory".equals(function)) {
        Category category = request2CategoryDetail(resourcesManagerSC, request);
        resourcesManagerSC.updateCategory(category);
        destination = getDestination("ViewCategories", resourcesManagerSC, request);
      } else if ("ViewCategories".equals(function)) {
        List<Category> list = resourcesManagerSC.getCategories();
        request.setAttribute("categories", list);
        destination = root + "categories.jsp";
      } else if ("DeleteCategory".equals(function)) {
        resourcesManagerSC.deleteCategory(request.getParameterAsLong("id"));
        destination = getDestination("ViewCategories", resourcesManagerSC, request);
      } else if ("NewResource".equals(function)) {
        resourcesManagerSC.setResourceIdForResource(null);
        resourcesManagerSC.setCategoryIdForResource(request.getParameterAsLong("categoryId"));
        // categorie pré-séléctionnée
        List<Category> list = resourcesManagerSC.getCategories();
        request.setAttribute("listCategories", list);
        request.setAttribute("categoryId", resourcesManagerSC.getCategoryIdForResource());
        setXMLFormIntoRequest(request, resourcesManagerSC);
        destination = root + "resourceManager.jsp";
      } else if ("SaveResource".equals(function)) {
        // récupération des données saisies dans le formulaire
        List<FileItem> items = request.getFileItems();

        Resource resource = request2ResourceDetail(resourcesManagerSC, items);
        resourcesManagerSC.createResource(resource);
        List<Long> managers = getManagers(items);
        resourcesManagerSC.updateResource(resource, managers);

        request.setAttribute("resourceId", resource.getId());
        request.setAttribute("provenance", "resources");

        resourcesManagerSC.setResourceIdForResource(resource.getId());
        updateXMLForm(resourcesManagerSC, items);

        destination = getDestination("ViewResource", resourcesManagerSC, request);
      } else if ("EditResource".equals(function)) {
        resourceId = request.getParameterAsLong("resourceId");
        if (resourceId == null) {
          resourceId = resourcesManagerSC.getCurrentResource();
        }
        Resource resource = resourcesManagerSC.getResource(resourceId);

        // on récupère l'ensemble des catégories pour la liste déroulante
        List<Category> list = resourcesManagerSC.getCategories();
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
      } else if ("ModifyResource".equals(function)) {
        // récupération des données saisies dans le formulaire
        List<FileItem> items = request.getFileItems();

        Resource resource = request2ResourceDetail(resourcesManagerSC, items);
        List<Long> managers = getManagers(items);
        resourcesManagerSC.updateResource(resource, managers);

        resourcesManagerSC.setResourceIdForResource(resource.getId());
        updateXMLForm(resourcesManagerSC, items);

        request.setAttribute("id", resource.getCategoryId());
        destination = getDestination("ViewResources", resourcesManagerSC, request);
      } else if ("ViewResources".equals(function)) {
        categoryId = request.getAttributeAsLong("id");
        if (categoryId == null) {
          categoryId = request.getParameterAsLong("id");
        }
        List<Resource> list = resourcesManagerSC.getResourcesByCategory(categoryId);
        List<Category> listcategories = resourcesManagerSC.getCategories();
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
        reservationId = request.getParameterAsLong("reservationId");
        if (reservationId != null) {
          resourcesManagerSC.setReservationIdForResource(reservationId);
        }
        resourceId = request.getParameterAsLong("resourceId");
        if (resourceId == null) {
          resourceId = request.getAttributeAsLong("resourceId");
        }
        if (resourceId != null) {
          resourcesManagerSC.setResourceIdForResource(resourceId);
        } else {
          resourceId = resourcesManagerSC.getResourceIdForResource();
        }
        Resource resource = resourcesManagerSC.getResource(resourceId);
        List<UserDetail> managers = resourcesManagerSC.getManagers(resourceId);
        request.setAttribute("Managers", managers);
        Category category = resourcesManagerSC.getCategory(resource.getCategoryId());

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
        resourcesManagerSC.deleteResource(request.getParameterAsLong("resourceId"));
        request.setAttribute("id", request.getParameterAsLong("categoryId"));
        destination = getDestination("ViewResources", resourcesManagerSC, request);
      } else if ("NewReservation".equals(function)) {
        request.setAttribute("objectView", request.getParameter("objectView"));
        String iso8601Date = request.getParameter("Day");
        if (StringUtil.isDefined(iso8601Date)) {
          Date date = DateUtil.parseISO8601Date(iso8601Date);
          request.setAttribute("defaultDate",
              DateUtil.dateToString(date, resourcesManagerSC.getLanguage()));
          if (DateUtil.resetHour(date).compareTo(date) != 0) {
            // Time is defined
            request.setAttribute("defaultTime",
                DateUtil.getOutputHour(date, resourcesManagerSC.getLanguage()));
          }
        }
        destination = root + "reservationManager.jsp";
      } else if ("GetAvailableResources".equals(function)) {
        reservationId = request.getParameterAsLong("reservationId");
        if (reservationId == null) {
          reservationId = request.getAttributeAsLong("reservationId");
        }
        @SuppressWarnings("unchecked")
        List<Resource> unavailableReservationResources = (List<Resource>) request.getAttribute(
            "unavailableReservationResources");
        List<Resource> resourcesOfReservation = null;
        // si unavailableReservationResources c'est qu'il n y a pas eu de problème d'enregistrement
        if (unavailableReservationResources == null) {
          Reservation reservation = request2ReservationDetail(request);
          resourcesManagerSC.createReservation(reservation);
          resourcesManagerSC.setBeginDateReservation(reservation.getBeginDate());
          resourcesManagerSC.setEndDateReservation(reservation.getEndDate());
        }
        Reservation reservation = resourcesManagerSC.getReservationCourante();
        List<Resource> reservableResources = resourcesManagerSC.getResourcesReservable(
            reservation.getBeginDate(), reservation.getEndDate());
        if (reservationId != null) {
          resourcesOfReservation = resourcesManagerSC.getResourcesofReservation(reservationId);
          reservableResources.removeAll(resourcesOfReservation);
          SilverTrace.info("resourcesManager", "ResourcesManagerRequestRouter.getDestination()",
              "root.MSG_GEN_PARAM_VALUE", "dans le if,idReservation=" + reservationId);

          if (unavailableReservationResources == null) {
            // When update process, verify resource collisions with other reservations to warn
            // the user immediately if any
            unavailableReservationResources = resourcesManagerSC
                .verifyUnavailableResources(ResourceUtil.toIdList(resourcesOfReservation),
                    resourcesManagerSC.getBeginDateReservation(),
                    resourcesManagerSC.getEndDateReservation(), reservationId);
          }

          // Resources that are not available are removed from the list of resource reservation
          if (CollectionUtils.isNotEmpty(resourcesOfReservation) && CollectionUtils.isNotEmpty(
              unavailableReservationResources)) {
            resourcesOfReservation.removeAll(unavailableReservationResources);
          }
        }
        List<Category> categories = resourcesManagerSC.getCategories();
        Map<Long, List<Resource>> resourcesAvailablePerCategory = new HashMap<Long, List<Resource>>(
            categories.size());
        for (Resource resourceReservable : reservableResources) {
          if (resourcesAvailablePerCategory.containsKey(resourceReservable.getCategoryId())) {
            resourcesAvailablePerCategory.get(resourceReservable.getCategoryId()).add(
                resourceReservable);
          } else {
            List<Resource> resourceReservables = new ArrayList<Resource>();
            resourceReservables.add(resourceReservable);
            resourcesAvailablePerCategory.put(resourceReservable.getCategoryId(),
                resourceReservables);
          }
        }
        SilverTrace.info("resourcesManager", "ResourcesManagerRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE",
            "listResourcesReservable=" + reservableResources.size());
        // on envoie l'id de la réservation et l'ensemble des resources
        // associées à celles -ci
        request.setAttribute("objectView", request.getParameter("objectView"));
        request.setAttribute("idReservation", reservationId);
        request.setAttribute("listResourceEverReserved", resourcesOfReservation);
        request.setAttribute("mapResourcesReservable", resourcesAvailablePerCategory);
        request.setAttribute("reservation", reservation);
        request.setAttribute("unavailableReservationResources", unavailableReservationResources);
        request.setAttribute("categories", categories);
        destination = root + "cart.jsp";
      } else if ("FinalReservation".equals(function)) {
        // resourceIds est la liste complète des ressources réservées lors
        // d'une création ou d'une édition de réservation
        // modifiedReservationId est l'id de la réservation à modifié lors d'une
        // édition de réservation
        // on doit vérifier que celles-ci n'ont pas été prises avant de mettre à
        // jour la réservation.
        Long modifiedReservationId = request.getParameterAsLong("modifiedReservationId");
        List<Long> resourceIds = ResourceUtil.toIdList(request.getParameter("listeResa"));
        // si modifiedReservationId n'est pas nulle, on est en train de modifier
        // une réservation
        // sinon on est en train de créer une réservation
        if (modifiedReservationId != null) {
          // on vérifie que les nouvelles ressources que l'on veut réserver ne
          // sont pas déjà prises.
          List<Resource> listeResourcesProblemeReservationTotal = resourcesManagerSC.
              verifyUnavailableResources(resourceIds,
                  resourcesManagerSC.getBeginDateReservation(),
                  resourcesManagerSC.getEndDateReservation(), modifiedReservationId);
          if (listeResourcesProblemeReservationTotal.isEmpty()) {
            // regarder si les dates ont été modifiées
            Reservation resa = resourcesManagerSC.getReservation(modifiedReservationId);
            boolean updateDate = false;
            if (!resourcesManagerSC.getBeginDateReservation().equals(resa.getBeginDate())
                || !resourcesManagerSC.getEndDateReservation().equals(resa.getEndDate())) {
              updateDate = true;
            }
            resourcesManagerSC.updateReservation(resa, resourceIds, updateDate);

            // redirection vers la réservation
            request.setAttribute("reservationId", modifiedReservationId);
            destination = getDestination("ViewReservation", resourcesManagerSC, request);
          } else {
            request.setAttribute("unavailableReservationResources",
                listeResourcesProblemeReservationTotal);
            request.setAttribute("reservationId", modifiedReservationId);
            destination = getDestination("GetAvailableResources", resourcesManagerSC, request);
          }
        } else {
          resourcesManagerSC.setListReservationCurrent(resourceIds);
          List<Resource> listeResourcesProblem = resourcesManagerSC.verifyUnavailableResources(
              resourceIds);
          if (listeResourcesProblem.isEmpty()) {
            resourcesManagerSC.saveReservation();
            request.setAttribute("reservationId",
                resourcesManagerSC.getReservationCourante().getId());
            resourcesManagerSC.getViewContext()
                .setReferenceDay(resourcesManagerSC.getReservationCourante().getBeginDate());
            destination = getDestination("ViewReservation", resourcesManagerSC, request);
          } else {
            request.setAttribute("unavailableReservationResources", listeResourcesProblem);
            destination = getDestination("GetAvailableResources", resourcesManagerSC, request);
          }
        }
      } else if ("EditReservation".equals(function)) {
        // listResources est la liste des ressources qui peuvent poser problème
        // quand on change la date de réservation
        List listResourcesProblem = null;
        reservationId = request.getParameterAsLong("id");
        if (reservationId == null) {
          reservationId = request.getAttributeAsLong("id");
          listResourcesProblem = (List) request.getAttribute("listResources");
        }
        Reservation reservation = resourcesManagerSC.getReservation(reservationId);
        // on envoie la réservation de l'id et la liste des ressources associées
        // ainsi que la liste qui posent problème quand on change les dates
        request.setAttribute("reservation", reservation);
        request.setAttribute("listResourcesProblem", listResourcesProblem);
        destination = root + "reservationManager.jsp";
      } else if ("ViewReservation".equals(function)) {
        reservationId = request.getParameterAsLong("reservationId");
        if (reservationId == null) {
          reservationId = request.getAttributeAsLong("reservationId");
        }
        String objectView = getView(request, resourcesManagerSC);
        // si on vient de resource.jsp, reservationId a été stocké dans le
        // session controler
        if (reservationId == null) {
          reservationId = resourcesManagerSC.getReservationIdForResource();
        }
        Reservation reservation = resourcesManagerSC.getReservation(reservationId);
        List<Resource> listResourcesofReservation = resourcesManagerSC.getResourcesofReservation(
            reservationId);
        request.setAttribute("listResourcesofReservation", listResourcesofReservation);
        request.setAttribute("reservationId", reservationId);
        request.setAttribute("reservation", reservation);
        request.setAttribute("objectView", objectView);
        destination = root + "viewReservation.jsp";
      } else if ("ViewReservations".equals(function)) {
        List<Reservation> listOfReservation = resourcesManagerSC.getReservationUser();
        request.setAttribute("listOfReservation", listOfReservation);
        destination = root + "viewReservations.jsp";
      } else if ("DeleteReservation".equals(function)) {
        resourcesManagerSC.deleteReservation(request.getParameterAsLong("id"));
        destination = displayCalendarView(request, resourcesManagerSC);
      } else if ("CategoryIdFilter".equals(function)) {
        resourcesManagerSC.getViewContext().setCategoryId(request.getParameterAsLong(
            "categoryIdFilter"));
        resourcesManagerSC.getViewContext().setResourceId(null);
        destination = displayCalendarView(request, resourcesManagerSC);
      } else if ("ResourceIdFilter".equals(function)) {
        resourcesManagerSC.getViewContext().setResourceId(
            request.getParameterAsLong("resourceIdFilter"));
        destination = displayCalendarView(request, resourcesManagerSC);
      } else if ("Calendar".equals(function)) {
        destination = displayCalendarView(request, resourcesManagerSC);
      } else if ("ViewReservationData".equals(function)) {
        resourcesManagerSC.getViewContext().setDataViewType(
            ResourceManagerDataViewType.reservations);
        destination = displayCalendarView(request, resourcesManagerSC);
      } else if ("ViewResourceData".equals(function)) {
        resourcesManagerSC.getViewContext().setDataViewType(ResourceManagerDataViewType.resources);
        destination = displayCalendarView(request, resourcesManagerSC);
      } else if ("ViewReservationListingData".equals(function)) {
        resourcesManagerSC.getViewContext().setDataViewType(
            ResourceManagerDataViewType.reservationListing);
        destination = displayCalendarView(request, resourcesManagerSC);
      } else if ("ViewByMonth".equals(function)) {
        resourcesManagerSC.getViewContext().setViewType(CalendarViewType.MONTHLY);
        destination = displayCalendarView(request, resourcesManagerSC);
      } else if ("ViewByWeek".equals(function)) {
        resourcesManagerSC.getViewContext().setViewType(CalendarViewType.WEEKLY);
        destination = displayCalendarView(request, resourcesManagerSC);
      } else if ("PreviousPeriod".equals(function)) {
        resourcesManagerSC.getViewContext().previous();
        destination = displayCalendarView(request, resourcesManagerSC);
      } else if ("NextPeriod".equals(function)) {
        resourcesManagerSC.getViewContext().next();
        destination = displayCalendarView(request, resourcesManagerSC);
      } else if ("GoToday".equals(function)) {
        Date selectedDate = request.getParameterAsDate("selectedDate");
        if (selectedDate == null) {
          resourcesManagerSC.getViewContext().today();
        } else {
          resourcesManagerSC.getViewContext().setReferenceDay(selectedDate);
        }
        destination = displayCalendarView(request, resourcesManagerSC);
      } else if ("Comments".equals(function)) {
        String provenance = resourcesManagerSC.getProvenanceResource();
        resourceId = resourcesManagerSC.getResourceIdForResource();
        Resource myResource = resourcesManagerSC.getResource(resourceId);
        ResourcesWrapper resources = resourcesManagerSC.getResources();
        String chemin = "";
        if ("resources".equals(provenance)) {
          // on vient de resources
          chemin = "<a href=\"ViewCategories\">" + EncodeHelper.javaStringToHtmlString(resources.
              getString(
                  "resourcesManager.listCategorie")) + "</a>";
          String chemin2 = "<a href=\"ViewResources?id=" + myResource.getCategoryId() + "\">"
              + EncodeHelper.javaStringToHtmlString(
                  resources.getString("resourcesManager.categorie")) + "</a>";
          chemin = chemin + " > " + chemin2;
        } else if ("reservation".equals(provenance)) {
          // on vient du récapitulatif de la réservation
          chemin = "<a href=\"ViewReservation\">" + EncodeHelper.javaStringToHtmlString(resources.
              getString(
                  "resourcesManager.recapitulatifReservation")) + "</a>";
        }
        request.setAttribute("Path", chemin);
        request.setAttribute("resourceId", resourceId);
        request.setAttribute("resourceName", myResource.getName());
        request.setAttribute("resourceType", getSessionControlBeanName());

        destination = root + "comments.jsp";
      } else if ("SelectManager".equals(function)) {
        destination = resourcesManagerSC.initUPToSelectManager("");
      } else if (function.startsWith("ChooseOtherPlanning")) {
        destination = resourcesManagerSC.initUserPanelOtherPlanning();
      } else if (function.startsWith("ViewOtherPlanning")) {
        // userPanel return
        UserDetail selectedUser = resourcesManagerSC.getSelectedUser();
        if (selectedUser != null) {
          request.setAttribute("userId", selectedUser.getId());
          // on indique au rooter qu'on regarde le calandrier de quelqu un
          request.setAttribute("objectView", "PlanningOtherUser");
        }
        destination = displayCalendarView(request, resourcesManagerSC);
      } else if (function.startsWith("searchResult")) {
        resourcesManagerSC.getViewContext().resetFilters();
        // traitement des recherches
        Long id = request.getParameterAsLong("Id");
        String type = request.getParameter("Type");
        if ("Reservation".equals(type)) {
          // traitement des réservations
          request.setAttribute("reservationId", id);
          resourcesManagerSC.getViewContext()
              .setReferenceDay(resourcesManagerSC.getReservation(id).getBeginDate());
          destination = getDestination("ViewReservation", resourcesManagerSC,
              request);
        } else if ("Category".equals(type)) {
          resourcesManagerSC.getViewContext().setDataViewType(ResourceManagerDataViewType.resources);
          resourcesManagerSC.getViewContext().setCategoryId(id);
          destination = displayCalendarView(request, resourcesManagerSC);
        } else if ("Resource".equals(type)) {
          resourcesManagerSC.getViewContext().setDataViewType(ResourceManagerDataViewType.resources);
          resourcesManagerSC.getViewContext()
              .setCategoryId(resourcesManagerSC.getResource(id).getCategoryId());
          resourcesManagerSC.getViewContext().setResourceId(id);
          request.setAttribute("resourceId", id);
          request.setAttribute("provenance", "calendar");
          destination = getDestination("ViewResource", resourcesManagerSC,
              request);
        }
      } else if ("ToSelectManagers".equals(function)) {
        // récupération de la liste des responsables
        Collection<String> currentManagers = request2Managers(request);
        try {
          destination = resourcesManagerSC.initUserSelect(currentManagers);
        } catch (Exception e) {
          SilverTrace.warn("resourcesManager",
              "resourcesManagerRequestRouter.getDestination()",
              "root.EX_USERPANEL_FAILED", "function = " + function, e);
        }
      } else if ("FromUserSelect".equals(function)) {
        // récupération des valeurs de userPanel
        SilverTrace.debug("resourcesManager",
            "ResourcesManagerRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "FromUserSelect:");
        Selection sel = resourcesManagerSC.getSelection();
        // Get users selected in User Panel
        String[] userIds = SelectionUsersGroups.getDistinctUserIds(sel.getSelectedElements(), null);
        SilverTrace.debug("resourcesManager", "ResourcesManagerRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "userIds:" + Arrays.toString(userIds));
        if (userIds.length != 0) {
          SilverTrace.debug("resourcesManager",
              "ResourcesManagerRequestRouter.getDestination()",
              "root.MSG_GEN_PARAM_VALUE", "userIds.length():" + userIds.length);

          UserDetail[] userDetails = SelectionUsersGroups.getUserDetails(userIds);
          SilverTrace.debug("resourcesManager",
              "ResourcesManagerRequestRouter.getDestination()",
              "root.MSG_GEN_PARAM_VALUE", "userDetails:" + Arrays.toString(userDetails));
          request.setAttribute("Managers", Arrays.asList(userDetails));
        }
        destination = root + "refreshFromUserSelect.jsp";
      } else if ("ValidateResource".equals(function)) {
        resourceId = request.getParameterAsLong("ResourceId");
        reservationId = request.getParameterAsLong("reservationId");
        String objectView = request.getParameter("objectView");
        resourcesManagerSC.validateResource(resourceId, reservationId);
        request.setAttribute("reservationId", reservationId);
        request.setAttribute("objectView", objectView);
        destination = getDestination("ViewReservation", resourcesManagerSC, request);
      } else if ("ForRefuseResource".equals(function)) {
        resourceId = request.getParameterAsLong("ResourceId");
        reservationId = request.getParameterAsLong("reservationId");
        String resourceName = request.getParameter("ResourceName");
        String objectView = request.getParameter("objectView");
        request.setAttribute("reservationId", reservationId);
        request.setAttribute("ResourceId", resourceId);
        request.setAttribute("ResourceName", resourceName);
        request.setAttribute("objectView", objectView);
        destination = root + "refuseResource.jsp";
      } else if ("RefuseResource".equals(function)) {
        resourceId = request.getParameterAsLong("ResourceId");
        reservationId = request.getParameterAsLong("reservationId");
        String motive = request.getParameter("Motive");
        String objectView = request.getParameter("objectView");
        resourcesManagerSC.refuseResource(resourceId, reservationId, motive);
        request.setAttribute("reservationId", reservationId);
        request.setAttribute("objectView", objectView);
        destination = getDestination("ViewReservation", resourcesManagerSC, request);
      } else {
        resourcesManagerSC.getViewContext().resetFilters();
        displayCalendarView(request, resourcesManagerSC);
        destination = root + function + ".jsp";
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    SilverTrace.info("resourcesManager", "ResourcesManagerRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
    return destination;
  }

  /**
   * on regarde soit : ses réservations -> myObjectView = myReservation les réservations d'une autre
   * personne -> myObjectView = PlanningOtherUser le planning d'une catégorie -> myObjectView = l'id
   * de la catégorie le planning d'une ressource -> myObjectView = l'id de la catégorie myObjectView
   * représente ce qu'on est en train de visualiser, il est nul à l'initialisation. idUser sert à
   * savoir l'id de la personne dont on regarde le calandrier
   *
   * @param request
   * @param sessionController
   * @return
   */
  private String displayCalendarView(HttpRequest request,
      ResourcesManagerSessionController sessionController) {
    String myObjectView = getView(request, sessionController);
    String idUser = (String) request.getAttribute("userId");
    // on regarde le planning d'une catégorie ou d'une ressource
    Long categoryId = sessionController.getViewContext().getCategoryId();
    if (categoryId != null) {
      List<Resource> listResourcesofCategory = sessionController.getResourcesByCategory(categoryId);
      request.setAttribute("listResourcesofCategory", listResourcesofCategory);
    }
    if ("allReservations".equals(myObjectView)) {
      // on regarde toutes les réservations
      sessionController.getViewContext().setSelectedUserId(null);
      sessionController.getViewContext().setForValidation(false);
    } else if ("PlanningOtherUser".equals(myObjectView)) {
      // on récupère les réservations de l'utilisateur, ainsi que son nom et prenom
      if (idUser != null) {
        sessionController.getViewContext().setSelectedUserId(idUser);
      } else {
        idUser = sessionController.getViewContext().getSelectedUserId();
      }
      sessionController.getViewContext().setForValidation(false);
    } else if ("viewForValidation".equals(myObjectView)) {
      // on regarde les réservations à valider
      sessionController.getViewContext().setSelectedUserId(null);
      sessionController.getViewContext().setForValidation(true);
    } else {
      // on regarde ses propres réservations sans modifier les filtres
      sessionController.getViewContext().setSelectedUserId(sessionController.getUserId());
      sessionController.getViewContext().setForValidation(false);
    }
    // Some useful data
    List<Category> listOfCategories = sessionController.getCategories();
    request.setAttribute("idUser", idUser);
    request.setAttribute("listOfCategories", listOfCategories);
    request.setAttribute("objectView", myObjectView);
    request.setAttribute("IsResponsible", sessionController.isResponsible());
    if (StringUtil.getBooleanValue(request.getParameter("isPortlet"))) {
      return root + "portlet.jsp";
    }
    return root + "almanach.jsp";
  }

  private String getView(HttpRequest request,
      ResourcesManagerSessionController sessionController) {
    String myObjectView = request.getParameter("objectView");
    if (StringUtil.isNotDefined(myObjectView)) {
      myObjectView = (String) request.getAttribute("objectView");
    }
    if (StringUtil.isNotDefined(myObjectView)) {
      myObjectView = sessionController.getDefaultView();
    }
    return myObjectView;
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

  private void setXMLFormIntoRequest(HttpRequest request,
      ResourcesManagerSessionController resourcesManagerSC) throws Exception {
    String resourceIdAsString = String.valueOf(resourcesManagerSC.getResourceIdForResource());
    Long idCategory = resourcesManagerSC.getCategoryIdForResource();
    Category category = resourcesManagerSC.getCategory(idCategory);
    String xmlFormName = category.getForm();
    if (StringUtil.isDefined(xmlFormName)) {
      String xmlFormShortName = xmlFormName.substring(
          xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
      // création du PublicationTemplate
      getPublicationTemplateManager().addDynamicPublicationTemplate(
          resourcesManagerSC.getComponentId() + ":" + xmlFormShortName,
          xmlFormName);
      PublicationTemplateImpl pubTemplate
          = (PublicationTemplateImpl) getPublicationTemplateManager().getPublicationTemplate(
              resourcesManagerSC.getComponentId() + ":" + xmlFormShortName, xmlFormName);

      // création du formulaire et du DataRecord
      Form formUpdate = pubTemplate.getUpdateForm();
      RecordSet recordSet = pubTemplate.getRecordSet();
      // attention ici ce n est pas categoryId mais resourceId
      DataRecord data = recordSet.getRecord(resourceIdAsString);
      if (data == null) {
        data = recordSet.getEmptyRecord();
        data.setId(resourceIdAsString);
      }
      // appel de la jsp avec les paramètres
      request.setAttribute("Form", formUpdate);
      request.setAttribute("Data", data);
      request.setAttribute("XMLFormName", xmlFormName);
    }
  }

  private void putXMLDisplayerIntoRequest(Resource resource,
      HttpRequest request,
      ResourcesManagerSessionController resourcesManagerSC)
      throws PublicationTemplateException, FormException {
    // récupération de l’Id de l’objet en fonction de l’objet "object"
    String resouceIdAsString = resource.getIdAsString();
    Long categoryId = resource.getCategoryId();
    Category category = resourcesManagerSC.getCategory(categoryId);

    String xmlFormName = category.getForm();
    String xmlFormShortName = xmlFormName.substring(
        xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));

    // register xmlForm
    getPublicationTemplateManager().addDynamicPublicationTemplate(
        resourcesManagerSC.getComponentId() + ":" + xmlFormShortName, xmlFormName);

    // création du PublicationTemplate
    PublicationTemplateImpl pubTemplate = (PublicationTemplateImpl) getPublicationTemplateManager().
        getPublicationTemplate(resourcesManagerSC.getComponentId() + ":" + xmlFormShortName,
            xmlFormName);
    // récupération des données
    Form formView = pubTemplate.getViewForm();
    RecordSet recordSet = pubTemplate.getRecordSet();
    DataRecord data = recordSet.getRecord(resouceIdAsString);
    if (data == null) {
      data = recordSet.getEmptyRecord();
      data.setId(resouceIdAsString);
    }
    // passage des paramètres à la request avec les données du formulaire
    request.setAttribute("XMLForm", formView);
    request.setAttribute("XMLData", data);

    PagesContext context = new PagesContext("myForm", "0", resourcesManagerSC.getLanguage(), false,
        resourcesManagerSC.getComponentId(), resourcesManagerSC.getUserId());
    context.setBorderPrinted(false);
    context.setObjectId(resouceIdAsString);
    request.setAttribute("context", context);
  }

  private void updateXMLForm(
      ResourcesManagerSessionController resourcesManagerSC, List<FileItem> items)
      throws Exception {
    // récupération de l’objet et du nom du formulaire
    String resourceIdAsString = String.valueOf(resourcesManagerSC.getResourceIdForResource());
    Long idCategory = resourcesManagerSC.getCategoryIdForResource();

    Category category = resourcesManagerSC.getCategory(idCategory);

    String xmlFormName = category.getForm();
    if (StringUtil.isDefined(xmlFormName)) {
      String xmlFormShortName = xmlFormName.substring(
          xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
      // récupération des données du formulaire (via le DataRecord)
      PublicationTemplate pub = getPublicationTemplateManager().getPublicationTemplate(
          resourcesManagerSC.getComponentId() + ":" + xmlFormShortName);
      RecordSet set = pub.getRecordSet();
      Form form = pub.getUpdateForm();
      DataRecord data = set.getRecord(resourceIdAsString);
      if (data == null) {
        data = set.getEmptyRecord();
        data.setId(resourceIdAsString);
      }

      // sauvegarde des données du formulaire
      PagesContext context
          = new PagesContext("myForm", "0", resourcesManagerSC.getLanguage(), false,
              resourcesManagerSC.getComponentId(),
              resourcesManagerSC.getUserId());
      context.setObjectId(resourceIdAsString);
      form.update(items, data, context);
      set.save(data);
    }
  }

  private List<String> request2Managers(HttpRequest request) {
    List<String> managers = new ArrayList<String>();
    String managerIds = request.getParameter("ManagerIds");
    if (StringUtil.isDefined(managerIds)) {
      String[] tabResources = managerIds.split(",");
      Collections.addAll(managers, tabResources);
    }
    return managers;
  }

  private List<Long> getManagers(List<FileItem> items) {
    List<Long> managers = new ArrayList<Long>();
    String managerIds = FileUploadUtil.getParameter(items, "managerIds");
    if (StringUtil.isDefined(managerIds)) {
      String[] tabResources = managerIds.split(",");
      for (String managerId : tabResources) {
        if (StringUtil.isLong(managerId)) {
          managers.add(Long.parseLong(managerId));
        }
      }
    }
    return managers;
  }

  /**
   * Gets an instance of PublicationTemplateManager.
   *
   * @return an instance of PublicationTemplateManager.
   */
  private PublicationTemplateManager getPublicationTemplateManager() {
    return PublicationTemplateManager.getInstance();
  }

  private List<PublicationTemplate> getForms(ResourcesManagerSessionController sc)
      throws PublicationTemplateException {
    GlobalContext gc = new GlobalContext(sc.getSpaceId(), sc.getComponentId());
    return getPublicationTemplateManager().getPublicationTemplates(gc);
  }
}
