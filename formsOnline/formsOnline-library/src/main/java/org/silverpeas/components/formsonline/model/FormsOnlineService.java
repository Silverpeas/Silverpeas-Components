/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.components.formsonline.model;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.ApplicationService;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Collection;
import java.util.List;

public interface FormsOnlineService extends ApplicationService<FormInstance> {

  public static FormsOnlineService get() {
    return ServiceProvider.getService(FormsOnlineService.class);
  }

  List<FormDetail> getAllForms(String appId, String userId, boolean withSendInfo)
      throws FormsOnlineException;

  FormDetail loadForm(FormPK pk) throws FormsOnlineException;

  FormDetail storeForm(FormDetail form, String[] senderUserIds, String[] senderGroupIds,
      String[] receiverUserIds, String[] receiverGroupIds) throws FormsOnlineException;

  boolean deleteForm(FormPK pk) throws FormsOnlineException;

  void publishForm(FormPK pk) throws FormsOnlineException;

  void unpublishForm(FormPK pk) throws FormsOnlineException;

  List<FormDetail> getAvailableFormsToSend(Collection<String> appIds, String userId)
      throws FormsOnlineException;

  RequestsByStatus getAllUserRequests(String appId, String userId,
      final PaginationPage paginationPage)
      throws FormsOnlineException;

  RequestsByStatus getValidatorRequests(RequestsFilter filter, String userId,
      final PaginationPage paginationPage) throws FormsOnlineException;

  List<String> getAvailableFormIdsAsReceiver(String appId, String userId)
      throws FormsOnlineException;

  FormInstance loadRequest(RequestPK pk, String userId)
      throws FormsOnlineException, PublicationTemplateException, FormException;

  void saveRequest(FormPK pk, String userId, List<FileItem> items)
      throws FormsOnlineException, PublicationTemplateException, FormException;

  void setValidationStatus(RequestPK pk, String userId, String decision, String comment)
      throws FormsOnlineException;

  void archiveRequest(RequestPK pk) throws FormsOnlineException;

  void deleteRequest(RequestPK pk)
      throws FormsOnlineException, FormException, PublicationTemplateException;

  void index(String componentId);
}
