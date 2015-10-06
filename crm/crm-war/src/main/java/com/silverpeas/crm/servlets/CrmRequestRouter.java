/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.crm.servlets;

import com.silverpeas.crm.control.CrmSessionController;
import com.silverpeas.crm.model.Crm;
import com.silverpeas.crm.model.CrmContact;
import com.silverpeas.crm.model.CrmDelivery;
import com.silverpeas.crm.model.CrmEvent;
import com.silverpeas.crm.model.CrmParticipant;
import com.stratelia.silverpeas.containerManager.ContainerContext;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.persistence.IdPK;
import org.silverpeas.servlet.HttpRequest;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class CrmRequestRouter extends ComponentRequestRouter<CrmSessionController> {

  private static final long serialVersionUID = 8189464245986518392L;

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "crm";
  }

  /**
   * Method declaration
   * @param mainSC
   * @param componentContext
   * @return
   * @see
   */
  public CrmSessionController createComponentSessionController(
      MainSessionController mainSC, ComponentContext componentContext) {
    return new CrmSessionController(mainSC, componentContext);
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param crmSC The component Session Control, build and initialised.
   * @param request
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function, CrmSessionController crmSC, HttpRequest request) {
    String destination = "";
    SilverTrace.info("crm", "CrmRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE",
        "User=" + crmSC.getUserId() + " Function=" + function);

    try {
      resetContainerContext(crmSC, request);
      resetReturnURL(crmSC, request);

      if ("Main".equals(function)) {
        destination = setJournalContext(crmSC, request);
      } else if ("ViewClient".equals(function)) {
        destination = setClientContext(crmSC, request);
      } else if ("ViewProject".equals(function)) {
        destination = setProjectContext(crmSC, request);
      } else if ("ViewDelivrable".equals(function)) {
        destination = setDeliveryContext(crmSC, request);
      } else if ("ViewJournal".equals(function)) {
        destination = setJournalContext(crmSC, request);
      } else if ("NewEvent".equals(function)) {
        String eventId = param(request, "eventId");
        setNewEventContext(eventId, crmSC, request);
        destination = "/crm/jsp/newEvent.jsp";
      } else if ("ChangeEvent".equals(function)) {
        String eventId = param(request, "eventId");
        String userName = crmSC.getFilterLib();
        String userId = param(request, "FilterId");
        String eventLib = param(request, "eventLib");
        String eventDate = crmSC.getDate(param(request, "eventDate"));
        String actionTodo = param(request, "actionTodo");
        String actionDate = crmSC.getDate(param(request, "actionDate"));
        String state = request.getParameter("eventState");
        String sort = param(request, "ArrayPaneAction");
        if (!"Sort".equals(sort)) {
          Crm defaultCrm = crmSC.getCurrentCrm();
          CrmEvent event;
          boolean creation = eventId.equals("");
          if (creation) {
            event = new CrmEvent();
          } else {
            IdPK eventPK = new IdPK();
            eventPK.setId(eventId);
            event = crmSC.getCrmEvent(eventPK);
          }
          event.setUserName(userName);
          event.setUserId(userId);
          event.setEventLib(eventLib);
          event.setEventDate(eventDate);
          event.setActionDate(actionDate);
          event.setActionTodo(actionTodo);
          event.setState(state);
          event.setCrmId(Integer.parseInt(defaultCrm.getPK().getId()));
          if (creation) {
            crmSC.createCrmEvent(event);
          } else {
            crmSC.updateCrmEvent(event);
          }
        }
        destination = setJournalContext(crmSC, request);
      } else if ("CallUserPanelEvent".equals(function)) {
        // save request param
        crmSC.setEventId(param(request, "eventId"));
        crmSC.setEventState(request.getParameter("eventState"));
        crmSC.setFilterId(param(request, "FilterId"));
        crmSC.setEventLib(param(request, "eventLib"));
        crmSC.setEventDate(crmSC.getDate(param(request, "eventDate")));
        crmSC.setActionDate(crmSC.getDate(param(request, "actionDate")));
        crmSC.setActionTodo(param(request, "actionTodo"));

        // init user panel
        destination = crmSC.initUserPanelEvent();
      } else if ("ReturnFromUserPanelEvent".equals(function)) {
        // get user panel data (update FilterLib, FilterId)
        crmSC.userPanelReturn();

        // restore request param
        request.setAttribute("eventId", crmSC.getEventId());
        request.setAttribute("States", crmSC.getStates());
        request.setAttribute("eventState", crmSC.getEventState());
        request.setAttribute("FilterLib", crmSC.getFilterLib());
        request.setAttribute("FilterId", crmSC.getFilterId());
        request.setAttribute("eventLib", crmSC.getEventLib());
        request.setAttribute("eventDate", crmSC.getDisplayDate(crmSC.getEventDate()));
        request.setAttribute("actionDate", crmSC.getDisplayDate(crmSC.getActionDate()));
        request.setAttribute("actionTodo", crmSC.getActionTodo());

        destination = "/crm/jsp/newEvent.jsp";
      } else if ("DeleteEvent".equals(function)) {
        String eventId = request.getParameter("eventId");
        if (eventId != null) {
          IdPK eventPK = new IdPK();
          eventPK.setId(eventId);
          crmSC.deleteCrmEvent(eventPK);
        }
        destination = setJournalContext(crmSC, request);
      } else if ("NewDelivery".equals(function)) {
        String deliveryId = param(request, "deliveryId");
        setNewDeliveryContext(deliveryId, crmSC, request);
        destination = "/crm/jsp/newDelivery.jsp";
      } else if (function.equals("ChangeDelivery")) {
        String deliveryId = param(request, "deliveryId");
        String deliveryDate = crmSC.getDate(param(request, "deliveryDate"));
        String deliveryElement = param(request, "deliveryElement");
        String deliveryVersion = param(request, "deliveryVersion");
        String userName = crmSC.getFilterLib();
        String userId = param(request, "FilterId");
        String deliveryContact = param(request, "deliveryContact");
        int contactId = (deliveryContact.length() > 0 ? Integer.parseInt(deliveryContact) : 0);
        String deliveryContactName = crmSC.getContactName(crmSC.getCurrentCrm().getPK(), contactId);

        String media = request.getParameter("deliveryMedia");
        String sort = param(request, "ArrayPaneAction");
        if (!"Sort".equals(sort)) {
          CrmDelivery delivery;
          boolean creation = (deliveryId.equals(""));
          if (creation) {
            delivery = new CrmDelivery();
          } else {
            IdPK deliveryPK = new IdPK();
            deliveryPK.setId(deliveryId);
            delivery = crmSC.getCrmDelivery(deliveryPK);
          }
          delivery.setDeliveryDate(deliveryDate);
          delivery.setElement(deliveryElement);
          delivery.setVersion(deliveryVersion);
          delivery.setDeliveryName(userName);
          delivery.setDeliveryId(userId);
          delivery.setContactId(contactId);
          delivery.setContactName(deliveryContactName);
          delivery.setMedia(media);
          delivery.setCrmId(Integer.parseInt(crmSC.getCurrentCrm().getPK().getId()));
          if (creation) {
            crmSC.createCrmDelivery(delivery);
          } else {
            crmSC.updateCrmDelivery(delivery);
          }
        }
        destination = setDeliveryContext(crmSC, request);
      } else if ("CallUserPanelDelivery".equals(function)) {
        // save request param
        crmSC.setDeliveryId(param(request, "deliveryId"));
        crmSC.setDeliveryDate(crmSC.getDate(param(request, "deliveryDate")));
        crmSC.setDeliveryElement(param(request, "deliveryElement"));
        crmSC.setDeliveryVersion(param(request, "deliveryVersion"));
        crmSC.setFilterId(param(request, "FilterId"));
        String deliveryContact = param(request, "deliveryContact");
        int contactId = (StringUtil.isDefined(deliveryContact)
            ? Integer.parseInt(deliveryContact) : 0);
        crmSC.setDeliveryContact(contactId);
        crmSC.setDeliveryMedia(request.getParameter("deliveryMedia"));

        // init user panel
        destination = crmSC.initUserPanelDelivery();
      } else if ("ReturnFromUserPanelDelivery".equals(function)) {
        // get user panel data (update FilterLib, FilterId)
        crmSC.userPanelReturn();

        // restore request param
        request.setAttribute("deliveryId", crmSC.getDeliveryId());
        request.setAttribute("deliveryDate", crmSC.getDisplayDate(crmSC.getDeliveryDate()));
        request.setAttribute("deliveryElement", crmSC.getDeliveryElement());
        request.setAttribute("deliveryVersion", crmSC.getDeliveryVersion());
        request.setAttribute("FilterLib", crmSC.getFilterLib());
        request.setAttribute("FilterId", crmSC.getFilterId());
        request.setAttribute("Contacts", crmSC.getContacts());
        request.setAttribute("deliveryContact", crmSC.getDeliveryContact());
        request.setAttribute("Medias", crmSC.getMedias());
        request.setAttribute("deliveryMedia", crmSC.getDeliveryMedia());

        destination = "/crm/jsp/newDelivery.jsp";
      } else if ("DeleteDelivery".equals(function)) {
        String deliveryId = request.getParameter("deliveryId");

        if (deliveryId != null) {
          IdPK deliveryPK = new IdPK();
          deliveryPK.setId(deliveryId);
          crmSC.deleteCrmDelivery(deliveryPK);
        }
        destination = setDeliveryContext(crmSC, request);
      } else if ("NewContact".equals(function)) {
        String contactId = param(request, "contactId");
        setNewContactContext(contactId, crmSC, request);
        destination = "/crm/jsp/newContact.jsp";
      } else if ("ChangeContact".equals(function)) {
        String contactId = param(request, "contactId");

        String contactName = param(request, "contactName");
        String contactFunction = param(request, "contactFunction");
        String contactTel = param(request, "contactTel");
        String contactEmail = param(request, "contactEmail");
        String contactAddress = param(request, "contactAddress");
        String contactActif = request.getParameter("contactActif");
        if (!"1".equals(contactActif)) {
          contactActif = "0";
        }

        String sort = param(request, "ArrayPaneAction");
        if (!"Sort".equals(sort)) {
          CrmContact contact;
          boolean creation = contactId.equals("");
          if (creation) {
            contact = new CrmContact();
          } else {
            IdPK contactPK = new IdPK();
            contactPK.setId(contactId);
            contact = crmSC.getCrmContact(contactPK);
          }
          contact.setName(contactName);
          contact.setFunctionContact(contactFunction);
          contact.setTel(contactTel);
          contact.setEmail(contactEmail);
          contact.setAddress(contactAddress);
          contact.setActive(contactActif);
          contact.setCrmId(Integer.parseInt(crmSC.getCurrentCrm().getPK().getId()));
          if (creation) {
            crmSC.createCrmContact(contact);
          } else {
            crmSC.updateCrmContact(contact);
          }
        }
        destination = setClientContext(crmSC, request);
      } else if ("DeleteContact".equals(function)) {
        String contactId = request.getParameter("contactId");

        if (contactId != null) {
          // 1. suppression des Delivrables liés au contact
          List<CrmDelivery> deliveries = crmSC.getCrmDeliverys(crmSC.getCurrentCrm().getPK());
          for (CrmDelivery delivery : deliveries) {
            if (contactId.equals(delivery.getContactId())) {
              crmSC.deleteCrmDelivery(delivery.getPK());
            }
          }
          // 2. suppression du contact
          IdPK contactPK = new IdPK();
          contactPK.setId(contactId);
          crmSC.deleteCrmContact(contactPK);
        }
        destination = setClientContext(crmSC, request);
      } else if (function.equals("UpdateClient")) {
        request.setAttribute("clientName", crmSC.getCurrentCrm().getClientName());
        destination = "/crm/jsp/updateClient.jsp";
      } else if (function.equals("ChangeClient")) {
        Crm defaultCrm = crmSC.getCurrentCrm();
        String clientName = param(request, "clientName");
        defaultCrm.setClientName(clientName);
        crmSC.updateCrm(defaultCrm);
        destination = setClientContext(crmSC, request);
      } else if (function.equals("UpdateProject")) {
        request.setAttribute("projectCode", crmSC.getCurrentCrm().getProjectCode());
        destination = "/crm/jsp/updateProject.jsp";
      } else if (function.equals("ChangeProject")) {
        Crm defaultCrm = crmSC.getCurrentCrm();
        String projectCode = param(request, "projectCode");
        defaultCrm.setProjectCode(projectCode);
        crmSC.updateCrm(defaultCrm);
        destination = setProjectContext(crmSC, request);
      } else if (function.equals("NewParticipant")) {
        String participantId = param(request, "participantId");
        setNewParticipantContext(participantId, crmSC, request);
        destination = "/crm/jsp/newParticipant.jsp";
      } else if (function.equals("ChangeParticipant")) {
        String participantId = param(request, "participantId");
        String participantName = crmSC.getFilterLib();
        String participantUserId = param(request, "FilterId");
        String participantFunction = param(request, "participantFunction");
        String participantEmail = param(request, "participantEmail");
        String participantActif = request.getParameter("participantActif");
        if (!"1".equals(participantActif)) {
          participantActif = "0";
        }

        String sort = param(request, "ArrayPaneAction");
        if (!sort.equals("Sort")) {
          CrmParticipant participant;
          boolean creation = participantId.equals("");
          if (creation) {
            participant = new CrmParticipant();
          } else {
            IdPK participantPK = new IdPK();
            participantPK.setId(participantId);
            participant = crmSC.getCrmParticipant(participantPK);
          }
          participant.setUserName(participantName);
          participant.setUserId(participantUserId);
          participant.setFunctionParticipant(participantFunction);
          participant.setEmail(participantEmail);
          participant.setActive(participantActif);
          participant.setCrmId(Integer.parseInt(crmSC.getCurrentCrm().getPK().getId()));
          if (creation) {
            crmSC.createCrmParticipant(participant);
            participantId = participant.getPK().getId();
          } else {
            crmSC.updateCrmParticipant(participant);
          }
        }
        destination = setProjectContext(crmSC, request);
      } else if (function.equals("CallUserPanelParticipant")) {
        // save request param
        crmSC.setParticipantId(param(request, "participantId"));
        crmSC.setParticipantEmail(param(request, "participantEmail"));
        crmSC.setParticipantActif(param(request, "participantActif"));
        crmSC.setFunction(param(request, "participantFunction"));
        // crmSC.setFilterLib(param(request, "FilterLib"));
        crmSC.setFilterId(param(request, "FilterId"));

        // init user panel
        destination = crmSC.initUserPanelParticipant();
      } else if (function.equals("ReturnFromUserPanelParticipant")) {
        // get user panel data (update FilterLib, FilterId)
        crmSC.userPanelReturn();

        request.setAttribute("participantId", crmSC.getParticipantId());
        request.setAttribute("Functions", crmSC.getFunctions());
        request.setAttribute("participantFunction", crmSC.getFunction());
        request.setAttribute("FilterLib", crmSC.getFilterLib());
        request.setAttribute("FilterId", crmSC.getFilterId());
        request.setAttribute("participantEmail", crmSC.getParticipantEmail());
        request.setAttribute("participantActif", crmSC.getParticipantActif());

        destination = "/crm/jsp/newParticipant.jsp";
      } else if (function.equals("DeleteParticipant")) {
        String participantId = request.getParameter("participantId");
        if (participantId != null) {
          IdPK participantPK = new IdPK();
          participantPK.setId(participantId);
          crmSC.deleteCrmParticipant(participantPK);
        }
        destination = setProjectContext(crmSC, request);
      } else if (function.equals("searchResult")) {
        String id = request.getParameter("Id");
        String type = request.getParameter("Type");

        if (type.equals("crm")) {
          destination = setClientContext(crmSC, request);
        } else if (type.equals("Contact")) {
          setNewContactContext(id, crmSC, request);
          destination = "/crm/jsp/viewContact.jsp";
        } else if (type.equals("Delivery")) {
          setNewDeliveryContext(id, crmSC, request);
          destination = "/crm/jsp/viewDelivery.jsp";
        } else if (type.equals("Event")) {
          setNewEventContext(id, crmSC, request);
          destination = "/crm/jsp/viewEvent.jsp";
        } else if (type.equals("Participant")) {
          setNewParticipantContext(id, crmSC, request);
          destination = "/crm/jsp/viewParticipant.jsp";
        }
      } else {
        destination = "/crm/jsp/" + function;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    request.setAttribute("context",
        ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL"));

    SilverTrace.info("crm", "CrmRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE",
        "Destination=" + destination);
    return destination;
  }

  private String getFlag(String[] profiles) {
    String flag = "user";
    for (String profile : profiles) {
      if (profile.equals("admin")) {
        return profile;
      } else if (!profile.equals("user")) {
        flag = profile;
      }
    }
    return flag;
  }

  private String setClientContext(CrmSessionController crmSC, HttpServletRequest request) {
    setAdminAttribute(crmSC, request);
    request.setAttribute("contactVOs", crmSC.getContactVOs());
    request.setAttribute("clientName", crmSC.getCurrentCrm().getClientName());
    return "/crm/jsp/client.jsp";
  }

  private String setProjectContext(CrmSessionController crmSC, HttpServletRequest request) {
    setAdminAttribute(crmSC, request);
    request.setAttribute("participantVOs", crmSC.getParticipantVOs());
    request.setAttribute("projectCode", crmSC.getCurrentCrm().getProjectCode());
    return "/crm/jsp/project.jsp";
  }

  private String setDeliveryContext(CrmSessionController crmSC, HttpServletRequest request) {
    setAdminAttribute(crmSC, request);
    request.setAttribute("deliveryVOs", crmSC.getDeliveryVOs());
    return "/crm/jsp/delivery.jsp";
  }

  private String setJournalContext(CrmSessionController crmSC, HttpServletRequest request) {
    setAdminAttribute(crmSC, request);
    request.setAttribute("eventVOs", crmSC.getEventVOs());
    return "/crm/jsp/journal.jsp";
  }

  private void setNewEventContext(String eventId, CrmSessionController crmSC,
      HttpServletRequest request) {
    crmSC.setEventLib("");
    crmSC.setEventDate("");
    crmSC.setActionDate("");
    crmSC.setActionTodo("");
    crmSC.setFilterLib("");
    crmSC.setFilterId("");
    crmSC.setEventState("1");

    if (!eventId.equals("")) {
      IdPK eventPK = new IdPK();
      eventPK.setId(eventId);
      CrmEvent event = crmSC.getCrmEvent(eventPK);

      crmSC.setEventState(event.getState());
      crmSC.setFilterLib(event.getUserName());
      crmSC.setFilterId(event.getUserId());
      crmSC.setEventLib(event.getEventLib());
      crmSC.setEventDate(event.getEventDate());
      crmSC.setActionDate(event.getActionDate());
      crmSC.setActionTodo(event.getActionTodo());
    }

    request.setAttribute("eventId", eventId);
    request.setAttribute("States", crmSC.getStates());
    request.setAttribute("eventState", crmSC.getEventState());
    request.setAttribute("FilterLib", crmSC.getFilterLib());
    request.setAttribute("FilterId", crmSC.getFilterId());
    request.setAttribute("eventLib", crmSC.getEventLib());
    request.setAttribute("eventDate", crmSC.getDisplayDate(crmSC.getEventDate()));
    request.setAttribute("actionDate", crmSC.getDisplayDate(crmSC.getActionDate()));
    request.setAttribute("actionTodo", crmSC.getActionTodo());
  }

  private void setNewDeliveryContext(String deliveryId, CrmSessionController crmSC,
      HttpServletRequest request) {
    crmSC.setDeliveryDate("");
    crmSC.setDeliveryElement("");
    crmSC.setDeliveryVersion("");
    crmSC.setFilterLib("");
    crmSC.setFilterId("");
    crmSC.setDeliveryContact(0);
    crmSC.setDeliveryMedia("1");

    if (!deliveryId.equals("")) {
      IdPK deliveryPK = new IdPK();
      deliveryPK.setId(deliveryId);
      CrmDelivery delivery = crmSC.getCrmDelivery(deliveryPK);

      crmSC.setDeliveryDate(delivery.getDeliveryDate());
      crmSC.setDeliveryElement(delivery.getElement());
      crmSC.setDeliveryVersion(delivery.getVersion());
      crmSC.setFilterLib(delivery.getDeliveryName());
      crmSC.setFilterId(delivery.getDeliveryId());
      crmSC.setDeliveryContact(delivery.getContactId());
      crmSC.setDeliveryMedia(delivery.getMedia());
    }

    request.setAttribute("deliveryId", deliveryId);
    request.setAttribute("deliveryDate", crmSC.getDisplayDate(crmSC.getDeliveryDate()));
    request.setAttribute("deliveryElement", crmSC.getDeliveryElement());
    request.setAttribute("deliveryVersion", crmSC.getDeliveryVersion());
    request.setAttribute("FilterLib", crmSC.getFilterLib());
    request.setAttribute("FilterId", crmSC.getFilterId());
    request.setAttribute("Contacts", crmSC.getContacts());
    request.setAttribute("deliveryContact", crmSC.getDeliveryContact());
    request.setAttribute("Medias", crmSC.getMedias());
    request.setAttribute("deliveryMedia", crmSC.getDeliveryMedia());
  }

  private void setNewContactContext(String contactId, CrmSessionController crmSC,
      HttpServletRequest request) {
    String contactName = "";
    String contactFunction = "";
    String contactTel = "";
    String contactEmail = "";
    String contactAddress = "";
    String contactActif = "1";

    if (!contactId.equals("")) {
      IdPK contactPK = new IdPK();
      contactPK.setId(contactId);
      CrmContact contact = crmSC.getCrmContact(contactPK);

      contactName = contact.getName();
      contactFunction = contact.getFunctionContact();
      contactTel = contact.getTel();
      contactEmail = contact.getEmail();
      contactAddress = contact.getAddress();
      contactActif = contact.getActive();
    }
    request.setAttribute("contactId", contactId);
    request.setAttribute("contactName", contactName);
    request.setAttribute("contactFunction", contactFunction);
    request.setAttribute("contactTel", contactTel);
    request.setAttribute("contactEmail", contactEmail);
    request.setAttribute("contactAddress", contactAddress);
    request.setAttribute("contactActif", contactActif);
  }

  private void setNewParticipantContext(String participantId, CrmSessionController crmSC,
      HttpServletRequest request) {
    String participantEmail = "";
    String participantActif = "1";

    crmSC.setFunction("1");
    crmSC.setFilterLib("");
    crmSC.setFilterId("");

    if (!participantId.equals("")) {
      IdPK participantPK = new IdPK();
      participantPK.setId(participantId);
      CrmParticipant participant = crmSC.getCrmParticipant(participantPK);

      crmSC.setFunction(participant.getFunctionParticipant());
      crmSC.setFilterLib(participant.getUserName());
      crmSC.setFilterId(participant.getUserId());

      participantEmail = participant.getEmail();
      participantActif = participant.getActive();
    }

    request.setAttribute("participantId", participantId);
    request.setAttribute("Functions", crmSC.getFunctions());
    request.setAttribute("participantFunction", crmSC.getFunction());
    request.setAttribute("FilterLib", crmSC.getFilterLib());
    request.setAttribute("FilterId", crmSC.getFilterId());
    request.setAttribute("participantEmail", participantEmail);
    request.setAttribute("participantActif", participantActif);
  }

  /**
   * Extract the container context from the request and save it in the session controller. If this
   * context is null then get the last one from the session controller. So the containerContext is
   * the same in the request and the session.
   * @param crmSC
   * @param request
   */
  private void resetContainerContext(CrmSessionController crmSC, HttpServletRequest request) {
    ContainerContext containerContext = (ContainerContext) request.getAttribute("ContainerContext");
    if (containerContext != null) {
      crmSC.setContainerContext(containerContext);
    } else {
      containerContext = crmSC.getContainerContext();
      request.setAttribute("ContainerContext", containerContext);
    }
  }

  private void resetReturnURL(CrmSessionController crmSC, HttpServletRequest request) {
    String returnURL = (String) request.getParameter("ReturnURL");
    if (StringUtil.isDefined(returnURL)) {
      crmSC.setReturnURL(returnURL);
    } else {
      returnURL = crmSC.getReturnURL();
    }
    request.setAttribute("ReturnURL", returnURL);
  }

  private String param(HttpServletRequest request, String name) {
    String result = request.getParameter(name);
    return (result != null ? result : "");
  }

  private void setAdminAttribute(CrmSessionController crmSC, HttpServletRequest request) {
    String flag = getFlag(crmSC.getUserRoles());
    if (flag.equals("publisher") || flag.equals("admin")) {
      request.setAttribute("admin", "true");
    }
  }

}
