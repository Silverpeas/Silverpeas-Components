/*
 * Copyright (C) 2000 - 2014 Silverpeas
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

import com.silverpeas.util.template.SilverpeasTemplate;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import org.silverpeas.components.suggestionbox.model.Suggestion;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Yohann Chastagnier
 */
public class SuggestionValidationUserNotification extends AbstractSuggestionActionUserNotification {

  public SuggestionValidationUserNotification(final Suggestion resource) {
    super(resource, null);
  }

  @Override
  protected String getBundleSubjectKey() {
    return getResource().getValidation().isValidated() ?
        "suggestionBox.suggestion.notification.validated.subject" :
        "suggestionBox.suggestion.notification.refused.subject";
  }

  @Override
  protected String getFileName() {
    return getResource().getValidation().isValidated() ? "validatedNotification" :
        "refusedNotification";
  }

  @Override
  protected void perform(final Suggestion resource) {
    super.perform(resource);
    getNotificationMetaData().setOriginalExtraMessage(resource.getValidation().getComment());
  }

  @Override
  protected void performTemplateData(final String language, final Suggestion resource,
      final SilverpeasTemplate template) {
    super.performTemplateData(language, resource, template);
    template.setAttribute("validationComment", resource.getValidation().getComment());
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return Collections.singleton(getResource().getCreatedBy());
  }

  @Override
  protected String getSender() {
    return getResource().getValidation().getValidator().getId();
  }

  @Override
  protected NotifAction getAction() {
    return getResource().getValidation().isValidated() ? NotifAction.VALIDATE : NotifAction.REFUSE;
  }
}
