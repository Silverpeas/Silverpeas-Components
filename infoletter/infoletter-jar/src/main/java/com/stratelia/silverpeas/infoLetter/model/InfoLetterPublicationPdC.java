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

import java.util.Iterator;

import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * 
 * @author lbertin
 * @since February 2002
 */
public class InfoLetterPublicationPdC extends InfoLetterPublication implements
    SilverContentInterface {

  /** icone d'une publication */
  private String iconUrl = "infoLetterSmall.gif";

  /**
   * Constructeur sans parametres
   * 
   * @author frageade
   * @since February 2002
   */
  public InfoLetterPublicationPdC() {
    super();
  }

  /**
   * Constructeur pour convertir une InfoLetterPublication en
   * InfoLetterPublicationPdc
   * 
   * @param ilp
   *          InfoLetterPublication
   * @author lbertin
   * @since February 2002
   */
  public InfoLetterPublicationPdC(InfoLetterPublication ilp) {
    super(ilp.getPK(), ilp.getTitle(), ilp.getDescription(), ilp
        .getParutionDate(), ilp.getPublicationState(), ilp.getLetterId());
  }

  /**
   * Constructeur a 6 parametres
   * 
   * @param WAPrimaryKey
   *          pk
   * @param String
   *          title
   * @param String
   *          description
   * @param String
   *          parutionDate
   * @param int publicationState
   * @param String
   *          letterId
   * @author frageade
   * @since February 2002
   */
  public InfoLetterPublicationPdC(WAPrimaryKey pk, String title,
      String description, String parutionDate, int publicationState,
      int letterId) {
    super(pk, title, description, parutionDate, publicationState, letterId);
  }

  // methods to be implemented by SilverContentInterface

  public String getName() {
    return getTitle();
  }

  public String getURL() {
    return "searchResult?Type=Publication&Id=" + getId();
  }

  public String getId() {
    return getPK().getId();
  }

  public String getDate() {
    return getParutionDate();
  }

  public String getCreatorId() {
    return null;
  }

  public String getIconUrl() {
    return iconUrl;
  }

  public String getSilverCreationDate() {
    return getParutionDate();
  }

  public String getDescription(String language) {
    return getDescription();
  }

  public String getName(String language) {
    return getName();
  }

  public Iterator getLanguages() {
    return null;
  }
}
/*************************
 *** Fin du fichier ***
 ************************/
