/*
 * Copyright (C) 2000 - 2020 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General License as
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
 * GNU Affero General License for more details.
 *
 * You should have received a copy of the GNU Affero General License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.formsonline.model;

import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.util.SilverpeasList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FormsOnlineDAO {

  /**
   * Get all forms that has been created in given instance
   * @param instanceId the instance id
   * @return a List of FormDetail object
   */
  List<FormDetail> findAllForms(String instanceId) throws FormsOnlineException;

  /**
   * Load forms from database with given instance Id and form id
   * @param pk the form primary key
   * @return a FormDetail object
   */
  FormDetail getForm(FormPK pk) throws FormsOnlineException;

  /**
   * Save new form in database
   * @param formDetail the form detail
   * @return the created FormDetail
   * @throws FormsOnlineException
   */
  FormDetail createForm(FormDetail formDetail) throws FormsOnlineException;

  /**
   * Update form in database
   * @param formDetail the form detail
   * @return a List of FormDetail object
   * @throws FormsOnlineException
   */
  void updateForm(FormDetail formDetail) throws FormsOnlineException;

  /**
   * Delete Form from database
   * @param pk the pk of form to be deleted
   * @return the deleted FormDetail
   * @throws FormsOnlineException
   */
  FormDetail deleteForm(FormPK pk) throws FormsOnlineException;

  /**
   * Update form senders list.
   * @param pk the form primary key
   * @param newUserSenderIds the new sender list as user ids
   * @param newGroupSenderIds the new sender list as group ids
   * @throws FormsOnlineException
   */
  void updateSenders(FormPK pk, String[] newUserSenderIds, String[] newGroupSenderIds)
      throws FormsOnlineException;

  /**
   * Update form receivers list.
   * @param pk the form primary key
   * @param newUserReceiverIds the new receivers list as user ids
   * @param newGroupReceiverIds the new receivers list as group ids
   * @throws FormsOnlineException
   */
  void updateReceivers(FormPK pk, String[] newUserReceiverIds, String[] newGroupReceiverIds,
      String rightType) throws FormsOnlineException;

  /**
   * Get the form's senders list where users has been declared directly.
   * @param pk the form primary key
   * @return user ids as a list of String
   * @throws FormsOnlineException
   */
  List<String> getSendersAsUsers(FormPK pk)
      throws FormsOnlineException;

  /**
   * Get the form's senders list where groups has been declared directly.
   * @param pk the form primary key
   * @return group ids as a list of String
   * @throws FormsOnlineException
   */
  List<String> getSendersAsGroups(FormPK pk) throws FormsOnlineException;

  /**
   * Get the form's receivers list where users has been declared directly.
   * @param pk the form primary key
   * @return user ids as a list of String
   * @throws FormsOnlineException
   */
  List<String> getReceiversAsUsers(FormPK pk, String rightType) throws FormsOnlineException;

  /**
   * Get the form's receivers list where groups has been declared directly.
   * @param pk the form primary key
   * @return group ids as a list of String
   * @throws FormsOnlineException
   */
  List<String> getReceiversAsGroups(FormPK pk, String rightType) throws FormsOnlineException;

  /**
   * Get the form available to be sent for given user or given groups
   * @param componentIds the component instance id
   * @param userId the user id
   * @param userGroupIds the user's groups id list
   * @return a list of FormDetail objects
   * @throws FormsOnlineException
   */
  List<FormDetail> getUserAvailableForms(Collection<String> componentIds, String userId,
      String[] userGroupIds) throws FormsOnlineException;

  /**
   * Get all form instances that have been sent by given user (excepted the ones that have been
   * archived.
   * @param pk the form primary key
   * @param userId the user id
   * @param states the states to filter on if any
   * @param paginationPage pagination which can be null if no pagination is
   * requested.
   * @return a list of FormInstance objects
   */
  SilverpeasList<FormInstance> getSentFormInstances(FormPK pk, String userId,
      final List<Integer> states, final PaginationPage paginationPage) throws FormsOnlineException;

  /**
   * Get all requests associated to given form ordered from the newest to the older.
   * @param form the form primary key
   * @param allRequests true to get all request of any state, false to get only request to validate
   * @param validatorId the user id
   * @param senderIdsManagedByValidator sender ids which are managed by the given validator
   * @param states the states to filter on if any
   * @param paginationPage pagination which can be null if no pagination is
   * requested.
   * @return if allRequests is false only requests to validate and requests validated by given
   * user are returned. If true, all requests (validated or not) are returned.
   */
  SilverpeasList<FormInstance> getReceivedRequests(FormDetail form, boolean allRequests,
      String validatorId, final Set<String> senderIdsManagedByValidator,
      final List<Integer> states, final PaginationPage paginationPage) throws FormsOnlineException;

  List<String> getAvailableFormIdsAsReceiver(String instanceId, String userId,
      String[] userGroupIds) throws FormsOnlineException;

  FormInstance getRequest(RequestPK pk) throws FormsOnlineException;

  List<FormDetail> getForms(Collection<String> formIds) throws FormsOnlineException;

  /**
   * Inserts or updates the given request (also called a form instance).
   * <p>
   * All validation data contained into {@link FormInstance#getValidations()} are also inserted
   * or updated.
   * </p>
   * @param request the request to insert or update.
   * @return the {@link FormInstance} itself.
   * @throws FormsOnlineException on database integrity error.
   */
  FormInstance saveRequest(FormInstance request) throws FormsOnlineException;

  /**
   * Saves the state of the given request without updating anything else.
   * @param request the request which the state MUST be updated.
   * @throws FormsOnlineException if the form instance id does not exist.
   */
  void saveRequestState(FormInstance request) throws FormsOnlineException;

  void deleteRequest(RequestPK pk) throws FormsOnlineException;

  Map<Integer, Integer> getNumberOfRequestsByForm(String instanceId)
      throws FormsOnlineException;

  SilverpeasList<FormInstance> getAllRequests(FormPK pk) throws FormsOnlineException;
}
