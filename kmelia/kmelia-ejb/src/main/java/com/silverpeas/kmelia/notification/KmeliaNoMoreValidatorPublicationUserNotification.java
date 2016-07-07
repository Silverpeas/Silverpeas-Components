/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package com.silverpeas.kmelia.notification;

import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.silverpeas.util.StringUtil.isDefined;

/**
 * @author Yohann Chastagnier
 */
public class KmeliaNoMoreValidatorPublicationUserNotification
    extends AbstractKmeliaActionPublicationUserNotification {

  public KmeliaNoMoreValidatorPublicationUserNotification(final NodePK nodePK,
      final PublicationDetail resource) {
    super(nodePK, resource, null);
  }

  @Override
  protected String getBundleSubjectKey() {
    return "kmelia.publication.validators.nomore";
  }

  @Override
  protected String getFileName() {
    return "notificationNoMoreValidator";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return Collections.singletonList(getMostRecentPublicationUpdater());
  }

  @Override
  protected String getSender() {
    return "-1";
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.PENDING_VALIDATION;
  }
}
