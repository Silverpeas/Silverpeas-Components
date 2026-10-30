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
package org.silverpeas.components.discussion.web;

import java.util.Map;
import jakarta.inject.Inject;
import org.silverpeas.components.discussion.service.DiscussionAccess;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.annotation.Service;

@Service
public class DiscussionInstanceInfoService {
  @Inject
  private OrganizationController organization;
  @Inject
  private DiscussionAccess access;

  public Map<String, String> info(String instanceId, String language) {
    access.require(instanceId, SilverpeasRole.WRITER);
    var component = organization.getComponentInstLight(instanceId);
    return Map.of("title", component.getLabel(language), "description",
        component.getDescription(language) == null ? "" : component.getDescription(language));
  }
}
