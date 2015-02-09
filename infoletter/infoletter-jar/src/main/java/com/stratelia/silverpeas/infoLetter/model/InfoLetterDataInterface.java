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
package com.stratelia.silverpeas.infoLetter.model;

import java.sql.Connection;
import java.util.List;
import java.util.Set;

import com.silverpeas.subscribe.util.SubscriptionSubscriberList;
import com.stratelia.silverpeas.infoLetter.InfoLetterException;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.util.ServiceProvider;
import org.silverpeas.util.WAPrimaryKey;

/**
 * Contract to access info letter data
 * @author
 */
public interface InfoLetterDataInterface {

  static InfoLetterDataInterface get() {
    return ServiceProvider.getService(InfoLetterDataInterface.class);
  }

  /**
   * Open connection
   * @return Connection
   * @throws InfoLetterException
   */
  public Connection openConnection() throws InfoLetterException;

  /**
   * Create information letter
   * @param il the information letter to create
   */
  public void createInfoLetter(InfoLetter il);

  /**
   * Update information letter
   * @param il the information letter to update
   */
  public void updateInfoLetter(InfoLetter il);

  /**
   * Retrieve information letters
   * @param applicationId the application identifier
   * @return the information letters of the current application identifier given in parameter
   */
  public List<InfoLetter> getInfoLetters(String applicationId);

  /**
   * Retrieve information letter publications
   * @param letterPK the information letter primary key
   * @return the list of information letter publications of an information letter
   */
  public List<InfoLetterPublication> getInfoLetterPublications(WAPrimaryKey letterPK);

  /**
   * Create information letter publication
   * @param ilp the information letter publication pdc to create
   * @param userId the creator user identifier
   */
  public void createInfoLetterPublication(InfoLetterPublicationPdC ilp, String userId);

  /**
   * Delete information letter publication
   * @param pk the information letter publication primary key
   * @param componentId the component identifier
   */
  public void deleteInfoLetterPublication(WAPrimaryKey pk, String componentId);

  /**
   * Update information letter publication
   * @param ilp the information letter publication to update
   */
  public void updateInfoLetterPublication(InfoLetterPublicationPdC ilp);

  /**
   * Retrieve an information letter from his primary key
   * @param letterPK the letter primary key
   * @return the infirmation letter
   */
  public InfoLetter getInfoLetter(WAPrimaryKey letterPK);

  /**
   * Retrieve information letter publication from his key
   * @param publiPK the information letter publication primary key
   * @return an Information Letter Publication PdC
   */
  public InfoLetterPublicationPdC getInfoLetterPublication(WAPrimaryKey publiPK);

  /**
   * Create a default Info Letter when instanciated
   * @param componentId the component identifier
   * @return a default Info Letter
   */
  public InfoLetter createDefaultLetter(String componentId);

  /**
   * @param componentId componentId component instance id
   * @return map of subscriber ids indexed by type of subscriber
   */
  public SubscriptionSubscriberList getInternalSuscribers(String componentId);

  /**
   * Update internal user subscribers list
   * @param componentId componentId component instance id
   * @param users an array of User detail
   * @param groups an array of Group
   */
  public void setInternalSuscribers(String componentId, UserDetail[] users, Group[] groups);

  /**
   * Retrieve external emails address
   * @param letterPK the info letter identifier (letter primary key)
   * @return a set of external emails
   */
  public Set<String> getEmailsExternalsSuscribers(WAPrimaryKey letterPK);

  /**
   * Save external subscriber emails address
   * @param letterPK the letter primary key
   * @param emails the list of external emails to save
   */
  public void setEmailsExternalsSubscribers(WAPrimaryKey letterPK, Set<String> emails);

  /**
   * Toggle subscription unsubscription of a user to the news letter
   * @param userId the user identifier
   * @param componentId the info letter component instance identifier
   * @param isUserSubscribing true if user is subscribing, false else if
   */
  public void toggleSuscriber(String userId, String componentId, boolean isUserSubscribing);

  /**
   * Check if use is an internal subscriber of the information letter
   * @param userId the user identifier
   * @param componentId the info letter component instance identifier
   * @return true if user is a subscriber, false else if
   */
  public boolean isUserSuscribed(String userId, String componentId);

  /**
   * Initialize template
   * @param componentId the info letter component instance identifier
   * @param letterPK the info letter identifier
   * @param userId the user identifier
   */
  public void initTemplate(String componentId, WAPrimaryKey letterPK, String userId);

  public int getSilverObjectId(String pubId, String componentId);

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
  public Set<String> sendLetterByMail(InfoLetterPublicationPdC ilp, String server,
      String mimeMultipart, Set<String> listEmailDest, String subject, String emailFrom);
}
