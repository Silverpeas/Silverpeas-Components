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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.community;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.user.model.User;

import java.util.ArrayList;

/**
 * Scope of a running integration test. It provides useful and utility methods for tests.
 * @author mmoquillon
 */
public class TestScope {

  public static ComponentInst newCommunityAppInstance() {
    ComponentInst componentInst = new ComponentInst();
    componentInst.setDomainFatherId("WA4");
    componentInst.setCreatorUserId(User.getCurrentUser().getId());
    componentInst.setName("community");
    componentInst.setLabel("WA4 Community");
    componentInst.setDescription("Community of users for space WA4");
    componentInst.setPublic(true);
    componentInst.setHidden(false);
    componentInst.setInheritanceBlocked(true);
    componentInst.setParameters(new ArrayList<>());
    return componentInst;
  }
}
  