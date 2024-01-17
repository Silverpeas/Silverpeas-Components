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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.formsonline.notification;

import org.silverpeas.components.formsonline.model.FormDetail;
import org.silverpeas.components.formsonline.model.FormPK;
import org.silverpeas.components.formsonline.model.FormsOnlineException;
import org.silverpeas.components.formsonline.model.FormsOnlineService;
import org.silverpeas.core.notification.user.AbstractComponentInstanceManualUserNotification;
import org.silverpeas.core.notification.user.NotificationContext;
import org.silverpeas.core.notification.user.UserNotification;

import javax.inject.Named;

/**
 * @author neysseri
 */
@Named
public class FormsOnlineInstanceManualUserNotification extends
    AbstractComponentInstanceManualUserNotification {

  private static final String FORM_KEY = "FormDetailKey";

  @Override
  protected boolean check(final NotificationContext context) {
    final String formId = context.getContributionId();
    final String instanceId = context.getComponentId();
    try {
      final FormDetail form = FormsOnlineService.get().loadForm(new FormPK(formId, instanceId));
      context.put(FORM_KEY, form);
      return form.isPublished();
    } catch (FormsOnlineException e) {
      return false;
    }
  }

  @Override
  public UserNotification createUserNotification(final NotificationContext context) {
    final FormDetail form = context.getObject(FORM_KEY);
    context.put(NotificationContext.PUBLICATION_ID, form.getId());
    return new FormsOnlineUserAlertNotification(form, context.getSender()).build();
  }

}