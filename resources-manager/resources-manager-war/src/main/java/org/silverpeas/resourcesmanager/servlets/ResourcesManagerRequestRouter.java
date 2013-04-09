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
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.resourcemanager.model.Category;
import org.silverpeas.resourcemanager.model.Reservation;
import org.silverpeas.resourcemanager.model.Resource;
import org.silverpeas.resourcemanager.util.ResourceUtil;
import org.silverpeas.resourcesmanager.control.ResourcesManagerSessionController;
import org.silverpeas.servlet.HttpServletRequestWrapper;
import org.silverpeas.servlet.ServletRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
      ServletRequestWrapper request) {
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

  private Reservation request2ReservationDetail(ServletRequestWrapper request) {
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
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param resourcesManagerSC The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function,
      ResourcesManagerSessionController resourcesManagerSC, HttpServletRequest request) {
    return getDestination(function, resourcesManagerSC,
        new HttpServletRequestWrapper(request, resourcesManagerSC.getLanguage()));
  }

  /**
   * {@link #getDestination(String, com.stratelia.silverpeas.peasCore.ComponentSessionController,
   * javax.servlet.http.HttpServletRequest)}
   * @param function
   * @param resourcesManagerSC
   * @param request
   * @return
   */
  public String getDestination(String function,
      ResourcesManagerSessionController resourcesManagerSC, HttpServletRequestWrapper request) {
    Long categoryId;
    Long reservationId;
    Long resourceId;

    String destination = "";
    request.setAttribute("rsc", resourcesManagerSC);
    String flag = getFlag(resourcesManagerSC.getUserRoles());
    String userId = resourcesManagerSC.getUserId();
    request.setAttribute("Profile", flag);
    request.setAttribute("UserId", userId);
    try {
      if (function.startsWith("Main")) {
        destination = displayCalendarView(request, resourcesManagerSC);
      } else if ("NewCategory".equals(function)) {
        List<PublicationTemplate> listTemplates = getPublicationTemplateManager().
            getPublicationTemplates();
        request.setAttribute("listTemplates", listTemplates);
        destination = root + "categoryManager.jsp";
      } else if ("SaveCategory".equals(function)) {
        Category category = request2CategoryDetail(resourcesManagerSC, request);
        resourcesManagerSC.createCategory(category);
        destination = getDestination("ViewCategories", resourcesManagerSC, request);
      } else if ("EditCategory".equals(function)) {
        Category category = resourcesManagerSC.getCategory(request.getParameterAsLong("id"));
        List<PublicationTemplate> listTemplates = getPublicationTemplateManager().
            getPublicationTemplates();
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
        String date = request.getParameter("Day");
        if (StringUtil.isDefined(date)) {
          request.setAttribute("DefaultDate", date);
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
              "root.MSG_GEN_PARAM_VALUE", "dans le if,idReservation=" +
              reservationId);

          if (unavailableReservationResources == null) {
            // When update process, verify resource collisions with other reservations to warn
            // the user immediately if any
            unavailableReservationResources = resourcesManagerSC
                .verifyUnavailableResources(ResourceUtil.toIdList(resourcesOfReservation),
                    resourcesManagerSC.getBeginDateReservation(),
                    resourcesManagerSC.getEndDateReservation(), reservationId);
          }

          // Resources that are not available are removed from the list of resource reservation
          if (CollectionUtils.isNotEmpty(resourcesOfReservation) &&
              CollectionUtils.isNotEmpty(unavailableReservationResources)) {
            resourcesOfReservation.removeAll(unavailableReservationResources);
          }
        }
        List<Category> categories = resourcesManagerSC.getCategories();
        Map<Long, List<Resource>> resourcesAvailablePerCategory =
            new HashMap<Long, List<Resource>>(categories.size());
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
          List<Resource> listeResourcesProblemeReservationTotal =
              resourcesManagerSC.verifyUnavailableResources(resourceIds,
                  resourcesManagerSC.getBeginDateReservation(),
                  resourcesManagerSC.getEndDateReservation(), modifiedReservationId);
          if (listeResourcesProblemeReservationTotal.isEmpty()) {
            // regarder si les dates ont été modifiées
            Reservation resa = resourcesManagerSC.getReservation(modifiedReservationId);
            boolean updateDate = false;
            if (!resourcesManagerSC.getBeginDateReservation().equals(resa.getBeginDate()) ||
                !resourcesManagerSC.getEndDateReservation().equals(resa.getEndDate())) {
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
        String objectView = getView(request);
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
      } else if ("Calendar".equals(function)) {
        destination = displayCalendarView(request, resourcesManagerSC);
      } else if ("PreviousMonth".equals(function)) {
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
        resourceId = resourcesManagerSC.getResourceIdForResource();
        Resource myResource = resourcesManagerSC.getResource(resourceId);
        ResourcesWrapper resources = resourcesManagerSC.getResources();
        String chemin = "";
        if ("resources".equals(provenance)) {
          // on vient de resources
          chemin = "<a href=\"ViewCategories\">" +
              EncodeHelper.javaStringToHtmlString(resources.getString(
              "resourcesManager.listCategorie")) + "</a>";
          String chemin2 = "<a href=\"ViewResources?id=" + myResource.getCategoryId() + "\">" +
              EncodeHelper.javaStringToHtmlString(
              resources.getString("resourcesManager.categorie")) + "</a>";
          chemin = chemin + " > " + chemin2;
        } else if ("reservation".equals(provenance)) {
          // on vient du récapitulatif de la réservation
          chemin = "<a href=\"ViewReservation\">" +
              EncodeHelper.javaStringToHtmlString(resources.getString(
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
        }
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
              "root.MSG_GEN_PARAM_VALUE", "userDetails:" +
              Arrays.toString(userDetails));
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
  private String displayCalendarView(ServletRequestWrapper request,
      ResourcesManagerSessionController sessionController) {
    String myObjectView = getView(request);
    String idUser = (String) request.getAttribute("userId");
    List<Reservation> listOfReservation = null;
    List<Reservation> listReservationsOfCategory = null;
    Long currentResourceId = null;
    // on regarde le planning d'une catégorie ou d'une ressource
    if ((myObjectView != null) && isNotAnUserView(myObjectView)) {
      Long categoryId = Long.valueOf(myObjectView);
      listReservationsOfCategory = sessionController.getMonthReservationOfCategory(categoryId);
      List<Resource> listResourcesofCategory = sessionController.getResourcesByCategory(categoryId);
      currentResourceId = request.getParameterAsLong("resourceId");
      if (currentResourceId != null) {
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
    List<Category> listOfCategories = sessionController.getCategories();
    request.setAttribute("idUser", idUser);
    request.setAttribute("listOfCategories", listOfCategories);
    request.setAttribute("idCategory", myObjectView);
    request.setAttribute("monthC", monthC);
    request.setAttribute("IsResponsible", sessionController.isResponsible());
    if (StringUtil.getBooleanValue(request.getParameter("isPortlet"))) {
      return root + "portlet.jsp";
    }
    return root + "almanach.jsp";
  }

  private String getView(ServletRequestWrapper request) {
    String myObjectView = request.getParameter("objectView");
    if (!StringUtil.isDefined(myObjectView)) {
      myObjectView = (String) request.getAttribute("objectView");
    }
    return myObjectView;
  }

  private boolean isNotAnUserView(String myObjectView) {
    return (!"myReservation".equals(myObjectView)) &&
        (!"PlanningOtherUser".equals(myObjectView)) &&
        (!"viewUser".equals(myObjectView)) &&
        (!"viewForValidation".equals(myObjectView));
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

  private void setXMLFormIntoRequest(ServletRequestWrapper request,
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
      PublicationTemplateImpl pubTemplate =
          (PublicationTemplateImpl) getPublicationTemplateManager().getPublicationTemplate(
          resourcesManagerSC.getComponentId() + ":" +
          xmlFormShortName, xmlFormName);

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
      ServletRequestWrapper request,
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
        resourcesManagerSC.getComponentId() +
        ":" + xmlFormShortName, xmlFormName);

    // création du PublicationTemplate
    PublicationTemplateImpl pubTemplate =
        (PublicationTemplateImpl) getPublicationTemplateManager().
        getPublicationTemplate(resourcesManagerSC.getComponentId() + ":" +
        xmlFormShortName, xmlFormName);
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
      PublicationTemplate pub =
          getPublicationTemplateManager().getPublicationTemplate(resourcesManagerSC.getComponentId() + ":" +
          xmlFormShortName);
      RecordSet set = pub.getRecordSet();
      Form form = pub.getUpdateForm();
      DataRecord data = set.getRecord(resourceIdAsString);
      if (data == null) {
        data = set.getEmptyRecord();
        data.setId(resourceIdAsString);
      }

      // sauvegarde des données du formulaire
      PagesContext context =
          new PagesContext("myForm", "0", resourcesManagerSC.getLanguage(), false,
          resourcesManagerSC.getComponentId(),
          resourcesManagerSC.getUserId());
      context.setObjectId(resourceIdAsString);
      form.update(items, data, context);
      set.save(data);
    }
  }

  private List<String> request2Managers(ServletRequestWrapper request) {
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

  private void setEvents(MonthCalendar monthC, List<Reservation> listOfReservation,
      List<Reservation> listReservationsOfCategory,
      ResourcesManagerSessionController resourcesManagerSC, String view,
      Long currentResourceId) {
    String objectView = view;
    if (view == null) {
      objectView = "myReservation";
    }
    // transformation des réservations (Reservation) en Event du MonthCalendar
    if (listOfReservation != null) {
      for (Reservation maReservation : listOfReservation) {
        Long reservationId = maReservation.getId();
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
      for (Reservation maReservation : listReservationsOfCategory) {
        List<Resource> listResourcesReserved = resourcesManagerSC.getResourcesofReservation(
            maReservation.getId());
        // listResourcesReserved contient la liste des ressources reservees de la reservation pour
        // la categorie
        if (listResourcesReserved != null) {
          for (Resource myResource : listResourcesReserved) {
            Long categoryId = myResource.getCategoryId();
            // on affiche les ressources de la réservation qui possèdent la même categoryId que la
            // catégorie sélectionnée
            if (categoryId.toString().equals(objectView)) {
              // si currentResourceId est nulle aucune ressource n'a été sélectionnée
              // donc on affiche toutes les ressources de la catégorie
              Long resourceId = myResource.getId();
              if (currentResourceId == null || currentResourceId.equals(resourceId)) {
                String resourceName = myResource.getName();
                Event evt =
                    reservation2Event(maReservation, resourceId, resourceName, resourcesManagerSC);
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

  private Event reservation2Event(Reservation reservation, Long id, String label,
      ResourcesManagerSessionController sc) {
    Date endDate = reservation.getEndDate();
    Date startDate = reservation.getBeginDate();
    String minuteHourDateBegin = DateUtil.getFormattedTime(reservation.getBeginDate());
    String minuteHourDateEnd = DateUtil.getFormattedTime(reservation.getEndDate());
    Event evt = new Event(String.valueOf(id), label, startDate, endDate, null, 0);
    evt.setStartHour(minuteHourDateBegin);
    evt.setEndHour(minuteHourDateEnd);
    evt.setPlace(reservation.getPlace());
    evt.setInstanceId(reservation.getInstanceId());
    evt.setTooltip(sc.getString("resourcesManager.bookedBy") +
        sc.getUserDetail(reservation.getUserId()).getDisplayedName());
    return evt;
  }
}