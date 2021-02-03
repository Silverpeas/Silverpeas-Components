/*
 * Copyright (C) 2000 - 2021 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
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

import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.silverpeas.core.notification.user.AbstractComponentInstanceManualUserNotification;
import org.silverpeas.core.notification.user.NotificationContext;
import org.silverpeas.core.notification.user.UserNotification;

import javax.inject.Named;

/**
 * @author silveryocha
 */
@Named
public class SuggestionBoxInstanceManualUserNotification
    extends AbstractComponentInstanceManualUserNotification {

  private static final String SUGGESTION_KEY = "SuggestionBoxKey";

  @Override
  protected boolean check(final NotificationContext context) {
    final String boxId = context.getComponentId();
    final String suggestionId = context.getContributionId();
    final SuggestionBox box = SuggestionBox.getByComponentInstanceId(boxId);
    final Suggestion suggestion = box.getSuggestions().get(suggestionId);
    context.put(SUGGESTION_KEY, suggestion);
    return suggestion.canBeAccessedBy(context.getSender());
  }

  @Override
  public UserNotification createUserNotification(final NotificationContext context) {
    final Suggestion suggestion = context.getObject(SUGGESTION_KEY);
    return new SuggestionNotifyManuallyUserNotification(suggestion, context.getSender()).build();
  }
}
