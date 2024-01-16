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
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.components.formsonline.servlets;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.components.formsonline.control.FormsOnlineSessionController;
import org.silverpeas.components.formsonline.model.FormDetail;
import org.silverpeas.components.formsonline.model.FormInstance;
import org.silverpeas.components.formsonline.model.FormInstanceValidationType;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.export.ExportCSVBuilder;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;

import java.util.List;

import static org.silverpeas.components.formsonline.control.FormsOnlineSessionController.*;
import static org.silverpeas.core.util.StringUtil.isInteger;
import static org.silverpeas.core.util.StringUtil.isNotDefined;

public class FormsOnlineRequestRouter extends ComponentRequestRouter<FormsOnlineSessionController> {

  private static final long serialVersionUID = -6152014003939730643L;
  private static final String INBOX = "InBox";
  private static final String USER_PANEL_CURRENT_USER_IDS = "UserPanelCurrentUserIds";
  private static final String USER_PANEL_CURRENT_GROUP_IDS = "UserPanelCurrentGroupIds";
  private static final String FORM_CONTEXT = "FormContext";
  private static final String PARAM_FORMID = "FormId";
  private static final String PARAM_REQUESTID = "Id";
  private static final String ROOT_DESTINATION = "/formsOnline/jsp/";

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "FormsOnline";
  }

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

    try {
      if ("Main".equals(function)) {
        formsOnlineSC.setCurrentFilter(-1, null);
        formsOnlineSC.getSelectedValidatorRequestIds().clear();
        final String role = formsOnlineSC.getBestProfile();
        if ("Administrator".equals(role)) {
          request.setAttribute("formsList", formsOnlineSC.getAllForms(true));
          request.setAttribute("RequestsAsValidator", formsOnlineSC.getHomepageValidatorRequests());
          request.setAttribute("Role", "admin");
        } else {
          if (!formsOnlineSC.getAvailableFormIdsAsReceiver().isEmpty()) {
            request.setAttribute("RequestsAsValidator", formsOnlineSC.getHomepageValidatorRequests());
            request.setAttribute("Role", "validator");
          } else {
            request.setAttribute("Role", "senderOnly");
          }
          request.setAttribute("formsList", formsOnlineSC.getAvailableFormsToSend());
        }
        request.setAttribute("UserRequests", formsOnlineSC.getAllUserRequests());
        request.setAttribute("App", formsOnlineSC.getComponentInstLight());
        formsOnlineSC.resetCurrentForm();
        destination = ROOT_DESTINATION + "formsList.jsp";
      } else if ("CreateForm".equals(function)) {
        final FormDetail form = new FormDetail();
        formsOnlineSC.setCurrentForm(form);
        request.setAttribute("currentForm", form);
        request.setAttribute("availableTemplates", formsOnlineSC.getTemplates());
        destination = ROOT_DESTINATION + "editForm.jsp";
      } else if ("SaveForm".equals(function)) {
        FormDetail form = formsOnlineSC.getCurrentForm();
        form.setDescription(request.getParameter("description"));
        form.setTitle(request.getParameter("title"));
        form.setName(form.getTitle());
        if (request.getParameter("template") != null) {
          form.setXmlFormName(request.getParameter("template"));
        }
        form.setHierarchicalValidation(request.getParameterAsBoolean("bossValidation"));
        form.setDeleteAfterRequestExchange(request.getParameterAsBoolean("directDeletion"));
        form.setRequestExchangeReceiver(request.getParameter("sendEmail"));
        final List<String> senderUserIds = request.getParameterAsList(USER_PANEL_SENDERS_PREFIX + USER_PANEL_CURRENT_USER_IDS);
        final List<String> senderGroupIds = request.getParameterAsList(USER_PANEL_SENDERS_PREFIX + USER_PANEL_CURRENT_GROUP_IDS);
        final List<String> interReceiverUserIds = request.getParameterAsList(USER_PANEL_INTERMEDIATE_RECEIVERS_PREFIX + USER_PANEL_CURRENT_USER_IDS);
        final List<String> interReceiverGroupIds = request.getParameterAsList(USER_PANEL_INTERMEDIATE_RECEIVERS_PREFIX + USER_PANEL_CURRENT_GROUP_IDS);
        final List<String> receiverUserIds = request.getParameterAsList(USER_PANEL_RECEIVERS_PREFIX + USER_PANEL_CURRENT_USER_IDS);
        final List<String> receiverGroupIds = request.getParameterAsList(USER_PANEL_RECEIVERS_PREFIX + USER_PANEL_CURRENT_GROUP_IDS);
        formsOnlineSC.saveCurrentForm(senderUserIds, senderGroupIds, interReceiverUserIds,
            interReceiverGroupIds, receiverUserIds, receiverGroupIds);
        return getDestination("Main", formsOnlineSC, request);
      } else if ("EditForm".equals(function)) {
        String formId = request.getParameter(PARAM_FORMID);
        FormDetail form = formsOnlineSC.getCurrentForm();
        if (formId != null) {
          form = formsOnlineSC.setCurrentForm(formId);
        }

        if (form == null) {
          return getDestination("Main", formsOnlineSC, request);
        }
        request.setAttribute("currentForm", form);
        request.setAttribute("availableTemplates", formsOnlineSC.getTemplates());

        destination = ROOT_DESTINATION + "editForm.jsp";
      } else if ("DeleteForm".equals(function)) {
        String formId = request.getParameter(PARAM_FORMID);

        if (formId != null) {
          formsOnlineSC.deleteForm(Integer.parseInt(formId));
        }
        return getDestination("Main", formsOnlineSC, request);
      } else if ("ModifySenders".equals(function) || "ModifyReceivers".equals(function) ||
          "ModifyIntermediateReceivers".equals(function)) {
        List<String> userIds = (List<String>) StringUtil
            .splitString(request.getParameter(USER_PANEL_CURRENT_USER_IDS), ',');
        List<String> groupIds = (List<String>) StringUtil
            .splitString(request.getParameter(USER_PANEL_CURRENT_GROUP_IDS), ',');

        if ("ModifySenders".equals(function)) {
          return formsOnlineSC.initSelectionSenders(userIds, groupIds);
        } else if ("ModifyIntermediateReceivers".equals(function)) {
          return formsOnlineSC.initSelectionIntermediateReceivers(userIds, groupIds);
        }

        return formsOnlineSC.initSelectionReceivers(userIds, groupIds);
      } else if ("Preview".equals(function)) {
        // form object and data fetching
        String xmlFormName = request.getParameter("Form");

        // DataRecord and form creation
        Form formUpdate = formsOnlineSC.getEmptyForm(xmlFormName);

        // call to the JSP with required parameters
        request.setAttribute("Form", formUpdate);
        request.setAttribute("XMLFormName", xmlFormName);
        request.setAttribute(FORM_CONTEXT, formsOnlineSC.getFormPageContext());

        destination = ROOT_DESTINATION + "preview.jsp";
      } else if ("PublishForm".equals(function)) {
        formsOnlineSC.publishForm(request.getParameter("Id"));
        return getDestination("Main", formsOnlineSC, request);
      } else if ("UnpublishForm".equals(function)) {
        formsOnlineSC.unpublishForm(request.getParameter("Id"));
        return getDestination("Main", formsOnlineSC, request);
      } else if (INBOX.equals(function)) {
        // Selection
        request.mergeSelectedItemsInto(formsOnlineSC.getSelectedValidatorRequestIds());
        request.setAttribute("Requests", formsOnlineSC.getAllValidatorRequests());
        request.setAttribute("CurrentForm", formsOnlineSC.getCurrentForm());
        request.setAttribute("Forms", formsOnlineSC.getAllForms(false));
        destination = ROOT_DESTINATION + "inbox.jsp";
      } else if ("FilterRequests".equals(function)) {
        String formId = request.getParameter(PARAM_FORMID);
        formsOnlineSC.setCurrentForm(formId);
        final String filterValue = request.getParameter("State");
        final int state = isInteger(filterValue) ? Integer.parseInt(filterValue) : -1;
        final FormInstanceValidationType validation = state > -1 || isNotDefined(filterValue)
            ? null
            : FormInstanceValidationType.valueOf(filterValue);
        formsOnlineSC.setCurrentFilter(state, validation);
        return getDestination(INBOX, formsOnlineSC, request);
      } else if ("Export".equals(function)) {
        ExportCSVBuilder csvBuilder = formsOnlineSC.export();

        destination = csvBuilder.setupRequest(request);
      } else if ("NewRequest".equals(function)) {
        String formId = request.getParameter(PARAM_FORMID);
        if (StringUtil.isNotDefined(formId)) {
          formId = (String) request.getAttribute(PARAM_FORMID);
        }
        FormDetail form = formsOnlineSC.setCurrentForm(formId);

        // form and DataRecord creation
        Form formUpdate = formsOnlineSC.getCurrentEmptyForm();

        // call of the JSP with required parameters
        request.setAttribute("Form", formUpdate);
        request.setAttribute(FORM_CONTEXT, formsOnlineSC.getFormPageContext());
        request.setAttribute("FormDetail", form);

        destination = ROOT_DESTINATION + "newFormInstance.jsp";
      } else if ("EditRequest".equals(function)) {
        String id = request.getParameter(PARAM_REQUESTID);

        FormInstance userRequest = formsOnlineSC.loadRequest(id, true);

        request.setAttribute("UserRequest", userRequest);
        PagesContext formContext = formsOnlineSC.getFormPageContext();
        formContext.setObjectId(userRequest.getId());
        request.setAttribute(FORM_CONTEXT, formContext);

        destination = ROOT_DESTINATION + "editFormInstance.jsp";
      } else if (function.startsWith("SaveRequest")) {
        final boolean isDraft = function.endsWith("AsDraft");
        final List<FileItem> items = request.getFileItems();
        formsOnlineSC.saveRequest(items, isDraft);
        return getDestination("Main", formsOnlineSC, request);
      } else if ("ViewRequest".equals(function)) {
        String formInstanceId = request.getParameter("Id");
        FormInstance userRequest = formsOnlineSC.loadRequest(formInstanceId, false);

        // Add attribute inside request to display data inside JSP view
        request.setAttribute("Form", userRequest.getFormWithData());
        PagesContext formContext = formsOnlineSC.getFormPageContext();
        formContext.setObjectId(formInstanceId);
        request.setAttribute(FORM_CONTEXT, formContext);
        request.setAttribute("ValidationEnabled", userRequest.isValidationEnabled());
        request.setAttribute("UserRequest", userRequest);
        request.setAttribute("FormDetail", userRequest.getForm());
        request.setAttribute("FinalValidator",
            userRequest.getForm().isFinalValidator(User.getCurrentRequester().getId()));
        request.setAttribute("Origin", checkOrigin(request));

        destination = ROOT_DESTINATION + "viewInstance.jsp";
      } else if ("EffectiveValideForm".equals(function)) {
        String requestId = request.getParameter("Id");
        String decision = request.getParameter("decision");
        String comment = request.getParameter("comment");
        boolean follower = request.getParameterAsBoolean("follower");
        String origin = checkOrigin(request);
        formsOnlineSC.updateValidationStatus(requestId, decision, comment, follower);

        return getDestination(origin, formsOnlineSC, request);
      } else if ("CancelRequest".equals(function)) {
        String id = request.getParameter("Id");
        formsOnlineSC.cancelRequest(id);

        return getDestination("Main", formsOnlineSC, request);
      } else if ("ArchiveRequest".equals(function)) {
        String id = request.getParameter("Id");
        formsOnlineSC.archiveRequest(id, true);

        return getDestination("Main", formsOnlineSC, request);
      } else if ("DeleteRequest".equals(function)) {
        String id = request.getParameter("Id");
        String origin = checkOrigin(request);

        formsOnlineSC.deleteRequest(id, true);

        return getDestination(origin, formsOnlineSC, request);
      } else if ("DeleteRequests".equals(function) || "ArchiveRequests".equals(function)) {

        // Selection
        request.mergeSelectedItemsInto(formsOnlineSC.getSelectedValidatorRequestIds());

        if ("DeleteRequests".equals(function)) {
          // Deletion
          formsOnlineSC.deleteRequests(formsOnlineSC.getSelectedValidatorRequestIds());
        } else {
          formsOnlineSC.archiveRequests(formsOnlineSC.getSelectedValidatorRequestIds());
        }

        // Clear selection
        formsOnlineSC.getSelectedValidatorRequestIds().clear();

        return getDestination(INBOX, formsOnlineSC, request);
      } else if ("searchResult".equals(function)) {
        request.setAttribute(PARAM_FORMID, request.getParameter("Id"));

        return getDestination("NewRequest", formsOnlineSC, request);
      } else {
        destination = ROOT_DESTINATION + "welcome.jsp";
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).warn(e);
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    return destination;
  }

  private String checkOrigin(HttpRequest httpRequest) {
    String origin = httpRequest.getParameter("Origin");
    if (!StringUtil.isDefined(origin)) {
      origin = "Main";
    }
    return origin;
  }

}