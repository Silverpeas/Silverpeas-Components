/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
package com.silverpeas.classifieds.control.ejb;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.EJBObject;

import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.silverpeas.classifieds.model.Subscribe;
import com.stratelia.webactiv.searchEngine.model.QueryDescription;

/**
 * @author
 */
public interface ClassifiedsBm extends EJBObject {

  /**
   * get the classified corresponding to classifiedId
   * @param classifiedId : String
   * @return classified : ClassifiedDetail
   * @throws RemoteException
   */
  public ClassifiedDetail getClassified(String classifiedId) throws RemoteException;

  /**
   * create a classified
   * @param classified : ClassifiedDetail
   * @return classifiedId : String
   * @throws RemoteException
   */
  public String createClassified(ClassifiedDetail classified) throws RemoteException;

  /**
   * update the classified and send notification if notify is true
   * @param classified : ClassifiedDetail
   * @param notify : boolean
   * @throws RemoteException
   */
  public void updateClassified(ClassifiedDetail classified, boolean notify) throws RemoteException;

  /**
   * delete the classified corresponding to classifiedId
   * @param classifiedId : String
   * @throws RemoteException
   */
  public void deleteClassified(String classifiedId) throws RemoteException;

  /**
   * delete all classifieds for the instance corresponding to instanceId
   * @param instanceId : String
   * @throws RemoteException
   */
  public void deleteAllClassifieds(String instanceId) throws RemoteException;

  /**
   * pass the classified corresponding to classifiedId in draft mode
   * @param classifiedId : String
   * @throws RemoteException
   */
  public void draftInClassified(String classifiedId) throws RemoteException;

  /**
   * take out draft mode the classified corresponding to classified
   * @param classifiedId : String
   * @param profile : String
   * @throws RemoteException
   */
  public void draftOutClassified(String classifiedId, String profile) throws RemoteException;

  /**
   * get all classifieds for an instance corresponding to instanceId
   * @param instanceId : String
   * @return a collection of ClassifiedDetail
   * @throws RemoteException
   */
  public Collection<ClassifiedDetail> getAllClassifieds(String instanceId) throws RemoteException;

  /**
   * get all classifieds for user and instance corresponding to userId and instanceId
   * @param instanceId : String
   * @param userId : String
   * @return a collection of ClassifiedDetail
   * @throws RemoteException
   */
  public Collection<ClassifiedDetail> getClassifiedsByUser(String instanceId, String userId)
      throws RemoteException;

  /**
   * get the number of classifieds for an instance corresponding to instanceId
   * @param instanceId : String
   * @return the number of classified : String
   * @throws RemoteException
   */
  public String getNbTotalClassifieds(String instanceId) throws RemoteException;

  /**
   * get all classifieds to validate for an instance corresponding to instanceId
   * @param instanceId : String
   * @return a collection of ClassifiedDetail
   * @throws RemoteException
   */
  public Collection<ClassifiedDetail> getClassifiedsToValidate(String instanceId)
      throws RemoteException;

  /**
   * pass to status refused because the user corresponding to userId refused the classified
   * corresponding to classifiedId for the motive ResusalMotive
   * @param classifiedId : String
   * @param userId : String
   * @param refusalMotive : String
   * @throws RemoteException
   */
  public void refusedClassified(String classifiedId, String userId, String refusalMotive)
      throws RemoteException;

  /**
   * pass to status validate because the user corresponding to userId validated the classified
   * corresponding to classifiedId
   * @param classifiedId
   * @param userId
   * @throws RemoteException
   */
  public void validateClassified(String classifiedId, String userId) throws RemoteException;

  /**
   * search all classifieds corresponding to the query
   * @param query : QueryDescription
   * @return a collection of ClassifiedDetail
   * @throws RemoteException
   */
  public Collection<ClassifiedDetail> search(QueryDescription query) throws RemoteException;

  /**
   * index all the classifieds for the instance corresponding to instanceId
   * @param instanceId : String
   * @throws RemoteException
   */
  public void indexClassifieds(String instanceId) throws RemoteException;

  /**
   * get all expiring classifieds (corresponding of a number of day nbDays)
   * @param nbDays : int
   * @return a collection of ClassifiedDetail
   * @throws RemoteException
   */
  public Collection<ClassifiedDetail> getAllClassifiedsToDelete(int nbDays) throws RemoteException;

  /**
   * create a subscription
   * @param subscribe : Subscribe
   * @throws RemoteException
   */
  public void createSubscribe(Subscribe subscribe) throws RemoteException;

  /**
   * delete a subscription corresponding to subscribeId
   * @param subscribeId : String
   * @throws RemoteException
   */
  public void deleteSubscribe(String subscribeId) throws RemoteException;

  /**
   * get all subscriptions for user and instance corresponding to userId and instanceId
   * @param instanceId : String
   * @param userId : String
   * @return a collection of Subscribe
   * @throws RemoteException
   */
  public Collection<Subscribe> getSubscribesByUser(String instanceId, String userId)
      throws RemoteException;

  /**
   * get all subscribing users to a search corresponding to fields field1 and field2
   * @param field1 : String
   * @param field2 : String
   * @return a collection of userId (String)
   * @throws RemoteException
   */
  public Collection<String> getUsersBySubscribe(String field1, String field2)
      throws RemoteException;

  /**
   * delete all subscriptions for the instance corresponding to instanceId
   * @param instanceId
   * @throws RemoteException
   */
  public void deleteAllSubscribes(String instanceId) throws RemoteException;

  /**
   * send a notification for subscribers to field1 and field2 when classified modified
   * @param field1 : string
   * @param field2 : String
   * @param classified : ClassifiedDetail
   * @throws RemoteException
   */
  public void sendSubscriptionsNotification(String field1, String field2,
      ClassifiedDetail classified) throws RemoteException;
}