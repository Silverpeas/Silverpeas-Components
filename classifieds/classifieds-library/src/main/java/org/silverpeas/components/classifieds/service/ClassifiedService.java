/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.classifieds.service;

import org.silverpeas.core.ApplicationService;
import org.silverpeas.components.classifieds.model.ClassifiedDetail;
import org.silverpeas.components.classifieds.model.Subscribe;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.index.search.model.QueryDescription;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Services provided by the Classified Silverpeas component.
 * It has to be managed by the IoC container under the name classifiedService.
 */
public interface ClassifiedService extends ApplicationService {

  @Override
  @SuppressWarnings("unchecked")
  Optional<ClassifiedDetail> getContributionById(ContributionIdentifier classifiedId);

  /**
   * create a classified
   * @param classified : ClassifiedDetail
   * @return classifiedId : String
   */
  String createClassified(ClassifiedDetail classified);

  /**
   * update the classified and send notification if notify is true
   * @param classified : ClassifiedDetail
   * @param notify : boolean
   */
  void updateClassified(ClassifiedDetail classified, boolean notify);

  /**
   * delete the classified corresponding to classifiedId
   * @param classifiedId the unique identifier of the classified.
   */
  void deleteClassified(ContributionIdentifier classifiedId);

  /**
   * delete all classifieds for the instance corresponding to instanceId
   * @param instanceId : String
   */
  void deleteAllClassifieds(String instanceId);

  /**
   * pass the classified corresponding to classifiedId in draft mode
   * @param classifiedId the unique identifier of the classified
   */
  void draftInClassified(ContributionIdentifier classifiedId);

  /**
   * take out draft mode the classified corresponding to classified
   * @param classifiedId the unique identifier of the classified
   * @param profile a role profile of the user performing the action.
   * @param isValidationEnabled is the validation enabled?
   */
  void draftOutClassified(ContributionIdentifier classifiedId, String profile, boolean isValidationEnabled);

  /**
   * get all classifieds for an instance corresponding to instanceId
   * @param instanceId : String
   * @return a collection of ClassifiedDetail
   */
  Collection<ClassifiedDetail> getAllClassifieds(String instanceId);

  /**
   * get all classifieds for user and instance corresponding to userId and instanceId
   * @param instanceId : String
   * @param userId : String
   * @return a collection of ClassifiedDetail
   */
  List<ClassifiedDetail> getClassifiedsByUser(String instanceId, String userId);

  /**
   * get the number of classifieds for an instance corresponding to instanceId
   * @param instanceId : String
   * @return the number of classified : String
   * @
   */
  String getNbTotalClassifieds(String instanceId);

  /**
   * get all classifieds to validate for an instance corresponding to instanceId
   * @param instanceId : String
   * @return a Collection of ClassifiedDetail
   */
  List<ClassifiedDetail> getClassifiedsToValidate(String instanceId);

  /**
   * pass to status refused because the user corresponding to userId refused the classified
   * corresponding to classifiedId for the given motive.
   * @param classifiedId unique identifier of a classified
   * @param userId unique identifier of the user refusing the classified
   * @param refusalMotive the text about the motive of the refusal
   */
  void refusedClassified(ContributionIdentifier classifiedId, String userId, String refusalMotive);

  /**
   * pass to status validate because the user corresponding to userId validated the classified
   * corresponding to classifiedId
   * @param classifiedId the unique identifier of a classified.
   * @param userId the unique identifier of the user validating the classified.
   */
  void validateClassified(ContributionIdentifier classifiedId, String userId);

  /**
   * search all classifieds corresponding to the query
   * @param query the query from which the search will be performed.
   * @return a collection of ClassifiedDetail
   */
  List<ClassifiedDetail> search(QueryDescription query);

  /**
   * index all the classifieds for the instance corresponding to instanceId
   * @param instanceId : String
   */
  void indexClassifieds(String instanceId);

  /**
   * get all expiring classifieds (corresponding of a number of day nbDays)
   * @param nbDays : int
   * @param instanceId : classified component instance id
   * @return a collection of ClassifiedDetail
   */
  Collection<ClassifiedDetail> getAllClassifiedsToUnpublish(int nbDays, String instanceId);

  /**
   * create a subscription
   * @param subscribe : Subscribe
   * @
   */
  void createSubscribe(Subscribe subscribe);

  /**
   * delete a subscription corresponding to subscribeId
   * @param subscribeId : String
   */
  void deleteSubscribe(String subscribeId);

  /**
   * Unpublish a subscription corresponding to classifiedId
   * @param classifiedId the unique identifier of a classified
   */
  void unpublishClassified(final ContributionIdentifier classifiedId);

  /**
   * get all subscriptions for user and instance corresponding to userId and instanceId
   * @param instanceId : String
   * @param userId : String
   * @return a collection of Subscribe
   */
  Collection<Subscribe> getSubscribesByUser(String instanceId, String userId);

  /**
   * get all subscribing users to a search corresponding to fields field1 and field2
   * @param field1 : String
   * @param field2 : String
   * @return a collection of userId (String)
   */
  Collection<String> getUsersBySubscribe(String instanceId, String field1, String field2);

  /**
   * delete all subscriptions for the instance corresponding to instanceId
   * @param instanceId unique identifier of a component instance.
   */
  void deleteAllSubscribes(String instanceId);

  /**
   * send a notification for subscribers to field1 and field2 when classified modified
   * @param field1 the first classified field
   * @param field2 the second classified field
   */
  void sendSubscriptionsNotification(String field1, String field2,
      ClassifiedDetail classified);

  /**
   * get all valid classifieds
   * @param instanceId : String
   * @param mapFields1 : HashMap des champs de recherche 1
   * @param mapFields2 : HashMap des champs de recherche 1
   * @param searchField1 : champ de recherche 1
   * @param searchField2 : champ de recherche 2
   * @param firstItemIndex : index of first item to display
   * @param elementsPerPage : nombre d'éléments à afficher par page
   * @return a collection of ClassifiedDetail
   */
  List<ClassifiedDetail> getAllValidClassifieds(String instanceId,
      Map<String, String> mapFields1, Map<String, String> mapFields2, String searchField1,
      String searchField2, int firstItemIndex, int elementsPerPage);

  List<ClassifiedDetail> getAllValidClassifieds(String instanceId);

  void setClassification(ClassifiedDetail classified, String searchField1,
      String searchField2, String xmlFormName);

}