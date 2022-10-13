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

package org.silverpeas.components.classifieds.notification;

import org.silverpeas.components.classifieds.model.ClassifiedDetail;
import org.silverpeas.core.NotFoundException;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.notification.user.AbstractComponentInstanceManualUserNotification;
import org.silverpeas.core.notification.user.NotificationContext;
import org.silverpeas.core.notification.user.UserNotification;

import javax.inject.Named;

import static org.silverpeas.components.classifieds.service.ClassifiedServiceProvider.getClassifiedService;

/**
 * @author silveryocha
 */
@Named
public class ClassifiedsInstanceManualUserNotification extends
    AbstractComponentInstanceManualUserNotification {

  private static final String CLASSIFIED_KEY = "ClassifiedDetailKey";

  @Override
  protected boolean check(final NotificationContext context) {
    final String classifiedId = context.getContributionId();
    final String instanceId = context.getComponentId();
    final ContributionIdentifier id = ContributionIdentifier.from(instanceId, classifiedId,
        ClassifiedDetail.getResourceType());
    final ClassifiedDetail classified = getClassifiedService().getContributionById(id)
        .orElseThrow(() -> new NotFoundException("No classified found with id " + id.asString()));
    context.put(CLASSIFIED_KEY, classified);
    return classified.canBeAccessedBy(context.getSender());
  }

  @Override
  public UserNotification createUserNotification(final NotificationContext context) {
    final ClassifiedDetail classified = context.getObject(CLASSIFIED_KEY);
    return new ClassifiedSimpleNotification(classified, context.getSender()).build();
  }
}
