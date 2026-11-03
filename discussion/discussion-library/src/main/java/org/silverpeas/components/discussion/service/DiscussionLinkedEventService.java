/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.components.discussion.service;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.net.URI;
import java.util.Optional;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.repository.CalendarEventRepository;
import org.silverpeas.core.util.URLUtil;

/** Message deletion and native Almanach cancellation participate in the same transaction. */
@Service
@Transactional(Transactional.TxType.MANDATORY)
public class DiscussionLinkedEventService {
  @Inject
  private CalendarEventRepository events;

  public DiscussionEventDeletion describe(String body) {
    return resolve(body).map(event -> {
      var user = User.getCurrentRequester();
      boolean editable = event.canBeModifiedBy(user);
      boolean visible = editable || event.canBeAccessedBy(user);
      return new DiscussionEventDeletion(true, editable, visible ? event.getTitle() : "", visible && event.isRecurrent());
    }).orElseGet(DiscussionEventDeletion::none);
  }

  public void delete(String body) {
    resolve(body).ifPresent(event -> {
      if (!event.canBeModifiedBy(User.getCurrentRequester())) {
        throw new SecurityException("Almanach event deletion denied");
      }
      event.delete();
    });
  }

  private Optional<CalendarEvent> resolve(String body) {
    return DiscussionEventReference.find(body, URI.create(URLUtil.getAbsoluteApplicationURL()))
        .flatMap(reference -> Optional.ofNullable(events.getById(reference.getLocalId()))
            .filter(event -> reference.getComponentInstanceId().equals(event.getCalendar().getComponentInstanceId())));
  }
}
