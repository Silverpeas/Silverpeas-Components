/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.components.kmelia.notification;

import static org.silverpeas.core.util.StringUtil.isDefined;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;

/**
 * @author Yohann Chastagnier
 */
public class KmeliaValidationPublicationUserNotification extends AbstractKmeliaActionPublicationUserNotification {

  private final String refusalMotive;
  private final String userIdWhoRefuse;

  public KmeliaValidationPublicationUserNotification(final NodePK nodePK, final PublicationDetail resource,
      final String refusalMotive,
      final String userIdWhoRefuse) {
    super(nodePK, resource, null);
    this.refusalMotive = refusalMotive;
    this.userIdWhoRefuse = userIdWhoRefuse;
  }

  @Override
  protected String getBundleSubjectKey() {
    if (!isDefined(refusalMotive)) {
      return "PublicationValidated";
    }
    return "PublicationRefused";
  }

  @Override
  protected String getTemplateFileName() {
    if (!isDefined(refusalMotive)) {
      return "notificationValidation";
    }
    return "notificationRefused";
  }

  @Override
  protected void perform(final PublicationDetail resource) {
    super.perform(resource);
    getNotificationMetaData().setOriginalExtraMessage(refusalMotive);
  }

  @Override
  protected void performTemplateData(final String language, final PublicationDetail resource,
      final SilverpeasTemplate template) {
    super.performTemplateData(language, resource, template);
    template.setAttribute("refusalMotive", refusalMotive);
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    final Set<String> userIds = new HashSet<>();
    if (isDefined(getResource().getUpdaterId())) {
      userIds.add(getResource().getUpdaterId());
    }
    return userIds;
  }

  @Override
  protected String getSender() {
    return userIdWhoRefuse;
  }

  @Override
  protected NotifAction getAction() {
    if (!isDefined(refusalMotive)) {
      return NotifAction.VALIDATE;
    }
    return NotifAction.REFUSE;
  }
}
