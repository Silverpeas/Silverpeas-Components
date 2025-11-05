/*
 * Copyright (C) 2000 - 2025 Silverpeas
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

package org.silverpeas.components.community;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.admin.service.CommunityFactory;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Provider;
import org.silverpeas.kernel.annotation.NonNull;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Implementation of the {@link CommunityFactory}. It will automatically creates an instance of this
 * Silverpeas component for a given community space.
 *
 * @author mmoquillon
 */
@Provider
public class CommunityInstanceFactory implements CommunityFactory {

  @Override
  @NonNull
  public ComponentInst createCommunity(@NonNull SpaceInst communitySpace) {
    Objects.requireNonNull(communitySpace);
    String language = User.getCurrentUser().getUserPreferences().getLanguage();
    var bundle = CommunityComponentSettings.getMessagesIn(language);
    var maybeComponent = WAComponent.getByName(CommunityComponentSettings.COMPONENT_NAME);
    var component = maybeComponent.orElseThrow(() ->
        new IllegalStateException("The community application should be defined!"));

    ComponentInst membershipMgtApp = new ComponentInst();
    membershipMgtApp.setName(component.getName());
    membershipMgtApp.setDomainFatherId(communitySpace.getId());
    membershipMgtApp.setInheritanceBlocked(true);
    membershipMgtApp.setPublic(true);
    membershipMgtApp.setHidden(false);
    membershipMgtApp.setLabel(bundle.getString("community.membership.management.label"));
    membershipMgtApp.setDescription(bundle.getString("community.membership.management.desc"));
    membershipMgtApp.setOrderNum(0);
    membershipMgtApp.setParameters(new ArrayList<>(component.getParameters()));

    return membershipMgtApp;
  }
}
  