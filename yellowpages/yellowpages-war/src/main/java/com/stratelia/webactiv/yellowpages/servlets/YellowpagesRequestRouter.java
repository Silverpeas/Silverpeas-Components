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
package com.stratelia.webactiv.yellowpages.servlets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;

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
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.util.contact.model.ContactDetail;
import com.stratelia.webactiv.util.contact.model.ContactFatherDetail;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.yellowpages.control.YellowpagesSessionController;
import com.stratelia.webactiv.yellowpages.model.GroupDetail;
import com.stratelia.webactiv.yellowpages.model.TopicDetail;

public class YellowpagesRequestRouter extends ComponentRequestRouter {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    ComponentSessionController component =
        (ComponentSessionController) new YellowpagesSessionController(
            mainSessionCtrl, componentContext);
    return component;
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "yellowpagesScc";
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param componentSC The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request) {
    SilverTrace.info("yellowpages",
        "YellowpagesRequestRooter.getDestination()",
        "root.MSG_GEN_ENTER_METHOD");
    SilverTrace.info("yellowpages",
        "YellowpagesRequestRooter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "function = " + function);
    YellowpagesSessionController scc = (YellowpagesSessionController) componentSC;

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

        TopicDetail currentTopic = null;
        Collection<ContactFatherDetail> contacts = null;
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
        request.setAttribute("PortletMode", new Boolean(scc.isPortletMode()));

        scc.setCurrentContacts(contacts);

        destination = "/yellowpages/jsp/annuaire.jsp?Action=GoTo&Profile="
            + flag;
      }

      else if (function.startsWith("portlet")) {
        scc.setPortletMode(true);
        destination = getDestination("GoTo", scc, request);
      }

      else if (function.startsWith("annuaire")) {
        destination = "/yellowpages/jsp/annuaire.jsp?Profile=" + flag;
      }

      else if (function.startsWith("topicManager")) {
        scc.clearGroupPath();
        destination = "/yellowpages/jsp/topicManager.jsp?Profile=" + flag;
      }

      else if (function.equals("GoToGroup")) {
        String id = request.getParameter("Id");
        id = id.substring(id.indexOf("_") + 1, id.length()); // remove "group_"

        GroupDetail group = scc.getGroup(id);

        request.setAttribute("Group", group);
        request.setAttribute("GroupPath", scc.getGroupPath());
        destination = "/yellowpages/jsp/groupManager.jsp";
      } else if (function.equals("RemoveGroup")) {
        String id = request.getParameter("Id");
        id = id.substring(id.indexOf("_") + 1, id.length()); // remove "group_"

        scc.removeGroup(id);

        destination = getDestination("topicManager", scc, request);
      }

      else if (function.equals("ViewUserFull")) {
        String id = request.getParameter("Id");

        UserFull user = scc.getOrganizationController().getUserFull(id);

        request.setAttribute("UserFull", user);
        destination = "/yellowpages/jsp/userFull.jsp";
      }

      else if (function.startsWith("searchResult")) {
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
          request.setAttribute("PortletMode", new Boolean(scc.isPortletMode()));

          destination = "/yellowpages/jsp/annuaire.jsp?Action=SearchResults&Profile="
              + flag;
        } else if (type.equals("Node")) {
          destination = getDestination("GoTo", scc, request);
        }
      }

      else if (function.equals("Search")) {
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
        request.setAttribute("PortletMode", new Boolean(scc.isPortletMode()));
        request.setAttribute("TypeSearch", typeSearch);
        request.setAttribute("SearchCriteria", searchCriteria);

        destination = "/yellowpages/jsp/annuaire.jsp?Action=SearchResults&Profile="
            + flag;
      }

      else if (function.equals("PrintList")) {
        Collection<ContactFatherDetail> contacts = scc.getCurrentContacts();
        TopicDetail currentTopic = scc.getCurrentTopic();

        request.setAttribute("Contacts", contacts);
        request.setAttribute("CurrentTopic", currentTopic);

        destination = "/yellowpages/jsp/printContactList.jsp";
      }

      else if (function.startsWith("contactManager")) {

        String action = (String) request.getParameter("Action");
        String contactId = (String) request.getParameter("ContactId");
        String topicId = (String) request.getParameter("TopicId");

        if ("ViewContactInTopic".equals(action)) {
          request.setAttribute("TopicId", topicId);
          String modelId = scc.getTopic(topicId).getNodeDetail().getModelId();

          if (StringUtil.isDefined(modelId) && modelId.endsWith(".xml")) {
            String xmlFormName = modelId;
            String xmlFormShortName = xmlFormName.substring(xmlFormName
                .indexOf("/") + 1, xmlFormName.indexOf("."));
            // création du PublicationTemplate
            PublicationTemplateManager.addDynamicPublicationTemplate(scc
                .getComponentId()
                + ":" + xmlFormShortName, xmlFormName);
            PublicationTemplateImpl pubTemplate =
                (PublicationTemplateImpl) PublicationTemplateManager
                    .getPublicationTemplate(scc.getComponentId() + ":"
                    + xmlFormShortName, xmlFormName);

            // création du formulaire et du DataRecord
            Form formView = pubTemplate.getViewForm();
            RecordSet recordSet = pubTemplate.getRecordSet();
            DataRecord data = recordSet.getRecord(contactId);
            if (data == null) {
              data = recordSet.getEmptyRecord();
              data.setId(contactId);// id contact
            }

            // appel de la jsp avec les paramètres
            request.setAttribute("Form", formView);
            request.setAttribute("Data", data);

            PagesContext context = new PagesContext("modelForm", "0", scc
                .getLanguage(), false, scc.getComponentId(), scc.getUserId());
            context.setBorderPrinted(false);
            context.setObjectId(contactId);
            request.setAttribute("PagesContext", context);
          }
        }

        request.setAttribute("ContactId", contactId);
        request.setAttribute("Profile", flag);
        request.setAttribute("Action", action);

        destination = "/yellowpages/jsp/contactManager.jsp";
      }

      else if (function.startsWith("http")) {
        destination = function;
      }

      else if (function.startsWith("selectUser")) {
        // initialisation du userPanel avec les participants
        destination = scc.initUserPanel();
      }

      else if (function.startsWith("saveUser")) {
        // retour du userPanel
        scc.setContactUserSelected();
        request.setAttribute("Action", "SaveUser");
        destination = "/yellowpages/jsp/contactManager.jsp";
      }

      else if (function.equals("ToChooseGroup")) {
        destination = scc.initGroupPanel();
      } else if (function.equals("AddGroup")) {
        // retour du userPanel
        scc.setGroupSelected();
        destination = getDestination("topicManager", scc, request);
      } else if (function.equals("ModelUsed")) {
        try {
          List<PublicationTemplate> templates = PublicationTemplateManager.getPublicationTemplates();
          request.setAttribute("XMLForms", templates);
        } catch (Exception e) {
          SilverTrace.info("yellowPages",
              "YellowPagesRequestRouter.getDestination(ModelUsed)",
              "root.MSG_GEN_PARAM_VALUE", "", e);
        }

        Collection<String> modelUsed = scc.getModelUsed();
        request.setAttribute("ModelUsed", modelUsed);

        destination = rootDestination + "modelUsedList.jsp";
      } else if (function.equals("DeleteContact")) {
        // Delete contact
        String contactId = (String) request.getParameter("ContactId");
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
      } else if (function.startsWith("modelManager")) {
        // récupération des données saisies dans le formulaire
        List<FileItem> items = FileUploadUtil.parseRequest(request);

        String action = FileUploadUtil.getParameter(items, "Action", "");
        String contactId = FileUploadUtil.getParameter(items, "ContactId", "");
        String modelId = FileUploadUtil.getParameter(items, "ModelId", ""); // Id Node de
        // rubrique ou
        // Id de
        // contact

        if ("ModelChoice".equals(action)) {
          // List listTemplate =
          // PublicationTemplateManager.getPublicationTemplates();
          List<PublicationTemplate> listTemplates = new ArrayList<PublicationTemplate>();
          ArrayList<String> usedTemplates = new ArrayList<String>(scc.getModelUsed());
          try {
            List<PublicationTemplate> allTemplates = PublicationTemplateManager
                .getPublicationTemplates();
            PublicationTemplate xmlForm;
            Iterator<PublicationTemplate> iterator = allTemplates.iterator();
            while (iterator.hasNext()) {
              xmlForm = (PublicationTemplate) iterator.next();
              if (usedTemplates.contains(xmlForm.getFileName()))
                listTemplates.add(xmlForm);
            }
            request.setAttribute("XMLForms", listTemplates);
          } catch (Exception e) {
            SilverTrace.info("yellowpages",
                "YellowpagesRequestRouter.getDestination(modelManager)",
                "root.MSG_GEN_PARAM_VALUE", "", e);
          }

          if ((modelId == null || "".equals(modelId)) && contactId != null) {
            NodeDetail topic = scc.getSubTopicDetail(contactId);
            modelId = topic.getModelId();
          }

          if (modelId != null && "0".equals(modelId)) {
            modelId = null;
          }

          if (modelId != null && !"".equals(modelId)) {
            String xmlFormName = modelId;
            String xmlFormShortName = xmlFormName.substring(xmlFormName
                .indexOf("/") + 1, xmlFormName.indexOf("."));
            // création du PublicationTemplate
            PublicationTemplateManager.addDynamicPublicationTemplate(scc
                .getComponentId()
                + ":" + xmlFormShortName, xmlFormName);
            PublicationTemplateImpl pubTemplate =
                (PublicationTemplateImpl) PublicationTemplateManager
                    .getPublicationTemplate(scc.getComponentId() + ":"
                    + xmlFormShortName, xmlFormName);

            // création du formulaire et du DataRecord
            Form formUpdate = pubTemplate.getUpdateForm();
            RecordSet recordSet = pubTemplate.getRecordSet();
            DataRecord data = recordSet.getEmptyRecord();
            data.setId(contactId); // id Rubrique = id NodeDetail

            // appel de la jsp avec les paramètres
            request.setAttribute("Form", formUpdate);
            request.setAttribute("Data", data);

            PagesContext context = new PagesContext("modelForm", "0", scc
                .getLanguage(), false, scc.getComponentId(), scc.getUserId());
            context.setBorderPrinted(false);
            context.setObjectId(contactId);
            request.setAttribute("PagesContext", context);
          }
        } else if ("NewModel".equals(action)) {

          if (!StringUtil.isDefined(modelId) && contactId != null) {
            modelId = scc.getCurrentTopic().getNodeDetail().getModelId();
          }

          if (StringUtil.isDefined(modelId) && modelId.endsWith(".xml")) {
            String xmlFormName = modelId;
            String xmlFormShortName = xmlFormName.substring(xmlFormName
                .indexOf("/") + 1, xmlFormName.indexOf("."));
            // création du PublicationTemplate
            PublicationTemplateManager.addDynamicPublicationTemplate(scc
                .getComponentId()
                + ":" + xmlFormShortName, xmlFormName);
            PublicationTemplateImpl pubTemplate =
                (PublicationTemplateImpl) PublicationTemplateManager
                    .getPublicationTemplate(scc.getComponentId() + ":"
                    + xmlFormShortName, xmlFormName);

            // création du formulaire et du DataRecord
            Form formUpdate = pubTemplate.getUpdateForm();
            RecordSet recordSet = pubTemplate.getRecordSet();
            DataRecord data = recordSet.getRecord(contactId);
            if (data == null) {
              data = recordSet.getEmptyRecord();
              data.setId(contactId);// id contact
            }

            // appel de la jsp avec les paramètres
            request.setAttribute("Form", formUpdate);
            request.setAttribute("Data", data);

            PagesContext context = new PagesContext("modelForm", "0", scc
                .getLanguage(), false, scc.getComponentId(), scc.getUserId());
            context.setBorderPrinted(false);
            context.setObjectId(contactId);
            request.setAttribute("PagesContext", context);
          }
        } else if ("Add".equals(action)) { // met à jour le choix de formulaire
          // XML
          if (StringUtil.isDefined(modelId)) {
            String xmlFormName = modelId;
            String xmlFormShortName = xmlFormName.substring(xmlFormName
                .indexOf("/") + 1, xmlFormName.indexOf("."));

            // récupération des données du formulaire (via le DataRecord)
            PublicationTemplate pubTemplate = PublicationTemplateManager
                .getPublicationTemplate(scc.getComponentId() + ":"
                + xmlFormShortName);
            Form formUpdate = pubTemplate.getUpdateForm();
            RecordSet recordSet = pubTemplate.getRecordSet();
            DataRecord data = recordSet.getRecord(contactId);
            if (data == null) {
              data = recordSet.getEmptyRecord();
              data.setId(contactId);// id contact
            }

            // sauvegarde des données du formulaire
            PagesContext context = new PagesContext("modelForm", "0", scc
                .getLanguage(), false, scc.getComponentId(), scc.getUserId());
            context.setObjectId(contactId);
            formUpdate.update(items, data, context);
            recordSet.save(data);

            // sauvegarde du contact et du model
            scc.createInfoModel(contactId, modelId);

          }
        }

        request.setAttribute("ContactId", contactId);
        request.setAttribute("ModelId", modelId);
        request.setAttribute("Action", action);
        destination = "/yellowpages/jsp/modelManager.jsp";
      } else if (function.equals("DeleteBasketContent")) {
        scc.deleteBasketContent();
        // Back to topic
        destination = getDestination("topicManager", scc, request);
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

}