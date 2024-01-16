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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia.notification;

import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.util.StringUtil;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Yohann Chastagnier
 */
public class KmeliaNoMoreValidatorPublicationUserNotification
    extends AbstractKmeliaActionPublicationUserNotification {

  private String userToNotify;

  public KmeliaNoMoreValidatorPublicationUserNotification(final NodePK nodePK,
      final PublicationDetail resource) {
    super(nodePK, resource, null);
  }

  public KmeliaNoMoreValidatorPublicationUserNotification(final NodePK nodePK,
      final PublicationDetail resource, final String userToNotify) {
    super(nodePK, resource, null);
    this.userToNotify = userToNotify;
  }

  @Override
  protected String getBundleSubjectKey() {
    return "kmelia.publication.validators.nomore";
  }

  @Override
  protected String getTemplateFileName() {
    if (StringUtil.isDefined(userToNotify)) {
      return "notificationNoMoreValidatorToNonContributor";
    }
    return "notificationNoMoreValidator";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    if (StringUtil.isDefined(userToNotify)) {
      return Collections.singletonList(userToNotify);
    }
    return Collections.singletonList(getMostRecentPublicationUpdater());
  }

  @Override
  protected String getSender() {
    return "";
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.PENDING_VALIDATION;
  }
}