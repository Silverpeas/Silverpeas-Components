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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.crm.control;

import com.silverpeas.crm.model.Crm;
import com.silverpeas.crm.model.CrmContact;
import com.silverpeas.crm.model.CrmDelivery;
import com.silverpeas.crm.model.CrmEvent;
import com.silverpeas.crm.model.CrmParticipant;
import com.silverpeas.crm.vo.ContactVO;
import com.silverpeas.crm.vo.DeliveryVO;
import com.silverpeas.crm.vo.EventVO;
import com.silverpeas.crm.vo.ParticipantVO;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.containerManager.ContainerContext;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CrmSessionController extends AbstractComponentSessionController {

  private ContainerContext containerContext;
  private String returnURL = "";

  private CrmDataManager dataManager = null;

  // Participant data
  private String participantId = null;
  private String participantEmail = null;
  private String participantActif = null;
  private String function = null;
  private String filterLib = null;
  private String filterId = null;

  // Event data
  private String eventId = null;
  private String eventState = null;
  private String eventLib = null;
  private String eventDate = null;
  private String actionDate = null;
  private String actionTodo = null;

  // Delivery data
  private String deliveryId = null;
  private String deliveryDate = null;
  private String deliveryElement = null;
  private String deliveryVersion = null;
  private int deliveryContact = 0;
  private String deliveryMedia = null;

  private String[] functions = null;
  private String[] eventStates = null;
  private String[] medias = null;

  private ResourcesWrapper resources = null;

  private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
  private DateFormat displayDateFormat = null;

  public CrmSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, "com.silverpeas.crm.multilang.crmBundle",
        "com.silverpeas.crm.settings.crmIcons");
    functions = CrmSettings.getFunction();
    eventStates = CrmSettings.getEventState();
    medias = CrmSettings.getMedia();
    dataManager = ServiceFactory.getCrmData();
  }

  public ResourcesWrapper getResouces() {
    if (resources == null) {
      resources = new ResourcesWrapper(getMultilang(), getIcon(), getSettings(), getLanguage());
    }
    return resources;
  }

  public void setContainerContext(ContainerContext containerContext) {
    this.containerContext = containerContext;
  }

  public ContainerContext getContainerContext() {
    return containerContext;
  }

  public void setReturnURL(String returnURL) {
    this.returnURL = returnURL;
  }

  public String getReturnURL() {
    return returnURL;
  }

  // Creation d'un crm
  public void createCrm(Crm crm) {
    dataManager.createCrm(crm);
    createIndex(crm);
  }

  // Suppression d'un crm
  public void deleteCrm(WAPrimaryKey pk) {
    deleteIndex(dataManager.getCrm(pk));
    dataManager.deleteCrm(pk);
  }

  // Mise a jour d'un crm
  public void updateCrm(Crm crm) {
    dataManager.updateCrm(crm);
    deleteIndex(crm);
    createIndex(crm);
  }

  // Recuperation de la liste des crm
  public List<Crm> getCrms() {
    return dataManager.listAllCrms(getComponentId());
  }

  // Recuperation de la liste des contacts
  public List<CrmContact> getCrmContacts(WAPrimaryKey crmPK) {
    return dataManager.listContactsOfCrm(crmPK);
  }

  // Creation d'un contact
  public void createCrmContact(CrmContact contact) {
    contact.setInstanceId(getComponentId());
    dataManager.createCrmContact(contact);
    createIndex(contact);
  }

  // Suppression d'un contact
  public void deleteCrmContact(WAPrimaryKey pk) {
    CrmContact contact = getCrmContact(pk);
    deleteIndex(contact);
    contact.deleteAttachments();
    dataManager.deleteCrmContact(pk, getComponentId());
  }

  // Mise a jour d'un contact
  public void updateCrmContact(CrmContact contact) {
    contact.setInstanceId(getComponentId());
    dataManager.updateCrmContact(contact);
    deleteIndex(contact);
    createIndex(contact);
  }

  // Recuperation d'un contact par sa clef
  public CrmContact getCrmContact(WAPrimaryKey contactPK) {
    return dataManager.getCrmContact(contactPK);
  }

  // Indexation d'un contact
  public void createIndex(CrmContact contact) {
    FullIndexEntry indexEntry = null;
    if (contact != null) {
      indexEntry = new FullIndexEntry(getComponentId(), "Contact", contact.getPK().getId());
      indexEntry.setTitle(contact.getName());
      indexEntry.setPreView(contact.getFunctionContact());
      indexEntry.setCreationUser(getUserId());
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  // Suppression de l'index d'un contact
  private void deleteIndex(CrmContact contact) {
    IndexEntryPK indexEntry =
        new IndexEntryPK(getComponentId(), "Contact", contact.getPK().getId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  // Indexation d'un crm
  public void createIndex(Crm crm) {
    FullIndexEntry indexEntry = null;
    if (crm != null) {
      indexEntry = new FullIndexEntry(getComponentId(), "crm", crm.getPK().getId());
      indexEntry.setTitle(crm.getClientName());
      indexEntry.setPreView(crm.getProjectCode());
      indexEntry.setCreationUser(getUserId());
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  // Suppression de l'index d'un crm
  private void deleteIndex(Crm crm) {
    IndexEntryPK indexEntry = new IndexEntryPK(getComponentId(), "crm", crm.getPK().getId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  // Recuperation de la liste des participants
  public List<CrmParticipant> getCrmParticipants(WAPrimaryKey crmPK) {
    return dataManager.listCrmParticipantsOfCrm(crmPK);
  }

  // Creation d'un participant
  public void createCrmParticipant(CrmParticipant participant) {
    participant.setInstanceId(getComponentId());
    dataManager.createCrmParticipant(participant);
    createIndex(participant);
  }

  // Suppression d'un participant
  public void deleteCrmParticipant(WAPrimaryKey pk) {
    CrmParticipant participant = getCrmParticipant(pk);
    deleteIndex(participant);
    participant.deleteAttachments();
    dataManager.deleteCrmParticipant(pk, getComponentId());
  }

  // Mise a jour d'un participant
  public void updateCrmParticipant(CrmParticipant participant) {
    participant.setInstanceId(getComponentId());
    dataManager.updateCrmParticipant(participant);
    deleteIndex(participant);
    createIndex(participant);
  }

  // Recuperation d'un participant par sa clef
  public CrmParticipant getCrmParticipant(WAPrimaryKey participantPK) {
    return dataManager.getCrmParticipant(participantPK);
  }

  // Indexation d'un participant
  public void createIndex(CrmParticipant participant) {
    FullIndexEntry indexEntry = null;
    if (participant != null) {
      indexEntry = new FullIndexEntry(getComponentId(), "Participant", participant.getPK().getId());
      indexEntry.setTitle(participant.getUserName());
      indexEntry.setPreView(getLibFunction(participant.getFunctionParticipant()));
      indexEntry.setCreationUser(getUserId());
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  // Suppression de l'index d'un participant
  private void deleteIndex(CrmParticipant participant) {
    IndexEntryPK indexEntry = new IndexEntryPK(
        getComponentId(), "Participant", participant.getPK().getId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  // Recuperation de la liste des events
  public List<CrmEvent> getCrmEvents(WAPrimaryKey crmPK) {
    return dataManager.listEventsOfCrm(crmPK);
  }

  public List<EventVO> getEventVOs() {
    List<EventVO> eventVOs = new ArrayList<EventVO>();
    List<CrmEvent> events = getCrmEvents(getCurrentCrm().getPK());
    for (CrmEvent event : events) {
      event.setState(getLibState(event.getState()));
      eventVOs.add(new EventVO(event, getResouces()));
    }
    return eventVOs;
  }

  public List<ContactVO> getContactVOs() {
    List<ContactVO> contactVOs = new ArrayList<ContactVO>();
    List<CrmContact> contacts = getCrmContacts(getCurrentCrm().getPK());
    for (CrmContact contact : contacts) {
      contactVOs.add(new ContactVO(contact, getResouces()));
    }
    return contactVOs;
  }

  public List<ParticipantVO> getParticipantVOs() {
    List<ParticipantVO> participantVOs = new ArrayList<ParticipantVO>();
    List<CrmParticipant> participants = getCrmParticipants(getCurrentCrm().getPK());
    for (CrmParticipant participant : participants) {
      participant.setFunctionParticipant(getLibFunction(participant.getFunctionParticipant()));
      participantVOs.add(new ParticipantVO(participant, getResouces()));
    }
    return participantVOs;
  }

  public List<DeliveryVO> getDeliveryVOs() {
    List<DeliveryVO> deliveryVOs = new ArrayList<DeliveryVO>();
    WAPrimaryKey crmPK = getCurrentCrm().getPK();
    List<CrmDelivery> deliverys = getCrmDeliverys(crmPK);
    for (CrmDelivery delivery : deliverys) {
      delivery.setMedia(getLibMedia(delivery.getMedia()));
      delivery.setContactName(getContactName(crmPK, delivery.getContactId()));
      deliveryVOs.add(new DeliveryVO(delivery, getResouces()));
    }
    return deliveryVOs;
  }

  // Creation d'un event
  public void createCrmEvent(CrmEvent event) {
    event.setInstanceId(getComponentId());
    dataManager.createCrmEvent(event);
    createIndex(event);
  }

  // Suppression d'un event
  public void deleteCrmEvent(WAPrimaryKey pk) {
    CrmEvent event = getCrmEvent(pk);
    deleteIndex(event);
    event.deleteAttachments();
    dataManager.deleteCrmEvent(pk, getComponentId());
  }

  // Mise a jour d'un event
  public void updateCrmEvent(CrmEvent event) {
    event.setInstanceId(getComponentId());
    dataManager.updateCrmEvent(event);
    deleteIndex(event);
    createIndex(event);
  }

  // Recuperation d'un event par sa clef
  public CrmEvent getCrmEvent(WAPrimaryKey eventPK) {
    return dataManager.getCrmEvent(eventPK);
  }

  // Indexation d'un event
  public void createIndex(CrmEvent event) {
    FullIndexEntry indexEntry = null;
    if (event != null) {
      indexEntry = new FullIndexEntry(getComponentId(), "Event", event.getPK().getId());
      indexEntry.setTitle(event.getEventLib());
      indexEntry.setPreView(event.getActionTodo());
      indexEntry.setCreationUser(getUserId());
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  // Suppression de l'index d'un event
  private void deleteIndex(CrmEvent event) {
    IndexEntryPK indexEntry = new IndexEntryPK(getComponentId(), "Event", event.getPK().getId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  // Recuperation de la liste des deliverys
  public List<CrmDelivery> getCrmDeliverys(WAPrimaryKey crmPK) {
    return dataManager.listDeliveriesOfCrm(crmPK);
  }

  // Creation d'un delivery
  public void createCrmDelivery(CrmDelivery delivery) {
    delivery.setInstanceId(getComponentId());
    dataManager.createCrmDelivery(delivery);
    createIndex(delivery);
  }

  // Suppression d'un delivery
  public void deleteCrmDelivery(WAPrimaryKey pk) {
    CrmDelivery delivery = getCrmDelivery(pk);
    deleteIndex(delivery);
    delivery.deleteAttachments();
    dataManager.deleteCrmDelivery(pk, getComponentId());
  }

  // Mise a jour d'un delivery
  public void updateCrmDelivery(CrmDelivery delivery) {
    delivery.setInstanceId(getComponentId());
    dataManager.updateCrmDelivery(delivery);
    deleteIndex(delivery);
    createIndex(delivery);
  }

  // Recuperation d'un delivery par sa clef
  public CrmDelivery getCrmDelivery(WAPrimaryKey deliveryPK) {
    return dataManager.getCrmDelivery(deliveryPK);
  }

  // Indexation d'un delivery
  public void createIndex(CrmDelivery delivery) {
    FullIndexEntry indexEntry = null;
    if (delivery != null) {
      indexEntry = new FullIndexEntry(getComponentId(), "Delivery", delivery.getPK().getId());
      indexEntry.setTitle(delivery.getElement());
      indexEntry.setPreView(delivery.getVersion());
      indexEntry.setCreationUser(getUserId());
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  // Suppression de l'index d'un delivery
  private void deleteIndex(CrmDelivery delivery) {
    IndexEntryPK indexEntry = new IndexEntryPK(
        getComponentId(), "Delivery", delivery.getPK().getId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  protected Selection communInitUserPanel(String compoName, String operation) {
    String m_context =
        GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
    String hostSpaceName = getSpaceLabel();
    PairObject hostComponentName = new PairObject(getComponentLabel(),
        m_context + getComponentUrl() + compoName);
    String hostUrl = m_context + getComponentUrl() + operation;

    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName(hostSpaceName);
    sel.setHostComponentName(hostComponentName);
    sel.setHostPath(null);

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(hostUrl);

    sel.setMultiSelect(false);
    sel.setPopupMode(true);

    return sel;
  }

  /*
   * Initialisation du UserPanel
   */
  public String initUserPanelParticipant() {
    Selection sel = communInitUserPanel("NewParticipant", "ReturnFromUserPanelParticipant");
    sel.setSetSelectable(false);
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  public String initUserPanelEvent() {
    Selection sel = communInitUserPanel("NewEvent", "ReturnFromUserPanelEvent");
    sel.setSetSelectable(false);
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  public String initUserPanelDelivery() {
    Selection sel = communInitUserPanel("NewEvent", "ReturnFromUserPanelDelivery");
    sel.setSetSelectable(false);
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  /*
   * Retour du UserPanel
   */
  public void userPanelReturn() {
    Selection sel = getSelection();
    String user = sel.getFirstSelectedElement();

    if (user != null && user.length() != 0) {
      setFilterId(user);
      setFilterLib(getOrganizationController().getUserDetail(user).getLastName());
      setParticipantEmail(getOrganizationController().getUserDetail(user).geteMail());
    }

    SilverTrace.debug("crm", "CrmSessionController.userPanelReturn()",
        "filterId=" + filterId + " ; filterLib=" + filterLib);
  }

  public void setParticipantId(String participantId) {
    this.participantId = participantId;
  }

  public void setParticipantEmail(String participantEmail) {
    this.participantEmail = participantEmail;
  }

  public void setParticipantActif(String participantActif) {
    this.participantActif = participantActif;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  public void setEventState(String eventState) {
    this.eventState = eventState;
  }

  public void setEventLib(String eventLib) {
    this.eventLib = eventLib;
  }

  public void setEventDate(String eventDate) {
    this.eventDate = eventDate;
  }

  public void setActionDate(String actionDate) {
    this.actionDate = actionDate;
  }

  public void setActionTodo(String actionTodo) {
    this.actionTodo = actionTodo;
  }

  public void setFilterLib(String filterLib) {
    this.filterLib = filterLib;
  }

  public void setFilterId(String filterId) {
    this.filterId = filterId;
  }

  public void setFunction(String function) {
    this.function = function;
  }

  public String getFilterLib() {
    return filterLib;
  }

  public String getFilterId() {
    return filterId;
  }

  public String getFunction() {
    return function;
  }

  public String getParticipantId() {
    return participantId;
  }

  public String getParticipantEmail() {
    return participantEmail;
  }

  public String getParticipantActif() {
    return participantActif;
  }

  public String getEventId() {
    return eventId;
  }

  public String getEventState() {
    return eventState;
  }

  public String getEventLib() {
    return eventLib;
  }

  public String getEventDate() {
    return eventDate;
  }

  public String getActionDate() {
    return actionDate;
  }

  public String getActionTodo() {
    return actionTodo;
  }

  public void setDeliveryId(String deliveryId) {
    this.deliveryId = deliveryId;
  }

  public void setDeliveryDate(String deliveryDate) {
    this.deliveryDate = deliveryDate;
  }

  public void setDeliveryElement(String deliveryElement) {
    this.deliveryElement = deliveryElement;
  }

  public void setDeliveryVersion(String deliveryVersion) {
    this.deliveryVersion = deliveryVersion;
  }

  public void setDeliveryContact(int deliveryContact) {
    this.deliveryContact = deliveryContact;
  }

  public void setDeliveryMedia(String deliveryMedia) {
    this.deliveryMedia = deliveryMedia;
  }

  public String getDeliveryId() {
    return deliveryId;
  }

  public String getDeliveryDate() {
    return deliveryDate;
  }

  public String getDeliveryElement() {
    return deliveryElement;
  }

  public String getDeliveryVersion() {
    return deliveryVersion;
  }

  public int getDeliveryContact() {
    return deliveryContact;
  }

  public String getDeliveryMedia() {
    return deliveryMedia;
  }

  public String getContactName(WAPrimaryKey crmPK, int contactId) {
    List<CrmContact> crmContacts = getCrmContacts(crmPK);
    for (CrmContact crmContact : crmContacts) {
      if (crmContact.getPK().getId().equals(String.valueOf(contactId))) {
        return crmContact.getName();
      }
    }
    return "";
  }

  public String getLibMedia(String media) {
    return getValue(media, medias);
  }

  public String getLibState(String eventState) {
    return getValue(eventState, eventStates);
  }

  public String getLibFunction(String participantFunction) {
    return getValue(participantFunction, functions);
  }

  public Collection<String[]> getFunctions() {
    return getComboValues(functions);
  }

  public Collection<String[]> getStates() {
    return getComboValues(eventStates);
  }

  public Collection<String[]> getMedias() {
    return getComboValues(medias);
  }

  private Collection<String[]> getComboValues(String[] values) {
    List<String[]> comboValues = new ArrayList<String[]>();
    for (int i = 0; i < values.length; i++) {
      comboValues.add(new String[] { String.valueOf(i + 1), values[i] });
    }
    return comboValues;
  }

  public String getValue(String index, String[] tabContents) {
    return tabContents[Integer.parseInt(index) - 1];
  }

  public Collection<String[]> getContacts() {
    List<String[]> crmList = new ArrayList<String[]>();
    List<CrmContact> crmContacts = getCrmContacts(getCurrentCrm().getPK());
    for (CrmContact crmContact : crmContacts) {
      crmList.add(new String[] {
          crmContact.getPK().getId(),
          crmContact.getName() + " - " + crmContact.getFunctionContact() });
    }
    return crmList;
  }

  /**
   * @param date The date corresponding to the user's language (dd/mm/yyyy or mm/dd/yyyy)
   * @return The equivalent yyyy/mm/dd date
   */
  public String getDate(String date) {
    if (StringUtil.isDefined(date)) {
      try {
        return dateFormat.format(getDisplayDateFormat().parse(date));
      } catch (ParseException e) {
        // do nothing
      }
    }
    return "";
  }

  /**
   * @param date a yyyy/mm/dd date
   * @return The formatted date corresponding to the user's language (dd/mm/yyyy or mm/dd/yyyy)
   */
  public String getDisplayDate(String date) {
    if (StringUtil.isDefined(date)) {
      try {
        return getDisplayDateFormat().format(dateFormat.parse(date));
      } catch (ParseException e) {
        // do nothing
      }
    }
    return "";
  }

  private DateFormat getDisplayDateFormat() {
    if (displayDateFormat == null) {
      displayDateFormat = new SimpleDateFormat(getString("GML.dateFormat"));
    }
    return displayDateFormat;
  }

  public Crm getCurrentCrm() {
    return getCrms().get(0);
  }

}
