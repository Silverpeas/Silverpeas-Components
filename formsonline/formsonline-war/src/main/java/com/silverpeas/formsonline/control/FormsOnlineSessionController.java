/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.formsonline.control;

import com.silverpeas.accesscontrol.ForbiddenRuntimeException;
import com.silverpeas.form.FormException;
import com.silverpeas.formsonline.FormsOnlineComponentSettings;
import com.silverpeas.formsonline.model.FormDetail;
import com.silverpeas.formsonline.model.FormInstance;
import com.silverpeas.formsonline.model.FormPK;
import com.silverpeas.formsonline.model.FormsOnlineDAO;
import com.silverpeas.formsonline.model.FormsOnlineDatabaseException;
import com.silverpeas.formsonline.model.FormsOnlineService;
import com.silverpeas.formsonline.model.RequestPK;
import com.silverpeas.formsonline.model.RequestsByStatus;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.util.GeneralPropertiesManager;
import org.silverpeas.util.GlobalContext;
import org.silverpeas.util.NotifierUtil;
import org.silverpeas.util.Pair;
import org.silverpeas.util.ServiceProvider;
import org.silverpeas.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class FormsOnlineSessionController extends AbstractComponentSessionController {

  private FormsOnlineDAO dao = ServiceProvider.getService(FormsOnlineDAO.class);
  private FormDetail currentForm;
  protected Selection m_Selection = null;

  public final static String userPanelSendersPrefix = "listSenders";
  public final static String userPanelReceiversPrefix = "listReceivers";

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public FormsOnlineSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.formsonline.multilang.formsOnlineBundle",
        "org.silverpeas.formsonline.settings.formsOnlineIcons",
        "org.silverpeas.formsonline.settings.formsOnlineSettings");
    m_Selection = getSelection();
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
      throwForbiddenException("updateCurrentForm");
    }

    if (currentForm.getId() == -1) {
      currentForm.setCreatorId(getUserId());
      currentForm.setInstanceId(getComponentId());
      NotifierUtil.addInfo(getString("formsOnline.form.creation.succeed"));
    } else {
      NotifierUtil.addSuccess("formsOnline.form.update.succeed");
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
      throwForbiddenException("updateCurrentForm");
    }
    getService().deleteForm(getFormPK(formId));
  }

  // initialisation de Selection pour nav vers SelectionPeas
  private String initSelection(SelectionUsersGroups sug, String goFunction, List<String> userIds,
      List<String> groupIds) {
    String url = GeneralPropertiesManager.getString("ApplicationURL") +
        URLManager.getURL(getSpaceId(), getComponentId());
    String goUrl = url + goFunction;
    String cancelUrl = url + "SendersReceivers";

    m_Selection.resetAll();

    m_Selection.setGoBackURL(goUrl);
    m_Selection.setCancelURL(cancelUrl);

    m_Selection.setSelectedElements(userIds);
    m_Selection.setSelectedSets(groupIds);

    // bien que le up s'affiche en popup, le mecanisme de fermeture est
    // assure par le composant=> il est donc necessaire d'indiquer
    // a l'UserPanelPeas de ne pas s'occuper de cette fermeture!
    m_Selection.setHostPath(null);
    Pair<String, String> hostComponentName = new Pair<>(getComponentLabel(), null);
    m_Selection.setHostComponentName(hostComponentName);
    m_Selection.setHostSpaceName(getSpaceLabel());
    m_Selection.setFirstPage(Selection.FIRST_PAGE_BROWSE);
    m_Selection.setPopupMode(true);
    m_Selection.setHtmlFormElementId(goFunction);
    m_Selection.setHtmlFormName("dummy");

    // Add extra params
    m_Selection.setExtraParams(sug);
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  public String initSelectionSenders(List<String> userIds, List<String> groupIds)
      throws FormsOnlineDatabaseException {
    ArrayList<String> profiles = new ArrayList<String>();
    profiles.add("SenderReceiver");
    SelectionUsersGroups sugSenders = new SelectionUsersGroups();
    sugSenders.setComponentId(getComponentId());
    sugSenders.setProfileNames(profiles);

    return initSelection(sugSenders, userPanelSendersPrefix, userIds, groupIds);
  }

  public String initSelectionReceivers(List<String> userIds, List<String> groupIds)
      throws FormsOnlineDatabaseException {
    ArrayList<String> profiles = new ArrayList<String>();
    profiles.add("SenderReceiver");
    SelectionUsersGroups sugReceivers = new SelectionUsersGroups();
    sugReceivers.setComponentId(getComponentId());
    sugReceivers.setProfileNames(profiles);

    return initSelection(sugReceivers, userPanelReceiversPrefix, userIds, groupIds);
  }

  public void publishForm(String formId) throws FormsOnlineDatabaseException {
    if (!isAdmin()) {
      throwForbiddenException("updateCurrentForm");
    }
    getService().publishForm(getFormPK(formId));
  }

  public void unpublishForm(String formId) throws FormsOnlineDatabaseException {
    if (!isAdmin()) {
      throwForbiddenException("updateCurrentForm");
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
      throwForbiddenException("loadRequest");
    }

    setCurrentForm(form);
    return request;
  }

  public void updateValidationStatus(String requestId, String decision, String comments)
      throws FormsOnlineDatabaseException {
    if (!getCurrentForm().isValidator(getUserId())) {
      throwForbiddenException("loadRequest");
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
      throwForbiddenException("loadRequest");
    }
    getService().deleteRequest(getRequestPK(id));
  }

  public int deleteRequests(String[] ids)
      throws PublicationTemplateException, FormsOnlineDatabaseException, FormException {
    int nbDeletedRequests = 0;
    if (ids != null) {
      for (String id : ids) {
        deleteRequest(id);
        nbDeletedRequests++;
      }
    }
    if (nbDeletedRequests > 0) {
      NotifierUtil.addSuccess(getString("formsOnline.requests.action.delete.succeed"),
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
    List<PublicationTemplate> templates = new ArrayList<PublicationTemplate>();
    try {
      GlobalContext aContext = new GlobalContext(getSpaceId(), getComponentId());
      templates = getPublicationTemplateManager().getPublicationTemplates(aContext);
    } catch (PublicationTemplateException e) {
      SilverTrace.error("formManager", "FormsOnlineSessionController.getForms()",
          "root.CANT_GET_FORMS", e);
    }
    return templates;
  }

  public RequestsByStatus getAllUserRequests() throws FormsOnlineDatabaseException {
    return getService().getAllUserRequests(getComponentId(), getUserId());
  }

  public RequestsByStatus getAllValidatorRequests() throws FormsOnlineDatabaseException {
    return getService().getAllValidatorRequests(getComponentId(), isWorkgroupEnabled(),
        getUserId());
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
      if (profile.equals("Administrator")) {
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

}