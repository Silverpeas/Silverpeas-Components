/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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
package com.silverpeas.classifieds.control;

import com.silverpeas.ApplicationService;
import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.silverpeas.classifieds.model.Subscribe;
import org.silverpeas.search.searchEngine.model.QueryDescription;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Services provided by the Classified Silverpeas component.
 * It has to be managed by the IoC container under the name classifiedService.
 */
public interface ClassifiedService extends ApplicationService<ClassifiedDetail> {

  /**
   * create a classified
   * @param classified : ClassifiedDetail
   * @return classifiedId : String
   * @
   */
  public String createClassified(ClassifiedDetail classified) ;

  /**
   * update the classified and send notification if notify is true
   * @param classified : ClassifiedDetail
   * @param notify : boolean
   * @
   */
  public void updateClassified(ClassifiedDetail classified, boolean notify) ;

  /**
   * delete the classified corresponding to classifiedId
   * @param instanceId : String
   * @param classifiedId : String
   * @
   */
  public void deleteClassified(String instanceId, String classifiedId);

  /**
   * delete all classifieds for the instance corresponding to instanceId
   * @param instanceId : String
   * @
   */
  public void deleteAllClassifieds(String instanceId) ;

  /**
   * pass the classified corresponding to classifiedId in draft mode
   * @param classifiedId : String
   * @
   */
  public void draftInClassified(String classifiedId) ;

  /**
   * take out draft mode the classified corresponding to classified
   * @param classifiedId : String
   * @param profile : String
   * @param isValidationEnabled : boolean
   * @
   */
  public void draftOutClassified(String classifiedId, String profile, boolean isValidationEnabled) ;

  /**
   * get all classifieds for an instance corresponding to instanceId
   * @param instanceId : String
   * @return a collection of ClassifiedDetail
   * @
   */
  public Collection<ClassifiedDetail> getAllClassifieds(String instanceId) ;

  /**
   * get all classifieds for user and instance corresponding to userId and instanceId
   * @param instanceId : String
   * @param userId : String
   * @return a collection of ClassifiedDetail
   * @
   */
  public Collection<ClassifiedDetail> getClassifiedsByUser(String instanceId, String userId)
      ;

  /**
   * get the number of classifieds for an instance corresponding to instanceId
   * @param instanceId : String
   * @return the number of classified : String
   * @
   */
  public String getNbTotalClassifieds(String instanceId) ;

  /**
   * get all classifieds to validate for an instance corresponding to instanceId
   * @param instanceId : String
   * @return a Collection of ClassifiedDetail
   * @
   */
  public Collection<ClassifiedDetail> getClassifiedsToValidate(String instanceId)
      ;

  /**
   * pass to status refused because the user corresponding to userId refused the classified
   * corresponding to classifiedId for the motive ResusalMotive
   * @param classifiedId : String
   * @param userId : String
   * @param refusalMotive : String
   * @
   */
  public void refusedClassified(String classifiedId, String userId, String refusalMotive)
      ;

  /**
   * pass to status validate because the user corresponding to userId validated the classified
   * corresponding to classifiedId
   * @param classifiedId
   * @param userId
   * @
   */
  public void validateClassified(String classifiedId, String userId) ;

  /**
   * search all classifieds corresponding to the query
   * @param query : QueryDescription
   * @return a collection of ClassifiedDetail
   * @
   */
  public List<ClassifiedDetail> search(QueryDescription query) ;

  /**
   * index all the classifieds for the instance corresponding to instanceId
   * @param instanceId : String
   * @
   */
  public void indexClassifieds(String instanceId) ;

  /**
   * get all expiring classifieds (corresponding of a number of day nbDays)
   * @param nbDays : int
   * @param instanceId : classified component instance id
   * @return a collection of ClassifiedDetail
   * @
   */
  public Collection<ClassifiedDetail> getAllClassifiedsToUnpublish(int nbDays, String instanceId) ;

  /**
   * create a subscription
   * @param subscribe : Subscribe
   * @
   */
  public void createSubscribe(Subscribe subscribe) ;

  /**
   * delete a subscription corresponding to subscribeId
   * @param subscribeId : String
   * @
   */
  public void deleteSubscribe(String subscribeId) ;

  /**
   * unpublish a subscription corresponding to subscribeId
   * @param subscribeId : String
   * @
   */
  public void unpublishClassified(String classifiedId);

  /**
   * get all subscriptions for user and instance corresponding to userId and instanceId
   * @param instanceId : String
   * @param userId : String
   * @return a collection of Subscribe
   * @
   */
  public Collection<Subscribe> getSubscribesByUser(String instanceId, String userId);

  /**
   * get all subscribing users to a search corresponding to fields field1 and field2
   * @param field1 : String
   * @param field2 : String
   * @return a collection of userId (String)
   * @
   */
  public Collection<String> getUsersBySubscribe(String field1, String field2);
  
  /**
   * delete all subscriptions for the instance corresponding to instanceId
   * @param instanceId
   * @
   */
  public void deleteAllSubscribes(String instanceId);
  
  /**
   * send a notification for subscribers to field1 and field2 when classified modified
   * @param field1 : string
   * @param field2 : String
   * @param classified : ClassifiedDetail
   * @throws RemoteException
   */
  public void sendSubscriptionsNotification(String field1, String field2,
      ClassifiedDetail classified);

  /**
   * get all classifieds unpublished for an instance corresponding to instanceId and for given user
   * @param instanceId : String
   * @param userId : creator user id
   * @return a collection of ClassifiedDetail
   * @
   */
  Collection<ClassifiedDetail> getUnpublishedClassifieds(String instanceId, String userId);
  
  /**
  * get all valid classifieds
  * @param instanceId : String
  * @param mapFields1 : HashMap des champs de recherche 1 
  * @param mapFields2 : HashMap des champs de recherche 1
  * @param searchField1 : champ de recherche 1
  * @param searchField2 : champ de recherche 2
  * @param currentPage : numéro de page actuelle 
  * @param elementsPerPage : nombre d'éléments à afficher par page
  * @return a collection of ClassifiedDetail
  */
  public Collection<ClassifiedDetail> getAllValidClassifieds(String instanceId, Map<String, String> mapFields1, Map<String, String> mapFields2, String searchField1, String searchField2, int currentPage, int elementsPerPage);
  
  public void setClassification(ClassifiedDetail classified, String searchField1, String searchField2, String xmlFormName);
  
}