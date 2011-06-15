package com.silverpeas.crm.servlets;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.crm.control.CrmSessionController;
import com.silverpeas.crm.model.Crm;
import com.silverpeas.crm.model.CrmContact;
import com.silverpeas.crm.model.CrmDelivery;
import com.silverpeas.crm.model.CrmEvent;
import com.silverpeas.crm.model.CrmParticipant;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.containerManager.ContainerContext;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

public class CrmRequestRouter extends ComponentRequestRouter {

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
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSC, ComponentContext componentContext) {
    return new CrmSessionController(mainSC, componentContext);
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param componentSC The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function, ComponentSessionController componentSC,
      HttpServletRequest request) {
    String destination = "";
    CrmSessionController crmSC = (CrmSessionController) componentSC;
    SilverTrace.info("crm", "CrmRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE",
      "User=" + componentSC.getUserId() + " Function=" + function);

    try {
      resetContainerContext(crmSC, request);
      resetReturnURL(crmSC, request);

      if (function.equals("Main")) {
        destination = setJournalContext(crmSC, request);
      } else if (function.equals("ViewClient")) {
        destination = setClientContext(crmSC, request);
      } else if (function.equals("ViewProject")) {
        destination = setProjectContext(crmSC, request);
      } else if (function.equals("ViewDelivrable")) {
        destination = setDeliveryContext(crmSC, request);
      } else if (function.equals("ViewJournal")) {
        destination = setJournalContext(crmSC, request);
      } else if (function.equals("NewEvent")) {
        String eventId = param(request, "eventId");
        setNewEventContext(eventId, crmSC, request);
        destination = "/crm/jsp/newEvent.jsp";
      } else if (function.equals("ChangeEvent")) {
        String eventId = param(request, "eventId");
        String userName = crmSC.getFilterLib();
        String userId = param(request, "FilterId");
        String eventLib = param(request, "eventLib");
        String eventDate = crmSC.getDate(param(request, "eventDate"));
        String actionTodo = param(request, "actionTodo");
        String actionDate = crmSC.getDate(param(request, "actionDate"));
        String state = request.getParameter("eventState");
        String sort = param(request, "ArrayPaneAction");
        if (!sort.equals("Sort")) {
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
      } else if (function.equals("CallUserPanelEvent")) {
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
      } else if (function.equals("ReturnFromUserPanelEvent")) {
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
      } else if (function.equals("DeleteEvent")) {
        String eventId = request.getParameter("eventId");
        if (eventId != null) {
          IdPK eventPK = new IdPK();
          eventPK.setId(eventId);
          crmSC.deleteCrmEvent(eventPK);
        }
        destination = setJournalContext(crmSC, request);
      } else if (function.equals("NewDelivery")) {
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
        if (!sort.equals("Sort")) {
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
      } else if (function.equals("CallUserPanelDelivery")) {
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
      } else if (function.equals("ReturnFromUserPanelDelivery")) {
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
      } else if (function.equals("DeleteDelivery")) {
        String deliveryId = request.getParameter("deliveryId");

        if (deliveryId != null) {
          IdPK deliveryPK = new IdPK();
          deliveryPK.setId(deliveryId);
          crmSC.deleteCrmDelivery(deliveryPK);
        }
        destination = setDeliveryContext(crmSC, request);
      } else if (function.equals("NewContact")) {
        String contactId = param(request, "contactId");
        setNewContactContext(contactId, crmSC, request);
        destination = "/crm/jsp/newContact.jsp";
      } else if (function.equals("ChangeContact")) {
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
        if (!sort.equals("Sort")) {
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
      } else if (function.equals("DeleteContact")) {
        String contactId = request.getParameter("contactId");

        if (contactId != null) {
          // 1. suppression des Delivrables liés au contact
          ArrayList<CrmDelivery> deliveries = crmSC.getCrmDeliverys(crmSC.getCurrentCrm().getPK());
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
      GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL"));

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
