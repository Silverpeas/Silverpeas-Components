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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.suggestionbox.notification;

import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.components.suggestionbox.model.Suggestion;

import java.util.Collection;

/**
 * @author Yohann Chastagnier
 */
public class SuggestionPendingValidationUserNotification
    extends AbstractSuggestionActionUserNotification {

  public SuggestionPendingValidationUserNotification(final Suggestion resource) {
    super(resource, NotifAction.PENDING_VALIDATION);
  }

  @Override
  protected void perform(final Suggestion resource) {
    super.perform(resource);
    getNotificationMetaData().displayReceiversInFooter();
  }

  @Override
  protected String getBundleSubjectKey() {
    return "suggestionBox.suggestion.notification.pendingValidation.subject";
  }

  @Override
  protected String getTemplateFileName() {
    return "pendingValidationNotification";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return getSuggestionBoxModerators();
  }
}
