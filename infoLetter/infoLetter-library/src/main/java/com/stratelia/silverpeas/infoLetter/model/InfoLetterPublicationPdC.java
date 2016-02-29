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

import com.silverpeas.SilverpeasContent;
import com.silverpeas.accesscontrol.AccessController;
import com.silverpeas.accesscontrol.AccessControllerProvider;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.silverpeas.infoLetter.InfoLetterContentManager;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.accesscontrol.ComponentAccessControl;

import java.util.Date;
import java.util.Iterator;

/**
 * @author lbertin
 */
public class InfoLetterPublicationPdC extends InfoLetterPublication
    implements SilverContentInterface, SilverpeasContent {
  private static final long serialVersionUID = -2174573301215680444L;
  /**
   * icone d'une publication
   */
  private String iconUrl = "infoLetterSmall.gif";
  private static final String TYPE = "publication";

  private static final InfoLetterContentManager contentMgr = new InfoLetterContentManager();
  private String silverObjectId;
  private String positions;

  /**
   * Default constructor
   */
  public InfoLetterPublicationPdC() {
    super();
  }

  /**
   * Constructor from InfoLetterPublication
   * @param ilp InfoLetterPublication
   */
  public InfoLetterPublicationPdC(InfoLetterPublication ilp) {
    super(ilp.getPK(), ilp.getInstanceId(), ilp.getTitle(), ilp.getDescription(),
        ilp.getParutionDate(), ilp.getPublicationState(), ilp.getLetterId());
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
   * <p>
   * A user can access an information letter if it has enough rights to access the InfoLetter
   * instance in which is managed this letter.
   * @param user a user in Silverpeas.
   * @return true if the user can access this letter, false otherwise.
   */
  @Override
  public boolean canBeAccessedBy(final UserDetail user) {
    AccessController<String> accessController =
        AccessControllerProvider.getAccessController(ComponentAccessControl.class);
    return accessController.isUserAuthorized(user.getId(), getComponentInstanceId());
  }
}
