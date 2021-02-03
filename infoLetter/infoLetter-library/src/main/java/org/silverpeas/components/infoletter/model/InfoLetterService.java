/*
 * Copyright (C) 2000 - 2021 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.infoletter.model;

import java.util.List;
import java.util.Set;

import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.WAPrimaryKey;

/**
 * Contract to access info letter data
 * @author
 */
public interface InfoLetterService {

  static InfoLetterService get() {
    return ServiceProvider.getService(InfoLetterService.class);
  }

  /**
   * Create information letter
   * @param il the information letter to create
   */
  void createInfoLetter(InfoLetter il);

  /**
   * Update information letter
   * @param il the information letter to update
   */
  void updateInfoLetter(InfoLetter il);

  /**
   * Retrieve information letters
   * @param applicationId the application identifier
   * @return the information letters of the current application identifier given in parameter
   */
  List<InfoLetter> getInfoLetters(String applicationId);

  /**
   * Retrieve information letter publications
   * @param letterPK the information letter primary key
   * @return the list of information letter publications of an information letter
   */
  List<InfoLetterPublication> getInfoLetterPublications(WAPrimaryKey letterPK);

  /**
   * Create information letter publication
   * @param ilp the information letter publication pdc to create
   * @param userId the creator user identifier
   */
  void createInfoLetterPublication(InfoLetterPublicationPdC ilp, String userId);

  /**
   * Delete information letter publication
   * @param pk the information letter publication primary key
   * @param componentId the component identifier
   */
  void deleteInfoLetterPublication(WAPrimaryKey pk, String componentId);

  /**
   * Update information letter publication
   * @param ilp the information letter publication to update
   */
  void updateInfoLetterPublication(InfoLetterPublicationPdC ilp);

  /**
   * Retrieve an information letter from his primary key
   * @param letterPK the letter primary key
   * @return the infirmation letter
   */
  InfoLetter getInfoLetter(WAPrimaryKey letterPK);

  /**
   * Retrieve information letter publication from his key
   * @param publiPK the information letter publication primary key
   * @return an Information Letter Publication PdC
   */
  InfoLetterPublicationPdC getInfoLetterPublication(WAPrimaryKey publiPK);

  /**
   * Create a default Info Letter when instanciated
   * @param componentId the component identifier
   * @return a default Info Letter
   */
  InfoLetter createDefaultLetter(String componentId);

  /**
   * Deletes all the info letters (and then all the publications and external subscribers) in the
   * specified component instance.
   * @param componentId the unique identifier of the InfoLetter instance.
   */
  void deleteAllInfoLetters(String componentId);

  /**
   * @param componentId componentId component instance id
   * @return map of subscriber ids indexed by type of subscriber
   */
  SubscriptionSubscriberList getInternalSuscribers(String componentId);

  /**
   * Update internal user subscribers list
   * @param componentId componentId component instance id
   * @param users an array of User detail
   * @param groups an array of Group
   */
  void setInternalSuscribers(String componentId, UserDetail[] users, Group[] groups);

  /**
   * Retrieve external emails address
   * @param letterPK the info letter identifier (letter primary key)
   * @return a set of external emails
   */
  Set<String> getEmailsExternalsSuscribers(WAPrimaryKey letterPK);

  /**
   * Save external subscriber emails address
   * @param letterPK the letter primary key
   * @param emails the list of external emails to save
   */
  void setEmailsExternalsSubscribers(WAPrimaryKey letterPK, Set<String> emails);

  /**
   * Toggle subscription unsubscription of a user to the news letter
   * @param userId the user identifier
   * @param componentId the info letter component instance identifier
   * @param isUserSubscribing true if user is subscribing, false else if
   */
  void toggleSuscriber(String userId, String componentId, boolean isUserSubscribing);

  /**
   * Check if use is an internal subscriber of the information letter
   * @param userId the user identifier
   * @param componentId the info letter component instance identifier
   * @return true if user is a subscriber, false else if
   */
  boolean isUserSuscribed(String userId, String componentId);

  /**
   * Initialize template
   * @param componentId the info letter component instance identifier
   * @param letterPK the info letter identifier
   * @param userId the user identifier
   */
  void initTemplate(String componentId, WAPrimaryKey letterPK, String userId);

  int getSilverObjectId(String pubId, String componentId);

  /**
   * Send letter by mail
   * @param ilp the information letter
   * @param server
   * @param mimeMultipart
   * @param listEmailDest
   * @param subject
   * @param emailFrom
   * @return list of emails in error
   */
  Set<String> sendLetterByMail(InfoLetterPublicationPdC ilp, String server,
      String mimeMultipart, Set<String> listEmailDest, String subject, String emailFrom);
}
