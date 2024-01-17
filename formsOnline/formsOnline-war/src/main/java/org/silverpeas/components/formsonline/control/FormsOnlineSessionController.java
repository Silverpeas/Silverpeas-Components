/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.formsonline.control;

import net.htmlparser.jericho.Source;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.components.formsonline.FormsOnlineComponentSettings;
import org.silverpeas.components.formsonline.model.*;
import org.silverpeas.core.SilverpeasException;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.GlobalContext;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.notification.message.MessageNotifier;
import org.silverpeas.core.security.authorization.ForbiddenRuntimeException;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.csv.CSVRow;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.export.ExportCSVBuilder;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.webcomponent.WebMessager;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.selection.SelectionUsersGroups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.singleton;
import static org.silverpeas.components.formsonline.model.FormDetail.*;

public class FormsOnlineSessionController extends AbstractComponentSessionController {

  private static final int DEFAULT_ITEM_PER_PAGE = 10;
  private static final String UPDATE_CURRENT_FORM = "updateCurrentForm";
  private static final String LOAD_REQUEST = "loadRequest";
  private FormDetail currentForm;
  private int currentStateFilter;
  private FormInstanceValidationType currentValidationTypeFilter;
  private Selection selection = null;
  private Set<String> selectedValidatorRequestIds = new HashSet<>();
  private Map<Integer, String> statusLabels = new HashMap<>();

  public static final String USER_PANEL_SENDERS_PREFIX = "listSenders";
  public static final String USER_PANEL_RECEIVERS_PREFIX = "listReceivers";
  public static final String USER_PANEL_INTERMEDIATE_RECEIVERS_PREFIX = "listIntermediateReceivers";

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   */
  public FormsOnlineSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        FormsOnlineComponentSettings.MESSAGES_PATH,
        "org.silverpeas.formsonline.settings.formsOnlineIcons",
        FormsOnlineComponentSettings.SETTINGS_PATH);
    selection = getSelection();
    loadStatusLabels();
    setCurrentFilter(-1, null);
  }

  public List<FormDetail> getAllForms(boolean withSendInfo) throws FormsOnlineException {
    return getService().getAllForms(getComponentId(), getUserId(), withSendInfo);
  }

  public void setCurrentForm(FormDetail form) {
    this.currentForm = form;
  }

  public void resetCurrentForm() {
    this.currentForm = null;
  }

  public FormDetail setCurrentForm(String id) throws FormsOnlineException {
    if (StringUtil.isNotDefined(id)) {
      resetCurrentForm();
      return null;
    } else {
      setCurrentForm(loadForm(Integer.parseInt(id)));
      return getCurrentForm();
    }
  }

  public FormDetail getCurrentForm() {
    return this.currentForm;
  }

  public void setCurrentFilter(int state, FormInstanceValidationType validationType) {
    if (validationType != null) {
      this.currentStateFilter = -1;
      this.currentValidationTypeFilter = validationType;
    } else {
      this.currentStateFilter = state;
      this.currentValidationTypeFilter = null;
    }
  }

  public int getCurrentStateFilter() {
    return currentStateFilter;
  }

  public FormInstanceValidationType getCurrentValidationTypeFilter() {
    return currentValidationTypeFilter;
  }

  public void saveCurrentForm(List<String> senderUserIds, List<String> senderGroupIds,
      List<String> intermediateReceiverUserIds, List<String> intermediateReceiverGroupIds,
      List<String> receiverUserIds, List<String> receiverGroupIds) throws FormsOnlineException {
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
    final Map<String, Pair<List<String>, List<String>>> userAndGroupIdsByRightTypes = new HashMap<>();
    userAndGroupIdsByRightTypes.put(SENDERS_TYPE, Pair.of(senderUserIds, senderGroupIds));
    userAndGroupIdsByRightTypes.put(RECEIVERS_TYPE_INTERMEDIATE, Pair.of(intermediateReceiverUserIds, intermediateReceiverGroupIds));
    userAndGroupIdsByRightTypes.put(RECEIVERS_TYPE_FINAL, Pair.of(receiverUserIds, receiverGroupIds));
    currentForm = getService().saveForm(currentForm, userAndGroupIdsByRightTypes);
  }

  private FormDetail loadForm(int formId) throws FormsOnlineException {
    return getService().loadForm(getFormPK(formId));
  }

  public void deleteForm(int formId) throws FormsOnlineException {
    if (!isAdmin()) {
      throwForbiddenException(UPDATE_CURRENT_FORM);
    }
    boolean reallyDeleted = getService().deleteForm(getFormPK(formId));
    if (!reallyDeleted) {
      MessageNotifier.addError(getString("formsOnline.form.deletion.failed"));
    } else {
      MessageNotifier.addSuccess(getString("formsOnline.form.deletion.succeed"));
    }
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

  public String initSelectionSenders(List<String> userIds, List<String> groupIds) {
    return initSelection(userIds, groupIds, USER_PANEL_SENDERS_PREFIX);
  }

  public String initSelectionReceivers(List<String> userIds, List<String> groupIds) {
    return initSelection(userIds, groupIds, USER_PANEL_RECEIVERS_PREFIX);
  }

  public String initSelectionIntermediateReceivers(List<String> userIds, List<String> groupIds) {
    return initSelection(userIds, groupIds, USER_PANEL_INTERMEDIATE_RECEIVERS_PREFIX);
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

  public void publishForm(String formId) throws FormsOnlineException {
    if (!isAdmin()) {
      throwForbiddenException(UPDATE_CURRENT_FORM);
    }
    final FormDetail form = loadForm(Integer.parseInt(formId));
    if (!form.isDeleteAfterRequestExchange() && !form.isFinalValidation()) {
      WebMessager.getInstance()
          .addError(getString("formsOnline.publishForm.error.finalValidatorMissing"),
              form.getTitle());
      return;
    }
    getService().publishForm(form.getPK());
    final String message;
    if (form.isNotYetPublished()) {
      message = getString("formsOnline.publishForm.success");
    } else {
      message = getString("formsOnline.republishForm.success");
    }
    WebMessager.getInstance().addSuccess(message, form.getTitle());
  }

  public void unpublishForm(String formId) throws FormsOnlineException {
    if (!isAdmin()) {
      throwForbiddenException(UPDATE_CURRENT_FORM);
    }
    final FormDetail form = loadForm(Integer.parseInt(formId));
    getService().unpublishForm(form.getPK());
    WebMessager.getInstance().addSuccess(getString("formsOnline.unpublishForm.success"), form.getTitle());
  }

  public List<FormDetail> getAvailableFormsToSend() throws FormsOnlineException {
    String orderBy = getOrganisationController().getComponentParameterValue(getComponentId(), "displaySort");
    orderBy = StringUtil.isDefined(orderBy) ? orderBy: "name asc";

    return getService().getAvailableFormsToSend(singleton(getComponentId()), getUserId(), orderBy);
  }

  public void saveRequest(List<FileItem> items, boolean draft) throws FormsOnlineException {
    if (!getCurrentForm().canBeSentBy(User.getCurrentRequester())) {
      throwForbiddenException("saveRequest");
    }
    getService().saveRequest(getCurrentForm().getPK(), getUserId(), items, draft);
  }

  public Set<String> getAvailableFormIdsAsReceiver() throws FormsOnlineException {
    return getService().getValidatorFormIdsWithValidationTypes(getComponentId(), getUserId(), null).keySet();
  }

  public FormInstance loadRequest(String id, boolean editionMode) throws FormsOnlineException {
    FormInstance request = getService().loadRequest(getRequestPK(id), getUserId(), editionMode);

    FormDetail form = request.getForm();
    if (!request.canBeAccessedBy(User.getCurrentRequester())) {
      throwForbiddenException(LOAD_REQUEST);
    }

    setCurrentForm(form);
    return request;
  }

  public void updateValidationStatus(String requestId, String decision, String comments,
      boolean follower) throws FormsOnlineException {
    getService().saveNextRequestValidationStep(getRequestPK(requestId), getUserId(), decision, comments, follower);
  }

  public void cancelRequest(String id) throws FormsOnlineException {
    getService().cancelRequest(getRequestPK(id));
  }

  public void archiveRequest(String id, boolean notify) throws FormsOnlineException {
    getService().archiveRequest(getRequestPK(id));
    if (notify) {
      MessageNotifier.addSuccess(getString("formsOnline.requests.action.archive.succeed"),
          1);
    }
  }

  public int archiveRequests(Set<String> ids) throws FormsOnlineException {
    int nbRequests = 0;
    if (ids != null) {
      for (String id : ids) {
        archiveRequest(id, false);
        nbRequests++;
      }
    }
    if (nbRequests > 0) {
      MessageNotifier.addSuccess(getString("formsOnline.requests.action.archive.succeed"),
          nbRequests);
    }
    return nbRequests;
  }

  public void deleteRequest(String id, boolean notify) throws FormsOnlineException {
    getService().deleteRequest(getRequestPK(id));
    if (notify) {
      MessageNotifier.addSuccess(getString("formsOnline.requests.action.delete.succeed"),
          1);
    }
  }

  public int deleteRequests(Set<String> ids) throws FormsOnlineException {
    int nbDeletedRequests = 0;
    if (ids != null) {
      for (String id : ids) {
        deleteRequest(id, false);
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

  public RequestsByStatus getAllUserRequests() throws FormsOnlineException {
    return getService().getAllUserRequests(getComponentId(), getUserId(), null);
  }

  public RequestsByStatus getHomepageValidatorRequests() throws FormsOnlineException {
    return getService().getValidatorRequests(getRequestsFilter(), getUserId(),
        new PaginationPage(1, DEFAULT_ITEM_PER_PAGE));
  }

  public RequestsByStatus getAllValidatorRequests() throws FormsOnlineException {
    RequestsFilter filter = getRequestsFilter();
    if (getCurrentForm() != null) {
      filter.getFormIds().add(Integer.toString(getCurrentForm().getId()));
    }
    filter.setState(getCurrentStateFilter());
    filter.setPendingValidationType(getCurrentValidationTypeFilter());
    return getService().getValidatorRequests(filter, getUserId(), null);
  }

  public Form getCurrentEmptyForm() throws SilverpeasException {
    PublicationTemplate pubTemplate = getCurrentPublicationTemplate(true);
    return getEmptyForm(pubTemplate);
  }

  public Form getEmptyForm(String xmlFormName) throws SilverpeasException {
    PublicationTemplate template = getPublicationTemplate(xmlFormName,true);
    return getEmptyForm(template);
  }

  private Form getEmptyForm(PublicationTemplate template) throws SilverpeasException {
    Form form;
    try {
      // form and DataRecord creation
      form = template.getUpdateForm();
      RecordSet recordSet = template.getRecordSet();
      form.setData(recordSet.getEmptyRecord());
    } catch (Exception e) {
      throw new SilverpeasException("Can't load form "+getCurrentForm().getXmlFormName(), e);
    }
    return form;
  }

  public ExportCSVBuilder export() throws SilverpeasException {
    ExportCSVBuilder csvBuilder = new ExportCSVBuilder();
    CSVRow csvHeader = new CSVRow();

    boolean isHierarchicalValidation = getCurrentForm().isHierarchicalValidation();
    boolean isIntermediateValidation = getCurrentForm().isIntermediateValidation();

    // adding columns relative to request metadata
    List<String> csvCols = new ArrayList<>();
    csvCols.add("id");
    csvHeader.addCell("Id");
    csvCols.add("status");
    csvHeader.addCell(getString("GML.status"));
    csvCols.add("creationDate");
    csvHeader.addCell(getString("formsOnline.sendDate"));
    csvCols.add("requester");
    csvHeader.addCell(getString("formsOnline.sender"));

    String labelPrefix;
    String idPrefix;
    if (isHierarchicalValidation) {
      // adding columns of hierarchical validation
      labelPrefix = getString("formsOnline.requests.array.col.vh.label") + " - ";
      idPrefix = "boss-";
      addColumnsToExport(csvCols, csvHeader, idPrefix, labelPrefix);
    }

    if (isIntermediateValidation) {
      // adding columns of intermediate validation
      labelPrefix = getString("formsOnline.requests.array.col.vi.label") + " - ";
      idPrefix = "inter-";
      addColumnsToExport(csvCols, csvHeader, idPrefix, labelPrefix);
    }

    // adding columns of finale validation
    labelPrefix = getString("formsOnline.requests.array.col.vf.label") + " - ";
    addColumnsToExport(csvCols, csvHeader, "",labelPrefix);

    int nbMetaDataCols = csvCols.size();

    // adding columns relative to request content
    RecordSet recordSet;
    FieldTemplate[] fields;
    try {
      PublicationTemplate template = getCurrentPublicationTemplate(false);
      recordSet = template.getRecordSet();
      fields = template.getRecordTemplate().getFieldTemplates();
    } catch (Exception e) {
      throw new SilverpeasException("Can't load form '"+getCurrentForm().getXmlFormName()+"'", e);
    }
    for (FieldTemplate field : fields) {
      csvCols.add(field.getFieldName());
      csvHeader.addCell(field.getLabel(getLanguage()));
    }
    csvBuilder.setHeader(csvHeader);

    // getting rows
    RequestsByStatus requestsByStatus = getAllValidatorRequests();
    List<FormInstance> requests = requestsByStatus.getAll();
    for (FormInstance request : requests) {
      CSVRow csvRow = new CSVRow();

      csvRow.addCell(request.getId());
      csvRow.addCell(statusLabels.get(request.getState()));
      csvRow.addCell(request.getCreationDate());
      csvRow.addCell(request.getCreator());

      if (isHierarchicalValidation) {
        Optional<FormInstanceValidation> validation = request.getValidations().getHierarchicalValidation();
        addValidationDataToExport(validation, csvRow);
      }

      if (isIntermediateValidation) {
        Optional<FormInstanceValidation> validation = request.getValidations().getIntermediateValidation();
        addValidationDataToExport(validation, csvRow);
      }

      Optional<FormInstanceValidation> validation = request.getValidations().getFinalValidation();
      addValidationDataToExport(validation, csvRow);

      addRequestDataToExport(recordSet, request, nbMetaDataCols, csvCols, csvRow);

      csvBuilder.addLine(csvRow);
    }

    return csvBuilder;
  }

  private void addColumnsToExport(List<String> csvCols, CSVRow csvHeader, String idPrefix,
      String labelPrefix) {
    csvCols.add(idPrefix+"validator");
    csvHeader.addCell(labelPrefix + getString("formsOnline.request.process.user"));
    csvCols.add(idPrefix+"processDate");
    csvHeader.addCell(labelPrefix + getString("GML.date"));
    csvCols.add(idPrefix+"comment");
    csvHeader.addCell(labelPrefix + getString("GML.comments"));
  }

  private void addValidationDataToExport(Optional<FormInstanceValidation> validation, CSVRow csvRow) {
    if (validation.isPresent()) {
      csvRow.addCell(validation.get().getValidator());
      csvRow.addCell(validation.get().getDate());
      csvRow.addCell(validation.get().getComment());
    } else {
      csvRow.addCell("");
      csvRow.addCell("");
      csvRow.addCell("");
    }
  }

  private void addRequestDataToExport(RecordSet recordSet, FormInstance request, int nbMetaDataCols,
      List<String> csvCols, CSVRow csvRow) {
    DataRecord data = null;
    try {
      data = recordSet.getRecord(request.getId());
    } catch (Exception e) {
      SilverLogger.getLogger(this).error("RequestId = "+request.getId(), e);
    }
    if (data != null) {
      Map<String, String> values = data.getValues(getLanguage());

      for (int i=nbMetaDataCols; i<csvCols.size(); i++) {
        String value = values.getOrDefault(csvCols.get(i), "");
        // removing all HTML
        value = new Source(value).getTextExtractor().toString();
        csvRow.addCell(value);
      }
    }
  }

  public ComponentInstLight getComponentInstLight() {
    return getOrganisationController().getComponentInstLight(getComponentId());
  }

  public Set<String> getSelectedValidatorRequestIds() {
    return selectedValidatorRequestIds;
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
    throw new ForbiddenRuntimeException(
        "User is not allowed to do the following operation: " + method);
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

  private RequestsFilter getRequestsFilter() {
    return new RequestsFilter(getComponentId(), isWorkgroupEnabled());
  }

  private PublicationTemplate getCurrentPublicationTemplate(boolean registerIt)
      throws SilverpeasException {
    if (getCurrentForm() == null) {
      throw new SilverpeasException("No form currently used !");
    }
    String xmlFormName = getCurrentForm().getXmlFormName();
    return getPublicationTemplate(xmlFormName, registerIt);
  }

  private PublicationTemplate getPublicationTemplate(String xmlFormName, boolean registerIt)
      throws SilverpeasException {
    try {
      String xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName.indexOf('.'));
      if (registerIt) {
        getPublicationTemplateManager()
            .addDynamicPublicationTemplate(getComponentId() + ":" + xmlFormShortName, xmlFormName);
      }
      return getPublicationTemplateManager()
          .getPublicationTemplate(getComponentId()+":"+xmlFormShortName, xmlFormName);
    } catch (Exception e) {
      throw new SilverpeasException("Can't load form '"+xmlFormName+"'", e);
    }
  }

  private void loadStatusLabels() {
    loadStatusLabel(FormInstance.STATE_UNREAD,"formsOnline.stateUnread");
    loadStatusLabel(FormInstance.STATE_READ, "formsOnline.stateRead");
    loadStatusLabel(FormInstance.STATE_VALIDATED,"formsOnline.stateValidated");
    loadStatusLabel(FormInstance.STATE_REFUSED,"formsOnline.stateRefused");
    loadStatusLabel(FormInstance.STATE_ARCHIVED,"formsOnline.stateArchived");
  }

  private void loadStatusLabel(int status, String key) {
    statusLabels.put(Integer.valueOf(status), getString(key));
  }

  public PagesContext getFormPageContext() {
    return new PagesContext("unknown", "0", getLanguage(), false, getComponentId(), getUserId());
  }

}