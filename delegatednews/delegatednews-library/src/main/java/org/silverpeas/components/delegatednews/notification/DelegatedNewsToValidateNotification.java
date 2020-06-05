/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.components.delegatednews.notification;

import org.silverpeas.components.delegatednews.model.DelegatedNews;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.util.URLUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class DelegatedNewsToValidateNotification extends AbstractDelegatedNewsUserNotification {

  private final String[] usersToBeNotified;
  private final String delegatedNewsComponentId;

  public DelegatedNewsToValidateNotification(final DelegatedNews delegatedNews, final User user,
      final String[] usersToBeNotified, String delegatedNewsComponentId) {
    super(delegatedNews, user);
    this.usersToBeNotified = usersToBeNotified;
    this.delegatedNewsComponentId = delegatedNewsComponentId;
  }

  @Override
  protected String getTemplateFileName() {
    return "delegatednewsNotificationToValidate";
  }

  @Override
  protected String getBundleSubjectKey() {
    return "delegatednews.newsSuggest";
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "delegatednews.notif.tovalidate.link.label";
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.PENDING_VALIDATION;
  }

  @Override
  protected String getResourceURL(final DelegatedNews delegatedNews) {
    return URLUtil.getSimpleURL(URLUtil.URL_COMPONENT, delegatedNewsComponentId, false);
  }

  @Override
  protected String getComponentInstanceId() {
    return delegatedNewsComponentId;
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    if (usersToBeNotified == null) {
      return Collections.emptyList();
    }
    return new ArrayList<>(Arrays.asList(usersToBeNotified));
  }
}