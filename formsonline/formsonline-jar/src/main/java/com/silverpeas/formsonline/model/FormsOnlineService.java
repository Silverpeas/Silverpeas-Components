/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.formsonline.model;

import com.silverpeas.ApplicationService;
import com.silverpeas.form.FormException;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.util.ServiceProvider;

import java.util.List;

public interface FormsOnlineService extends ApplicationService<FormInstance> {

  public static FormsOnlineService get() {
    return ServiceProvider.getService(FormsOnlineService.class);
  }

  List<FormDetail> getAllForms(String appId, String userId, boolean withSendInfo)
      throws FormsOnlineDatabaseException;

  FormDetail loadForm(FormPK pk) throws FormsOnlineDatabaseException;

  FormDetail storeForm(FormDetail form, String[] senderUserIds, String[] senderGroupIds,
      String[] receiverUserIds, String[] receiverGroupIds) throws FormsOnlineDatabaseException;

  void deleteForm(FormPK pk) throws FormsOnlineDatabaseException;

  void publishForm(FormPK pk) throws FormsOnlineDatabaseException;

  void unpublishForm(FormPK pk) throws FormsOnlineDatabaseException;

  List<FormDetail> getAvailableFormsToSend(String appId, String userId)
      throws FormsOnlineDatabaseException;

  RequestsByStatus getAllUserRequests(String appId, String userId)
      throws FormsOnlineDatabaseException;

  List<FormInstance> getUserRequestsByForm(FormPK pk, String userId)
      throws FormsOnlineDatabaseException;

  RequestsByStatus getAllValidatorRequests(String appId, boolean allRequests, String userId)
      throws FormsOnlineDatabaseException;

  List<String> getAvailableFormIdsAsReceiver(String appId, String userId)
      throws FormsOnlineDatabaseException;

  FormInstance loadRequest(RequestPK pk, String userId)
      throws FormsOnlineDatabaseException, PublicationTemplateException, FormException;

  void saveRequest(FormPK pk, String userId, List<FileItem> items)
      throws FormsOnlineDatabaseException, PublicationTemplateException, FormException;

  void setValidationStatus(RequestPK pk, String userId, String decision, String comments)
      throws FormsOnlineDatabaseException;

  void archiveRequest(RequestPK pk) throws FormsOnlineDatabaseException;

  void deleteRequest(RequestPK pk)
      throws FormsOnlineDatabaseException, FormException, PublicationTemplateException;
}
