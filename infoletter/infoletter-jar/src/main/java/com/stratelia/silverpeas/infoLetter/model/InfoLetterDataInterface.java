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
package com.stratelia.silverpeas.infoLetter.model;

import com.stratelia.silverpeas.infoLetter.*;
import com.stratelia.webactiv.util.WAPrimaryKey;
import java.sql.Connection;
import java.util.Vector;

/**
 * Interface declaration
 * 
 * 
 * @author
 */
public interface InfoLetterDataInterface {

  /**
   * Ouverture de la connection vers la source de donnees
   * 
   * @return Connection la connection
   * @exception InfoLetterException
   * @author frageade
   * @since 26 Fevrier 2002
   */
  public Connection openConnection() throws InfoLetterException;

  // Creation d'une lettre d'information
  public void createInfoLetter(InfoLetter ie);

  // Suppression d'une lettre d'information
  public void deleteInfoLetter(WAPrimaryKey pk);

  // Mise a jour d'une lettre d'information
  public void updateInfoLetter(InfoLetter ie);

  // Recuperation de la liste des lettres
  public Vector getInfoLetters(String instanceId);

  // Recuperation de la liste des publications
  public Vector getInfoLetterPublications(WAPrimaryKey letterPK);

  // Creation d'une publication
  public void createInfoLetterPublication(InfoLetterPublicationPdC ilp,
      String userId);

  // Suppression d'une publication
  public void deleteInfoLetterPublication(WAPrimaryKey pk, String componentId);

  // Mise a jour d'une publication
  public void updateInfoLetterPublication(InfoLetterPublicationPdC ilp);

  // Validation d'une publication
  public void validateInfoLetterPublication(InfoLetterPublication ilp);

  // Recuperation d'une lettre par sa clef
  public InfoLetter getInfoLetter(WAPrimaryKey letterPK);

  // Recuperation d'une publication par sa clef
  public InfoLetterPublicationPdC getInfoLetterPublication(WAPrimaryKey publiPK);

  // Creation de la lettre par defaut a l'instanciation
  public InfoLetter createDefaultLetter(String spaceId, String componentId);

  // Recuperation de la liste des abonnes internes
  public Vector getInternalSuscribers(WAPrimaryKey letterPK);

  // Mise a jour de la liste des abonnes internes
  public void setInternalSuscribers(WAPrimaryKey letterPK, Vector abonnes);

  // Recuperation de la liste des emails externes
  public Vector getExternalsSuscribers(WAPrimaryKey letterPK);

  // Sauvegarde de la liste des emails externes
  public void setExternalsSuscribers(WAPrimaryKey letterPK, Vector emails);

  // abonnement ou desabonnement d'un utilisateur interne
  public void toggleSuscriber(String userId, WAPrimaryKey letterPK, boolean flag);

  // test d'abonnement d'un utilisateur interne
  public boolean isSuscriber(String userId, WAPrimaryKey letterPK);

  // initialisation du template
  public void initTemplate(String spaceId, String componentId,
      WAPrimaryKey letterPK);

  public int getSilverObjectId(String pubId, String componentId);
}
