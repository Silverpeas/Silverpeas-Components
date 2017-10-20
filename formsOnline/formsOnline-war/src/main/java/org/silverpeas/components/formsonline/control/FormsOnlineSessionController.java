/*
 * Copyright (C) 2000 - 2017 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
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
package org.silverpeas.components.formsonline.control;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.components.formsonline.FormsOnlineComponentSettings;
import org.silverpeas.components.formsonline.model.FormDetail;
import org.silverpeas.components.formsonline.model.FormInstance;
import org.silverpeas.components.formsonline.model.FormPK;
import org.silverpeas.components.formsonline.model.FormsOnlineDAO;
import org.silverpeas.components.formsonline.model.FormsOnlineDatabaseException;
import org.silverpeas.components.formsonline.model.FormsOnlineService;
import org.silverpeas.components.formsonline.model.RequestPK;
import org.silverpeas.components.formsonline.model.RequestsByStatus;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.GlobalContext;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.notification.message.MessageNotifier;
import org.silverpeas.core.security.authorization.ForbiddenRuntimeException;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.selection.SelectionUsersGroups;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FormsOnlineSessionController extends AbstractComponentSessionController {

  private static final int DEFAULT_ITEM_PER_PAGE = 10;
  private static final String UPDATE_CURRENT_FORM = "updateCurrentForm";
  private static final String LOAD_REQUEST = "loadRequest";
  private FormsOnlineDAO dao = ServiceProvider.getService(FormsOnlineDAO.class);
  private FormDetail currentForm;
  private Selection selection = null;
  private Set<String> selectedValidatorRequestIds = new HashSet<>();

  public static final String USER_PANEL_SENDERS_PREFIX = "listSenders";
  public static final String USER_PANEL_RECEIVERS_PREFIX = "listReceivers";

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   */
  public FormsOnlineSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.formsonline.multilang.formsOnlineBundle",
        "org.silverpeas.formsonline.settings.formsOnlineIcons",
        "org.silverpeas.formsonline.settings.formsOnlineSettings");
    selection = getSelection();
  }

  public List<FormDetail> getAllForms(boolean withSendInfo) throws FormsOnlineDatabaseException {
    return getService().getAllForms(getComponentId(), getUserId(), withSendInfo);
  }

  public void setCurrentForm(FormDetail form) {
    this.currentForm = form;
  }

  public FormDetail getCurrentForm() {
    return this.currentForm;
  }

  public void updateCurrentForm(String[] senderUserIds, String[] senderGroupIds,
      String[] receiverUserIds, String[] receiverGroupIds) throws FormsOnlineDatabaseException {

    if (!isAdmin()) {
      throwForbiddenException(UPDATE_CURRENT_FORM);
    }

    if (currentForm.getId() == -1) {
      currentForm.setCreatorId(getUserId());
      currentForm.setInstanceId(getComponentId());
      MessageNotifier.addInfo(getString("formsOnline.form.creation.succeed"));
    } else {
      MessageNotifier.addSuccess(getString("formsOnline.form.update.succeed"));
    }
    currentForm =
        getService().storeForm(currentForm, senderUserIds, senderGroupIds, receiverUserIds,
            receiverGroupIds);
  }

  public FormDetail loadForm(int formId) throws FormsOnlineDatabaseException {
    this.currentForm = getService().loadForm(getFormPK(formId));
    return currentForm;
  }


  public void deleteForm(int formId) throws FormsOnlineDatabaseException {
    if (!isAdmin()) {
      throwForbiddenException(UPDATE_CURRENT_FORM);
    }
    getService().deleteForm(getFormPK(formId));
  }

  // initialisation de Selection pour nav vers SelectionPeas
  private String initSelection(SelectionUsersGroups sug, String goFunction, List<String> userIds,
      List<String> groupIds) {
    String url = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL") +
        URLUtil.getURL(getSpaceId(), getComponentId());
    String goUrl = url + goFunction;
    String cancelUrl = url + "SendersReceivers";

    selection.resetAll();

    selection.setGoBackURL(goUrl);
    selection.setCancelURL(cancelUrl);

    selection.setSelectedElements(userIds);
    selection.setSelectedSets(groupIds);

    // bien que le up s'affiche en popup, le mecanisme de fermeture est
    // assure par le composant=> il est donc necessaire d'indiquer
    // a l'UserPanelPeas de ne pas s'occuper de cette fermeture!
    selection.setHostPath(null);
    Pair<String, String> hostComponentName = new Pair<>(getComponentLabel(), null);
    selection.setHostComponentName(hostComponentName);
    selection.setHostSpaceName(getSpaceLabel());
    selection.setPopupMode(true);
    selection.setHtmlFormElementId(goFunction);
    selection.setHtmlFormName("dummy");

    // Add extra params
    selection.setExtraParams(sug);
    return Selection.getSelectionURL();
  }

  public String initSelectionSenders(List<String> userIds, List<String> groupIds)
      throws FormsOnlineDatabaseException {
    return initSelection(userIds, groupIds, USER_PANEL_SENDERS_PREFIX);
  }

  public String initSelectionReceivers(List<String> userIds, List<String> groupIds)
      throws FormsOnlineDatabaseException {
    return initSelection(userIds, groupIds, USER_PANEL_RECEIVERS_PREFIX);
  }

  private String initSelection(final List<String> userIds, final List<String> groupIds,
      final String userPanelReceiversPrefix) {
    ArrayList<String> profiles = new ArrayList<>();
    profiles.add("SenderReceiver");
    SelectionUsersGroups userGroupSelection = new SelectionUsersGroups();
    userGroupSelection.setComponentId(getComponentId());
    userGroupSelection.setProfileNames(profiles);

    return initSelection(userGroupSelection, userPanelReceiversPrefix, userIds, groupIds);
  }

  public void publishForm(String formId) throws FormsOnlineDatabaseException {
    if (!isAdmin()) {
      throwForbiddenException(UPDATE_CURRENT_FORM);
    }
    getService().publishForm(getFormPK(formId));
  }

  public void unpublishForm(String formId) throws FormsOnlineDatabaseException {
    if (!isAdmin()) {
      throwForbiddenException(UPDATE_CURRENT_FORM);
    }
    getService().unpublishForm(getFormPK(formId));
  }

  public List<FormDetail> getAvailableFormsToSend() throws FormsOnlineDatabaseException {
    return getService().getAvailableFormsToSend(getComponentId(), getUserId());
  }

  public void saveRequest(List<FileItem> items)
      throws FormsOnlineDatabaseException, PublicationTemplateException, FormException {
    if (!getCurrentForm().isSender(getUserId())) {
      throwForbiddenException("saveRequest");
    }
    getService().saveRequest(getCurrentForm().getPK(), getUserId(), items);
  }

  public List<String> getAvailableFormIdsAsReceiver() throws FormsOnlineDatabaseException {
    String userId = getUserId();
    String[] userGroupIds = getOrganisationController().getAllGroupIdsOfUser(userId);
    return dao.getAvailableFormIdsAsReceiver(getComponentId(), userId, userGroupIds);
  }

  public FormInstance loadRequest(String id)
      throws FormsOnlineDatabaseException, PublicationTemplateException, FormException {
    FormInstance request = getService().loadRequest(getRequestPK(id), getUserId());

    FormDetail form = request.getForm();
    if (!request.getCreatorId().equals(getUserId()) && !form.isValidator(getUserId())) {
      throwForbiddenException(LOAD_REQUEST);
    }

    setCurrentForm(form);
    return request;
  }

  public void updateValidationStatus(String requestId, String decision, String comments)
      throws FormsOnlineDatabaseException {
    if (!getCurrentForm().isValidator(getUserId())) {
      throwForbiddenException(LOAD_REQUEST);
    }
    getService().setValidationStatus(getRequestPK(requestId), getUserId(), decision, comments);
  }

  public void archiveRequest(String id) throws FormsOnlineDatabaseException {
    getService().archiveRequest(getRequestPK(id));
  }

  public void deleteRequest(String id)
      throws FormsOnlineDatabaseException, FormException, PublicationTemplateException {
    FormInstance request = getService().loadRequest(getRequestPK(id), getUserId());
    FormDetail form = request.getForm();
    if (!form.isValidator(getUserId())) {
      throwForbiddenException(LOAD_REQUEST);
    }
    getService().deleteRequest(getRequestPK(id));
  }

  public int deleteRequests(Set<String> ids)
      throws PublicationTemplateException, FormsOnlineDatabaseException, FormException {
    int nbDeletedRequests = 0;
    if (ids != null) {
      for (String id : ids) {
        deleteRequest(id);
        nbDeletedRequests++;
      }
    }
    if (nbDeletedRequests > 0) {
      MessageNotifier.addSuccess(getString("formsOnline.requests.action.delete.succeed"),
          nbDeletedRequests);
    }
    return nbDeletedRequests;
  }

  /**
   * Gets an instance of PublicationTemplateManager.
   * @return an instance of PublicationTemplateManager.
   */
  private PublicationTemplateManager getPublicationTemplateManager() {
    return PublicationTemplateManager.getInstance();
  }

  public List<PublicationTemplate> getTemplates() {
    List<PublicationTemplate> templates = new ArrayList<>();
    try {
      GlobalContext aContext = new GlobalContext(getSpaceId(), getComponentId());
      templates = getPublicationTemplateManager().getPublicationTemplates(aContext);
    } catch (PublicationTemplateException e) {
      SilverLogger.getLogger(this).error(e);
    }
    return templates;
  }

  public RequestsByStatus getAllUserRequests() throws FormsOnlineDatabaseException {
    return getService().getAllUserRequests(getComponentId(), getUserId(), null);
  }

  public RequestsByStatus getHomepageValidatorRequests() throws FormsOnlineDatabaseException {
    return getService().getValidatorRequests(getComponentId(), isWorkgroupEnabled(), getUserId(),
        new PaginationPage(1, DEFAULT_ITEM_PER_PAGE));
  }

  public RequestsByStatus getAllValidatorRequests() throws FormsOnlineDatabaseException {
    return getService()
        .getValidatorRequests(getComponentId(), isWorkgroupEnabled(), getUserId(), null);
  }

  public ComponentInstLight getComponentInstLight() {
    return getOrganisationController().getComponentInstLight(getComponentId());
  }

  private FormsOnlineService getService() {
    return FormsOnlineService.get();
  }

  private FormPK getFormPK(int id) {
    return getFormPK(Integer.toString(id));
  }

  private FormPK getFormPK(String id) {
    return new FormPK(id, getComponentId());
  }

  private RequestPK getRequestPK(String id) {
    return new RequestPK(id, getComponentId());
  }

  private void throwForbiddenException(String method) {
    throw new ForbiddenRuntimeException("FormsOnlineSessionController." + method,
        ForbiddenRuntimeException.WARNING, "User is not allowed to do this operation !");
  }

  /* getFlag */
  public String getBestProfile() {
    String flag = "SenderReceiver";
    for (String profile : getUserRoles()) {
      // if Administrator, return it, we won't find a better profile
      if ("Administrator".equals(profile)) {
        return profile;
      }
    }
    return flag;
  }

  private boolean isAdmin() {
    return "Administrator".equals(getBestProfile());
  }

  private boolean isWorkgroupEnabled() {
    return StringUtil.getBooleanValue(
        getComponentParameterValue(FormsOnlineComponentSettings.PARAM_WORKGROUP));
  }

  public Set<String> getSelectedValidatorRequestIds() {
    return selectedValidatorRequestIds;
  }
}