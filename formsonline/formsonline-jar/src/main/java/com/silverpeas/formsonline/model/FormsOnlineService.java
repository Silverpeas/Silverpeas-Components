package com.silverpeas.formsonline.model;

import com.silverpeas.SilverpeasComponentService;
import com.silverpeas.form.FormException;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import org.apache.commons.fileupload.FileItem;

import java.util.List;

public interface FormsOnlineService extends SilverpeasComponentService<FormInstance> {

  public List<FormDetail> getAllForms(String appId, String userId, boolean withSendInfo)
      throws FormsOnlineDatabaseException;

  public FormDetail loadForm(FormPK pk) throws FormsOnlineDatabaseException;

  public FormDetail storeForm(FormDetail form, String[] senderUserIds, String[] senderGroupIds,
      String[] receiverUserIds, String[] receiverGroupIds) throws FormsOnlineDatabaseException;

  public void deleteForm(FormPK pk) throws FormsOnlineDatabaseException;

  public void publishForm(FormPK pk) throws FormsOnlineDatabaseException;

  public void unpublishForm(FormPK pk) throws FormsOnlineDatabaseException;

  public List<FormDetail> getAvailableFormsToSend(String appId, String userId)
      throws FormsOnlineDatabaseException;

  public RequestsByStatus getAllUserRequests(String appId, String userId)
      throws FormsOnlineDatabaseException;

  public List<FormInstance> getUserRequestsByForm(FormPK pk, String userId)
      throws FormsOnlineDatabaseException;

  public RequestsByStatus getAllValidatorRequests(String appId, boolean allRequests, String userId)
      throws FormsOnlineDatabaseException;

  public List<String> getAvailableFormIdsAsReceiver(String appId, String userId)
      throws FormsOnlineDatabaseException;

  public FormInstance loadRequest(RequestPK pk, String userId)
      throws FormsOnlineDatabaseException, PublicationTemplateException, FormException;

  public void saveRequest(FormPK pk, String userId, List<FileItem> items)
      throws FormsOnlineDatabaseException, PublicationTemplateException, FormException;

  public void setValidationStatus(RequestPK pk, String userId, String decision, String comments)
      throws FormsOnlineDatabaseException;

  public void archiveRequest(RequestPK pk) throws FormsOnlineDatabaseException;

  public void deleteRequest(RequestPK pk)
      throws FormsOnlineDatabaseException, FormException, PublicationTemplateException;
}
