/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.formsonline.model;

import java.util.List;

public interface FormsOnlineDAO {

  /**
   * Get all forms that has been created in given instance
   * @param instanceId the instance id
   * @return a List of FormDetail object
   */
  public List<FormDetail> findAllForms(String instanceId) throws FormsOnlineDatabaseException;

  /**
   * Load forms from database with given instance Id and form id
   * @param instanceId the instance id
   * @param formId the form id
   * @return a FormDetail object
   */
  public FormDetail getForm(String instanceId, int formId) throws FormsOnlineDatabaseException;

  /**
   * Save new form in database
   * @param formDetail the form detail
   * @return the created FormDetail
   * @throws FormsOnlineDatabaseException
   */
  public FormDetail createForm(FormDetail formDetail) throws FormsOnlineDatabaseException;

  /**
   * Update form in database
   * @param formDetail the form detail
   * @return a List of FormDetail object
   * @throws FormsOnlineDatabaseException
   */
  public void updateForm(FormDetail formDetail) throws FormsOnlineDatabaseException;

  /**
   * Delete Form from database
   * @param formPK the pk of form to be deleted
   * @return the deleted FormDetail
   * @throws FormsOnlineDatabaseException
   */
  public FormDetail deleteForm(String instanceId, int formId) throws FormsOnlineDatabaseException;

  /**
   * Update form senders list.
   * @param id the form id
   * @param componentId the component instanceId
   * @param newUserSenderIds the new sender list as user ids
   * @param newGroupSenderIds the new sender list as group ids
   * @throws FormsOnlineDatabaseException
   */
  public void updateSenders(int id, String componentId,
      String[] newUserSenderIds, String[] newGroupSenderIds) throws FormsOnlineDatabaseException;

  /**
   * Update form receivers list.
   * @param id the form id
   * @param componentId the component instanceId
   * @param newUserReceiverIds the new receivers list as user ids
   * @param newGroupReceiverIds the new receivers list as group ids
   * @throws FormsOnlineDatabaseException
   */
  public void updateReceivers(int id, String componentId,
      String[] newUserReceiverIds, String[] newGroupReceiverIds)
      throws FormsOnlineDatabaseException;

  /**
   * Get the form's senders list where users has been declared directly.
   * @param id the form id
   * @param componentId the component instance id
   * @return user ids as a list of String
   * @throws FormsOnlineDatabaseException
   */
  public List<String> getSendersAsUsers(int id, String componentId)
      throws FormsOnlineDatabaseException;

  /**
   * Get the form's senders list where groups has been declared directly.
   * @param id the form id
   * @param componentId the component instance id
   * @return group ids as a list of String
   * @throws FormsOnlineDatabaseException
   */
  public List<String> getSendersAsGroups(int id, String componentId)
      throws FormsOnlineDatabaseException;

  /**
   * Get the form's receivers list where users has been declared directly.
   * @param id the form id
   * @param componentId the component instance id
   * @return user ids as a list of String
   * @throws FormsOnlineDatabaseException
   */
  public List<String> getReceiversAsUsers(int id, String componentId)
      throws FormsOnlineDatabaseException;

  /**
   * Get the form's receivers list where groups has been declared directly.
   * @param id the form id
   * @param componentId the component instance id
   * @return group ids as a list of String
   * @throws FormsOnlineDatabaseException
   */
  public List<String> getReceiversAsGroups(int id, String componentId)
      throws FormsOnlineDatabaseException;

  /**
   * Get the form available to be sent for given user or given groups
   * @param componentId the component instance id
   * @param userId the user id
   * @param userGroupIds the user's groups id list
   * @return a list of FormDetail objects
   * @throws FormsOnlineDatabaseException
   */
  public List<FormDetail> getUserAvailableForms(String componentId, String userId,
      String[] userGroupIds) throws FormsOnlineDatabaseException;

  /**
   * Get all form instances that have been sent by given user (excepted the ones that have been
   * archived.
   * @param componentId the component instance id
   * @param formId the form id
   * @param userId the user id
   * @return a list of FormInstance objects
   * @throws FormsOnlineDatabaseException
   */
  public List<FormInstance> getSentFormInstances(String componentId, int formId, String userId)
      throws FormsOnlineDatabaseException;

  /**
   * Get all form instances received by that have been sent by given user (excepted the ones that
   * have been archived.
   * @param componentId the component instance id
   * @param formId the form id
   * @param userId the user id
   * @return a list of FormInstance objects
   * @throws FormsOnlineDatabaseException
   */
  public List<FormInstance> getReceivedFormInstances(String instanceId, String userId, int formId)
      throws FormsOnlineDatabaseException;

  public List<String> getAvailableFormIdsAsReceiver(String instanceId, String userId,
      String[] userGroupIds) throws FormsOnlineDatabaseException;

  public FormInstance createInstance(FormInstance instance) throws FormsOnlineDatabaseException;

  public FormInstance getFormInstance(String componentId, int formInstanceId)
      throws FormsOnlineDatabaseException;

  public List<FormDetail> getForms(List<String> formIds) throws FormsOnlineDatabaseException;

  public void updateFormInstance(FormInstance instance) throws FormsOnlineDatabaseException;

  public void deleteFormInstance(String componentId, int id) throws FormsOnlineDatabaseException;
}
