/*
 * Copyright (C) 2000 - 2021 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.yellowpages;

import org.silverpeas.components.yellowpages.service.YellowpagesService;
import org.silverpeas.core.admin.user.notification.GroupEvent;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.notification.system.CDIResourceEventListener;

import javax.inject.Inject;

/**
 * A listener of events coming from the changes operating on the user groups.
 * @author miguel
 */
@Bean
public class GroupEventListener extends CDIResourceEventListener<GroupEvent> {

  @Inject
  private YellowpagesService yellowpagesService;

  /**
   * An event on the deletion of a group has be listened. A deleted group is then nonexistent and
   * nonrecoverable.
   * In that case, as a group can be referred by a yellowpages instance (as a group
   * of inner contacts), take caution to remove also this group from the yellowpages instance.
   * @param event the event on the deletion of a group.
   * @throws Exception if an error occurs while treating the event.
   */
  @Override
  public void onDeletion(final GroupEvent event) throws Exception {
    yellowpagesService.removeGroup(event.getTransition().getBefore().getId());
  }
}
