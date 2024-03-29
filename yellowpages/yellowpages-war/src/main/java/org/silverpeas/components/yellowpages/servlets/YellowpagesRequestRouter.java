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
package org.silverpeas.components.yellowpages.servlets;

import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.web.export.ExportCSVBuilder;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.contact.model.CompleteContact;
import org.silverpeas.core.contact.model.ContactDetail;
import org.silverpeas.core.contact.model.ContactFatherDetail;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.components.yellowpages.control.YellowpagesSessionController;
import org.silverpeas.components.yellowpages.model.YellowPagesGroupDetail;
import org.silverpeas.components.yellowpages.model.TopicDetail;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.kernel.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class YellowpagesRequestRouter extends ComponentRequestRouter<YellowpagesSessionController> {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final YellowpagesActionAccessController actionAccessController =
      new YellowpagesActionAccessController();

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
   * @param function The entering request function (ex : "Main.jsp")
   * @param scc The component Session Control, build and initialised.
   * @param request
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, YellowpagesSessionController scc,
      HttpRequest request) {



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
      } else if ("GoTo".equals(function)) {
        String id = request.getParameter("Id");
        String action = request.getParameter("Action");

        TopicDetail currentTopic;
        Collection<ContactFatherDetail> contacts;
        if (id == null || (!id.startsWith("group_"))) {
          String rootId = "0";
          if (id == null) {
            currentTopic = scc.getCurrentTopic();
            if (currentTopic != null) {
              id = currentTopic.getNodePK().getId();
              if ("1".equals(id)) {
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
          } else {
            // id != null
            currentTopic = scc.getTopic(id);
            scc.setCurrentTopic(currentTopic);

            if ("0".equals(id) && action == null) {
              // racine : affiche les contacts courants
              contacts = scc.getCurrentContacts();
              request.setAttribute("SearchCriteria", scc.getCurrentSearchCriteria());
            } else {
              // réinitialise la liste
              contacts = scc.getAllContactDetails(currentTopic.getNodePK());
              scc.resetCurrentTypeSearchCriteria();
            }
          }
          request.setAttribute("CurrentTopic", currentTopic);

        } else {
          id = id.substring(id.indexOf("_") + 1, id.length());
          // remove "group_"
          YellowPagesGroupDetail group = scc.getGroup(id);

          request.setAttribute("Group", group);

          contacts = scc.getAllUsersOfGroup(id);
          scc.resetCurrentTypeSearchCriteria();
        }

        request.setAttribute("Contacts", contacts);
        setCommonAttributesOfMainPage(scc, request);

        scc.setCurrentContacts(contacts);

        destination = "/yellowpages/jsp/annuaire.jsp?Action=GoTo&Profile=" + flag;
      } else if (function.startsWith("portlet")) {
        scc.setPortletMode(true);
        destination = getDestination("GoTo", scc, request);
      } else if (function.startsWith("annuaire")) {
        destination = "/yellowpages/jsp/annuaire.jsp?Profile=" + flag;
      } else if (function.startsWith("topicManager")) {
        scc.clearGroupPath();
        destination = "/yellowpages/jsp/topicManager.jsp?Profile=" + flag;
      } else if ("GoToGroup".equals(function)) {
        String id = request.getParameter("Id");
        id = id.substring(id.indexOf("_") + 1, id.length());
        // remove "group_"
        YellowPagesGroupDetail group = scc.getGroup(id);
        request.setAttribute("Group", group);
        request.setAttribute("GroupPath", scc.getGroupPath());
        destination = "/yellowpages/jsp/groupManager.jsp";
      } else if ("RemoveGroup".equals(function)) {
        String id = request.getParameter("ToDeleteId");
        id = id.substring(id.indexOf("_") + 1, id.length());
        // remove "group_"
        scc.removeGroup(id);
        destination = getDestination("topicManager", scc, request);
      } else if ("ViewUserFull".equals(function)) {
        String id = request.getParameter("Id");

        UserFull theUser = scc.getOrganisationController().getUserFull(id);

        request.setAttribute("UserFull", theUser);
        destination = "/yellowpages/jsp/userFull.jsp";
      } else if (function.startsWith("searchResult")) {
        scc.setPortletMode(false);

        String id = request.getParameter("Id");
        String type = request.getParameter("Type");

        if ("Contact".equals(type)) {
          // un contact peut-être dans plusieurs noeuds de l'annuaire
          TopicDetail currentTopic = scc.getTopic("0");
          scc.setCurrentTopic(currentTopic);

          ContactDetail contactDetail = scc.getContactDetail(id);

          List<ContactDetail> listContact = new ArrayList<>();
          listContact.add(contactDetail);

          request.setAttribute("Contacts", scc.getListContactFather(listContact, true));
          request.setAttribute("CurrentTopic", currentTopic);
          setCommonAttributesOfMainPage(scc, request);

          destination = "/yellowpages/jsp/annuaire.jsp?Action=SearchResults&Profile=" + flag;
        } else if ("Node".equals(type)) {
          destination = getDestination("GoTo", scc, request);
        }
      } else if ("Search".equals(function)) {
        TopicDetail currentTopic = scc.getTopic("0");
        scc.setCurrentTopic(currentTopic);

        String searchCriteria = request.getParameter("SearchCriteria");

        List<ContactFatherDetail> searchResults = scc.search(searchCriteria);

        scc.setCurrentContacts(searchResults);

        request.setAttribute("Contacts", searchResults);
        request.setAttribute("CurrentTopic", currentTopic);
        request.setAttribute("SearchCriteria", searchCriteria);
        setCommonAttributesOfMainPage(scc, request);

        destination = "/yellowpages/jsp/annuaire.jsp?Action=SearchResults&Profile=" + flag;
      } else if ("ToAddFolder".equals(function)) {

        setAvailableForms(request, scc);
        destination = "/yellowpages/jsp/addTopic.jsp";

      } else if ("AddFolder".equals(function)) {

        String name = request.getParameter("Name");
        String description = request.getParameter("Description");
        String modelId = request.getParameter("FormId");

        NodeDetail topic = new NodeDetail("-1", name, description, 0, "X");
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

        NodeDetail topic = new NodeDetail(id, name, description, 0, "X");
        topic.setModelId(modelId);

        scc.updateTopicHeader(topic);

        destination = getDestination("topicManager", scc, request);
      } else if ("DeleteFolder".equals(function)) {
        String id = request.getParameter("ToDeleteId");
        scc.deleteTopic(id);
        destination = getDestination("topicManager", scc, request);
      } else if ("PrintList".equals(function)) {
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
      } else if ("selectUser".equals(function)) {
        // initialisation du userPanel avec les participants
        destination = scc.initUserPanel();
      } else if (function.startsWith("saveUser")) {
        // retour du userPanel
        boolean userSelected = scc.setContactUserSelected();
        if (userSelected) {
          request.setAttribute("Profile", flag);
          destination = manageContact("ContactNewFromUser", request, scc);
        } else {
          destination = getDestination("ContactNew", scc, request);
        }
      } else if ("ToChooseGroup".equals(function)) {
        destination = scc.initGroupPanel();
      } else if ("AddGroup".equals(function)) {
        // retour du userPanel
        scc.setGroupSelected();
        destination = getDestination("topicManager", scc, request);
      } else if ("ModelUsed".equals(function)) {
        request.setAttribute("XMLForms", scc.getForms());

        Collection<String> modelUsed = scc.getModelUsed();
        request.setAttribute("ModelUsed", modelUsed);

        destination = rootDestination + "modelUsedList.jsp";
      } else if ("DeleteContact".equals(function)) {
        // Delete contact
        String contactId = request.getParameter("ContactId");
        scc.deleteContact(contactId);

        // Back to topic
        destination = getDestination("topicManager", scc, request);
      } else if ("SelectModel".equals(function)) {
        scc.setModelUsed(request.getParameterValues("modelChoice"));
        destination = getDestination("topicManager", scc, request);
      } else if ("DeleteBasketContent".equals(function)) {
        scc.deleteBasketContent();
        // Back to topic
        destination = getDestination("topicManager", scc, request);
      } else if ("ExportCSV".equals(function)) {
        ExportCSVBuilder csvBuilder = scc.exportAsCSV();
        return csvBuilder.setupRequest(request);
      } else if ("ToImportCSV".equals(function)) {
        destination = rootDestination + "importCSV.jsp";
      } else if ("ImportCSV".equals(function)) {
        List<FileItem> parameters = request.getFileItems();
        FileItem fileItem = FileUploadUtil.getFile(parameters);
        request.setAttribute("Result", scc.importCSV(fileItem));
        destination = rootDestination + "importCSV.jsp";
      } else {
        destination = "/yellowpages/jsp/" + function;
      }
    } catch (Exception ex) {
      request.setAttribute("javax.servlet.jsp.jspException", ex);
      return "/admin/jsp/errorpageMain.jsp";
    }

    return destination;
  }

  @Override
  protected boolean checkUserAuthorization(final String function,
      final YellowpagesSessionController componentSC) {
    return actionAccessController
        .hasRightAccess(function, componentSC.getHighestSilverpeasUserRole());
  }

  private void setAvailableForms(HttpServletRequest request, YellowpagesSessionController ysc) {
    List<PublicationTemplate> listTemplates = new ArrayList<>();
    List<String> usedTemplates = new ArrayList<>(ysc.getModelUsed());

    List<PublicationTemplate> allTemplates = ysc.getForms();
    for (PublicationTemplate xmlForm : allTemplates) {
      if (usedTemplates.contains(xmlForm.getFileName())) {
        listTemplates.add(xmlForm);
      }
    }
    request.setAttribute("XMLForms", listTemplates);
  }

  private String manageContact(String function, HttpRequest request,
      YellowpagesSessionController ysc) {
    if ("ContactView".equals(function)) {
      String contactId = request.getParameter("ContactId");
      String topicId = request.getParameter("TopicId");

      CompleteContact contact;
      if (StringUtil.isDefined(topicId)) {
        contact = ysc.getCompleteContactInNode(contactId, topicId);
      } else {
        topicId = ysc.getCurrentTopic().getNodePK().getId();
        contact = ysc.getCompleteContact(contactId);
      }
      ysc.setCurrentContact(contact);
      request.setAttribute("Contact", contact);
      request.setAttribute("TopicId", topicId);

      setPageContext(contactId, request, ysc);

      return "/yellowpages/jsp/contact.jsp";
    } else if ("ContactExternalView".equals(function)) {
      // case of contacts displayed in global directory
      String contactId = request.getParameter("Id");
      CompleteContact contact = ysc.getCompleteContact(contactId);

      setPageContext(contactId, request, ysc);

      request.setAttribute("Contact", contact);
      return "/yellowpages/jsp/contact.jsp";
    } else if ("ContactNew".equals(function)) {
      String modelId = ysc.getCurrentModel();
      CompleteContact contact = new CompleteContact(ysc.getComponentId(), modelId);
      setPageContext(null, request, ysc);
      request.setAttribute("Contact", contact);
      return "/yellowpages/jsp/contactManager.jsp";
    } else if ("ContactNewFromUser".equals(function)) {
      String modelId = ysc.getCurrentModel();
      ysc.getCurrentContact().setModelId(modelId);
      setPageContext(null, request, ysc);
      request.setAttribute("Contact", ysc.getCurrentContact());
      return "/yellowpages/jsp/contactManager.jsp";
    } else if ("ContactUpdate".equals(function)) {
      String contactId = request.getParameter("ContactId");
      String topicId = request.getParameter("TopicId");

      CompleteContact contact;
      if (StringUtil.isDefined(topicId)) {
        contact = ysc.getCompleteContactInNode(contactId, topicId);
      } else {
        topicId = ysc.getCurrentTopic().getNodePK().getId();
        contact = ysc.getCompleteContact(contactId);
      }
      ysc.setCurrentContact(contact);
      request.setAttribute("Contact", contact);

      setPageContext(contactId, request, ysc);

      return "/yellowpages/jsp/contactManager.jsp";
    } else if ("ContactSave".equals(function)) {
      List<FileItem> items = request.getFileItems();
      String modelId = ysc.getCurrentModel();
      String contactId = FileUploadUtil.getParameter(items, "ContactId");
      ContactDetail contact = request2ContactDetail(items);
      CompleteContact fullContact = new CompleteContact(contact, modelId);
      fullContact.setFormItems(items);
      if (StringUtil.isInteger(contactId)) {
        // update an existing contact
        contact.getPK().setId(contactId);
        ysc.updateContact(fullContact);
      } else {
        // create a new contact
        contactId = ysc.createContact(fullContact);
      }

      ysc.setCurrentContact(ysc.getCompleteContact(contactId));

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

    ContactDetail contact =
        new ContactDetail("X", firstName, lastName, email, phone, fax, null, null, null);
    if (StringUtil.isDefined(userId)) {
      contact.setUserId(userId);
    }

    return contact;
  }

  private void setPageContext(String contactId, HttpRequest request,
      YellowpagesSessionController ysc) {
    PagesContext context =
        new PagesContext("modelForm", "0", ysc.getLanguage(), false, ysc.getComponentId(),
            ysc.getUserId());
    context.setBorderPrinted(false);
    context.setObjectId(contactId);
    request.setAttribute("PagesContext", context);

    request.setAttribute("FullPage", request.getParameterAsBoolean("FullPage"));
  }

  private void setCommonAttributesOfMainPage(YellowpagesSessionController scc,
      HttpServletRequest request) {
    request.setAttribute("PortletMode", scc.isPortletMode());
    request.setAttribute("Tree", scc.getTree());
  }
}
