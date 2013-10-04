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
package com.stratelia.webactiv.yellowpages.servlets;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.contact.model.ContactDetail;
import com.stratelia.webactiv.util.contact.model.ContactFatherDetail;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.yellowpages.control.YellowpagesSessionController;
import com.stratelia.webactiv.yellowpages.model.GroupDetail;
import com.stratelia.webactiv.yellowpages.model.TopicDetail;
import com.stratelia.webactiv.yellowpages.model.UserCompleteContact;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;

public class YellowpagesRequestRouter extends ComponentRequestRouter<YellowpagesSessionController> {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  @Override
  public YellowpagesSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new YellowpagesSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  @Override
  public String getSessionControlBeanName() {
    return "yellowpagesScc";
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param scc The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, YellowpagesSessionController scc,
      HttpServletRequest request) {
    SilverTrace.info("yellowpages", "YellowpagesRequestRooter.getDestination()",
        "root.MSG_GEN_ENTER_METHOD");
    SilverTrace.info("yellowpages", "YellowpagesRequestRooter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "function = " + function);

    String destination = "";
    String rootDestination = "/yellowpages/jsp/";

    try {

      // the flag is the best user's profile
      String flag = scc.getProfile();
      request.setAttribute("Profile", flag);
      if (function.startsWith("Main")) {
        scc.setPortletMode(false);
        scc.resetCurrentTypeSearchCriteria();
        destination = getDestination("GoTo", scc, request);
      } else if (function.equals("GoTo")) {
        String id = request.getParameter("Id");
        String action = request.getParameter("Action");

        TopicDetail currentTopic;
        Collection<ContactFatherDetail> contacts;
        if (id == null || (id != null && !id.startsWith("group_"))) {
          String rootId = "0";
          if (id == null) {
            currentTopic = scc.getCurrentTopic();
            if (currentTopic != null) {
              id = currentTopic.getNodePK().getId();
              if (id.equals("1")) {
                id = rootId;
                currentTopic = scc.getTopic(id);
                scc.setCurrentTopic(currentTopic);
              }
            } else {
              id = rootId;
              currentTopic = scc.getTopic(id);
              scc.setCurrentTopic(currentTopic);
            }

            contacts = scc.getAllContactDetails(currentTopic.getNodePK());
            scc.resetCurrentTypeSearchCriteria();
          } else {// id != null
            currentTopic = scc.getTopic(id);
            scc.setCurrentTopic(currentTopic);

            if (id.equals("0") && action == null) {// racine : affiche les
              // contacts courants
              contacts = scc.getCurrentContacts();
              request.setAttribute("TypeSearch", scc.getCurrentTypeSearch());
              request.setAttribute("SearchCriteria", scc
                  .getCurrentSearchCriteria());
            } else {// réinitialise la liste
              contacts = scc.getAllContactDetails(currentTopic.getNodePK());
              scc.resetCurrentTypeSearchCriteria();
            }
          }
          request.setAttribute("CurrentTopic", currentTopic);

        } else {
          id = id.substring(id.indexOf("_") + 1, id.length()); // remove
          // "group_"
          GroupDetail group = scc.getGroup(id);

          request.setAttribute("Group", group);

          contacts = scc.getAllUsersOfGroup(id);
          scc.resetCurrentTypeSearchCriteria();
        }

        request.setAttribute("Contacts", contacts);
        request.setAttribute("PortletMode", scc.isPortletMode());

        scc.setCurrentContacts(contacts);

        destination = "/yellowpages/jsp/annuaire.jsp?Action=GoTo&Profile="
            + flag;
      } else if (function.startsWith("portlet")) {
        scc.setPortletMode(true);
        destination = getDestination("GoTo", scc, request);
      } else if (function.startsWith("annuaire")) {
        destination = "/yellowpages/jsp/annuaire.jsp?Profile=" + flag;
      } else if (function.startsWith("topicManager")) {
        scc.clearGroupPath();
        destination = "/yellowpages/jsp/topicManager.jsp?Profile=" + flag;
      } else if (function.equals("GoToGroup")) {
        String id = request.getParameter("Id");
        id = id.substring(id.indexOf("_") + 1, id.length()); // remove "group_"

        GroupDetail group = scc.getGroup(id);

        request.setAttribute("Group", group);
        request.setAttribute("GroupPath", scc.getGroupPath());
        destination = "/yellowpages/jsp/groupManager.jsp";
      } else if (function.equals("RemoveGroup")) {
        String id = request.getParameter("ToDeleteId");
        id = id.substring(id.indexOf("_") + 1, id.length()); // remove "group_"

        scc.removeGroup(id);

        destination = getDestination("topicManager", scc, request);
      } else if (function.equals("ViewUserFull")) {
        String id = request.getParameter("Id");

        UserFull theUser = scc.getOrganisationController().getUserFull(id);

        request.setAttribute("UserFull", theUser);
        destination = "/yellowpages/jsp/userFull.jsp";
      } else if (function.startsWith("searchResult")) {
        scc.setPortletMode(false);

        String id = request.getParameter("Id");
        String type = request.getParameter("Type");

        if (type.equals("Contact")) { // un contact peut-être dans plusieurs
          // noeuds de l'annuaire
          TopicDetail currentTopic = scc.getTopic("0");
          scc.setCurrentTopic(currentTopic);

          ContactDetail contactDetail = scc.getContactDetail(id);

          List<ContactDetail> listContact = new ArrayList<ContactDetail>();
          listContact.add(contactDetail);

          request.setAttribute("Contacts", scc.getListContactFather(
              listContact, true));
          request.setAttribute("CurrentTopic", currentTopic);
          request.setAttribute("PortletMode", scc.isPortletMode());

          destination = "/yellowpages/jsp/annuaire.jsp?Action=SearchResults&Profile="
              + flag;
        } else if (type.equals("Node")) {
          destination = getDestination("GoTo", scc, request);
        }
      } else if (function.equals("Search")) {
        TopicDetail currentTopic = scc.getTopic("0");
        scc.setCurrentTopic(currentTopic);

        String typeSearch = request.getParameter("Action"); // All || LastName
        // || FirstName ||
        // LastNameFirstName
        String searchCriteria = request.getParameter("SearchCriteria");

        scc.setCurrentTypeSearch(typeSearch);
        scc.setCurrentSearchCriteria(searchCriteria);

        List<ContactFatherDetail> searchResults = scc.search(typeSearch, searchCriteria);

        scc.setCurrentContacts(searchResults);

        request.setAttribute("Contacts", searchResults);
        request.setAttribute("CurrentTopic", currentTopic);
        request.setAttribute("PortletMode", scc.isPortletMode());
        request.setAttribute("TypeSearch", typeSearch);
        request.setAttribute("SearchCriteria", searchCriteria);

        destination = "/yellowpages/jsp/annuaire.jsp?Action=SearchResults&Profile="
            + flag;
      } else if ("ToAddFolder".equals(function)) {

        setAvailableForms(request, scc);
        destination = "/yellowpages/jsp/addTopic.jsp";

      } else if ("AddFolder".equals(function)) {

        String name = request.getParameter("Name");
        String description = request.getParameter("Description");
        String modelId = request.getParameter("FormId");

        NodeDetail topic = new NodeDetail("-1", name, description, null, null, null, "0", "X");
        topic.setModelId(modelId);

        scc.addSubTopic(topic);

        destination = getDestination("topicManager", scc, request);

      } else if ("ToUpdateFolder".equals(function)) {

        String id = request.getParameter("Id");

        request.setAttribute("Node", scc.getSubTopicDetail(id));
        setAvailableForms(request, scc);

        destination = "/yellowpages/jsp/addTopic.jsp";
      } else if ("UpdateFolder".equals(function)) {

        String id = request.getParameter("TopicId");
        String name = request.getParameter("Name");
        String description = request.getParameter("Description");
        String modelId = request.getParameter("FormId");

        NodeDetail topic = new NodeDetail(id, name, description, null, null, null, "0", "X");
        topic.setModelId(modelId);

        scc.updateTopicHeader(topic);

        destination = getDestination("topicManager", scc, request);
      } else if ("DeleteFolder".equals(function)) {
        String id = request.getParameter("ToDeleteId");
        scc.deleteTopic(id);
        destination = getDestination("topicManager", scc, request);
      } else if (function.equals("PrintList")) {
        Collection<ContactFatherDetail> contacts = scc.getCurrentContacts();
        TopicDetail currentTopic = scc.getCurrentTopic();

        request.setAttribute("Contacts", contacts);
        request.setAttribute("CurrentTopic", currentTopic);

        destination = "/yellowpages/jsp/printContactList.jsp";
      } else if (function.startsWith("Contact")) {
        request.setAttribute("Profile", flag);
        destination = manageContact(function, request, scc);
      } else if (function.startsWith("http")) {
        destination = function;
      } else if (function.equals("selectUser")) {
        // initialisation du userPanel avec les participants
        destination = scc.initUserPanel();
      } else if (function.startsWith("saveUser")) {
        // retour du userPanel
        scc.setContactUserSelected();
        request.setAttribute("Profile", flag);
        destination = manageContact("ContactNewFromUser", request, scc);
      } else if (function.equals("ToChooseGroup")) {
        destination = scc.initGroupPanel();
      } else if (function.equals("AddGroup")) {
        // retour du userPanel
        scc.setGroupSelected();
        destination = getDestination("topicManager", scc, request);
      } else if (function.equals("ModelUsed")) {
        request.setAttribute("XMLForms", scc.getForms());

        Collection<String> modelUsed = scc.getModelUsed();
        request.setAttribute("ModelUsed", modelUsed);

        destination = rootDestination + "modelUsedList.jsp";
      } else if (function.equals("DeleteContact")) {
        // Delete contact
        String contactId = request.getParameter("ContactId");
        scc.deleteContact(contactId);

        // Back to topic
        destination = getDestination("topicManager", scc, request);
      } else if (function.equals("SelectModel")) {
        Object o = request.getParameterValues("modelChoice");
        if (o != null) {
          String[] models = (String[]) o;
          scc.addModelUsed(models);
        }
        destination = getDestination("topicManager", scc, request);
      } else if (function.equals("DeleteBasketContent")) {
        scc.deleteBasketContent();
        // Back to topic
        destination = getDestination("topicManager", scc, request);
      } else if ("ExportCSV".equals(function)) {
        String csvFilename = scc.exportAsCSV();
        request.setAttribute("CSVFilename", csvFilename);
        if (StringUtil.isDefined(csvFilename)) {
          File file = new File(FileRepositoryManager.getTemporaryPath() + csvFilename);
          request.setAttribute("CSVFileSize", Long.valueOf(file.length()));
          request.setAttribute("CSVFileURL", FileServerUtils.getUrlToTempDir(csvFilename));
        }
        return "/yellowpages/jsp/downloadCSV.jsp";
      } else if ("ToImportCSV".equals(function)) {
        destination = rootDestination + "importCSV.jsp";
      } else if ("ImportCSV".equals(function)) {
        List<FileItem> parameters = FileUploadUtil.parseRequest(request);
        FileItem fileItem = FileUploadUtil.getFile(parameters);
        String modelId = scc.getCurrentTopic().getNodeDetail().getModelId();
        request.setAttribute("Result", scc.importCSV(fileItem, modelId));
        destination = rootDestination + "importCSV.jsp";
      } else {
        destination = "/yellowpages/jsp/" + function;
      }
    } catch (Exception exce_all) {
      request.setAttribute("javax.servlet.jsp.jspException", exce_all);
      return "/admin/jsp/errorpageMain.jsp";
    }
    SilverTrace.info("yellowpages",
        "YellowpagesRequestRooter.getDestination()",
        "root.MSG_GEN_EXIT_METHOD", "destination = " + destination);
    return destination;
  }

  /**
   * Gets an instance of PublicationTemplateManager.
   *
   * @return an instance of PublicationTemplateManager.
   */
  private PublicationTemplateManager getPublicationTemplateManager() {
    return PublicationTemplateManager.getInstance();
  }

  private void setAvailableForms(HttpServletRequest request, YellowpagesSessionController ysc) {
    List<PublicationTemplate> listTemplates = new ArrayList<PublicationTemplate>();
    List<String> usedTemplates = new ArrayList<String>(ysc.getModelUsed());

    List<PublicationTemplate> allTemplates = ysc.getForms();
    for (PublicationTemplate xmlForm : allTemplates) {
      if (usedTemplates.contains(xmlForm.getFileName())) {
        listTemplates.add(xmlForm);
      }
    }
    request.setAttribute("XMLForms", listTemplates);
  }

  private String manageContact(String function, HttpServletRequest request,
      YellowpagesSessionController ysc) {
    if ("ContactView".equals(function)) {
      String contactId = request.getParameter("ContactId");
      String topicId = request.getParameter("TopicId");

      UserCompleteContact contact;
      if (StringUtil.isDefined(topicId)) {
        contact = ysc.getCompleteContactInNode(contactId, topicId);
      } else {
        topicId = ysc.getCurrentTopic().getNodePK().getId();
        contact = ysc.getCompleteContact(contactId);
      }
      ysc.setCurrentContact(contact);
      request.setAttribute("Contact", contact);
      request.setAttribute("TopicId", topicId);

      String modelId = ysc.getSubTopicDetail(topicId).getModelId();
      if (StringUtil.isDefined(modelId) && modelId.endsWith(".xml")) {
        setForm(contactId, modelId, true, request, ysc);
      }
      return "/yellowpages/jsp/contact.jsp";
    } else if ("ContactNew".equals(function)) {
      String modelId = ysc.getCurrentTopic().getNodeDetail().getModelId();
      if (StringUtil.isDefined(modelId) && modelId.endsWith(".xml")) {
        setForm(null, modelId, false, request, ysc);
      }
      return "/yellowpages/jsp/contactManager.jsp";
    } else if ("ContactNewFromUser".equals(function)) {
      request.setAttribute("Contact", ysc.getCurrentContact());
      String modelId = ysc.getCurrentTopic().getNodeDetail().getModelId();
      if (StringUtil.isDefined(modelId) && modelId.endsWith(".xml")) {
        setForm(null, modelId, false, request, ysc);
      }
      return "/yellowpages/jsp/contactManager.jsp";
    } else if ("ContactUpdate".equals(function)) {
      String contactId = request.getParameter("ContactId");
      String topicId = request.getParameter("TopicId");

      UserCompleteContact contact;
      if (StringUtil.isDefined(topicId)) {
        contact = ysc.getCompleteContactInNode(contactId, topicId);
      } else {
        topicId = ysc.getCurrentTopic().getNodePK().getId();
        contact = ysc.getCompleteContact(contactId);
      }
      ysc.setCurrentContact(contact);
      request.setAttribute("Contact", contact);

      String modelId = ysc.getSubTopicDetail(topicId).getModelId();
      if (StringUtil.isDefined(modelId) && modelId.endsWith(".xml")) {
        setForm(contactId, modelId, false, request, ysc);
      }

      return "/yellowpages/jsp/contactManager.jsp";
    } else if ("ContactSave".equals(function)) {
      List<FileItem> items = FileUploadUtil.parseRequest(request);
      String modelId = ysc.getCurrentTopic().getNodeDetail().getModelId();
      String contactId = FileUploadUtil.getParameter(items, "ContactId");
      if (StringUtil.isInteger(contactId)) {
        // update an existing contact
        ContactDetail contact = request2ContactDetail(items);
        contact.getPK().setId(contactId);
        ysc.updateContact(contact);
      } else {
        // create a new contact
        ContactDetail contact = request2ContactDetail(items);
        contactId = ysc.createContact(contact);
      }
      ysc.setCurrentContact(ysc.getCompleteContact(contactId));
      if (StringUtil.isDefined(modelId) && modelId.endsWith(".xml")) {
        saveForm(contactId, modelId, items, ysc);
        ysc.createInfoModel(contactId, modelId);
      }
      return getDestination("topicManager", ysc, request);
    } else if ("ContactSetFolders".equals(function)) {
      String listeTopics = request.getParameter("ListeTopics");
      String contactId = request.getParameter("ContactId");
      ysc.deleteContactFathers(contactId);
      String[] ids = StringUtil.splitByWholeSeparator(listeTopics, ",");
      for (String id : ids) {
        ysc.addContactToTopic(contactId, id);
      }
      return getDestination("topicManager", ysc, request);
    }

    return "";
  }

  private ContactDetail request2ContactDetail(List<FileItem> items) {
    String firstName = FileUploadUtil.getParameter(items, "FirstName");
    String lastName = FileUploadUtil.getParameter(items, "LastName");
    String email = FileUploadUtil.getParameter(items, "Email");
    String phone = FileUploadUtil.getParameter(items, "Phone");
    String fax = FileUploadUtil.getParameter(items, "Fax");
    String userId = FileUploadUtil.getParameter(items, "UserId");

    ContactDetail contact = new ContactDetail("X", firstName, lastName, email, phone, fax, null,
        null, null);
    if (StringUtil.isDefined(userId)) {
      contact.setUserId(userId);
    }

    return contact;
  }

  private void setForm(String contactId, String modelId, boolean view, HttpServletRequest request,
      YellowpagesSessionController ysc) {
    try {
      String xmlFormName = modelId;
      String xmlFormShortName = FilenameUtils.getBaseName(xmlFormName);
      // création du PublicationTemplate
      String key = ysc.getComponentId() + ":" + xmlFormShortName;
      PublicationTemplateManager templateManager = getPublicationTemplateManager();
      templateManager.addDynamicPublicationTemplate(key, xmlFormName);
      PublicationTemplateImpl pubTemplate = (PublicationTemplateImpl) templateManager.
          getPublicationTemplate(key, xmlFormName);

      // création du formulaire et du DataRecord
      Form form;
      if (view) {
        form = pubTemplate.getViewForm();
      } else {
        form = pubTemplate.getUpdateForm();
      }
      RecordSet recordSet = pubTemplate.getRecordSet();
      DataRecord data = recordSet.getRecord(contactId);
      if (data == null) {
        data = recordSet.getEmptyRecord();
        data.setId(contactId);// id contact
      }

      PagesContext context =
          new PagesContext("modelForm", "0", ysc.getLanguage(), false, ysc.getComponentId(),
          ysc.getUserId());
      context.setBorderPrinted(false);
      context.setObjectId(contactId);

      request.setAttribute("PagesContext", context);
      request.setAttribute("Form", form);
      request.setAttribute("Data", data);
    } catch (Exception e) {
      SilverTrace.
          error("yellowpages", getClass().getSimpleName() + ".setForm()", "root.NO_EX_MESSAGE", e);
    }
  }

  private void saveForm(String contactId, String modelId, List<FileItem> items,
      YellowpagesSessionController ysc) {
    try {
      String xmlFormName = modelId;
      String xmlFormShortName = FilenameUtils.getBaseName(xmlFormName);
      // création du PublicationTemplate
      String key = ysc.getComponentId() + ":" + xmlFormShortName;
      PublicationTemplateManager templateManager = getPublicationTemplateManager();
      templateManager.addDynamicPublicationTemplate(key, xmlFormName);
      PublicationTemplateImpl pubTemplate = (PublicationTemplateImpl) templateManager.
          getPublicationTemplate(key, xmlFormName);

      Form formUpdate = pubTemplate.getUpdateForm();
      RecordSet recordSet = pubTemplate.getRecordSet();
      DataRecord data = recordSet.getRecord(contactId);
      if (data == null) {
        data = recordSet.getEmptyRecord();
        data.setId(contactId);// id contact
      }

      // sauvegarde des données du formulaire
      PagesContext context = new PagesContext("modelForm", "0", ysc.getLanguage(), false, ysc.
          getComponentId(), ysc.getUserId());
      context.setObjectId(contactId);
      formUpdate.update(items, data, context);
      recordSet.save(data);
    } catch (Exception e) {
      SilverTrace.
          error("yellowpages", getClass().getSimpleName() + ".setForm()", "root.NO_EX_MESSAGE", e);
    }
  }
}
