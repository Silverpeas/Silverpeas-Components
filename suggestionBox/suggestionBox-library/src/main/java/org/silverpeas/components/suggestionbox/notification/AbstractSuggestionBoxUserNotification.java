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
package org.silverpeas.components.suggestionbox.notification;

import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.util.CollectionUtil;

import java.util.Collection;
import java.util.Collections;

/**
 * @param <T> the type of resource concerned by the notification.
 * @author Yohann Chastagnier
 */
public abstract class AbstractSuggestionBoxUserNotification<T>
    extends AbstractTemplateUserNotificationBuilder<T> {

  public AbstractSuggestionBoxUserNotification(final T resource) {
    super(resource);
  }

  @Override
  protected String getLocalizationBundlePath() {
    return "org.silverpeas.components.suggestionbox.multilang.SuggestionBoxBundle";
  }

  @Override
  protected String getTemplatePath() {
    return "suggestionbox";
  }

  /**
   * Gets the list of identifier of users that are moderators on the suggestion box.
   * @return identifier array of users.
   */
  protected Collection<String> getSuggestionBoxModerators() {
    return CollectionUtil.asList(OrganizationControllerProvider.getOrganisationController()
        .getUsersIdsByRoleNames(getComponentInstanceId(),
            Collections.singletonList(SilverpeasRole.admin.name())));
  }


  /**
   * Gets the name of the sender.
   * @return
   */
  protected String getSenderName() {
    User sender = getSenderDetail();
    if (sender != null) {
      return sender.getDisplayedName();
    }
    return getSender();
  }

  /**
   * Gets the {@link UserDetail} instance of the sender.
   * @return
   */
  protected abstract User getSenderDetail();

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "suggestionBox.notifSuggestionLinkLabel";
  }
}
