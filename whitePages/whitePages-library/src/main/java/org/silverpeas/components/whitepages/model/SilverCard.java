/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.whitepages.model;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.text.ParseException;
import java.util.Date;

public class SilverCard implements SilverpeasContent {

  private static final long serialVersionUID = 28853110916688897L;
  private final String id;
  private final String instanceId;
  private final String creatorId;
  private Date creationDate;
  private final String silverpeasContentId;

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
  public User getLastUpdater() {
    return getCreator();
  }

  @Override
  public Date getLastUpdateDate() {
    return getCreationDate();
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
}
