/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.formsonline.servlets;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.formsonline.control.FormsOnlineSessionController;
import com.silverpeas.formsonline.model.FormDetail;
import com.silverpeas.formsonline.model.FormInstance;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.servlet.HttpRequest;
import org.silverpeas.util.StringUtil;

import java.util.List;

import static com.silverpeas.formsonline.control.FormsOnlineSessionController
    .userPanelReceiversPrefix;
import static com.silverpeas.formsonline.control.FormsOnlineSessionController
    .userPanelSendersPrefix;

public class FormsOnlineRequestRouter extends ComponentRequestRouter<FormsOnlineSessionController> {

  private static final long serialVersionUID = -6152014003939730643L;

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "FormsOnline";
  }

  /**
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   */
  public FormsOnlineSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new FormsOnlineSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param formsOnlineSC The component Session Control, build and initialised.
   * @param request
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function, FormsOnlineSessionController formsOnlineSC,
      HttpRequest request) {
    String destination = "";
    SilverTrace.info("formsOnline", "FormsOnlineRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "User=" + formsOnlineSC.getUserId() + " Function=" + function);

    try {
      if ("Main".equals(function)) {
        String role = formsOnlineSC.getBestProfile();
        if ("Administrator".equals(role)) {
          request.setAttribute("formsList", formsOnlineSC.getAllForms(true));
          request.setAttribute("RequestsAsValidator", formsOnlineSC.getAllValidatorRequests());
          request.setAttribute("Role", "admin");
        } else {
          if (!formsOnlineSC.getAvailableFormIdsAsReceiver().isEmpty()) {
            request.setAttribute("RequestsAsValidator", formsOnlineSC.getAllValidatorRequests());
            request.setAttribute("Role", "validator");
          } else {
            request.setAttribute("Role", "senderOnly");
          }
          request.setAttribute("formsList", formsOnlineSC.getAvailableFormsToSend());
        }

        request.setAttribute("UserRequests", formsOnlineSC.getAllUserRequests());
        request.setAttribute("App", formsOnlineSC.getComponentInstLight());
        formsOnlineSC.setCurrentForm(null);

        destination = "formsList.jsp";
      } else if ("CreateForm".equals(function)) {
        FormDetail form = new FormDetail();
        formsOnlineSC.setCurrentForm(form);

        request.setAttribute("currentForm", form);
        request.setAttribute("availableTemplates", formsOnlineSC.getTemplates());
        destination = "editForm.jsp";
      } else if ("SaveForm".equals(function)) {
        FormDetail form = formsOnlineSC.getCurrentForm();
        form.setDescription(request.getParameter("description"));
        form.setTitle(request.getParameter("title"));
        form.setName(form.getTitle());
        if (request.getParameter("template") != null) {
          form.setXmlFormName(request.getParameter("template"));
        }

        String[] senderUserIds = StringUtil.split(
            request.getParameter(userPanelSendersPrefix + "UserPanelCurrentUserIds"), ',');
        String[] senderGroupIds = StringUtil.split(
            request.getParameter(userPanelSendersPrefix + "UserPanelCurrentGroupIds"), ',');

        String[] receiverUserIds = StringUtil.split(
            request.getParameter(userPanelReceiversPrefix + "UserPanelCurrentUserIds"), ',');
        String[] receiverGroupIds = StringUtil.split(
            request.getParameter(userPanelReceiversPrefix + "UserPanelCurrentGroupIds"), ',');

        formsOnlineSC.updateCurrentForm(senderUserIds, senderGroupIds, receiverUserIds,
            receiverGroupIds);

        return getDestination("Main", formsOnlineSC, request);
      } else if (function.equals("EditForm")) {
        String formId = request.getParameter("formId");
        FormDetail form;
        if (formId != null) {
          form = formsOnlineSC.loadForm(Integer.parseInt(formId));
        } else {
          form = formsOnlineSC.getCurrentForm();
        }

        if (form == null) {
          return getDestination("Main", formsOnlineSC, request);
        }
        request.setAttribute("currentForm", form);
        request.setAttribute("availableTemplates", formsOnlineSC.getTemplates());

        destination = "editForm.jsp";
      } else if ("DeleteForm".equals(function)) {
        String formId = request.getParameter("formId");

        if (formId != null) {
          formsOnlineSC.deleteForm(Integer.parseInt(formId));
        }
        return getDestination("Main", formsOnlineSC, request);
      } else if (function.equals("ModifySenders") || function.equals("ModifyReceivers")) {
        List<String> userIds =
            (List<String>) StringUtil.splitString(request.getParameter("UserPanelCurrentUserIds"),
                ',');
        List<String> groupIds =
            (List<String>) StringUtil.splitString(request.getParameter("UserPanelCurrentGroupIds"),
                ',');

        if (function.equals("ModifySenders")) {
          return formsOnlineSC.initSelectionSenders(userIds, groupIds);
        }

        return formsOnlineSC.initSelectionReceivers(userIds, groupIds);
      } else if (function.equals("Preview")) {
        // form object and data fetching
        String xmlFormName = request.getParameter("Form");
        String xmlFormShortName =
            xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName.indexOf('.'));

        // PublicationTemplate creation
        getPublicationTemplateManager()
            .addDynamicPublicationTemplate(formsOnlineSC.getComponentId() + ":" + xmlFormShortName,
                xmlFormName);
        PublicationTemplateImpl pubTemplate =
            (PublicationTemplateImpl) getPublicationTemplateManager()
                .getPublicationTemplate(formsOnlineSC.getComponentId() + ":" + xmlFormShortName,
                    xmlFormName);

        // DataRecord and form creation
        Form formUpdate = pubTemplate.getUpdateForm();
        RecordSet recordSet = pubTemplate.getRecordSet();
        DataRecord data = recordSet.getEmptyRecord();

        // call to the JSP with required parameters
        request.setAttribute("Form", formUpdate);
        request.setAttribute("Data", data);
        request.setAttribute("XMLFormName", xmlFormName);
        request.setAttribute("FormContext", getFormContext(formsOnlineSC));

        destination = "preview.jsp";
      } else if (function.equals("PublishForm")) {
        formsOnlineSC.publishForm(request.getParameter("Id"));
        return getDestination("Main", formsOnlineSC, request);
      } else if (function.equals("UnpublishForm")) {
        formsOnlineSC.unpublishForm(request.getParameter("Id"));
        return getDestination("Main", formsOnlineSC, request);
      } else if (function.equals("InBox")) {
        request.setAttribute("Requests", formsOnlineSC.getAllValidatorRequests());
        destination = "inbox.jsp";
      } else if (function.equals("NewRequest")) {
        String formId = request.getParameter("FormId");
        FormDetail form = formsOnlineSC.loadForm(Integer.parseInt(formId));
        formsOnlineSC.setCurrentForm(form);
        // form object and name fetching
        String xmlFormName = form.getXmlFormName();
        String xmlFormShortName =
            xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName.indexOf('.'));

        // PublicationTemplate creation
        getPublicationTemplateManager()
            .addDynamicPublicationTemplate(formsOnlineSC.getComponentId() + ":" + xmlFormShortName,
                xmlFormName);
        PublicationTemplateImpl pubTemplate =
            (PublicationTemplateImpl) getPublicationTemplateManager()
                .getPublicationTemplate(formsOnlineSC.getComponentId() + ":" + xmlFormShortName,
                    xmlFormName);

        // form and DataRecord creation
        Form formUpdate = pubTemplate.getUpdateForm();
        RecordSet recordSet = pubTemplate.getRecordSet();
        DataRecord data = recordSet.getEmptyRecord();
        formUpdate.setData(data);

        // call of the JSP with required parameters
        request.setAttribute("Form", formUpdate);
        request.setAttribute("FormContext", getFormContext(formsOnlineSC));
        request.setAttribute("FormDetail", form);

        destination = "newFormInstance.jsp";
      } else if (function.equals("SaveRequest")) {
        // recuperation des donnees saisies dans le formulaire
        List<FileItem> items = request.getFileItems();

        // Sauvegarde des donnees
        formsOnlineSC.saveRequest(items);

        return getDestination("Main", formsOnlineSC, request);
      } else if ("ViewRequest".equals(function)) {
        String formInstanceId = request.getParameter("Id");
        FormInstance userRequest = formsOnlineSC.loadRequest(formInstanceId);

        // Add attribute inside request to display data inside JSP view
        request.setAttribute("Form", userRequest.getFormWithData());
        PagesContext formContext = getFormContext(formsOnlineSC);
        formContext.setObjectId(formInstanceId);
        request.setAttribute("FormContext", formContext);
        request.setAttribute("ValidationEnabled", userRequest.isValidationEnabled());
        request.setAttribute("UserRequest", userRequest);
        request.setAttribute("FormDetail", userRequest.getForm());
        request.setAttribute("Origin", checkOrigin(request));

        destination = "viewInstance.jsp";
      } else if (function.equals("EffectiveValideForm")) {
        String requestId = request.getParameter("Id");
        String decision = request.getParameter("decision");
        String comment = request.getParameter("comment");
        String origin = checkOrigin(request);
        formsOnlineSC.updateValidationStatus(requestId, decision, comment);

        return getDestination(origin, formsOnlineSC, request);
      } else if (function.equals("ArchiveRequest")) {
        String id = request.getParameter("Id");
        formsOnlineSC.archiveRequest(id);

        return getDestination("Main", formsOnlineSC, request);
      } else if (function.equals("DeleteRequest")) {
        String id = request.getParameter("Id");
        formsOnlineSC.deleteRequest(id);

        return getDestination("InBox", formsOnlineSC, request);
      } else if (function.equals("DeleteRequests")) {
        String[] ids = request.getParameterValues("Id");
        formsOnlineSC.deleteRequests(ids);

        return getDestination("InBox", formsOnlineSC, request);
      } else {
        destination = "welcome.jsp";
      }
      destination = "/formsOnline/jsp/" + destination;
    } catch (Exception e) {
      SilverTrace.warn("formsOnline", "FormsOnlineRequestRouter.getDestination()",
          "An error occured when processing function", "function = " + function, e);
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    SilverTrace.info("formsOnline", "FormsOnlineRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
    return destination;
  }

  /**
   * Gets an instance of the PublicationTemplateManager manager.
   * @return an instance of PublicationTemplateManager.
   */
  private PublicationTemplateManager getPublicationTemplateManager() {
    return PublicationTemplateManager.getInstance();
  }

  private String checkOrigin(HttpRequest httpRequest) {
    String origin = httpRequest.getParameter("Origin");
    if (!StringUtil.isDefined(origin)) {
      origin = "Main";
    }
    return origin;
  }

  private PagesContext getFormContext(FormsOnlineSessionController fosc) {
    PagesContext formContext =
        new PagesContext("unknown", "0", fosc.getLanguage(), false, fosc.getComponentId(),
            fosc.getUserId());
    return formContext;
  }
}