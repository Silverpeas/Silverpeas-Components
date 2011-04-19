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

package com.silverpeas.formsonline.servlets;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.RecordSet;
import com.silverpeas.formsonline.control.FormsOnlineSessionController;
import com.silverpeas.formsonline.control.TitleHelper;
import com.silverpeas.formsonline.model.FormDetail;
import com.silverpeas.formsonline.model.FormInstance;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class FormsOnlineRequestRouter extends ComponentRequestRouter {

  private static final long serialVersionUID = -6152014003939730643L;

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "FormsOnline";
  }

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    return new FormsOnlineSessionController(mainSessionCtrl,
        componentContext);
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
    String destination = "";
    FormsOnlineSessionController formsOnlineSC = (FormsOnlineSessionController) componentSC;
    SilverTrace.info("formsOnline",
        "FormsOnlineRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "User=" + componentSC.getUserId()
        + " Function=" + function);

    try {
      if (function.equals("Main")) {

        /* this area's access is restricted to Administrators */
        if (!getFlag(formsOnlineSC.getUserRoles()).equals("Administrator")) {
          return getDestination("OutBox", componentSC, request);
        }

        request.setAttribute("formsList", formsOnlineSC.getAllForms());
        formsOnlineSC.setCurrentForm(null);

        destination = "formsList.jsp";
      }

      else if (function.equals("CreateForm")) {
        FormDetail form = new FormDetail();
        form.setCreatorId(formsOnlineSC.getUserId());
        List<PublicationTemplate> templates = getPublicationTemplateManager()
            .getPublicationTemplates();

        formsOnlineSC.setCurrentForm(form);

        request.setAttribute("currentForm", form);
        request.setAttribute("availableTemplates", templates);

        destination = "editForm.jsp";
      }

      else if (function.equals("SaveForm")) {
        FormDetail form = formsOnlineSC.getCurrentForm();

        form.setName(request.getParameter("name"));
        form.setDescription(request.getParameter("description"));
        form.setTitle(request.getParameter("title"));
        if (request.getParameter("template") != null) {
          form.setXmlFormName(request.getParameter("template"));
        }

        formsOnlineSC.updateCurrentForm();

        return getDestination("EditForm", componentSC, request);
      }

      else if (function.equals("EditForm")) {
        String formId = request.getParameter("formId");
        FormDetail form = null;

        if (formId != null) {
          form = formsOnlineSC.loadForm(Integer.parseInt(formId));
        } else {
          form = formsOnlineSC.getCurrentForm();
        }

        if (form == null) {
          return getDestination("Main", componentSC, request);
        }

        List<PublicationTemplate> templates = getPublicationTemplateManager()
            .getPublicationTemplates();
        request.setAttribute("currentForm", form);
        request.setAttribute("availableTemplates", templates);

        destination = "editForm.jsp";
      }

      else if (function.equals("DeleteForm")) {
        String formId = request.getParameter("formId");

        if (formId != null) {
          formsOnlineSC.deleteForm(Integer.parseInt(formId));
        }

        return getDestination("Main", componentSC, request);
      }

      else if (function.equals("SendersReceivers")) {
        try {
          request.setAttribute("sendersAsUser", formsOnlineSC
              .getSendersAsUsers());
          request.setAttribute("sendersAsGroup", formsOnlineSC
              .getSendersAsGroups());
          request.setAttribute("receiversAsUser", formsOnlineSC
              .getReceiversAsUsers());
          request.setAttribute("receiversAsGroup", formsOnlineSC
              .getReceiversAsGroups());
          destination = "sendersReceivers.jsp";
        } catch (Exception e) {
          SilverTrace.warn("formsOnline",
              "FormsOnlineRequestRouter.getDestination()",
              "root.EX_USERPANEL_FAILED", "function = "
              + function, e);
        }
      }

      else if (function.equals("ModifySenders")) {
        try {
          return formsOnlineSC.initSelectionSenders();
        } catch (Exception e) {
          SilverTrace.warn("formsOnline",
              "FormsOnlineRequestRouter.getDestination()",
              "root.EX_USERPANEL_FAILED", "function = "
              + function, e);
        }
      }

      else if (function.equals("ModifyReceivers")) {
        try {
          return formsOnlineSC.initSelectionReceivers();
        } catch (Exception e) {
          SilverTrace.warn("formsOnline",
              "FormsOnlineRequestRouter.getDestination()",
              "root.EX_USERPANEL_FAILED", "function = "
              + function, e);
        }
      }

      else if (function.equals("UpdateSenders")) {
        formsOnlineSC.updateSenders();
        return getDestination("SendersReceivers", componentSC, request);
      }

      else if (function.equals("UpdateReceivers")) {
        formsOnlineSC.updateReceivers();
        return getDestination("SendersReceivers", componentSC, request);
      }

      else if (function.equals("Preview")) {
        // form object and data fetching
        String xmlFormName = formsOnlineSC.getCurrentForm()
            .getXmlFormName();
        String xmlFormShortName = xmlFormName.substring(xmlFormName
            .indexOf("/") + 1, xmlFormName.indexOf("."));

        // PublicationTemplate creation
        getPublicationTemplateManager()
            .addDynamicPublicationTemplate(formsOnlineSC
            .getComponentId()
            + ":" + xmlFormShortName, xmlFormName);
        PublicationTemplateImpl pubTemplate = 
                (PublicationTemplateImpl) getPublicationTemplateManager().getPublicationTemplate(
                    formsOnlineSC.getComponentId() + ":" + xmlFormShortName, xmlFormName);

        // DataRecord and form creation
        Form formUpdate = pubTemplate.getUpdateForm();
        RecordSet recordSet = pubTemplate.getRecordSet();
        DataRecord data = recordSet.getEmptyRecord();

        // Fake formInstance to compute title
        FormInstance fake = new FormInstance();
        fake.setCreatorId(formsOnlineSC.getUserId());

        // call to the JSP with required parameters
        request.setAttribute("Form", formUpdate);
        request.setAttribute("Data", data);
        request.setAttribute("XMLFormName", xmlFormName);
        request.setAttribute("title", TitleHelper.computeTitle(fake, formsOnlineSC.getCurrentForm()
            .getTitle()));

        destination = "preview.jsp";
      }

      else if (function.equals("PublishForm")) {
        formsOnlineSC.publishForm(request.getParameter("formId"));
        return getDestination("Main", componentSC, request);
      }

      else if (function.equals("UnpublishForm")) {
        formsOnlineSC.unpublishForm(request.getParameter("formId"));
        return getDestination("Main", componentSC, request);
      }

      else if (function.equals("OutBox")) {
        List<FormDetail> availableForms = formsOnlineSC.getAvailableFormsToSend();
        request.setAttribute("availableForms", availableForms);

        int requestFormId = (request.getParameter("formId") == null) ? -1
            : Integer.parseInt(request.getParameter("formId"));
        int choosenFormId = -1;
        FormDetail choosenForm = null;

        if (requestFormId != -1) {
          for (FormDetail form : availableForms) {
            if (requestFormId == form.getId()) {
              choosenFormId = requestFormId;
              choosenForm = form;
              break;
            }
          }
        }

        if (choosenFormId == -1) {
          if (availableForms.size() > 0) {
            FormDetail form = (FormDetail) availableForms.get(0);
            choosenFormId = form.getId();
            choosenForm = form;
          }
        }

        formsOnlineSC.setChoosenForm(choosenForm);

        // Filter instance on state
        List<FormInstance> sentForInstances = formsOnlineSC.getFormInstances(choosenFormId);
        formsOnlineSC.filter(sentForInstances, request.getParameter("filteredState"));

        request.setAttribute("choosenForm", choosenForm);
        request.setAttribute("formInstances", sentForInstances);

        destination = "outbox.jsp";
      }

      else if (function.equals("InBox")) {
        // List receivedForInstances = formsOnlineSC.getAvailableFormInstancesReceived();
        // Iterator itInstances = receivedForInstances.iterator();
        // Set formIds = new HashSet();
        // while (itInstances.hasNext()) {
        // FormInstance instance = (FormInstance) itInstances.next();
        // formIds.add(String.valueOf(instance.getFormId()));
        // }

        List<String> formIds = formsOnlineSC.getAvailableFormIdsAsReceiver();
        List<FormDetail> availableForms = formsOnlineSC.getForms(formIds);

        int requestFormId = (request.getParameter("formId") == null) ? -1
            : Integer.parseInt(request.getParameter("formId"));
        int choosenFormId = -1;
        FormDetail choosenForm = null;

        if (requestFormId != -1) {
          for (FormDetail form : availableForms) {
            if (requestFormId == form.getId()) {
              choosenFormId = requestFormId;
              choosenForm = form;
              break;
            }
          }
        }

        if (choosenFormId == -1) {
          if (availableForms.size() > 0) {
            FormDetail form = availableForms.get(0);
            choosenFormId = form.getId();
            choosenForm = form;
          }
        }

        List<FormInstance> receivedForInstances =
            formsOnlineSC.getAvailableFormInstancesReceived(choosenFormId);

        // Filter instance on state
        formsOnlineSC.filter(receivedForInstances, request.getParameter("filteredState"));

        formsOnlineSC.setChoosenForm(choosenForm);
        request.setAttribute("choosenForm", choosenForm);
        request.setAttribute("formInstances", receivedForInstances);
        request.setAttribute("availableForms", availableForms);

        destination = "inbox.jsp";
      }

      else if (function.equals("CreateInstance")) {
        FormDetail form = formsOnlineSC.getChoosenForm();

        // form object and name fetching
        String xmlFormName = form.getXmlFormName();
        String xmlFormShortName = xmlFormName.substring(xmlFormName
            .indexOf("/") + 1, xmlFormName.indexOf("."));

        // PublicationTemplate creation
        getPublicationTemplateManager()
            .addDynamicPublicationTemplate(formsOnlineSC
            .getComponentId()
            + ":" + xmlFormShortName, xmlFormName);
        PublicationTemplateImpl pubTemplate =
                (PublicationTemplateImpl) getPublicationTemplateManager().getPublicationTemplate(
                    formsOnlineSC.getComponentId() + ":" + xmlFormShortName, xmlFormName);

        // form and DataRecord creation
        Form formUpdate = pubTemplate.getUpdateForm();
        RecordSet recordSet = pubTemplate.getRecordSet();
        DataRecord data = recordSet.getEmptyRecord();

        // call of the JSP with required parameters
        request.setAttribute("Form", formUpdate);
        request.setAttribute("Data", data);
        request.setAttribute("XMLFormName", xmlFormName);

        destination = "newFormInstance.jsp";
      }

      else if (function.equals("SaveNewInstance")) {
        // r�cup�ration des donn�es saisies dans le formulaire
        List<FileItem> items = FileUploadUtil.parseRequest(request);

        // Sauvegarde des donn�es
        formsOnlineSC.saveNewInstance(items);

        // Mise � jour du formulaire pour indiquer qu'il a �t� utilis�
        FormDetail form = formsOnlineSC.loadForm(formsOnlineSC
            .getChoosenForm().getId());
        if (!form.isAlreadyUsed()) {
          form.setAlreadyUsed(true);
          formsOnlineSC.updateCurrentForm();
        }

        return getDestination("OutBox", componentSC, request);
      }

      else if (function.equals("ViewFormInstance")) {
        String formInstanceId = request.getParameter("formInstanceId");
        FormInstance currentFormInstance =
            formsOnlineSC.loadFormInstance(Integer.parseInt(formInstanceId));

        // r�cuperation de l�objet et du nom du formulaire
        FormDetail form = formsOnlineSC.loadForm(currentFormInstance.getFormId());
        formsOnlineSC.setChoosenForm(form);
        String xmlFormName = form.getXmlFormName();
        String xmlFormShortName = xmlFormName.substring(xmlFormName
            .indexOf("/") + 1, xmlFormName.indexOf("."));

        // cr�ation du PublicationTemplate
        getPublicationTemplateManager()
            .addDynamicPublicationTemplate(formsOnlineSC
            .getComponentId()
            + ":" + xmlFormShortName, xmlFormName);
        PublicationTemplateImpl pubTemplate =
                (PublicationTemplateImpl) getPublicationTemplateManager().getPublicationTemplate(
                    formsOnlineSC.getComponentId() + ":" + xmlFormShortName, xmlFormName);

        // cr�ation du formulaire et du DataRecord
        Form formView = pubTemplate.getViewForm();
        RecordSet recordSet = pubTemplate.getRecordSet();
        DataRecord data = recordSet.getRecord(formInstanceId);
        if (data == null) {
          return getDestination("OutBox", componentSC, request);
        }

        // appel de la jsp avec les param�tres
        request.setAttribute("Form", formView);
        request.setAttribute("Data", data);
        request.setAttribute("XMLFormName", xmlFormName);
        request.setAttribute("validationMode", "inactive");
        request.setAttribute("currentFormInstance", currentFormInstance);
        request.setAttribute("title", TitleHelper.computeTitle(currentFormInstance, formsOnlineSC
            .getChoosenForm().getTitle()));
        request.setAttribute("backFunction", "OutBox");
        destination = "viewInstance.jsp";
      }

      else if (function.equals("ValidFormInstance")) {
        String formInstanceId = request.getParameter("formInstanceId");
        FormInstance formInstance =
            formsOnlineSC.loadFormInstance(Integer.parseInt(formInstanceId));

        // r�cuperation de l�objet et du nom du formulaire
        FormDetail formDetail = formsOnlineSC.loadForm(formInstance.getFormId());
        formsOnlineSC.setChoosenForm(formDetail);
        String xmlFormName = formDetail
            .getXmlFormName();
        String xmlFormShortName = xmlFormName.substring(xmlFormName
            .indexOf("/") + 1, xmlFormName.indexOf("."));

        // cr�ation du PublicationTemplate
        getPublicationTemplateManager()
            .addDynamicPublicationTemplate(formsOnlineSC
            .getComponentId()
            + ":" + xmlFormShortName, xmlFormName);
        PublicationTemplateImpl pubTemplate =
                (PublicationTemplateImpl) getPublicationTemplateManager()
            .getPublicationTemplate(formsOnlineSC.getComponentId()
            + ":" + xmlFormShortName, xmlFormName);

        // cr�ation du formulaire et du DataRecord
        Form formView = pubTemplate.getViewForm();
        RecordSet recordSet = pubTemplate.getRecordSet();
        DataRecord data = recordSet.getRecord(formInstanceId);
        if (data == null) {
          return getDestination("OutBox", componentSC, request);
        }

        // mise a jour du statut de l'instance
        if (formInstance.getState() == FormInstance.STATE_UNREAD) {
          formInstance.setState(FormInstance.STATE_READ);
          formsOnlineSC.updateFormInstance(formInstance);
        }

        // appel de la jsp avec les param�tres
        request.setAttribute("Form", formView);
        request.setAttribute("Data", data);
        request.setAttribute("XMLFormName", xmlFormName);
        request.setAttribute("validationMode", "active");
        request.setAttribute("currentFormInstance", formInstance);
        request.setAttribute("backFunction", "InBox");
        request
            .setAttribute("title", TitleHelper.computeTitle(formInstance, formDetail.getTitle()));

        destination = "viewInstance.jsp";
      }

      else if (function.equals("EffectiveValideForm")) {
        int formInstanceId = Integer.parseInt(request.getParameter("formInstanceId"));
        String decision = request.getParameter("decision");
        String comment = request.getParameter("comment");
        formsOnlineSC.updateValidationStatus(formInstanceId, decision, comment);

        return getDestination("InBox", componentSC, request);
      }

      else if (function.equals("ArchiveFormInstances")) {
        String[] formInstanceIds = request.getParameterValues("archiveInst");
        if ((formInstanceIds != null) && (formInstanceIds.length > 0)) {
          formsOnlineSC.archiveFormInstances(formInstanceIds);
        }

        return getDestination("OutBox", componentSC, request);
      }

      else if (function.equals("DeleteFormInstances")) {
        String[] formInstanceIds = request.getParameterValues("suppInst");
        if ((formInstanceIds != null) && (formInstanceIds.length > 0)) {
          formsOnlineSC.deleteFormInstances(formInstanceIds);
        }

        return getDestination("InBox", componentSC, request);
      }

      else {
        destination = "welcome.jsp";
      }
      destination = "/formsOnline/jsp/" + destination;
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    // store user's best profile in request
    request.setAttribute("userBestProfile", getFlag(formsOnlineSC.getUserRoles()));

    SilverTrace.info("formsOnline",
        "FormsOnlineRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
    return destination;
  }

  /* getFlag */
  private String getFlag(String[] profiles) {
    String flag = "SenderReceiver";
    for (int i = 0; i < profiles.length; i++) {
      // if Administrator, return it, we won't find a better profile
      if (profiles[i].equals("Administrator"))
        return profiles[i];
    }
    return flag;
  }
  
  /**
   * Gets an instance of the PublicationTemplateManager manager.
   * @return an instance of PublicationTemplateManager.
   */
  private PublicationTemplateManager getPublicationTemplateManager() {
    return PublicationTemplateManager.getInstance();
  }
}