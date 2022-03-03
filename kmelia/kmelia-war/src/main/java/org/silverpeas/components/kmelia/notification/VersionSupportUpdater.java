/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.kmelia.notification;

import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.notification.ComponentInstanceEvent;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.notification.system.CDIResourceEventListener;
import org.silverpeas.core.notification.system.StateTransition;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Inject;

/**
 * Listens for events about the update of component instances. When this component instance is a
 * Kmelia one and it is about an update of the versionning parameter, then applies this change to
 * all attachments of the publications managed by this instance of Kmelia.
 * @author mmoquillon
 */
@Bean
public class VersionSupportUpdater extends CDIResourceEventListener<ComponentInstanceEvent> {

  @Inject
  private AttachmentService service;
  @Inject
  private Administration administration;

  @Override
  public void onUpdate(final ComponentInstanceEvent event) throws Exception {
    StateTransition<ComponentInst> transition = event.getTransition();
    String instanceType = event.getTransition().getBefore().getName();
    if ("kmelia".equalsIgnoreCase(instanceType)) {
      ComponentInst previousInst = transition.getBefore();
      ComponentInst updatedInst = transition.getAfter();
      if (isVersionParameterChanged(previousInst, updatedInst)) {
        boolean versioned = StringUtil
            .getBooleanValue(updatedInst.getParameterValue(AttachmentService.VERSION_MODE));
        boolean alwaysVisible = StringUtil
            .getBooleanValue(administration.getComponentParameterValue(previousInst.getId(),
                "publicationAlwaysVisible"));
        if (!alwaysVisible || !versioned) {
          service.switchComponentBehaviour(updatedInst.getId(), versioned);
        }
      }
    }
  }

  private boolean isVersionParameterChanged(ComponentInst previousInst, ComponentInst actualInst) {
    return !previousInst.getParameterValue(AttachmentService.VERSION_MODE)
        .equals(actualInst.getParameterValue(AttachmentService.VERSION_MODE));
  }
}
