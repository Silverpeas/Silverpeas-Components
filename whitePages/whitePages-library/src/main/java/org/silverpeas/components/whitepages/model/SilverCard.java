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

package org.silverpeas.components.whitepages.model;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.security.authorization.AccessController;
import org.silverpeas.core.security.authorization.AccessControllerProvider;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.text.ParseException;
import java.util.Date;

public class SilverCard implements SilverpeasContent {

  private static final long serialVersionUID = 28853110916688897L;
  private String id;
  private String instanceId;
  private String creatorId;
  private Date creationDate;
  private String silverpeasContentId;

  public SilverCard(Card card, int silverContentId) {
    id = card.getPK().getId();
    instanceId = card.getInstanceId();
    creatorId = Integer.toString(card.getCreatorId());
    try {
      creationDate = DateUtil.parse(card.getCreationDate());
    } catch (ParseException ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
    silverpeasContentId = Integer.toString(silverContentId);
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getComponentInstanceId() {
    return instanceId;
  }

  @Override
  public String getSilverpeasContentId() {
    return silverpeasContentId;
  }

  @Override
  public User getCreator() {
    return User.getById(creatorId);
  }

  @Override
  public Date getCreationDate() {
    return creationDate;
  }

  @Override
  public String getTitle() {
    return null;
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public String getContributionType() {
    return null;
  }

  /**
   * Is the specified user can access this card?
   * <p/>
   * A user can access a card if it has enough rights to access the WhitePages instance in
   * which is managed this card.
   * @param user a user in Silverpeas.
   * @return true if the user can access this card, false otherwise.
   */
  @Override
  public boolean canBeAccessedBy(final User user) {
    AccessController<String> accessController = AccessControllerProvider
        .getAccessController(ComponentAccessControl.class);
    return accessController.isUserAuthorized(user.getId(), getComponentInstanceId());
  }
}
