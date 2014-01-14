/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

import java.util.Date;
import java.util.Iterator;

import com.silverpeas.SilverpeasContent;
import com.silverpeas.accesscontrol.AccessController;
import com.silverpeas.accesscontrol.AccessControllerProvider;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.silverpeas.infoLetter.InfoLetterContentManager;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * @author lbertin
 * @since February 2002
 */
public class InfoLetterPublicationPdC extends InfoLetterPublication implements
    SilverContentInterface, SilverpeasContent {
  private static final long serialVersionUID = -2174573301215680444L;
  /** icone d'une publication */
  private String iconUrl = "infoLetterSmall.gif";
  private static final String TYPE = "publication";
  
  private static final InfoLetterContentManager contentMgr = new InfoLetterContentManager();
  private String silverObjectId;
  private String positions;

  /**
   * Constructeur sans parametres
   * @author frageade
   * @since February 2002
   */
  public InfoLetterPublicationPdC() {
    super();
  }

  /**
   * Constructeur pour convertir une InfoLetterPublication en InfoLetterPublicationPdc
   * @param ilp InfoLetterPublication
   * @author lbertin
   * @since February 2002
   */
  public InfoLetterPublicationPdC(InfoLetterPublication ilp) {
    super(ilp.getPK(), ilp.getTitle(), ilp.getDescription(), ilp
        .getParutionDate(), ilp.getPublicationState(), ilp.getLetterId());
  }

  /**
   * Constructeur a 6 parametres
   * @param WAPrimaryKey pk
   * @param String title
   * @param String description
   * @param String parutionDate
   * @param int publicationState
   * @param String letterId
   * @author frageade
   * @since February 2002
   */
  public InfoLetterPublicationPdC(WAPrimaryKey pk, String title,
      String description, String parutionDate, int publicationState,
      int letterId) {
    super(pk, title, description, parutionDate, publicationState, letterId);
  }

  /**
   * @return the positions
   */
  public String getPositions() {
    return positions;
  }

  /**
   * @param positions the positions to set
   */
  public void setPositions(String positions) {
    this.positions = positions;
  }

  /**
   * methods to be implemented by SilverContentInterface
   */

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

  public Iterator<String> getLanguages() {
    return null;
  }

  /**
   * Method which implements SilverpeasContent
   */

  @Override
  public String getComponentInstanceId() {
    return getInstanceId();
  }

  @Override
  public String getSilverpeasContentId() {
    if (this.silverObjectId == null) {
      int objectId = contentMgr.getSilverObjectId(getId(), getComponentInstanceId());
      if (objectId >= 0) {
        this.silverObjectId = String.valueOf(objectId);
      }
    }
    return this.silverObjectId;    
  }

  @Override
  public UserDetail getCreator() {
    return UserDetail.getById(this.getCreatorId());
  }

  @Override
  public Date getCreationDate() {
    // no need date to classify this content
    return null;
  }

  @Override
  public String getContributionType() {
    return TYPE;
  }

  /**
   * Is the specified user can access this information letter?
   * <p/>
   * A user can access an information letter if it has enough rights to access the InfoLetter
   * instance in which is managed this letter.
   * @param user a user in Silverpeas.
   * @return true if the user can access this letter, false otherwise.
   */
  @Override
  public boolean canBeAccessedBy(final UserDetail user) {
    AccessController<String> accessController =
        AccessControllerProvider.getAccessController("componentAccessController");
    return accessController.isUserAuthorized(user.getId(), getComponentInstanceId());
  }
}
