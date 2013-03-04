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
package com.stratelia.silverpeas.infoLetter.model;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.silverpeas.subscribe.constant.SubscriberType;
import com.stratelia.silverpeas.infoLetter.InfoLetterException;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * Interface declaration
 * @author
 */
public interface InfoLetterDataInterface {

  /**
   * Ouverture de la connection vers la source de donnees
   * @return Connection la connection
   * @exception InfoLetterException
   * @author frageade
   * @since 26 Fevrier 2002
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
   * @param spaceId the space identifier
   * @param componentId the component identifier
   * @return a default Info Letter
   */
  public InfoLetter createDefaultLetter(String spaceId, String componentId);

  /**
   * @param componentId componentId component instance id
   * @return map of subscriber ids indexed by type of subscriber
   */
  public Map<SubscriberType, Collection<String>> getInternalSuscribers(String componentId);

  /**
   * Update internal user subscribers list
   * @param componentId componentId component instance id
   * @param users
   * @param groups
   */
  public void setInternalSuscribers(String componentId, UserDetail[] users, Group[] groups);

  // Recuperation de la liste des emails externes
  public Collection<String> getExternalsSuscribers(WAPrimaryKey letterPK);

  /**
   * Save external subscriber emails address 
   * @param letterPK the letter primary key
   * @param emails the list of external emails to save
   */
  public void setExternalsSuscribers(WAPrimaryKey letterPK, Collection<String> emails);

  // abonnement ou desabonnement d'un utilisateur interne
  public void toggleSuscriber(String userId, String componentId, boolean isUserSubscribing);

  /**
   * Check if use is an internal subscriber of the information letter
   *
   *
   * @param userId the user identifier
   * @param componentId
   * @return true if user is a subscriber, false else if
   */
  public boolean isUserSuscribed(String userId, String componentId);

  // initialisation du template
  public void initTemplate(String spaceId, String componentId, WAPrimaryKey letterPK);

  public int getSilverObjectId(String pubId, String componentId);
}
